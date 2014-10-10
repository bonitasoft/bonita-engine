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
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationState;
import com.bonitasoft.engine.business.application.ApplicationUpdater;
import com.bonitasoft.engine.business.application.impl.ApplicationImpl;
import com.bonitasoft.engine.business.application.impl.SApplicationFields;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationState;
import com.bonitasoft.engine.business.application.model.impl.SApplicationImpl;


public class ApplicationConvertorTest {

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
    private final ApplicationConvertor convertor = new ApplicationConvertor();

    @Test
    public void buildSApplication_should_map_all_information_from_creator_and_initialize_mandatory_fields() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
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
        final SApplicationImpl sApp = new SApplicationImpl(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, currentDate, CREATOR_ID,
                state);
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
        final SApplicationImpl sApp1 = new SApplicationImpl(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, System.currentTimeMillis(), CREATOR_ID, SApplicationState.DEACTIVATED.name());
        final SApplicationImpl sApp2 = new SApplicationImpl("app2", " my app2", APP_VERSION, System.currentTimeMillis(), CREATOR_ID, SApplicationState.DEACTIVATED.name());
        final ApplicationImpl app1 = new ApplicationImpl(APP_NAME, APP_VERSION, APP_DESC);
        final ApplicationImpl app2 = new ApplicationImpl("app2", APP_VERSION, APP_DESC);
        final ApplicationConvertor convertorMock = spy(convertor);
        doReturn(app1).when(convertorMock).toApplication(sApp1);
        doReturn(app2).when(convertorMock).toApplication(sApp2);

        //when
        final List<Application> applications = convertorMock.toApplication(Arrays.<SApplication> asList(sApp1, sApp2));

        //then
        assertThat(applications).containsExactly(app1, app2);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_map_all_fields() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setName("My-updated-app");
        updater.setDisplayName("Updated display name");
        updater.setVersion("1.1");
        updater.setDescription("Up description");
        updater.setIconPath("/newIcon.jpg");
        updater.setProfileId(10L);
        updater.setState(ApplicationState.ACTIVATED.name());


        //when
        final EntityUpdateDescriptor updateDescriptor = convertor.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).hasSize(9);
        assertThat(fields.get(SApplicationFields.NAME)).isEqualTo("My-updated-app");
        assertThat(fields.get(SApplicationFields.DISPLAY_NAME)).isEqualTo("Updated display name");
        assertThat(fields.get(SApplicationFields.VERSION)).isEqualTo("1.1");
        assertThat(fields.get(SApplicationFields.DESCRIPTION)).isEqualTo("Up description");
        assertThat(fields.get(SApplicationFields.ICON_PATH)).isEqualTo("/newIcon.jpg");
        assertThat(fields.get(SApplicationFields.PROFILE_ID)).isEqualTo(10L);
        assertThat(fields.get(SApplicationFields.STATE)).isEqualTo(ApplicationState.ACTIVATED.name());
        assertThat(fields.get(SApplicationFields.UPDATED_BY)).isEqualTo(LOGGED_USER_ID);
    }

    @Test
    public void toApplicationUpdateDescriptor_should_return_empty_map_if_no_field_is_updated() throws Exception {
        //given
        final ApplicationUpdater updater = new ApplicationUpdater();

        //when
        final EntityUpdateDescriptor updateDescriptor = convertor.toApplicationUpdateDescriptor(updater, LOGGED_USER_ID);

        //then
        assertThat(updateDescriptor).isNotNull();
        final Map<String, Object> fields = updateDescriptor.getFields();
        assertThat(fields).isEmpty();
    }

}
