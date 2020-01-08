/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
 * @author Yanyan Liu
 */
public class SearchOpenProcessInstancesInvolvingUser extends AbstractProcessInstanceSearchEntity {

    private final ProcessInstanceService processInstanceService;

    private final long userId;

    public SearchOpenProcessInstancesInvolvingUser(final ProcessInstanceService processInstanceService,
            final SearchProcessInstanceDescriptor searchEntitiesDescriptor, final long userId,
            final SearchOptions options,
            final ProcessDefinitionService processDefinitionService) {
        super(searchEntitiesDescriptor, options, processDefinitionService);
        this.processInstanceService = processInstanceService;
        this.userId = userId;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return processInstanceService.getNumberOfOpenProcessInstancesInvolvingUser(userId, searchOptions);
    }

    @Override
    public List<SProcessInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return processInstanceService.searchOpenProcessInstancesInvolvingUser(userId, searchOptions);
    }
}
