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
package org.bonitasoft.engine.work;

import java.util.Collection;
import java.util.HashSet;

import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

public abstract class AbstractWorkSynchronization implements BonitaTransactionSynchronization {

    private final Collection<BonitaWork> works;

    protected final BonitaExecutorService executorService;

    protected final WorkService workService;

    private long tenantId;

    public AbstractWorkSynchronization(final BonitaExecutorService executorService, final SessionAccessor sessionAccessor, final WorkService workService) {
        super();
        this.executorService = executorService;
        works = new HashSet<BonitaWork>();
        try {
            // Instead of doing this which is not so clear using sessionAccessor, we should add the tenantId as a parameter of the class
            tenantId = sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException e) {
            // We are not in a tenant
            tenantId = -1L;
        }
        this.workService = workService;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void addWork(final BonitaWork work) {
        works.add(work);
    }

    @Override
    public void beforeCommit() {
        // NOTHING
    }

    @Override
    public void afterCompletion(final TransactionState transactionStatus) {
        if (TransactionState.COMMITTED == transactionStatus) {
            for (final BonitaWork work : works) {
                work.setTenantId(tenantId);
            }
            executeRunnables(works);
        }
        workService.removeSynchronization();
    }

    protected abstract void executeRunnables(Collection<BonitaWork> works);

}
