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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.springframework.stereotype.Component;

/**
 * For a given flow node type, this class maintains the supported flow node states and the sequence
 * and transitions from one state to another.
 * Each <code>FlowNodeStatesAndTransitions</code> class defines 3 state sequences:
 * <ul>
 * <li>one for the normal flow</li>
 * <li>one for the aborting flow</li>
 * <li>one for the cancelling flow</li>
 * </ul>
 */
@Component
public abstract class FlowNodeStateSequences {

    /*
     * store all states of a given state category, and their order.
     */
    private static class StateSequence {

        Map<Integer, FlowNodeState> mapOfCurrentStateIdToNextState = new HashMap<>();
        FlowNodeState firstState;

        public StateSequence(FlowNodeState... flowNodeStates) {
            firstState = flowNodeStates[0];
            for (int i = 0; i < flowNodeStates.length - 1; i++) {
                // key = id of the current state
                // value = next state
                mapOfCurrentStateIdToNextState.put(flowNodeStates[i].getId(), flowNodeStates[i + 1]);
            }
        }

        public FlowNodeState getFirstState() {
            return firstState;
        }

        public FlowNodeState getStateAfter(int previousStateId) {
            return mapOfCurrentStateIdToNextState.get(previousStateId);
        }
    }

    private StateSequence normalSequence;
    private StateSequence cancelSequence;
    private StateSequence abortSequence;

    protected void defineNormalSequence(FlowNodeState... flowNodeStates) {
        normalSequence = new StateSequence(flowNodeStates);
    }

    protected void defineCancelSequence(FlowNodeState... flowNodeStates) {
        cancelSequence = new StateSequence(flowNodeStates);
    }

    protected void defineAbortSequence(FlowNodeState... flowNodeStates) {
        abortSequence = new StateSequence(flowNodeStates);
    }

    public abstract SFlowNodeType getFlowNodeType();

    public FlowNodeState getFirstState(SStateCategory category) {
        return getSequence(category).getFirstState();
    }

    public FlowNodeState getStateAfter(SStateCategory category, int currentStateId) {
        return getSequence(category).getStateAfter(currentStateId);
    }

    private StateSequence getSequence(SStateCategory category) {
        switch (category) {
            case NORMAL:
                return normalSequence;
            case ABORTING:
                return abortSequence;
            case CANCELLING:
                return cancelSequence;
            default:
                throw new IllegalStateException("Unexpected value: " + category);
        }
    }

    public Set<String> getSupportedStates() {
        Set<String> names = new HashSet<>();
        names.add(normalSequence.firstState.getName());
        normalSequence.mapOfCurrentStateIdToNextState.values().forEach(s -> names.add(s.getName()));
        return names;
    }
}
