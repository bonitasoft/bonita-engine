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
package org.bonitasoft.engine.api.impl.transaction.platform;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActivateTenantTest {

    @Mock
    private ConnectorExecutor connectorExecutor;

    @Mock
    private PlatformService platformService;

    @Mock
    private SchedulerService schedulerService;

    private final long tenantId = 17L;

    @Mock
    private WorkService workService;

    private ActivateTenant activateTenant;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        activateTenant = new ActivateTenant(tenantId, platformService, schedulerService, workService, connectorExecutor);
    }

    @Test
    public void should_start_connector_service_if_tenant_was_not_activated() throws Exception {
        STenant tenant = new STenant();
        tenant.setStatus(STenant.PAUSED);
        doReturn(tenant).when(platformService).getTenant(tenantId);

        activateTenant.execute();

        verify(connectorExecutor).start();
    }

    @Test
    public void should_resume_jobs_of_the_tenant_if_tenant_was_not_activated() throws Exception {
        STenant tenant = new STenant();
        tenant.setStatus(STenant.PAUSED);
        doReturn(tenant).when(platformService).getTenant(tenantId);

        activateTenant.execute();
        verify(schedulerService).resumeJobs(tenantId);
    }


    @Test
    public void should_not_do_anything_if_tenant_was_already_activated() throws Exception {
        STenant tenant = new STenant();
        tenant.setStatus(STenant.ACTIVATED);
        doReturn(tenant).when(platformService).getTenant(tenantId);

        activateTenant.execute();

        verifyZeroInteractions(schedulerService);
        verifyZeroInteractions(connectorExecutor);
    }
}
