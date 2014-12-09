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

import com.bonitasoft.engine.business.application.impl.ApplicationServiceImpl;
import com.bonitasoft.engine.business.application.impl.filter.ChildrenMenusFilterBuilder;
import com.bonitasoft.engine.business.application.impl.filter.SelectRange;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;

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
