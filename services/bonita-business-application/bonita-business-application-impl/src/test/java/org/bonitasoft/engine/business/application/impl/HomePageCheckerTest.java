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
package org.bonitasoft.engine.business.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.HomePageChecker;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
