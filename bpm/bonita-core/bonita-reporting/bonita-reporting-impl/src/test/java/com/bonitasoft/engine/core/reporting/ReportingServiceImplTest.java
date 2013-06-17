/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@SuppressWarnings("javadoc")
public class ReportingServiceImplTest {

    private static String lineSeparator = "\n";

    @Test(expected = SQLException.class)
    public void cannotExecuteADeleteStatement() throws SQLException {
        final EventService eventService = mock(EventService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, null, null, eventService, null, null);
        serviceImpl.selectList("DELETE FROM activities;");
    }

    @Test(expected = SQLException.class)
    public void cannotExecuteAnUpdateStatement() throws SQLException {
        final EventService eventService = mock(EventService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, null, null, eventService, null, null);
        serviceImpl.selectList("UPDATE activities SET name = 'step2';");
    }

    @Test
    public void executeASelectQuery() throws SQLException {
        final EventService eventService = mock(EventService.class);
        final String sql = "SELECT id, name  FROM activities;";
        final DataSource dataSource = mock(DataSource.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(dataSource, null, null, eventService, null, null);
        final Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(statement);
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.getResultSet()).thenReturn(resultSet);
        final ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(resultSet.next()).thenReturn(true, true, false);
        when(metaData.getColumnLabel(1)).thenReturn("id");
        when(metaData.getColumnLabel(2)).thenReturn("name");
        when(resultSet.getObject(1)).thenReturn(1l, 424l);
        when(resultSet.getObject(2)).thenReturn("step1", "step2");

        final String actual = serviceImpl.selectList(sql);
        final String expected = "ID,NAME" + lineSeparator + "1,step1" + lineSeparator + "424,step2" + lineSeparator;
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = SQLException.class)
    public void connectionCloses() throws SQLException {
        final EventService eventService = mock(EventService.class);
        final String sql = "SELECT COUNT(*) FROM activities;";
        final DataSource dataSource = mock(DataSource.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(dataSource, null, null, eventService, null, null);
        final Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(statement);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                throw new SQLException();
            }

        }).when(connection).close();
        serviceImpl.selectList(sql);
    }

    @Test
    public void getNumberOfReports() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, null, eventService, logger, null);
        final QueryOptions options = mock(QueryOptions.class);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long expected = 50;
        when(persistence.getNumberOfEntities(SReport.class, options, null)).thenReturn(expected);
        final long numberOfReports = serviceImpl.getNumberOfReports(options);
        Assert.assertEquals(expected, numberOfReports);
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfReportsThrowsException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, null, eventService, logger, null);
        final QueryOptions options = mock(QueryOptions.class);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        when(persistence.getNumberOfEntities(SReport.class, options, null)).thenThrow(new SBonitaReadException("ouch!"));
        serviceImpl.getNumberOfReports(options);
    }

    @Test
    public void searchReports() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, null, eventService, logger, null);
        final QueryOptions options = mock(QueryOptions.class);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final List<SReport> expected = new ArrayList<SReport>();
        expected.add(new SReportImpl("report1", 123456, 45, true));
        expected.add(new SReportImpl("report2", 12345656, 145, false));
        when(persistence.searchEntity(SReport.class, options, null)).thenReturn(expected);

        final List<SReport> reports = serviceImpl.searchReports(options);
        Assert.assertEquals(expected, reports);
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchReportsThrowsException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, null, eventService, logger, null);
        final QueryOptions options = mock(QueryOptions.class);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        when(persistence.searchEntity(SReport.class, options, null)).thenThrow(new SBonitaReadException("ouch!"));
        serviceImpl.searchReports(options);
    }

    @Test
    public void getReport() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, null, eventService, logger, null);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long reportId = 15;
        final SReport expected = new SReportImpl("report1", 123456, 45, true);
        expected.setId(reportId);
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportyId", SReport.class, reportId))).thenReturn(expected);

        final SReport report = serviceImpl.getReport(reportId);
        Assert.assertEquals(expected, report);
    }

    @Test(expected = SReportNotFoundException.class)
    public void getReportThrowsReportNotFoundException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, null, eventService, logger, null);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long reportId = 15;
        final SReport expected = new SReportImpl("report1", 123456, 45, true);
        expected.setId(reportId);
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportyId", SReport.class, reportId))).thenReturn(null);

        serviceImpl.getReport(reportId);
    }

    @Test(expected = SBonitaReadException.class)
    public void getReportThrowsException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, null, eventService, logger, null);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long reportId = 15;
        final SReport expected = new SReportImpl("report1", 123456, 45, true);
        expected.setId(reportId);
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportyId", SReport.class, reportId))).thenThrow(new SBonitaReadException("ouch!"));

        serviceImpl.getReport(reportId);
    }

    @Test
    public void deleteReport() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final Recorder recorder = mock(Recorder.class);
        final QueriableLoggerService loggerService = mock(QueriableLoggerService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, recorder, eventService, logger, loggerService);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long reportId = 15;
        final SReport expected = new SReportImpl("report1", 123456, 45, true);
        expected.setId(reportId);

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Deletion OK
                return null;
            }

        }).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportyId", SReport.class, reportId))).thenReturn(expected);
        when(loggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);

        serviceImpl.deleteReport(reportId);
    }

    @Test(expected = SReportDeletionException.class)
    public void deleteReportThrowsReportNotFoundException() throws SBonitaException {
        final EventService eventService = mock(EventService.class);
        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadPersistenceService persistence = mock(ReadPersistenceService.class);
        final Recorder recorder = mock(Recorder.class);
        final QueriableLoggerService loggerService = mock(QueriableLoggerService.class);
        final ReportingServiceImpl serviceImpl = new ReportingServiceImpl(null, persistence, recorder, eventService, logger, loggerService);
        when(logger.isLoggable(ReportingServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);
        final long reportId = 15;
        final SReport expected = new SReportImpl("report1", 123456, 45, true);
        expected.setId(reportId);

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                throw new SRecorderException("ouch !");
            }

        }).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportyId", SReport.class, reportId))).thenReturn(expected);
        when(loggerService.isLoggable(any(String.class), any(SQueriableLogSeverity.class))).thenReturn(true);

        serviceImpl.deleteReport(reportId);
    }

}
