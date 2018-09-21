package org.bonitasoft.engine.execution.work;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.impl.SAutomaticTaskInstanceImpl;
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

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;

public class NotifyChildFinishedWorkTest {

    private static final int PROCESS_DEFINITION_ID = 123;
    private static final int FLOW_NODE_INSTANCE_ID = 345;
    private static final int PARENT_ID = 456;
    private static final String PARENT_TYPE = "parentType";
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
        doReturn(this.getClass().getClassLoader()).when(classLoaderService).getLocalClassLoader(anyString(), anyLong());
        context = Collections.singletonMap(TenantAwareBonitaWork.TENANT_ACCESSOR, tenantServiceAccessor);
        notifyChildFinishedWork = new NotifyChildFinishedWork(PROCESS_DEFINITION_ID, FLOW_NODE_INSTANCE_ID, PARENT_ID, PARENT_TYPE);

        doReturn(classLoaderService).when(tenantServiceAccessor).getClassLoaderService();
        doReturn(flowNodeInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
    }

    @Test
    public void should_throw_precondition_exception_when_flownode_is_not_found() throws Exception {
        doThrow(SFlowNodeNotFoundException.class).when(flowNodeInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);

        expectedException.expect(SWorkPreconditionException.class);
        expectedException.expectMessage("Flow node is already completed ( not found )");

        notifyChildFinishedWork.work(context);
    }

    @Test
    public void should_throw_precondition_exception_when_flownode_is_completed() throws Exception {
        SAutomaticTaskInstanceImpl flowNodeInstance = new SAutomaticTaskInstanceImpl();
        flowNodeInstance.setTerminal(false);
        doReturn(flowNodeInstance).when(flowNodeInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);


        expectedException.expect(SWorkPreconditionException.class);
        expectedException.expectMessage("Flow node is not in a terminal state");

        notifyChildFinishedWork.work(context);
    }

    @Test
    public void should_notify_child_finished_if_flow_node_is_completed() throws Exception {
        doReturn(containerRegistry).when(tenantServiceAccessor).getContainerRegistry();
        SAutomaticTaskInstanceImpl flowNodeInstance = new SAutomaticTaskInstanceImpl();
        flowNodeInstance.setTerminal(true);
        doReturn(flowNodeInstance).when(flowNodeInstanceService).getFlowNodeInstance(FLOW_NODE_INSTANCE_ID);

        notifyChildFinishedWork.work(context);

        verify(containerRegistry).nodeReachedState(PROCESS_DEFINITION_ID, flowNodeInstance, PARENT_ID, PARENT_TYPE);
    }

}