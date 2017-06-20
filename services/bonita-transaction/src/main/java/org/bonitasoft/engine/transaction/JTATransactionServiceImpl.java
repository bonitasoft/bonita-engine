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
package org.bonitasoft.engine.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class JTATransactionServiceImpl implements TransactionService {

    protected final TechnicalLoggerService logger;

    private final TransactionManager txManager;

    private final AtomicLong numberOfActiveTransactions = new AtomicLong(0);

    private final ThreadLocal<TransactionServiceContext> txContextThreadLocal;

    private boolean isTraceLoggable;

    public JTATransactionServiceImpl(final TechnicalLoggerService logger, final TransactionManager txManager) {
        this.logger = logger;
        if (txManager == null) {
            throw new IllegalArgumentException("The TransactionManager cannot be null.");
        }
        this.txManager = txManager;
        txContextThreadLocal = new TransactionServiceContextThreadLocal();
        isTraceLoggable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
    }

    public static TransactionState convert(int status) {
        switch (status) {
            case Status.STATUS_ACTIVE:
                return TransactionState.ACTIVE;
            case Status.STATUS_COMMITTED:
                return TransactionState.COMMITTED;
            case Status.STATUS_MARKED_ROLLBACK:
                return TransactionState.ROLLBACKONLY;
            case Status.STATUS_ROLLEDBACK:
                return TransactionState.ROLLEDBACK;
            case Status.STATUS_NO_TRANSACTION:
                return TransactionState.NO_TRANSACTION;
            default:
                throw new IllegalStateException("Can't map the JTA status : " + status);
        }
    }

    @Override
    public void begin() throws STransactionCreationException {
        final TransactionServiceContext txContext = getTransactionServiceContext();
        try {
            int status = txManager.getStatus();
            checkForNestedBonitaTransaction(txContext, status);
            clearPreviousTransaction(status);
            initTxContext(txContext, status);
            if (txContext.externallyManaged) {
                //do not open transaction because it was open externally
                return;
            }
            if (isTraceLoggable) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE,
                        "Beginning transaction in thread " + Thread.currentThread().getId() + " " + txManager.getTransaction());
                txContext.stackTraceThatMadeLastBegin = generateCurrentStack();
            }
            txManager.begin();
            handleNumberOfActiveTransactions();
        } catch (final STransactionCreationException e) {
            resetTxContext(txContext);
            throw e;
        } catch (final Throwable e) {
            resetTxContext(txContext);
            throw new STransactionCreationException(e);
        }
    }

    private void clearPreviousTransaction(int status) throws STransactionCreationException {
        if (status != Status.STATUS_ACTIVE && status != Status.STATUS_NO_TRANSACTION) {
            //the transaction is in an inconsistent state, we try to rollback
            logger.log(this.getClass(), TechnicalLogSeverity.WARNING,
                    "Starting a new transaction on the thread but there is already a transaction is state " + status +
                            ". Will try to call rollback on the transaction manager to cleanup the thread from this transaction.");
            try {
                txManager.rollback();
            } catch (SystemException e) {
                throw new STransactionCreationException("A transaction was already associated with the thread. We tried to " +
                        " rollback it but without success. If the transaction manager does not disassociate the thread with the transaction," +
                        " restart the server. Status of the transaction was " + status, e);
            }
        }
    }

    private void handleNumberOfActiveTransactions() throws RollbackException, SystemException {
        numberOfActiveTransactions.getAndIncrement();
        txManager.getTransaction().registerSynchronization(new DecrementNumberOfActiveTransactionsSynchronization(this));
    }

    private void initTxContext(TransactionServiceContext txContext, int status) {
        txContext.externallyManaged = (status == Status.STATUS_ACTIVE);
        txContext.isInScopeOfBonitaTransaction = true;
        txContext.beforeCommitCallables.clear();
    }

    private void checkForNestedBonitaTransaction(TransactionServiceContext txContext, int status) throws STransactionCreationException {
        if (txContext.isInScopeOfBonitaTransaction) {
            String message = "We do not support nested calls to the transaction service. Current state is: " + status + ". ";
            if (isTraceLoggable) {
                message += "Last begin made by: " + txContext.stackTraceThatMadeLastBegin;
            }
            throw new STransactionCreationException(message);
        }
    }

    private String generateCurrentStack() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append("\nNew transaction started by: ");
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTraceElements.length; i++) {
            sb.append("\n        at ");
            sb.append(stackTraceElements[i]);
        }
        return sb.toString();
    }

    @Override
    public void complete() throws STransactionCommitException, STransactionRollbackException {
        // Depending of the txManager status we either commit or rollback.
        final TransactionServiceContext txContext = getTransactionServiceContext();
        try {
            if (isTraceLoggable) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE,
                        "Completing transaction in thread " + Thread.currentThread().getId() + " " + txManager.getTransaction().toString());
            }
            final int status = txManager.getStatus();
            if (status == Status.STATUS_NO_TRANSACTION) {
                throw new STransactionCommitException("No transaction started.");
            }
            if (txContext.externallyManaged) {
                return; // We do not manage the transaction boundaries
            }
            if (status == Status.STATUS_MARKED_ROLLBACK) {
                if (isTraceLoggable) {
                    logger.log(getClass(), TechnicalLogSeverity.TRACE,
                            "Rollbacking transaction in thread " + Thread.currentThread().getId() + " " + txManager.getTransaction().toString());
                }
                txManager.rollback();
            } else {
                try {
                    executeBeforeCommitCallables(txContext);
                } finally {
                    //even if there is an issue in before commit callables execution, we always call the commit to ensure end of the tx
                    txManager.commit();
                }
            }
        } catch (final SystemException | HeuristicMixedException | HeuristicRollbackException | RollbackException e) {
            throw new STransactionCommitException(e);
        } finally {
            resetTxContext(txContext);
        }
    }

    TransactionServiceContext getTransactionServiceContext() {
        return txContextThreadLocal.get();
    }

    private void executeBeforeCommitCallables(TransactionServiceContext txContext) throws STransactionCommitException {
        final List<Callable<Void>> callables = txContext.beforeCommitCallables;
        for (final Callable<Void> callable : callables) {
            try {
                callable.call();
            } catch (Exception e) {
                throw new STransactionCommitException("Exception while executing callable in beforeCommit phase", e);
            }
        }
    }

    void resetTxContext(TransactionServiceContext txContext) {
        txContext.isInScopeOfBonitaTransaction = false;
        txContext.externallyManaged = false;
        txContext.beforeCommitCallables.clear();
        txContext.stackTraceThatMadeLastBegin = null;
    }

    public TransactionState getState() throws STransactionException {
        try {
            return convert(txManager.getStatus());
        } catch (final SystemException e) {
            throw new STransactionException(e);
        }
    }

    @Deprecated
    @Override
    public boolean isTransactionActive() throws STransactionException {
        try {
            return txManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (final SystemException e) {
            throw new STransactionException(e);
        }
    }

    @Override
    public void setRollbackOnly() throws STransactionException {
        try {
            txManager.setRollbackOnly();
        } catch (final IllegalStateException | SystemException e) {
            throw new STransactionException(e);
        }
    }

    @Override
    public boolean isRollbackOnly() throws STransactionException {
        try {
            return txManager.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (final SystemException e) {
            throw new STransactionException("Error while trying to get the transaction's status.", e);
        }
    }

    @Override
    public void registerBonitaSynchronization(final BonitaTransactionSynchronization txSync) throws STransactionNotFoundException {
        try {
            final Transaction transaction = txManager.getTransaction();
            if (transaction == null) {
                throw new STransactionNotFoundException("No active transaction.");
            }
            transaction.registerSynchronization(new JTATransactionWrapper(logger, txSync));
        } catch (final IllegalStateException | SystemException | RollbackException e) {
            throw new STransactionNotFoundException(e);
        }
    }

    @Override
    public void registerBeforeCommitCallable(final Callable<Void> callable) throws STransactionNotFoundException {
        try {
            final Transaction transaction = txManager.getTransaction();
            if (transaction == null) {
                throw new STransactionNotFoundException("No active transaction");
            }
            getTransactionServiceContext().beforeCommitCallables.add(callable);
        } catch (final IllegalStateException | SystemException e) {
            throw new STransactionNotFoundException(e.getMessage());
        }
    }

    @Override
    public <T> T executeInTransaction(final Callable<T> callable) throws Exception {
        begin();
        try {
            return callable.call();
        } catch (final Exception e) {
            log(callable, e);
            setRollbackOnly();
            throw e;
        } catch (final Throwable t) {
            log(callable, t);
            setRollbackOnly();
            throw new SBonitaRuntimeException(t);
        } finally {
            complete();
        }
    }

    private <T> void log(Callable<T> callable, Throwable e) {
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Setting rollbackOnly on current transaction because callable '" + callable
                    + "' has thrown an exception: " + e.getMessage(), e);
        }
    }

    @Override
    public long getNumberOfActiveTransactions() {
        return numberOfActiveTransactions.get();
    }

    private static class DecrementNumberOfActiveTransactionsSynchronization implements Synchronization {

        private final JTATransactionServiceImpl txService;

        public DecrementNumberOfActiveTransactionsSynchronization(final JTATransactionServiceImpl txService) {
            this.txService = txService;
        }

        @Override
        public void beforeCompletion() {
            // Nothing to do
        }

        @Override
        public void afterCompletion(final int status) {
            // Whatever the status, decrement the number of active transactions
            txService.numberOfActiveTransactions.getAndDecrement();
        }
    }

    static class TransactionServiceContext {

        /*
         * this flag means that we already called begin on the bonita transaction service whether or not the transaction is managed externally
         */
        boolean isInScopeOfBonitaTransaction = false;
        /*
         * true when the transaction was open outside of bonita
         */
        boolean externallyManaged = false;

        /**
         * We maintain a list of Callables that must be executed just before the real commit (and before the beforeCompletion method is called), so that we
         * ensure
         * that Hibernate has not already flushed its session.
         */
        List<Callable<Void>> beforeCommitCallables = new ArrayList<>();

        /**
         * for tracing only
         */
        String stackTraceThatMadeLastBegin;
    }

    private static class TransactionServiceContextThreadLocal extends ThreadLocal<TransactionServiceContext> {

        @Override
        protected TransactionServiceContext initialValue() {
            return new TransactionServiceContext();
        }
    }
}
