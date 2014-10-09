/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.application;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationMenus;
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationMenuAPIDelegate {

    private final ApplicationConvertor convertor;
    private final ApplicationService applicationService;
    private final SearchApplicationMenus searchApplicationMenus;

    public ApplicationMenuAPIDelegate(final TenantServiceAccessor accessor, final ApplicationConvertor convertor,
            final SearchApplicationMenus searchApplicationMenus) {
        this.searchApplicationMenus = searchApplicationMenus;
        applicationService = accessor.getApplicationService();
        this.convertor = convertor;
    }

    public ApplicationMenu createApplicationMenu(final ApplicationMenuCreator applicationMenuCreator) throws CreationException {
        try {
            final SApplicationMenu sApplicationMenu = applicationService.createApplicationMenu(convertor.buildSApplicationMenu(applicationMenuCreator));
            return convertor.toApplicationMenu(sApplicationMenu);
        } catch (final SObjectCreationException e) {
            throw new CreationException(e);
        }
    }

    public ApplicationMenu getApplicationMenu(final long applicationMenuId) throws ApplicationMenuNotFoundException {
        try {
            final SApplicationMenu sApplicationMenu = applicationService.getApplicationMenu(applicationMenuId);
            return convertor.toApplicationMenu(sApplicationMenu);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationMenuNotFoundException(e.getMessage());
        }
    }

    public void deleteApplicationMenu(final long applicationMenuId) throws DeletionException {
        try {
            applicationService.deleteApplicationMenu(applicationMenuId);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    public SearchResult<ApplicationMenu> searchApplicationMenus() throws SearchException {
        try {
            searchApplicationMenus.execute();
            return searchApplicationMenus.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

}
