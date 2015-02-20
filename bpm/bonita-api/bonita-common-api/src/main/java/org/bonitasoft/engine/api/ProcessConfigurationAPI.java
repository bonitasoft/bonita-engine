/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.api;

import org.bonitasoft.engine.form.mapping.FormMapping;
import org.bonitasoft.engine.form.mapping.FormMappingType;
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
     * @since 7.0.0
     */
    SearchResult<FormMapping> searchFormMappings(SearchOptions searchOptions);

    /**
     * Get the form mapping that contains the link to the process instantiation form
     * 
     * @param processDefinitionId
     *        the process
     * @return
     *         the form mapping of the process instantiation form
     */
    FormMapping getProcessStartForm(long processDefinitionId);

    /**
     * Get the form mapping that contains the link to the process overview form
     * 
     * @param processDefinitionId
     *        the process
     * @return
     *         the form mapping of the process overview form
     */
    FormMapping getProcessOverviewForm(long processDefinitionId);

    /**
     * Get the form mapping that contains the link to the task form
     * 
     * @param processDefinitionId
     *        the process
     * @param taskName
     *        the name of the task
     * @return
     *         the form mapping of the task form
     */
    FormMapping getHumanTaskForm(long processDefinitionId, String taskName);

    /**
     * Update a form mapping with the given values
     * 
     * @param formMappingId
     *        the form mapping to update
     * @param page
     *        the name of the form or the url to the form
     * @param type
     *        the form mapping type
     * @param external
     *        true if the page attribute is an url to an external page, false if it's a internal form
     */
    void updateFormMapping(long formMappingId, String page, FormMappingType type, boolean external);
}
