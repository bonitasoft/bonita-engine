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
package org.bonitasoft.engine.persistence;

import static org.junit.Assert.*;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.persistence.model.Parent;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.Test;

public class MultiTenancyTest extends CommonBPMServicesTest {

    public static final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    private ReadPersistenceService persistenceService;
    private Recorder recorder;

    public MultiTenancyTest() {
        persistenceService = getTenantAccessor().getReadPersistenceService();
        recorder = getTenantAccessor().getRecorder();
    }

    @Override
    public void after() throws Exception {
        changeTenant(getDefaultTenantId());
    }

    @Test
    public void testMultiSchema() throws Exception {
        final String firstName = "firstName";
        final String lastName = "lastName";
        final int age = 12;

        final Human human1 = PersistenceTestUtil.buildHuman(firstName, lastName, age);

        getTransactionService().begin();

        try {
            PersistenceTestUtil.checkHuman(human1, persistenceService.selectById(new SelectByIdDescriptor<Human>("getHumanById", Human.class, human1.getId())));
            fail("human1 must not be found in tenant2");
        } catch (final AssertionError e) {
            // OK
        }

        assertEquals(
                0,
                persistenceService.selectList(
                        new SelectListDescriptor<Human>("getHumanByFirstName", PersistenceTestUtil.getMap("firstName", firstName), Human.class,
                                new QueryOptions(0, 20, Human.class, "id", OrderByType.ASC))).size());

        recorder.recordInsert(new InsertRecord(human1), null);
        PersistenceTestUtil.checkHuman(human1, persistenceService.selectById(new SelectByIdDescriptor<Human>("getHumanById", Human.class, human1.getId())));

        assertEquals(
                1,
                persistenceService.selectList(
                        new SelectListDescriptor<Human>("getHumanByFirstName", PersistenceTestUtil.getMap("firstName", firstName), Human.class,
                                new QueryOptions(0, 20, Human.class, "id", OrderByType.ASC))).size());

        recorder.recordDelete(new DeleteRecord(human1), null);

        getTransactionService().complete();
    }

    @Test
    public void testIfOneFailAllFail() throws Exception {
        // Initialize

        String tenantName = "testIfOneFailAllFail";
        final long tenant1Id = createTenant(tenantName);
        changeTenant(tenant1Id);
        getTransactionService().begin();

        final Parent parent = PersistenceTestUtil.buildParent("parent1FN", "parent1LN", 45);
        recorder.recordInsert(new InsertRecord(parent), null);
        try {
            persistenceService.selectOne(new SelectOneDescriptor<Parent>("wrong query", null, Parent.class));
            fail("Exception expected");
        } catch (final Exception e) {
            getTransactionService().setRollbackOnly();
        } finally {
            getTransactionService().complete();
        }

        getTransactionService().begin();
        try {
            assertNull(persistenceService.selectOne(new SelectOneDescriptor<Human>("getHumanById", PersistenceTestUtil.getMap("id", parent.getId()),
                    Human.class)));
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testMultiTenant() throws Exception {
        final long tenant1Id = createTenant("tenant1");
        final long tenant2Id = createTenant("tenant2");
        try {
            changeTenant(tenant1Id);

            final String firstName = "firstName";
            final String lastName = "lastName";
            final int age = 12;

            final Human human1 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            final Human human2 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            getTransactionService().begin();
            recorder.recordInsert(new InsertRecord(human1), null);
            PersistenceTestUtil.checkHuman(human1, persistenceService.selectById(new SelectByIdDescriptor<>("getHumanById", Human.class, human1.getId())));
            getTransactionService().complete();

            changeTenant(tenant2Id);
            getTransactionService().begin();
            try {
                PersistenceTestUtil.checkHuman(human1,
                        persistenceService.selectById(new SelectByIdDescriptor<>("getHumanById", Human.class, human1.getId())));
                fail("human1 must not be found in tenant1");
            } catch (final AssertionError e) {
                // OK
            }
            recorder.recordInsert(new InsertRecord(human2), null);
            getTransactionService().complete();

            changeTenant(tenant1Id);
            getTransactionService().begin();
            PersistenceTestUtil.checkHuman(human1, persistenceService.selectById(new SelectByIdDescriptor<>("getHumanById", Human.class, human1.getId())));
            try {
                PersistenceTestUtil.checkHuman(human2,
                        persistenceService.selectById(new SelectByIdDescriptor<>("getHumanById", Human.class, human2.getId())));
                fail("human1 must not be found in default");
            } catch (final AssertionError e) {
                // OK
            }
            getTransactionService().complete();
        } finally {
            TestUtil.closeTransactionIfOpen(getTransactionService());
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformAccessor().getPlatformService(), tenant1Id);
        }
    }

    @Test
    public void testSearchWith3Tenants() throws Exception {
        final long tenant1Id = createTenant("testSearchWith3Tenants1");
        final long tenant2Id = createTenant("testSearchWith3Tenants2");
        final long tenant3Id = createTenant("testSearchWith3Tenants3");
        try {
            changeTenant(tenant1Id);
            getTransactionService().begin();
            final Human human1 = PersistenceTestUtil.buildHuman("default", "lastName", 45);
            recorder.recordInsert(new InsertRecord(human1), null);
            getTransactionService().complete();

            changeTenant(tenant2Id);
            getTransactionService().begin();
            final Human human2 = PersistenceTestUtil.buildHuman("tenant1", "lastName", 32);
            recorder.recordInsert(new InsertRecord(human2), null);
            getTransactionService().complete();

            changeTenant(tenant3Id);
            getTransactionService().begin();
            final Human human3 = PersistenceTestUtil.buildHuman("tenant2", "lastName", 12);
            recorder.recordInsert(new InsertRecord(human3), null);
            getTransactionService().complete();
        } finally {
            TestUtil.closeTransactionIfOpen(getTransactionService());
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformAccessor().getPlatformService(), tenant1Id);
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformAccessor().getPlatformService(), tenant2Id);
        }
    }

    @Test
    public void sequenceWithMultiTenancy() throws Exception {

        final long tenant1Id = createTenant("sequenceWithMultiTenancy1");
        final long tenant2Id = createTenant("sequenceWithMultiTenancy2");

        try {
            changeTenant(tenant1Id);
            final String firstName = "firstName";
            final String lastName = "lastName";
            final int age = 12;

            getTransactionService().begin();
            final Human human1 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            recorder.recordInsert(new InsertRecord(human1), null);
            assertEquals(1, human1.getId());
            getTransactionService().complete();
            changeTenant(tenant2Id);

            getTransactionService().begin();
            final Long nbOfHuman = persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfHumans", null, Human.class, Long.class));
            getTransactionService().complete();
            getTransactionService().begin();
            final Human human2 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            recorder.recordInsert(new InsertRecord(human2), null);
            assertEquals(1, human2.getId());// not the same sequence as tenant 1

            getTransactionService().complete();
            for (int i = 0; i < 150; i++) {// this should not cause constraint violation
                getTransactionService().begin();
                final Human human = PersistenceTestUtil.buildHuman(firstName, lastName, age);
                recorder.recordInsert(new InsertRecord(human), null);
                getTransactionService().complete();
            }
            getTransactionService().begin();
            assertEquals(Long.valueOf(151 + nbOfHuman),
                    persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfHumans", null, Human.class, Long.class)));
            getTransactionService().complete();
        } finally {
            TestUtil.closeTransactionIfOpen(getTransactionService());
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformAccessor().getPlatformService(), tenant1Id);
        }
    }
}
