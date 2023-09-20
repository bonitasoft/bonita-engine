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

import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class APIUserTest {

    public static final APIID USER_ID = APIID.makeAPIID(123L);
    @Mock
    private UserItem userItem;
    private APIUser apiUser;
    @Mock
    private UserDatastore userDatastore;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(mock(ItemDefinitionFactory.class));
        apiUser = spy(new APIUser());
        doReturn(userDatastore).when(apiUser).getDefaultDatastore();
        doReturn(TestValidator.class.getName()).when(apiUser).getValidatorClassName();
        I18n.getInstance();
        APIServletCall caller = mock(APIServletCall.class);
        apiUser.setCaller(caller);
        doReturn("en_US").when(caller).getLocale();
    }

    @Test
    public void should_user_be_updated_with_icon_as_submit_by_the_API() throws Exception {
        //when
        apiUser.update(USER_ID, map(UserItem.ATTRIBUTE_ICON, "theAvatar.jpg"));
        //then
        verify(userDatastore).update(eq(USER_ID), eq(map(UserItem.ATTRIBUTE_ICON, "theAvatar.jpg")));
    }

    private HashMap<String, String> map(String key, String value) {
        HashMap<String, String> item = new HashMap<>();
        item.put(key, value);
        return item;
    }

    @Test
    public void should_not_update_password_if_empty() throws Exception {
        apiUser.update(USER_ID, map(UserItem.ATTRIBUTE_PASSWORD, ""));

        verify(userDatastore).update(eq(USER_ID), eq(Collections.<String, String> emptyMap()));
    }

    @Test
    public void should_check_password_robustness_on_update() throws Exception {
        expectedException.expect(ValidationException.class);
        //see TestValidator class
        expectedException.expectMessage("the validator TestValidator rejected this password");

        apiUser.update(USER_ID,
                map(UserItem.ATTRIBUTE_PASSWORD, "this password is not accepted by the TestValidator validator"));
    }

    @Test
    public void should_update_password_if_valid() throws Exception {
        apiUser.update(USER_ID, map(UserItem.ATTRIBUTE_PASSWORD, "accepted password"));

        verify(userDatastore).update(eq(USER_ID), eq(map(UserItem.ATTRIBUTE_PASSWORD, "accepted password")));
    }

    @Test
    public void should_check_password_robustness_on_add() throws Exception {
        UserItem userItem = new UserItem();
        userItem.setUserName("John");
        userItem.setPassword("this password is not accepted by the TestValidator validator");

        expectedException.expect(ValidationException.class);
        //see TestValidator class
        expectedException.expectMessage("the validator TestValidator rejected this password");
        apiUser.add(userItem);
    }

    @Test
    public void should_throw_exception_when_adding_a_user_with_no_password() throws Exception {
        UserItem userItem = new UserItem();
        userItem.setUserName("John");

        expectedException.expect(ValidationException.class);
        apiUser.add(userItem);
    }

    @Test
    public void should_add_user_if_password_is_valid() throws Exception {
        UserItem userItem = new UserItem();
        userItem.setUserName("John");
        userItem.setPassword("accepted password");

        apiUser.add(userItem);
    }

}
