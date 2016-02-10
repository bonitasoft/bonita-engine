/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.sql.SQLException;
import java.util.List;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.ReportingAPI;
import com.bonitasoft.engine.api.impl.transaction.reporting.AddReport;
import com.bonitasoft.engine.api.impl.transaction.reporting.DeleteReport;
import com.bonitasoft.engine.api.impl.transaction.reporting.DeleteReports;
import com.bonitasoft.engine.api.impl.transaction.reporting.GetReport;
import com.bonitasoft.engine.api.impl.transaction.reporting.GetReportContent;
import com.bonitasoft.engine.api.impl.transaction.reporting.SearchReports;
import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.core.reporting.SReport;
import com.bonitasoft.engine.core.reporting.SReportBuilder;
import com.bonitasoft.engine.core.reporting.SReportBuilderFactory;
import com.bonitasoft.engine.core.reporting.SReportNotFoundException;
import com.bonitasoft.engine.reporting.Report;
import com.bonitasoft.engine.reporting.ReportCreator;
import com.bonitasoft.engine.reporting.ReportNotFoundException;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ReportingAPIExt implements ReportingAPI {

    protected void checkReportAlreadyExists(final String name, final TenantServiceAccessor tenantAccessor) throws AlreadyExistsException {
        // Check if the problem is primary key duplication:
        try {
            final GetReport getReport = new GetReport(tenantAccessor, name);
            getReport.execute();
            if (getReport.getResult() != null) {
                throw new AlreadyExistsException("A report already exists with the name " + name);
            }
        } catch (SBonitaException e) {
            // ignore it
        }
    }

    @Override
    public Report createReport(final String name, final String description, final byte[] content) throws AlreadyExistsException, CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        ReportingService reportingService = tenantAccessor.getReportingService();
        final long userId = SessionInfos.getUserIdFromSession();
        final SReportBuilder reportBuilder = BuilderFactory.get(SReportBuilderFactory.class).createNewInstance(name, userId, false, description, null);
        SReport report = reportBuilder.done();
        checkReportAlreadyExists(name, tenantAccessor);
        final AddReport addReport = new AddReport(reportingService, report, content);
        try {
            addReport.execute();
            return SPModelConvertor.toReport(addReport.getResult());
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public Report createReport(final ReportCreator reportCreator, final byte[] content) throws AlreadyExistsException, CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final long userId = SessionInfos.getUserIdFromSession();
        ReportingService reportingService = tenantAccessor.getReportingService();
        final SReport sReport = SPModelConvertor.constructSReport(reportCreator, userId);
        final AddReport addReport = new AddReport(reportingService, sReport, content);
        checkReportAlreadyExists((String) reportCreator.getFields().get(ReportCreator.ReportField.NAME), tenantAccessor);
        try {
            addReport.execute();
            return SPModelConvertor.toReport(addReport.getResult());
        } catch (final SBonitaException sbe) {
            throw new CreationException(sbe);
        }
    }

    @Override
    public void deleteReport(final long id) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DeleteReport deleteReport = new DeleteReport(tenantAccessor, id);
        try {
            deleteReport.execute();
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    @Override
    public void deleteReports(final List<Long> reportIds) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DeleteReports deleteReports = new DeleteReports(tenantAccessor, reportIds);
        try {
            deleteReports.execute();
        } catch (final SBonitaException sbe) {
            throw new DeletionException(sbe);
        }
    }

    private static TenantServiceAccessor getTenantAccessor() {
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
        try {
            getReport.execute();
            final SReport report = getReport.getResult();
            return SPModelConvertor.toReport(report);
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
        try {
            getReport.execute();
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
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ReportingService reportingService = tenantAccessor.getReportingService();
        final SearchReports searchReports = new SearchReports(reportingService, searchEntitiesDescriptor.getSearchReportDescriptor(), options);
        try {
            searchReports.execute();
            return searchReports.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

}
