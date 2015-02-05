/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.core.reporting.processor.QueryPreProcessor;
import com.bonitasoft.engine.core.reporting.processor.Vendor;

@RunWith(MockitoJUnitRunner.class)
public class ReportingServiceImplTest {

    private static String lineSeparator = "\n";

    @InjectMocks
    private ReportingServiceImpl reportingService;

    @Mock
    private Connection connection;

    @Mock
    private ReadPersistenceService persistence;

    @Mock
    private Recorder recorder;

    @Mock
    private QueryPreProcessor preProcessor;

    @Mock
    private Manager manager;

    @Mock
    private DefaultReportImporter defaultProfileImporter;

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    private SReport aReport(final long reportId) {
        final SReport expected = new SReportImpl("report1", 123456, 45, true);
        expected.setId(reportId);
        return expected;
    }

    @Test(expected = SQLException.class)
    public void cannotExecuteADeleteStatement() throws SQLException {
        reportingService.selectList("DELETE FROM activities;");
    }

    @Test(expected = SQLException.class)
    public void cannotExecuteAnUpdateStatement() throws SQLException {
        reportingService.selectList("UPDATE activities SET name = 'step2';");
    }

    @Test
    public void executeASelectQuery() throws SQLException {
        final String sql = "SELECT id, name  FROM activities;";
        final Statement statement = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(statement);
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        final ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(resultSet.next()).thenReturn(true, true, false);
        when(metaData.getColumnLabel(1)).thenReturn("id");
        when(metaData.getColumnLabel(2)).thenReturn("name");
        when(resultSet.getObject(1)).thenReturn(1l, 424l);
        when(resultSet.getObject(2)).thenReturn("step1", "step2");

        final String actual = reportingService.selectList(sql);

        final String expected = "ID,NAME" + lineSeparator + "1,step1" + lineSeparator + "424,step2" + lineSeparator;
        assertEquals(expected, actual);
    }

    @Test
    public void should_selectList_escaping_comma_quotes() throws Exception {
        final String sql = "SELECT id, name  FROM activities;";
        final Statement statement = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(statement);
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        final ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(resultSet.next()).thenReturn(true, true, false);
        when(metaData.getColumnLabel(1)).thenReturn("id");
        when(metaData.getColumnLabel(2)).thenReturn("name");
        when(metaData.getColumnLabel(3)).thenReturn("counter");
        when(resultSet.getObject(1)).thenReturn(1l, 424l);
        when(resultSet.getObject(2)).thenReturn("\"dsd\" , s tep1", "s\"t\"ep2");
        when(resultSet.getObject(3)).thenReturn(1, 2);

        final String actual = reportingService.selectList(sql);

        final String expected = "ID,NAME,COUNTER" + lineSeparator + "1,\"\"\"dsd\"\" , s tep1\",1" + lineSeparator + "424,\"s\"\"t\"\"ep2\",2" + lineSeparator;
        assertEquals(expected, actual);
    }

    @Test(expected = SQLException.class)
    public void connectionCloses() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM activities;";
        final Statement statement = mock(Statement.class);
        when(connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(statement);
        doThrow(new SQLException()).when(connection).close();

        reportingService.selectList(sql);
    }

    @Test
    public void getNumberOfReports() throws SBonitaException {
        final long expectedReportNumber = 50;
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.getNumberOfEntities(SReport.class, options, null)).thenReturn(expectedReportNumber);

        final long numberOfReports = reportingService.getNumberOfReports(options);

        assertEquals(expectedReportNumber, numberOfReports);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfReportsThrowsException() throws SBonitaException {
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.getNumberOfEntities(SReport.class, options, null)).thenThrow(new SBonitaReadException("ouch!"));

        reportingService.getNumberOfReports(options);
    }

    @Test
    public void searchReports() throws SBonitaException {
        final QueryOptions options = mock(QueryOptions.class);
        final List<SReport> expected = new ArrayList<SReport>();
        expected.add(new SReportImpl("report1", 123456, 45, true));
        expected.add(new SReportImpl("report2", 12345656, 145, false));
        when(persistence.searchEntity(SReport.class, options, null)).thenReturn(expected);

        final List<SReport> reports = reportingService.searchReports(options);

        assertEquals(expected, reports);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchReportsThrowsException() throws SBonitaException {
        final QueryOptions options = mock(QueryOptions.class);
        when(persistence.searchEntity(SReport.class, options, null)).thenThrow(new SBonitaReadException("ouch!"));

        reportingService.searchReports(options);
    }

    @Test
    public void getReport() throws SBonitaException {
        final long reportId = 15;
        final SReport expected = aReport(reportId);
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportById", SReport.class, reportId))).thenReturn(expected);

        final SReport report = reportingService.getReport(reportId);

        assertEquals(expected, report);
    }

    @Test(expected = SReportNotFoundException.class)
    public void getReportThrowsReportNotFoundException() throws SBonitaException {
        final long reportId = 15;
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportById", SReport.class, reportId))).thenReturn(null);

        reportingService.getReport(reportId);
    }

    @Test(expected = SBonitaReadException.class)
    public void getReportThrowsException() throws SBonitaException {
        final long reportId = 15;
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportById", SReport.class, reportId))).thenThrow(new SBonitaReadException("ouch!"));

        reportingService.getReport(reportId);
    }

    @Test(expected = SReportDeletionException.class)
    public void deleteReportThrowsReportNotFoundException() throws SBonitaException {
        final long reportId = 15;
        final SReport expected = aReport(reportId);
        doThrow(new SRecorderException("ouch !")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        when(persistence.selectById(new SelectByIdDescriptor<SReport>("getReportById", SReport.class, reportId))).thenReturn(expected);

        reportingService.deleteReport(reportId);
    }

    @Test
    public void should_preprocess_query_before_execute_it() throws Exception {
        ReportingServiceImpl reportingServicespy = spy(reportingService);
        doReturn("something we don't care").when(reportingServicespy).executeQuery(anyString(), any(Statement.class));

        reportingServicespy.selectList("SELECT something FROM somewhere");

        InOrder inOrder = inOrder(preProcessor, reportingServicespy);
        inOrder.verify(preProcessor).preProcessFor(Vendor.OTHER, "SELECT something FROM somewhere");
        inOrder.verify(reportingServicespy).executeQuery(anyString(), any(Statement.class));
    }

    @Test
    public void should_start_import_default_report() throws Exception {
        //given

        //when
        reportingService.start();

        //then
        verify(defaultProfileImporter,times(3)).invoke(anyString());
    }

    @Test
    public void should_start_import_default_report_with_traceability_licence() throws Exception {
        //given
        doReturn(true).when(manager).isFeatureActive(Features.TRACEABILITY);
        reportingService = new ReportingServiceImpl(dataSource,persistence,preProcessor,recorder,eventService,technicalLoggerService,queriableLoggerService,manager,defaultProfileImporter);

        //when
        reportingService.start();

        //then
        verify(defaultProfileImporter,times(4)).invoke(anyString());
    }
}
