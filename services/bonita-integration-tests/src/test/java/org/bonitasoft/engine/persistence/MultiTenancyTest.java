package org.bonitasoft.engine.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.persistence.model.Human;
import org.bonitasoft.engine.persistence.model.Parent;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.Test;

public class MultiTenancyTest extends CommonServiceTest {

    public static final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    private static PersistenceService persistenceService;

    static {
        persistenceService = getServicesBuilder().buildJournal();
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
                                new QueryOptions(0, 20))).size());

        persistenceService.insert(human1);
        PersistenceTestUtil.checkHuman(human1, persistenceService.selectById(new SelectByIdDescriptor<Human>("getHumanById", Human.class, human1.getId())));

        assertEquals(
                1,
                persistenceService.selectList(
                        new SelectListDescriptor<Human>("getHumanByFirstName", PersistenceTestUtil.getMap("firstName", firstName), Human.class,
                                new QueryOptions(0, 20))).size());

        persistenceService.delete(human1);

        getTransactionService().complete();
    }

    @Test
    public void testIfOneFailAllFail() throws Exception {
        // Initialize
        TestUtil.deleteDefaultTenantAndPlatForm(getTransactionService(), getPlatformService(), getSessionAccessor(), getSessionService());
        TestUtil.createPlatformAndDefaultTenant(getTransactionService(), getPlatformService(), getSessionAccessor(),
                getSessionService());

        getTransactionService().begin();

        final Parent parent = PersistenceTestUtil.buildParent("parent1FN", "parent1LN", 45);
        persistenceService.insert(parent);
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

    private void changeTenant(final long tenantId) throws SSessionException, SessionIdNotSetException, Exception {
        getTransactionService().begin();
        TestUtil.createSessionOn(getSessionAccessor(), getSessionService(), tenantId);
        getTransactionService().complete();
    }

    @Test
    public void testMultiTenant() throws Exception {
        final long tenant1Id = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), "tenant1",
                PlatformUtil.DEFAULT_CREATED_BY, DEFAULT_TENANT_STATUS);
        try {
            final String firstName = "firstName";
            final String lastName = "lastName";
            final int age = 12;

            final Human human1 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            final Human human2 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            getTransactionService().begin();
            persistenceService.insert(human1);
            PersistenceTestUtil.checkHuman(human1, persistenceService.selectById(new SelectByIdDescriptor<Human>("getHumanById", Human.class, human1.getId())));
            getTransactionService().complete();

            changeTenant(tenant1Id);
            getTransactionService().begin();
            try {
                PersistenceTestUtil.checkHuman(human1,
                        persistenceService.selectById(new SelectByIdDescriptor<Human>("getHumanById", Human.class, human1.getId())));
                fail("human1 must not be found in tenant1");
            } catch (final AssertionError e) {
                // OK
            }
            persistenceService.insert(human2);
            getTransactionService().complete();

            // getTransactionService().begin();
            final long defaultTenantId = getDefaultTenantId();
            // getTransactionService().complete();
            changeTenant(defaultTenantId);
            getTransactionService().begin();
            PersistenceTestUtil.checkHuman(human1, persistenceService.selectById(new SelectByIdDescriptor<Human>("getHumanById", Human.class, human1.getId())));
            try {
                PersistenceTestUtil.checkHuman(human2,
                        persistenceService.selectById(new SelectByIdDescriptor<Human>("getHumanById", Human.class, human2.getId())));
                fail("human1 must not be found in default");
            } catch (final AssertionError e) {
                // OK
            }
            getTransactionService().complete();
        } finally {
            TestUtil.closeTransactionIfOpen(getTransactionService());
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenant1Id);
        }
    }

    @Test
    public void testSearchWith3Tenants() throws Exception {
        final long tenant1Id = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), "tenant2",
                PlatformUtil.DEFAULT_CREATED_BY, DEFAULT_TENANT_STATUS);
        final long tenant2Id = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), "tenant1",
                PlatformUtil.DEFAULT_CREATED_BY, DEFAULT_TENANT_STATUS);
        try {
            // getTransactionService().begin();
            final long defaultTenantId = getDefaultTenantId();
            // getTransactionService().complete();
            changeTenant(defaultTenantId);
            getTransactionService().begin();
            final Human human1 = PersistenceTestUtil.buildHuman("default", "lastName", 45);
            persistenceService.insert(human1);
            getTransactionService().complete();

            changeTenant(tenant1Id);
            getTransactionService().begin();
            final Human human2 = PersistenceTestUtil.buildHuman("tenant1", "lastName", 32);
            persistenceService.insert(human2);
            getTransactionService().complete();

            changeTenant(tenant2Id);
            getTransactionService().begin();
            final Human human3 = PersistenceTestUtil.buildHuman("tenant2", "lastName", 12);
            persistenceService.insert(human3);
            getTransactionService().complete();
        } finally {
            TestUtil.closeTransactionIfOpen(getTransactionService());
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenant1Id);
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenant2Id);
        }
    }

    @Test
    public void sequenceWithMultiTenancy() throws Exception {

        // Initialize
        TestUtil.deleteDefaultTenantAndPlatForm(getTransactionService(), getPlatformService(), getSessionAccessor(), getSessionService());
        TestUtil.createPlatformAndDefaultTenant(getTransactionService(), getPlatformService(), getSessionAccessor(),
                getSessionService());

        final long tenant1Id = PlatformUtil.createTenant(getTransactionService(), getPlatformService(), "tenant1",
                PlatformUtil.DEFAULT_CREATED_BY, DEFAULT_TENANT_STATUS);
        try {
            final String firstName = "firstName";
            final String lastName = "lastName";
            final int age = 12;

            getTransactionService().begin();
            final Human human1 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            persistenceService.insert(human1);
            assertEquals(1, human1.getId());
            getTransactionService().complete();
            changeTenant(tenant1Id);

            getTransactionService().begin();
            final Long nbOfHuman = persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfHumans", null, Human.class, Long.class));
            getTransactionService().complete();
            getTransactionService().begin();
            final Human human2 = PersistenceTestUtil.buildHuman(firstName, lastName, age);
            persistenceService.insert(human2);
            assertEquals(1, human2.getId());// not the same sequence as tenant 1

            getTransactionService().complete();
            for (int i = 0; i < 150; i++) {// this should not cause constraint violation
                getTransactionService().begin();
                final Human human = PersistenceTestUtil.buildHuman(firstName, lastName, age);
                persistenceService.insert(human);
                getTransactionService().complete();
            }
            getTransactionService().begin();
            assertEquals(Long.valueOf(151 + nbOfHuman),
                    persistenceService.selectOne(new SelectOneDescriptor<Long>("getNumberOfHumans", null, Human.class, Long.class)));
            getTransactionService().complete();
        } finally {
            TestUtil.closeTransactionIfOpen(getTransactionService());
            PlatformUtil.deleteTenant(getTransactionService(), getPlatformService(), tenant1Id);
        }
    }
}
