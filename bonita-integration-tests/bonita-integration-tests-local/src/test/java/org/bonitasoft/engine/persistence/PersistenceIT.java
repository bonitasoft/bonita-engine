/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.junit.Before;
import org.junit.Test;

public class PersistenceIT extends CommonBPMServicesTest {

    private ReadPersistenceService persistenceService;
    private Recorder recorder;

    @Before
    public void before() {
        persistenceService = getTenantAccessor().getReadPersistenceService();
        recorder = getTenantAccessor().getRecorder();
    }

    @Override
    public void after() throws Exception {
        changeTenant(getDefaultTenantId());
    }

    @Test
    public void testIfOneFailAllFail() throws Exception {
        // Initialize

        String tenantName = "testIfOneFailAllFail";
        final long tenant1Id = createTenant(tenantName);
        changeTenant(tenant1Id);
        getTransactionService().begin();

        final SUser SUser = buildSUserImpl("SUserImpl1FN", "SUserImpl1LN");
        recorder.recordInsert(new InsertRecord(SUser), "USER");
        try {
            persistenceService.selectOne(new SelectOneDescriptor<SUser>("wrong query", null, SUser.class));
            fail("Exception expected");
        } catch (final Exception e) {
            getTransactionService().setRollbackOnly();
        } finally {
            getTransactionService().complete();
        }
    }

    @Test
    public void testMultiTenant() throws Exception {
        final long tenant1Id = createTenant("tenant1");
        final long tenant2Id = createTenant("tenant2");
        changeTenant(tenant1Id);

        final SUser SUser1 = buildSUserImpl("user1Test", "password");
        final SUser SUser2 = buildSUserImpl("user2Test", "password");
        getTransactionService().begin();
        recorder.recordInsert(new InsertRecord(SUser1), "USER");
        checkSUserImpl(SUser1, persistenceService.selectById(new SelectByIdDescriptor<>(SUser.class, SUser1.getId())));
        getTransactionService().complete();

        changeTenant(tenant2Id);
        getTransactionService().begin();
        try {
            checkSUserImpl(SUser1,
                    persistenceService.selectById(new SelectByIdDescriptor<>(SUser.class, SUser1.getId())));
            fail("SUserImpl1 must not be found in tenant1");
        } catch (final AssertionError e) {
            // OK
        }
        recorder.recordInsert(new InsertRecord(SUser2), "USER");
        getTransactionService().complete();

        changeTenant(tenant1Id);
        getTransactionService().begin();
        checkSUserImpl(SUser1, persistenceService.selectById(new SelectByIdDescriptor<>(SUser.class, SUser1.getId())));
        try {
            checkSUserImpl(SUser2,
                    persistenceService.selectById(new SelectByIdDescriptor<>(SUser.class, SUser2.getId())));
            fail("SUserImpl1 must not be found in default");
        } catch (final AssertionError e) {
            // OK
        }
        getTransactionService().complete();
    }

    @Test
    public void testSearchWith3Tenants() throws Exception {
        final long tenant1Id = createTenant("testSearchWith3Tenants1");
        final long tenant2Id = createTenant("testSearchWith3Tenants2");
        final long tenant3Id = createTenant("testSearchWith3Tenants3");
        changeTenant(tenant1Id);
        getTransactionService().begin();
        final SUser SUser1 = buildSUserImpl("tenantUserTest", "password");
        recorder.recordInsert(new InsertRecord(SUser1), "USER");
        getTransactionService().complete();

        changeTenant(tenant2Id);
        getTransactionService().begin();
        final SUser SUser2 = buildSUserImpl("tenantUserTest", "password");
        recorder.recordInsert(new InsertRecord(SUser2), "USER");
        getTransactionService().complete();

        changeTenant(tenant3Id);
        getTransactionService().begin();
        final SUser SUser3 = buildSUserImpl("tenantUserTest", "password");
        recorder.recordInsert(new InsertRecord(SUser3), "USER");
        getTransactionService().complete();
    }

    @Test
    public void sequenceWithMultiTenancy() throws Exception {

        final long tenant1Id = createTenant("sequenceWithMultiTenancy1");
        final long tenant2Id = createTenant("sequenceWithMultiTenancy2");

        changeTenant(tenant1Id);
        final String firstName = "theName";
        final String lastName = "thePassword";

        getTransactionService().begin();
        final SUser SUser1 = buildSUserImpl(firstName, lastName);
        recorder.recordInsert(new InsertRecord(SUser1), "USER");
        assertEquals(1, SUser1.getId());
        getTransactionService().complete();
        changeTenant(tenant2Id);

        getTransactionService().begin();
        final Long nbOfSUserImpl = persistenceService
                .selectOne(new SelectOneDescriptor<>("getNumberOfSUser", null, SUser.class, Long.class));
        getTransactionService().complete();
        getTransactionService().begin();
        final SUser SUser2 = buildSUserImpl(firstName, lastName);
        recorder.recordInsert(new InsertRecord(SUser2), "USER");
        assertEquals(1, SUser2.getId());// not the same sequence as tenant 1

        getTransactionService().complete();
        for (int i = 0; i < 150; i++) {// this should not cause constraint violation
            getTransactionService().begin();
            final SUser SUser = buildSUserImpl(firstName + i, lastName);
            recorder.recordInsert(new InsertRecord(SUser), "USER");
            getTransactionService().complete();
        }
        getTransactionService().begin();
        assertEquals(Long.valueOf(151 + nbOfSUserImpl),
                persistenceService
                        .selectOne(new SelectOneDescriptor<>("getNumberOfSUser", null, SUser.class, Long.class)));
        getTransactionService().complete();
    }

    protected void checkSUserImpl(final SUser expected, final SUser actual) {
        assertThat(actual).as("SUserImpl").isNotNull();
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getPassword()).isEqualTo(expected.getPassword());
        assertThat(actual.getUserName()).isEqualTo(expected.getUserName());
    }

    protected SUser buildSUserImpl(final String username, final String password) {
        final SUser user = new SUser();
        user.setUserName(username);
        user.setPassword(password);
        return user;
    }

}
