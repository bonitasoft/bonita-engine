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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import com.bonitasoft.engine.business.application.impl.filter.ChildrenMenusFilterBuilder;
import com.bonitasoft.engine.business.application.impl.filter.SelectRange;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuDestructorTest {

    @Mock
    private ApplicationMenuCleaner cleaner;

    @InjectMocks
    private ApplicationMenuDestructor destructor;

    @Test
    public void onDeleteApplicationMenu_should_clean_children_menus() throws Exception {
        //given
        long applicationMenuId = 3L;
        SApplicationMenu menu = mock(SApplicationMenu.class);
        given(menu.getId()).willReturn(applicationMenuId);

        //when
        destructor.onDeleteApplicationMenu(menu);

        //then
        verify(cleaner, times(1)).deleteRelatedApplicationMenus(new ChildrenMenusFilterBuilder(new SelectRange(0, ApplicationServiceImpl.MAX_RESULTS), applicationMenuId));
    }
}
