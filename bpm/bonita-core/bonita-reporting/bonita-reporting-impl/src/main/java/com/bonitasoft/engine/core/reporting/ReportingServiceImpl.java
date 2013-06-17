/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
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
import java.util.List;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Matthieu Chaffotte
 */
public class ReportingServiceImpl implements ReportingService {

    private final DataSource dataSource;

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final SEventBuilder eventBuilder;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    public ReportingServiceImpl(final DataSource dataSource, final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        this.dataSource = dataSource;
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        eventBuilder = eventService.getEventBuilder();
        this.recorder = recorder;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public String selectList(final String selectQuery) throws SQLException {
        final String lowerSQ = selectQuery.toLowerCase();
        if (!lowerSQ.startsWith("select")) {
            throw new SQLException("The statement is not a SELECT query");
        }
        final StringBuilder builder = new StringBuilder();
        final Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            final Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.execute(selectQuery);
            final ResultSet resultSet = statement.getResultSet();
            if (resultSet != null) {
                final String newline = "\n";
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columns = metaData.getColumnCount();
                for (int i = 1; i < columns; i++) {
                    final String columnName = metaData.getColumnLabel(i);
                    // in order to use the same case for all database
                    builder.append(columnName.toUpperCase()).append(",");
                }
                // Special treatment of last record (to avoid having extra comma at the end):
                final String columnName = metaData.getColumnLabel(columns);
                builder.append(columnName.toUpperCase()).append(newline);
                while (resultSet.next()) {
                    for (int j = 1; j < columns; j++) {
                        final Object value = resultSet.getObject(j);
                        builder.append(value).append(",");
                    }
                    final Object value = resultSet.getObject(columns);
                    // Special treatment of last record (to avoid having extra comma at the end):
                    builder.append(value).append(newline);
                }
            }
            return builder.toString();
        } finally {
            connection.close();
        }
    }

    @Override
    public SReport addReport(final SReport report, final byte[] content) throws SReportCreationException, SReportAlreadyExistsException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "addReport"));
        }
        final String message = "Adding a new report with name " + report.getName();
        final SReportLogBuilder logBuilder = getReportLog(ActionType.CREATED, message);
        try {
            final InsertRecord insertRecord = new InsertRecord(report);
            final SInsertEvent insertEvent = getInsertEvent(report, REPORT);
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(report.getId(), SQueriableLog.STATUS_OK, logBuilder, "addReport");
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
            final SReport report = persistenceService.selectById(new SelectByIdDescriptor<SReport>("getReportyId", SReport.class, reportId));
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
    public long getNumberOfReports(final QueryOptions options) throws SBonitaSearchException {
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
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SReport> searchReports(final QueryOptions options) throws SBonitaSearchException {
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
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public void deleteReport(final long reportId) throws SReportDeletionException, SReportNotFoundException {
        try {
            final SReport report = getReport(reportId);
            deleteReport(report);
        } catch (final SBonitaReadException sbe) {
            new SReportDeletionException(sbe);
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
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SInsertEvent getInsertEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.CREATED)) {
            return (SInsertEvent) eventBuilder.createInsertEvent(type).setObject(object).done();
        } else {
            return null;
        }
    }

    private SDeleteEvent getDeleteEvent(final Object object, final String type) {
        if (eventService.hasHandlers(type, EventActionType.DELETED)) {
            return (SDeleteEvent) eventBuilder.createDeleteEvent(type).setObject(object).done();
        } else {
            return null;
        }
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
    public SReportBuilder getReportBuilder() {
        return new SReportBuilderImpl();
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

}
