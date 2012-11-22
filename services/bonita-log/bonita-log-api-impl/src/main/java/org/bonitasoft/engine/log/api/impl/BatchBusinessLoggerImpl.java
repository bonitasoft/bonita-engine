/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.log.api.impl;

import java.util.List;

import org.bonitasoft.engine.businesslogger.model.SBusinessLog;
import org.bonitasoft.engine.businesslogger.model.builder.SBusinessLogModelBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.services.BusinessLogSessionProvider;
import org.bonitasoft.engine.services.BusinessLoggerServiceConfiguration;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.impl.AbstractBusinessLoggerImpl;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Baptiste Mesta
 */
public class BatchBusinessLoggerImpl extends AbstractBusinessLoggerImpl {

    private final ThreadLocal<BatchLogSynchronization> synchronizations = new ThreadLocal<BatchLogSynchronization>();

    private final TransactionService transactionService;

    private final PersistenceService persistenceService;

    private final TechnicalLoggerService logger;

    /**
     * @param persistenceService
     * @param builder
     * @param loggerConfiguration
     * @param sessionProvider
     */
    public BatchBusinessLoggerImpl(final PersistenceService persistenceService, final TransactionService transactionService,
            final SBusinessLogModelBuilder builder, final BusinessLoggerServiceConfiguration loggerConfiguration,
            final BusinessLogSessionProvider sessionProvider, final TechnicalLoggerService logger) {
        super(persistenceService, builder, loggerConfiguration, sessionProvider);
        this.persistenceService = persistenceService;
        this.transactionService = transactionService;
        this.logger = logger;
    }

    private synchronized BatchLogSynchronization getBatchLogSynchronization() throws STransactionNotFoundException {
        BatchLogSynchronization synchro = synchronizations.get();
        if (synchro == null) {
            synchro = new BatchLogSynchronization(persistenceService);
            transactionService.getTransaction().registerSynchronization(synchro);
        }
        return synchro;
    }

    @Override
    protected void log(final List<SBusinessLog> loggableLogs) {
        BatchLogSynchronization synchro;
        try {
            synchro = getBatchLogSynchronization();
            for (final SBusinessLog sBusinessLog : loggableLogs) {
                synchro.addLog(sBusinessLog);
            }
        } catch (final STransactionNotFoundException e) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "Unable to register synchronization to log business logs: transaction not found");
        }
    }

}
