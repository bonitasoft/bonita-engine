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
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class RestartFlowNodesHandlerTest {

    @InjectMocks
    private RestartFlowNodesHandler restartFlowNodesHandler;

    private PlatformServiceAccessor platformServiceAccessor;
    private TenantServiceAccessor tenantServiceAccessor;
    private TechnicalLoggerService logger;
    private ActivityInstanceService activityInstanceService;

    @Before
    public void before() {
        platformServiceAccessor = mock(PlatformServiceAccessor.class);
        tenantServiceAccessor = mock(TenantServiceAccessor.class);

        logger = mock(TechnicalLoggerService.class);
        doReturn(false).when(logger).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
        doReturn(logger).when(tenantServiceAccessor).getTechnicalLoggerService();
        activityInstanceService = mock(ActivityInstanceService.class);
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();
    }

    @Test
    public final void do_nothing_if_no_flownode() throws Exception {
        //given
        doReturn(123l).when(tenantServiceAccessor).getTenantId();
        doReturn(Collections.EMPTY_LIST).when(activityInstanceService).getFlowNodeInstanceIdsToRestart(any(QueryOptions.class));

        //when
        restartFlowNodesHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);

        //then
        assertThat(restartFlowNodesHandler.flownodesToRestartByTenant.get(123l)).isEmpty();
    }

    @Test(expected = RestartException.class)
    public final void throw_exception_if_error_when_get_flownode() throws Exception {
        //given
        doThrow(new SBonitaReadException("plop")).when(activityInstanceService).getFlowNodeInstanceIdsToRestart(any(QueryOptions.class));

        //when
        restartFlowNodesHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
    }
}
