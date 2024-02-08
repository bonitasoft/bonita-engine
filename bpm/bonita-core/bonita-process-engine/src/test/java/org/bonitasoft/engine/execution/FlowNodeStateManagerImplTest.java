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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.*;
import static org.bonitasoft.engine.execution.TestFlowNodeState.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.execution.transition.FlowNodeStateSequences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FlowNodeStateManagerImplTest {

    private FlowNodeStateManagerImpl stateManager;

    @Mock
    BPMInstancesCreator bpmInstancesCreator;
    List<FlowNodeState> states = new ArrayList<>();

    @Before
    public void before() {
        stateManager = new FlowNodeStateManagerImpl(bpmInstancesCreator, null,
                asList(
                        new TestFlowNodeStateSequences(SFlowNodeType.AUTOMATIC_TASK,
                                asList(state(normalState(10, NORMAL)), state(normalState(11, NORMAL)),
                                        state(terminalState(12, NORMAL))),
                                asList(state(normalState(120, ABORTING)), state(terminalState(121, ABORTING))),
                                asList(state(normalState(122, CANCELLING)), state(terminalState(123, CANCELLING)))),
                        new TestFlowNodeStateSequences(SFlowNodeType.USER_TASK,
                                asList(state(normalState(20, NORMAL)), state(normalState(21, NORMAL)),
                                        state(skippedState(22, NORMAL)), state(terminalState(23, NORMAL))),
                                asList(state(normalState(130, ABORTING)), state(terminalState(131, ABORTING))),
                                asList(state(normalState(132, CANCELLING)), state(terminalState(133, CANCELLING))))),
                states);
    }

    private FlowNodeState state(FlowNodeState state) {
        states.add(state);
        return state;
    }

    @Test
    public void should_return_first_normal_state_of_AUTOMATIC_TASK() {
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.AUTOMATIC_TASK);

        assertThat(firstState.getId()).isEqualTo(10);
    }

    @Test
    public void should_return_first_normal_state_of_USER_TASK() {
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.USER_TASK);

        assertThat(firstState.getId()).isEqualTo(20);
    }

    @Test
    public void should_return_next_state_of_a_normal_state() throws Exception {
        SAutomaticTaskInstance automaticTaskInstance = new SAutomaticTaskInstance("toto", 1, 2, 3, 4, 5);

        FlowNodeState nextState = stateManager.getNextState(null, automaticTaskInstance, 11);

        assertThat(nextState.getId()).isEqualTo(12);
    }

    @Test
    public void should_skip_state_that_should_not_be_executed() throws Exception {
        SUserTaskInstance userTaskInstance = new SUserTaskInstance("toto", 1, 2, 3, 4, STaskPriority.ABOVE_NORMAL, 5,
                6);

        FlowNodeState nextState = stateManager.getNextState(null, userTaskInstance, 21);

        // then: the state "21" is skipped because "shouldExecuteState" returned false
        assertThat(nextState.getId()).isEqualTo(23);
    }

    @Test
    public void should_return_first_state_of_aborting_branch_when_flownode_was_just_aborted() throws Exception {
        SAutomaticTaskInstance automaticTaskInstance = new SAutomaticTaskInstance("toto", 1, 2, 3, 4, 5);
        automaticTaskInstance.setStateCategory(ABORTING);

        FlowNodeState nextState = stateManager.getNextState(null, automaticTaskInstance, 11);

        //then: task should be in the first "ABORTING" state
        assertThat(nextState.getId()).isEqualTo(120);
    }

    @Test
    public void should_return_next_state_of_aborting_branch() throws Exception {
        SAutomaticTaskInstance automaticTaskInstance = new SAutomaticTaskInstance("toto", 1, 2, 3, 4, 5);
        automaticTaskInstance.setStateCategory(ABORTING);

        FlowNodeState nextState = stateManager.getNextState(null, automaticTaskInstance, 120);

        assertThat(nextState.getId()).isEqualTo(121);
    }

    @Test(expected = SActivityExecutionException.class)
    public void should_throw_exception_when_there_is_no_state_after_current() throws Exception {
        SAutomaticTaskInstance automaticTaskInstance = new SAutomaticTaskInstance("toto", 1, 2, 3, 4, 5);

        stateManager.getNextState(null, automaticTaskInstance, 12);
    }

    @Test
    public void should_return_supported_states() {
        Set<String> supportedState = stateManager.getSupportedState(SFlowNodeType.USER_TASK);

        assertThat(supportedState).containsExactlyInAnyOrder("test-state-20", "test-state-21", "test-state-22",
                "test-state-23");
    }

    static class TestFlowNodeStateSequences extends FlowNodeStateSequences {

        private final SFlowNodeType type;

        @Override
        public SFlowNodeType getFlowNodeType() {
            return type;
        }

        TestFlowNodeStateSequences(SFlowNodeType type,
                List<FlowNodeState> normalTransition,
                List<FlowNodeState> abortTransition,
                List<FlowNodeState> cancelTransition) {
            this.type = type;

            defineNormalSequence(toVarArgs(normalTransition));
            defineAbortSequence(toVarArgs(abortTransition));
            defineCancelSequence(toVarArgs(cancelTransition));
        }

        private FlowNodeState[] toVarArgs(List<FlowNodeState> states) {
            return states.toArray(new FlowNodeState[0]);
        }
    }
}
