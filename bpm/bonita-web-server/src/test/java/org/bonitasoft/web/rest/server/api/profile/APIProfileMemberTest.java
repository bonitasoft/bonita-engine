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
package org.bonitasoft.web.rest.server.api.profile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.model.identity.RoleItem;
import org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem;
import org.bonitasoft.web.rest.model.portal.profile.ProfileMemberItem;
import org.bonitasoft.web.rest.server.api.deployer.DeployerFactory;
import org.bonitasoft.web.rest.server.datastore.organization.GroupDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.RoleDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.Deployer;
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

/**
 * Covers the dangling-reference tolerance of {@link AbstractAPIMember}, exercised here through its concrete
 * subclass {@code APIProfileMember}. The {@code user_id} / {@code role_id} / {@code group_id} deploys live in
 * the abstract base, which {@code APIProfileMember#fillDeploys} delegates to via {@code super.fillDeploys}.
 */
@ExtendWith(MockitoExtension.class)
class APIProfileMemberTest {

    @Mock
    private UserDatastore userDatastore;

    @Mock
    private RoleDatastore roleDatastore;

    @Mock
    private GroupDatastore groupDatastore;

    @Mock
    private DeployerFactory deployerFactory;

    private APIProfileMember apiProfileMember;

    @BeforeAll
    static void initEnvironment() {
        I18n.getInstance();
    }

    @BeforeEach
    void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiProfileMember = spy(new APIProfileMember());
        // APIProfileMember#fillDeploys registers a profile deployer before delegating to the abstract base;
        // stub the factory so that registration is inert and the test focuses on the user/role/group deploys.
        doReturn(deployerFactory).when(apiProfileMember).getDeployerFactory();
        doReturn(mock(Deployer.class)).when(deployerFactory)
                .createProfileDeployer(ProfileMemberItem.ATTRIBUTE_PROFILE_ID);
    }

    @Test
    void fillDeploys_should_skip_user_deploy_and_keep_other_deploys_when_user_no_longer_exists() {
        // Given a profile member whose user was deleted from the organization, but whose role and group still exist
        doReturn(userDatastore).when(apiProfileMember).getUserDatastore();
        doReturn(roleDatastore).when(apiProfileMember).getRoleDatastore();
        doReturn(groupDatastore).when(apiProfileMember).getGroupDatastore();

        final APIID deletedUserId = APIID.makeAPIID(3L);
        final APIID roleId = APIID.makeAPIID(4L);
        final APIID groupId = APIID.makeAPIID(5L);
        final ProfileMemberItem item = mock(ProfileMemberItem.class);
        doReturn("3").when(item).getAttributeValue(AbstractMemberItem.ATTRIBUTE_USER_ID);
        doReturn("4").when(item).getAttributeValue(AbstractMemberItem.ATTRIBUTE_ROLE_ID);
        doReturn("5").when(item).getAttributeValue(AbstractMemberItem.ATTRIBUTE_GROUP_ID);
        doReturn(deletedUserId).when(item).getUserId();
        doReturn(roleId).when(item).getRoleId();
        doReturn(groupId).when(item).getGroupId();

        final List<String> deploys = Arrays.asList(AbstractMemberItem.ATTRIBUTE_USER_ID,
                AbstractMemberItem.ATTRIBUTE_ROLE_ID, AbstractMemberItem.ATTRIBUTE_GROUP_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);
        final RoleItem roleItem = new RoleItem();
        doReturn(roleItem).when(roleDatastore).get(roleId);
        final GroupItem groupItem = new GroupItem();
        doReturn(groupItem).when(groupDatastore).get(groupId);

        // When the unresolvable user must not fail the whole member list
        assertThatCode(() -> apiProfileMember.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the user deploy is skipped, but the role and group deploys are still applied
        verify(item, never()).setDeploy(eq(AbstractMemberItem.ATTRIBUTE_USER_ID), any(Item.class));
        verify(item).setDeploy(AbstractMemberItem.ATTRIBUTE_ROLE_ID, roleItem);
        verify(item).setDeploy(AbstractMemberItem.ATTRIBUTE_GROUP_ID, groupItem);
    }
}
