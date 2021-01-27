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
package org.bonitasoft.engine.execution.work;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.SWorkPreconditionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class NotifyChildFinishedWorkTest {

    private static final int PROCESS_DEFINITION_ID = 123;
    private static final int FLOW_NODE_INSTANCE_ID = 345;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private ActivityInstanceService flowNodeInstanceService;
    @Mock
    private ContainerRegistry containerRegistry;
    @Mock
    private ClassLoaderService classLoaderService;
    private Map<String, Object> context;
    private NotifyChildFinishedWork notifyChildFinishedWork;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws SClassLoaderException {
        doReturn(this.getClass().getClassLoader()).when(classLoaderService).getLocalClassLoader(any());
        context = Collections.singletonMap(TenantAwareBonitaWork.TENANT_ACCESSOR, tenantServiceAccessor);

        doReturn(classLoaderService).when(tenantServiceAccessor).getClassLoaderService();
        doReturn(flowNodeInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
    }

    @Test
    public void should_throw_precondition_exception_when_flownode_is_not_found() throws Exception {
        notifyChildFinishedWork = new NotifyChildFinishedWork(PROCESS_DEFINITION_ID, FLOW_NODE_INSTANCE_ID, 4, false,
                false, false);
        doThrow(SFlowNodeNotFoundException.class).when(flowNodeInstanceService)
                .getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);

        expectedException.expect(SWorkPreconditionException.class);
        expectedException.expectMessage("Flow node " + FLOW_NODE_INSTANCE_ID + " is already completed ( not found )");

        notifyChildFinishedWork.work(context);
    }

    @Test
    public void should_throw_precondition_exception_when_flownode_is_completed() throws Exception {
        notifyChildFinishedWork = new NotifyChildFinishedWork(PROCESS_DEFINITION_ID, FLOW_NODE_INSTANCE_ID, 1, false,
                false, false);
        SAutomaticTaskInstance flowNodeInstance = new SAutomaticTaskInstance();
        flowNodeInstance.setTerminal(false);
        flowNodeInstance.setStateId(1);
        doReturn(flowNodeInstance).when(flowNodeInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);

        expectedException.expect(SWorkPreconditionException.class);
        expectedException.expectMessage("Flow node " + FLOW_NODE_INSTANCE_ID + " is not yet completed");

        notifyChildFinishedWork.work(context);
    }

    @Test
    public void should_throw_precondition_exception_when_flownode_changed() throws Exception {
        notifyChildFinishedWork = new NotifyChildFinishedWork(PROCESS_DEFINITION_ID, FLOW_NODE_INSTANCE_ID, 3, false,
                false, false);
        SAutomaticTaskInstance flowNodeInstance = new SAutomaticTaskInstance();
        flowNodeInstance.setTerminal(true);
        flowNodeInstance.setStateId(2);
        doReturn(flowNodeInstance).when(flowNodeInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);

        expectedException.expect(SWorkPreconditionException.class);
        expectedException.expectMessage("Unable to execute flow node " + FLOW_NODE_INSTANCE_ID
                + " because it is not in the expected state " +
                "( expected state: 3, transitioning: false, aborting: false, canceling: false, but got  state: 2, transitioning: false, aborting: false, canceling: false)."
                +
                " Someone probably already called execute on it.");

        notifyChildFinishedWork.work(context);
    }

    @Test
    public void should_notify_child_finished_if_flow_node_is_completed() throws Exception {
        notifyChildFinishedWork = new NotifyChildFinishedWork(PROCESS_DEFINITION_ID, FLOW_NODE_INSTANCE_ID, 2, false,
                false, false);
        doReturn(containerRegistry).when(tenantServiceAccessor).getContainerRegistry();
        SAutomaticTaskInstance flowNodeInstance = new SAutomaticTaskInstance();
        flowNodeInstance.setTerminal(true);
        flowNodeInstance.setStateId(2);
        doReturn(flowNodeInstance).when(flowNodeInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);

        notifyChildFinishedWork.work(context);

        verify(containerRegistry).nodeReachedState(flowNodeInstance);
    }

}
