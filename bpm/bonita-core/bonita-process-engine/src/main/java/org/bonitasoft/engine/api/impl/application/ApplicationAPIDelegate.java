/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl.application;

import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplications;
import org.bonitasoft.engine.api.impl.validator.TokenValidator;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationField;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.business.application.model.SApplication;
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
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIDelegate {

    private final ApplicationModelConverter converter;
    private final long loggedUserId;
    private final ApplicationService applicationService;

    public ApplicationAPIDelegate(final TenantServiceAccessor accessor, final ApplicationModelConverter converter, final long loggedUserId) {
        applicationService = accessor.getApplicationService();
        this.converter = converter;
        this.loggedUserId = loggedUserId;
    }

    public Application createApplication(final ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException {
        try {
            validateCreator(applicationCreator);
            final SApplication sApplication = applicationService.createApplication(converter.buildSApplication(applicationCreator, loggedUserId));
            return converter.toApplication(sApplication);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    private void validateCreator(final ApplicationCreator applicationCreator) throws CreationException {
        final TokenValidator tokenValidator = new TokenValidator(applicationCreator.getToken());
        if (!tokenValidator.validate()) {
            throw new CreationException(tokenValidator.getError());
        }
        final String displayName = (String) applicationCreator.getFields().get(ApplicationField.DISPLAY_NAME);
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new CreationException("The application display name can not be null or empty");
        }
    }

    public Application getApplication(final long applicationId) throws ApplicationNotFoundException {
        try {
            final SApplication sApplication = applicationService.getApplication(applicationId);
            return converter.toApplication(sApplication);
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

    public SearchResult<Application> searchApplications(final SearchApplications searchApplications) throws SearchException {
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
                sApplication = applicationService.updateApplication(applicationId, converter.toApplicationUpdateDescriptor(updater, loggedUserId));
            } else {
                sApplication = applicationService.getApplication(applicationId);
            }
            return converter.toApplication(sApplication);
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
            final String displayName = (String) updater.getFields().get(ApplicationField.DISPLAY_NAME);
            if (displayName == null || displayName.trim().isEmpty()) {
                throw new UpdateException("The application display name can not be null or empty");
            }
        }
    }

    private void validateToken(final ApplicationUpdater updater) throws UpdateException {
        if (updater.getFields().keySet().contains(ApplicationField.TOKEN)) {
            final String token = (String) updater.getFields().get(ApplicationField.TOKEN);
            final TokenValidator tokenValidator = new TokenValidator(token);
            if (!tokenValidator.validate()) {
                throw new UpdateException(tokenValidator.getError());
            }
        }
    }

}
