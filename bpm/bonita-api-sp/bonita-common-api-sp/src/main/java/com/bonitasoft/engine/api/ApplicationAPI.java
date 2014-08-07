/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import com.bonitasoft.engine.page.Page;

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
     * @throws DeletionException if an error occurs during the deletion
     */
    void deleteApplication(long applicationId) throws DeletionException;

    /**
     * Searches for applications with specific search criteria.
     *
     * @param searchOptions the search options. See {@link SearchOptions} for details.
     * @return a {@link SearchResult} containing the number and the list of applications matching the search criteria.
     * @throws SearchException if an error occurs during search
     */
    SearchResult<Application> searchApplications(final SearchOptions searchOptions) throws SearchException;

    /**
     * Creates an {@link ApplicationPage} (association between a {@link Page} and an {@link Application}).
     *
     * @param applicationId the identifier of the application where the page will be associated
     * @param pagedId the identifier of page to be associated to the application
     * @param name the name that this page will take in this application. The name must be unique for a given application.
     * @return the created {@link ApplicationPage}
     * @throws AlreadyExistsException if the name is already used for another page on this application
     * @throws CreationException if an error occurs during the creation
     */
    ApplicationPage createApplicationPage(long applicationId, long pagedId, String name) throws AlreadyExistsException, CreationException;

    /**
     * Retrieves the {@link ApplicationPage} for the given application name and application page name
     *
     * @param applicationName the application name
     * @param applicationPageName the application page name
     * @return the {@link ApplicationPage} for the given application name and application page name
     * @throws ApplicationPageNotFoundException if no {@link ApplicationPage} is found for the given application name and application page name
     */
    ApplicationPage getApplicationPage(String applicationName, String applicationPageName) throws ApplicationPageNotFoundException;

    /**
     * Retrieves the {@link ApplicationPage} from its identifier
     *
     * @param applicationPageId the application page identifier
     * @return the {@link ApplicationPage} from its identifier
     * @throws ApplicationPageNotFoundException if no {@link ApplicationPage} for the given identifier
     */
    ApplicationPage getApplicationPage(long applicationPageId) throws ApplicationPageNotFoundException;

    /**
     * Deletes an {@link ApplicationPage} by its identifier
     *
     * @param applicationpPageId the {@link ApplicationPage} identifier
     * @throws DeletionException if an error occurs during the deletion
     */
    void deleteApplicationPage(long applicationpPageId) throws DeletionException;

    /**
     * Searches for application pages with specific search criteria.
     *
     * @param searchOptions the search options. See {@link SearchOptions} for details.
     * @return SearchException a {@link SearchResult} containing the number and the list of application pages matching the search criteria.
     * @throws if an error occurs during search
     */
    SearchResult<ApplicationPage> searchApplicationPages(final SearchOptions searchOptions) throws SearchException;

    /**
     * Defines the home page for the application
     *
     * @param applicationId the {@link Application} identifier
     * @param applicationPageId the identifier of the {@link ApplicationPage} to be used as home page
     * @throws UpdateException if an error occurs during the home page definition
     */
    void setApplicationHomePage(long applicationId, long applicationPageId) throws UpdateException;

    /**
     * Retrieves the application home page
     *
     * @param applicationId the {@link Application} identifier
     * @return the application home page
     * @throws ApplicationPageNotFoundException if no home page is found for the given application
     */
    ApplicationPage getApplicationHomePage(long applicationId) throws ApplicationPageNotFoundException;

}
