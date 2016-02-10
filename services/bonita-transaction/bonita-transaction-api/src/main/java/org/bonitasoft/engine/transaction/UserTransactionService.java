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
