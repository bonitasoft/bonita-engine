/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.reports;

import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.manager.Features;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Vincent Elcrin
 * Date: 02/12/13
 * Time: 16:37
 */
public class DefaultReportList {

    private List<DefaultReport> reports = Arrays.asList(
            new DefaultReport("case_avg_time"),
            new DefaultReport("case_list"),
            new DefaultReport("task_list"),
            new ProtectedReport("case_history", Features.TRACEABILITY, LicenseChecker.getInstance())
    );

    private TechnicalLoggerService logger;
    private String reportFolder;

    public DefaultReportList(TechnicalLoggerService logger, String reportFolder) {
        this.logger = logger;
        this.reportFolder = reportFolder;
    }

    public void deploy(ReportDeployer deployer) throws Exception {
        for(DefaultReport report: reports) {
            try {
                report.deploy(reportFolder, deployer);
            } catch (IllegalStateException e) {
                log("Valid license for " + report + " missing.", e);
            }
        }
    }

    private void log(String message, Throwable e) {
        if(logger.isLoggable(DefaultReportList.class, TechnicalLogSeverity.DEBUG)) {
            logger.log(DefaultReportList.class, TechnicalLogSeverity.DEBUG, message, e);
        }
    }
}
