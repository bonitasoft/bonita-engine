/**
 * Copyright (C) 2014 BonitaSoft S.A.
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.Collections;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Celine Souchet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WorkFactory.class)
public class RestartFlowNodesHandlerTest {

    @InjectMocks
    private RestartFlowNodesHandler restartFlowNodesHandler;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.execution.work.RestartFlowNodesHandler#handleRestart(org.bonitasoft.engine.service.PlatformServiceAccessor, org.bonitasoft.engine.service.TenantServiceAccessor)}
     * .
     */
    @Test
    public final void create_execute_flowNode_work_when_flownode_isnt_terminal() throws Exception {
        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        final WorkService workService = mock(WorkService.class);

        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        doReturn(false).when(logger).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
        doReturn(logger).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(workService).when(tenantServiceAccessor).getWorkService();

        final ActivityInstanceService activityInstanceService = mock(ActivityInstanceService.class);
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
        final SFlowNodeInstance sFlowNodeInstance = mock(SFlowNodeInstance.class);
        doReturn(1L).when(sFlowNodeInstance).getId();
        doReturn(2L).when(sFlowNodeInstance).getParentProcessInstanceId();
        doReturn(false).when(sFlowNodeInstance).isTerminal();
        doReturn(Collections.singletonList(sFlowNodeInstance)).doReturn(Collections.EMPTY_LIST).when(activityInstanceService)
                .getFlowNodeInstancesToRestart(any(QueryOptions.class));

        PowerMockito.mockStatic(WorkFactory.class);

        restartFlowNodesHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);

        PowerMockito.verifyStatic(times(1));
        WorkFactory.createExecuteFlowNodeWork(sFlowNodeInstance.getProcessDefinitionId(), sFlowNodeInstance.getParentProcessInstanceId(),
                sFlowNodeInstance.getId(), null, null);
    }

    @Test
    public final void create_notify_child_finished_work_when_flownode_is_terminal() throws Exception {
        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        final WorkService workService = mock(WorkService.class);

        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        doReturn(false).when(logger).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
        doReturn(logger).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(workService).when(tenantServiceAccessor).getWorkService();

        final ActivityInstanceService activityInstanceService = mock(ActivityInstanceService.class);
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
        final SFlowNodeInstance sFlowNodeInstance = mock(SFlowNodeInstance.class);
        doReturn(1L).when(sFlowNodeInstance).getId();
        doReturn(2L).when(sFlowNodeInstance).getParentProcessInstanceId();
        doReturn(3L).when(sFlowNodeInstance).getProcessDefinitionId();
        doReturn(4L).when(sFlowNodeInstance).getParentContainerId();
        doReturn(SFlowElementsContainerType.PROCESS).when(sFlowNodeInstance).getParentContainerType();
        doReturn(5).when(sFlowNodeInstance).getStateId();
        doReturn(true).when(sFlowNodeInstance).isTerminal();
        doReturn(Collections.singletonList(sFlowNodeInstance)).doReturn(Collections.EMPTY_LIST).when(activityInstanceService)
                .getFlowNodeInstancesToRestart(any(QueryOptions.class));

        PowerMockito.mockStatic(WorkFactory.class);

        restartFlowNodesHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);

        PowerMockito.verifyStatic(times(1));
        WorkFactory.createNotifyChildFinishedWork(sFlowNodeInstance.getProcessDefinitionId(), sFlowNodeInstance.getParentProcessInstanceId(),
                sFlowNodeInstance.getId(), sFlowNodeInstance.getParentContainerId(), sFlowNodeInstance.getParentContainerType().name());
    }

    @Test
    public final void do_nothing_if_no_flownode() throws Exception {
        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        final WorkService workService = mock(WorkService.class);

        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        doReturn(false).when(logger).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
        doReturn(logger).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(workService).when(tenantServiceAccessor).getWorkService();

        final ActivityInstanceService activityInstanceService = mock(ActivityInstanceService.class);
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
        doReturn(Collections.EMPTY_LIST).when(activityInstanceService).getFlowNodeInstancesToRestart(any(QueryOptions.class));

        PowerMockito.mockStatic(WorkFactory.class);

        restartFlowNodesHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);

        PowerMockito.verifyStatic(times(0));
    }

    @Test(expected = RestartException.class)
    public final void throw_exception_if_error_when_registers_work() throws Exception {
        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        final WorkService workService = mock(WorkService.class);
        doThrow(new SWorkRegisterException("plop")).when(workService).registerWork(any(BonitaWork.class));

        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        doReturn(false).when(logger).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
        doReturn(logger).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(workService).when(tenantServiceAccessor).getWorkService();

        final ActivityInstanceService activityInstanceService = mock(ActivityInstanceService.class);
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
        final SFlowNodeInstance sFlowNodeInstance = mock(SFlowNodeInstance.class);
        doReturn(1L).when(sFlowNodeInstance).getId();
        doReturn(2L).when(sFlowNodeInstance).getParentProcessInstanceId();
        doReturn(false).when(sFlowNodeInstance).isTerminal();
        doReturn(Collections.singletonList(sFlowNodeInstance)).doReturn(Collections.EMPTY_LIST).when(activityInstanceService)
                .getFlowNodeInstancesToRestart(any(QueryOptions.class));

        PowerMockito.mockStatic(WorkFactory.class);

        restartFlowNodesHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);
    }

    @Test(expected = RestartException.class)
    public final void throw_exception_if_error_when_get_flownode() throws Exception {
        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        final WorkService workService = mock(WorkService.class);

        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        doReturn(false).when(logger).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
        doReturn(logger).when(tenantServiceAccessor).getTechnicalLoggerService();
        doReturn(workService).when(tenantServiceAccessor).getWorkService();

        final ActivityInstanceService activityInstanceService = mock(ActivityInstanceService.class);
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
        doThrow(new SFlowNodeReadException("plop")).when(activityInstanceService).getFlowNodeInstancesToRestart(any(QueryOptions.class));

        PowerMockito.mockStatic(WorkFactory.class);

        restartFlowNodesHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);
    }
}
