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
package org.bonitasoft.engine.execution.work;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FailedStateSetterTest {

    @Mock
    private WaitingEventsInterrupter waitingEventsInterrupter;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private FlowNodeStateManager flowNodeStateManager;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Mock
    private FlowNodeState failedState;

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private FailedStateSetter failedStateSetter;

    public static final long FLOW_NODE_INSTANCE_ID = 15;

    public static final long PROCESS_DEFINITION_ID = 25;

    public static final int STATE_ID = 10;

    public static final int FAILED_STATE_ID = 100;

    @Before
    public void setUp() throws Exception {
        given(flowNodeStateManager.getFailedState()).willReturn(failedState);
        given(failedState.getId()).willReturn(FAILED_STATE_ID);
        given(flowNodeInstance.getProcessDefinitionId()).willReturn(PROCESS_DEFINITION_ID);
        given(flowNodeInstance.getStateId()).willReturn(STATE_ID);
        given(loggerService.isLoggable(Matchers.<Class<?>> any(), any(TechnicalLogSeverity.class))).willReturn(true);
    }

    @Test
    public void setAsFailed_should_setState_to_failed_and_interrupt_waitingEvents() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willReturn(flowNodeInstance);

        //when
        failedStateSetter.setAsFailed(FLOW_NODE_INSTANCE_ID);

        //then
        verify(waitingEventsInterrupter).interruptWaitingEvents(flowNodeInstance);
        verify(activityInstanceService).setState(flowNodeInstance, failedState);
    }

    @Test
    public void setAsFailed_should_log_message_when_flowNodeInstance_is_not_found() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willThrow(new SFlowNodeNotFoundException(FLOW_NODE_INSTANCE_ID));

        //when
        failedStateSetter.setAsFailed(FLOW_NODE_INSTANCE_ID);

        //then
        verify(loggerService).log(Matchers.<Class<?>>any(), eq(TechnicalLogSeverity.DEBUG),
                eq("Impossible to put flow node instance in failed state: flow node instance with id '" + FLOW_NODE_INSTANCE_ID + "' not found."));
        verify(activityInstanceService, never()).setState(flowNodeInstance, failedState);
    }

    @Test
    public void setAsFailed_should_do_nothing_when_flow_node_is_already_in_failed_state() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willReturn(flowNodeInstance);
        given(flowNodeInstance.getStateId()).willReturn(FAILED_STATE_ID);

        //when
        failedStateSetter.setAsFailed(FLOW_NODE_INSTANCE_ID);

        //then
        verify(activityInstanceService, never()).setState(any(SFlowNodeInstance.class), any(FlowNodeState.class));
    }
}
