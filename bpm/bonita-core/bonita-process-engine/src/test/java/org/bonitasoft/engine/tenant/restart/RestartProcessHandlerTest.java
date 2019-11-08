/*
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
 */
package org.bonitasoft.engine.tenant.restart;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.FlowNodeStateManagerImpl;
import org.bonitasoft.engine.execution.state.FailedActivityStateImpl;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class RestartProcessHandlerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    TechnicalLoggerService logger;
    @Mock
    ActivityInstanceService activityInstanceService;
    @Mock
    WorkService workService;
    @Mock
    BPMWorkFactory workFactory;
    @Mock
    FlowNodeStateManagerImpl flowNodeStateManager;

    @InjectMocks
    private RestartProcessHandler restartProcessHandler;

    @Test
    public void handleCompletionShouldDoNothingIfNoCallerId() throws Exception {

        restartProcessHandler.handleCompletion(mock(SProcessInstance.class), logger, activityInstanceService, workService, flowNodeStateManager, workFactory);
    }

    @Test
    public void handleCompletionShouldDoNothingIfCallActivityIsInFailedState() throws Exception {
        int failedStateId = 3;
        SActivityInstance callActivity = mock(SActivityInstance.class);
        when(callActivity.getStateId()).thenReturn(failedStateId);
        when(flowNodeStateManager.getFailedState()).thenReturn(new FailedActivityStateImpl());
        doReturn(callActivity).when(activityInstanceService).getActivityInstance(anyLong());
        SProcessInstance processInstance = mock(SProcessInstance.class);
        when(processInstance.getCallerId()).thenReturn(5L);
        restartProcessHandler.handleCompletion(processInstance, logger, activityInstanceService, workService, flowNodeStateManager, workFactory);
    }

    @Test
    public void handleCompletionShouldExecuteParentCallActivityIfItIsNOTInFailedState() throws Exception {
        int callActivityStateId = 8;
        SActivityInstance callActivity = mock(SActivityInstance.class);
        when(callActivity.getStateId()).thenReturn(callActivityStateId);
        when(flowNodeStateManager.getFailedState()).thenReturn(new FailedActivityStateImpl());
        doReturn(callActivity).when(activityInstanceService).getActivityInstance(anyLong());
        SProcessInstance processInstance = mock(SProcessInstance.class);
        //        when(processInstance.getId()).thenReturn(1654534L);
        when(processInstance.getCallerId()).thenReturn(5L);
        restartProcessHandler.handleCompletion(processInstance, logger, activityInstanceService, workService, flowNodeStateManager, workFactory);
    }
}
