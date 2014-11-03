/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.cleaner;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import com.bonitasoft.engine.business.application.impl.HomePageChecker;
import com.bonitasoft.engine.business.application.impl.filter.ApplicationPageRelatedMenusFilterBuilder;
import com.bonitasoft.engine.business.application.impl.filter.SelectRange;
import com.bonitasoft.engine.business.application.model.SApplicationPage;

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
