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
package org.bonitasoft.engine.execution.state;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class EndingIntermediateCatchEventExceptionStateImpl implements FlowNodeState {

    private final WaitingEventsInterrupter waitingEventsInterrupter;

    public EndingIntermediateCatchEventExceptionStateImpl(WaitingEventsInterrupter waitingEventsInterrupter) {
        super();
        this.waitingEventsInterrupter = waitingEventsInterrupter;
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) {
        return true;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance instance) throws SActivityStateExecutionException {
        final SCatchEventDefinition catchEventDef = (SCatchEventDefinition) processDefinition.getProcessContainer().getFlowNode(
                instance.getFlowNodeDefinitionId());
        try {
            final SIntermediateCatchEventInstance intermediateCatchEventInstance = (SIntermediateCatchEventInstance) instance;
            waitingEventsInterrupter.interruptWaitingEvents(processDefinition, intermediateCatchEventInstance, catchEventDef);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
        return StateCode.DONE;
    }

    @Override
    public boolean hit(final SProcessDefinition processDefinition, final SFlowNodeInstance parentInstance, final SFlowNodeInstance childInstance) {
        return true;
    }

    @Override
    public boolean isInterrupting() {
        return false;
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

}
