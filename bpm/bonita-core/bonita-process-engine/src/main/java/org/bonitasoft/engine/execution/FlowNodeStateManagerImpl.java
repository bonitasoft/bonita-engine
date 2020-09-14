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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.transition.FlowNodeStatesAndTransitions;
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

    protected static final int FIRST_STATE_KEY = -1;

    protected final Map<Integer, FlowNodeState> allStates = new HashMap<>();
    protected final Map<SFlowNodeType, Map<Integer, FlowNodeState>> normalFlowNodeStatesAndTransitions = new HashMap<>();
    protected final Map<SFlowNodeType, Map<Integer, FlowNodeState>> abortFlowNodeStatesAndTransitions = new HashMap<>();
    protected final Map<SFlowNodeType, Map<Integer, FlowNodeState>> cancelFlowNodeStatesAndTransitions = new HashMap<>();

    protected StateBehaviors stateBehaviors;
    private final List<FlowNodeStatesAndTransitions> flowNodeStatesAndTransitions;

    protected final Set<Integer> unstableStates = new HashSet<>();
    protected final Set<Integer> stableStates = new HashSet<>();

    public FlowNodeStateManagerImpl(
            BPMInstancesCreator bpmInstancesCreator,
            StateBehaviors stateBehaviors,
            List<FlowNodeStatesAndTransitions> flowNodeStatesAndTransitions,
            List<FlowNodeState> allStates) {
        this.stateBehaviors = stateBehaviors;
        this.flowNodeStatesAndTransitions = flowNodeStatesAndTransitions;
        bpmInstancesCreator.setStateManager(this);
        storeAllStates(allStates);
        defineTransitionsForAllNodesType();
    }

    private void storeAllStates(List<FlowNodeState> states) {
        for (FlowNodeState state : states) {
            final int stateId = state.getId();
            if (state.isStable()) {
                stableStates.add(stateId);
            } else {
                unstableStates.add(stateId);
            }
            allStates.put(stateId, state);
        }
    }

    @Override
    public void setProcessExecutor(final ProcessExecutor processExecutor) {
        stateBehaviors.setProcessExecutor(processExecutor);
    }

    @Override
    public StateBehaviors getStateBehaviors() {
        return stateBehaviors;
    }

    private void defineTransitionsForAllNodesType() {
        for (FlowNodeStatesAndTransitions flowNodeStatesAndTransition : flowNodeStatesAndTransitions) {
            defineTransitionsForFlowNode(flowNodeStatesAndTransition.getFlowNodeType(),
                    normalFlowNodeStatesAndTransitions,
                    flowNodeStatesAndTransition.getNormalTransition());
            defineTransitionsForFlowNode(flowNodeStatesAndTransition.getFlowNodeType(),
                    cancelFlowNodeStatesAndTransitions,
                    flowNodeStatesAndTransition.getCancelTransitionForFlowNode());
            defineTransitionsForFlowNode(flowNodeStatesAndTransition.getFlowNodeType(),
                    abortFlowNodeStatesAndTransitions,
                    flowNodeStatesAndTransition.getAbortTransitionForFlowNode());
        }
    }

    private void defineTransitionsForFlowNode(final SFlowNodeType flowNodeType,
            final Map<SFlowNodeType, Map<Integer, FlowNodeState>> transitions,
            final FlowNodeState... states) {
        final Map<Integer, FlowNodeState> currentStateIdToNextState = new HashMap<>();
        int stateIndex = 0;
        currentStateIdToNextState.put(FIRST_STATE_KEY, states[0]);
        while (stateIndex < states.length - 1) {
            // key = current state id , value = nextState (Full Object). Eg. for a human task:
            // [
            //   0 (Initializing id) -> ReadyActivityStateImpl
            //   4 (Ready id) -> ExecutingFlowNodeStateImpl
            //   1 (Executing id) -> CompletedActivityStateImpl (terminal state)
            // ]
            currentStateIdToNextState.put(states[stateIndex].getId(), states[stateIndex + 1]);
            stateIndex++;
        }
        transitions.put(flowNodeType, currentStateIdToNextState);
    }

    @Override
    public FlowNodeState getNextState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance, final int currentStateId)
            throws SActivityExecutionException {
        FlowNodeState currentState = getCurrentNonInterruptingState(flowNodeInstance, currentStateId);
        do {
            currentState = getNextStateToHandle(flowNodeInstance, currentState);
        } while (!currentState.shouldExecuteState(processDefinition, flowNodeInstance));
        return currentState;
    }

    private FlowNodeState getCurrentNonInterruptingState(final SFlowNodeInstance flowNodeInstance,
            final int currentStateId) {
        final FlowNodeState currentState = getState(currentStateId);
        if (currentState.isInterrupting()) {
            final int previousStateId = flowNodeInstance.getPreviousStateId();
            return allStates.get(previousStateId);
        }
        return currentState;
    }

    private FlowNodeState getNextStateToHandle(final SFlowNodeInstance flowNodeInstance,
            final FlowNodeState flowNodeStateToExecute) throws SActivityExecutionException {
        FlowNodeState nextStateToHandle;
        switch (flowNodeInstance.getStateCategory()) {
            case ABORTING:
                final ExceptionalStateTransitionsManager abortStateTransitionsManager = new ExceptionalStateTransitionsManager(
                        abortFlowNodeStatesAndTransitions.get(flowNodeInstance.getType()), flowNodeInstance);
                nextStateToHandle = abortStateTransitionsManager.getNextState(flowNodeStateToExecute);
                break;

            case CANCELLING:
                final ExceptionalStateTransitionsManager cancelStateTransitionsManager = new ExceptionalStateTransitionsManager(
                        cancelFlowNodeStatesAndTransitions.get(flowNodeInstance.getType()), flowNodeInstance);
                nextStateToHandle = cancelStateTransitionsManager.getNextState(flowNodeStateToExecute);
                break;

            default:
                final NormalStateTransitionsManager normalStateTransitionsManager = new NormalStateTransitionsManager(
                        normalFlowNodeStatesAndTransitions.get(flowNodeInstance
                                .getType()),
                        flowNodeInstance);
                nextStateToHandle = normalStateTransitionsManager.getNextState(flowNodeStateToExecute);
                break;
        }
        if (nextStateToHandle == null) {
            throw new SActivityExecutionException(
                    "no state found after " + allStates.get(flowNodeStateToExecute.getId()).getClass() + " for "
                            + flowNodeInstance.getClass() + " in state category " + flowNodeInstance.getStateCategory()
                            + " activity id=" + flowNodeInstance.getId());
        }
        return nextStateToHandle;
    }

    @Override
    public FlowNodeState getState(final int stateId) {
        return allStates.get(stateId);
    }

    @Override
    public Set<String> getSupportedState(final FlowNodeType nodeType) {
        final SFlowNodeType type = SFlowNodeType.valueOf(nodeType.toString());
        final Map<Integer, FlowNodeState> states = normalFlowNodeStatesAndTransitions.get(type);
        final Set<String> stateNames = new HashSet<>();
        for (final FlowNodeState state : states.values()) {
            stateNames.add(state.getName());
        }
        return stateNames;
    }

    public FlowNodeState getFirstState(SFlowNodeType nodeType) {
        return normalFlowNodeStatesAndTransitions.get(nodeType).get(FIRST_STATE_KEY);
    }

}
