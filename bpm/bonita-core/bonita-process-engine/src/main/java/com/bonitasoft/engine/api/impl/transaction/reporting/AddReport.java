/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.reporting;

import java.io.IOException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.core.reporting.SReport;
import com.bonitasoft.engine.core.reporting.SReportBuilder;
import com.bonitasoft.engine.core.reporting.SReportCreationException;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
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
