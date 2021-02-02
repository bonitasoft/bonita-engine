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
package org.bonitasoft.engine.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WorkServiceImplTest {

    private WorkServiceImpl workService;
    @Mock
    private UserTransactionService transactionService;
    @Mock
    private TechnicalLoggerService loggerService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private WorkExecutorService workExecutorService;
    @Mock
    private EngineClock engineClock;

    @Before
    public void before() throws Exception {
        doReturn(mock(TechnicalLogger.class)).when(loggerService).asLogger(any());
        doReturn(1L).when(sessionAccessor).getTenantId();
        workService = new WorkServiceImpl(transactionService, loggerService, sessionAccessor, workExecutorService,
                engineClock, 0);
    }

    @Test
    public void should_register_work_set_the_work_registration_instant()
            throws SBonitaException {
        Instant registrationInstant = Instant.now().minus(25, ChronoUnit.HOURS);
        doReturn(registrationInstant).when(engineClock).now();
        // given
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        // when
        workService.registerWork(workDescriptor);

        // then
        assertThat(workDescriptor.getRegistrationDate()).isEqualTo(registrationInstant);
    }

    @Test
    public void should_register_a_new_synchronization_for_each_work() throws SBonitaException {
        // given
        WorkDescriptor workDescriptor1 = WorkDescriptor.create("MY_WORK1");
        WorkDescriptor workDescriptor2 = WorkDescriptor.create("MY_WORK2");

        // when
        workService.registerWork(workDescriptor1);
        workService.registerWork(workDescriptor2);

        // then
        verify(transactionService).registerBonitaSynchronization(
                argThat(s -> ((WorkSynchronization) s).getWork().getType().equals("MY_WORK1")));
        verify(transactionService).registerBonitaSynchronization(
                argThat(s -> ((WorkSynchronization) s).getWork().getType().equals("MY_WORK2")));
    }

}
