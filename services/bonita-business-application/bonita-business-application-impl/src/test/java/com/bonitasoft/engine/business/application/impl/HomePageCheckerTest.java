/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;

@RunWith(MockitoJUnitRunner.class)
public class HomePageCheckerTest {

    public static final long APPLICATION_ID = 10L;
    public static final long APPLICATION_PAGE_ID = 2L;


    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private HomePageChecker checker;

    @Mock
    private SApplication application;

    @Mock
    private SApplicationPage page;

    @Before
    public void setUp() throws Exception {
        given(page.getId()).willReturn(APPLICATION_PAGE_ID);
        given(page.getApplicationId()).willReturn(APPLICATION_ID);
        given(applicationService.getApplication(APPLICATION_ID)).willReturn(application);
    }

    @Test
    public void isHomePage_should_return_true_if_application_home_page_id_is_equals_to_applicationPage_id() throws Exception {
        //given
        given(application.getHomePageId()).willReturn(APPLICATION_PAGE_ID);

        //when
        boolean isHomePage = checker.isHomePage(page);


        //then
        assertThat(isHomePage).isTrue();
    }

    @Test
    public void isHomePage_should_return_false_if_application_home_page_id_is_different_of_applicationPage_id() throws Exception {
        //given
        given(application.getHomePageId()).willReturn(1L);

        //when
        boolean isHomePage = checker.isHomePage(page);


        //then
        assertThat(isHomePage).isFalse();
    }

    @Test
    public void isHomePage_should_return_false_if_application_home_page_id_is_null() throws Exception {
        //given
        given(application.getHomePageId()).willReturn(null);

        //when
        boolean isHomePage = checker.isHomePage(page);

        //then
        assertThat(isHomePage).isFalse();
    }

}
