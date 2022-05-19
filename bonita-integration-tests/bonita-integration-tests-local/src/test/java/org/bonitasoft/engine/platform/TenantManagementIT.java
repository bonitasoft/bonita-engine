/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantUpdateException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TenantManagementIT extends CommonBPMServicesTest {

    private final static String STATUS_DEACTIVATED = "DEACTIVATED";

    private PlatformService platformService;

    @Before
    public void setup() {
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

        final STenant tenant = STenant.builder().name(name).createdBy(createdBy).created(created)
                .status(STATUS_DEACTIVATED).defaultTenant(false)
                .description(description).build();

        assertThat(tenant.getName()).isEqualTo(name);
        assertThat(tenant.getCreatedBy()).isEqualTo(createdBy);
        assertThat(tenant.getCreated()).isEqualTo(created);
        assertThat(tenant.getDescription()).isEqualTo(description);
    }

    @Test
    public void updateTenantShouldUpdateAllFields() throws Exception {
        getTransactionService().begin();

        final String newDescription = "newDescription";

        final STenantUpdateBuilderFactory updateBuilderFactory = BuilderFactory.get(STenantUpdateBuilderFactory.class);
        final STenantUpdateBuilder updateDescriptor = updateBuilderFactory.createNewInstance();
        updateDescriptor.setDescription(newDescription);

        platformService.updateTenant(platformService.getDefaultTenant(), updateDescriptor.done());
        getTransactionService().complete();

        getTransactionService().begin();
        final STenant readTenant = platformService.getDefaultTenant();
        getTransactionService().complete();

        assertThat(readTenant.getDescription()).isEqualTo(newDescription);
    }

    @Test(expected = STenantUpdateException.class)
    public void updateInexistantTenantShouldFail() throws Exception {
        final STenant tenant = STenant.builder().name("tenant1").createdBy("mycreatedBy")
                .created(System.currentTimeMillis()).status(STATUS_DEACTIVATED).defaultTenant(false).build();

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

    @Test(expected = STenantNotFoundException.class)
    public void shouldNotBeAbleToRetrieveUnknownTenantByName() throws Exception {
        getTransactionService().begin();
        try {
            platformService.getTenantByName("probably_not_existing");
        } finally {
            getTransactionService().complete();
        }

    }

}
