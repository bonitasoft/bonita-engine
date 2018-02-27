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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.model.TestLogBuilderFactory;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros, Yanyan Liu
 */
public class RecorderTest extends CommonBPMServicesTest {

    private static final String TEST_DELETE = "TEST_DELETED";

    private static final String TEST_UPDATE = "TEST_UPDATED";

    private static final String TEST_CREATED = "TEST_CREATED";

    private final static int SLEEP_TIME = 200;

    private final static String SUSER_IMPL = "SUSER_IMPL";

    protected ReadPersistenceService persitenceService;

    protected Recorder recorder;

    protected SchedulerService scheduler;

    protected EventService eventService;

    protected QueriableLoggerService loggerService;

    private TestLogBuilderFactory logModelBuilderFactory;

    @Before
    public void setUp() throws Exception {
        persitenceService = getTenantAccessor().getReadPersistenceService();
        recorder = getTenantAccessor().getRecorder();
        scheduler = getTenantAccessor().getSchedulerService();
        eventService = getTenantAccessor().getEventService();
        loggerService = getTenantAccessor().getQueriableLoggerService();
        TestUtil.startScheduler(scheduler);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
        TestUtil.stopScheduler(scheduler, getTransactionService());
    }

    private SUserImpl buildSUserImpl(final String firstName, final String lastName) {
        final SUserImpl sUserImpl = new SUserImpl();
        sUserImpl.setUserName(firstName);
        sUserImpl.setPassword(lastName);
        return sUserImpl;
    }

    private List<SQueriableLog> getLogs(final long indexValue, final String actionType) throws SBonitaReadException {
        final List<FilterOption> filters = new ArrayList<>(2);
        filters.add(getActionTypeFilterOption(actionType));
        filters.add(new FilterOption(SQueriableLog.class, getLogModelBuilderFactory().getObjectIdKey(), indexValue));
        final List<OrderByOption> orders = Collections.singletonList(new OrderByOption(SQueriableLog.class, "id", OrderByType.ASC));
        final QueryOptions opts = new QueryOptions(0, 10, orders, filters, null);
        return loggerService.searchLogs(opts);
    }

    private FilterOption getActionTypeFilterOption(final String actionType) {
        return new FilterOption(SQueriableLog.class, getLogModelBuilderFactory().getActionTypeKey(), actionType);
    }

    private List<SQueriableLog> getLogs(final String actionType) throws SBonitaReadException {
        final List<FilterOption> filters = Collections.singletonList(getActionTypeFilterOption(actionType));
        final List<OrderByOption> orders = Collections.singletonList(new OrderByOption(SQueriableLog.class, "id", OrderByType.ASC));
        return loggerService.searchLogs(new QueryOptions(0, 10, orders, filters, null));
    }

    private void checkSUserImpl(final SUserImpl expectedSUserImpl, final SUserImpl retrievedSUserImpl) {
        assertNotNull(retrievedSUserImpl);
        assertEquals(expectedSUserImpl, retrievedSUserImpl);
    }

    private SUserImpl getUserByUsername(final String firstName) throws SBonitaReadException {
        return getPersistenceService()
                .selectOne(new SelectOneDescriptor<SUserImpl>("getUserByUserName", Collections.singletonMap("userName", (Object) firstName), SUserImpl.class));
    }

    @Test
    public void testNotLogOnInsertRecordWhenBTXRolledBack() throws Exception {
        getTransactionService().begin();
        final SelectOneDescriptor<SUserImpl> selectDescriptor = new SelectOneDescriptor<>("getUserByUserName",
                (Map<String, Object>) Collections.singletonMap("userName", (Object) "firstName"), SUserImpl.class);
        SUserImpl retrievedSUserImpl = getPersistenceService().selectOne(selectDescriptor);
        assertNull("Should not have any SUSER_IMPL in DB before test", retrievedSUserImpl);

        final String firstName = "Laurent";
        final SUserImpl sUserImpl = buildSUserImpl(firstName, "Vaills");
        recorder.recordInsert(new InsertRecord(sUserImpl), SUSER_IMPL);

        // set rollback
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        Thread.sleep(SLEEP_TIME);

        // The transaction has been rolled back no SUserImpl nor log should have been inserted.
        getTransactionService().begin();

        retrievedSUserImpl = getPersistenceService().selectOne(selectDescriptor);
        assertNull(retrievedSUserImpl);

        final List<SQueriableLog> retrievedLogs = getLogs(TEST_CREATED);
        assertEquals(0, retrievedLogs.size());

        getTransactionService().complete();
    }

    @Test
    public void testNotLogOnUpdateRecordWhenBTXRolledBack() throws Exception {
        // add sUserImpl using persistence service
        getTransactionService().begin();

        final SUserImpl sUserImpl = buildSUserImpl("firstName", "lastName");
        recorder.recordInsert(new InsertRecord(sUserImpl), SUSER_IMPL);
        getTransactionService().complete();

        // update sUserImpl (fail)
        getTransactionService().begin();

        final SUserImpl sUserImplToUpdate = getUserByUsername("firstName");
        assertNotNull(sUserImplToUpdate);
        final Map<String, Object> stringObjectMap = Collections.<String, Object> singletonMap("userName", "firstName");
        recorder.recordUpdate(UpdateRecord.buildSetFields(sUserImplToUpdate, stringObjectMap), SUSER_IMPL);
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        // wait
        Thread.sleep(SLEEP_TIME);

        // query
        getTransactionService().begin();
        final SUserImpl retrivedSUserImpl = getPersistenceService().selectOne(
                new SelectOneDescriptor<SUserImpl>("getUserByUserName", Collections.singletonMap("userName", (Object) "firstNameUpdate"), SUserImpl.class));
        assertNull(retrivedSUserImpl);

        final List<SQueriableLog> retrievedLogs = getLogs(TEST_UPDATE);
        assertEquals(0, retrievedLogs.size());

        final SUserImpl addedSUserImpl = getUserByUsername("firstName");
        recorder.recordDelete(new DeleteRecord(addedSUserImpl), SUSER_IMPL);

        getTransactionService().complete();
    }

    @Test
    public void testNotLogOnDeleteRecordIfBTXRolledBack() throws Exception {
        // add sUserImpl using persistence service
        getTransactionService().begin();
        final SUserImpl sUserImpl = buildSUserImpl("firstName", "lastName");
        recorder.recordInsert(new InsertRecord(sUserImpl), SUSER_IMPL);
        getTransactionService().complete();

        // delete sUserImpl using recorder
        getTransactionService().begin();

        final SUserImpl sUserImplToDelete = getUserByUsername("firstName");
        assertNotNull(sUserImplToDelete);
        recorder.recordDelete(new DeleteRecord(sUserImplToDelete), SUSER_IMPL);
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();

        // wait
        Thread.sleep(SLEEP_TIME);

        // query
        getTransactionService().begin();
        final SUserImpl retrievedSUserImpl = getUserByUsername("firstName");
        checkSUserImpl(sUserImpl, retrievedSUserImpl);

        final List<SQueriableLog> retrievedLogs = getLogs(retrievedSUserImpl.getId(), TEST_DELETE);
        assertEquals(0, retrievedLogs.size());

        getTransactionService().complete();

        // clean up:
        getTransactionService().begin();
        recorder.recordDelete(new DeleteRecord(retrievedSUserImpl), SUSER_IMPL);
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

}
