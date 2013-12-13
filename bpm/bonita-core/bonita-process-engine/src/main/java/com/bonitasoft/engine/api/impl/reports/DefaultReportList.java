/*
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
