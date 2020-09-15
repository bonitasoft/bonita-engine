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
package org.bonitasoft.engine.execution;

import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.NORMAL;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.transition.FlowNodeStateSequences;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the activity state manager.
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Zhang Bole
 * @author Celine Souchet
 */
@Component("flowNodeStateManager")
public class FlowNodeStateManagerImpl implements FlowNodeStateManager {

    protected final Map<Integer, FlowNodeState> allStates;
    protected StateBehaviors stateBehaviors;
    private final Map<SFlowNodeType, FlowNodeStateSequences> flowNodeStateSequences;

    public FlowNodeStateManagerImpl(
            BPMInstancesCreator bpmInstancesCreator,
            StateBehaviors stateBehaviors,
            List<FlowNodeStateSequences> flowNodeStateSequences,
            List<FlowNodeState> allStates) {
        this.stateBehaviors = stateBehaviors;
        this.flowNodeStateSequences = flowNodeStateSequences.stream()
                .collect(Collectors.toMap(FlowNodeStateSequences::getFlowNodeType, e -> e));
        this.allStates = allStates.stream().collect(Collectors.toMap(FlowNodeState::getId, s -> s));
        bpmInstancesCreator.setStateManager(this);
    }

    @Override
    public void setProcessExecutor(final ProcessExecutor processExecutor) {
        stateBehaviors.setProcessExecutor(processExecutor);
    }

    @Override
    public StateBehaviors getStateBehaviors() {
        return stateBehaviors;
    }

    @Override
    public FlowNodeState getNextState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance, final int currentStateId)
            throws SActivityExecutionException {
        FlowNodeState currentState = getState(currentStateId);
        do {
            currentState = getNextStateToHandle(flowNodeInstance, currentState);
        } while (!currentState.shouldExecuteState(processDefinition, flowNodeInstance));
        return currentState;
    }

    private FlowNodeState getNextStateToHandle(final SFlowNodeInstance flowNodeInstance,
            final FlowNodeState currentState) throws SActivityExecutionException {
        FlowNodeStateSequences stateSequence = this.flowNodeStateSequences.get(flowNodeInstance.getType());
        SStateCategory stateCategory = flowNodeInstance.getStateCategory();
        if (currentState.getStateCategory() != stateCategory) {
            // the state category changed (flow node was aborted or cancelled), get the first state of the corresponding state category
            return stateSequence.getFirstState(stateCategory);
        } else {
            FlowNodeState nextState = stateSequence.getStateAfter(stateCategory, currentState.getId());
            if (nextState == null) {
                throw new SActivityExecutionException(
                        "no state found after " + allStates.get(currentState.getId()).getClass() + " for "
                                + flowNodeInstance.getClass() + " in state category "
                                + flowNodeInstance.getStateCategory()
                                + " activity id=" + flowNodeInstance.getId());
            }
            return nextState;
        }
    }

    @Override
    public FlowNodeState getState(final int stateId) {
        return allStates.get(stateId);
    }

    @Override
    public Set<String> getSupportedState(final SFlowNodeType nodeType) {
        return flowNodeStateSequences.get(nodeType).getSupportedStates();
    }

    public FlowNodeState getFirstState(SFlowNodeType nodeType) {
        return flowNodeStateSequences.get(nodeType).getFirstState(NORMAL);
    }

}
