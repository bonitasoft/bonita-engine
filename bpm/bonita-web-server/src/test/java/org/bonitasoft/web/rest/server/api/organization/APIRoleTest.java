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
package org.bonitasoft.web.rest.server.api.organization;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.identity.RoleItem;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class APIRoleTest {

    @Mock
    private UserDatastore userDatastore;

    private APIRole apiRole;

    @BeforeAll
    static void initEnvironment() {
        I18n.getInstance();
    }

    @BeforeEach
    void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiRole = spy(new APIRole());
    }

    @Test
    void fillDeploys_should_fill_created_by_when_user_is_active() {
        // Given a role whose creator still exists in the organization
        doReturn(userDatastore).when(apiRole).getUserDatastore();
        final APIID userId = APIID.makeAPIID(3L);
        final RoleItem item = mock(RoleItem.class);
        doReturn("3").when(item).getAttributeValue(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID);
        doReturn(userId).when(item).getCreatedByUserId();

        final List<String> deploys = List.of(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(userId);

        // When
        apiRole.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID, userItem);
    }

    @Test
    void fillDeploys_should_skip_created_by_deploy_when_user_no_longer_exists() {
        // Given a role whose creator was deleted from the organization
        doReturn(userDatastore).when(apiRole).getUserDatastore();
        final APIID deletedUserId = APIID.makeAPIID(3L);
        final RoleItem item = mock(RoleItem.class);
        doReturn("3").when(item).getAttributeValue(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID);
        doReturn(deletedUserId).when(item).getCreatedByUserId();

        final List<String> deploys = List.of(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        // When the unresolvable creator must not fail the whole role list
        assertThatCode(() -> apiRole.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the created-by deploy is skipped (left empty)
        verify(item, never()).setDeploy(eq(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID), any(Item.class));
    }
}
