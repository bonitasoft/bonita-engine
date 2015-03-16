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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuCleaner;
import org.bonitasoft.engine.business.application.impl.cleaner.ApplicationMenuDestructor;
import org.bonitasoft.engine.business.application.impl.filter.ChildrenMenusFilterBuilder;
import org.bonitasoft.engine.business.application.impl.filter.SelectRange;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
