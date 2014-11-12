/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;

/**
 * @author Elias Ricken de Medeiros
 */
public class HomePageChecker {

    private ApplicationService applicationService;

    public HomePageChecker(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public boolean isHomePage(SApplicationPage applicationPage) throws SBonitaReadException, SObjectNotFoundException {
        SApplication application = applicationService.getApplication(applicationPage.getApplicationId());
        return application.getHomePageId() != null && applicationPage.getId() == application.getHomePageId();
    }

}
