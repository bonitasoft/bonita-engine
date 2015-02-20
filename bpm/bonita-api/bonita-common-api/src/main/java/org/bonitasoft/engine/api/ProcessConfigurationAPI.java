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

import java.util.List;

import org.bonitasoft.engine.form.mapping.FormMapping;
import org.bonitasoft.engine.form.mapping.FormMappingType;
import org.bonitasoft.engine.search.SearchOptions;

/**
 * Contains methods related to the configuration processes
 *
 * @author Baptiste Mesta
 * @since 7.0.0
 */
public interface ProcessConfigurationAPI {

    List<FormMapping> searchFormMappings(SearchOptions searchOptions);

    FormMapping getProcessStartForm(long processDefinitionId);

    FormMapping getProcessOverviewForm(long processDefinitionId);

    FormMapping getHumanTaskForm(long processDefinitionId, String taskName);

    void updateFormMapping(long formMappingId, String page, FormMappingType type, boolean external);
}
