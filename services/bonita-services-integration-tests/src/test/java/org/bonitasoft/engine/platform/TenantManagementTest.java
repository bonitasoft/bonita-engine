package org.bonitasoft.engine.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Test;

public class TenantManagementTest extends CommonServiceTest {

    private final static String STATUS_DEACTIVATED = "DEACTIVATED";

    @After
    public void cleanUpOpenTransaction() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
    }

    @Test
    public void tenantBuilderShouldBuildValidTenant() {
        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();
        final String description = "description";

        final STenantBuilder sTenantBuilder = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(name, createdBy, created, STATUS_DEACTIVATED,
                false);
        sTenantBuilder.setDescription(description);

        final STenant tenant = sTenantBuilder.done();

        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getCreatedBy()).isEqualTo(createdBy);
        assertThat(tenant.getCreated()).isEqualTo(created);
        assertThat(tenant.getDescription()).isEqualTo(description);
    }

    @Test(expected = STenantAlreadyExistException.class)
    public void cannotCreateSecondTenantWithSameName() throws Exception {
        STenant sTenant = BuilderFactory.get(STenantBuilderFactory.class)
                .createNewInstance(PlatformUtil.DEFAULT_TENANT_NAME, "anyone", System.currentTimeMillis(), STATUS_DEACTIVATED, false).done();
        getTransactionService().begin();
        try {
            getPlatformService().createTenant(sTenant);
            fail("Tenant should already exist...");
        } finally {
            getTransactionService().complete();
        }

    }

    @Test
    public void newlyCreatedTenantShouldBeAvailableForRetrieve() throws Exception {
        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();
        long tenantId = getPlatformService().createTenant(tenant);
        getTransactionService().complete();

        getTransactionService().begin();
        final STenant readTenant = getPlatformService().getTenant(tenantId);
        getTransactionService().complete();

        assertThat(readTenant.getName()).isEqualTo(tenant.getName());
        assertThat(readTenant.getCreatedBy()).isEqualTo(tenant.getCreatedBy());
        assertThat(readTenant.getCreated()).isEqualTo(tenant.getCreated());

        deleteTenant(tenant.getId());
    }

    @Test(expected = STenantNotFoundException.class)
    public void deletedTenantShouldNotBeAvailableAnymore() throws Exception {
        getTransactionService().begin();
        long tenantId = getPlatformService().createTenant(
                BuilderFactory.get(STenantBuilderFactory.class)
                        .createNewInstance("deletedTenantShouldNotBeAvailableAnymore", "created by me", System.currentTimeMillis(), STATUS_DEACTIVATED, false)
                        .done());
        getTransactionService().complete();

        getTransactionService().begin();
        getPlatformService().deleteTenant(tenantId);
        getTransactionService().complete();

        // check the tenant was well deleted by trying to delete it again:
        getTransactionService().begin();
        getPlatformService().deleteTenant(tenantId);
        getTransactionService().complete();
    }

    @Test
    public void updateTenantShouldUpdateAllFields() throws Exception {
        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();
        getPlatformService().createTenant(tenant);

        final String newCreatedBy = "newCreatedBy";
        final long newCreated = System.currentTimeMillis();
        final String newDescription = "newDescription";
        final String newName = "newName";

        final STenantBuilderFactory sTenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(sTenantBuilderFact.getCreatedByKey(), newCreatedBy);
        updateDescriptor.addField(sTenantBuilderFact.getCreatedKey(), newCreated);
        updateDescriptor.addField(sTenantBuilderFact.getDescriptionKey(), newDescription);
        updateDescriptor.addField(sTenantBuilderFact.getNameKey(), newName);

        getPlatformService().updateTenant(tenant, updateDescriptor);
        getTransactionService().complete();

        getTransactionService().begin();
        final STenant readTenant = getPlatformService().getTenant(tenant.getId());
        getTransactionService().complete();

        assertThat(readTenant.getName()).isEqualTo(newName);
        assertThat(readTenant.getCreatedBy()).isEqualTo(newCreatedBy);
        assertThat(readTenant.getCreated()).isEqualTo(newCreated);
        assertThat(readTenant.getDescription()).isEqualTo(newDescription);

        deleteTenant(tenant.getId());
    }

    @Test(expected = STenantUpdateException.class)
    public void updateInexistantTenantShouldFail() throws Exception {
        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();
        long createdTenant = getPlatformService().createTenant(tenant);
        getPlatformService().deleteTenant(createdTenant);

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();

        try {
            getPlatformService().updateTenant(tenant, updateDescriptor);
            fail("Tenant update should not work on inexistant tenant");
        } finally {
            getTransactionService().complete();
        }
    }

    @Test(expected = STenantUpdateException.class)
    public void updatingTenantNameWithAlreadyExistingShouldFail() throws Exception {
        final String tenant1Name = "tenant1";
        final String tenant2Name = "tenant2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();
        final STenant tenant2 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();

        getTransactionService().begin();

        long tenantId1 = getPlatformService().createTenant(tenant1);
        long tenantId2 = getPlatformService().createTenant(tenant2);

        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(BuilderFactory.get(STenantBuilderFactory.class).getNameKey(), tenant1Name);

        try {
            getPlatformService().updateTenant(tenant2, updateDescriptor);
            fail("should not ne able to update the tenant with a name that already exists");
        } finally {
            getTransactionService().complete();
            getTransactionService().begin();
            getPlatformService().deleteTenant(tenantId1);
            getPlatformService().deleteTenant(tenantId2);
            getTransactionService().complete();
        }
    }

    @Test(expected = STenantNotFoundException.class)
    public void shouldNotBeAbleToRetrieveUnknownTenantByName() throws Exception {
        getTransactionService().begin();
        try {
            getPlatformService().getTenantByName("probably_not_existing");
        } finally {
            getTransactionService().complete();
        }

    }

    @Test
    public void shouldBeAbleToRetrieveTenantByName() throws Exception {
        final String name = "tenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();
        getPlatformService().createTenant(tenant);
        getTransactionService().complete();

        getTransactionService().begin();
        final STenant readTenant = getPlatformService().getTenantByName(name);
        getTransactionService().complete();

        assertThat(readTenant.getName()).isEqualTo(tenant.getName());

        deleteTenant(tenant.getId());
    }

    @Test
    public void getTenantsShouldFilterOnSearchCriteria() throws Exception {
        final String tenant1Name = "tenant1";
        final String tenant2Name = "tenant2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();
        final STenant tenant2 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();

        getTransactionService().begin();
        getPlatformService().createTenant(tenant1);
        getPlatformService().createTenant(tenant2);
        getTransactionService().complete();

        try {
            final List<OrderByOption> orderbyOptions = new ArrayList<OrderByOption>();
            orderbyOptions.add(new OrderByOption(STenant.class, BuilderFactory.get(STenantBuilderFactory.class).getIdKey(), OrderByType.DESC));
            final QueryOptions queryOptions = new QueryOptions(0, 20, orderbyOptions);

            getTransactionService().begin();
            List<STenant> readTenants = getPlatformService().getTenants(queryOptions);
            getTransactionService().complete();

            // count also default tenant:
            assertThat(readTenants.size()).isEqualTo(3);
            assertThat(readTenants.get(0).getId()).isEqualTo(tenant2.getId());
            assertThat(readTenants.get(1).getId()).isEqualTo(tenant1.getId());

            final Collection<Long> ids = new ArrayList<Long>();
            ids.add(tenant1.getId());
            ids.add(tenant2.getId());

            getTransactionService().begin();
            readTenants = getPlatformService().getTenants(ids, queryOptions);
            getTransactionService().complete();

            assertThat(readTenants.size()).isEqualTo(2);
            assertThat(readTenants.get(0).getId()).isEqualTo(tenant2.getId());
            assertThat(readTenants.get(1).getId()).isEqualTo(tenant1.getId());

            ids.clear();
            ids.add(tenant1.getId());
            getTransactionService().begin();
            readTenants = getPlatformService().getTenants(ids, queryOptions);
            getTransactionService().complete();

            assertThat(readTenants.size()).isEqualTo(1);
            assertThat(readTenants.get(0).getId()).isEqualTo(tenant1.getId());
        } finally {
            deleteTenant(tenant1.getId());
            deleteTenant(tenant2.getId());
        }
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
    public void searchTenants() throws Exception {
        final String tenant1Name = "tenant1";
        final String tenant2Name = "tenant2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();
        final STenant tenant2 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();

        getTransactionService().begin();

        getPlatformService().createTenant(tenant1);
        getPlatformService().createTenant(tenant2);

        // sort
        final List<OrderByOption> orderbyOptions = new ArrayList<OrderByOption>();
        orderbyOptions.add(new OrderByOption(STenant.class, BuilderFactory.get(STenantBuilderFactory.class).getIdKey(), OrderByType.DESC));
        final QueryOptions queryOptions = new QueryOptions(0, 10, orderbyOptions);

        final List<STenant> readTenants = getPlatformService().searchTenants(queryOptions);
        // count also default tenant:
        assertThat(readTenants.size()).isEqualTo(3);
        assertThat(readTenants.get(0).getId()).isEqualTo(tenant2.getId());
        assertThat(readTenants.get(1).getId()).isEqualTo(tenant1.getId());
        getTransactionService().complete();

        deleteTenant(tenant1.getId());
        deleteTenant(tenant2.getId());
    }

}
