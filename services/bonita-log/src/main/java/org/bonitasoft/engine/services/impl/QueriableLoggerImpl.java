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
package org.bonitasoft.engine.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;
import org.bonitasoft.engine.services.SQueriableLogNotFoundException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Elias Ricken de Medeiros
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class QueriableLoggerImpl implements QueriableLoggerService {

    private final PersistenceService persistenceService;
    private final QueriableLoggerStrategy loggerStrategy;
    private TransactionService transactionService;
    protected final TechnicalLoggerService logger;
    private final QueriableLogUpdater logUpdater;
    private final ThreadLocal<BatchLogSynchronization> synchronizations = new ThreadLocal<>();


    public QueriableLoggerImpl(PersistenceService persistenceService,
                               TransactionService transactionService,
                               QueriableLoggerStrategy loggerStrategy,
                               QueriableLogSessionProvider sessionProvider,
                               PlatformService platformService,
                               TechnicalLoggerService logger) {
        this.transactionService = transactionService;
        this.logger = logger;
        this.persistenceService = persistenceService;
        this.loggerStrategy = loggerStrategy;
        logUpdater = new QueriableLogUpdater(sessionProvider, platformService, logger);
    }

    @Override
    public long getNumberOfLogs() throws SBonitaReadException {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return persistenceService.selectOne(new SelectOneDescriptor<>("getNumberOfLogs", emptyMap, SQueriableLog.class, Long.class));
    }

    @Override
    public List<SQueriableLog> getLogs(final int startIndex, final int maxResults, final String field, final OrderByType order) throws SBonitaReadException {
        return persistenceService.selectList(
                new SelectListDescriptor<SQueriableLog>("getLogs", null, SQueriableLog.class,
                        new QueryOptions(startIndex, maxResults, SQueriableLog.class, field, order)));
    }

    @Override
    public long getNumberOfLogs(final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SQueriableLog.class, searchOptions, null);
    }

    @Override
    public List<SQueriableLog> searchLogs(final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(SQueriableLog.class, searchOptions, null);
    }

    @Override
    public void log(final String callerClassName, final String callerMethodName, final SQueriableLog... queriableLogs) {
        if (queriableLogs.length == 0) {
            return;
        }
        BatchLogSynchronization synchronization = getBatchLogSynchronization();
        for (SQueriableLog log : queriableLogs) {
            if (isLoggable(log.getActionType(), log.getSeverity())) {
                log = logUpdater.buildFinalLog(callerClassName, callerMethodName, log);
                synchronization.addLog(log);
            }
        }
    }

    private synchronized BatchLogSynchronization getBatchLogSynchronization() {
        BatchLogSynchronization synchronization = synchronizations.get();
        if (synchronization == null) {
            synchronization = new BatchLogSynchronization(persistenceService, this);
            synchronizations.set(synchronization);
            registerSynchronization(synchronization);
        }
        return synchronization;
    }

    private void registerSynchronization(BatchLogSynchronization synchro) {
        try {
            transactionService.registerBonitaSynchronization(synchro);
        } catch (STransactionNotFoundException e) {
            throw new SBonitaRuntimeException(e);
        }
    }

    void clearSynchronization() {
        synchronizations.remove();
    }

    @Override
    public boolean isLoggable(final String actionType, final SQueriableLogSeverity severity) {
        return loggerStrategy.isLoggable(actionType, severity);
    }

    @Override
    public SQueriableLog getLog(final long logId) throws SQueriableLogNotFoundException, SBonitaReadException {
        final SQueriableLog selectOne = persistenceService.selectById(new SelectByIdDescriptor<>(SQueriableLog.class, logId));
        if (selectOne == null) {
            throw new SQueriableLogNotFoundException(logId);
        }
        return selectOne;
    }

}
