/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.search.process;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.search.AbstractProcessDeploymentInfoSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchProcessDefinitionsDescriptor;

/**
 * @author Celine Souchet
 * 
 */
public class SearchProcessDeploymentInfosCanBeStartedByUsersManagedBy extends AbstractProcessDeploymentInfoSearchEntity {

    private final ProcessDefinitionService processDefinitionService;

    private final long managerUserId;

    public SearchProcessDeploymentInfosCanBeStartedByUsersManagedBy(final ProcessDefinitionService processDefinitionService,
            final SearchProcessDefinitionsDescriptor searchEntitiesDescriptor, final SearchOptions options, final long managerUserId) {
        super(searchEntitiesDescriptor, options);
        this.processDefinitionService = processDefinitionService;
        this.managerUserId = managerUserId;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaSearchException {
        return processDefinitionService.getNumberOfProcessDeploymentInfosCanBeStartedByUsersManagedBy(managerUserId, searchOptions);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> executeSearch(final QueryOptions searchOptions) throws SBonitaSearchException {
        return processDefinitionService.searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(managerUserId, searchOptions);
    }

}
