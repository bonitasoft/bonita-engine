package org.bonitasoft.engine.test.execution;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.execution.TransactionExecutorImpl;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Matthieu Chaffotte
 */
public class TransactionExecutorImplTest {

    @Test(expected = SBonitaException.class)
    public void failTransaction() throws SBonitaException {
        final TransactionService transactionService = mock(TransactionService.class);
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final TransactionExecutorImpl executor = new TransactionExecutorImpl(transactionService);
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
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final TransactionExecutorImpl executor = new TransactionExecutorImpl(transactionService);
        final CountAnswer count = new CountAnswer();

        doAnswer(count).when(transactionContent).execute();
        executor.execute(transactionContent);

        verify(transactionService, times(1)).begin();
        verify(transactionService, never()).setRollbackOnly();
        verify(transactionService, times(1)).complete();
        assertEquals(1, count.getCount());
    }

}
