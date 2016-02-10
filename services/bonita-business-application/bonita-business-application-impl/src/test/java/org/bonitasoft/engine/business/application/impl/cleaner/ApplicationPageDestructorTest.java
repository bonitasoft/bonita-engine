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
package org.bonitasoft.engine.business.application.impl.cleaner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import org.bonitasoft.engine.business.application.impl.HomePageChecker;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuCleaner;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationPageDestructor;
import org.bonitasoft.engine.business.application.impl.filter.ApplicationPageRelatedMenusFilterBuilder;
import org.bonitasoft.engine.business.application.impl.filter.SelectRange;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
