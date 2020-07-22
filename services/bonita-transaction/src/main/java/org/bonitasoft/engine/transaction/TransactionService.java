/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

/**
 * @author Matthieu Chaffotte
 * @author Laurent Vaills
 */
public interface TransactionService extends UserTransactionService {

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
     * @throws STransactionRollbackException
     */
    void complete() throws STransactionCommitException, STransactionRollbackException;

    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @throws IllegalStateException Thrown if the current thread is
     *         not associated with a transaction.
     * @throws STransactionException Thrown if the transaction manager
     *         encounters an unexpected error condition.
     */
    void setRollbackOnly() throws STransactionException;

    boolean isRollbackOnly() throws STransactionException;

    /**
     * Get the number of active transactions (i.e. transactions that opened but not yet completed or rolledback).
     * A transaction that was just mark as "rollbackOnly" is considered as an active one.
     *
     * @return the number of active transactions
     */
    long getNumberOfActiveTransactions();

}
