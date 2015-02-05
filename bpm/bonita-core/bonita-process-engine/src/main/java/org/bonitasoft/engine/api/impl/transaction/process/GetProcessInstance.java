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

import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor;
import org.bonitasoft.engine.search.process.SearchProcessInstances;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @deprecated since 6.3.0
 */
@Deprecated
public class GetProcessInstance implements TransactionContentWithResult<ProcessInstance> {

    private ProcessInstance processInstance;

    private final ProcessInstanceService processInstanceService;

    private final ProcessDefinitionService processDefinitionService;

    private final long processInstanceId;

    private final SearchProcessInstanceDescriptor searchProcessInstanceDescriptor;

    public GetProcessInstance(final ProcessInstanceService processInstanceService, final ProcessDefinitionService processDefinitionService,
            final SearchProcessInstanceDescriptor searchProcessInstanceDescriptor, final long processInstanceId) {
        this.processInstanceService = processInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceId = processInstanceId;
        this.searchProcessInstanceDescriptor = searchProcessInstanceDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 2);
        searchOptionsBuilder.filter(ProcessInstanceSearchDescriptor.ID, processInstanceId);

        final SearchProcessInstances searchProcessInstances = new SearchProcessInstances(processInstanceService,
                searchProcessInstanceDescriptor, searchOptionsBuilder.done(), processDefinitionService);
        searchProcessInstances.execute();
        try {
            processInstance = searchProcessInstances.getResult().getResult().get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new SProcessInstanceNotFoundException(processInstanceId);
        }
    }

    @Override
    public ProcessInstance getResult() {
        return processInstance;
    }

}
