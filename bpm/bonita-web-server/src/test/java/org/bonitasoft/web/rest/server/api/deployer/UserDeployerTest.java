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
package org.bonitasoft.web.rest.server.api.deployer;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Vincent Elcrin
 */
public class UserDeployerTest extends APITestWithMock {

    @Mock
    private DatastoreHasGet<UserItem> getter;

    @Before
    public void setUp() {
        initMocks(this);
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
    }

    @Test
    public void testDeployableAttributeIsDeployed() {
        UserItem user = prepareGetterToReturnAUser();
        GroupItem group = aGroupInstalledBy(APIID.makeAPIID(6L));

        UserDeployer installedByDeployer = new UserDeployer(getter, GroupItem.ATTRIBUTE_CREATED_BY_USER_ID);
        installedByDeployer.deployIn(group);

        assertEquals(user, group.getCreatedByUser());
    }

    @Test
    public void testStringValueIsDeployed() {
        prepareGetterToReturnAUser();
        GroupItem group = spy(aGroupInstalledBy(APIID.makeAPIID("unusedId")));

        UserDeployer nameDeployer = new UserDeployer(getter, GroupItem.ATTRIBUTE_CREATED_BY_USER_ID);
        nameDeployer.deployIn(group);

        verify(group, times(1)).setDeploy(any(), any());
    }

    @Test
    public void testCompoundLongValueIsDeployed() {
        prepareGetterToReturnAUser();
        GroupItem group = spy(aGroupInstalledBy(APIID.makeAPIID(3L, -1L, null)));

        UserDeployer nameDeployer = new UserDeployer(getter, GroupItem.ATTRIBUTE_CREATED_BY_USER_ID);
        nameDeployer.deployIn(group);

        verify(group, times(1)).setDeploy(any(), any());
    }

    @Test
    public void testCompoundStringValueIsDeployed() {
        prepareGetterToReturnAUser();
        GroupItem group = spy(aGroupInstalledBy(APIID.makeAPIID("3", "-1", null, "unusedId")));

        UserDeployer nameDeployer = new UserDeployer(getter, GroupItem.ATTRIBUTE_CREATED_BY_USER_ID);
        nameDeployer.deployIn(group);

        verify(group, times(1)).setDeploy(any(), any());
    }

    @Test
    public void testNotDeployableAttributeIsNotDeployed() {
        prepareGetterToReturnAUser();
        GroupItem group = spy(aGroupInstalledBy(null));

        UserDeployer nameDeployer = new UserDeployer(getter, GroupItem.ATTRIBUTE_CREATED_BY_USER_ID);
        nameDeployer.deployIn(group);

        verify(group, never()).setDeploy(any(), any());
    }

    @Test
    public void testNegativeStringValueIsNotDeployed() {
        prepareGetterToReturnAUser();
        GroupItem group = spy(aGroupInstalledBy(APIID.makeAPIID("-1")));

        UserDeployer nameDeployer = new UserDeployer(getter, GroupItem.ATTRIBUTE_CREATED_BY_USER_ID);
        nameDeployer.deployIn(group);

        verify(group, never()).setDeploy(any(), any());
    }

    @Test
    public void testNegativeLongValueIsNotDeployed() {
        prepareGetterToReturnAUser();
        GroupItem group = spy(aGroupInstalledBy(APIID.makeAPIID(-1L)));

        UserDeployer nameDeployer = new UserDeployer(getter, GroupItem.ATTRIBUTE_CREATED_BY_USER_ID);
        nameDeployer.deployIn(group);

        verify(group, never()).setDeploy(any(), any());
    }

    private UserItem prepareGetterToReturnAUser() {
        UserItem user = new UserItem();
        doReturn(user).when(getter).get(any(APIID.class));
        return user;
    }

    private GroupItem aGroupInstalledBy(APIID userId) {
        GroupItem item = new GroupItem();
        item.setCreatedByUserId(userId);
        return item;
    }

}
