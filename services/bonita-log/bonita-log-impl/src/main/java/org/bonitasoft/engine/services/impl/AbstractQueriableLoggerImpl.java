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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.NullCheckingUtil;
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
import org.bonitasoft.engine.services.SQueriableLogException;
import org.bonitasoft.engine.services.SQueriableLogNotFoundException;

/**
 * @author Elias Ricken de Medeiros
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class AbstractQueriableLoggerImpl implements QueriableLoggerService {

    private final PersistenceService persistenceService;

    private final QueriableLoggerStrategy loggerStrategy;

    protected final TechnicalLoggerService logger;

    private final QueriableLogUpdater logUpdater;

    public AbstractQueriableLoggerImpl(final PersistenceService persistenceService,
            final QueriableLoggerStrategy loggerStrategy, final QueriableLogSessionProvider sessionProvider, final PlatformService platformService,
            final TechnicalLoggerService logger) {
        this.logger = logger;
        NullCheckingUtil.checkArgsNotNull(persistenceService, loggerStrategy, sessionProvider);
        this.persistenceService = persistenceService;
        this.loggerStrategy = loggerStrategy;
        logUpdater = new QueriableLogUpdater(sessionProvider, platformService, logger);
    }

    @Override
    public int getNumberOfLogs() throws SQueriableLogException {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        try {
            final Long read = persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfLogs", emptyMap, SQueriableLog.class, Long.class));
            return read.intValue();
        } catch (final SBonitaReadException e) {
            throw handleError("can't get the number of log", e);
        }
    }

    @Override
    public List<SQueriableLog> getLogs(final int startIndex, final int maxResults, final String field, final OrderByType order) throws SQueriableLogException {
        List<SQueriableLog> logs;
        final QueryOptions queryOptions;
        queryOptions = new QueryOptions(startIndex, maxResults, SQueriableLog.class, field, order);
        try {
            logs = persistenceService.selectList(new SelectListDescriptor<SQueriableLog>("getLogs", null, SQueriableLog.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw handleError("can't get logs", e);
        }
        return logs;
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
        NullCheckingUtil.checkArgsNotNull((Object[]) queriableLogs);
        final List<SQueriableLog> loggableLogs = new ArrayList<SQueriableLog>();
        for (SQueriableLog log : queriableLogs) {
            if (isLoggable(log.getActionType(), log.getSeverity())) {
                log = logUpdater.buildFinalLog(callerClassName, callerMethodName, log);
                loggableLogs.add(log);
            }
        }

        if (loggableLogs.size() > 0) { // there is logs in a loggable level
            log(loggableLogs);
        }
    }

    protected abstract void log(final List<SQueriableLog> loggableLogs);

    @Override
    public boolean isLoggable(final String actionType, final SQueriableLogSeverity severity) {
        NullCheckingUtil.checkArgsNotNull(actionType, severity);
        return loggerStrategy.isLoggable(actionType, severity);
    }

    protected PersistenceService getPersitenceService() {
        return persistenceService;
    }

    protected QueriableLoggerStrategy getQueriableLogConfiguration() {
        return loggerStrategy;
    }

    @Override
    public SQueriableLog getLog(final long logId) throws SQueriableLogNotFoundException, SQueriableLogException {
        try {
            final SQueriableLog selectOne = persistenceService.selectById(new SelectByIdDescriptor<SQueriableLog>("getQueriableLogById", SQueriableLog.class,
                    logId));
            if (selectOne == null) {
                throw new SQueriableLogNotFoundException(logId);
            }
            return selectOne;
        } catch (final SBonitaReadException sbre) {
            throw new SQueriableLogException(sbre);
        }
    }

    private SQueriableLogException handleError(final String message, final Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        return new SQueriableLogException(message, e);
    }
}
