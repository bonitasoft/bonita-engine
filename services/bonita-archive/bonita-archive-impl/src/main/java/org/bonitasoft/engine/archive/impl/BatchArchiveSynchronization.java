/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.archive.impl;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * Transaction synchronization to insert archives in batch mode at the end of the transaction.
 * 
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class BatchArchiveSynchronization implements BonitaTransactionSynchronization {

    private final PersistenceService persistenceService;

    private final BatchArchiveCallable batchArchiveCallable;

    public BatchArchiveSynchronization(final PersistenceService persistenceService, final BatchArchiveCallable batchArchiveCallable) {
        super();
        this.persistenceService = persistenceService;
        this.batchArchiveCallable = batchArchiveCallable;
    }

    @SuppressWarnings("unused")
    @Override
    public void afterCompletion(final TransactionState status) {
        // NOTHING
    }

    @Override
    public void beforeCommit() {
        if (this.batchArchiveCallable.hasObjects()) {
            try {
                this.batchArchiveCallable.call();
                this.persistenceService.flushStatements();
            } catch (final Exception e) {
                throw new SBonitaRuntimeException(e);
            }
        }
    }

}
