/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.reporting;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class GetReportContent implements TransactionContentWithResult<byte[]> {

    private byte[] reportContent;

    private final TenantServiceAccessor accessor;

    private final long reportId;

    public GetReportContent(final TenantServiceAccessor accessor, final long reportId) {
        super();
        this.accessor = accessor;
        this.reportId = reportId;
    }

    @Override
    public void execute() throws SBonitaException {
        final ReportingService reportingService = accessor.getReportingService();
        reportContent = reportingService.getReportContent(reportId);
    }

    @Override
    public byte[] getResult() {
        return reportContent;
    }

}
