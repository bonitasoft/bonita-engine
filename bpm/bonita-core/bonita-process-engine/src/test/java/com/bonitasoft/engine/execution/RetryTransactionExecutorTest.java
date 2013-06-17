/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
