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
import javax.transaction.NotSupportedException;
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

    /**
     * We maintain a list of Callables that must be executed just before the real commit (and before the beforeCompletion method is called), so that we ensure
     * that Hibernate has not already flushed its session.
     */
    private final ThreadLocal<List<Callable<Void>>> beforeCommitCallables = new ThreadLocal<>();

    private final ThreadLocal<String> txLastBegin = new ThreadLocal<>();

    public JTATransactionServiceImpl(final TechnicalLoggerService logger, final TransactionManager txManager) {
        this.logger = logger;
        if (txManager == null) {
            throw new IllegalArgumentException("The TransactionManager cannot be null.");
        }
        this.txManager = txManager;
        txContextThreadLocal = new TransactionServiceContextThreadLocal();
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

        final TransactionServiceContext txContext = txContextThreadLocal.get();
        try {
            checkForNestedBonitaTransaction(txContext);
            txContext.setExternallyManaged(txManager.getStatus() == Status.STATUS_ACTIVE);
            txContext.setInScopeOfBonitaTransaction(true);

            //always clear before commit callables on begin
            beforeCommitCallables.remove();
            createTransaction(txContext);
            // Always Register a synchronization to clean the ThreadLocal variables.
            final Transaction tx = txManager.getTransaction();

            // Ensure the transaction is created and not set to rollback.
            if (tx != null) {
                registerSynchronization(tx);
            }
        } catch (final SystemException e) {
            resetTxContext(txContext);
            throw new STransactionCreationException(e);
        } catch (final STransactionCreationException e) {
            resetTxContext(txContext);
            throw e;
        }
    }

    void checkForNestedBonitaTransaction(TransactionServiceContext txContext) throws STransactionCreationException {
        if (txContext.isInScopeOfBonitaTransaction()) {
            TransactionState txState = null;
            try {
                txState = getState();
            } catch (STransactionException e) {
                e.printStackTrace();
            }
            String message = "We do not support nested calls to the transaction service. Current state is: " + txState + ". ";
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                message += "Last begin made by: " + txLastBegin.get();
            }
            throw new STransactionCreationException(message);
        }
    }

    private void registerSynchronization(final Transaction tx) throws SystemException, STransactionCreationException {
        try {
            // Then the monitoring of numberOfActiveTransactions is up-to-date.
            tx.registerSynchronization(new DecrementNumberOfActiveTransactionsSynchronization(this));
        } catch (final IllegalStateException | RollbackException e) {
            throw new STransactionCreationException(e);
        }
    }

    void createTransaction(TransactionServiceContext txContext) throws STransactionCreationException, SystemException {
        if (txContext.isExternallyManaged()) {
            //do not create the transaction if it's opened by an other system
            return;
        }
        boolean transactionStarted = false;
        try {
            txManager.begin();
            transactionStarted = true;
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE,
                        "Beginning transaction in thread " + Thread.currentThread().getId() + " " + txManager.getTransaction());
            }
            numberOfActiveTransactions.getAndIncrement();
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                txLastBegin.set(generateCurrentStack());
            }
        } catch (final NotSupportedException e) {
            // Should never happen as we do not want to support nested transaction
            throw new STransactionCreationException(e);
        } catch (final Exception t) {
            if (transactionStarted) {
                txManager.rollback();
            }
            throw new STransactionCreationException(t);
        }
    }

    private String generateCurrentStack() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append(" new transaction started by: ");
        sb.append("\n");
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
            sb.append("\n        at ");
            sb.append(stackTraceElement);
        }
        return sb.toString();
    }

    @Override
    public void complete() throws STransactionCommitException, STransactionRollbackException {
        // Depending of the txManager status we either commit or rollback.
        final TransactionServiceContext txContext = txContextThreadLocal.get();
        try {
            final Transaction tx = txManager.getTransaction();
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "Completing transaction in thread " + Thread.currentThread().getId() + " " + tx.toString());
            }

            final int status = txManager.getStatus();

            if (status == Status.STATUS_NO_TRANSACTION) {
                throw new SBonitaRuntimeException("No transaction started.");
            }

            if (txContext.isExternallyManaged()) {
                return; // We do not manage the transaction boundaries
            }

            if (status == Status.STATUS_MARKED_ROLLBACK) {
                rollback(tx);
            } else {
                commit();
            }
            this.txLastBegin.set(null);
        } catch (final SystemException e) {
            throw new STransactionCommitException(e);
        } finally {
            resetTxContext(txContext);
        }
    }

    protected void resetTxContext(TransactionServiceContext txContext) {
        txContext.setInScopeOfBonitaTransaction(false);
        txContext.setExternallyManaged(false);
    }

    void commit() throws SystemException, STransactionCommitException {
        try {
            final List<Callable<Void>> callables = beforeCommitCallables.get();
            if (callables != null) {
                for (final Callable<Void> callable : callables) {
                    try {
                        callable.call();
                    } catch (Exception e) {
                        throw new STransactionCommitException("Exception while executing callable in beforeCommit phase", e);
                    }
                }
                beforeCommitCallables.remove();
            }

            txManager.commit();
        } catch (final SecurityException | HeuristicRollbackException | HeuristicMixedException | RollbackException | IllegalStateException e) {
            throw new STransactionCommitException(e);
        }
    }

    private void rollback(final Transaction tx) throws SystemException, STransactionRollbackException {
        try {
            txManager.rollback();
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "Rollbacking transaction in thread " + Thread.currentThread().getId() + " " + tx.toString());
            }
        } catch (final IllegalStateException | SecurityException e) {
            throw new STransactionRollbackException(e);
        }
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
            transaction.registerSynchronization(new JTATransactionWrapper(txSync));
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
            List<Callable<Void>> callables = beforeCommitCallables.get();
            if (callables == null) {
                callables = new ArrayList<>();
                beforeCommitCallables.set(callables);
            }
            callables.add(callable);
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
        private boolean isInScopeOfBonitaTransaction = false;
        /*
         * true when the transaction was open outside of bonita
         */
        private boolean externallyManaged = false;

        public boolean isInScopeOfBonitaTransaction() {
            return isInScopeOfBonitaTransaction;
        }

        public void setInScopeOfBonitaTransaction(boolean inScopeOfBonitaTransaction) {
            this.isInScopeOfBonitaTransaction = inScopeOfBonitaTransaction;
        }

        public boolean isExternallyManaged() {
            return externallyManaged;
        }

        public void setExternallyManaged(boolean externallyManaged) {
            this.externallyManaged = externallyManaged;
        }
    }

    private static class TransactionServiceContextThreadLocal extends ThreadLocal<TransactionServiceContext> {

        @Override
        protected TransactionServiceContext initialValue() {
            return new TransactionServiceContext();
        }
    }
}
