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

import org.bonitasoft.engine.exception.FormMappingNotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
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
     * Get the form mapping that contains the link to the process start form
     * 
     * @param processDefinitionId
     *        the process
     * @return
     *         the form mapping of the process start form
     * @throws org.bonitasoft.engine.exception.FormMappingNotFoundException
     *         when the form mapping with these properties is not found
     * @since 7.0.0
     */
    FormMapping getProcessStartForm(long processDefinitionId) throws FormMappingNotFoundException;

    /**
     * Get the form mapping that contains the link to the process overview form
     * 
     * @param processDefinitionId
     *        the process
     * @return
     *         the form mapping of the process overview form
     * @throws org.bonitasoft.engine.exception.FormMappingNotFoundException
     *         when the form mapping with these properties is not found
     * @since 7.0.0
     */
    FormMapping getProcessOverviewForm(long processDefinitionId) throws FormMappingNotFoundException;

    /**
     * Get the form mapping that contains the link to the task form
     * 
     * @param processDefinitionId
     *        the process
     * @param taskName
     *        the name of the task
     * @return
     *         the form mapping of the task form
     * @throws org.bonitasoft.engine.exception.FormMappingNotFoundException
     *         when the form mapping with these properties is not found
     * @since 7.0.0
     */
    FormMapping getTaskForm(long processDefinitionId, String taskName) throws FormMappingNotFoundException;

    /**
     * Update a form mapping with the given values
     * 
     * @param formMappingId
     *        the form mapping to update
     * @param form
     *        the name of the form or the url to the form
     * @param target
     *        the type of the target form
     * @throws org.bonitasoft.engine.exception.FormMappingNotFoundException
     *         when the formMappingId is not an existing form mapping
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         when there is an issue when updating the form mapping
     * @since 7.0.0
     */
    void updateFormMapping(final long formMappingId, final String form, FormMappingTarget target) throws FormMappingNotFoundException, UpdateException;
}
