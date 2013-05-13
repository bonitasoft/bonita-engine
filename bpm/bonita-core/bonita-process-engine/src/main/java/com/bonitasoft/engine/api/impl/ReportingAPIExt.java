/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.api.impl.ReportingAPIImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.reporting.SReportAlreadyExistsException;
import org.bonitasoft.engine.core.reporting.SReportNotFoundException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.reporting.Report;
import org.bonitasoft.engine.reporting.ReportNotFoundException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

import com.bonitasoft.engine.api.ReportingAPI;
import com.bonitasoft.engine.api.impl.transaction.reporting.AddReport;
import com.bonitasoft.engine.api.impl.transaction.reporting.DeleteReport;
import com.bonitasoft.engine.api.impl.transaction.reporting.DeleteReports;
import com.bonitasoft.engine.reporting.ReportAlreadyExistsException;
import com.bonitasoft.engine.reporting.ReportDeletionException;

/**
 * @author Matthieu Chaffotte
 */
public class ReportingAPIExt extends ReportingAPIImpl implements ReportingAPI {

    @Override
    public Report addReport(final String name, final String description, final byte[] content) throws ReportAlreadyExistsException, CreationException {

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final AddReport addReport = new AddReport(tenantAccessor, name, description, content);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(addReport);
            return ModelConvertor.toReport(addReport.getResult());
        } catch (final SReportAlreadyExistsException sraee) {
            throw new ReportAlreadyExistsException(sraee);
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void deleteReport(final long reportId) throws ReportNotFoundException, ReportDeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DeleteReport deleteReport = new DeleteReport(tenantAccessor, reportId);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(deleteReport);
        } catch (final SReportNotFoundException srnfe) {
            throw new ReportNotFoundException(srnfe);
        } catch (final SBonitaException sbe) {
            throw new ReportDeletionException(sbe);
        }
    }

    @Override
    public void deleteReports(final List<Long> reportIds) throws ReportNotFoundException, ReportDeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DeleteReports deleteReports = new DeleteReports(tenantAccessor, reportIds);
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            transactionExecutor.execute(deleteReports);
        } catch (final SReportNotFoundException srnfe) {
            throw new ReportNotFoundException(srnfe);
        } catch (final SBonitaException sbe) {
            throw new ReportDeletionException(sbe);
        }
    }

}
