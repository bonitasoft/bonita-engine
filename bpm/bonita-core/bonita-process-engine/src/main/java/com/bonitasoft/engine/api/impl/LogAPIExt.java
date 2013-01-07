/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.api.impl.PageIndexCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SIndexedLogBuilder;
import org.bonitasoft.engine.search.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.impl.transaction.GetLogInstance;
import com.bonitasoft.engine.api.impl.transaction.GetLogsWithOrder;
import com.bonitasoft.engine.api.impl.transaction.GetNumberOfLogInstance;
import com.bonitasoft.engine.exception.LogNotFoundException;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogBuilder;
import com.bonitasoft.engine.log.LogCriterion;
import com.bonitasoft.engine.log.SeverityLevel;
import com.bonitasoft.engine.search.SearchLogs;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class LogAPIExt implements LogAPI {

    private static TenantServiceAccessor getTenantAccessor() throws InvalidSessionException {
        SessionAccessor sessionAccessor = null;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
        try {
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final TenantIdNotSetException e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public Log getLog(final long logId) throws InvalidSessionException, LogNotFoundException {
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final QueriableLoggerService loggerService = getTenantAccessor().getQueriableLoggerService();
        try {
            final TransactionContentWithResult<SQueriableLog> transactionContentWithResult = new GetLogInstance(logId, loggerService);
            transactionExecutor.execute(transactionContentWithResult);
            final SQueriableLog sLog = transactionContentWithResult.getResult();
            if (sLog == null) {
                throw new LogNotFoundException("log Not Found.");
            }
            final LogBuilder logBuilder = new LogBuilder().createNewInstance(sLog.getRawMessage(), sLog.getUserId(), new Date(sLog.getTimeStamp()));
            logBuilder.setLogId(sLog.getId());
            logBuilder.setActionType(sLog.getActionType());
            logBuilder.setActionScope(sLog.getActionScope());
            logBuilder.setCallerClassName(sLog.getCallerClassName());
            logBuilder.setCallerMethodName(sLog.getCallerMethodName());
            logBuilder.setSeverity(SeverityLevel.valueOf(sLog.getSeverity().name()));
            final Log log = logBuilder.done();
            return log;
        } catch (final SBonitaException e) {
            throw new LogNotFoundException("log Not Found.", e);
        }
    }

    @Override
    public int getNumberOfLogs() throws InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final QueriableLoggerService loggerService = tenantAccessor.getQueriableLoggerService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final TransactionContentWithResult<Integer> transactionContent = new GetNumberOfLogInstance("getNumberOfLogs", loggerService);

            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            return 0;
        }
    }

    @Override
    public List<Log> getLogs(final int pageIndex, final int numberPerPage, final LogCriterion pagingCriterion) throws PageOutOfRangeException,
            InvalidSessionException {
        final int totalNumber = getNumberOfLogs();
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final QueriableLoggerService loggerService = tenantAccessor.getQueriableLoggerService();
        final SIndexedLogBuilder queriableLogBuilder = tenantAccessor.getSQueriableLogModelBuilder().getQueriableLogBuilder();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {

            case CREATED_BY_ASC:
                field = queriableLogBuilder.getUserIdKey();
                order = OrderByType.ASC;
                break;
            case CREATED_BY_DESC:
                field = queriableLogBuilder.getUserIdKey();
                order = OrderByType.DESC;
                break;
            case CREATION_DATE_ASC:
                field = queriableLogBuilder.getTimeStampKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
                field = queriableLogBuilder.getTimeStampKey();
                order = OrderByType.DESC;
                break;
            case SEVERITY_LEVEL_ASC:
                field = queriableLogBuilder.getSeverityKey();
                order = OrderByType.ASC;
                break;
            case SEVERITY_LEVEL_DESC:
                field = queriableLogBuilder.getSeverityKey();
                order = OrderByType.DESC;
                break;
            case DEFAULT:
                field = queriableLogBuilder.getTimeStampKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final String fieldContent = field;
            final OrderByType orderContent = order;
            final TransactionContentWithResult<List<SQueriableLog>> transactionContent = new GetLogsWithOrder(numberPerPage, orderContent, loggerService,
                    fieldContent, pageIndex);
            transactionExecutor.execute(transactionContent);
            return getLogsFromSLogs(transactionContent.getResult());
        } catch (final SBonitaException e) {
            throw new PageOutOfRangeException(e);
        }
    }

    @Override
    public SearchResult<Log> searchLogs(final SearchOptions searchOptions) throws InvalidSessionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final QueriableLoggerService loggerService = tenantAccessor.getQueriableLoggerService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        final SearchLogs searchLogs = new SearchLogs(loggerService, searchEntitiesDescriptor.getLogDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchLogs);
            return searchLogs.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    private List<Log> getLogsFromSLogs(final List<SQueriableLog> sLogs) {
        final List<Log> logs = new ArrayList<Log>();
        if (sLogs != null) {
            for (final SQueriableLog sLog : sLogs) {
                final LogBuilder logBuilder = new LogBuilder().createNewInstance(sLog.getRawMessage(), sLog.getUserId(), new Date(sLog.getTimeStamp()));
                logBuilder.setLogId(sLog.getId());
                logBuilder.setActionType(sLog.getActionType());
                logBuilder.setActionScope(sLog.getActionScope());
                logBuilder.setCallerClassName(sLog.getCallerClassName());
                logBuilder.setCallerMethodName(sLog.getCallerMethodName());
                logBuilder.setSeverity(SeverityLevel.valueOf(sLog.getSeverity().name()));
                final Log log = logBuilder.done();
                logs.add(log);
            }
        }
        return Collections.unmodifiableList(logs);
    }

}
