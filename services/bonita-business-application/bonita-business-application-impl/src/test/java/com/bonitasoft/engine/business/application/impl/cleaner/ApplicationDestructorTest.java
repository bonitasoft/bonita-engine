/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.cleaner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import com.bonitasoft.engine.business.application.impl.filter.ApplicationRelatedMenusFilterBuilder;
import com.bonitasoft.engine.business.application.impl.filter.SelectRange;
import com.bonitasoft.engine.business.application.model.SApplication;

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
