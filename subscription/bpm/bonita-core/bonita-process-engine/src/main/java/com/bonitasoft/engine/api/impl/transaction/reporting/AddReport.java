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
import com.bonitasoft.engine.core.reporting.SReport;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class AddReport implements TransactionContentWithResult<SReport> {

    private final byte[] content;

    private SReport report;

    private final ReportingService reportingService;

    public AddReport(final ReportingService reportingService, final SReport report, final byte[] content) {
        this.reportingService = reportingService;
        this.report = report;
        this.content = content;
    }

    @Override
    public void execute() throws SBonitaException {
        report = reportingService.addReport(report, content);
    }

    @Override
    public SReport getResult() {
        return report;
    }

}
