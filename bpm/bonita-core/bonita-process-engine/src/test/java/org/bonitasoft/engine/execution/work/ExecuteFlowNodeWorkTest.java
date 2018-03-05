/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution.work;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteFlowNodeWorkTest {

    public static final long FLOW_NODE_INSTANCE_ID = 564325L;
    public static final long PROCESS_INSTANCE_ID = 34552L;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private FlowNodeExecutor flowNodeExecutor;
    private SUserTaskInstanceImpl sHumanTaskInstance;
    private Map<String, Object> context;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws Exception {
        context = Collections.<String, Object> singletonMap(TenantAwareBonitaWork.TENANT_ACCESSOR, tenantServiceAccessor);
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
        doReturn(flowNodeExecutor).when(tenantServiceAccessor).getFlowNodeExecutor();
        sHumanTaskInstance = new SUserTaskInstanceImpl();
        sHumanTaskInstance.setId(FLOW_NODE_INSTANCE_ID);
        doReturn(sHumanTaskInstance).when(activityInstanceService).getHumanTaskInstance(FLOW_NODE_INSTANCE_ID);
    }

    @Test
    public void should_throw_exception_if_executing_human_task_not_ready_when_isReadyHumanTask_flag_is_true() throws Exception {
        //given
        ExecuteFlowNodeWork executeFlowNodeWork = new ExecuteFlowNodeWork(FLOW_NODE_INSTANCE_ID);
        executeFlowNodeWork.setReadyHumanTask(true);
        sHumanTaskInstance.setStateId(2);
        //when
        expectedException.expect(SFlowNodeExecutionException.class);
        expectedException.expectMessage("Unable to execute flow node " + FLOW_NODE_INSTANCE_ID + " because it is in an incompatible state");
        executeFlowNodeWork.work(context);
        //then exception
    }

    @Test
    public void should_throw_exception_if_executing_human_task_ready_but_not_executing_when_isReadyHumanTask_flag_is_true() throws Exception {
        //given
        ExecuteFlowNodeWork executeFlowNodeWork = new ExecuteFlowNodeWork(FLOW_NODE_INSTANCE_ID);
        executeFlowNodeWork.setReadyHumanTask(true);
        sHumanTaskInstance.setStateId(4);
        sHumanTaskInstance.setStateExecuting(false);
        //when
        expectedException.expect(SFlowNodeExecutionException.class);
        expectedException.expectMessage("Unable to execute flow node " + FLOW_NODE_INSTANCE_ID + " because it is in an incompatible state");
        executeFlowNodeWork.work(context);
        //then exception
    }

    @Test
    public void should_execute_human_task_in_ready_and_executing_if_isReadyHumanTask_flag_is_true() throws Exception {
        //given
        ExecuteFlowNodeWork executeFlowNodeWork = new ExecuteFlowNodeWork(FLOW_NODE_INSTANCE_ID);
        executeFlowNodeWork.setReadyHumanTask(true);
        sHumanTaskInstance.setStateId(4);
        sHumanTaskInstance.setStateExecuting(true);
        //when
        executeFlowNodeWork.work(context);
        //then
        verify(flowNodeExecutor).executeFlowNode(FLOW_NODE_INSTANCE_ID, null, null);
    }

    @Test
    public void should_execute_flow_node_in_any_state_if_isReadyHumanTask_flag_is_false() throws Exception {
        //given
        ExecuteFlowNodeWork executeFlowNodeWork = new ExecuteFlowNodeWork(FLOW_NODE_INSTANCE_ID);
        sHumanTaskInstance.setStateId(2);
        sHumanTaskInstance.setStateExecuting(true);
        //when
        executeFlowNodeWork.work(context);
        //then
        verify(flowNodeExecutor).executeFlowNode(FLOW_NODE_INSTANCE_ID, null, null);
    }
}
