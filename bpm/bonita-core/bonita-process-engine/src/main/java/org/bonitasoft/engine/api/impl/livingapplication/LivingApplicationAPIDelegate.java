/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl.livingapplication;

import java.util.Optional;
import java.util.function.Predicate;

import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.business.application.*;
import org.bonitasoft.engine.business.application.impl.IconImpl;
import org.bonitasoft.engine.business.application.importer.validator.ApplicationTokenValidator;
import org.bonitasoft.engine.business.application.importer.validator.ValidationStatus;
import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.ServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class LivingApplicationAPIDelegate {

    private final ApplicationModelConverter converter;
    private final long loggedUserId;
    private final ApplicationService applicationService;
    private final ApplicationTokenValidator tokenValidator;

    public LivingApplicationAPIDelegate(final ServiceAccessor accessor, final ApplicationModelConverter converter,
            final long loggedUserId, final ApplicationTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
        applicationService = accessor.getApplicationService();
        this.converter = converter;
        this.loggedUserId = loggedUserId;
    }

    /**
     * @deprecated as of 9.0.0, Applications should be created at startup.
     */
    @Deprecated(since = "9.0.0")
    public Application createApplication(final ApplicationCreator applicationCreator)
            throws CreationException {
        try {
            validateCreator(applicationCreator);
            final SApplicationWithIcon sApplicationWithIcon = applicationService
                    .createApplication(converter.buildSApplication(applicationCreator, loggedUserId));
            var converted = converter.toApplication(sApplicationWithIcon);
            if (converted instanceof Application res) {
                return res;
            } else {
                // should not occur anyway
                throw new CreationException("Use dedicated API for application links.");
            }
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    /**
     * @deprecated as of 9.0.0, Applications should be created at startup. This also concerns application links
     *             introduced in 10.2.0.
     */
    @Deprecated(since = "10.2.0")
    public ApplicationLink createApplicationLink(final ApplicationLinkCreator applicationLinkCreator)
            throws CreationException {
        try {
            validateCreator(applicationLinkCreator);
            final SApplicationWithIcon sApplicationWithIcon = applicationService
                    .createApplication(converter.buildSApplication(applicationLinkCreator, loggedUserId));
            var converted = converter.toApplication(sApplicationWithIcon);
            if (converted instanceof ApplicationLink res) {
                return res;
            } else {
                // should not occur anyway
                throw new CreationException("Use dedicated API for legacy applications.");
            }
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    private void validateCreator(final AbstractApplicationCreator<?> applicationCreator) throws CreationException {
        ValidationStatus validationStatus = tokenValidator.validate(applicationCreator.getToken());
        if (!validationStatus.isValid()) {
            throw new CreationException(validationStatus.getMessage());
        }
        final String displayName = (String) applicationCreator.getFields().get(ApplicationField.DISPLAY_NAME);
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new CreationException("The application display name can not be null or empty");
        }
        Class<? extends IApplication> appClass = applicationCreator.isLink() ? ApplicationLink.class
                : Application.class;
        if (!applicationCreator.getFields().keySet().stream().allMatch(k -> k.isForClass(appClass))) {
            throw new CreationException("The application fields used must be valid for the application type");
        }
    }

    public IApplication getIApplication(final long applicationId) throws ApplicationNotFoundException {
        try {
            return converter.toApplication(applicationService.getApplication(applicationId));
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        }
    }

    public IApplication getIApplicationByToken(final String applicationToken) throws ApplicationNotFoundException {
        try {
            SApplication applicationByToken = applicationService.getApplicationByToken(applicationToken);
            if (applicationByToken == null) {
                throw new ApplicationNotFoundException(applicationToken);
            }
            return converter.toApplication(applicationByToken);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    public Icon getIconOfApplication(final long applicationId) throws ApplicationNotFoundException {
        try {
            SApplicationWithIcon application = applicationService.getApplicationWithIcon(applicationId);
            if (application.hasIcon()) {
                return new IconImpl(application.getIconMimeType(), application.getIconContent());
            }
            return null;
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        }
    }

    public void deleteApplication(final long applicationId) throws DeletionException {
        try {
            applicationService.deleteApplication(applicationId);
        } catch (final SObjectNotFoundException sonfe) {
            throw new DeletionException(new ApplicationNotFoundException(applicationId));
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    public <E extends IApplication> SearchResult<E> searchIApplications(
            final AbstractSearchEntity<E, SApplication> searchApplications)
            throws SearchException {
        try {
            searchApplications.execute();
            return searchApplications.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    /**
     * @deprecated as of 9.0.0, Applications should be updated at startup.
     */
    @Deprecated(since = "9.0.0")
    public Application updateApplication(final long applicationId, final ApplicationUpdater updater)
            throws UpdateException,
            AlreadyExistsException, ApplicationNotFoundException {
        try {
            validateUpdater(updater);
            AbstractSApplication application;
            if (!updater.getFields().isEmpty()) {
                /*
                 * This API may be called within our without a transaction.
                 * So we must check first whether the application is a link to have a consistent behavior
                 * and never update the application link.
                 */
                if (Optional.ofNullable(applicationService.getApplicationWithIcon(applicationId))
                        .filter(AbstractSApplication::isLink).isPresent()) {
                    throw new UpdateException("Use dedicated API for application links.");
                }
                application = applicationService.updateApplication(applicationId,
                        converter.toApplicationUpdateDescriptor(updater, loggedUserId));
            } else {
                application = applicationService.getApplication(applicationId);
            }
            var converted = converter.toApplication(application);
            if (converted instanceof Application res) {
                return res;
            } else {
                throw new UpdateException("Use dedicated API for application links.");
            }
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    /**
     * @deprecated as of 9.0.0, Applications should be updated at startup. This also concerns application links
     *             introduced in 10.2.0.
     */
    @Deprecated(since = "10.2.0")
    public ApplicationLink updateApplicationLink(final long applicationId, final ApplicationLinkUpdater updater)
            throws UpdateException,
            AlreadyExistsException, ApplicationNotFoundException {
        try {
            validateUpdater(updater);
            AbstractSApplication application;
            if (!updater.getFields().isEmpty()) {
                /*
                 * This API may be called within our without a transaction.
                 * So we must check first whether the application is a link to have a consistent behavior
                 * and never update the legacy application.
                 */
                Predicate<SApplicationWithIcon> isLink = AbstractSApplication::isLink;
                if (Optional.ofNullable(applicationService.getApplicationWithIcon(applicationId))
                        .filter(isLink.negate()).isPresent()) {
                    throw new UpdateException("Use dedicated API for legacy applications.");
                }
                application = applicationService.updateApplication(applicationId,
                        converter.toApplicationUpdateDescriptor(updater, loggedUserId));
            } else {
                application = applicationService.getApplication(applicationId);
            }
            var converted = converter.toApplication(application);
            if (converted instanceof ApplicationLink res) {
                return res;
            } else {
                throw new UpdateException("Use dedicated API for legacy applications.");
            }
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    private void validateUpdater(final AbstractApplicationUpdater<?> updater) throws UpdateException {
        validateToken(updater);
        validateDisplayName(updater);
        Class<? extends IApplication> appClass = updater instanceof ApplicationLinkUpdater ? ApplicationLink.class
                : Application.class;
        if (!updater.getFields().keySet().stream().allMatch(k -> k.isForClass(appClass))) {
            throw new UpdateException("The application fields used must be valid for the application type");
        }
    }

    private void validateDisplayName(final AbstractApplicationUpdater<?> updater) throws UpdateException {
        if (updater.getFields().keySet().contains(ApplicationField.DISPLAY_NAME)) {
            final String displayName = (String) updater.getFields().get(ApplicationField.DISPLAY_NAME);
            if (displayName == null || displayName.trim().isEmpty()) {
                throw new UpdateException("The application display name can not be null or empty");
            }
        }
    }

    private void validateToken(final AbstractApplicationUpdater<?> updater) throws UpdateException {
        if (updater.getFields().keySet().contains(ApplicationField.TOKEN)) {
            final String token = (String) updater.getFields().get(ApplicationField.TOKEN);
            ValidationStatus validationStatus = tokenValidator.validate(token);
            if (!validationStatus.isValid()) {
                throw new UpdateException(validationStatus.getMessage());
            }
        }
    }

}
