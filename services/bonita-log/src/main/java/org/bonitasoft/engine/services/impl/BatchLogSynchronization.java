/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.engine.services.impl;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
class BatchLogSynchronization implements BonitaTransactionSynchronization {

    private final PersistenceService persistenceService;
    private QueriableLoggerImpl queriableLogger;
    private final List<SQueriableLog> logs = new ArrayList<>();

    public BatchLogSynchronization(PersistenceService persistenceService, QueriableLoggerImpl queriableLogger) {
        super();
        this.persistenceService = persistenceService;
        this.queriableLogger = queriableLogger;
    }

    @Override
    public void afterCompletion(final TransactionState transactionState) {
        queriableLogger.clearSynchronization();
    }

    @Override
    public void beforeCommit() {
        if (!logs.isEmpty()) {
            try {
                persistenceService.insertInBatch(logs);
                // this is mandatory (probably because we are in a synchronization)
                persistenceService.flushStatements();
            } catch (final SPersistenceException e) {
                throw new SBonitaRuntimeException(e);
            }
        }
    }

    public void addLog(final SQueriableLog sQueriableLog) {
        logs.add(sQueriableLog);
    }

    List<SQueriableLog> getLogs() {
        return logs;
    }
}
