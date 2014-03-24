package org.bonitasoft.engine.transaction;

import java.util.LinkedList;
import java.util.List;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public class MyTransactionManager implements TransactionManager {

    private Transaction transaction;

    private final Transaction mockTransaction;

    public MyTransactionManager(final Transaction mockTransaction) {
        this.mockTransaction = mockTransaction;
    }

    @Override
    public void begin() throws SystemException {
        if (transaction == null) {
            transaction = this.mockTransaction != null ? mockTransaction : new MyTransaction();
        } else {
            throw new SystemException("A transaction has already begun.");
        }
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException,
            SystemException {
        transaction.commit();
        transaction = null;
    }

    @Override
    public int getStatus() throws SystemException {
        return transaction != null ? transaction.getStatus() : Status.STATUS_NO_TRANSACTION;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public void resume(final Transaction tobj) throws IllegalStateException {
        transaction = tobj;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        transaction.rollback();
        transaction = null;
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        transaction.setRollbackOnly();
    }

    @SuppressWarnings("unused")
    @Override
    public void setTransactionTimeout(final int seconds) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Transaction suspend() {
        Transaction result = transaction;
        transaction = null;
        return result;
    }

    public static class MyTransaction implements Transaction {

        private final List<Synchronization> synchros = new LinkedList<Synchronization>();

        private int status = Status.STATUS_ACTIVE;

        @Override
        public void commit() throws SecurityException, IllegalStateException, SystemException {
            if (status == Status.STATUS_ACTIVE) {
                status = Status.STATUS_COMMITTING;

                for (Synchronization synchro : synchros) {
                    synchro.beforeCompletion();
                }

                // Commit
                try {
                    status = internalCommit();
                } finally {
                    for (Synchronization synchro : synchros) {
                        synchro.afterCompletion(Status.STATUS_COMMITTED);
                    }
                }
            } else {
                throw new RuntimeException("Can't commit since the transaction is not yet started.");
            }
        }

        // Extension point during the commit phase, between the synchros executions.
        @SuppressWarnings("unused")
        int internalCommit() throws SystemException {
            return Status.STATUS_COMMITTED;
        }

        @SuppressWarnings("unused")
        @Override
        public boolean delistResource(final XAResource xaRes, final int flag) throws IllegalStateException {
            return false;
        }

        @SuppressWarnings("unused")
        @Override
        public boolean enlistResource(final XAResource xaRes) throws IllegalStateException {
            return false;
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public void registerSynchronization(final Synchronization sync) throws IllegalStateException {
            synchros.add(sync);
        }

        @Override
        public void rollback() throws IllegalStateException {
            if (status == Status.STATUS_MARKED_ROLLBACK) {
                status = Status.STATUS_ROLLING_BACK;

                for (Synchronization synchro : synchros) {
                    synchro.beforeCompletion();
                }

                // Rollback
                try {
                    status = internalRollback();
                } finally {
                    for (Synchronization synchro : synchros) {
                        synchro.afterCompletion(Status.STATUS_ROLLEDBACK);
                    }
                }

            }
        }

        // Extension point during the rollback phase, between the synchros executions.
        int internalRollback() {
            return Status.STATUS_ROLLEDBACK;
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException {
            status = Status.STATUS_MARKED_ROLLBACK;
        }
    }

}
