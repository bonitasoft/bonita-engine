/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.application.*;
import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
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
    private static final String ICON_MIME_TYPE = "app mime_type";
    private static final byte[] ICON_CONTENT = "app_icon_content".getBytes(StandardCharsets.UTF_8);

    @Mock
    private PageService pageService;
    @Mock
    private SPage defaultLayout;
    @Mock
    private SPage defaultTheme;

    @InjectMocks
    private ApplicationModelConverter converter;

    @Before
    public void before() throws Exception {
        given(defaultLayout.getId()).willReturn(LAYOUT_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(defaultLayout);
        given(defaultTheme.getId()).willReturn(THEME_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(defaultTheme);
    }

    @Test
    public void buildSApplication_should_map_all_information_from_creator_and_initialize_mandatory_fields()
            throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        creator.setDescription(APP_DESC);
        creator.setIconPath(ICON_PATH);
        creator.setProfileId(PROFILE_ID);
        creator.setIcon("myIcon.jpg", ICON_CONTENT);
        final long userId = 10;
        final long before = System.currentTimeMillis();

        //when
        final SApplicationWithIcon application = converter.buildSApplication(creator, userId);

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
        assertThat(application.getIconContent()).isEqualTo(ICON_CONTENT);
        assertThat(application.getIconMimeType()).isEqualTo("image/jpeg");
        assertThat(application.hasIcon()).isTrue();
    }

    @Test
    public void should_not_have_icon_when_filename_is_empty() throws Exception {
        ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        creator.setIcon("", ICON_CONTENT);

        SApplicationWithIcon application = converter.buildSApplication(creator, 10);

        assertThat(application.getIconContent()).isNull();
        assertThat(application.getIconMimeType()).isNull();
        assertThat(application.hasIcon()).isFalse();
    }

    @Test
    public void should_not_have_icon_when_filename_is_null_but_content_is_set() throws Exception {
        ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        creator.setIcon(null, ICON_CONTENT);

        SApplicationWithIcon application = converter.buildSApplication(creator, 10);

        assertThat(application.getIconContent()).isNull();
        assertThat(application.getIconMimeType()).isNull();
        assertThat(application.hasIcon()).isFalse();
    }

    @Test
    public void should_not_have_icon_when_content_is_empty() throws Exception {
        ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        creator.setIcon("someIcon.png", new byte[] {});

        SApplicationWithIcon application = converter.buildSApplication(creator, 10);

        assertThat(application.getIconContent()).isNull();
        assertThat(application.getIconMimeType()).isNull();
        assertThat(application.hasIcon()).isFalse();
    }

    @Test
    public void should_have_correct_mime_type_when_filename_is_invalid() throws Exception {
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        creator.setIcon("nodotfile", ICON_CONTENT);

        assertThatThrownBy(() -> converter.buildSApplication(creator, 10)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An icon can't have mimetype application/octet-stream");

    }

    @Test(expected = CreationException.class)
    public void buildSApplication_should_throw_CreationException_when_the_default_page_layout_is_not_available()
            throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        final long userId = 10;

        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(null);

        //when
        converter.buildSApplication(creator, userId);

        //then exception
    }

    @Test(expected = CreationException.class)
    public void buildSApplication_should_throw_CreationException_when_the_default_theme_is_not_available()
            throws Exception {
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
        final SApplication sApp = new SApplication(APP_NAME, APP_DISPLAY_NAME, APP_VERSION,
                currentDate, CREATOR_ID, state);
        sApp.setDescription(APP_DESC);
        sApp.setId(ID);
        sApp.setTenantId(TENANT_ID);
        sApp.setIconPath(ICON_PATH);
        sApp.setHomePageId(HOME_PAGE_ID);
        sApp.setProfileId(PROFILE_ID);
        sApp.setThemeId(THEME_ID);
        sApp.setLayoutId(LAYOUT_ID);
        sApp.setIconMimeType(ICON_MIME_TYPE);
        sApp.setEditable(false);
        InternalProfiles internalProfileAll = InternalProfiles.INTERNAL_PROFILE_ALL;
        sApp.setInternalProfile(internalProfileAll.getProfileName());

        //when
        final Application application = (Application) converter.toApplication(sApp);

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
        assertThat(application.isEditable()).isFalse();
        assertThat(application.getVisibility()).isEqualTo(internalProfileAll.getApplicationVisibility());
        assertThat(application.hasIcon()).isTrue();

    }

    @Test
    public void toApplication_must_set_false_when_application_icon_mime_type_is_null() throws Exception {
        //given
        final long currentDate = System.currentTimeMillis();
        final String state = SApplicationState.DEACTIVATED.name();
        final SApplication sApp = new SApplication(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, currentDate, CREATOR_ID,
                state);

        //when
        final Application application = (Application) converter.toApplication(sApp);

        //then
        assertThat(application.hasIcon()).isFalse();

    }

    @Test
    public void toApplicationList_should_call_toApplication_for_each_element_in_the_list_and_return_the_list_of_converted_values()
            throws Exception {
        //given
        final SApplication sApp1 = new SApplication(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, System.currentTimeMillis(),
                CREATOR_ID, SApplicationState.DEACTIVATED.name());
        final SApplication sApp2 = new SApplication(APP_NAME2, " my app2", APP_VERSION, System.currentTimeMillis(),
                CREATOR_ID, SApplicationState.DEACTIVATED.name());

        //when
        final List<IApplication> applications = converter.toApplication(Arrays.<SApplication> asList(sApp1, sApp2));

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
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater,
                LOGGED_USER_ID);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).hasSize(10); // field lastUpdateDate cannot be checked:
        assertThat(fields).contains(
                entry(AbstractSApplication.TOKEN, "My-updated-app"),
                entry(AbstractSApplication.DISPLAY_NAME, "Updated display name"),
                entry(AbstractSApplication.VERSION, "1.1"),
                entry(AbstractSApplication.DESCRIPTION, "Up description"),
                entry(AbstractSApplication.ICON_PATH, "/newIcon.jpg"),
                entry(AbstractSApplication.PROFILE_ID, 10L),
                entry(AbstractSApplication.STATE, ApplicationState.ACTIVATED.name()),
                entry(AbstractSApplication.UPDATED_BY, LOGGED_USER_ID),
                entry(AbstractSApplication.HOME_PAGE_ID, 11L));
    }

    @Test
    public void toApplicationUpdateDescriptor_should_set_iconFileName_to_null_when_empty() throws Exception {
        ApplicationUpdater updater = new ApplicationUpdater();
        updater.setIcon("", null);

        EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater,
                LOGGED_USER_ID);

        assertThat(updateDescriptor.getFields()).contains(entry("iconMimeType", null));
    }

    @Test
    public void toApplicationUpdateDescriptor_should_fail_to_update_icon_filename_when_invalid() throws Exception {
        ApplicationUpdater updater = new ApplicationUpdater();
        updater.setIcon("someFile.exe", "content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> converter.toApplicationUpdateDescriptor(updater,
                LOGGED_USER_ID)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("An icon can't have mimetype");
    }

    @Test
    public void toApplicationUpdateDescriptor_should_return_empty_map_if_no_field_is_updated() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();

        //when
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater,
                LOGGED_USER_ID);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).hasSize(2);
    }

}
