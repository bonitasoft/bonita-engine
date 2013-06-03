package org.bonitasoft.engine.queriablelogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.MockQueriableLogSessionProviderImpl;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.SIndexedLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogModelBuilder;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QueriableLoggerServiceTest extends CommonServiceTest {

    final static int NUMERIC_INDEX1 = 0;

    final static int NUMERIC_INDEX2 = 1;

    final static int NUMERIC_INDEX3 = 2;

    final static int NUMERIC_INDEX4 = 3;

    final static int NUMERIC_INDEX5 = 4;

    final static int TEXTUAL_INDEX1 = 0;

    final static int TEXTUAL_INDEX2 = 1;

    final static int TEXTUAL_INDEX3 = 2;

    final static int TEXTUAL_INDEX4 = 3;

    final static int TEXTUAL_INDEX5 = 4;

    final static int SLEEP_TIME = 1400;

    private final QueriableLoggerService loggerService;

    private final SQueriableLogModelBuilder logModelBuilder;

    private final SIndexedLogBuilder logbuilder;

    private final PersistenceService persistenceService;

    private final QueriableLogSessionProvider qlSessionProvider;

    public QueriableLoggerServiceTest() throws Exception {
        loggerService = getServicesBuilder().buildQueriableLogger("syncQueriableLoggerService");
        logModelBuilder = getServicesBuilder().getInstanceOf(SQueriableLogModelBuilder.class);
        logbuilder = logModelBuilder.getQueriableLogBuilder();
        persistenceService = getServicesBuilder().buildPersistence();
        qlSessionProvider = getServicesBuilder().getInstanceOf(MockQueriableLogSessionProviderImpl.class);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        getTransactionService().begin();
        persistenceService.deleteAll(SQueriableLog.class);
        getTransactionService().complete();
    }

    private SIndexedLogBuilder buildQueriableLog(final String actionType, final String actionScope, final SQueriableLogSeverity severity,
            final int actionStatus, final String message) {
        final SIndexedLogBuilder logBuilder = logModelBuilder.getQueriableLogBuilder();
        logBuilder.createNewInstance();
        logBuilder.actionType(actionType).actionScope(actionScope).severity(severity);
        logBuilder.actionStatus(actionStatus).rawMessage(message).done();

        return logBuilder;
    }

    private SIndexedLogBuilder buildQueriableLog(final String actionType, final String actionScope, final SQueriableLogSeverity severity,
            final int actionStatus, final String message, final Map<Integer, Long> numericIndexes) {
        final SIndexedLogBuilder logBuilder = buildQueriableLog(actionType, actionScope, severity, actionStatus, message);
        for (final Integer numericIndex : numericIndexes.keySet()) {
            logBuilder.numericIndex(numericIndex, numericIndexes.get(numericIndex));
        }
        return logBuilder;
    }

    protected boolean needSleep() {
        return false;
    }

    @Test
    public void testLogNominal() throws Exception {
        getTransactionService().begin();
        final SIndexedLogBuilder log1 = buildQueriableLog("execute_connector", "setVariableConnector", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK,
                "sucessFull", Collections.singletonMap(NUMERIC_INDEX1, 123L));
        loggerService.log(this.getClass().getName(), "testLogNominal", log1.done());
        getTransactionService().complete();

        // wait job execution
        if (needSleep()) {
            Thread.sleep(SLEEP_TIME);
        }

        getTransactionService().begin();

        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(logbuilder.getNumericIndexKey(1), 123L);
        final String queryName = "getLogsFromNumericIndex1";
        final List<SQueriableLog> retrievedLogs = persistenceService.selectList(new SelectListDescriptor<SQueriableLog>(queryName, inputs, SQueriableLog.class,
                new QueryOptions(0, 10)));
        assertEquals(1, retrievedLogs.size());
        final SQueriableLog firstRetrievedLog = retrievedLogs.get(0);
        assertEquals("execute_connector", firstRetrievedLog.getActionType());
        assertEquals(SQueriableLogSeverity.BUSINESS, firstRetrievedLog.getSeverity());
        assertEquals("setVariableConnector", firstRetrievedLog.getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testLoggetTransactionRolledBack() throws Exception {
        getTransactionService().begin();
        final SQueriableLog log1 = buildQueriableLog("execute_connector", "setVariableConnector", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK,
                "sucessFull", Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();
        loggerService.log(this.getClass().getName(), "testLoggetTransactionService()RolledBack", log1);
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        // wait job execution
        if (needSleep()) {
            Thread.sleep(SLEEP_TIME);
        }

        getTransactionService().begin();

        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(logbuilder.getNumericIndexKey(1), 123L);
        final String queryName = "getLogsFromNumericIndex1";
        final List<SQueriableLog> retrievedLogs = persistenceService.selectList(new SelectListDescriptor<SQueriableLog>(queryName, inputs, SQueriableLog.class,
                new QueryOptions(0, 10)));
        assertEquals(0, retrievedLogs.size());
        getTransactionService().complete();
    }

    @Test
    public void testLogWhenAllLoggable() throws Exception {
        getTransactionService().begin();
        final SQueriableLog log1 = buildQueriableLog("execute_connector", "setVariableConnector", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK,
                "sucessFull", Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();
        Thread.sleep(10);

        final SQueriableLog log2 = buildQueriableLog("variable_update", "booleanVar", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "sucessFull",
                Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

        loggerService.log(this.getClass().getName(), "testLogWhenAllLoggable", log1, log2);
        getTransactionService().complete();

        // wait job execution
        if (needSleep()) {
            Thread.sleep(SLEEP_TIME);
        }

        getTransactionService().begin();

        final Map<String, Object> inputs = new HashMap<String, Object>();
        inputs.put(logbuilder.getNumericIndexKey(1), 123L);
        final String queryName = "getLogsFromNumericIndex1";

        final List<SQueriableLog> retrievedLogs = persistenceService.selectList(new SelectListDescriptor<SQueriableLog>(queryName, inputs, SQueriableLog.class,
                new QueryOptions(0, 10, SQueriableLog.class, logbuilder.getTimeStampKey(), OrderByType.DESC)));
        assertEquals(2, retrievedLogs.size());
        final SQueriableLog firstRetrievedLog = retrievedLogs.get(0);
        final SQueriableLog secondRetrievedLog = retrievedLogs.get(1);
        assertEquals("variable_update", firstRetrievedLog.getActionType());
        assertEquals("execute_connector", secondRetrievedLog.getActionType());
        getTransactionService().complete();
    }

    @Test
    public void testLogWhenSomeLoggable() throws Exception {
        getTransactionService().begin();
        final SQueriableLog log1 = buildQueriableLog("execute_connector_", "setVariableConnector", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK,
                "sucessFull", Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

        Thread.sleep(10);

        final SQueriableLog log2 = buildQueriableLog("variable_update", "booleanVar", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "sucessFull",
                Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

        loggerService.log(this.getClass().getName(), "testLogWhenSomeLoggable", log1, log2);
        getTransactionService().complete();

        // wait job execution
        if (needSleep()) {
            Thread.sleep(SLEEP_TIME);
        }

        getTransactionService().begin();
        final List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 10, null, null);
        assertEquals(1, retrievedLogs.size());
        final SQueriableLog retrievedLog = retrievedLogs.get(0);
        assertEquals("variable_update", retrievedLog.getActionType());
        getTransactionService().complete();
    }

    @Test
    public void testLogWhenNoneLoggable() throws Exception {
        getTransactionService().begin();
        final SQueriableLog log1 = buildQueriableLog("execute_connector_", "setVariableConnector", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK,
                "sucessFull", Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

        Thread.sleep(10);

        final SQueriableLog log2 = buildQueriableLog("variable_update_", "booleanVar", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "sucessFull",
                Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

        loggerService.log(this.getClass().getName(), "testLogWhenNoneLoggable", log1, log2);
        getTransactionService().complete();

        // wait job execution
        if (needSleep()) {
            Thread.sleep(SLEEP_TIME);
        }

        getTransactionService().begin();

        final List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 10, null, null);
        assertEquals(0, retrievedLogs.size());
        getTransactionService().complete();
    }

    @Test
    public void testSessionProviderInformation() throws Exception {
        getTransactionService().begin();
        final SQueriableLog log1 = buildQueriableLog("variable_update", "booleanVar", SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "sucessFull",
                Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

        loggerService.log(this.getClass().getName(), "testSessionProviderInformation", log1);
        getTransactionService().complete();

        // wait job execution
        if (needSleep()) {
            Thread.sleep(SLEEP_TIME);
        }

        getTransactionService().begin();

        final List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 10, null, null);
        assertEquals(1, retrievedLogs.size());
        final SQueriableLog retrievedLog = retrievedLogs.get(0);
        assertEquals("variable_update", retrievedLog.getActionType());
        assertEquals("admin", retrievedLog.getUserId());
        assertEquals("node1", retrievedLog.getClusterNode());
        assertEquals("BOS-6.0", retrievedLog.getProductVersion());
        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex1OrderByTimeAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex1OrderByTimeDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex1OrderByDefault() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex2OrderByTimeAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX2, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex2OrderByTimeDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX2, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex2OrderByDefault() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX2, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex3OrderByTimeAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX3, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex3OrderByTimeDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX3, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex3OrderByDefault() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX3, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex4OrderByTimeAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX4, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex4OrderByTimeDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX4, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex4OrderByDefault() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX4, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex5OrderByTimeAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX5, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex5OrderByTimeDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX5, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex5OrderByDefault() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "logged",
                    Collections.singletonMap(NUMERIC_INDEX5, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 0, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector6", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector5", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector4", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 3, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector3", retrievedLogs.get(0).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals("setVariableConnector2", retrievedLogs.get(1).getActionScope());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals("setVariableConnector1", retrievedLogs.get(2).getActionScope());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 6, 3, logbuilder.getTimeStampKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals("setVariableConnector0", retrievedLogs.get(0).getActionScope());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex1OrderByStatusAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex1OrderByStatusDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX1, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex2OrderByStatusAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX2, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex2OrderByStatusDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX2, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX2, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex3OrderByStatusAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX3, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex3OrderByStatusDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX3, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX3, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex4OrderByStatusAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX4, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex4OrderByStatusDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX4, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX4, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex5OrderByStatusAsc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX5, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.ASC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    @Test
    public void testGetLogsFromLongIndex5OrderByStatusDesc() throws Exception {
        getTransactionService().begin();

        final int numberOfLogs = 7;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, i % 2, "logged",
                    Collections.singletonMap(NUMERIC_INDEX5, 123L)).done();

            Thread.sleep(10);
        }

        persisteLogWithPersistenceService(logs);
        getTransactionService().complete();

        getTransactionService().begin();

        List<SQueriableLog> retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 0, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(1, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(1, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(1, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 3, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(3, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(1).getActionType());
        assertEquals(0, retrievedLogs.get(1).getActionStatus());
        assertEquals("execute_connector", retrievedLogs.get(2).getActionType());
        assertEquals(0, retrievedLogs.get(2).getActionStatus());

        retrievedLogs = loggerService.getLogsFromLongIndex(NUMERIC_INDEX5, 123L, 6, 3, logbuilder.getActionStatusKey(), OrderByType.DESC);
        assertEquals(1, retrievedLogs.size());
        assertEquals("execute_connector", retrievedLogs.get(0).getActionType());
        assertEquals(0, retrievedLogs.get(0).getActionStatus());

        getTransactionService().complete();
    }

    private void persisteLogWithPersistenceService(final SQueriableLog... logs) throws SPersistenceException {
        for (SQueriableLog sQueriableLog : logs) {

            // add attributes injected by the log service implementation
            sQueriableLog = logModelBuilder.getQueriableLogBuilder().fromInstance(sQueriableLog).userId(qlSessionProvider.getUserId())
                    .clusterNode(qlSessionProvider.getClusterNode()).productVersion(qlSessionProvider.getProductVersion()).done();

            persistenceService.insert(sQueriableLog);
        }
    }

    @Test
    public void testGetLogByLogId() throws Exception {
        getTransactionService().begin();
        final SQueriableLog testGetLog = buildQueriableLog("execute_connector", "setVariableConnector", SQueriableLogSeverity.BUSINESS,
                SQueriableLog.STATUS_OK, "sucessFull", Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();
        persisteLogWithPersistenceService(testGetLog);

        final long id = testGetLog.getId();
        final SQueriableLog log = loggerService.getLog(id);
        assertNotNull(log);
        assertEquals("execute_connector", testGetLog.getActionType());
        assertEquals("setVariableConnector", testGetLog.getActionScope());
        getTransactionService().complete();
    }

    @Test
    public void testGetNumberOfLogs() throws Exception {
        getTransactionService().begin();
        final int numberOfLogs = 5;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "setVariableConnector" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "sucessFull",
                    Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();
        }
        persisteLogWithPersistenceService(logs);
        final int logCounts = loggerService.getNumberOfLogs();
        assertEquals(5, logCounts);
        getTransactionService().complete();
    }

    @Test
    public void testGetLogs() throws Exception {
        getTransactionService().begin();
        final int numberOfLogs = 5;
        final SQueriableLog[] logs = new SQueriableLog[numberOfLogs];
        for (int i = 0; i < numberOfLogs; i++) {
            logs[i] = buildQueriableLog("execute_connector", "testGetLogsNumber" + i, SQueriableLogSeverity.BUSINESS, SQueriableLog.STATUS_OK, "sucessFull",
                    Collections.singletonMap(NUMERIC_INDEX1, 123L)).done();
        }
        persisteLogWithPersistenceService(logs);

        List<SQueriableLog> queriablelogs = new ArrayList<SQueriableLog>();
        queriablelogs = loggerService.getLogs(0, 3, logModelBuilder.getQueriableLogBuilder().getActionScopeKey(), OrderByType.ASC);
        assertEquals(3, queriablelogs.size());
        assertEquals("testGetLogsNumber0", queriablelogs.get(0).getActionScope());
        assertEquals("testGetLogsNumber1", queriablelogs.get(1).getActionScope());
        assertEquals("testGetLogsNumber2", queriablelogs.get(2).getActionScope());

        queriablelogs = loggerService.getLogs(0, 3, logModelBuilder.getQueriableLogBuilder().getActionScopeKey(), OrderByType.DESC);
        assertEquals(3, queriablelogs.size());
        assertEquals("testGetLogsNumber4", queriablelogs.get(0).getActionScope());
        assertEquals("testGetLogsNumber3", queriablelogs.get(1).getActionScope());
        assertEquals("testGetLogsNumber2", queriablelogs.get(2).getActionScope());
        getTransactionService().complete();
    }

}
