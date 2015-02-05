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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SAutomaticTaskInstanceImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkService;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteFlowNodesTest {

    /**
     * @author Baptiste Mesta
     */
    private final class WrapWorkOfType extends BaseMatcher<BonitaWork> {

        private final Class<?> class1;

        public WrapWorkOfType(final Class<?> class1) {
            this.class1 = class1;
        }

        @Override
        public boolean matches(final Object item) {
            if (item instanceof WrappingBonitaWork) {
                BonitaWork wrappedWork = ((WrappingBonitaWork) item).getWrappedWork();
                if (wrappedWork instanceof WrappingBonitaWork) {
                    WrappingBonitaWork innerWork = (WrappingBonitaWork) wrappedWork;
                    return new WrapWorkOfType(class1).matches(innerWork);
                }
                return class1.isInstance(wrappedWork);
            }
            return false;
        }

        @Override
        public void describeTo(final Description description) {

        }
    }

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private final ProcessDefinitionService processDefinitionService = mock(ProcessDefinitionService.class);

    @Mock
    private final GatewayInstanceService gatewayInstanceService = mock(GatewayInstanceService.class);

    @Mock
    private Iterator<Long> iterator;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private WorkService workService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Before
    public void before() {
        when(tenantServiceAccessor.getActivityInstanceService()).thenReturn(activityInstanceService);
        when(tenantServiceAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        when(tenantServiceAccessor.getGatewayInstanceService()).thenReturn(gatewayInstanceService);
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(logger);
        when(tenantServiceAccessor.getWorkService()).thenReturn(workService);
    }

    private ExecuteFlowNodes createExecutorWith(final SFlowNodeInstance... flowNodes) throws Exception {
        ArrayList<Long> nodes = new ArrayList<Long>();
        for (SFlowNodeInstance node : flowNodes) {
            nodes.add(node.getId());
            when(activityInstanceService.getFlowNodeInstance(node.getId())).thenReturn(node);
        }
        return new ExecuteFlowNodes(tenantServiceAccessor, nodes.iterator());
    }

    @Test
    public final void execute_with_terminal_flow_node_should_register_notify_work() throws Exception {
        ExecuteFlowNodes executeFlowNodes = createExecutorWith(createTask(123l, true));

        executeFlowNodes.call();

        verify(workService).registerWork(argThat(new WrapWorkOfType(NotifyChildFinishedWork.class)));

    }

    private SAutomaticTaskInstanceImpl createTask(final long id, final boolean terminal) {
        SAutomaticTaskInstanceImpl sAutomaticTaskInstanceImpl = new SAutomaticTaskInstanceImpl();
        sAutomaticTaskInstanceImpl.setId(id);
        sAutomaticTaskInstanceImpl.setTerminal(terminal);
        sAutomaticTaskInstanceImpl.setLogicalGroup(3, 456l);
        return sAutomaticTaskInstanceImpl;
    }

    @Test
    public final void execute_with_non_terminal_flow_node_should_register_execute_work() throws Exception {
        ExecuteFlowNodes executeFlowNodes = createExecutorWith(createTask(123l, false));

        executeFlowNodes.call();

        verify(workService).registerWork(argThat(new WrapWorkOfType(ExecuteFlowNodeWork.class)));

    }

    @Test
    public final void executeFlownodeShouldNotCreateWorkIfNotApplicable() throws Exception {
        SAutomaticTaskInstanceImpl autoTask = createTask(123l, false);
        ExecuteFlowNodes executeFlowNodes = spy(createExecutorWith(autoTask));
        when(executeFlowNodes.shouldExecuteFlownode(autoTask)).thenReturn(false);
        executeFlowNodes.call();

        verify(executeFlowNodes, times(0)).createExecuteFlowNodeWork(workService, logger, autoTask);

    }

    @Test
    public final void execute_21_flow_node_only_execute_20() throws Exception {
        ArrayList<SFlowNodeInstance> list = new ArrayList<SFlowNodeInstance>();
        for (int i = 1; i <= 21; i++) {
            list.add(createTask(123 + i, false));
        }
        ExecuteFlowNodes executeFlowNodes = createExecutorWith(list.toArray(new SFlowNodeInstance[] {}));

        executeFlowNodes.call();

        assertThat(list.size()).isEqualTo(21);
        verify(workService, times(20)).registerWork(argThat(new WrapWorkOfType(ExecuteFlowNodeWork.class)));
    }

    @Test
    public void shouldExecuteFlownodeIfNotGateway() throws Exception {
        long gatewayId = 17L;
        SGatewayInstance gatewayInstance = mock(SGatewayInstance.class);
        when(gatewayInstance.getId()).thenReturn(gatewayId);
        when(gatewayInstance.getType()).thenReturn(SFlowNodeType.AUTOMATIC_TASK);
        ExecuteFlowNodes executeFlowNodes = new ExecuteFlowNodes(tenantServiceAccessor, null);

        boolean shouldExecuteFlownode = executeFlowNodes.shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isTrue();
        verifyNoMoreInteractions(processDefinitionService);
        verifyNoMoreInteractions(gatewayInstanceService);
    }

    @Test
    public void shouldExecuteFlownodeForGatewayWithMatchingMergeCondition() throws Exception {
        SGatewayInstance gatewayInstance = mock(SGatewayInstance.class);
        when(gatewayInstance.getType()).thenReturn(SFlowNodeType.GATEWAY);
        when(gatewayInstanceService.checkMergingCondition(any(SProcessDefinition.class), eq(gatewayInstance))).thenReturn(true);

        boolean shouldExecuteFlownode = new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isTrue();
    }

    @Test
    public void shouldNotExecuteFlownodeForGatewayWithNonMatchingMergeCondition() throws Exception {
        SGatewayInstance gatewayInstance = mock(SGatewayInstance.class);
        when(gatewayInstance.getType()).thenReturn(SFlowNodeType.GATEWAY);
        when(gatewayInstanceService.checkMergingCondition(any(SProcessDefinition.class), eq(gatewayInstance))).thenReturn(false);

        boolean shouldExecuteFlownode = new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isFalse();
    }

}
