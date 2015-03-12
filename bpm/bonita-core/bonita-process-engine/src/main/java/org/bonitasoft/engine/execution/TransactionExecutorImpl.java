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
package org.bonitasoft.engine.execution;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @deprecated Use {@link org.bonitasoft.engine.transaction.TransactionService#executeInTransaction(Callable)} instead.
 */
@Deprecated
public class TransactionExecutorImpl implements TransactionExecutor {

    private final TransactionService transactionService;

    public TransactionExecutorImpl(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void execute(final TransactionContent transactionContent) throws SBonitaException {
        Callable<Void> txContentCallable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                transactionContent.execute();
                return null;
            }
        };

        try {
            transactionService.executeInTransaction(txContentCallable);
        } catch (SBonitaException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SBonitaRuntimeException(e);
        }
    }

}
