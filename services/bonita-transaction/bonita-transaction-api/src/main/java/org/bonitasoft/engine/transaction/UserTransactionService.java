package org.bonitasoft.engine.transaction;

import java.util.concurrent.Callable;

public interface UserTransactionService {
    /**
     * Execute the given callable inside a transaction.
     * @param callable
     * @return
     * @throws Exception
     */
    <T> T executeInTransaction(Callable<T> callable) throws Exception;

    /**
     * Register a synchronization object for the transaction currently
     * associated with the target object. The transaction manager invokes
     * the beforeCompletion method prior to starting the two-phase transaction
     * commit process. After the transaction is completed, the transaction
     * manager invokes the afterCompletion method.
     *
     * @param txSync
     *            The Synchronization object for the transaction associated
     *            with the target object.
     * @exception RollbackException
     *                Thrown to indicate that
     *                the transaction has been marked for rollback only.
     * @exception IllegalStateException
     *                Thrown if the transaction in the
     *                target object is in the prepared state or the transaction is
     *                inactive.
     * @exception STransactionNotFoundException
     *                Thrown if the transaction manager
     *                encounters an unexpected error condition.
     */
    void registerBonitaSynchronization(BonitaTransactionSynchronization txSync) throws STransactionNotFoundException;

    void registerBeforeCommitCallable(Callable<Void> callable) throws STransactionNotFoundException;

}
