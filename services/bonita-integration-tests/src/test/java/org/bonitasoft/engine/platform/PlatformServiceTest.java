package org.bonitasoft.engine.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Test;

public class PlatformServiceTest extends CommonServiceTest {

    private final static String STATUS_DEACTIVATED = "DEACTIVATED";

    @Override
    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
    }

    private void createDefaultPlatform() throws Exception {
        getTransactionService().begin();
        final SPlatform platform = getPlatformBuilder().createNewInstance("defaultVersion", "previousVersion", "initialVersion", "defaultUser",
                System.currentTimeMillis()).done();
        getPlatformService().createPlatformTables();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().createPlatform(platform);
        getTransactionService().complete();
    }

    @Test
    public void testPlatformBuilder() throws Exception {
        final String version = "myVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();
        final String initialVersion = "initialVersion";
        final String previousVersion = "previousVersion";

        getPlatformBuilder().createNewInstance(version, previousVersion, initialVersion, createdBy, created);

        final SPlatform platform = getPlatformBuilder().done();

        assertEquals(version, platform.getVersion());
        assertEquals(createdBy, platform.getCreatedBy());
        assertEquals(created, platform.getCreated());
        assertEquals(initialVersion, platform.getInitialVersion());
        assertEquals(previousVersion, platform.getPreviousVersion());
    }

    @Test
    public void testCreatePlatform() throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        getTransactionService().begin();

        final SPlatform platform = getPlatformBuilder().createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
        getPlatformService().createPlatformTables();

        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().createPlatform(platform);
        final SPlatform readPlatform = getPlatformService().getPlatform();
        assertEquals(platform.getVersion(), readPlatform.getVersion());
        assertEquals(platform.getCreatedBy(), readPlatform.getCreatedBy());
        assertEquals(platform.getCreated(), readPlatform.getCreated());

        try {
            getPlatformService().createPlatformTables();
            fail("Platform alreadyExist...");
        } catch (final SPlatformAlreadyExistException e) {
            // OK
        }
        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();

        getTransactionService().begin();
        // check the platform was well deleted and try to recreate it
        getPlatformService().createPlatformTables();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testGetPlatform() throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        getTransactionService().begin();

        try {
            getPlatformService().getPlatform();
        } catch (final SPlatformNotFoundException e) {
            // OK
        }

        final SPlatform platform = getPlatformBuilder().createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
        getPlatformService().createPlatformTables();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().createPlatform(platform);
        assertNotNull(getPlatformService().getPlatform());

        getPlatformService().deletePlatform();

        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testUpdatePlatform() throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        getTransactionService().begin();

        SPlatform platform = getPlatformBuilder().createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
        final EntityUpdateDescriptor dummyDescriptor = new EntityUpdateDescriptor();
        try {
            getPlatformService().updatePlatform(platform, dummyDescriptor);
            fail("Platform does not exist. Could not be updated.");
        } catch (final SPlatformUpdateException e) {

        }

        getPlatformService().createPlatformTables();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().createPlatform(platform);
        final SPlatform readPlatform = getPlatformService().getPlatform();
        assertEquals(platform.getVersion(), readPlatform.getVersion());
        assertEquals(platform.getCreatedBy(), readPlatform.getCreatedBy());
        assertEquals(platform.getCreated(), readPlatform.getCreated());

        final String newCreatedBy = "newCreatedBy";
        final String newInitialVersion = "initialVersion";
        final String newPreviousVersion = "previousVersion";
        final String newVersion = "newVersion";
        final long newCreated = System.currentTimeMillis();

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(getPlatformBuilder().getCreatedByKey(), newCreatedBy);
        updateDescriptor.addField(getPlatformBuilder().getInitialVersionKey(), newInitialVersion);
        updateDescriptor.addField(getPlatformBuilder().getPreviousVersionKey(), newPreviousVersion);
        updateDescriptor.addField(getPlatformBuilder().getVersionKey(), newVersion);
        updateDescriptor.addField(getPlatformBuilder().getCreatedKey(), newCreated);

        getPlatformService().updatePlatform(platform, updateDescriptor);

        assertEquals(newCreatedBy, platform.getCreatedBy());
        assertEquals(newInitialVersion, platform.getInitialVersion());
        assertEquals(newPreviousVersion, platform.getPreviousVersion());
        assertEquals(newVersion, platform.getVersion());
        assertEquals(newCreated, platform.getCreated());

        platform = getPlatformService().getPlatform();

        assertEquals(newCreatedBy, platform.getCreatedBy());
        assertEquals(newInitialVersion, platform.getInitialVersion());
        assertEquals(newPreviousVersion, platform.getPreviousVersion());
        assertEquals(newVersion, platform.getVersion());
        assertEquals(newCreated, platform.getCreated());

        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testgetTenantBuilder() throws Exception {
        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();
        final String description = "description";

        getTenantBuilder().createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false);
        getTenantBuilder().setDescription(description);

        final STenant tenant = getTenantBuilder().done();

        assertEquals(name, tenant.getName());
        assertEquals(createdBy, tenant.getCreatedBy());
        assertEquals(created, tenant.getCreated());
        assertEquals(description, tenant.getDescription());
    }

    @Test
    public void testCreateTenant() throws Exception {
        createDefaultPlatform();

        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = getTenantBuilder().createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();

        getPlatformService().createTenant(tenant);

        final STenant readTenant = getPlatformService().getTenant(tenant.getId());

        assertEquals(tenant.getName(), readTenant.getName());
        assertEquals(tenant.getCreatedBy(), readTenant.getCreatedBy());
        assertEquals(tenant.getCreated(), readTenant.getCreated());

        try {
            getPlatformService().createTenant(tenant);
            fail("Tenant alreadyExist...");
        } catch (final STenantAlreadyExistException e) {
            // OK
        }
        getTransactionService().complete();

        deleteTenant(tenant.getId());

        getTransactionService().begin();
        // check the tenant was well deleted and try to recreate it
        getPlatformService().createTenant(getTenantBuilder().createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done());
        getTransactionService().complete();

        deleteTenant(tenant.getId());

        getTransactionService().begin();
        getPlatformService().deletePlatform();
        getTransactionService().complete();

        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testUpdateTenant() throws Exception {
        createDefaultPlatform();

        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = getTenantBuilder().createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();

        getPlatformService().createTenant(tenant);

        assertEquals(name, tenant.getName());
        assertEquals(createdBy, tenant.getCreatedBy());
        assertEquals(created, tenant.getCreated());
        assertNull(tenant.getDescription());

        final String newCreatedBy = "newCreatedBy";
        final long newCreated = System.currentTimeMillis();
        final String newDescription = "newDescription";
        final String newName = "newName";

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(getTenantBuilder().getCreatedByKey(), newCreatedBy);
        updateDescriptor.addField(getTenantBuilder().getCreatedKey(), newCreated);
        updateDescriptor.addField(getTenantBuilder().getDescriptionKey(), newDescription);
        updateDescriptor.addField(getTenantBuilder().getNameKey(), newName);

        getPlatformService().updateTenant(tenant, updateDescriptor);

        assertEquals(newName, tenant.getName());
        assertEquals(newCreatedBy, tenant.getCreatedBy());
        assertEquals(newCreated, tenant.getCreated());
        assertEquals(newDescription, tenant.getDescription());

        final STenant readTenant = getPlatformService().getTenant(tenant.getId());

        assertEquals(newName, readTenant.getName());
        assertEquals(newCreatedBy, readTenant.getCreatedBy());
        assertEquals(newCreated, readTenant.getCreated());
        assertEquals(newDescription, readTenant.getDescription());
        getTransactionService().complete();

        deleteTenant(tenant.getId());

        getTransactionService().begin();
        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testUpdateTenantTenantNotFoundException() throws Exception {
        createDefaultPlatform();

        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = getTenantBuilder().createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();

        try {
            getPlatformService().updateTenant(tenant, updateDescriptor);
            fail("Tenant does not exists!");
        } catch (final STenantUpdateException e) {
            // OK
        }

        getPlatformService().deletePlatform();
        getTransactionService().complete();

        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testUpdateTenantTenantAlreadyExistException() throws Exception {
        createDefaultPlatform();

        final String tenant1Name = "tenant1";
        final String tenant2Name = "tenant2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = getTenantBuilder().createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false).done();
        final STenant tenant2 = getTenantBuilder().createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();

        getPlatformService().createTenant(tenant1);
        getPlatformService().createTenant(tenant2);

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(getTenantBuilder().getNameKey(), tenant1Name);

        try {
            getPlatformService().updateTenant(tenant2, updateDescriptor);
            fail("should not ne able to update the tenant with a name that already exists");
        } catch (final STenantUpdateException e) {
            // OK
        }

        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testGetTenantByName() throws Exception {
        createDefaultPlatform();

        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = getTenantBuilder().createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();

        getPlatformService().createTenant(tenant);

        final STenant readTenant = getPlatformService().getTenantByName(name);

        assertEquals(tenant.getName(), readTenant.getName());
        assertEquals(tenant.getCreatedBy(), readTenant.getCreatedBy());
        assertEquals(tenant.getCreated(), readTenant.getCreated());
        getTransactionService().complete();

        deleteTenant(tenant.getId());

        getTransactionService().begin();
        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    @Test
    public void testGetTenants() throws Exception {
        createDefaultPlatform();

        final String tenant1Name = "tenant1";
        final String tenant2Name = "tenant2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = getTenantBuilder().createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false).done();
        final STenant tenant2 = getTenantBuilder().createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();

        getPlatformService().createTenant(tenant1);
        getPlatformService().createTenant(tenant2);

        final List<OrderByOption> orderbyOptions = new ArrayList<OrderByOption>();
        orderbyOptions.add(new OrderByOption(STenant.class, getTenantBuilder().getNameKey(), OrderByType.DESC));
        final QueryOptions queryOptions = new QueryOptions(0, 20, orderbyOptions);

        List<STenant> readTenants = null;

        readTenants = getPlatformService().getTenants(queryOptions);
        assertEquals(2, readTenants.size());
        assertEquals(tenant2.getId(), readTenants.get(0));
        assertEquals(tenant1.getId(), readTenants.get(1));

        final Collection<Long> ids = new ArrayList<Long>();
        ids.add(tenant1.getId());
        ids.add(tenant2.getId());

        readTenants = getPlatformService().getTenants(ids, queryOptions);
        assertEquals(2, readTenants.size());
        assertEquals(tenant2.getId(), readTenants.get(0));
        assertEquals(tenant1.getId(), readTenants.get(1));

        ids.clear();
        ids.add(tenant1.getId());
        readTenants = getPlatformService().getTenants(ids, queryOptions);
        assertEquals(1, readTenants.size());
        assertEquals(tenant1.getId(), readTenants.get(0));
        getTransactionService().complete();

        deleteTenant(tenant1.getId());
        deleteTenant(tenant2.getId());

        getTransactionService().begin();
        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

    /**
     * @param id
     * @throws Exception
     */
    private void deleteTenant(final long id) throws Exception {
        // delete tenant objects
        getTransactionService().begin();
        getPlatformService().deleteTenantObjects(id);
        getTransactionService().complete();

        // delete tenant
        getTransactionService().begin();
        getPlatformService().deleteTenant(id);
        getTransactionService().complete();
    }

    @Test
    public void testSearchTenants() throws Exception {
        createDefaultPlatform();

        final String tenant1Name = "tenant1";
        final String tenant2Name = "tenant2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = getTenantBuilder().createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false).done();
        final STenant tenant2 = getTenantBuilder().createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();

        getPlatformService().createTenant(tenant1);
        getPlatformService().createTenant(tenant2);

        // sort
        final List<OrderByOption> orderbyOptions = new ArrayList<OrderByOption>();
        orderbyOptions.add(new OrderByOption(STenant.class, getTenantBuilder().getNameKey(), OrderByType.DESC));
        final QueryOptions queryOptions = new QueryOptions(0, 10, orderbyOptions);

        final List<STenant> readTenants = getPlatformService().searchTenants(queryOptions);
        assertEquals(2, readTenants.size());
        assertEquals(tenant2.getId(), readTenants.get(0));
        assertEquals(tenant1.getId(), readTenants.get(1));
        getTransactionService().complete();

        deleteTenant(tenant1.getId());
        deleteTenant(tenant2.getId());

        getTransactionService().begin();
        getPlatformService().deletePlatform();
        getTransactionService().complete();
        getTransactionService().begin();
        getPlatformService().deletePlatformTables();
        getTransactionService().complete();
    }

}
