/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.api.LogAPI;
import org.bonitasoft.engine.businesslogger.model.SBusinessLog;
import org.bonitasoft.engine.businesslogger.model.builder.SIndexedLogBuilder;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.search.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.services.BusinessLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

import com.bonitasoft.engine.api.impl.transaction.GetLogInstance;
import com.bonitasoft.engine.api.impl.transaction.GetLogsWithOrder;
import com.bonitasoft.engine.api.impl.transaction.GetNumberOfLogInstance;
import com.bonitasoft.engine.exception.LogNotFoundException;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogBuilder;
import com.bonitasoft.engine.log.LogCriterion;
import com.bonitasoft.engine.log.SeverityLevel;
import com.bonitasoft.engine.search.SearchLogs;

/**
 * @author Bole Zhang
 */
public class LogAPIImpl implements LogAPI {

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
        final BusinessLoggerService loggerService = getTenantAccessor().getBusinessLoggerService();
        try {
            final TransactionContentWithResult<SBusinessLog> transactionContentWithResult = new GetLogInstance(logId, loggerService);
            transactionExecutor.execute(transactionContentWithResult);
            final SBusinessLog sLog = transactionContentWithResult.getResult();
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
        final BusinessLoggerService loggerService = tenantAccessor.getBusinessLoggerService();
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
        final BusinessLoggerService loggerService = tenantAccessor.getBusinessLoggerService();
        final SIndexedLogBuilder businessLogBuilder = tenantAccessor.getSBusinessLogModelBuilder().getBusinessLogBuilder();
        String field = null;
        OrderByType order = null;
        switch (pagingCriterion) {

            case CREATED_BY_ASC:
                field = businessLogBuilder.getUserIdKey();
                order = OrderByType.ASC;
                break;
            case CREATED_BY_DESC:
                field = businessLogBuilder.getUserIdKey();
                order = OrderByType.DESC;
                break;
            case CREATION_DATE_ASC:
                field = businessLogBuilder.getTimeStampKey();
                order = OrderByType.ASC;
                break;
            case CREATION_DATE_DESC:
                field = businessLogBuilder.getTimeStampKey();
                order = OrderByType.DESC;
                break;
            case SEVERITY_LEVEL_ASC:
                field = businessLogBuilder.getSeverityKey();
                order = OrderByType.ASC;
                break;
            case SEVERITY_LEVEL_DESC:
                field = businessLogBuilder.getSeverityKey();
                order = OrderByType.DESC;
                break;
            case DEFAULT:
                field = businessLogBuilder.getTimeStampKey();
                order = OrderByType.DESC;
                break;
        }
        try {
            final String fieldContent = field;
            final OrderByType orderContent = order;
            final TransactionContentWithResult<List<SBusinessLog>> transactionContent = new GetLogsWithOrder(numberPerPage, orderContent, loggerService,
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
        final BusinessLoggerService loggerService = tenantAccessor.getBusinessLoggerService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        final SearchLogs searchLogs = new SearchLogs(loggerService, searchEntitiesDescriptor.getLogDescriptor(), searchOptions);
        try {
            transactionExecutor.execute(searchLogs);
            return searchLogs.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    private List<Log> getLogsFromSLogs(final List<SBusinessLog> sLogs) {
        final List<Log> logs = new ArrayList<Log>();
        if (sLogs != null) {
            for (final SBusinessLog sLog : sLogs) {
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
