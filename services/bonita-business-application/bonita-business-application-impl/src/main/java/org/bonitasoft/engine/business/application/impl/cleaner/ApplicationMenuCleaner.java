/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.impl.cleaner;

import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.filter.FilterBuilder;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuCleaner {

    private ApplicationService applicationService;

    public ApplicationMenuCleaner(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public void deleteRelatedApplicationMenus(FilterBuilder filterBuilder) throws SBonitaException {
        QueryOptions options = filterBuilder.buildQueryOptions();
        List<SApplicationMenu> relatedMenus;
        do {
            relatedMenus = applicationService.searchApplicationMenus(options);
            for (SApplicationMenu relatedMenu : relatedMenus) {
                applicationService.deleteApplicationMenu(relatedMenu);
            }
        } while (relatedMenus.size() == options.getNumberOfResults());

    }

}
