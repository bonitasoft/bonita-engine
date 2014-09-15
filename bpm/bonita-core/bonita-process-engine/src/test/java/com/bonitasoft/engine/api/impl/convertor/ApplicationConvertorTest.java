/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.convertor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.impl.ApplicationImpl;
import com.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.SApplicationState;
import com.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import com.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;
import com.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;


public class ApplicationConvertorTest {

    private static final String ICON_PATH = "/icon.jpg";
    private static final long TENANT_ID = 1;
    private static final long ID = 11;
    private static final long CREATOR_ID = 16;
    private static final long APPLICATION_ID = 20;
    private static final long PAGE_ID = 30;
    private static final long HOME_PAGE_ID = 130;
    private static final long PROFILE_ID = 40;
    private static final String APP_DESC = "app desc";
    private static final String APP_PATH = "/app";
    private static final String APP_VERSION = "1.0";
    private static final String APP_NAME = "app";
    private static final String APP_DISPLAY_NAME = "My application";
    private static final String APP_PAGE_NAME = "firstPage";
    private final ApplicationConvertor convertor = new ApplicationConvertor();

    @Test
    public void buildSApplication_should_map_all_information_from_creator_and_initialize_mandatory_fields() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, APP_PATH);
        creator.setDescription(APP_DESC);
        creator.setIconPath(ICON_PATH);
        creator.setProfileId(PROFILE_ID);
        final long userId = 10;
        final long before = System.currentTimeMillis();

        //when
        final SApplication application = convertor.buildSApplication(creator, userId);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getName()).isEqualTo(APP_NAME);
        assertThat(application.getDisplayName()).isEqualTo(APP_DISPLAY_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getPath()).isEqualTo(APP_PATH);
        assertThat(application.getDescription()).isEqualTo(APP_DESC);
        assertThat(application.getIconPath()).isEqualTo(ICON_PATH);
        assertThat(application.getCreatedBy()).isEqualTo(userId);
        assertThat(application.getCreationDate()).isGreaterThanOrEqualTo(before);
        assertThat(application.getUpdatedBy()).isEqualTo(userId);
        assertThat(application.getLastUpdateDate()).isEqualTo(application.getCreationDate());
        assertThat(application.getState()).isEqualTo(SApplicationState.DEACTIVATED.name());
        assertThat(application.getProfileId()).isEqualTo(PROFILE_ID);
    }

    @Test
    public void toAppplication_must_map_all_server_fields() throws Exception {
        //given
        final long currentDate = System.currentTimeMillis();
        final String state = SApplicationState.DEACTIVATED.name();
        final SApplicationImpl sApp = new SApplicationImpl(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, APP_PATH, currentDate,
                CREATOR_ID, state);
        sApp.setDescription(APP_DESC);
        sApp.setId(ID);
        sApp.setTenantId(TENANT_ID);
        sApp.setIconPath(ICON_PATH);
        sApp.setHomePageId(HOME_PAGE_ID);
        sApp.setProfileId(PROFILE_ID);

        //when
        final Application application = convertor.toApplication(sApp);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getId()).isEqualTo(ID);
        assertThat(application.getName()).isEqualTo(APP_NAME);
        assertThat(application.getDisplayName()).isEqualTo(APP_DISPLAY_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getPath()).isEqualTo(APP_PATH);
        assertThat(application.getDescription()).isEqualTo(APP_DESC);
        assertThat(application.getIconPath()).isEqualTo(ICON_PATH);
        assertThat(application.getCreatedBy()).isEqualTo(CREATOR_ID);
        assertThat(application.getCreationDate()).isEqualTo(new Date(currentDate));
        assertThat(application.getUpdatedBy()).isEqualTo(CREATOR_ID);
        assertThat(application.getLastUpdateDate()).isEqualTo(new Date(currentDate));
        assertThat(application.getState()).isEqualTo(state);
        assertThat(application.getHomePageId()).isEqualTo(HOME_PAGE_ID);
        assertThat(application.getProfileId()).isEqualTo(PROFILE_ID);
    }

    @Test
    public void toApplicationList_should_call_toApplition_for_each_element_in_the_list_and_return_the_list_of_converted_values() throws Exception {
        //given
        final SApplicationImpl sApp1 = new SApplicationImpl(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, APP_PATH, System.currentTimeMillis(), CREATOR_ID,
                SApplicationState.DEACTIVATED.name());
        final SApplicationImpl sApp2 = new SApplicationImpl("app2", " my app2", APP_VERSION, "/app2", System.currentTimeMillis(), CREATOR_ID,
                SApplicationState.DEACTIVATED.name());
        final ApplicationImpl app1 = new ApplicationImpl(APP_NAME, APP_VERSION, APP_PATH, APP_DESC);
        final ApplicationImpl app2 = new ApplicationImpl("app2", APP_VERSION, "/app2", APP_DESC);
        final ApplicationConvertor convertorMock = spy(convertor);
        doReturn(app1).when(convertorMock).toApplication(sApp1);
        doReturn(app2).when(convertorMock).toApplication(sApp2);

        //when
        final List<Application> applications = convertorMock.toApplication(Arrays.<SApplication> asList(sApp1, sApp2));

        //then
        assertThat(applications).containsExactly(app1, app2);
    }

    @Test
    public void toApplicationPage_should_map_all_server_fields() throws Exception {
        //given
        final SApplicationPageImpl sAppPage = new SApplicationPageImpl(APPLICATION_ID, PAGE_ID, APP_PAGE_NAME);
        sAppPage.setId(ID);

        //when
        final ApplicationPage appPage = convertor.toApplicationPage(sAppPage);

        //then
        assertThat(appPage).isNotNull();
        assertThat(appPage.getId()).isEqualTo(ID);
        assertThat(appPage.getApplicationId()).isEqualTo(APPLICATION_ID);
        assertThat(appPage.getPageId()).isEqualTo(PAGE_ID);
        assertThat(appPage.getName()).isEqualTo(APP_PAGE_NAME);
    }

    @Test
    public void toApplicationPageList_should_call_toApplitionPage_for_each_element_and_return_the_list_of_converted_values() throws Exception {
        //given
        final SApplicationPageImpl sAppPage1 = new SApplicationPageImpl(10, 21, "appPage1");
        final SApplicationPageImpl sAppPage2 = new SApplicationPageImpl(10, 21, "appPage2");
        final ApplicationPageImpl appPage1 = new ApplicationPageImpl(10, 21, "appPage1");
        final ApplicationPageImpl appPage2 = new ApplicationPageImpl(10, 21, "appPage2");
        final ApplicationConvertor convertorMock = spy(convertor);
        doReturn(appPage1).when(convertorMock).toApplicationPage(sAppPage1);
        doReturn(appPage2).when(convertorMock).toApplicationPage(sAppPage2);

        //when
        final List<ApplicationPage> applicationPages = convertorMock.toApplicationPage(Arrays.<SApplicationPage> asList(sAppPage1, sAppPage2));

        //then
        assertThat(applicationPages).containsExactly(appPage1, appPage2);
    }

    @Test
    public void buildSApplicationMenu_should_map_all_creator_fields() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("main", 20, 1);
        creator.setParentId(11);

        //when
        final SApplicationMenu menu = convertor.buildSApplicationMenu(creator);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getDisplayName()).isEqualTo("main");
        assertThat(menu.getApplicationPageId()).isEqualTo(20);
        assertThat(menu.getIndex()).isEqualTo(1);
        assertThat(menu.getParentId()).isEqualTo(11);
    }

    @Test
    public void buildSApplicationMenu_should_have_null_parentId_when_not_set_on_creator() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("main", 20, 1);

        //when
        final SApplicationMenu menu = convertor.buildSApplicationMenu(creator);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getParentId()).isNull();
    }

    @Test
    public void toApplicationMenu_should_map_all_server_object_fields() throws Exception {
        //given
        final SApplicationMenuImpl sMenu = new SApplicationMenuImpl("main", 15, 1);
        sMenu.setId(3);
        sMenu.setParentId(21L);

        //when
        final ApplicationMenu menu = convertor.toApplicationMenu(sMenu);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getId()).isEqualTo(3);
        assertThat(menu.getDisplayName()).isEqualTo("main");
        assertThat(menu.getApplicationPageId()).isEqualTo(15);
        assertThat(menu.getIndex()).isEqualTo(1);
        assertThat(menu.getParentId()).isEqualTo(21);
    }

    @Test
    public void toApplicationMenu_list_should_call_toApplitionMenu_for_each_element_in_the_list_and_return_the_list_of_converted_values() throws Exception {
        //given
        final SApplicationMenu sMenu1 = mock(SApplicationMenu.class);
        final SApplicationMenu sMenu2 = mock(SApplicationMenu.class);
        final ApplicationMenu menu1 = mock(ApplicationMenu.class);
        final ApplicationMenu menu2 = mock(ApplicationMenu.class);
        final ApplicationConvertor convertorMock = spy(convertor);
        doReturn(menu1).when(convertorMock).toApplicationMenu(sMenu1);
        doReturn(menu2).when(convertorMock).toApplicationMenu(sMenu2);

        //when
        final List<ApplicationMenu> applicationMenus = convertorMock.toApplicationMenu(Arrays.<SApplicationMenu> asList(sMenu1, sMenu2));

        //then
        assertThat(applicationMenus).containsExactly(menu1, menu2);
    }

}
