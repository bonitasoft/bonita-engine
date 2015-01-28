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
import org.bonitasoft.engine.business.application.impl.HomePageChecker;
import org.bonitasoft.engine.business.application.impl.filter.ApplicationPageRelatedMenusFilterBuilder;
import org.bonitasoft.engine.business.application.impl.filter.SelectRange;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationPageDestructor {

    private ApplicationMenuCleaner applicationMenuCleaner;
    private HomePageChecker homePageChecker;

    public ApplicationPageDestructor(ApplicationMenuCleaner applicationMenuCleaner, HomePageChecker homePageChecker) {
        this.applicationMenuCleaner = applicationMenuCleaner;
        this.homePageChecker = homePageChecker;
    }

    public void onDeleteApplicationPage(SApplicationPage applicationPage) throws SBonitaException {
        verifyIfIsHomePage(applicationPage);
        applicationMenuCleaner.deleteRelatedApplicationMenus(new ApplicationPageRelatedMenusFilterBuilder(new SelectRange(0, ApplicationServiceImpl.MAX_RESULTS), applicationPage.getId()));
    }

    private void verifyIfIsHomePage(SApplicationPage applicationPage) throws SBonitaReadException, SObjectNotFoundException, SObjectModificationException {
        if(homePageChecker.isHomePage(applicationPage)) {
            throw new SObjectModificationException("The application page with id '" + applicationPage.getId() + "' cannot be deleted because it is set as the application home page");
        }
    }

}
