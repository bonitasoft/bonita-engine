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
package org.bonitasoft.engine.transaction;

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

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class JTATransactionServiceImpl implements TransactionService {

    protected final TechnicalLoggerService logger;

    private final TransactionManager txManager;

    private final AtomicLong numberOfActiveTransactions = new AtomicLong(0);

    private final ThreadLocal<AtomicLong> counter = new ThreadLocal<AtomicLong>();

    public JTATransactionServiceImpl(final TechnicalLoggerService logger, final BonitaTransactionManagerLookup txManagerLookup) {
        this(logger, txManagerLookup.getTransactionManager());
    }

    public JTATransactionServiceImpl(final TechnicalLoggerService logger, final TransactionManager txManager) {
        this.logger = logger;
        if (txManager == null) {
            throw new IllegalArgumentException("The parameter txManager can't be null.");
        }
        this.txManager = txManager;
    }

    @Override
    public void begin() throws STransactionCreationException {
        try {
            AtomicLong counterTL = counter.get();
            synchronized(counter) {
                if (counterTL == null) {
                    counterTL = new AtomicLong();
                    counter.set(counterTL);
                }
            }
            if (counterTL.get() == 1) {
                throw new STransactionCreationException("We do not support nested calls to the transaction service.");
            }
            counterTL.getAndIncrement();

            if (txManager.getStatus() == Status.STATUS_NO_TRANSACTION) {
                boolean transactionStarted = false;
                try {
                    txManager.begin();
                    transactionStarted = true;

                    final Transaction tx = txManager.getTransaction();
                    if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                        logger.log(getClass(), TechnicalLogSeverity.TRACE,
                                "Beginning transaction in thread " + Thread.currentThread().getId() + " " + tx.toString());
                    }
                    // Avoid a memory-leak by cleaning the ThreadLocal after the transaction's completion
                    tx.registerSynchronization(new ResetCounterSynchronization(this));

                    // Ensuite the monitoring of numberOfActiveTransactions is up-to-date.
                    numberOfActiveTransactions.getAndIncrement();
                    tx.registerSynchronization(new DecrementNumberOfActiveTransactionsSynchronization(this));

                } catch (final NotSupportedException e) {
                    // Should never happen as we do not want to support nested transaction
                    throw new STransactionCreationException(e);
                } catch (final Throwable t) {
                    if (transactionStarted) {
                        txManager.rollback();
                    }
                    throw new STransactionCreationException(t);
                }
            }
        } catch (final SystemException e) {
            throw new STransactionCreationException(e);
        }
    }

    @Override
    public void complete() throws STransactionCommitException, STransactionRollbackException {

        // Depending of the txManager status we either commit or rollback.
        try {
            final Transaction tx = txManager.getTransaction();
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "Completing transaction in thread " + Thread.currentThread().getId() + " " + tx.toString());
            }

            final int status = txManager.getStatus();

            if (status == Status.STATUS_NO_TRANSACTION) {
                throw new RuntimeException("No transaction started.");
            }

            final AtomicLong counterTL = counter.get();
            counterTL.getAndDecrement();
            if (counterTL.get() != 0) {
                return; // We do not manage the transaction boundaries
            }

            if (status == Status.STATUS_MARKED_ROLLBACK) {
                try {
                    txManager.rollback();
                    if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                        logger.log(getClass(), TechnicalLogSeverity.TRACE,
                                "Rollbacking transaction in thread " + Thread.currentThread().getId() + " " + tx.toString());
                    }
                } catch (final IllegalStateException e) {
                    throw new STransactionRollbackException("", e);
                } catch (final SecurityException e) {
                    throw new STransactionRollbackException("", e);
                }
            } else {
                try {
                    txManager.commit();
                } catch (final SecurityException e) {
                    throw new STransactionCommitException("", e);
                } catch (final IllegalStateException e) {
                    throw new STransactionCommitException("", e);
                } catch (final RollbackException e) {
                    throw new STransactionCommitException("", e);
                } catch (final HeuristicMixedException e) {
                    throw new STransactionCommitException("", e);
                } catch (final HeuristicRollbackException e) {
                    throw new STransactionCommitException("", e);
                }
            }
        } catch (final SystemException e) {
            throw new STransactionCommitException("", e);
        }

    }

    @Override
    public TransactionState getState() throws STransactionException {
        // TODO Factorize this with the TransactionWrapper.convert
        try {
            final int status = txManager.getStatus();

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
                throw new STransactionException("Can't map the JTA status : " + status);
            }
        } catch (final SystemException e) {
            throw new STransactionException("", e);
        }
    }

    @Override
    public boolean isTransactionActive() throws STransactionException {
        try {
            return txManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (final SystemException e) {
            throw new STransactionException("", e);
        }
    }

    @Override
    public void setRollbackOnly() throws STransactionException {
        try {
            txManager.setRollbackOnly();
        } catch (final IllegalStateException e) {
            throw new STransactionException("", e);
        } catch (final SystemException e) {
            throw new STransactionException("", e);
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
                throw new STransactionNotFoundException("No active transaction");
            }
            transaction.registerSynchronization(new JTATransactionWrapper(txSync));
        } catch (final IllegalStateException e) {
            throw new STransactionNotFoundException(e.getMessage());
        } catch (final RollbackException e) {
            throw new STransactionNotFoundException(e.getMessage());
        } catch (final SystemException e) {
            throw new STransactionNotFoundException(e.getMessage());
        }
    }

    @Override
    public <T> T executeInTransaction(final Callable<T> callable) throws Exception {
        begin();
        try {
            return callable.call();
        } catch (final Exception e) {
            setRollbackOnly();
            throw e;
        } finally {
            complete();
        }
    }

    @Override
    public long getNumberOfActiveTransactions() {
        return numberOfActiveTransactions.get();
    }


    private class ResetCounterSynchronization implements Synchronization {

        private final JTATransactionServiceImpl txService;

        public ResetCounterSynchronization(final JTATransactionServiceImpl txService) {
            this.txService = txService;
        }

        @Override
        public void beforeCompletion() {
            // Nothing to do
        }

        @Override
        public void afterCompletion(final int status) {
            // Whatever the tx status, reset the counter
            txService.counter.set(null);
        }
    }

    private class DecrementNumberOfActiveTransactionsSynchronization implements Synchronization {

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

}
