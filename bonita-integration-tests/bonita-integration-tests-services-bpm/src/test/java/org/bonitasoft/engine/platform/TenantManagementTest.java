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
package org.bonitasoft.engine.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.exception.STenantAlreadyExistException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantUpdateException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Test;

public class TenantManagementTest extends CommonBPMServicesTest {

    private final static String STATUS_DEACTIVATED = "DEACTIVATED";

    private final PlatformService platformService;

    public TenantManagementTest() {
        platformService = getPlatformAccessor().getPlatformService();
    }

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
            platformService.createTenant(sTenant);
            fail("Tenant should already exist...");
        } finally {
            getTransactionService().complete();
        }

    }

    @Test
    public void newlyCreatedTenantShouldBeAvailableForRetrieve() throws Exception {
        final String name = "newlyCreatedTenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();
        long tenantId = platformService.createTenant(tenant);
        getTransactionService().complete();

        getTransactionService().begin();
        final STenant readTenant = platformService.getTenant(tenantId);
        getTransactionService().complete();

        assertThat(readTenant.getName()).isEqualTo(tenant.getName());
        assertThat(readTenant.getCreatedBy()).isEqualTo(tenant.getCreatedBy());
        assertThat(readTenant.getCreated()).isEqualTo(tenant.getCreated());

        deleteTenant(tenant.getId());
    }

    @Test(expected = STenantNotFoundException.class)
    public void deletedTenantShouldNotBeAvailableAnymore() throws Exception {
        getTransactionService().begin();
        long tenantId = platformService.createTenant(
                BuilderFactory.get(STenantBuilderFactory.class)
                        .createNewInstance("deletedTenantShouldNotBeAvailableAnymore", "created by me", System.currentTimeMillis(), STATUS_DEACTIVATED, false)
                        .done());
        getTransactionService().complete();

        getTransactionService().begin();
        platformService.deleteTenant(tenantId);
        getTransactionService().complete();

        // check the tenant was well deleted by trying to delete it again:
        getTransactionService().begin();
        platformService.deleteTenant(tenantId);
        getTransactionService().complete();
    }

    @Test
    public void updateTenantShouldUpdateAllFields() throws Exception {
        final String name = "updateTenant1";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(name, createdBy, created, STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();
        platformService.createTenant(tenant);

        final String newDescription = "newDescription";
        final String newName = "newName";

        final STenantUpdateBuilderFactory updateBuilderFactory = BuilderFactory.get(STenantUpdateBuilderFactory.class);
        final STenantUpdateBuilder updateDescriptor = updateBuilderFactory.createNewInstance();
        updateDescriptor.setDescription(newDescription);
        updateDescriptor.setName(newName);

        platformService.updateTenant(tenant, updateDescriptor.done());
        getTransactionService().complete();

        getTransactionService().begin();
        final STenant readTenant = platformService.getTenant(tenant.getId());
        getTransactionService().complete();

        assertThat(readTenant.getName()).isEqualTo(newName);
        assertThat(readTenant.getDescription()).isEqualTo(newDescription);

        deleteTenant(tenant.getId());
    }

    @Test(expected = STenantUpdateException.class)
    public void updateInexistantTenantShouldFail() throws Exception {
        final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance("tenant1", "mycreatedBy", System.currentTimeMillis(), STATUS_DEACTIVATED, false).done();

        getTransactionService().begin();
        final STenantUpdateBuilderFactory updateBuilderFactory = BuilderFactory.get(STenantUpdateBuilderFactory.class);
        final STenantUpdateBuilder updateDescriptor = updateBuilderFactory.createNewInstance();
        try {
            platformService.updateTenant(tenant, updateDescriptor.done());
            fail("Tenant update should not work on inexistant tenant");
        } finally {
            getTransactionService().complete();
        }
    }

    @Test(expected = STenantUpdateException.class)
    public void updatingTenantNameWithAlreadyExistingShouldFail() throws Exception {
        final String tenant1Name = "updatingTenantName1";
        final String tenant2Name = "updatingTenantName2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();
        final STenant tenant2 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();

        getTransactionService().begin();

        long tenantId1 = platformService.createTenant(tenant1);
        long tenantId2 = platformService.createTenant(tenant2);

        final STenantUpdateBuilderFactory updateBuilderFactory = BuilderFactory.get(STenantUpdateBuilderFactory.class);
        final STenantUpdateBuilder updateDescriptor = updateBuilderFactory.createNewInstance();
        updateDescriptor.setName(tenant1Name);

        try {
            platformService.updateTenant(tenant2, updateDescriptor.done());
            fail("should not ne able to update the tenant with a name that already exists");
        } finally {
            getTransactionService().complete();
            getTransactionService().begin();
            platformService.deleteTenant(tenantId1);
            platformService.deleteTenant(tenantId2);
            getTransactionService().complete();
        }
    }

    @Test(expected = STenantNotFoundException.class)
    public void shouldNotBeAbleToRetrieveUnknownTenantByName() throws Exception {
        getTransactionService().begin();
        try {
            platformService.getTenantByName("probably_not_existing");
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
        platformService.createTenant(tenant);
        getTransactionService().complete();

        getTransactionService().begin();
        final STenant readTenant = platformService.getTenantByName(name);
        getTransactionService().complete();

        assertThat(readTenant.getName()).isEqualTo(tenant.getName());

        deleteTenant(tenant.getId());
    }

    @Test
    public void getTenantsShouldFilterOnSearchCriteria() throws Exception {
        final String tenant1Name = "tTenantsShouldFilter1";
        final String tenant2Name = "tTenantsShouldFilter2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();
        final STenant tenant2 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();

        getTransactionService().begin();
        platformService.createTenant(tenant1);
        platformService.createTenant(tenant2);
        getTransactionService().complete();

        try {
            final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
            orderByOptions.add(new OrderByOption(STenant.class, BuilderFactory.get(STenantBuilderFactory.class).getIdKey(), OrderByType.DESC));
            final QueryOptions queryOptions = new QueryOptions(0, 20, orderByOptions, Collections.singletonList(new FilterOption(STenant.class, "name").like("tTenantsShouldFilter")),null);


            getTransactionService().begin();
            List<STenant> readTenants = platformService.getTenants(queryOptions);
            getTransactionService().complete();

            // count also default tenant:
            assertThat(readTenants.size()).isEqualTo(2);
            assertThat(readTenants.get(0).getId()).isEqualTo(tenant2.getId());
            assertThat(readTenants.get(1).getId()).isEqualTo(tenant1.getId());

            final Collection<Long> ids = new ArrayList<Long>();
            ids.add(tenant1.getId());
            ids.add(tenant2.getId());

            getTransactionService().begin();
            readTenants = platformService.getTenants(ids, queryOptions);
            getTransactionService().complete();

            assertThat(readTenants.size()).isEqualTo(2);
            assertThat(readTenants.get(0).getId()).isEqualTo(tenant2.getId());
            assertThat(readTenants.get(1).getId()).isEqualTo(tenant1.getId());

            ids.clear();
            ids.add(tenant1.getId());
            getTransactionService().begin();
            readTenants = platformService.getTenants(ids, queryOptions);
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
        platformService.deleteTenantObjects(id);
        getTransactionService().complete();

        // delete tenant
        getTransactionService().begin();
        platformService.deleteTenant(id);
        getTransactionService().complete();
    }

    @Test
    public void searchTenants() throws Exception {
        final String tenant1Name = "searchTenants1";
        final String tenant2Name = "searchTenants2";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        final STenant tenant1 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant1Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();
        final STenant tenant2 = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenant2Name, createdBy, created, STATUS_DEACTIVATED, false)
                .done();

        getTransactionService().begin();

        platformService.createTenant(tenant1);
        platformService.createTenant(tenant2);

        // sort
        final List<OrderByOption> orderbyOptions = new ArrayList<OrderByOption>();
        orderbyOptions.add(new OrderByOption(STenant.class, BuilderFactory.get(STenantBuilderFactory.class).getIdKey(), OrderByType.DESC));
        final QueryOptions queryOptions = new QueryOptions(0, 10, orderbyOptions, Collections.singletonList(new FilterOption(STenant.class,"name").like("searchTenants")),null);

        final List<STenant> readTenants = platformService.searchTenants(queryOptions);
        // count also default tenant:
        assertThat(readTenants.size()).isEqualTo(2);
        assertThat(readTenants.get(0).getId()).isEqualTo(tenant2.getId());
        assertThat(readTenants.get(1).getId()).isEqualTo(tenant1.getId());
        getTransactionService().complete();

        deleteTenant(tenant1.getId());
        deleteTenant(tenant2.getId());
    }

}
