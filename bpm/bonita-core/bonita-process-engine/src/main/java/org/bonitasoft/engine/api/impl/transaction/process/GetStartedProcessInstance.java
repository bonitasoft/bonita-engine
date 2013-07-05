/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.process.SearchProcessInstances;

/**
 * Returns the process instance, if is started.
 * 
 * @author Celine Souchet
 */
public class GetStartedProcessInstance implements TransactionContentWithResult<ProcessInstance> {

    private final ProcessInstanceService processInstanceService;

    private final ProcessDefinitionService processDefinitionService;

    private final SearchEntitiesDescriptor searchEntitiesDescriptor;

    private final long processInstanceId;

    private ProcessInstance processInstance;

    public GetStartedProcessInstance(final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService,
            final SearchEntitiesDescriptor searchEntitiesDescriptor, final long processInstanceId) {
        this.processInstanceService = processInstanceService;
        this.processInstanceId = processInstanceId;
        this.processDefinitionService = processDefinitionService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 2);
        searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.ID, processInstanceId);
        searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.STATE_ID, ProcessInstanceState.STARTED.getId());

        final SearchProcessInstances searchProcessInstances = new SearchProcessInstances(processInstanceService,
                searchEntitiesDescriptor.getProcessInstanceDescriptor(), searchOptionsBuilder.done(), processDefinitionService);
        searchProcessInstances.execute();

        try {
            processInstance = searchProcessInstances.getResult().getResult().get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new SProcessInstanceNotFoundException(processInstanceId, ProcessInstanceState.STARTED.name());
        }
    }

    @Override
    public ProcessInstance getResult() {
        return processInstance;
    }

}
