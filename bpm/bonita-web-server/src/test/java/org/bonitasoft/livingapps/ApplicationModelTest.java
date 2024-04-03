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
package org.bonitasoft.livingapps;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationVisibility;
import org.bonitasoft.engine.business.application.impl.ApplicationImpl;
import org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.impl.PageImpl;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.bonitasoft.engine.profile.impl.ProfileImpl;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.Sort;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.livingapps.menu.MenuFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationModelTest {

    @Mock
    ApplicationAPI applicationApi;

    @Mock
    PageAPI pageApi;

    @Mock
    Page page;

    @Mock
    APISession session;

    @Mock
    private MenuFactory factory;

    @Mock
    private ProfileAPI profileApi;

    ApplicationModel model;

    ApplicationImpl application = new ApplicationImpl("token", "version", "description", 1L, 2L);

    @Before
    public void beforeEach() throws Exception {
        application.setId(1L);
        model = new ApplicationModel(applicationApi, pageApi, profileApi, application, factory);
    }

    @Test
    public void should_filter_application_menu_search_for_the_given_application() throws Exception {
        givenSearchApplicationMenusWillReturns(Collections.<ApplicationMenu> emptyList());

        model.getMenuList();
        final ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        verify(applicationApi).searchApplicationMenus(captor.capture());

        final SearchFilter filter = captor.getValue().getFilters().get(0);
        assertThat(filter.getField()).isEqualTo(ApplicationMenuSearchDescriptor.APPLICATION_ID);
        assertThat(filter.getValue()).isEqualTo(application.getId());
    }

    @Test
    public void should_sort_application_menu_search() throws Exception {
        givenSearchApplicationMenusWillReturns(Collections.<ApplicationMenu> emptyList());

        model.getMenuList();
        final ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        verify(applicationApi).searchApplicationMenus(captor.capture());

        final Sort sort = captor.getValue().getSorts().get(0);
        assertThat(sort.getField()).isEqualTo(ApplicationMenuSearchDescriptor.INDEX);
        assertThat(sort.getOrder()).isEqualTo(Order.ASC);
    }

    @Test
    public void should_create_menu_using_menuList() throws Exception {
        final List<ApplicationMenu> menuList = Arrays
                .<ApplicationMenu> asList(new ApplicationMenuImpl("name", 1L, 2L, 1));
        givenSearchApplicationMenusWillReturns(menuList);

        model.getMenuList();

        verify(factory).create(menuList);
    }

    @Test
    public void should_authorize_a_user_with_the_configured_application_profile() throws Exception {
        final ProfileImpl profile1 = new ProfileImpl("user");
        profile1.setId(1L);
        final ProfileImpl profile2 = new ProfileImpl("administrator");
        profile1.setId(2L);
        given(profileApi.getProfilesForUser(1L, 0, Integer.MAX_VALUE, ProfileCriterion.ID_ASC))
                .willReturn(asList((Profile) profile1, profile2));
        given(session.getUserId()).willReturn(1L);
        application.setProfileId(2L);

        assertThat(model.authorize(session)).isTrue();
    }

    @Test
    public void should_not_authorize_a_user_without_the_configured_application_profile() throws Exception {
        final ProfileImpl profile1 = new ProfileImpl("user");
        profile1.setId(1L);
        final ProfileImpl profile2 = new ProfileImpl("administrator");
        profile1.setId(2L);
        given(profileApi.getProfilesForUser(1L, 0, Integer.MAX_VALUE, ProfileCriterion.ID_ASC))
                .willReturn(asList((Profile) profile1, profile2));
        given(session.getUserId()).willReturn(1L);
        application.setProfileId(3L);

        assertThat(model.authorize(session)).isFalse();
    }

    private void givenSearchApplicationMenusWillReturns(final List<ApplicationMenu> menuList) throws Exception {
        given(applicationApi.searchApplicationMenus(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(menuList.size(), menuList));
    }

    @Test
    public void should_getId_return_applicationId() throws Exception {
        assertThat(model.getId()).isEqualTo(1L);
    }

    @Test
    public void should_ApplicationHomePage_return_valide_path() throws Exception {
        given(applicationApi.getApplicationHomePage(1L)).willReturn(new ApplicationPageImpl(1, 1, "pageToken"));

        assertThat(model.getApplicationHomePage()).isEqualTo("pageToken/");
    }

    @Test
    public void should_getApplicationLayoutName_return_valide_name() throws Exception {
        given(page.getName()).willReturn("layoutPage");
        given(pageApi.getPage(1L)).willReturn(page);

        String appLayoutName = model.getApplicationLayoutName();

        assertThat(appLayoutName).isEqualTo("layoutPage");
    }

    @Test
    public void should_getApplicationThemeName_return_valide_name() throws Exception {
        given(page.getName()).willReturn("themePage");
        given(pageApi.getPage(2L)).willReturn(page);

        String appLayoutName = model.getApplicationThemeName();

        assertThat(appLayoutName).isEqualTo("themePage");
    }

    @Test
    public void should_hasPage_return_true() throws Exception {
        given(applicationApi.getApplicationPage("token", "pageToken"))
                .willReturn(new ApplicationPageImpl(1, 1, "pageToken"));

        assertThat(model.hasPage("pageToken")).isEqualTo(true);
    }

    @Test
    public void should_hasPage_return_false() throws Exception {
        given(applicationApi.getApplicationPage("token", "pageToken"))
                .willThrow(new ApplicationPageNotFoundException(""));

        assertThat(model.hasPage("pageToken")).isEqualTo(false);
    }

    @Test
    public void should_getCustomPage_return_expectedPage() throws Exception {
        given(applicationApi.getApplicationPage("token", "pageToken"))
                .willReturn(new ApplicationPageImpl(1, 1, "pageToken"));
        given(pageApi.getPage(1))
                .willReturn(new PageImpl(1, "", "", false, "", 0L, 0L, 0L, 0L, "", ContentType.PAGE, null));

        assertThat(model.getCustomPage("pageToken").getId()).isEqualTo(1);
    }

    @Test
    public void should_check_that_application_has_a_profile_mapped_to_it() throws Exception {
        application.setProfileId(1L);
        application.setVisibility(ApplicationVisibility.RESTRICTED);
        assertThat(model.hasProfileMapped()).isEqualTo(true);

        application.setProfileId(null);
        assertThat(model.hasProfileMapped()).isEqualTo(false);

        application.setVisibility(ApplicationVisibility.ALL);
        assertThat(model.hasProfileMapped()).isEqualTo(true);

        application.setVisibility(ApplicationVisibility.TECHNICAL_USER);
        assertThat(model.hasProfileMapped()).isEqualTo(true);
    }
}
