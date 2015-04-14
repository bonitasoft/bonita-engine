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
package org.bonitasoft.engine.api;

import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipContentException;
import org.bonitasoft.engine.exception.InvalidPageZipInconsistentException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingIndexException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageTokenException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageZipContentException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * This API gives access to all page features. Page is a way to add pages on portal.
 * <p>
 * Also allows to manipulate <code>Page</code>s, through creation, deletion, search.
 * </p>
 *
 * @author Laurent Leseigneur
 * @see org.bonitasoft.engine.page.Page
 */
public interface PageAPI {

    /**
     * Retrieves a page from its ID.
     *
     * @param pageId
     *        the Identifier of the page to retrieve
     * @return the found page
     * @throws org.bonitasoft.engine.page.PageNotFoundException
     *         if no page can be found with the provided ID.
     */
    Page getPage(final long pageId) throws PageNotFoundException;

    /**
     * Retrieves a page from its name.
     *
     * @param name
     *        the name of the page to retrieve
     * @return the found page
     * @throws PageNotFoundException
     *         if no page can be found with the provided page or is assigned with a process definition.
     */
    Page getPageByName(final String name) throws PageNotFoundException;

    /**
     * Retrieves a page from its name and processDefinitionId.
     *
     * @param name
     *        the name of the page to retrieve
     * @param processDefinitionId
     *        the process definition ID associated to the page
     * @return the found page
     * @throws PageNotFoundException
     *         if no page can be found with the provided name and process definition ID.
     */
    Page getPageByNameAndProcessDefinitionId(final String name, long processDefinitionId) throws PageNotFoundException;

    /**
     * Retrieves the binary content of a page.
     *
     * @param pageId
     *        the ID of the page to extract the content for.
     * @return
     *         the binary content of the page.
     * @throws PageNotFoundException
     *         if no page can be found with the provided ID.
     */
    byte[] getPageContent(final long pageId) throws PageNotFoundException;

    /**
     * Searches for pages with specific search criteria.
     *
     * @param searchOptions
     *        the search options for the search. See {@link org.bonitasoft.engine.search.SearchOptions} for search option details.
     * @return the <code>SearchResult</code> containing
     * @throws org.bonitasoft.engine.exception.SearchException
     *         if a problem occurs during the search.
     */
    SearchResult<Page> searchPages(final SearchOptions searchOptions) throws SearchException;

    /**
     * Creates a custom page.
     *
     * @param pageCreator
     *        the creator object to instantiate the new page.
     * @param content
     *        the binary content of the page.
     * @return the newly created page.
     * @throws org.bonitasoft.engine.exception.AlreadyExistsException
     *         if a page with this name already exists.
     * @throws org.bonitasoft.engine.exception.CreationException
     *         if an error occurs during the creation.
     */
    Page createPage(final PageCreator pageCreator, final byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException;

    /**
     * Updates a custom page.
     *
     * @param pageId
     *        the Identifier of the page to update
     * @param pageUpdater
     *        the creator object to instantiate the new page.
     * @return the newly created page.
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         if an error occurs during the update.
     * @throws org.bonitasoft.engine.exception.AlreadyExistsException
     *         if a page with this name already exists.
     */
    Page updatePage(final long pageId, final PageUpdater pageUpdater) throws UpdateException, AlreadyExistsException, UpdatingWithInvalidPageTokenException,
            UpdatingWithInvalidPageZipContentException;

    /**
     * Updates a custom page content.
     * it read the page.properties inside to update the page properties
     *
     * @param pageId
     *        the Identifier of the page to update
     * @param content
     *        the binary content of the page.
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         if an error occurs during the update.
     */
    void updatePageContent(final long pageId, final byte[] content) throws UpdateException, UpdatingWithInvalidPageTokenException,
            UpdatingWithInvalidPageZipContentException;

    /**
     * Deletes a page identified by its ID.
     *
     * @param pageId
     *        the page identifier to delete.
     * @throws org.bonitasoft.engine.exception.DeletionException
     *         if a problem occurs during deletion.
     */
    void deletePage(final long pageId) throws DeletionException;

    /**
     * Deletes a list of pages, given by their IDs.
     *
     * @param pageIds
     *        a list of page identifiers to delete.
     * @throws org.bonitasoft.engine.exception.DeletionException
     *         if a problem occurs during deletion.
     */
    void deletePages(final List<Long> pageIds) throws DeletionException;

    /**
     * create a page using the given content
     * the content must contain a page.properties file that contains informations on the page:
     * name, displayName and description
     *
     * @param contentName
     *        name of the zip file containing the page
     * @param content
     *        content of the zip file containing the page
     * @return
     *         the created page
     * @throws org.bonitasoft.engine.exception.AlreadyExistsException
     *         if a page with the same name already exists
     * @throws org.bonitasoft.engine.exception.CreationException
     * @since 6.3.1
     */
    Page createPage(String contentName, byte[] content) throws AlreadyExistsException, CreationException, InvalidPageTokenException,
            InvalidPageZipContentException;

    /**
     * Read the content of the page zip file check it is consistent and return it's properties
     *
     * @param content
     *        content of the zip file containing the page
     * @return
     *         the properties of the page
     * @since 6.4.0
     */
    Properties getPageProperties(byte[] content, boolean checkIfItAlreadyExists) throws InvalidPageTokenException,
            AlreadyExistsException, InvalidPageZipMissingPropertiesException, InvalidPageZipMissingIndexException, InvalidPageZipInconsistentException,
            InvalidPageZipMissingAPropertyException;

}
