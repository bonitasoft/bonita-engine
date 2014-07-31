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
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;

/**
 * This API allows to list and manage Bonita Living Applications.
 *
 * @author Elias Ricken de Medeiros
 * @see Application
 * @since 6.4
 */
public interface ApplicationAPI {

    /**
     * Creates a new {@link Application} based on the supplied {@link ApplicationCreator}
     *
     * @param applicationCreator creator describing characteristics of application to be created
     * @return the created {@link Application}
     * @throws AlreadyExistsException if an application already exists with the same name
     * @throws CreationException if an error occurs during the creation
     */
    Application createApplication(ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException;

    /**
     * Retrieves an {@link Application} from its identifier.
     *
     * @param applicationId the application identifier
     * @return an {@link Application} from its identifier.
     * @throws ApplicationNotFoundException if no application is found for the given identifier
     */
    Application getApplication(final long applicationId) throws ApplicationNotFoundException;

    /**
     * Deletes an application by its identifier
     *
     * @param applicationId the page identifier
     * @throws DeletionException if en error occurs during the deletion
     */
    void deleteApplication(long applicationId) throws DeletionException;

    /**
     * Searches for applications with specific search criteria.
     *
     * @param searchOptions the search options. See {@link SearchOptions} for details.
     * @return a {@link SearchResult} containing the number and the list of applications matching with the search criteria.
     * @throws SearchException
     */
    SearchResult<Application> searchApplications(final SearchOptions searchOptions) throws SearchException;

}
