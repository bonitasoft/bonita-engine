/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIDelegate {

    private final TenantServiceAccessor accessor;
    private final ApplicationConvertor convertor;
    private final long loggedUserId;

    public ApplicationAPIDelegate(final TenantServiceAccessor accessor, final ApplicationConvertor convertor, final long loggedUserId) {
        this.accessor = accessor;
        this.convertor = convertor;
        this.loggedUserId = loggedUserId;
    }

    public Application createApplication(final ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException {
        final ApplicationService applicationService = accessor.getApplicationService();
        try {
            final SApplication sApplication = applicationService.createApplication(convertor.buildSApplication(applicationCreator, loggedUserId));
            return convertor.toApplication(sApplication);
        } catch (final SObjectCreationException e) {
            throw new CreationException(e);
        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        }
    }

    public Application getApplication(final long applicationId) throws ApplicationNotFoundException {
        final ApplicationService applicationService = accessor.getApplicationService();
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
        final ApplicationService applicationService = accessor.getApplicationService();
        try {
            applicationService.deleteApplication(applicationId);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

}
