/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import com.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import com.bonitasoft.engine.business.application.ApplicationPageUpdater;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.impl.application.ApplicationAPIDelegate;
import com.bonitasoft.engine.api.impl.application.ApplicationMenuAPIDelegate;
import com.bonitasoft.engine.api.impl.application.ApplicationPageAPIDelegate;
import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.api.impl.convertor.ApplicationMenuConvertor;
import com.bonitasoft.engine.api.impl.convertor.ApplicationPageConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationMenus;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import com.bonitasoft.engine.api.impl.validator.ApplicationMenuCreatorValidator;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.ApplicationUpdater;
import com.bonitasoft.engine.search.descriptor.SearchApplicationDescriptor;
import com.bonitasoft.engine.search.descriptor.SearchApplicationMenuDescriptor;
import com.bonitasoft.engine.search.descriptor.SearchApplicationPageDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIImpl implements ApplicationAPI {

    @Override
    public Application createApplication(final ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException {
        return getApplicationAPIDelegate().createApplication(applicationCreator);
    }

    private ApplicationAPIDelegate getApplicationAPIDelegate() {
        return getApplicationAPIDelegate(null);
    }

    private ApplicationAPIDelegate getApplicationAPIDelegate(final SearchOptions searchOptions) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SearchApplicationDescriptor appSearchDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getSearchApplicationDescriptor();
        final ApplicationConvertor convertor = new ApplicationConvertor();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final SearchApplications searchApplications = new SearchApplications(applicationService, appSearchDescriptor, searchOptions, convertor);
        final ApplicationAPIDelegate delegate = new ApplicationAPIDelegate(tenantAccessor, convertor,
                SessionInfos.getUserIdFromSession(), searchApplications);
        return delegate;
    }

    private ApplicationPageAPIDelegate getApplicationPageAPIDelegate() {
        return getApplicationPageAPIDelegate(null);
    }

    private ApplicationPageAPIDelegate getApplicationPageAPIDelegate(final SearchOptions searchOptions) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SearchApplicationPageDescriptor appPageSearchDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getSearchApplicationPageDescriptor();
        final ApplicationPageConvertor convertor = new ApplicationPageConvertor();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final SearchApplicationPages searchApplicationPages = new SearchApplicationPages(applicationService, convertor, appPageSearchDescriptor,
                searchOptions);
        final ApplicationPageAPIDelegate delegate = new ApplicationPageAPIDelegate(tenantAccessor, convertor, searchApplicationPages);
        return delegate;
    }

    private ApplicationMenuAPIDelegate getApplicationMenuAPIDelegate() {
        return getApplicationMenuAPIDelegate(null);
    }

    private ApplicationMenuAPIDelegate getApplicationMenuAPIDelegate(final SearchOptions searchOptions) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ApplicationService applicationService = tenantAccessor.getApplicationService();
        final ApplicationMenuConvertor convertor = new ApplicationMenuConvertor();
        final SearchApplicationMenuDescriptor searchDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getSearchApplicationMenuDescriptor();
        final SearchApplicationMenus searchApplicationMenus = new SearchApplicationMenus(applicationService, convertor, searchDescriptor, searchOptions);
        final ApplicationMenuCreatorValidator validator = new ApplicationMenuCreatorValidator();
        final ApplicationMenuAPIDelegate delegate = new ApplicationMenuAPIDelegate(tenantAccessor, convertor, searchApplicationMenus, validator);
        return delegate;
    }

    @Override
    public Application getApplication(final long applicationId) throws ApplicationNotFoundException {
        return getApplicationAPIDelegate().getApplication(applicationId);
    }

    @Override
    public void deleteApplication(final long applicationId) throws DeletionException {
        getApplicationAPIDelegate().deleteApplication(applicationId);
    }

    @Override
    public Application updateApplication(final long applicationId, final ApplicationUpdater updater) throws ApplicationNotFoundException, UpdateException, AlreadyExistsException {
        return getApplicationAPIDelegate().updateApplication(applicationId, updater);
    }

    private TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public SearchResult<Application> searchApplications(final SearchOptions searchOptions) throws SearchException {
        return getApplicationAPIDelegate(searchOptions).searchApplications();
    }

    @Override
    public ApplicationPage createApplicationPage(final long applicationId, final long pagedId, final String token) throws AlreadyExistsException, CreationException {
        return getApplicationPageAPIDelegate().createApplicationPage(applicationId, pagedId, token);
    }

    @Override
    public ApplicationPage getApplicationPage(final String applicationName, final String applicationPageToken) throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationPage(applicationName, applicationPageToken);
    }

    @Override
    public Application updateApplicationPage(long applicationPageId, ApplicationPageUpdater updater) throws ApplicationPageNotFoundException, UpdateException, AlreadyExistsException {
        return null;
    }

    @Override
    public SearchResult<ApplicationPage> searchApplicationPages(final SearchOptions searchOptions) throws SearchException {
        return getApplicationPageAPIDelegate(searchOptions).searchApplicationPages();
    }

    @Override
    public ApplicationPage getApplicationPage(final long applicationPageId) throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationPage(applicationPageId);
    }

    @Override
    public void deleteApplicationPage(final long applicationpPageId) throws DeletionException {
        getApplicationPageAPIDelegate().deleteApplicationPage(applicationpPageId);
    }

    @Override
    public void setApplicationHomePage(final long applicationId, final long applicationPageId) throws UpdateException, ApplicationNotFoundException {
        getApplicationPageAPIDelegate().setApplicationHomePage(applicationId, applicationPageId);
    }

    @Override
    public ApplicationPage getApplicationHomePage(final long applicationId) throws ApplicationPageNotFoundException {
        return getApplicationPageAPIDelegate().getApplicationHomePage(applicationId);
    }

    @Override
    public ApplicationMenu createApplicationMenu(final ApplicationMenuCreator applicationMenuCreator) throws CreationException {
        return getApplicationMenuAPIDelegate().createApplicationMenu(applicationMenuCreator);
    }

    @Override
    public Application updateApplicationMenu(long applicationMenuId, ApplicationMenuUpdater updater) throws ApplicationMenuNotFoundException, UpdateException, AlreadyExistsException {
        return null;
    }

    @Override
    public ApplicationMenu getApplicationMenu(final long applicationMenuId) throws ApplicationMenuNotFoundException {
        return getApplicationMenuAPIDelegate().getApplicationMenu(applicationMenuId);
    }

    @Override
    public void deleteApplicationMenu(final long applicationMenuId) throws DeletionException {
        getApplicationMenuAPIDelegate().deleteApplicationMenu(applicationMenuId);
    }

    @Override
    public SearchResult<ApplicationMenu> searchApplicationMenus(final SearchOptions searchOptions) throws SearchException {
        return getApplicationMenuAPIDelegate(searchOptions).searchApplicationMenus();
    }

}
