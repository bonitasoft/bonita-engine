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

import org.bonitasoft.engine.api.impl.converter.ApplicationMenuModelConverter;
import org.bonitasoft.engine.api.impl.transaction.application.SearchApplicationMenus;
import org.bonitasoft.engine.api.impl.validator.ApplicationMenuCreatorValidator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.SApplicationUpdateBuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuAPIDelegate {

    private final ApplicationMenuModelConverter converter;
    private final ApplicationService applicationService;
    private final ApplicationMenuCreatorValidator creatorValidator;
    private final long loggedUserId;

    public ApplicationMenuAPIDelegate(final TenantServiceAccessor accessor, final ApplicationMenuModelConverter converter,
            final ApplicationMenuCreatorValidator creatorValidator, final long loggedUserId) {
        this.creatorValidator = creatorValidator;
        this.loggedUserId = loggedUserId;
        applicationService = accessor.getApplicationService();
        this.converter = converter;
    }

    public ApplicationMenu createApplicationMenu(final ApplicationMenuCreator applicationMenuCreator) throws CreationException {
        try {
            if (!creatorValidator.isValid(applicationMenuCreator)) {
                throw new CreationException("The ApplicationMenuCreator is invalid. Problems: " + creatorValidator.getProblems());
            }
            final int index = applicationService.getNextAvailableIndex(applicationMenuCreator.getParentId());
            final SApplicationMenu sApplicationMenu = applicationService.createApplicationMenu(converter.buildSApplicationMenu(applicationMenuCreator, index));
            applicationService.updateApplication(sApplicationMenu.getApplicationId(), BuilderFactory.get(SApplicationUpdateBuilderFactory.class)
                    .createNewInstance(loggedUserId).done());
            return converter.toApplicationMenu(sApplicationMenu);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    public ApplicationMenu updateApplicationMenu(final long applicationMenuId, final ApplicationMenuUpdater updater) throws ApplicationMenuNotFoundException,
            UpdateException {
        final EntityUpdateDescriptor updateDescriptor = converter.toApplicationMenuUpdateDescriptor(updater);
        try {
            final SApplicationMenu sApplicationMenu = applicationService.updateApplicationMenu(applicationMenuId, updateDescriptor);
            applicationService.updateApplication(sApplicationMenu.getApplicationId(), BuilderFactory.get(SApplicationUpdateBuilderFactory.class)
                    .createNewInstance(loggedUserId).done());
            return converter.toApplicationMenu(sApplicationMenu);
        } catch (final SObjectModificationException e) {
            throw new UpdateException(e);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationMenuNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    public ApplicationMenu getApplicationMenu(final long applicationMenuId) throws ApplicationMenuNotFoundException {
        try {
            final SApplicationMenu sApplicationMenu = applicationService.getApplicationMenu(applicationMenuId);
            return converter.toApplicationMenu(sApplicationMenu);
        } catch (final SObjectNotFoundException e) {
            throw new ApplicationMenuNotFoundException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    public void deleteApplicationMenu(final long applicationMenuId) throws DeletionException {
        try {
            final SApplicationMenu deletedApplicationMenu = applicationService.deleteApplicationMenu(applicationMenuId);
            applicationService.updateApplication(deletedApplicationMenu.getApplicationId(), BuilderFactory.get(SApplicationUpdateBuilderFactory.class)
                    .createNewInstance(loggedUserId).done());
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    public SearchResult<ApplicationMenu> searchApplicationMenus(final SearchApplicationMenus searchApplicationMenus) throws SearchException {
        try {
            searchApplicationMenus.execute();
            return searchApplicationMenus.getResult();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
    }

}
