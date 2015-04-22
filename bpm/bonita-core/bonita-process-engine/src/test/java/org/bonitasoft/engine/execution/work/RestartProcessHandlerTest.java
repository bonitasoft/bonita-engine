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
package org.bonitasoft.engine.execution.work;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.FlowNodeStateManagerImpl;
import org.bonitasoft.engine.execution.state.FailedActivityStateImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestartProcessHandlerTest {

    @Mock
    TechnicalLoggerService logger;
    @Mock
    ActivityInstanceService activityInstanceService;
    @Mock
    WorkService workService;
    @Mock
    FlowNodeStateManagerImpl flowNodeStateManager;

    @Test
    public void handleCompletionShouldDoNothingIfNoCallerId() throws Exception {
        new RestartProcessHandler().handleCompletion(mock(SProcessInstance.class), logger, activityInstanceService, workService, flowNodeStateManager);
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
        new RestartProcessHandler().handleCompletion(processInstance, logger, activityInstanceService, workService, flowNodeStateManager);
    }

    @Test
    public void handleCompletionShouldExecuteParentCallActivityIfItIsNOTInFailedState() throws Exception {
        int callActivityStateId = 8;
        SActivityInstance callActivity = mock(SActivityInstance.class);
        when(callActivity.getParentProcessInstanceId()).thenReturn(564654L);
        when(callActivity.getStateId()).thenReturn(callActivityStateId);
        when(flowNodeStateManager.getFailedState()).thenReturn(new FailedActivityStateImpl());
        doReturn(callActivity).when(activityInstanceService).getActivityInstance(anyLong());
        SProcessInstance processInstance = mock(SProcessInstance.class);
        //        when(processInstance.getId()).thenReturn(1654534L);
        when(processInstance.getCallerId()).thenReturn(5L);
        doNothing().when(workService).executeWork(any(BonitaWork.class));
        new RestartProcessHandler().handleCompletion(processInstance, logger, activityInstanceService, workService, flowNodeStateManager);
    }
}
