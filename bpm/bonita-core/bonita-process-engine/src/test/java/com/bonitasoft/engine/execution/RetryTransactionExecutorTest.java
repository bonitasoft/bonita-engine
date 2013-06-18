/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.execution;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Matthieu Chaffotte
 */
public class RetryTransactionExecutorTest {

    @Test
    public void runTheTransactionAtLeastOnce() throws SBonitaException {
        final TransactionService transactionService = mock(TransactionService.class);
        final TechnicalLoggerService loggerService = mock(TechnicalLoggerService.class);
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final RetryTransactionExecutor executor = new RetryTransactionExecutor(transactionService, loggerService, 0, 1, 2);
        final RetryAnswer retry = new RetryAnswer();
        doAnswer(retry).when(transactionContent).execute();
        executor.execute(transactionContent);
        assertEquals(1, retry.getCount());

        verify(transactionService, times(1)).begin();
        verify(transactionService, times(1)).setRollbackOnly();
        verify(transactionService, times(1)).complete();
    }

    @Test
    public void runTheTransactionTwice() throws SBonitaException {
        final TransactionService transactionService = mock(TransactionService.class);
        final TechnicalLoggerService loggerService = mock(TechnicalLoggerService.class);
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final RetryTransactionExecutor executor = new RetryTransactionExecutor(transactionService, loggerService, 1, 1, 2);
        final RetryAnswer retry = new RetryAnswer();
        doAnswer(retry).when(transactionContent).execute();

        executor.execute(transactionContent);
        assertEquals(2, retry.getCount());

        verify(transactionService, times(2)).begin();
        verify(transactionService, times(2)).setRollbackOnly();
        verify(transactionService, times(2)).complete();
    }

    @Test(expected = SBonitaException.class)
    public void failTransaction() throws SBonitaException {
        final TransactionService transactionService = mock(TransactionService.class);
        final TechnicalLoggerService loggerService = mock(TechnicalLoggerService.class);
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final RetryTransactionExecutor executor = new RetryTransactionExecutor(transactionService, loggerService, 0, 1, 2);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                throw new SUserNotFoundException(5);
            }
        }).when(transactionContent).execute();
        executor.execute(transactionContent);

        verify(transactionService, times(1)).begin();
        verify(transactionService, times(1)).setRollbackOnly();
        verify(transactionService, times(1)).complete();
    }

    @Test
    public void executeTransaction() throws SBonitaException {
        final TransactionService transactionService = mock(TransactionService.class);
        final TechnicalLoggerService loggerService = mock(TechnicalLoggerService.class);
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final RetryTransactionExecutor executor = new RetryTransactionExecutor(transactionService, loggerService, 10, 1, 2);
        final CountAnswer count = new CountAnswer();

        doAnswer(count).when(transactionContent).execute();
        executor.execute(transactionContent);

        verify(transactionService, times(1)).begin();
        verify(transactionService, never()).setRollbackOnly();
        verify(transactionService, times(1)).complete();
        assertEquals(1, count.getCount());
    }

}
