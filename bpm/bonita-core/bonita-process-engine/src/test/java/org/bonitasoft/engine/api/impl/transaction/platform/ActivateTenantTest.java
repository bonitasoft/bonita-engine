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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActivateTenantTest {

    @Mock
    private ConnectorExecutor connectorExecutor;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private NodeConfiguration plaformConfiguration;

    @Mock
    private PlatformService platformService;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private TenantConfiguration tenantConfiguration;

    private final long tenantId = 17L;

    @Mock
    private WorkService workService;

    private ActivateTenant activateTenant;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        activateTenant = new ActivateTenant(tenantId, platformService, schedulerService, logger, workService, connectorExecutor, plaformConfiguration,
                tenantConfiguration);
    }

    @Test
    public void executeShouldStartConnectorExecutor() throws Exception {
        given(platformService.activateTenant(tenantId)).willReturn(true);
        activateTenant.execute();

        verify(connectorExecutor).start();
    }

}
