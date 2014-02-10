package org.bonitasoft.engine.recorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.archive.model.TestLogBuilderFactory;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros, Yanyan Liu
 */
public class RecorderTest extends CommonServiceTest {

    private static final String TEST_DELETE = "TEST_DELETED";

    private static final String TEST_UPDATE = "TEST_UPDATED";

    private static final String TEST_CREATED = "TEST_CREATED";

    private final static int SLEEP_TIME = 200;

    private final static String HUMAN = "HUMAN";

    protected static PersistenceService persitenceService;

    protected static Recorder recorder;

    protected static SchedulerService scheduler;

    protected static SPlatformBuilder platformBuilder;

    protected static STenantBuilder tenantBuilder;

    protected static EventService eventService;

    protected static QueriableLoggerService loggerService;

    private TestLogBuilderFactory logModelBuilderFactory;

    static {
        persitenceService = getServicesBuilder().buildTenantPersistenceService();
        recorder = getServicesBuilder().buildRecorder();
        scheduler = getServicesBuilder().buildSchedulerService();
        eventService = getServicesBuilder().buildEventService();
        loggerService = getServicesBuilder().buildQueriableLogger();
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.startScheduler(scheduler);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
        TestUtil.stopScheduler(scheduler, getTransactionService());
    }

    private Human buildHuman(final String firstName, final String lastName, final int age) {
        final Human human = new Human();
        human.setFirstName(firstName);
        human.setLastName(lastName);
        human.setAge(age);
        return human;
    }

    private Map<String, Object> getMap(final String key, final Object value) {
        return Collections.singletonMap(key, value);
    }

    private List<SQueriableLog> getLogs(final long indexValue, final String actionType) throws SBonitaSearchException {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(getActionTypeFilterOption(actionType));
        filters.add(new FilterOption(SQueriableLog.class, getLogModelBuilderFactory().getObjectIdKey(), indexValue));
        final QueryOptions opts = new QueryOptions(0, 10, null, filters, null);
        return loggerService.searchLogs(opts);
    }

    private FilterOption getActionTypeFilterOption(final String actionType) {
        return new FilterOption(SQueriableLog.class, getLogModelBuilderFactory().getActionTypeKey(), actionType);
    }

    private List<SQueriableLog> getLogs(final String actionType) throws SBonitaSearchException {
        final List<FilterOption> filters = Arrays.asList(getActionTypeFilterOption(actionType));
        return loggerService.searchLogs(new QueryOptions(0, 10, null, filters, null));
    }

    private void checkHuman(final Human expectedHuman, final Human retrievedHuman) {
        assertNotNull(retrievedHuman);
        assertEquals(expectedHuman, retrievedHuman);
    }

    private Human getHumanByFirstName(final String firstName) throws SBonitaReadException {
        return getPersistenceService().selectOne(new SelectOneDescriptor<Human>("getHumanByFirstName", getMap("firstName", firstName), Human.class));
    }

    private static class Foo {

        boolean insertCompleted = false;

        boolean readCompleted = false;

        boolean foundUser = false;
    }

