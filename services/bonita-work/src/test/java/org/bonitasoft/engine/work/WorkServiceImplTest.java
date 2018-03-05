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
package org.bonitasoft.engine.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WorkServiceImplTest {

    @InjectMocks
    private WorkServiceImpl workService;
    @Mock
    private UserTransactionService transactionService;
    @Mock
    private TechnicalLoggerService loggerService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private WorkExecutorService workExecutorService;
    @Captor
    private ArgumentCaptor<WorkSynchronization> synchronizationArgumentCaptor;

    @Before
    public void before() throws Exception {
        doReturn(1L).when(sessionAccessor).getTenantId();
    }

    @Test
    public void should_register_work_on_the_transaction_synchronization() throws SBonitaException {
        // given
        WorkDescriptor bonitaWork = WorkDescriptor.create("MY_WORK");

        // when
        workService.registerWork(bonitaWork);

        // then
        assertThat(getWorkSynchronization().getWorks()).containsExactly(bonitaWork);
    }

    @Test
    public void should_register_multiple_work_on_the_same_transaction_synchronization() throws SBonitaException {
        // given
        WorkDescriptor bonitaWork1 = WorkDescriptor.create("MY_WORK1");
        WorkDescriptor bonitaWork2 = WorkDescriptor.create("MY_WORK2");

        // when
        workService.registerWork(bonitaWork1);
        workService.registerWork(bonitaWork2);

        // then
        assertThat(getWorkSynchronization().getWorks()).containsOnly(bonitaWork1, bonitaWork2);
    }

    private WorkSynchronization getWorkSynchronization() throws STransactionNotFoundException {
        verify(transactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());
        return synchronizationArgumentCaptor.getValue();
    }


}
