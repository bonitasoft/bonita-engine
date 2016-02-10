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
package org.bonitasoft.engine.search.process;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractProcessInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor;

/**
 * @author Celine Souchet
 */
public class SearchOpenProcessInstancesInvolvingUsersManagedBy extends AbstractProcessInstanceSearchEntity {

    private final ProcessInstanceService processInstanceService;

    private final long managerUserId;

    public SearchOpenProcessInstancesInvolvingUsersManagedBy(final ProcessInstanceService processInstanceService,
            final SearchProcessInstanceDescriptor searchEntitiesDescriptor, final long managerUserId, final SearchOptions options,
            final ProcessDefinitionService processDefinitionService) {
        super(searchEntitiesDescriptor, options, processDefinitionService);
        this.processInstanceService = processInstanceService;
        this.managerUserId = managerUserId;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return processInstanceService.getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(managerUserId, searchOptions);
    }

    @Override
    public List<SProcessInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return processInstanceService.searchOpenProcessInstancesInvolvingUsersManagedBy(managerUserId, searchOptions);
    }
}
