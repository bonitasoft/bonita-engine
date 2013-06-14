/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.sql.SQLException;
import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.reporting.GetReport;
import org.bonitasoft.engine.api.impl.transaction.reporting.GetReportContent;
import org.bonitasoft.engine.api.impl.transaction.reporting.SearchReports;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.reporting.ReportingService;
import org.bonitasoft.engine.core.reporting.SReport;
import org.bonitasoft.engine.core.reporting.SReportAlreadyExistsException;
import org.bonitasoft.engine.core.reporting.SReportNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
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

import com.bonitasoft.engine.api.ReportingAPI;
import com.bonitasoft.engine.api.impl.transaction.reporting.AddReport;
import com.bonitasoft.engine.api.impl.transaction.reporting.DeleteReport;
import com.bonitasoft.engine.api.impl.transaction.reporting.DeleteReports;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ReportingAPIExt implements ReportingAPI {

    @Override
    public Report createReport(final String name, final String description, final byte[] content) throws AlreadyExistsException, CreationException {

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final AddReport addReport = new AddReport(tenantAccessor, name, description, content);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(addReport);
            return ModelConvertor.toReport(addReport.getResult());
        } catch (final SReportAlreadyExistsException sraee) {
            throw new AlreadyExistsException(sraee);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void deleteReport(final long id) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DeleteReport deleteReport = new DeleteReport(tenantAccessor, id);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(deleteReport);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void deleteReports(final List<Long> reportIds) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DeleteReports deleteReports = new DeleteReports(tenantAccessor, reportIds);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(deleteReports);
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

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
