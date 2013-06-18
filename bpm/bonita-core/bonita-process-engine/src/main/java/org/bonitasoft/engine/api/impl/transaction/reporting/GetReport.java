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
package org.bonitasoft.engine.api.impl.transaction.reporting;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.reporting.ReportingService;
import org.bonitasoft.engine.core.reporting.SReport;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class GetReport implements TransactionContentWithResult<SReport> {

    private SReport report;

    private final TenantServiceAccessor accessor;

    private final long reportId;

    public GetReport(final TenantServiceAccessor accessor, final long reportId) {
        super();
        this.accessor = accessor;
        this.reportId = reportId;
    }

    @Override
    public void execute() throws SBonitaException {
        final ReportingService reportingService = accessor.getReportingService();
        report = reportingService.getReport(reportId);
    }

    @Override
    public SReport getResult() {
        return report;
    }

}
