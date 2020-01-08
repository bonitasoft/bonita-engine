/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.tenant.restart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.message.MessagesHandlingService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessagesRestartHandlerTest {

    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private EventInstanceRepository eventInstanceRepository;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Mock
    private MessagesHandlingService messagesHandlingService;
    @InjectMocks
    @Spy
    private MessagesRestartHandler messagesRestartHandler;

    @Test
    public void handleRestartShouldLog4Infos() throws Exception {
        // when:
        messagesRestartHandler.beforeServicesStart();

        // then:
        verify(messagesRestartHandler, times(4)).logInfo(any(TechnicalLoggerService.class), anyString());
    }

    @Test
    public void handleRestartShouldResetMessageInstances() throws Exception {
        // when:
        messagesRestartHandler.beforeServicesStart();

        // then:
        verify(eventInstanceRepository).resetProgressMessageInstances();
    }

    @Test
    public void handleRestartShouldResetWaitingEvents() throws Exception {
        // when:
        messagesRestartHandler.beforeServicesStart();

        // then:
        verify(eventInstanceRepository).resetInProgressWaitingEvents();
    }

    @Test
    public void should_execute_event_handling_in_transaction() throws Exception {
        //given

        //when
        messagesRestartHandler.afterServicesStart();
        //then
        verify(userTransactionService).executeInTransaction(any());
    }

}
