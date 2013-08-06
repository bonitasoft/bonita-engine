/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.queriablelogger.model.builder.SIndexedLogBuilder;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.services.SQueriableLogNotFoundException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.impl.transaction.GetLogInstance;
import com.bonitasoft.engine.api.impl.transaction.GetLogsWithOrder;
import com.bonitasoft.engine.api.impl.transaction.GetNumberOfLogInstances;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogCriterion;
import com.bonitasoft.engine.log.LogNotFoundException;
import com.bonitasoft.engine.search.SearchLogs;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class LogAPIExt implements LogAPI {

    private static TenantServiceAccessor getTenantAccessor() {
        SessionAccessor sessionAccessor = null;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        try {
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final TenantIdNotSetException e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public Log getLog(final long logId) throws LogNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final QueriableLoggerService loggerService = tenantAccessor.getQueriableLoggerService();
        try {
            final GetLogInstance getLogInstance = new GetLogInstance(logId, loggerService);
            getLogInstance.execute();
            return SPModelConvertor.toLog(getLogInstance.getResult());
        } catch (final SQueriableLogNotFoundException sqlnfe) {
            throw new LogNotFoundException(sqlnfe);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public int getNumberOfLogs() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final QueriableLoggerService loggerService = tenantAccessor.getQueriableLoggerService();
        try {
            final GetNumberOfLogInstances transactionContent = new GetNumberOfLogInstances(loggerService);
            transactionContent.execute();
            return transactionContent.getResult();
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public List<Log> getLogs(final int startIndex, final int maxResults, final LogCriterion criterion) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final QueriableLoggerService loggerService = tenantAccessor.getQueriableLoggerService();
        final SIndexedLogBuilder queriableLogBuilder = tenantAccessor.getSQueriableLogModelBuilder().getQueriableLogBuilder();
        String field = null;
        OrderByType order = null;
        switch (criterion) {
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
            final GetLogsWithOrder getLogs = new GetLogsWithOrder(loggerService, startIndex, maxResults, orderContent, fieldContent);
            getLogs.execute();
            return SPModelConvertor.toLogs(getLogs.getResult());
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public SearchResult<Log> searchLogs(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final QueriableLoggerService loggerService = tenantAccessor.getQueriableLoggerService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchLogs searchLogs = new SearchLogs(loggerService, searchEntitiesDescriptor.getLogDescriptor(), searchOptions);
        try {
            searchLogs.execute();
            return searchLogs.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

}
