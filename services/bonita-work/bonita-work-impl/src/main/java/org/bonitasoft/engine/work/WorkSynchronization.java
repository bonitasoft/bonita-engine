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

public class WorkSynchronization implements BonitaTransactionSynchronization {

    private final Collection<WorkDescriptor> works;

    private final WorkExecutorService workExecutorService;
    private final WorkServiceImpl workService;

    private long tenantId;

    WorkSynchronization(final WorkExecutorService workExecutorService, final SessionAccessor sessionAccessor,
                        WorkServiceImpl workService) {
        super();
        this.workService = workService;
        works = new HashSet<>();
        try {
            // Instead of doing this which is not so clear using sessionAccessor, we should add the tenantId as a parameter of the class
            tenantId = sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException e) {
            // We are not in a tenant
            tenantId = -1L;
        }
        this.workExecutorService = workExecutorService;
    }

    void addWork(final WorkDescriptor work) {
        works.add(work);
    }

    Collection<WorkDescriptor> getWorks() {
        return works;
    }

    @Override
    public void beforeCommit() {
    }

    @Override
    public void afterCompletion(final TransactionState transactionStatus) {
        if (TransactionState.COMMITTED == transactionStatus) {
            for (WorkDescriptor work : works) {
                work.setTenantId(tenantId);
                workExecutorService.execute(work);
            }
        }
        workService.removeSynchronization();
    }

}
