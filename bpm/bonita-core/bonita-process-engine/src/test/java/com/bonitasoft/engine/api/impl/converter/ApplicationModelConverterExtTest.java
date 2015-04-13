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
import static org.mockito.Mockito.mock;

import com.bonitasoft.engine.business.application.ApplicationCreatorExt;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
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
    public static final long LAYOUT_ID = 55L;

    @Mock
    private PageService pageService;

    @InjectMocks
    private ApplicationModelConverterExt converter;

    @Test
    public void toSApplication_should_create_application_with_specified_layout() throws Exception {
        //given
        final ApplicationCreatorExt creator = new ApplicationCreatorExt(APP_NAME, APP_DISPLAY_NAME, APP_VERSION, LAYOUT_ID);
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
        assertThat(application.getLayoutId()).isEqualTo(LAYOUT_ID);
    }

    @Test
    public void toSApplication_should_use_default_layout_when_layout_is_not_set_by_creator() throws Exception {
        //given
        final ApplicationCreatorExt creator = new ApplicationCreatorExt(APP_NAME, APP_DISPLAY_NAME, APP_VERSION);
        final long userId = 10;

        long defaultLayoutId = 201L;
        SPage page = mock(SPage.class);
        given(page.getId()).willReturn(defaultLayoutId);
        given(pageService.getPageByName("custompage_layout")).willReturn(page);

        //when
        final SApplication application = converter.buildSApplication(creator, userId);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getToken()).isEqualTo(APP_NAME);
        assertThat(application.getDisplayName()).isEqualTo(APP_DISPLAY_NAME);
        assertThat(application.getVersion()).isEqualTo(APP_VERSION);
        assertThat(application.getCreatedBy()).isEqualTo(userId);
        assertThat(application.getUpdatedBy()).isEqualTo(userId);
        assertThat(application.getLayoutId()).isEqualTo(defaultLayoutId);
    }
}