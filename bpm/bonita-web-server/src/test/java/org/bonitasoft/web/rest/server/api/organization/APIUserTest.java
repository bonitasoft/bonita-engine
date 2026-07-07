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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class APIUserTest {

    public static final APIID USER_ID = APIID.makeAPIID(123L);
    @Mock
    private UserItem userItem;
    private APIUser apiUser;
    @Mock
    private UserDatastore userDatastore;

    @BeforeEach
    void before() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(mock(ItemDefinitionFactory.class));
        apiUser = spy(new APIUser());
        // lenient: these are shared fixtures - password tests bypass the datastore, deploy/icon tests
        // bypass the validator, so not every test consumes every stub.
        lenient().doReturn(userDatastore).when(apiUser).getDefaultDatastore();
        lenient().doReturn(TestValidator.class.getName()).when(apiUser).getValidatorClassName();
        I18n.getInstance();
        APIServletCall caller = mock(APIServletCall.class);
        apiUser.setCaller(caller);
        lenient().doReturn("en_US").when(caller).getLocale();
    }

    @Test
    void should_user_be_updated_with_icon_as_submit_by_the_API() throws Exception {
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
    void should_not_update_password_if_empty() throws Exception {
        apiUser.update(USER_ID, map(UserItem.ATTRIBUTE_PASSWORD, ""));

        verify(userDatastore).update(eq(USER_ID), eq(Collections.<String, String> emptyMap()));
    }

    @Test
    void should_check_password_robustness_on_update() {
        //see TestValidator class
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> apiUser.update(USER_ID,
                        map(UserItem.ATTRIBUTE_PASSWORD,
                                "this password is not accepted by the TestValidator validator")))
                .withMessageContaining("the validator TestValidator rejected this password");
    }

    @Test
    void should_update_password_if_valid() throws Exception {
        apiUser.update(USER_ID, map(UserItem.ATTRIBUTE_PASSWORD, "accepted password"));

        verify(userDatastore).update(eq(USER_ID), eq(map(UserItem.ATTRIBUTE_PASSWORD, "accepted password")));
    }

    @Test
    void should_check_password_robustness_on_add() {
        UserItem userItem = new UserItem();
        userItem.setUserName("John");
        userItem.setPassword("this password is not accepted by the TestValidator validator");

        //see TestValidator class
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> apiUser.add(userItem))
                .withMessageContaining("the validator TestValidator rejected this password");
    }

    @Test
    void should_throw_exception_when_adding_a_user_with_no_password() {
        UserItem userItem = new UserItem();
        userItem.setUserName("John");

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> apiUser.add(userItem));
    }

    @Test
    void should_add_user_if_password_is_valid() throws Exception {
        UserItem userItem = new UserItem();
        userItem.setUserName("John");
        userItem.setPassword("accepted password");

        apiUser.add(userItem);
    }

    @Test
    void fillDeploys_should_skip_manager_and_created_by_deploys_when_users_no_longer_exist() {
        // Given a user whose manager and creator were both deleted from the organization
        final APIID deletedManagerId = APIID.makeAPIID(7L);
        final APIID deletedCreatorId = APIID.makeAPIID(8L);
        doReturn("7").when(userItem).getAttributeValue(UserItem.ATTRIBUTE_MANAGER_ID);
        doReturn("8").when(userItem).getAttributeValue(UserItem.ATTRIBUTE_CREATED_BY_USER_ID);
        doReturn(deletedManagerId).when(userItem).getManagerId();
        doReturn(deletedCreatorId).when(userItem).getCreatedByUserId();

        final List<String> deploys = Arrays.asList(UserItem.ATTRIBUTE_MANAGER_ID,
                UserItem.ATTRIBUTE_CREATED_BY_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("manager deleted")))
                .when(userDatastore).get(deletedManagerId);
        doThrow(new APINotFoundException(new UserNotFoundException("creator deleted")))
                .when(userDatastore).get(deletedCreatorId);

        // When the unresolvable references must not fail the whole user list
        assertThatCode(() -> apiUser.fillDeploys(userItem, deploys)).doesNotThrowAnyException();

        // Then both deploys are skipped (left empty)
        verify(userItem, never()).setDeploy(eq(UserItem.ATTRIBUTE_MANAGER_ID), any(Item.class));
        verify(userItem, never()).setDeploy(eq(UserItem.ATTRIBUTE_CREATED_BY_USER_ID), any(Item.class));
    }

}
