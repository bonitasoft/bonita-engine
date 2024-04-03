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
package org.bonitasoft.engine.execution.state;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.archive.BPMArchiverService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class EndingCallActivityExceptionState implements FlowNodeState {

    private final ProcessInstanceService processInstanceService;
    private final ProcessInstanceInterruptor processInstanceInterruptor;
    private final BPMArchiverService bpmArchiverService;

    public EndingCallActivityExceptionState(ProcessInstanceService processInstanceService,
            ProcessInstanceInterruptor processInstanceInterruptor,
            BPMArchiverService bpmArchiverService) {
        this.processInstanceService = processInstanceService;
        this.processInstanceInterruptor = processInstanceInterruptor;
        this.bpmArchiverService = bpmArchiverService;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        try {
            final SProcessInstance targetProcessInstance = processInstanceService
                    .getChildOfActivity(flowNodeInstance.getId());

            final boolean hasActiveChild = processInstanceInterruptor
                    .interruptProcessInstance(targetProcessInstance.getId(), getStateCategory());
            log.debug("{} activity id {}, name {}   with active process : {} ", getStateCategory(),
                    flowNodeInstance.getId(),
                    flowNodeInstance.getName(),
                    hasActiveChild);
            if (!hasActiveChild) {
                archiveChildProcessInstance(flowNodeInstance);
            }
            return hasActiveChild;
        } catch (SProcessInstanceNotFoundException e) {
            return false;
        } catch (final SBonitaException e) {
            throw new SActivityExecutionException(e);
        }
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance instance)
            throws SActivityStateExecutionException {
        // archive process target process instance
        try {
            archiveChildProcessInstance(instance);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(
                    "Unable to found the process instance called by call activity with id " + instance.getId(), e);
        }
        return StateCode.DONE;
    }

    private void archiveChildProcessInstance(final SFlowNodeInstance instance)
            throws SArchivingException, SBonitaReadException {
        try {
            final SProcessInstance childProcInst = processInstanceService.getChildOfActivity(instance.getId());
            bpmArchiverService.archiveAndDeleteProcessInstance(childProcInst);
        } catch (SProcessInstanceNotFoundException ignored) {
            log.warn("No target process instance found when archiving the call activity {}, in state {}",
                    instance.getId(), getName());
        }
    }

    @Override
    public boolean notifyChildFlowNodeHasFinished(final SProcessDefinition processDefinition,
            final SFlowNodeInstance parentInstance,
            final SFlowNodeInstance childInstance) {
        return true;
    }

    @Override
    public boolean isStable() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

}
