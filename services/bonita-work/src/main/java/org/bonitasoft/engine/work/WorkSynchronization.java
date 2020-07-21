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
package org.bonitasoft.engine.work;

import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkSynchronization implements BonitaTransactionSynchronization {

    private static final Logger LOG = LoggerFactory.getLogger(WorkSynchronization.class);

    private final WorkDescriptor work;

    private final WorkExecutorService workExecutorService;

    private long tenantId;

    WorkSynchronization(final WorkExecutorService workExecutorService, final SessionAccessor sessionAccessor,
            WorkDescriptor work) {
        try {
            // Instead of doing this which is not so clear using sessionAccessor, we should add the tenantId as a parameter of the class
            tenantId = sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException e) {
            // We are not in a tenant
            tenantId = -1L;
        }
        this.work = work;
        this.workExecutorService = workExecutorService;
    }

    WorkDescriptor getWork() {
        return work;
    }

    @Override
    public void beforeCommit() {
    }

    @Override
    public void afterCompletion(final TransactionState transactionStatus) {
        if (TransactionState.COMMITTED == transactionStatus) {
            work.setTenantId(tenantId);
            workExecutorService.execute(work);
        } else {
            LOG.debug("Transaction completion with state {} != COMMITTED. Not triggering the work: {}",
                    transactionStatus, work);
        }
    }

}
