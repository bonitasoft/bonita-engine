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
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessagesRestartHandlerTest {

    @Mock
    TenantServiceAccessor tenantServiceAccessor;
    @Mock
    UserTransactionService userTransactionService;
    @Mock
    PlatformServiceAccessor platformServiceAccessor;
    @Mock
    EventInstanceService eventInstanceService;
    @Mock
    TechnicalLoggerService technicalLoggerService;

    private MessagesRestartHandler messagesRestartHandler;

    @Before
    public void initMocks() {
        when(tenantServiceAccessor.getEventInstanceService()).thenReturn(eventInstanceService);
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(technicalLoggerService);
        when(tenantServiceAccessor.getUserTransactionService()).thenReturn(userTransactionService);
        messagesRestartHandler = spy(new MessagesRestartHandler());
    }

    @Test
    public void handleRestartShouldLog4Infos() throws Exception {
        // when:
        messagesRestartHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(messagesRestartHandler, times(4)).logInfo(any(TechnicalLoggerService.class), anyString());
    }

    @Test
    public void handleRestartShouldResetMessageInstances() throws Exception {
        // when:
        messagesRestartHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(eventInstanceService).resetProgressMessageInstances();
    }

    @Test
    public void handleRestartShouldResetWaitingEvents() throws Exception {
        // when:
        messagesRestartHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);

        // then:
        verify(eventInstanceService).resetInProgressWaitingEvents();
    }

    @Test
    public void should_execute_event_handling_in_transaction() throws Exception {
        //given

        //when
        messagesRestartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);
        //then
        verify(userTransactionService).executeInTransaction(any());
    }

}
