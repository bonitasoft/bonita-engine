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

import org.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import org.bonitasoft.engine.business.application.impl.filter.ChildrenMenusFilterBuilder;
import org.bonitasoft.engine.business.application.impl.filter.SelectRange;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuDestructor {

    private ApplicationMenuCleaner applicationMenuCleaner;

    public ApplicationMenuDestructor(ApplicationMenuCleaner applicationMenuCleaner) {
        this.applicationMenuCleaner = applicationMenuCleaner;
    }

    public void onDeleteApplicationMenu(SApplicationMenu applicationMenu) throws SBonitaException {
        applicationMenuCleaner.deleteRelatedApplicationMenus(new ChildrenMenusFilterBuilder(new SelectRange(0, ApplicationServiceImpl.MAX_RESULTS), applicationMenu.getId()));
    }

}
