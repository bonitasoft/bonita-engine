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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * A work that wrap an other work in a transaction
 * 
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Charles Souillard
 * @author Celine Souchet
 */
public class TxBonitaWork extends WrappingBonitaWork {

    public TxBonitaWork(final BonitaWork wrappedWork) {
        super(wrappedWork);
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        return userTransactionService.executeInTransaction(() -> getWrappedWork().work(context));
    }

}
