/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.transition;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.execution.state.AbortedFlowNodeState;
import org.bonitasoft.engine.execution.state.AbortingFlowNodeState;
import org.bonitasoft.engine.execution.state.CancelledFlowNodeState;
import org.bonitasoft.engine.execution.state.CancellingFlowNodeState;
import org.bonitasoft.engine.execution.state.CompletedActivityState;
import org.bonitasoft.engine.execution.state.ExecutingThrowEventState;
import org.springframework.stereotype.Component;

@Component
public class IntermediateThrowEventStates extends FlowNodeStatesAndTransitions {

    public SFlowNodeType getFlowNodeType() {
        return SFlowNodeType.INTERMEDIATE_THROW_EVENT;
    }

    public IntermediateThrowEventStates(
            CompletedActivityState completed,
            AbortedFlowNodeState aborted,
            CancelledFlowNodeState cancelled,
            AbortingFlowNodeState abortingFlowNode,
            CancellingFlowNodeState cancellingFlowNode,
            ExecutingThrowEventState executingThrowEvent) {

        defineNormalTransitionForFlowNode(executingThrowEvent, completed);
        defineAbortTransitionForFlowNode(abortingFlowNode, aborted);
        defineCancelTransitionForFlowNode(cancellingFlowNode, cancelled);
    }
}
