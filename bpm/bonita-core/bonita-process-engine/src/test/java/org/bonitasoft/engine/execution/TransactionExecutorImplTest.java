package org.bonitasoft.engine.execution;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class TransactionExecutorImplTest {

    @SuppressWarnings("unchecked")
    @Test
    public void executeTransaction() throws Exception {
        final TransactionService transactionService = mock(TransactionService.class);
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final TransactionExecutorImpl executor = new TransactionExecutorImpl(transactionService);

        executor.execute(transactionContent);

        verify(transactionService, times(1)).executeInTransaction(any(Callable.class));
    }
    
}
