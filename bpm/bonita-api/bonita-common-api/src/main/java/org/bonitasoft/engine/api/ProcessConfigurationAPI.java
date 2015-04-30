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
    PageURL resolvePageOrURL(String key, Map<String, Serializable> context, boolean executeAuthorizationRules) throws NotFoundException, UnauthorizedAccessException, ExecutionException;

    /**
     * @param formMappingId
     * @return
     * @throws FormMappingNotFoundException
     */
    FormMapping getFormMapping(final long formMappingId) throws FormMappingNotFoundException;
}
