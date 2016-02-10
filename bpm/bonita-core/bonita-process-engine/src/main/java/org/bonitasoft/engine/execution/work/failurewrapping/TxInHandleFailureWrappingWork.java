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
package org.bonitasoft.engine.execution.work.failurewrapping;

import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.execution.work.WrappingBonitaWork;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * This work manages the transaction in its handleFailure method.
 * Don't use this work with {@link org.bonitasoft.engine.execution.work.TxBonitaWork}
 * 
 * @author Celine Souchet
 * 
 */
public abstract class TxInHandleFailureWrappingWork extends WrappingBonitaWork {

    private static final long serialVersionUID = -731662535816176640L;

    public TxInHandleFailureWrappingWork(final BonitaWork work) {
        super(work);
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        getWrappedWork().work(context);
    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) throws Exception {
        // Enrich the exception before log it.
        if (e instanceof SBonitaException) {
            final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
            final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
            transactionService.executeInTransaction(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    setExceptionContext((SBonitaException) e, context);

                    return null;
                }
            });
        }
        getWrappedWork().handleFailure(e, context);
    }

    protected abstract void setExceptionContext(final SBonitaException sBonitaException, final Map<String, Object> context) throws SBonitaException;
}
