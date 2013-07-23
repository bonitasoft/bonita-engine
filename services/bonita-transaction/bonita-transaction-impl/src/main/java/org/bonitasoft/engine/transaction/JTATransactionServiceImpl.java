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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class JTATransactionServiceImpl implements TransactionService {

    protected final TechnicalLoggerService logger;

    private final TransactionManager txManager;

    private List<BonitaTransactionSynchronization> synchronizations;

    private final EventService eventService;

    public JTATransactionServiceImpl(final TechnicalLoggerService logger, final BonitaTransactionManagerLookup txManagerLookup, final EventService eventService) {
        this(logger, txManagerLookup.getTransactionManager(), eventService);
    }

    public JTATransactionServiceImpl(final TechnicalLoggerService logger, final TransactionManager txManager, final EventService eventService) {
        this.logger = logger;
        this.eventService = eventService;
        if (txManager == null) {
            throw new IllegalArgumentException("The parameter txManager can't be null.");
        }
        this.txManager = txManager;
    }

    @Override
    public void begin() throws STransactionCreationException {
        try {
            if (txManager.getStatus() == Status.STATUS_NO_TRANSACTION) {
                boolean transactionStarted = false;
                boolean mustRollback = false;
                try {
                    txManager.begin();
                    transactionStarted = true;
                    final Transaction tx = txManager.getTransaction();
                    if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                        logger.log(getClass(), TechnicalLogSeverity.DEBUG,
                                "Beginning transaction in thread " + Thread.currentThread().getId() + " " + tx.toString());
                    }

                    // Reset the synchronizations each time we begin a new transaction
                    if (synchronizations != null) {
                        synchronizations.clear();
                    }
                    synchronizations = new ArrayList<BonitaTransactionSynchronization>();
                    if (eventService.hasHandlers(TRANSACTION_ACTIVE_EVT, null)) {
                        final SEvent tr_active = eventService.getEventBuilder().createNewInstance(TRANSACTION_ACTIVE_EVT).done();
                        eventService.fireEvent(tr_active);
                    }
                } catch (final NotSupportedException e) {
                    // Should never happen as we do not want to support nested transaction
                    throw new STransactionCreationException(e);
                } catch (final Throwable t) {
                    mustRollback = true;
                    throw new STransactionCreationException(t);
                } finally {
                    if (transactionStarted && mustRollback) {
                        txManager.rollback();
                    }
                }
            } else {
                throw new STransactionCreationException("We do not support nested transaction.");
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
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Completing  transaction in thread " + Thread.currentThread().getId() + " " + tx.toString());
            }

            final int status = txManager.getStatus();

            if (status == Status.STATUS_MARKED_ROLLBACK) {
                try {
                    txManager.rollback();
                } catch (final IllegalStateException e) {
                    throw new STransactionRollbackException("", e);
                } catch (final SecurityException e) {
                    throw new STransactionRollbackException("", e);
                } finally {
                    if (eventService.hasHandlers(TRANSACTION_ROLLEDBACK_EVT, null)) {
                        // trigger the right event
                        final SEvent tr_rolledback = eventService.getEventBuilder().createNewInstance(TRANSACTION_ROLLEDBACK_EVT).done();
                        eventService.fireEvent(tr_rolledback);
                    }
                }
            } else {
                String eventName = TRANSACTION_ROLLEDBACK_EVT;
                try {
                    txManager.commit();
                    eventName = TRANSACTION_COMMITED_EVT;
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
                } finally {
                    if (eventService.hasHandlers(eventName, null)) {
                        // trigger the right event
                        final SEvent tr_commited = eventService.getEventBuilder().createNewInstance(eventName).done();
                        eventService.fireEvent(tr_commited);
                    }
                }
            }
        } catch (final SystemException e) {
            throw new STransactionCommitException("", e);
        } catch (final FireEventException e) {
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
            synchronizations.add(txSync);
        } catch (final IllegalStateException e) {
            throw new STransactionNotFoundException(e.getMessage());
        } catch (final RollbackException e) {
            throw new STransactionNotFoundException(e.getMessage());
        } catch (final SystemException e) {
            throw new STransactionNotFoundException(e.getMessage());
        }
    }

    @Override
    public List<BonitaTransactionSynchronization> getBonitaSynchronizations() {
        return synchronizations;
    }

    @Override
    public <T> T executeInTransaction(Callable<T> callable) throws Exception {
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

}
