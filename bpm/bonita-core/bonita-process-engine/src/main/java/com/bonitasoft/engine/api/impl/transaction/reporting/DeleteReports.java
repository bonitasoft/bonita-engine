/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.reporting;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;

import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class DeleteReports implements TransactionContent {

    private final TenantServiceAccessor serviceAccessor;

    private final List<Long> reportIds;

    public DeleteReports(final TenantServiceAccessor serviceAccessor, final List<Long> reportIds) {
        super();
        this.serviceAccessor = serviceAccessor;
        this.reportIds = reportIds;
    }

    @Override
    public void execute() throws SBonitaException {
        final ReportingService reportingService = serviceAccessor.getReportingService();
        for (final long reportId : reportIds) {
            reportingService.deleteReport(reportId);
        }
    }

}
