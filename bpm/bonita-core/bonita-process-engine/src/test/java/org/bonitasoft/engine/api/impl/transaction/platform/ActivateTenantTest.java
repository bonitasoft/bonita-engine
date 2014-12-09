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
