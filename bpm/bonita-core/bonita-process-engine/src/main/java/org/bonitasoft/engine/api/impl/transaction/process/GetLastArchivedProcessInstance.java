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
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstances;

/**
 * Returns the most recent archived process instance from the archives.
 * 
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class GetLastArchivedProcessInstance implements TransactionContentWithResult<ArchivedProcessInstance> {

    private ArchivedProcessInstance processInstance;

    private final ProcessInstanceService processInstanceService;

    private final long processInstanceId;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private final ProcessDefinitionService processDefinitionService;

    public GetLastArchivedProcessInstance(final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService,
            final long processInstanceId, final SearchEntitiesDescriptor searchEntitiesDescriptor) {
        this.processInstanceService = processInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceId = processInstanceId;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 2);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, Order.DESC);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.END_DATE, Order.DESC);
        searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId);

        final SearchArchivedProcessInstances searchArchivedProcessInstances = new SearchArchivedProcessInstances(processInstanceService,
                processDefinitionService, searchEntitiesDescriptor.getSearchArchivedProcessInstanceDescriptor(), searchOptionsBuilder.done());
        searchArchivedProcessInstances.execute();
        final List<ArchivedProcessInstance> processInstances = searchArchivedProcessInstances.getResult().getResult();
        if (processInstances.isEmpty()) {
            throw new SProcessInstanceNotFoundException(processInstanceId);
        }
        processInstance = processInstances.get(0);
    }

    @Override
    public ArchivedProcessInstance getResult() {
        return processInstance;
    }

}
