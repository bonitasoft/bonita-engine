/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.util.List;

import org.omg.CORBA.SystemException;

/**
 * @author Matthieu Chaffotte
 */
public interface TransactionService {

    // EVENTS
    String TRANSACTION_ACTIVE_EVT = "TRANSACTION_ACTIVE";

    String TRANSACTION_COMMITED_EVT = "TRANSACTION_COMMITED";

    String TRANSACTION_ROLLEDBACK_EVT = "TRANSACTION_ROLLEDBACK";

    /**
     * Create a new transaction and associate it with the current thread.
     * 
     * @throws STransactionCreationException
     */
    void begin() throws STransactionCreationException;

    /**
     * Complete the transaction : either commit or rollback.
     * 
     * @throws STransactionCommitException
     *             TODO
     * @throws STransactionRollbackException
     *             TODO
     */
    void complete() throws STransactionCommitException, STransactionRollbackException;

    /**
     * Obtain the status of the transaction associated with the current thread.
     * 
     * @return The transaction status. If no transaction is associated with
     *         the current thread, this method returns the Status.NoTransaction
     *         value.
     * @exception SystemException
     *                Thrown if the transaction manager
     *                encounters an unexpected error condition.
     */
    TransactionState getState() throws STransactionException;

    boolean isTransactionActive() throws STransactionException;

    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     * 
     * @exception IllegalStateException
     *                Thrown if the current thread is
     *                not associated with a transaction.
     * @exception SystemException
     *                Thrown if the transaction manager
     *                encounters an unexpected error condition.
     */
    void setRollbackOnly() throws STransactionException;

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
     * @exception SystemException
     *                Thrown if the transaction manager
     *                encounters an unexpected error condition.
     */
    void registerBonitaSynchronization(BonitaTransactionSynchronization txSync) throws STransactionNotFoundException;

    List<BonitaTransactionSynchronization> getBonitaSynchronizations();

    boolean isRollbackOnly() throws STransactionException;

}
