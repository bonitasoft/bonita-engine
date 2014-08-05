/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import com.bonitasoft.engine.search.descriptor.SearchApplicationDescriptor;
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
        final ApplicationAPIDelegate delegate = getDelegate();
        return delegate.createApplication(applicationCreator);
    }

    private ApplicationAPIDelegate getDelegate() {
        return getDelegate(null);
    }
    private ApplicationAPIDelegate getDelegate(final SearchOptions searchOptions) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SearchApplicationDescriptor searchDescriptor = tenantAccessor.getSearchEntitiesDescriptor().getSearchApplicationDescriptor();
        final ApplicationConvertor convertor = new ApplicationConvertor();
        final SearchApplications searchApplications = new SearchApplications(tenantAccessor.getApplicationService(), searchDescriptor, searchOptions, convertor);
        final ApplicationAPIDelegate delegate = new ApplicationAPIDelegate(tenantAccessor, convertor,
                SessionInfos.getUserIdFromSession(), searchApplications);
        return delegate;
    }

    @Override
    public Application getApplication(final long applicationId) throws ApplicationNotFoundException {
        final ApplicationAPIDelegate delegate = getDelegate();
        return delegate.getApplication(applicationId);
    }

    @Override
    public void deleteApplication(final long applicationId) throws DeletionException {
        final ApplicationAPIDelegate delegate = getDelegate();
        delegate.deleteApplication(applicationId);
    }

    protected TenantServiceAccessor getTenantAccessor() {
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
        final ApplicationAPIDelegate delegate = getDelegate(searchOptions);
        return delegate.searchApplications();
    }

    @Override
    public ApplicationPage createApplicationPage(final long applicationId, final long pagedId, final String name) throws AlreadyExistsException,
    CreationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationPage getApplicationPage(final String applicationName, final String applicationPageName) throws ApplicationPageNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchResult<ApplicationPage> searchApplicationPages(final SearchOptions searchOptions) throws SearchException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationPage getApplicationPage(final long applicationPageId) throws ApplicationPageNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

}
