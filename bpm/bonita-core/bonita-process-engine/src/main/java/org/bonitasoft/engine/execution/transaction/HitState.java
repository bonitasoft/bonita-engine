/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class HitState implements TransactionContentWithResult<Boolean> {

    private final long flowNodeInstanceId;

    private final SProcessDefinition processDefinition;

    private final ActivityInstanceService activityInstanceService;

    private final FlowNodeStateManager flowNodeStateManager;

    private final SFlowNodeInstance child;

    private boolean hit;

    public HitState(final SProcessDefinition processDefinition, final ActivityInstanceService activityInstanceService,
            final FlowNodeStateManager flowNodeStateManager, final long parentInstanceId, final SFlowNodeInstance child) {
        this.processDefinition = processDefinition;
        this.activityInstanceService = activityInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
        this.flowNodeInstanceId = parentInstanceId;
        this.child = child;
    }

    @Override
    public void execute() throws SBonitaException {
        final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        final FlowNodeState state = flowNodeStateManager.getState(flowNodeInstance.getStateId());
        hit = state.hit(processDefinition, flowNodeInstance, child);
    }

    @Override
    public Boolean getResult() {
        return hit;
    }

}