    @Ignore
    @Test
    public void testReadNotCommittedInsert() throws Exception {

        final Foo foo = new Foo();

        final String firstName = "Laurent";
        final Runnable insert = new Runnable() {

            private void setSessionInfo() {
                final TransactionService txService = getTransactionService();
                try {
                    txService.begin();
                    final SSession session = getSessionService().createSession(1, "install");
                    getSessionAccessor().setSessionInfo(session.getId(), 1);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        txService.complete();
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void run() {
                final TransactionService txService = getTransactionService();

                setSessionInfo();
                try {
                    txService.begin();
                    final Human human = buildHuman(firstName, "Vaills", 20);
                    final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(HUMAN).setObject(human).done();
                    recorder.recordInsert(new InsertRecord(human), insertEvent);
                    foo.insertCompleted = true;

                    while (!foo.readCompleted) {
                        Thread.sleep(500);
                    }
                    // set rollback
                    getTransactionService().setRollbackOnly();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        txService.complete();
                    } catch (final STransactionCommitException e) {
                        e.printStackTrace();
                    } catch (final STransactionRollbackException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        final Runnable read = new Runnable() {

            private void setSessionInfo() {
                final TransactionService txService = getTransactionService();
                try {
                    txService.begin();
                    final SSession session = getSessionService().createSession(1, "install");
                    getSessionAccessor().setSessionInfo(session.getId(), 1);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        txService.complete();
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void run() {
                final TransactionService txService = getTransactionService();
                final long start = System.currentTimeMillis();
                try {
                    while (!foo.insertCompleted) {
                        Thread.sleep(500);
                        if (System.currentTimeMillis() - start > 3000) {
                            throw new RuntimeException("timeout");
                        }
                    }
                } catch (final InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
                setSessionInfo();
                try {
                    txService.begin();
                    final Human retrievedHuman = getHumanByFirstName(firstName);
                    foo.foundUser = retrievedHuman != null;
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    foo.readCompleted = true;
                    try {
                        txService.complete();
                    } catch (final STransactionCommitException e) {
                        e.printStackTrace();
                    } catch (final STransactionRollbackException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        final Thread insertThread = new Thread(insert);
        final Thread readThread = new Thread(read);
        insertThread.start();
        readThread.start();

        insertThread.join();
        readThread.join();

        Thread.sleep(8000);

        assertFalse(foo.foundUser);
    }

    @Test
    public void testNotLogOnInsertRecordWhenBTXRolledBack() throws Exception {
        System.out.println(getTransactionService());
        getTransactionService().begin();
        final SelectOneDescriptor<Human> selectDescriptor = new SelectOneDescriptor<Human>("getHumanByFirstName", getMap("firstName", "firstName"), Human.class);
        Human retrievedHuman = getPersistenceService().selectOne(selectDescriptor);
        assertNull("Should not have any Human in DB before test", retrievedHuman);

        final String firstName = "Laurent";
        final Human human = buildHuman(firstName, "Vaills", 20);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(HUMAN).setObject(human).done();
        recorder.recordInsert(new InsertRecord(human), insertEvent);

        // set rollback
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        Thread.sleep(SLEEP_TIME);

        // The transaction has been rolled back no Human nor log should have been inserted.
        getTransactionService().begin();

        retrievedHuman = getPersistenceService().selectOne(selectDescriptor);
        assertNull(retrievedHuman);

        final List<SQueriableLog> retrievedLogs = getLogs(TEST_CREATED);
        assertEquals(0, retrievedLogs.size());

        getTransactionService().complete();
    }

    @Test
    public void testNotLogOnUpdateRecordWhenBTXRolledBack() throws Exception {
        // add human using persistence service
        getTransactionService().begin();

        final Human human = buildHuman("firstName", "lastName", 20);
        getPersistenceService().insert(human);
        getTransactionService().complete();

        // update human (fail)
        getTransactionService().begin();

        final Human humanToUpdate = getHumanByFirstName("firstName");
        assertNotNull(humanToUpdate);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(HUMAN).setObject(human).done();
        recorder.recordUpdate(
                UpdateRecord.buildSetField(humanToUpdate, "firstName", "firstName", "firstNameUpdate", "Update human", HumanRecordType.updateHuman),
                updateEvent);
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        // wait
        Thread.sleep(SLEEP_TIME);

        // query
        getTransactionService().begin();
        final Human retrivedHuman = getPersistenceService().selectOne(
                new SelectOneDescriptor<Human>("getHumanByFirstName", getMap("firstName", "firstNameUpdate"), Human.class));
        assertNull(retrivedHuman);

        final List<SQueriableLog> retrievedLogs = getLogs(TEST_UPDATE);
        assertEquals(0, retrievedLogs.size());

        final Human addedHuman = getHumanByFirstName("firstName");
        getPersistenceService().delete(addedHuman);

        getTransactionService().complete();
    }

    @Test
    public void testNotLogOnDeleteRecordIfBTXRolledBack() throws Exception {
        // add human using persistence service
        getTransactionService().begin();
        final Human human = buildHuman("firstName", "lastName", 20);
        getPersistenceService().insert(human);
        getTransactionService().complete();

        // delete human using recorder
        getTransactionService().begin();

        final Human humanToDelete = getHumanByFirstName("firstName");
        assertNotNull(humanToDelete);
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(HUMAN).setObject(human).done();
        recorder.recordDelete(new DeleteRecord(humanToDelete), deleteEvent);
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        // wait
        Thread.sleep(SLEEP_TIME);

        // query
        getTransactionService().begin();
        final Human retrievedHuman = getHumanByFirstName("firstName");
        checkHuman(human, retrievedHuman);

        final List<SQueriableLog> retrievedLogs = getLogs(retrievedHuman.getId(), TEST_DELETE);
        assertEquals(0, retrievedLogs.size());

        getTransactionService().complete();

        // clean up:
        getTransactionService().begin();
        getPersistenceService().delete(retrievedHuman);
        getTransactionService().complete();
    }

    protected synchronized PersistenceService getPersistenceService() {
        return persitenceService;
    }

    protected synchronized TestLogBuilderFactory getLogModelBuilderFactory() {
        if (logModelBuilderFactory == null) {
            logModelBuilderFactory = new TestLogBuilderFactory();
        }
        return logModelBuilderFactory;
    }

    private enum HumanRecordType {
        updateHuman
    }

}
