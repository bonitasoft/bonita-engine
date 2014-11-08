/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.api.impl.application;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.api.impl.convertor.ApplicationPageConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SInvalidTokenException;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationPageAPIDelegate {

    private final ApplicationPageConvertor convertor;
    private final ApplicationService applicationService;
    private final SearchApplicationPages searchApplicationPages;
    private final long loggedUserId;

    public ApplicationPageAPIDelegate(final TenantServiceAccessor accessor, final ApplicationPageConvertor convertor,
            final SearchApplicationPages searchApplicationPages, final long loggedUserId) {
        this.searchApplicationPages = searchApplicationPages;
        applicationService = accessor.getApplicationService();
        this.convertor = convertor;
        this.loggedUserId = loggedUserId;
    }

    public void setApplicationHomePage(final long applicationId, final long applicationPageId) throws UpdateException, ApplicationNotFoundException {
        BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(loggedUserId).updateHomePageId(applicationPageId);
        try {
            applicationService.updateApplication(applicationId, BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(loggedUserId).done());
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    public ApplicationPage createApplicationPage(final long applicationId, final long pageId, final String name) throws AlreadyExistsException,
            CreationException {
        final SApplicationPageBuilderFactory factory = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        final SApplicationPageBuilder pageBuilder = factory.createNewInstance(applicationId, pageId, name);
        SApplicationPage sAppPage;
        try {
            sAppPage = applicationService.createApplicationPage(pageBuilder.done());
            applicationService.updateApplication(applicationId, BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(loggedUserId).done());
            return convertor.toApplicationPage(sAppPage);
        } catch (final SObjectCreationException e) {
            throw new CreationException(e);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SInvalidTokenException e) {
            throw new CreationException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    public ApplicationPage getApplicationPage(final String applicationName, final String applicationPageToken) throws ApplicationPageNotFoundException {
        try {
            final SApplicationPage sAppPage = applicationService.getApplicationPage(applicationName, applicationPageToken);
            return convertor.toApplicationPage(sAppPage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public ApplicationPage getApplicationPage(final long applicationPageId) throws ApplicationPageNotFoundException {
        try {
            final SApplicationPage sApplicationPage = applicationService.getApplicationPage(applicationPageId);
            return convertor.toApplicationPage(sApplicationPage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public void deleteApplicationPage(final long applicationPageId) throws DeletionException {
        try {
            final SApplicationPage deletedApplicationPage = applicationService.deleteApplicationPage(applicationPageId);
            final SApplicationUpdateBuilder appBbuilder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(loggedUserId);
            applicationService.updateApplication(deletedApplicationPage.getApplicationId(), appBbuilder.done());
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    public ApplicationPage getApplicationHomePage(final long applicationId) throws ApplicationPageNotFoundException {
        SApplicationPage sHomePage;
        try {
            sHomePage = applicationService.getApplicationHomePage(applicationId);
            return convertor.toApplicationPage(sHomePage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public SearchResult<ApplicationPage> searchApplicationPages() throws SearchException {
        try {
            searchApplicationPages.execute();
            return searchApplicationPages.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

}
