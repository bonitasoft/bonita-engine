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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.springframework.stereotype.Component;

@Component
public class CancellingReceiveTaskState extends CancellingActivityWithBoundaryState {

    private final WaitingEventsInterrupter waitingEventsInterrupter;

    public CancellingReceiveTaskState(WaitingEventsInterrupter waitingEventsInterrupter) {
        this.waitingEventsInterrupter = waitingEventsInterrupter;
    }

    @Override
    public int getId() {
        return 39;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance instance)
            throws SActivityStateExecutionException {
        try {
            final SReceiveTaskInstance receiveTaskInstance = (SReceiveTaskInstance) instance;
            waitingEventsInterrupter.interruptWaitingEvents(receiveTaskInstance);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
        return super.execute(processDefinition, instance);
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) {
        return true;
    }

}
