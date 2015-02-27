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
package org.bonitasoft.engine.execution;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ExceptionalStateTransitionsManagerTest {

    private static final long FLOW_NODE_INSTANCE_ID = 100;

    private static final int NORMAL_NON_TERMINAL_STATE_ID = 50;

    private static final int NORMAL_TERMINAL_STATE_ID = 51;

    private static final int ABORTING_NON_TERMINAL_STATE_ID = 52;

    private static final int ABORTING_TERMINAL_STATE_ID = 53;

    @Mock
    private FlowNodeState normalNonTerminalState;

    @Mock
    private FlowNodeState normalTerminalState;

    @Mock
    private FlowNodeState abortingNonTerminalState;

    @Mock
    private FlowNodeState abortingTerminalState;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    private Map<Integer, FlowNodeState> stateTransitions;

    @Before
    public void setUp() {
        doReturn(NORMAL_NON_TERMINAL_STATE_ID).when(normalNonTerminalState).getId();
        doReturn(false).when(normalNonTerminalState).isTerminal();
        doReturn(SStateCategory.NORMAL).when(normalNonTerminalState).getStateCategory();

        doReturn(NORMAL_TERMINAL_STATE_ID).when(normalTerminalState).getId();
        doReturn(true).when(normalTerminalState).isTerminal();
        doReturn(SStateCategory.NORMAL).when(normalTerminalState).getStateCategory();

        doReturn(ABORTING_NON_TERMINAL_STATE_ID).when(abortingNonTerminalState).getId();
        doReturn(false).when(abortingNonTerminalState).isTerminal();
        doReturn(SStateCategory.ABORTING).when(abortingNonTerminalState).getStateCategory();

        doReturn(ABORTING_TERMINAL_STATE_ID).when(abortingTerminalState).getId();
        doReturn(true).when(abortingTerminalState).isTerminal();
        doReturn(SStateCategory.ABORTING).when(abortingTerminalState).getStateCategory();

        doReturn(FLOW_NODE_INSTANCE_ID).when(flowNodeInstance).getId();
        doReturn(SStateCategory.NORMAL).when(flowNodeInstance).getStateCategory();

        stateTransitions = new HashMap<Integer, FlowNodeState>(2);
        stateTransitions.put(-1, abortingNonTerminalState);
        stateTransitions.put(ABORTING_NON_TERMINAL_STATE_ID, abortingTerminalState);
    }

    @Test
    public void getNextState_returns_next_state_from_map_using_current_state_id_key_if_current_state_is_not_in_normal_category() throws SIllegalStateTransition {
        final ExceptionalStateTransitionsManager statesManager = new ExceptionalStateTransitionsManager(stateTransitions, flowNodeInstance);
        final FlowNodeState nextState = statesManager.getNextState(abortingNonTerminalState);
        assertEquals(abortingTerminalState, nextState);
    }

    @Test
    public void getNextState_returns_next_state_from_map_using_minus_one_if_current_state_is_in_normal_category_and_is_not_terminal()
            throws SIllegalStateTransition {
        final ExceptionalStateTransitionsManager statesManager = new ExceptionalStateTransitionsManager(stateTransitions, flowNodeInstance);
        final FlowNodeState nextState = statesManager.getNextState(normalNonTerminalState);
        assertEquals(abortingNonTerminalState, nextState);
    }

    @Test(expected = SIllegalStateTransition.class)
    public void getNextState_throws_SIllegalStateTransition_if_current_is_in_normal_category_and_is_terminal() throws SIllegalStateTransition {
        final ExceptionalStateTransitionsManager statesManager = new ExceptionalStateTransitionsManager(stateTransitions, flowNodeInstance);
        statesManager.getNextState(normalTerminalState);
    }

}
