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

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.FormMappingNotFoundException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UnauthorizedAccessException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.page.PageURL;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * Contains methods related to the configuration processes
 *
 * @author Baptiste Mesta
 * @since 7.0.0
 */
public interface ProcessConfigurationAPI {

    /**
     * Search for form mapping
     *
     * @param searchOptions
     *        search options to search for form mapping
     * @return the result of the search
     * @see org.bonitasoft.engine.form.FormMappingSearchDescriptor
     * @see org.bonitasoft.engine.form.FormMappingType
     * @since 7.0.0
     */
    SearchResult<FormMapping> searchFormMappings(SearchOptions searchOptions) throws SearchException;

    /**
     * Resolves a Page URL from a specific key.
     * 
     * @param key the key of the page to resolve.
     * @return the <code>PageURL</code> containing the pageId or the complete
     * @throws NotFoundException if the key does not match anything.
     * @see PageURL the structured PageURL that points to the Page or URL
     */
    //TODO add something like a boolean to ask if we check for secu or not if we get only web resources
    PageURL resolvePageOrURL(String key, Map<String,Serializable> context) throws NotFoundException, UnauthorizedAccessException, ExecutionException;

    /**
     * Update a form mapping with the given values
     * 
     * @param formMappingId
     *        the form mapping to update
     * @param url
     *        the name of the form or the url to the form
     * @param pageId
     * @throws org.bonitasoft.engine.exception.FormMappingNotFoundException
     *         when the formMappingId is not an existing form mapping
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         when there is an issue when updating the form mapping
     * @since 7.0.0
     */
    FormMapping updateFormMapping(final long formMappingId, final String url, Long pageId) throws FormMappingNotFoundException, UpdateException;
    FormMapping getFormMapping(final long formMappingId) throws FormMappingNotFoundException;
}
