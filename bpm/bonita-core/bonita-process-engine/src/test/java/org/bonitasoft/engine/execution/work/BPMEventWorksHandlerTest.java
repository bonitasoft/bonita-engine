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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
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

    @Mock
    TechnicalLoggerService technicalLoggerService;

    private BPMEventWorksHandler bpmEventWorksHandler;

    @Before
    public void initMocks() {
        when(tenantServiceAccessor.getEventInstanceService()).thenReturn(eventInstanceService);
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(technicalLoggerService);
        bpmEventWorksHandler = spy(new BPMEventWorksHandler());
    }

    @Test
    public void handleRestartShouldLog4Infos() throws Exception {
        // when:
        bpmEventWorksHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(bpmEventWorksHandler, times(4)).logInfo(any(TechnicalLoggerService.class), anyString());
    }

    @Test
    public void handleRestartShouldResetMessageInstances() throws Exception {
        // when:
        bpmEventWorksHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(eventInstanceService).resetProgressMessageInstances();
    }

    @Test
    public void handleRestartShouldResetWaitingEvents() throws Exception {
        // when:
        bpmEventWorksHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(eventInstanceService).resetInProgressWaitingEvents();
    }

}
