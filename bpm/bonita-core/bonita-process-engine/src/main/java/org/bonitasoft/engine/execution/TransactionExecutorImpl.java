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
package org.bonitasoft.engine.execution;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class TransactionExecutorImpl implements TransactionExecutor {

    private final TransactionService transactionService;

    public TransactionExecutorImpl(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final TransactionContent transactionContent) throws SBonitaException {
        execute(Arrays.asList(transactionContent));
    }

    @Override
    public void execute(final TransactionContent... transactionContents) throws SBonitaException {
        execute(Arrays.asList(transactionContents));
    }

    @Override
    public void execute(final List<TransactionContent> transactionContents) throws SBonitaException {
        final boolean txOpened = openTransaction();
        try {
            for (final TransactionContent transactionContent : transactionContents) {
                transactionContent.execute();
            }
        } catch (final SBonitaException sbe) {
            setTransactionRollback();
            throw sbe;
        } finally {
            completeTransaction(txOpened);
        }
    }

    @Override
    public boolean isTransactionActive() {
        try {
            return transactionService.isTransactionActive();
        } catch (STransactionException e) {
            return false;
        }
    }

    @Override
    public boolean openTransaction() throws STransactionException {
        if (isTransactionActive()) {
            return false;
        } else {
            try {
                transactionService.begin();
                return true;
            } catch (final STransactionCreationException e) {
                throw new STransactionException("Unable to open transaction, transaction state is " + transactionService.getState(), e);
            }
        }
    }

    @Override
    public void completeTransaction(final boolean txOpened) throws STransactionException {
        if (txOpened) {
            transactionService.complete();
        }
    }

    @Override
    public void setTransactionRollback() throws STransactionException {
        transactionService.setRollbackOnly();
    }

}
