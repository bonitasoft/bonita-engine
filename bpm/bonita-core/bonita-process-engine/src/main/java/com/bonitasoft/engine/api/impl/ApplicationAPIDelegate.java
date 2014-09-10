/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SInvalidNameException;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import com.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import com.bonitasoft.engine.exception.InvalidNameException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIDelegate {

    private final ApplicationConvertor convertor;
    private final long loggedUserId;
    private final SearchApplications searchApplications;
    private final ApplicationService applicationService;
    private final SearchApplicationPages searchApplicationPages;

    public ApplicationAPIDelegate(final TenantServiceAccessor accessor, final ApplicationConvertor convertor, final long loggedUserId,
            final SearchApplications searchApplications, final SearchApplicationPages searchApplicationPages) {
        this.searchApplicationPages = searchApplicationPages;
        applicationService = accessor.getApplicationService();
        this.convertor = convertor;
        this.loggedUserId = loggedUserId;
        this.searchApplications = searchApplications;
    }

    public Application createApplication(final ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException, InvalidNameException {
        try {
            final SApplication sApplication = applicationService.createApplication(convertor.buildSApplication(applicationCreator, loggedUserId));
            return convertor.toApplication(sApplication);
        } catch (final SObjectCreationException e) {
            throw new CreationException(e);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SInvalidNameException e) {
            throw new InvalidNameException(e.getMessage());
        }
    }

    public Application getApplication(final long applicationId) throws ApplicationNotFoundException {
        try {
            final SApplication sApplication = applicationService.getApplication(applicationId);
            return convertor.toApplication(sApplication);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        }
    }

    public void deleteApplication(final long applicationId) throws DeletionException {
        try {
            applicationService.deleteApplication(applicationId);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    public void setApplicationHomePage(final long applicationId, final long applicationPageId) throws UpdateException {
        final SApplicationUpdateBuilder updateBuilder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(new EntityUpdateDescriptor());
        updateBuilder.updateHomePageId(applicationPageId);
        try {
            applicationService.updateApplication(applicationId, updateBuilder.done());
        } catch (final SObjectModificationException e) {
            throw new UpdateException(e);
        }
    }

    public SearchResult<Application> searchApplications() throws SearchException {
        try {
            searchApplications.execute();
            return searchApplications.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    public ApplicationPage createApplicationPage(final long applicationId, final long pagedId, final String name) throws AlreadyExistsException,
    CreationException, InvalidNameException {
        final SApplicationPageBuilderFactory factory = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        final SApplicationPageBuilder builder = factory.createNewInstance(applicationId, pagedId, name);
        SApplicationPage sAppPage;
        try {
            sAppPage = applicationService.createApplicationPage(builder.done());
            return convertor.toApplicationPage(sAppPage);
        } catch (final SObjectCreationException e) {
            throw new CreationException(e);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SInvalidNameException e) {
            throw new InvalidNameException(e.getMessage());
        }
    }

    public ApplicationPage getApplicationPage(final String applicationName, final String applicationPageName) throws ApplicationPageNotFoundException {
        try {
            final SApplicationPage sAppPage = applicationService.getApplicationPage(applicationName, applicationPageName);
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

    public void deleteApplicationPage(final long applicationpPageId) throws DeletionException {
        try {
            applicationService.deleteApplicationPage(applicationpPageId);
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
