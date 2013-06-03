/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.sql.SQLException;

import org.bonitasoft.engine.api.ReportingAPI;
import org.bonitasoft.engine.api.impl.transaction.reporting.GetReport;
import org.bonitasoft.engine.api.impl.transaction.reporting.GetReportContent;
import org.bonitasoft.engine.api.impl.transaction.reporting.SearchReports;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.reporting.ReportingService;
import org.bonitasoft.engine.core.reporting.SReport;
import org.bonitasoft.engine.core.reporting.SReportNotFoundException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.reporting.Report;
import org.bonitasoft.engine.reporting.ReportNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ReportingAPIImpl implements ReportingAPI {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public String selectList(final String selectQuery) throws ExecutionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ReportingService reportingService = tenantAccessor.getReportingService();
        try {
            return reportingService.selectList(selectQuery);
        } catch (final SQLException sqle) {
            throw new ExecutionException(sqle);
        }
    }

    @Override
    public Report getReport(final long reportId) throws ReportNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final GetReport getReport = new GetReport(tenantAccessor, reportId);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(getReport);
            final SReport report = getReport.getResult();
            return ModelConvertor.toReport(report);
        } catch (final SReportNotFoundException srnfe) {
            throw new ReportNotFoundException(srnfe);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public byte[] getReportContent(final long reportId) throws ReportNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final GetReportContent getReport = new GetReportContent(tenantAccessor, reportId);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(getReport);
            return getReport.getResult();
        } catch (final SReportNotFoundException srnfe) {
            throw new ReportNotFoundException(srnfe);
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    @Override
    public SearchResult<Report> searchReports(final SearchOptions options) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ReportingService reportingService = tenantAccessor.getReportingService();
        final SearchReports searchReports = new SearchReports(reportingService, searchEntitiesDescriptor.getReportDescriptor(reportingService
                .getReportBuilder()), options);
        try {
            transactionExecutor.execute(searchReports);
            return searchReports.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

}
