/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.impl.cleaner;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuCleaner;
import org.bonitasoft.engine.business.application.impl.filter.FilterBuilder;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuCleanerTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private FilterBuilder filterBuilder;

    @Mock
    private QueryOptions options;

    private static int MAX_RESULTS = 2;

    @InjectMocks
    private ApplicationMenuCleaner cleaner;

    @Before
    public void setUp() throws Exception {
        given(options.getNumberOfResults()).willReturn(MAX_RESULTS);
    }

    @Test
    public void deleteApplicationPage_should_delete_related_applicationMenus() throws Exception {
        //given
        SApplicationMenu menu1 = mock(SApplicationMenu.class);
        SApplicationMenu menu2 = mock(SApplicationMenu.class);
        SApplicationMenu menu3 = mock(SApplicationMenu.class);
        given(filterBuilder.buildQueryOptions()).willReturn(options);
        given(applicationService.searchApplicationMenus(options)).willReturn(Arrays.asList(menu1, menu2)).willReturn(Arrays.asList(menu3));

        //when
        cleaner.deleteRelatedApplicationMenus(filterBuilder);

        //then
        verify(applicationService, times(1)).deleteApplicationMenu(menu1);
        verify(applicationService, times(1)).deleteApplicationMenu(menu2);
        verify(applicationService, times(1)).deleteApplicationMenu(menu3);
    }

}
