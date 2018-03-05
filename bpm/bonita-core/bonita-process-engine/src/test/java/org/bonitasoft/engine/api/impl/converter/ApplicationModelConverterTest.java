/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.ApplicationState;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationFields;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationModelConverterTest {

    private static final String ICON_PATH = "/icon.jpg";
    private static final long TENANT_ID = 1;
    private static final long ID = 11;
    private static final long CREATOR_ID = 16;
    private static final long HOME_PAGE_ID = 130;
    private static final long PROFILE_ID = 40;
    private static final String APP_DESC = "app desc";
    private static final String APP_VERSION = "1.0";
    private static final String APP_NAME = "app";
    private static final String APP_DISPLAY_NAME = "My application";
    private static final long LOGGED_USER_ID = 10;
    public static final long LAYOUT_ID = 55L;
    public static final long THEME_ID = 56L;
    public static final String APP_NAME2 = "app2";

    @Mock
    private PageService pageService;

    @InjectMocks
    private ApplicationModelConverter converter;

    @Test
    public void buildSApplication_should_map_all_information_from_creator_and_initialize_mandatory_fields() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        creator.setDescription(APP_DESC);
        creator.setIconPath(ICON_PATH);
        creator.setProfileId(PROFILE_ID);
        final long userId = 10;
        final long before = System.currentTimeMillis();

        SPage layout = mock(SPage.class);
        given(layout.getId()).willReturn(LAYOUT_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(layout);

        SPage theme = mock(SPage.class);
        given(theme.getId()).willReturn(THEME_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(theme);

        //when
        final SApplication application = converter.buildSApplication(creator, userId);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getToken()).isEqualTo(APP_NAME);
        assertThat(application.getDisplayName()).isEqualTo(APP_DISPLAY_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getDescription()).isEqualTo(APP_DESC);
        assertThat(application.getIconPath()).isEqualTo(ICON_PATH);
        assertThat(application.getCreatedBy()).isEqualTo(userId);
        assertThat(application.getCreationDate()).isGreaterThanOrEqualTo(before);
        assertThat(application.getUpdatedBy()).isEqualTo(userId);
        assertThat(application.getLastUpdateDate()).isEqualTo(application.getCreationDate());
        assertThat(application.getState()).isEqualTo(SApplicationState.ACTIVATED.name());
        assertThat(application.getProfileId()).isEqualTo(PROFILE_ID);
        assertThat(application.getLayoutId()).isEqualTo(LAYOUT_ID);
        assertThat(application.getThemeId()).isEqualTo(THEME_ID);
    }

    @Test(expected = CreationException.class)
    public void buildSApplication_should_throw_CreationException_when_the_default_page_layout_is_not_available() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        final long userId = 10;

        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(null);

        //when
        converter.buildSApplication(creator, userId);

        //then exception
    }

    @Test(expected = CreationException.class)
    public void buildSApplication_should_throw_CreationException_when_the_default_theme_is_not_available() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        final long userId = 10;

        SPage layout = mock(SPage.class);
        given(layout.getId()).willReturn(LAYOUT_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(layout);

        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(null);

        //when
        converter.buildSApplication(creator, userId);

        //then exception
    }

    @Test
    public void toApplication_must_map_all_server_fields() throws Exception {
        //given
        final long currentDate = System.currentTimeMillis();
        final String state = SApplicationState.DEACTIVATED.name();
        final SApplicationImpl sApp = new SApplicationImpl(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, currentDate, CREATOR_ID, state, LAYOUT_ID, THEME_ID);
        sApp.setDescription(APP_DESC);
        sApp.setId(ID);
        sApp.setTenantId(TENANT_ID);
        sApp.setIconPath(ICON_PATH);
        sApp.setHomePageId(HOME_PAGE_ID);
        sApp.setProfileId(PROFILE_ID);

        //when
        final Application application = converter.toApplication(sApp);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getId()).isEqualTo(ID);
        assertThat(application.getToken()).isEqualTo(APP_NAME);
        assertThat(application.getDisplayName()).isEqualTo(APP_DISPLAY_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getDescription()).isEqualTo(APP_DESC);
        assertThat(application.getIconPath()).isEqualTo(ICON_PATH);
        assertThat(application.getCreatedBy()).isEqualTo(CREATOR_ID);
        assertThat(application.getCreationDate()).isEqualTo(new Date(currentDate));
        assertThat(application.getUpdatedBy()).isEqualTo(CREATOR_ID);
        assertThat(application.getLastUpdateDate()).isEqualTo(new Date(currentDate));
        assertThat(application.getState()).isEqualTo(state);
        assertThat(application.getHomePageId()).isEqualTo(HOME_PAGE_ID);
        assertThat(application.getProfileId()).isEqualTo(PROFILE_ID);
        assertThat(application.getLayoutId()).isEqualTo(LAYOUT_ID);
        assertThat(application.getThemeId()).isEqualTo(THEME_ID);
    }

    @Test
    public void toApplicationList_should_call_toApplication_for_each_element_in_the_list_and_return_the_list_of_converted_values() throws Exception {
        //given
        final SApplicationImpl sApp1 = new SApplicationImpl(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, System.currentTimeMillis(), CREATOR_ID,
                SApplicationState.DEACTIVATED.name(), LAYOUT_ID, THEME_ID);
        final SApplicationImpl sApp2 = new SApplicationImpl(APP_NAME2, " my app2", APP_VERSION, System.currentTimeMillis(), CREATOR_ID,
                SApplicationState.DEACTIVATED.name(), LAYOUT_ID, THEME_ID);

        //when
        final List<Application> applications = converter.toApplication(Arrays.<SApplication> asList(sApp1, sApp2));

        //then
        assertThat(applications).extracting("token").containsExactly(APP_NAME, APP_NAME2);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_map_all_fields() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("My-updated-app");
        updater.setDisplayName("Updated display name");
        updater.setVersion("1.1");
        updater.setDescription("Up description");
        updater.setIconPath("/newIcon.jpg");
        updater.setProfileId(10L);
        updater.setState(ApplicationState.ACTIVATED.name());
        updater.setHomePageId(11L);

        //when
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).hasSize(10); // field lastUpdateDate cannot be checked:
        assertThat(fields.get(SApplicationFields.TOKEN)).isEqualTo("My-updated-app");
        assertThat(fields.get(SApplicationFields.DISPLAY_NAME)).isEqualTo("Updated display name");
        assertThat(fields.get(SApplicationFields.VERSION)).isEqualTo("1.1");
        assertThat(fields.get(SApplicationFields.DESCRIPTION)).isEqualTo("Up description");
        assertThat(fields.get(SApplicationFields.ICON_PATH)).isEqualTo("/newIcon.jpg");
        assertThat(fields.get(SApplicationFields.PROFILE_ID)).isEqualTo(10L);
        assertThat(fields.get(SApplicationFields.STATE)).isEqualTo(ApplicationState.ACTIVATED.name());
        assertThat(fields.get(SApplicationFields.UPDATED_BY)).isEqualTo(LOGGED_USER_ID);
        assertThat(fields.get(SApplicationFields.HOME_PAGE_ID)).isEqualTo(11L);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_return_empty_map_if_no_field_is_updated() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();

        //when
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).hasSize(2);
    }

}
