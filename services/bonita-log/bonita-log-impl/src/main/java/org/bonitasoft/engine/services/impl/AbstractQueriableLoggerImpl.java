/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.log.recorder.QueriableLogSelectDescriptorBuilder;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogModelBuilder;
import org.bonitasoft.engine.services.IllegalIndexPositionException;
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

    private final SQueriableLogModelBuilder builder;

    private final QueriableLoggerStrategy loggerConfiguration;

    private final QueriableLogSessionProvider sessionProvider;

    private final PlatformService platformService;

    public AbstractQueriableLoggerImpl(final PersistenceService persistenceService, final SQueriableLogModelBuilder builder,
            final QueriableLoggerStrategy loggerStrategy, final QueriableLogSessionProvider sessionProvider, final PlatformService platformService) {
        NullCheckingUtil.checkArgsNotNull(persistenceService, builder, loggerStrategy, sessionProvider);
        this.persistenceService = persistenceService;
        this.builder = builder;
        loggerConfiguration = loggerStrategy;
        this.sessionProvider = sessionProvider;
        this.platformService = platformService;
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
    public long getNumberOfLogs(final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            return persistenceService.getNumberOfEntities(SQueriableLog.class, searchOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SQueriableLog> searchLogs(final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SQueriableLog.class, searchOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public void log(final String callerClassName, final String callerMethodName, final SQueriableLog... queriableLogs) {
        NullCheckingUtil.checkArgsNotNull((Object[]) queriableLogs);
        final List<SQueriableLog> loggableLogs = new ArrayList<SQueriableLog>();
        for (SQueriableLog log : queriableLogs) {
            if (isLoggable(log.getActionType(), log.getSeverity())) {
                log = getBuilder().getQueriableLogBuilder().fromInstance(log).callerClassName(callerClassName).callerMethodName(callerMethodName)
                        .userId(sessionProvider.getUserId()).clusterNode(sessionProvider.getClusterNode())
                        .productVersion(platformService.getSPlatformProperties().getPlatformVersion())
                        .done();

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
        return loggerConfiguration.isLoggable(actionType, severity);
    }

    @Override
    public List<SQueriableLog> getLogsFromLongIndex(final int pos, final long value, final int fromIndex, final int numberOfResults)
            throws IllegalIndexPositionException, SQueriableLogException {
        return getLogsFromLongIndex(pos, value, fromIndex, numberOfResults, null, null);
    }

    @Override
    public List<SQueriableLog> getLogsFromLongIndex(final int pos, final long value, final int fromIndex, final int numberOfResults, final String fieldName,
            final OrderByType orderByType) throws IllegalIndexPositionException, SQueriableLogException {
        NullCheckingUtil.checkArgsNotNull(value);
        if (fromIndex < 0 || numberOfResults < 0) {
            throw new IllegalArgumentException("fromIndex and maxSize must be greater than zero");
        }
        String indexName = null;
        switch (pos) {
            case 0:
                indexName = "numericIndex1";
                break;
            case 1:
                indexName = "numericIndex2";
                break;
            case 2:
                indexName = "numericIndex3";
                break;
            case 3:
                indexName = "numericIndex4";
                break;
            case 4:
                indexName = "numericIndex5";
                break;
        }

        if (indexName == null) {
            throw new IllegalIndexPositionException(pos + "is not a legal position for a LongIndex. It must be between 0 and 4");
        }
        try {
            if (fieldName == null || orderByType == null) {
                return persistenceService.selectList(QueriableLogSelectDescriptorBuilder.getLogsFromLongIndex(indexName, value, new QueryOptions(fromIndex,
                        numberOfResults)));
            } else {
                return persistenceService.selectList(QueriableLogSelectDescriptorBuilder.getLogsFromLongIndex(indexName, value, new QueryOptions(fromIndex,
                        numberOfResults, SQueriableLog.class, fieldName, orderByType)));
            }
        } catch (final SBonitaReadException e) {
            final StringBuilder stb = new StringBuilder("Error while reading logs from long indexes. Index position: ");
            stb.append(pos);
            stb.append(", value: ");
            stb.append(value);
            stb.append('.');
            throw new SQueriableLogException(stb.toString(), e);
        }
    }

    protected PersistenceService getPersitenceService() {
        return persistenceService;
    }

    protected SQueriableLogModelBuilder getBuilder() {
        return builder;
    }

    protected QueriableLoggerStrategy getQueriableLogConfiguration() {
        return loggerConfiguration;
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
