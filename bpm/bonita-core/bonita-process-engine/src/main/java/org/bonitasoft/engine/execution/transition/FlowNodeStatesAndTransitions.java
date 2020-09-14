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

import java.util.LinkedHashSet;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.springframework.stereotype.Component;

@Component
public abstract class FlowNodeStatesAndTransitions {

    protected final LinkedHashSet<FlowNodeState> normalTransitionForFlowNode = new LinkedHashSet<>();
    protected final LinkedHashSet<FlowNodeState> abortTransitionForFlowNode = new LinkedHashSet<>();
    protected final LinkedHashSet<FlowNodeState> cancelTransitionForFlowNode = new LinkedHashSet<>();

    protected void defineNormalTransitionForFlowNode(FlowNodeState... flowNodeStates) {
        addToTransitionList(normalTransitionForFlowNode, flowNodeStates);
    }

    protected void defineCancelTransitionForFlowNode(FlowNodeState... flowNodeStates) {
        addToTransitionList(cancelTransitionForFlowNode, flowNodeStates);
    }

    protected void defineAbortTransitionForFlowNode(FlowNodeState... flowNodeStates) {
        addToTransitionList(abortTransitionForFlowNode, flowNodeStates);
    }

    private void addToTransitionList(LinkedHashSet transitionList, FlowNodeState... flowNodeStates) {
        for (int i = 0; i < flowNodeStates.length; i++) {
            transitionList.add(flowNodeStates[i]);
        }
    }

    public FlowNodeState[] getNormalTransition() {
        return this.normalTransitionForFlowNode.toArray(new FlowNodeState[normalTransitionForFlowNode.size()]);
    }

    public FlowNodeState[] getCancelTransitionForFlowNode() {
        return this.cancelTransitionForFlowNode.toArray(new FlowNodeState[cancelTransitionForFlowNode.size()]);
    }

    public FlowNodeState[] getAbortTransitionForFlowNode() {
        return this.abortTransitionForFlowNode.toArray(new FlowNodeState[abortTransitionForFlowNode.size()]);
    }

    public abstract SFlowNodeType getFlowNodeType();

}
