/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

import org.bonitasoft.engine.archive.model.TestLogBuilderFactory;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.scheduler.SchedulerService;
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
public class RecorderTest extends CommonBPMServicesTest {

    private static final String TEST_DELETE = "TEST_DELETED";

    private static final String TEST_UPDATE = "TEST_UPDATED";

    private static final String TEST_CREATED = "TEST_CREATED";

    private final static int SLEEP_TIME = 200;

    private final static String HUMAN = "HUMAN";

    protected ReadPersistenceService persitenceService;

    protected Recorder recorder;

    protected SchedulerService scheduler;

    protected EventService eventService;

    protected QueriableLoggerService loggerService;

    private TestLogBuilderFactory logModelBuilderFactory;

    public RecorderTest() {
        persitenceService = getTenantAccessor().getReadPersistenceService();
        recorder = getTenantAccessor().getRecorder();
        scheduler = getTenantAccessor().getSchedulerService();
        eventService = getTenantAccessor().getEventService();
        loggerService = getTenantAccessor().getQueriableLoggerService();
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.startScheduler(scheduler);
    }

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

    private List<SQueriableLog> getLogs(final long indexValue, final String actionType) throws SBonitaReadException {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(getActionTypeFilterOption(actionType));
        filters.add(new FilterOption(SQueriableLog.class, getLogModelBuilderFactory().getObjectIdKey(), indexValue));
        final List<OrderByOption> orders = Arrays.asList(new OrderByOption(SQueriableLog.class, "id", OrderByType.ASC));
        final QueryOptions opts = new QueryOptions(0, 10, orders, filters, null);
        return loggerService.searchLogs(opts);
    }

    private FilterOption getActionTypeFilterOption(final String actionType) {
        return new FilterOption(SQueriableLog.class, getLogModelBuilderFactory().getActionTypeKey(), actionType);
    }

    private List<SQueriableLog> getLogs(final String actionType) throws SBonitaReadException {
        final List<FilterOption> filters = Arrays.asList(getActionTypeFilterOption(actionType));
        final List<OrderByOption> orders = Arrays.asList(new OrderByOption(SQueriableLog.class, "id", OrderByType.ASC));
        return loggerService.searchLogs(new QueryOptions(0, 10, orders, filters, null));
    }

    private void checkHuman(final Human expectedHuman, final Human retrievedHuman) {
        assertNotNull(retrievedHuman);
        assertEquals(expectedHuman, retrievedHuman);
    }

    private Human getHumanByFirstName(final String firstName) throws SBonitaReadException {
        return getPersistenceService().selectOne(new SelectOneDescriptor<Human>("getHumanByFirstName", getMap("firstName", firstName), Human.class));
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
        recorder.recordInsert(new InsertRecord(human), null);
        getTransactionService().complete();

        // update human (fail)
        getTransactionService().begin();

        final Human humanToUpdate = getHumanByFirstName("firstName");
        assertNotNull(humanToUpdate);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(HUMAN).setObject(human).done();
        final Map<String, Object> stringObjectMap = Collections.<String, Object> singletonMap("firstName", "firstName");
        recorder.recordUpdate(UpdateRecord.buildSetFields(humanToUpdate, stringObjectMap), updateEvent);
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
        recorder.recordDelete(new DeleteRecord(addedHuman), null);

        getTransactionService().complete();
    }

    @Test
    public void testNotLogOnDeleteRecordIfBTXRolledBack() throws Exception {
        // add human using persistence service
        getTransactionService().begin();
        final Human human = buildHuman("firstName", "lastName", 20);
        recorder.recordInsert(new InsertRecord(human), null);
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
        recorder.recordDelete(new DeleteRecord(retrievedHuman), null);
        getTransactionService().complete();
    }

    protected ReadPersistenceService getPersistenceService() {
        return persitenceService;
    }

    protected TestLogBuilderFactory getLogModelBuilderFactory() {
        if (logModelBuilderFactory == null) {
            logModelBuilderFactory = new TestLogBuilderFactory();
        }
        return logModelBuilderFactory;
    }

    private enum HumanRecordType {
        updateHuman
    }

}
