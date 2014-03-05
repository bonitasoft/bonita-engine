package org.bonitasoft.engine.execution;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.junit.Test;
import org.mockito.Mockito;

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

    @SuppressWarnings("unchecked")
    @Test(expected = SBonitaException.class)
    public void executeTransactionWithSBonitaException() throws Exception {
        // To keep compatibility the SBonitaException has to be rethrown

        final TransactionService transactionService = new MockTransactionService();
        final TransactionContent transactionContent = mock(TransactionContent.class);
        @SuppressWarnings("deprecation")
        final TransactionExecutorImpl executor = new TransactionExecutorImpl(transactionService);

        Mockito.doThrow(new SBonitaException() {

            private static final long serialVersionUID = -2042720042398918977L;
        }).when(transactionContent).execute();

        executor.execute(transactionContent);

        verify(transactionService, times(1)).executeInTransaction(any(Callable.class));
    }

    // Minimal implementation of a TransactionService (we do not (and don't want to !) see the TransactionServiceForTest)
    class MockTransactionService implements TransactionService {

        @Override
        public void begin() throws STransactionCreationException {
            // TODO Auto-generated method stub

        }

        @Override
        public void complete() throws STransactionCommitException, STransactionRollbackException {
            // TODO Auto-generated method stub

        }

        @Override
        public TransactionState getState() throws STransactionException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isTransactionActive() throws STransactionException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setRollbackOnly() throws STransactionException {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isRollbackOnly() throws STransactionException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public <T> T executeInTransaction(final Callable<T> callable) throws Exception {
            begin();
            try {
                return callable.call();
            } catch (Exception e) {
                setRollbackOnly();
                throw e;
            } finally {
                complete();
            }
        }

        @Override
        public void registerBonitaSynchronization(final BonitaTransactionSynchronization txSync) throws STransactionNotFoundException {
            // TODO Auto-generated method stub

        }
        
        @Override
        public void registerBeforeCommitCallable(Callable<Void> callable)
                throws STransactionNotFoundException {
            // TODO Auto-generated method stub
            
        }


        @Override
        public long getNumberOfActiveTransactions() {
            // TODO Auto-generated method stub
            return 0;
        }

    }

}
