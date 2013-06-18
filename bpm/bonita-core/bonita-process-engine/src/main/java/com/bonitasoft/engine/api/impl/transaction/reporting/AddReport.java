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
package com.bonitasoft.engine.api.impl.transaction.reporting;

import java.io.IOException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.reporting.ReportingService;
import org.bonitasoft.engine.core.reporting.SReport;
import org.bonitasoft.engine.core.reporting.SReportBuilder;
import org.bonitasoft.engine.core.reporting.SReportCreationException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Matthieu Chaffotte
 */
public class AddReport implements TransactionContentWithResult<SReport> {

    private final TenantServiceAccessor tenantAccessor;

    private final String name;

    private final String description;

    private final byte[] content;

    private SReport report;

    public AddReport(final TenantServiceAccessor tenantAccessor, final String name, final String description, final byte[] content) {
        this.tenantAccessor = tenantAccessor;
        this.name = name;
        this.description = description;
        this.content = content;
    }

    @Override
    public void execute() throws SBonitaException {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = sessionAccessor.getSessionId();
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final long userId = platformServiceAccessor.getSessionService().getSession(sessionId).getUserId();
            final ReportingService reportingService = tenantAccessor.getReportingService();
            final SReportBuilder reportBuilder = reportingService.getReportBuilder();
            reportBuilder.createNewInstance(name, System.currentTimeMillis(), userId, false).description(description);
            report = reportingService.addReport(reportBuilder.done(), content);
        } catch (final BonitaHomeNotSetException bhnse) {
            throw new SReportCreationException(bhnse);
        } catch (final BonitaHomeConfigurationException bhce) {
            throw new SReportCreationException(bhce);
        } catch (final InstantiationException ie) {
            throw new SReportCreationException(ie);
        } catch (final IllegalAccessException iae) {
            throw new SReportCreationException(iae);
        } catch (final ClassNotFoundException cnfe) {
            throw new SReportCreationException(cnfe);
        } catch (final IOException ioe) {
            throw new SReportCreationException(ioe);
        }
    }

    @Override
    public SReport getResult() {
        return report;
    }

}
