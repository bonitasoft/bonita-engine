package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

@RunWith(MockitoJUnitRunner.class)
public class NormalStateTransitionsManagerTest {

    private static final int NORMAL_NON_TERMINAL_STATE_ID = 50;

    private static final int NORMAL_TERMINAL_STATE_ID = 51;

    private static final long FLOW_NODE_INSTANCE_ID = 100;

    @Mock
    private FlowNodeState normalNonTerminalState;

    @Mock
    private FlowNodeState normalTerminalState;

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

        doReturn(FLOW_NODE_INSTANCE_ID).when(flowNodeInstance).getId();
        doReturn(SStateCategory.NORMAL).when(flowNodeInstance).getStateCategory();

        stateTransitions = new HashMap<Integer, FlowNodeState>(2);
        stateTransitions.put(NORMAL_NON_TERMINAL_STATE_ID, normalTerminalState);
    }

    @Test
    public void getNextState_should_return_the_map_entry_value() throws Exception {
        // given
        NormalStateTransitionsManager transitionsManager = new NormalStateTransitionsManager(stateTransitions, flowNodeInstance);

        // when
        FlowNodeState nextState = transitionsManager.getNextState(normalNonTerminalState);

        // then
        assertThat(nextState).isEqualTo(normalTerminalState);
    }

    @Test
    public void getNextState_should_throw_terminal_SIllegalStateTransition_exception_if_ther_is_no_entry_in_the_map_and_state_is_terminal() {
        // given
        NormalStateTransitionsManager transitionsManager = new NormalStateTransitionsManager(stateTransitions, flowNodeInstance);

        try {
            // when
            transitionsManager.getNextState(normalTerminalState);
            fail("SIllegalStateTransition must be thrown");
        } catch (SIllegalStateTransition e) {
            // then
            String message = e.getMessage();
            assertThat(message).startsWith("no state found after");
            assertThat(message).contains("for flow node of type");
            assertThat(message).endsWith("in state category " + SStateCategory.NORMAL + ". Flow node instance id = " + FLOW_NODE_INSTANCE_ID);
            assertThat(e.isTransitionFromTerminalState()).isTrue();
        }

    }

    @Test
    public void getNextState_should_throw_non_terminal_SIllegalStateTransition_exception_if_ther_is_no_entry_in_the_map_and_state_is_non_terminal() {
        // given
        FlowNodeState terminalUnMappedState = mock(FlowNodeState.class);
        when(terminalUnMappedState.getStateCategory()).thenReturn(SStateCategory.NORMAL);
        when(terminalUnMappedState.getId()).thenReturn(1000);
        NormalStateTransitionsManager transitionsManager = new NormalStateTransitionsManager(stateTransitions, flowNodeInstance);

        try {
            // when
            transitionsManager.getNextState(terminalUnMappedState);
            fail("SIllegalStateTransition must be thrown");
        } catch (SIllegalStateTransition e) {
            // then
            assertThat(e.isTransitionFromTerminalState()).isFalse();
        }

    }

}
