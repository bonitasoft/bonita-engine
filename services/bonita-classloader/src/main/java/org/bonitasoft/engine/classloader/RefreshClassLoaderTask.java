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
package org.bonitasoft.engine.classloader;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.service.InjectedService;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 */
public class RefreshClassLoaderTask implements Callable<Void>, Serializable {

    private final ClassLoaderIdentifier id;
    private ClassLoaderService classLoaderService;
    private TransactionService transactionService;

    public RefreshClassLoaderTask(ClassLoaderIdentifier id) {
        this.id = id;
    }

    @Override
    public Void call() throws Exception {
        transactionService.executeInTransaction(() -> {
            getClassLoaderService().refreshClassLoaderImmediately(id);
            return null;
        });
        return null;
    }

    public ClassLoaderService getClassLoaderService() {
        return classLoaderService;
    }

    @InjectedService
    public void setClassLoaderService(ClassLoaderService classLoaderService) {
        this.classLoaderService = classLoaderService;
    }

    @InjectedService
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
