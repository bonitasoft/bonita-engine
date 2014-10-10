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
import com.bonitasoft.engine.business.application.ApplicationMenu;
import com.bonitasoft.engine.business.application.ApplicationMenuCreator;
import com.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationPage;
import com.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import com.bonitasoft.engine.business.application.ApplicationUpdater;
import com.bonitasoft.engine.exception.InvalidDisplayNameException;
import com.bonitasoft.engine.exception.InvalidTokenException;
import com.bonitasoft.engine.page.Page;

/**
 * This API allows to list and manage Bonita Living Applications ({@link Application}).
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
     * @return the created <code>Application</code>
     * @throws AlreadyExistsException if an application already exists with the same name
     * @throws CreationException if an error occurs during the creation
     * @throws InvalidTokenException if the token is empty or null or contains invalid characters. The token should contain only alpha numeric characters and
     *         the following special characters '-', '.', '_' or '~'.
     * @throws InvalidDisplayNameException if the display name is empty or null
     * @see Application
     * @see ApplicationCreator
     */
    Application createApplication(ApplicationCreator applicationCreator) throws AlreadyExistsException, CreationException, InvalidTokenException,
    InvalidDisplayNameException;

    /**
     * Retrieves an {@link Application} from its identifier.
     *
     * @param applicationId the application identifier
     * @return an <code>Application</code> from its identifier.
     * @throws ApplicationNotFoundException if no application is found for the given identifier
     * @see Application
     */
    Application getApplication(final long applicationId) throws ApplicationNotFoundException;

    /**
     * Deletes an {@link Application} by its identifier
     *
     * @param applicationId the <code>Application</code> identifier
     * @throws DeletionException if an error occurs during the deletion
     * @see Application
     */
    void deleteApplication(long applicationId) throws DeletionException;

    /**
     * Updates an {@link Application} based on the information supplied by the {@link ApplicationUpdater}
     *
     * @param applicationId a long representing the application identifier
     * @param updater an <code>ApplicationUpdater</code> describing the fields to be updated.
     * @return the <code>Application</code> as it is after the update.
     * @throws ApplicationNotFoundException if no <code>Application</code> is found for the given id
     * @throws AlreadyExistsException if another <code>Application</code> already exists with the new name value
     * @throws InvalidTokenException if the new token value is empty or null or contains invalid characters. The token should contain only alpha numeric
     *         characters and the following special characters '-', '.', '_' or '~'.
     * @throws InvalidDisplayNameException if the display name is empty
     * @throws UpdateException if an error occurs during the update
     * @see Application
     * @see ApplicationUpdater
     */
    Application updateApplication(long applicationId, ApplicationUpdater updater) throws ApplicationNotFoundException, UpdateException, AlreadyExistsException,
    InvalidTokenException,
    InvalidDisplayNameException;

    /**
     * Searches for {@link Application}s with specific search criteria. Use {@link ApplicationSearchDescriptor} to know the available filters.
     *
     * @param searchOptions the search criteria. See {@link SearchOptions} for details.
     * @return a {@link SearchResult} containing the number and the list of applications matching the search criteria.
     * @throws SearchException if an error occurs during search
     * @see Application
     * @see ApplicationSearchDescriptor
     * @see SearchOptions
     * @see SearchResult
     */
    SearchResult<Application> searchApplications(final SearchOptions searchOptions) throws SearchException;

    /**
     * Creates an {@link ApplicationPage}
     *
     * @param applicationId the identifier of the {@link Application} to which the {@link Page} will be associated
     * @param pagedId the identifier of <code>Page</code> to be associated to the <code>Application</code>
     * @param token the token that this <code>Page</code> will take in this <code>ApplicationPage</code>. The token must be unique for a given application and
     *        should contain only alpha numeric characters and the following special characters '-', '.', '_' or '~'.
     * @return the created {@link ApplicationPage}
     * @throws AlreadyExistsException if the token is already used by another <code>ApplicationPage</code> on this <code>Application</code>
     * @throws CreationException if an error occurs during the creation
     * @see ApplicationPage
     * @see Application
     * @see Page
     */
    ApplicationPage createApplicationPage(long applicationId, long pagedId, String token) throws AlreadyExistsException, CreationException, InvalidTokenException;

    /**
     * Retrieves the {@link ApplicationPage} for the given <code>Application</code> name and <code>ApplicationPage</code> token
     *
     * @param applicationName the <code>Application</code> name
     * @param applicationPageToken the <code>ApplicationPage</code> token
     * @return the {@link ApplicationPage} for the given application name and application page name
     * @throws ApplicationPageNotFoundException if no {@link ApplicationPage} is found for the given <code>Application</code> name and
     *         <code>ApplicationPage</code> token
     */
    ApplicationPage getApplicationPage(String applicationName, String applicationPageToken) throws ApplicationPageNotFoundException;

    /**
     * Retrieves the {@link ApplicationPage} from its identifier
     *
     * @param applicationPageId the application page identifier
     * @return the {@link ApplicationPage} from its identifier
     * @throws ApplicationPageNotFoundException if no {@link ApplicationPage} is found for the given identifier
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
     * @return a {@link SearchResult} containing the number and the list of application pages matching the search criteria.
     * @throws SearchException if an error occurs during search
     */
    SearchResult<ApplicationPage> searchApplicationPages(final SearchOptions searchOptions) throws SearchException;

    /**
     * Defines the home page for the application
     *
     * @param applicationId the {@link Application} identifier
     * @param applicationPageId the identifier of the {@link ApplicationPage} to be used as home page
     * @throws ApplicationNotFoundException if no <code>Applicaton</code> is found with the given id
     * @throws UpdateException if an error occurs during the update
     * @throws AlreadyExistsException if update with an already existing Name
     * @throws InvalidTokenException if the name is empty
     * @throws InvalidDisplayNameException if the display name is empty
     */
    void setApplicationHomePage(long applicationId, long applicationPageId) throws UpdateException, InvalidTokenException,
    InvalidDisplayNameException, AlreadyExistsException, ApplicationNotFoundException;

    /**
     * Retrieves the application home page
     *
     * @param applicationId the {@link Application} identifier
     * @return the application home page
     * @throws ApplicationPageNotFoundException if no home page is found for the given application
     */
    ApplicationPage getApplicationHomePage(long applicationId) throws ApplicationPageNotFoundException;

    /**
     * Creates a new {@link ApplicationMenu} based on the supplied {@link ApplicationMenuCreator}
     *
     * @param applicationMenuCreator creator describing characteristics of application menu to be created
     * @return the created {@link ApplicationMenu}
     * @throws CreationException if an error occurs during the creation
     */
    ApplicationMenu createApplicationMenu(ApplicationMenuCreator applicationMenuCreator) throws CreationException;

    /**
     * Retrieves the {@link ApplicationMenu} from its identifier
     *
     * @param applicationMenuId the application menu identifier
     * @return the {@link ApplicationMenu} from its identifier
     * @throws ApplicationMenuNotFoundException if no {@link ApplicationMenu} is found for the given identifier
     */
    ApplicationMenu getApplicationMenu(long applicationMenuId) throws ApplicationMenuNotFoundException;

    /**
     * Deletes an {@link ApplicationMenu} by its identifier
     *
     * @param applicationMenuId the {@link ApplicationMenu} identifier
     * @throws DeletionException if an error occurs during the deletion
     */
    void deleteApplicationMenu(long applicationMenuId) throws DeletionException;

    /**
     * Searches for application menus with specific search criteria.
     *
     * @param searchOptions the search options. See {@link SearchOptions} for details.
     * @return a {@link SearchResult} containing the number and the list of application menus matching the search criteria.
     * @throws SearchException if an error occurs during search
     */
    SearchResult<ApplicationMenu> searchApplicationMenus(final SearchOptions searchOptions) throws SearchException;

}
