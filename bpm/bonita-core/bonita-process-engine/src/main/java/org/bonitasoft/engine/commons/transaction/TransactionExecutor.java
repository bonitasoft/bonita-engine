/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.commons.transaction;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.transaction.STransactionException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public interface TransactionExecutor {

    void execute(TransactionContent transactionContent) throws SBonitaException;

    void execute(List<TransactionContent> transactionContents) throws SBonitaException;

    void execute(TransactionContent... transactionContents) throws SBonitaException;

    /**
     * @return true if the transaction has been opened, false it it was already active and has thus not been opened.
     * @throws STransactionException
     *             if an error occurs when trying to really open the transaction.
     */
    boolean openTransaction() throws STransactionException;

    void completeTransaction(boolean txOpened) throws STransactionException;

    void setTransactionRollback() throws STransactionException;

    /**
     * @return true if a transaction is currently active
     */
    boolean isTransactionActive();

}
