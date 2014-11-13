/*******************************************************************************
 * Copyright (C) 2009, 2013 - 2014 Bonitasoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

import com.bonitasoft.engine.core.reporting.processor.QueryPreProcessor;
import com.bonitasoft.engine.core.reporting.processor.Vendor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ReportingServiceImpl implements ReportingService, TenantLifecycleService {

    private static final CharSequence COMMA = ",";

    private static final CharSequence SEMICOLON = ";";

    private static final String NEW_LINE = "\n";

    private final DataSource dataSource;

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    private QueryPreProcessor queryPreProcessor;

    private boolean isTraceabilityActive;
    private DefaultReportImporter defaultProfileImporter;

    public ReportingServiceImpl(final DataSource dataSource, final ReadPersistenceService persistenceService, final QueryPreProcessor queryPreProcessor,
            final Recorder recorder, final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        this(dataSource, persistenceService, queryPreProcessor, recorder, eventService, logger, queriableLoggerService, Manager.getInstance(), null);
    }

    ReportingServiceImpl(final DataSource dataSource, final ReadPersistenceService persistenceService, final QueryPreProcessor queryPreProcessor,
            final Recorder recorder, final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService,
            Manager manager, DefaultReportImporter defaultProfileImporter) {
        this.dataSource = dataSource;
        this.persistenceService = persistenceService;
        this.queryPreProcessor = queryPreProcessor;
        this.eventService = eventService;
        this.recorder = recorder;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
        isTraceabilityActive = manager.isFeatureActive(Features.TRACEABILITY);
        if(defaultProfileImporter == null){
            this.defaultProfileImporter = new DefaultReportImporter(this, logger);
        }else{
            this.defaultProfileImporter = defaultProfileImporter;
        }

    }

    @Override
    public String selectList(final String selectQuery) throws SQLException {
        final String lowerSQ = selectQuery.toLowerCase();
        if (!lowerSQ.startsWith("select")) {
            throw new SQLException("The statement is not a SELECT query");
        }
        final Connection connection = dataSource.getConnection();
        String query = queryPreProcessor.preProcessFor(Vendor.fromDatabaseMetadata(connection.getMetaData()), selectQuery);

        try {
            final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                return executeQuery(query, statement);
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }

        } finally {
            connection.close();
        }
    }

    /** protected for mocking */
    protected String executeQuery(final String selectQuery, final Statement statement) throws SQLException {
        final ResultSet resultSet = statement.executeQuery(selectQuery);
        try {
            return parseResultSet(resultSet);
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private String parseResultSet(final ResultSet resultSet) throws SQLException {
        final StringBuilder builder = new StringBuilder();
        if (resultSet != null) {
            final int columns = appendColumnNames(resultSet, builder);
            appendValues(resultSet, builder, columns);
        }
        return builder.toString();
    }

    private void appendValues(final ResultSet resultSet, final StringBuilder builder, final int columns) throws SQLException {
        while (resultSet.next()) {
            for (int j = 1; j < columns; j++) {
                final Object value = resultSet.getObject(j);
                builder.append(protect(String.valueOf(value))).append(",");
            }
            final Object value = resultSet.getObject(columns);
            // Special treatment of last record (to avoid having extra comma at the end):
            builder.append(value).append(NEW_LINE);
        }
    }

    private int appendColumnNames(final ResultSet resultSet, final StringBuilder builder) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columns = metaData.getColumnCount();
        for (int i = 1; i < columns; i++) {
            final String columnName = metaData.getColumnLabel(i);
            // in order to use the same case for all database
            builder.append(columnName.toUpperCase()).append(",");
        }
        // Special treatment of last record (to avoid having extra comma at the end):
        final String columnName = metaData.getColumnLabel(columns);
        builder.append(columnName.toUpperCase()).append(NEW_LINE);
        return columns;
    }

    private String protect(final String value) {
        if (isDangerous(value)) {
            return "\"" + escape(value) + "\"";
        }
        return value;
    }

    private boolean isDangerous(final String value) {
        return value.contains(COMMA) || value.contains(SEMICOLON) || value.contains("\"");
    }

    private String escape(final String value) {
        if (value.contains("\"")) {
            return value.replaceAll("\"", "\"\"");
        }
        return value;
    }

    @Override
    public SReport addReport(final SReport report, final byte[] content) throws SReportCreationException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "addReport"));
        }
        final String message = "Adding a new report with name " + report.getName();
        final SReportLogBuilder logBuilder = getReportLog(ActionType.CREATED, message);
        try {
            SSaveReportWithContent reportContent = new SSaveReportWithContentImpl(report, content);
            final InsertRecord insertContentRecord = new InsertRecord(reportContent);
            final SInsertEvent insertContentEvent = getInsertEvent(insertContentRecord, REPORT);
            recorder.recordInsert(insertContentRecord, insertContentEvent);
            report.setId(reportContent.getId());

            initiateLogBuilder(reportContent.getId(), SQueriableLog.STATUS_OK, logBuilder, "addReport");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "addReport"));
            }
            return report;
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "addReport", re));
            }
            initiateLogBuilder(report.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addReport");
            throw new SReportCreationException(re);
        }
    }

    private SReportLogBuilder getReportLog(final ActionType actionType, final String message) {
        final SReportLogBuilder logBuilder = new SReportLogBuilderImpl();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public SReport getReport(final long reportId) throws SBonitaReadException, SReportNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getReport"));
        }
        try {
            final SReport report = persistenceService.selectById(new SelectByIdDescriptor<SReport>("getReportById", SReport.class, reportId));
            if (report == null) {
                throw new SReportNotFoundException(reportId);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getReport"));
            }
            return report;
        } catch (final SBonitaReadException sbe) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getReport", sbe));
            }
            throw sbe;
        }
    }

    @Override
    public SReport getReportByName(final String reportName) throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getReportByName"));
        }
        try {
            final SReport report = persistenceService.selectOne(new SelectOneDescriptor<SReport>("getReportByName", Collections.singletonMap("reportName",
                    (Object) reportName), SReport.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getReportByName"));
            }
            return report;
        } catch (final SBonitaReadException sbe) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getReportByName", sbe));
            }
            throw sbe;
        }
    }

    @Override
    public long getNumberOfReports(final QueryOptions options) throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getNumberOfReports"));
        }
        try {
            final long number = persistenceService.getNumberOfEntities(SReport.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfReports"));
            }
            return number;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfReports", bre));
            }
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public List<SReport> searchReports(final QueryOptions options) throws SBonitaReadException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "searchReports"));
        }
        try {
            final List<SReport> reports = persistenceService.searchEntity(SReport.class, options, null);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "searchReports"));
            }
            return reports;
        } catch (final SBonitaReadException bre) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "searchReports", bre));
            }
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public void deleteReport(final long reportId) throws SReportDeletionException, SReportNotFoundException {
        try {
            final SReport report = getReport(reportId);
            deleteReport(report);
        } catch (final SBonitaReadException sbe) {
            throw new SReportDeletionException(sbe);
        }
    }

    private void deleteReport(final SReport report) throws SReportDeletionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteReport"));
        }
        final SReportLogBuilder logBuilder = getReportLog(ActionType.DELETED, "Deleting report named: " + report.getName());
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(report);
            final SDeleteEvent deleteEvent = getDeleteEvent(report, REPORT);
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(report.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteReport");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteReport"));
            }
        } catch (final SRecorderException re) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteReport", re));
            }
            initiateLogBuilder(report.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteReport");
            throw new SReportDeletionException(re);
        }
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SInsertEvent getInsertEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.CREATED)) {
            return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(type).setObject(object).done();
        }
        return null;
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(type).setObject(object).done();
        }
        return null;
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String methodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), methodName, log);
        }
    }

    @Override
    public byte[] getReportContent(final long reportId) throws SBonitaReadException, SReportNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getReportContent"));
        }
        try {
            final SReportContent getReportContent = persistenceService.selectById(new SelectByIdDescriptor<SReportContent>("getReportContent",
                    SReportContent.class, reportId));
            if (getReportContent == null) {
                throw new SReportNotFoundException(reportId);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getReportContent"));
            }
            return getReportContent.getContent();
        } catch (final SBonitaReadException sbe) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getReportContent", sbe));
            }
            throw sbe;
        }
    }

    @Override
    public void start() throws SBonitaException {
        defaultProfileImporter.invoke("case_avg_time");
        defaultProfileImporter.invoke("case_list");
        defaultProfileImporter.invoke("task_list");
        if (isTraceabilityActive) {
            defaultProfileImporter.invoke("case_history");
        }
    }



    @Override
    public void stop() throws SBonitaException {

    }

    @Override
    public void pause() throws SBonitaException {

    }

    @Override
    public void resume() throws SBonitaException {

    }
}
