/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCommentItem;
import org.bonitasoft.web.rest.model.bpm.cases.CommentItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class APIArchivedCommentTest {

    @Mock
    private UserDatastore userDatastore;

    private APIArchivedComment apiArchivedComment;

    @BeforeAll
    static void initEnvironment() {
        I18n.getInstance();
    }

    @BeforeEach
    void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiArchivedComment = spy(new APIArchivedComment());
    }

    @Test
    void fillDeploys_should_fill_author_when_deploy_of_user_is_active() {
        // Given an archived comment whose author still exists in the organization
        doReturn(userDatastore).when(apiArchivedComment).getUserDatastore();
        final APIID userId = APIID.makeAPIID(3L);
        final ArchivedCommentItem item = mock(ArchivedCommentItem.class);
        doReturn(ArchivedCommentItem.ATTRIBUTE_USER_ID).when(item)
                .getAttributeValue(ArchivedCommentItem.ATTRIBUTE_USER_ID);
        doReturn(userId).when(item).getUserId();

        final List<String> deploys = List.of(ArchivedCommentItem.ATTRIBUTE_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(userId);

        // When
        apiArchivedComment.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(ArchivedCommentItem.ATTRIBUTE_USER_ID, userItem);
    }

    @Test
    void fillDeploys_should_fill_system_user_when_deploy_of_user_is_not_active() {
        // Given a system comment (no author to resolve)
        final ArchivedCommentItem item = mock(ArchivedCommentItem.class);
        final List<String> deploys = new ArrayList<>();

        // When
        apiArchivedComment.fillDeploys(item, deploys);

        // Then the placeholder "System" user is deployed
        final ArgumentCaptor<UserItem> captor = ArgumentCaptor.forClass(UserItem.class);
        verify(item).setDeploy(eq(CommentItem.ATTRIBUTE_USER_ID), captor.capture());
        assertThat(captor.getValue().getUserName()).isEqualTo("System");
    }

    @Test
    void fillDeploys_should_skip_user_deploy_when_author_no_longer_exists() {
        // Given an archived comment whose author was deleted from the organization
        doReturn(userDatastore).when(apiArchivedComment).getUserDatastore();
        final APIID deletedUserId = APIID.makeAPIID(3L);
        final ArchivedCommentItem item = mock(ArchivedCommentItem.class);
        doReturn(ArchivedCommentItem.ATTRIBUTE_USER_ID).when(item)
                .getAttributeValue(ArchivedCommentItem.ATTRIBUTE_USER_ID);
        doReturn(deletedUserId).when(item).getUserId();

        final List<String> deploys = List.of(ArchivedCommentItem.ATTRIBUTE_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        // When the unresolvable author must not fail the whole archived comment list
        assertThatCode(() -> apiArchivedComment.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the user deploy is skipped (left empty), and the System fallback is NOT used either
        verify(item, never()).setDeploy(eq(ArchivedCommentItem.ATTRIBUTE_USER_ID), any(Item.class));
    }
}
