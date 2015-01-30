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

import java.util.List;

import org.bonitasoft.engine.api.impl.converter.ApplicationPageModelConverter;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationPages;
import org.bonitasoft.engine.api.impl.validator.TokenValidator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationPageBuilderFactory;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
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
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationPageAPIDelegate {

    private final ApplicationPageModelConverter converter;
    private final ApplicationService applicationService;
    private final long loggedUserId;

    public ApplicationPageAPIDelegate(final TenantServiceAccessor accessor, final ApplicationPageModelConverter converter, final long loggedUserId) {
        applicationService = accessor.getApplicationService();
        this.converter = converter;
        this.loggedUserId = loggedUserId;
    }

    public void setApplicationHomePage(final long applicationId, final long applicationPageId) throws UpdateException, ApplicationNotFoundException {
        final SApplicationUpdateBuilder sApplicationUpdateBuilder = BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(loggedUserId)
                .updateHomePageId(applicationPageId);
        try {
            applicationService.updateApplication(applicationId, sApplicationUpdateBuilder.done());
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationNotFoundException(applicationId);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    public ApplicationPage createApplicationPage(final long applicationId, final long pageId, final String token) throws AlreadyExistsException,
            CreationException {
        validateToken(token);
        final SApplicationPageBuilderFactory factory = BuilderFactory.get(SApplicationPageBuilderFactory.class);
        final SApplicationPageBuilder pageBuilder = factory.createNewInstance(applicationId, pageId, token);
        SApplicationPage sAppPage;
        try {
            sAppPage = applicationService.createApplicationPage(pageBuilder.done());
            applicationService.updateApplication(applicationId, BuilderFactory.get(SApplicationUpdateBuilderFactory.class).createNewInstance(loggedUserId)
                    .done());
            return converter.toApplicationPage(sAppPage);
        } catch (final SObjectCreationException e) {
            throw new CreationException(e);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    private void validateToken(final String token) throws CreationException {
        final TokenValidator tokenValidator = new TokenValidator(token);
        if (!tokenValidator.validate()) {
            throw new CreationException(tokenValidator.getError());
        }
    }

    public ApplicationPage getApplicationPage(final String applicationName, final String applicationPageToken) throws ApplicationPageNotFoundException {
        try {
            final SApplicationPage sAppPage = applicationService.getApplicationPage(applicationName, applicationPageToken);
            return converter.toApplicationPage(sAppPage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public ApplicationPage getApplicationPage(final long applicationPageId) throws ApplicationPageNotFoundException {
        try {
            final SApplicationPage sApplicationPage = applicationService.getApplicationPage(applicationPageId);
            return converter.toApplicationPage(sApplicationPage);
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
            return converter.toApplicationPage(sHomePage);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationPageNotFoundException(e.getMessage());
        }
    }

    public SearchResult<ApplicationPage> searchApplicationPages(final SearchApplicationPages searchApplicationPages) throws SearchException {
        try {
            searchApplicationPages.execute();
            return searchApplicationPages.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

    public List<String> getAllPagesForProfile(final long profileId) {
        try {
            return applicationService.getAllPagesForProfile(profileId);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }
}
