/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Map;

import com.bonitasoft.engine.business.application.ApplicationCreatorExt;
import com.bonitasoft.engine.business.application.ApplicationUpdaterExt;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationFields;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationModelConverterExtTest {

    private static final String APP_VERSION = "1.0";
    private static final String APP_NAME = "app";
    private static final String APP_DISPLAY_NAME = "My application";
    public static final long SPECIFIC_LAYOUT_ID = 55L;
    public static final long SPECIFIC_THEME_ID = 65L;

    public static final long DEFAULT_LAYOUT_ID = 1L;
    public static final long DEFAULT_THEME_ID = 2L;

    @Mock
    private PageService pageService;

    @InjectMocks
    private ApplicationModelConverterExt converter;

    @Mock
    private SPage defaultTheme;

    @Mock
    private SPage defaultLayout;

    @Before
    public void setUp() throws Exception {
        given(defaultLayout.getId()).willReturn(DEFAULT_LAYOUT_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(defaultLayout);

        given(defaultTheme.getId()).willReturn(DEFAULT_THEME_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(defaultTheme);
    }

    @Test
    public void toSApplication_should_create_application_with_specified_layout_and_theme() throws Exception {
        //given
        final ApplicationCreatorExt creator = new ApplicationCreatorExt(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, SPECIFIC_LAYOUT_ID, SPECIFIC_THEME_ID);
        final long userId = 10;

        //when
        final SApplication application = converter.buildSApplication(creator, userId);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getToken()).isEqualTo(APP_NAME);
        assertThat(application.getDisplayName()).isEqualTo(APP_DISPLAY_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getCreatedBy()).isEqualTo(userId);
        assertThat(application.getUpdatedBy()).isEqualTo(userId);
        assertThat(application.getLayoutId()).isEqualTo(SPECIFIC_LAYOUT_ID);
        assertThat(application.getThemeId()).isEqualTo(SPECIFIC_THEME_ID);
    }

    @Test
    public void toSApplication_should_use_default_layout_when_layout_is_not_set_by_creator() throws Exception {
        //given
        final ApplicationCreatorExt creator = new ApplicationCreatorExt(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        final long userId = 10;

        //when
        final SApplication application = converter.buildSApplication(creator, userId);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getLayoutId()).isEqualTo(DEFAULT_LAYOUT_ID);
    }

    @Test
    public void toSApplication_should_use_default_theme_when_theme_is_not_set_by_creator() throws Exception {
        //given
        final ApplicationCreatorExt creator = new ApplicationCreatorExt(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        final long userId = 10;

        //when
        final SApplication application = converter.buildSApplication(creator, userId);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getThemeId()).isEqualTo(DEFAULT_THEME_ID);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_map_layoutId() throws Exception {
        //given
        long userId = 1L;
        long layoutId = 20L;
        final ApplicationUpdaterExt updater = new ApplicationUpdaterExt();
        updater.setLayoutId(layoutId);

        //when
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater, userId);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).hasSize(3); // fields updated by and last updated date are also updated
        assertThat(fields).containsKeys(SApplicationFields.UPDATED_BY, SApplicationFields.LAST_UPDATE_DATE, SApplicationFields.LAYOUT_ID);
        assertThat(fields.get(SApplicationFields.LAYOUT_ID)).isEqualTo(layoutId);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_not_update_layoutId_if_this_field_is_not_set_to_be_updated() throws Exception {
        //given
        long userId = 1L;
        final ApplicationUpdaterExt updater = new ApplicationUpdaterExt();
        updater.setToken("newToken");

        //when
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater, userId);

        //then
        assertThat(updateDescriptor).isNotNull();
        assertThat(updateDescriptor.getFields()).doesNotContainKey(SApplicationFields.LAYOUT_ID);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_map_themeId() throws Exception {
        //given
        long userId = 1L;
        long themeId = 20L;
        final ApplicationUpdaterExt updater = new ApplicationUpdaterExt();
        updater.setThemeId(themeId);

        //when
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater, userId);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).hasSize(3); // fields updated by and last updated date are also updated
        assertThat(fields).containsKeys(SApplicationFields.UPDATED_BY, SApplicationFields.LAST_UPDATE_DATE, SApplicationFields.THEME_ID);
        assertThat(fields.get(SApplicationFields.THEME_ID)).isEqualTo(themeId);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_not_update_themeId_if_this_field_is_not_set_to_be_updated() throws Exception {
        //given
        long userId = 1L;
        final ApplicationUpdaterExt updater = new ApplicationUpdaterExt();
        updater.setToken("newToken");

        //when
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationUpdateDescriptor(updater, userId);

        //then
        assertThat(updateDescriptor).isNotNull();
        assertThat(updateDescriptor.getFields()).doesNotContainKey(SApplicationFields.THEME_ID);
    }

}