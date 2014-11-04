/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Date;

import com.bonitasoft.engine.business.application.SBonitaExportException;
import com.bonitasoft.engine.business.application.impl.converter.ApplicationNodeConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import com.bonitasoft.engine.business.application.model.xml.ApplicationNode;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationNodeConverterTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationNodeConverter converter;

    @Test
    public void toNode_should_return_convert_all_string_fields() throws Exception {
        //given
        SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 11L, "enabled");
        application.setDescription("this is my app");
        application.setIconPath("/icon.jpg");

        //when
        ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getToken()).isEqualTo("app");
        assertThat(applicationNode.getDisplayName()).isEqualTo("my app");
        assertThat(applicationNode.getVersion()).isEqualTo("1.0");
        assertThat(applicationNode.getDescription()).isEqualTo("this is my app");
        assertThat(applicationNode.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(applicationNode.getState()).isEqualTo("enabled");
        assertThat(applicationNode.getProfile()).isNull();
        assertThat(applicationNode.getHomePage()).isNull();

    }

    @Test
    public void toNode_should_replace_profile_id_by_profile_name() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        given(application.getProfileId()).willReturn(7L);
        given(application.getHomePageId()).willReturn(null);
        SProfile profile = mock(SProfile.class);
        given(profile.getName()).willReturn("admin");

        given(profileService.getProfile(7L)).willReturn(profile);

        //when
        ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getProfile()).isEqualTo("admin");
    }

    @Test(expected = SBonitaExportException.class)
    public void toNode_should_throw_SBonitaExportException_when_profileService_throws_exception() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        given(application.getProfileId()).willReturn(7L);

        given(profileService.getProfile(7L)).willThrow(new SProfileNotFoundException(""));

        //when
        converter.toNode(application);

        //then exception
    }

    @Test
    public void toNode_should_replaceHomePageId_by_application_page_token() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        given(application.getHomePageId()).willReturn(8L);
        given(application.getProfileId()).willReturn(null);
        SApplicationPage homePage = mock(SApplicationPage.class);
        given(homePage.getToken()).willReturn("home");

        given(applicationService.getApplicationPage(8L)).willReturn(homePage);

        //when
        ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getHomePage()).isEqualTo("home");
    }

    @Test (expected = SBonitaExportException.class)
    public void toNode_should_throw_SBonitaExportException_when_applicationService_throws_exception() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        given(application.getHomePageId()).willReturn(8L);
        given(application.getProfileId()).willReturn(null);
        SApplicationPage homePage = mock(SApplicationPage.class);
        given(homePage.getToken()).willReturn("home");

        given(applicationService.getApplicationPage(8L)).willThrow(new SObjectNotFoundException());

        //when
        converter.toNode(application);

        //then exception
    }

}
