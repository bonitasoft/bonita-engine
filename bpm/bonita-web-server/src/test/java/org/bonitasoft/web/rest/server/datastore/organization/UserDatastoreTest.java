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
package org.bonitasoft.web.rest.server.datastore.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.IconDescriptor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.identity.impl.UserImpl;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.engineclient.ProcessEngineClient;
import org.bonitasoft.web.rest.server.engineclient.UserEngineClient;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class UserDatastoreTest {

    @Mock
    private ProcessAPI processAPI;
    @Mock
    private ProcessEngineClient processEngineClient;
    @Mock
    private IdentityAPI identityAPI;
    @Mock
    UserItemConverter userItemConverter;
    @Mock
    private BonitaHomeFolderAccessor bonitaHomeFolderAccessor;
    @Spy
    @InjectMocks
    private UserDatastore datastore = new UserDatastore(null);
    @Captor
    private ArgumentCaptor<UserUpdater> userUpdaterArgumentCaptor;

    @Before
    public void init() {
        UserEngineClient userEngineClient = new UserEngineClient(identityAPI);
        doReturn(userEngineClient).when(datastore).getUserEngineClient();
        doReturn(processEngineClient).when(datastore).getProcessEngineClient();
        when(processEngineClient.getProcessApi()).thenReturn(processAPI);
        doReturn(bonitaHomeFolderAccessor).when(datastore).getBonitaHomeFolderAccessor();
    }

    @Test
    public void testSearchWithMultipleSortOrderDoesNotThrowException() throws Exception {
        final String sort = UserItem.ATTRIBUTE_FIRSTNAME + "," + UserItem.ATTRIBUTE_LASTNAME;
        Mockito.doReturn(new SearchResultImpl<>(0, Collections.<User> emptyList())).when(identityAPI)
                .searchUsers(Mockito.any(SearchOptions.class));
        try {
            datastore.search(0, 1, "search", Collections.emptyMap(), sort);
        } catch (Exception e) {
            Assert.fail("Search should be able to handle multiple sort");
        }
    }

    @Test
    public void should_updateUser_call_engine_api() throws Exception {
        //given
        doReturn(new UserImpl(12L, "john")).when(identityAPI).updateUser(eq(12L), any(UserUpdater.class));
        //when
        Map<String, String> attributes = new HashMap<>();
        attributes.put("userName", "jack");
        attributes.put("icon", "");
        datastore.update(APIID.makeAPIID(12L), attributes);
        //then
        verify(identityAPI).updateUser(eq(12L), userUpdaterArgumentCaptor.capture());
        UserUpdater userUpdater = userUpdaterArgumentCaptor.getValue();
        assertThat(userUpdater.getFields()).containsOnly(entry(UserUpdater.UserField.USER_NAME, "jack"));
    }

    @Test
    public void should_updateUser_with_icon_call_engine_api_with_content_from_FS() throws Exception {
        //given
        doReturn(new UserImpl(12L, "john")).when(identityAPI).updateUser(eq(12L), any(UserUpdater.class));
        IconDescriptor iconDescriptor = new IconDescriptor("iconName", "content".getBytes());
        doReturn(iconDescriptor).when(bonitaHomeFolderAccessor).getIconFromFileSystem(eq("temp_icon_on_fs"));
        //when
        datastore.update(APIID.makeAPIID(12L), Collections.singletonMap("icon", "temp_icon_on_fs"));
        //then
        verify(identityAPI).updateUser(eq(12L), userUpdaterArgumentCaptor.capture());
        UserUpdater userUpdater = userUpdaterArgumentCaptor.getValue();
        assertThat(userUpdater.getFields().get(UserUpdater.UserField.ICON_FILENAME)).isEqualTo("iconName");
        assertThat(userUpdater.getFields().get(UserUpdater.UserField.ICON_CONTENT)).isEqualTo("content".getBytes());
    }

    @Test
    public void testSearchUsersWhoCanPerformTask_with_should_return_nothing() {
        when(processAPI.searchUsersWhoCanExecutePendingHumanTask(eq(0L), any(SearchOptions.class)))
                .thenReturn(mock(SearchResult.class));
        ItemSearchResult<UserItem> results = datastore.searchUsersWhoCanPerformTask("0", 0, 10, "jan",
                Collections.EMPTY_MAP, "");
        verify(processAPI, times(1)).searchUsersWhoCanExecutePendingHumanTask(anyLong(), any(SearchOptions.class));
        assertThat(results.getLength()).isEqualTo(10);
        assertThat(results.getPage()).isEqualTo(0);
        assertThat(results.getTotal()).isEqualTo(0);
        assertThat(results.getResults()).isEmpty();
    }

    @Test
    public void testSearchUsersWhoCanPerformTask_with_should_return_one_result() {
        @SuppressWarnings("rawtypes")
        SearchResult engineSearchResults = mock(SearchResult.class);
        long expected = 1;
        when(engineSearchResults.getCount()).thenReturn(expected);
        User user = mock(User.class);
        List<User> userList = Collections.singletonList(user);
        when(engineSearchResults.getResult()).thenReturn(userList);
        when(processAPI.searchUsersWhoCanExecutePendingHumanTask(eq(18L), any(SearchOptions.class)))
                .thenReturn(engineSearchResults);
        UserItem userItem = mock(UserItem.class);
        List<UserItem> userItemList = Collections.singletonList(userItem);
        when(userItemConverter.convert(userList)).thenReturn(userItemList);
        int page = 1;
        int resultsByPage = 8;
        ItemSearchResult<UserItem> results = datastore.searchUsersWhoCanPerformTask("18", page, resultsByPage, "jan",
                Collections.EMPTY_MAP, "");
        assertThat(results.getLength()).isEqualTo(resultsByPage);
        assertThat(results.getPage()).isEqualTo(page);
        assertThat(results.getTotal()).isEqualTo(expected);
        assertThat(results.getResults()).isNotEmpty().hasSize(1).containsExactly(userItem);
        verify(processAPI, times(1)).searchUsersWhoCanExecutePendingHumanTask(anyLong(), any(SearchOptions.class));
        verify(userItemConverter, times(1)).convert(userList);
    }

    @Test
    public void buildSearchOptionCreator_should_convert_enabled_attribute_to_boolean() {
        final List<SearchFilter> filters = datastore.buildSearchOptionCreator(0, 10, "",
                Collections.singletonMap(UserItem.ATTRIBUTE_ENABLED, "true"), "displayName ASC").create().getFilters();
        assertThat(filters.get(0).getValue()).isEqualTo(true);
    }

}
