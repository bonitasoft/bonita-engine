package org.bonitasoft.engine.execution.work;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BPMEventWorksHandlerTest {

    @Mock
    TenantServiceAccessor tenantServiceAccessor;

    @Mock
    PlatformServiceAccessor platformServiceAccessor;

    @Mock
    EventInstanceService eventInstanceService;

    @Test
    public void handleRestartShouldResetMessageInstances() throws Exception {
        // given:
        when(tenantServiceAccessor.getEventInstanceService()).thenReturn(eventInstanceService);
        BPMEventWorksHandler bpmEventWorksHandler = new BPMEventWorksHandler();

        // when:
        bpmEventWorksHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(eventInstanceService).resetProgressMessageInstances();
    }

    @Test
    public void handleRestartShouldResetWaitingEvents() throws Exception {
        // given:
        when(tenantServiceAccessor.getEventInstanceService()).thenReturn(eventInstanceService);
        BPMEventWorksHandler bpmEventWorksHandler = new BPMEventWorksHandler();

        // when:
        bpmEventWorksHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(eventInstanceService).resetInProgressWaitingEvents();
    }

}
