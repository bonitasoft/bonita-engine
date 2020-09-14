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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.execution.state.AbortedFlowNodeStateImpl;
import org.bonitasoft.engine.execution.state.AbortingActivityWithBoundaryStateImpl;
import org.bonitasoft.engine.execution.state.CancellingActivityWithBoundaryStateImpl;
import org.bonitasoft.engine.execution.state.ExecutingAutomaticActivityStateImpl;
import org.bonitasoft.engine.execution.transition.FlowNodeStatesAndTransitions;
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

    @Before
    public void before() {
        List<FlowNodeStatesAndTransitions> flowNodeStatesAndTransitions = new ArrayList<>();
        flowNodeStatesAndTransitions.add(new TestState());
        stateManager = new FlowNodeStateManagerImpl(bpmInstancesCreator, null, null, flowNodeStatesAndTransitions);
    }

    @Test
    public void getFirstState_for_sub_process_should_return_ExecutingAutomaticActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.AUTOMATIC_TASK);
        //then
        assertThat(firstState).isInstanceOf(ExecutingAutomaticActivityStateImpl.class);
    }

    static class TestState extends FlowNodeStatesAndTransitions {

        @Override
        public SFlowNodeType getFlowNodeType() {
            return SFlowNodeType.AUTOMATIC_TASK;
        }

        TestState() {
            defineNormalTransitionForFlowNode(new ExecutingAutomaticActivityStateImpl(null),
                    new AbortedFlowNodeStateImpl());
            defineAbortTransitionForFlowNode(new AbortingActivityWithBoundaryStateImpl(null));
            defineCancelTransitionForFlowNode(new CancellingActivityWithBoundaryStateImpl(null));
        }
    }
}
