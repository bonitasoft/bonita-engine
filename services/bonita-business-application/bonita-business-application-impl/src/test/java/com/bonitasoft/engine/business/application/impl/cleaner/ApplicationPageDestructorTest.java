/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.cleaner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import com.bonitasoft.engine.business.application.impl.HomePageChecker;
import com.bonitasoft.engine.business.application.impl.filter.ApplicationPageRelatedMenusFilterBuilder;
import com.bonitasoft.engine.business.application.impl.filter.SelectRange;
import com.bonitasoft.engine.business.application.model.SApplicationPage;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPageDestructorTest {

    public static final long APPLICATION_PAGE_ID = 2L;

    @Mock
    private ApplicationMenuCleaner applicationMenuCleaner;

    @Mock
    private HomePageChecker homePageChecker;

    @InjectMocks
    private ApplicationPageDestructor destructor;

    @Test
    public void onDeleteApplicationPage_should_call_applicationMenuCleaner() throws Exception {
        //given
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        given(applicationPage.getId()).willReturn(APPLICATION_PAGE_ID);

        //when
        destructor.onDeleteApplicationPage(applicationPage);

        //then
        verify(applicationMenuCleaner, times(1)).deleteRelatedApplicationMenus(new ApplicationPageRelatedMenusFilterBuilder(new SelectRange(0, ApplicationServiceImpl.MAX_RESULTS), APPLICATION_PAGE_ID));
    }

    @Test
    public void onDeleteApplicationPage_should_throw_SObjectModificationException_when_homePageChecker_returns_true() throws Exception {
        //given
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        given(applicationPage.getId()).willReturn(APPLICATION_PAGE_ID);
        given(homePageChecker.isHomePage(applicationPage)).willReturn(true);

        try {
            //when
            destructor.onDeleteApplicationPage(applicationPage);
            Assertions.fail("Exception expected");
        } catch (SObjectModificationException e) {
            //then
            assertThat(e.getMessage()).isEqualTo("The application page with id '" + APPLICATION_PAGE_ID + "' cannot be deleted because it is set as the application home page");
        }

    }
}
