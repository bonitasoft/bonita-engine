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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationDestructor;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuCleaner;
import org.bonitasoft.engine.business.application.impl.filter.ApplicationRelatedMenusFilterBuilder;
import org.bonitasoft.engine.business.application.impl.filter.SelectRange;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationDestructorTest {

    public static final long APPLICATION_ID = 5L;

    @Mock
    private ApplicationMenuCleaner applicationMenuCleaner;

    @InjectMocks
    private ApplicationDestructor applicationDestructor;

    @Mock
    private SApplication application;

    @Before
    public void setUp() throws Exception {
        given(application.getId()).willReturn(APPLICATION_ID);
    }

    @Test
    public void onDeleteApplication_should_call_applicationMenuCleaner() throws Exception {
        //when
        applicationDestructor.onDeleteApplication(application);

        //then
        verify(applicationMenuCleaner, times(1)).deleteRelatedApplicationMenus(new ApplicationRelatedMenusFilterBuilder(new SelectRange(0, ApplicationServiceImpl.MAX_RESULTS), APPLICATION_ID));
    }

}
