package org.bonitasoft.engine.transaction;

import java.util.LinkedList;
import java.util.List;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public class MyTransactionManager implements TransactionManager {

    Transaction transaction;

    @Override
    public void begin() throws NotSupportedException, SystemException {
        transaction = new MyTransaction();
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        transaction.commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return transaction != null ?transaction.getStatus() :Status.STATUS_NO_TRANSACTION;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return transaction;
    }

    @Override
    public void resume(final Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
        transaction = tobj;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        transaction.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        transaction.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(final int seconds) throws SystemException {
        // TODO Auto-generated method stub
    }

    @Override
    public Transaction suspend() throws SystemException {
        Transaction result = transaction;
        transaction = null;
        return result;
    }

    public static class MyTransaction implements Transaction {

        private final List<Synchronization> synchros = new LinkedList<Synchronization>();
        private int status = Status.STATUS_ACTIVE;

        @Override
        public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
            System.out.println("=== COMMIT === " + synchros.size());
            if (status == Status.STATUS_ACTIVE) {
                status = Status.STATUS_COMMITTING;

                System.out.println("=== BEFORE COMPLETION === " + synchros.size());
                for(Synchronization synchro : synchros) {
                    synchro.beforeCompletion();
                }

                // Commit
                status = Status.STATUS_COMMITTED;

                for(Synchronization synchro : synchros) {
                    synchro.afterCompletion(Status.STATUS_COMMITTED);
                }
            } else {
                throw new RuntimeException("Can't commit since the transaction is not yet started.");
            }
        }

        @Override
        public boolean delistResource(final XAResource xaRes, final int flag) throws IllegalStateException, SystemException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean enlistResource(final XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getStatus() throws SystemException {
            return status;
        }

        @Override
        public void registerSynchronization(final Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
            System.out.println("=== REGISTER SYNCHRO ===");
            synchros.add(sync);
        }

        @Override
        public void rollback() throws IllegalStateException, SystemException {
            if (status == Status.STATUS_MARKED_ROLLBACK) {
                status = Status.STATUS_ROLLING_BACK;

                for(Synchronization synchro : synchros) {
                    synchro.beforeCompletion();
                }

                // Rollback
                status = Status.STATUS_COMMITTED;

                for(Synchronization synchro : synchros) {
                    synchro.afterCompletion(Status.STATUS_ROLLEDBACK);
                }
            }

        }

        @Override
        public void setRollbackOnly() throws IllegalStateException, SystemException {
            status = Status.STATUS_MARKED_ROLLBACK;
        }

    }

}
