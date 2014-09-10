package com.bonitasoft.engine.api.impl.convertor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.impl.ApplicationImpl;
import com.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.SApplicationState;
import com.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import com.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;


public class ApplicationConvertorTest {

    private static final String ICON_PATH = "/icon.jpg";
    private static final int TENANT_ID = 1;
    private static final int ID = 11;
    private static final int CREATOR_ID = 16;
    private static final int APPLICATION_ID = 20;
    private static final int PAGE_ID = 30;
    private static final int HOME_PAGE_ID = 130;
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

}
