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
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import com.bonitasoft.engine.api.impl.validator.TokenValidator;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationField;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.ApplicationUpdater;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIDelegate {

    private final ApplicationConvertor convertor;
    private final long loggedUserId;
    private final SearchApplications searchApplications;
    private final ApplicationService applicationService;

    public ApplicationAPIDelegate(final TenantServiceAccessor accessor, final ApplicationConvertor convertor, final long loggedUserId,
            final SearchApplications searchApplications) {
        applicationService = accessor.getApplicationService();
        this.convertor = convertor;
        this.loggedUserId = loggedUserId;
        this.searchApplications = searchApplications;
    }

    public Application createApplication(final ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException {
        try {
            validateCreator(applicationCreator);
            final SApplication sApplication = applicationService.createApplication(convertor.buildSApplication(applicationCreator, loggedUserId));
            return convertor.toApplication(sApplication);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    private void validateCreator(final ApplicationCreator applicationCreator) throws CreationException {
        TokenValidator tokenValidator = new TokenValidator(applicationCreator.getToken());
        if (!tokenValidator.validate()) {
            throw new CreationException(tokenValidator.getError());
        }
        String displayName = (String) applicationCreator.getFields().get(ApplicationField.DISPLAY_NAME);
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new CreationException("The application display name can not be null or empty");
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

    public SearchResult<Application> searchApplications() throws SearchException {
        try {
            searchApplications.execute();
            return searchApplications.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    public Application updateApplication(final long applicationId, final ApplicationUpdater updater) throws UpdateException,
            AlreadyExistsException, ApplicationNotFoundException {
        try {
            validateUpdater(updater);
            SApplication sApplication;
            if (!updater.getFields().isEmpty()) {
                sApplication = applicationService.updateApplication(applicationId, convertor.toApplicationUpdateDescriptor(updater, loggedUserId));
            } else {
                sApplication = applicationService.getApplication(applicationId);
            }
            return convertor.toApplication(sApplication);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    private void validateUpdater(final ApplicationUpdater updater) throws UpdateException {
        validateToken(updater);
        validateDisplayName(updater);
    }

    private void validateDisplayName(final ApplicationUpdater updater) throws UpdateException {
        if (updater.getFields().keySet().contains(ApplicationField.DISPLAY_NAME)) {
            String displayName = (String) updater.getFields().get(ApplicationField.DISPLAY_NAME);
            if (displayName == null || displayName.trim().isEmpty()) {
                throw new UpdateException("The application display name can not be null or empty");
            }
        }
    }

    private void validateToken(final ApplicationUpdater updater) throws UpdateException {
        if (updater.getFields().keySet().contains(ApplicationField.TOKEN)) {
            String token = (String) updater.getFields().get(ApplicationField.TOKEN);
            TokenValidator tokenValidator = new TokenValidator(token);
            if (!tokenValidator.validate()) {
                throw new UpdateException(tokenValidator.getError());
            }
        }
    }

}
