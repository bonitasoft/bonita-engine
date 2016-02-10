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
package org.bonitasoft.engine.execution.work;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.UserTransactionService;

public class TransactionServiceForTest implements UserTransactionService {

    public TransactionServiceForTest() {
    }

    @Override
    public void registerBonitaSynchronization(final BonitaTransactionSynchronization txSync) {
    }

    @Override
    public <T> T executeInTransaction(final Callable<T> callable) throws Exception {
        return callable.call();
    }

    @Override
    public void registerBeforeCommitCallable(final Callable<Void> callable) {
    }

}
