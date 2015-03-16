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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.List;

import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstances;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class GetArchivedProcessInstanceList implements TransactionContentWithResult<List<ArchivedProcessInstance>> {

    private final ProcessInstanceService processInstanceService;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private final SearchOptionsBuilder searchOptionsBuilder;

    private List<ArchivedProcessInstance> processInstanceList;

    private final ProcessDefinitionService processDefinitionService;

    public GetArchivedProcessInstanceList(final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService,
            final SearchEntitiesDescriptor searchEntitiesDescriptor, final long processInstanceId, final int startIndex, final int maxResults) {
        this.processInstanceService = processInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
        searchOptionsBuilder = new SearchOptionsBuilder(startIndex, maxResults);
        searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId);
    }

    public GetArchivedProcessInstanceList(final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService,
            final SearchEntitiesDescriptor searchEntitiesDescriptor, final long processInstanceId, final int startIndex, final int maxResults,
            final String field, final OrderByType order) {
        this(processInstanceService, processDefinitionService, searchEntitiesDescriptor, processInstanceId, startIndex, maxResults);
        searchOptionsBuilder.sort(field, Order.valueOf(order.name()));
    }

    public GetArchivedProcessInstanceList(final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService,
            final SearchEntitiesDescriptor searchEntitiesDescriptor, final SearchOptions searchOptions) {
        this.processInstanceService = processInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
        searchOptionsBuilder = new SearchOptionsBuilder(searchOptions);
    }

    @Override
    public void execute() throws SBonitaException {
        final SearchArchivedProcessInstances searchArchivedProcessInstances = new SearchArchivedProcessInstances(processInstanceService,
                processDefinitionService, searchEntitiesDescriptor.getSearchArchivedProcessInstanceDescriptor(), searchOptionsBuilder.done());
        searchArchivedProcessInstances.execute();
        processInstanceList = searchArchivedProcessInstances.getResult().getResult();
    }

    @Override
    public List<ArchivedProcessInstance> getResult() {
        return processInstanceList;
    }

}
