/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.util;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

public class PlatformUtil {

    public static final String DEFAULT_TENANT_NAME = "default";

    public static final String DEFAULT_CREATED_BY = "admin";

    public static final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    public static long createTenant(final TransactionService transactionService, final PlatformService platformService, final STenantBuilder tenantBuilder,
            final String tenantName, final String createdBy, final String status) throws Exception {
        transactionService.begin();
        final long created = System.currentTimeMillis();

        tenantBuilder.createNewInstance(tenantName, createdBy, created, status, false);
        final STenant tenant = tenantBuilder.done();
        platformService.createTenant(tenant);
        platformService.activateTenant(tenant.getId());
        transactionService.complete();
        return tenant.getId();
    }

    public static long createDefaultTenant(final TransactionService transactionService, final PlatformService platformService,
            final STenantBuilder tenantBuilder, final String tenantName, final String createdBy, final String status) throws Exception {
        transactionService.begin();
        final long created = System.currentTimeMillis();

        tenantBuilder.createNewInstance(tenantName, createdBy, created, status, true);
        final STenant tenant = tenantBuilder.done();
        platformService.createTenant(tenant);
        platformService.activateTenant(tenant.getId());
        transactionService.complete();
        return tenant.getId();
    }

    public static boolean isPlatformCreated(final TransactionService transactionService, final PlatformService platformService)
            throws Exception {
        transactionService.begin();
        try {
            return platformService.isPlatformCreated();
        } finally {
            transactionService.complete();
        }
    }

    public static void deleteTenant(final TransactionService transactionService, final PlatformService platformService, final long tenantId) throws Exception {
        transactionService.begin();
        // delete tenant objects
        platformService.deactiveTenant(tenantId);
        platformService.deleteTenantObjects(tenantId);
        transactionService.complete();

        // delete tenant
        transactionService.begin();
        platformService.deleteTenant(tenantId);
        transactionService.complete();
    }

    public static void createPlatform(final TransactionService transactionService, final PlatformService platformService, final SPlatformBuilder platformBuilder)
            throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

//        transactionService.begin();
        platformService.createPlatformTables();
        platformService.createTenantTables();
//        transactionService.complete();

        transactionService.begin();
        platformService.initializePlatformStructure();
        transactionService.complete();

        platformBuilder.createNewInstance(version, previousVersion, initialVersion, createdBy, created);
        final SPlatform platform = platformBuilder.done();
        transactionService.begin();
        platformService.createPlatform(platform);
        transactionService.complete();
    }

    public static void deletePlatform(final TransactionService transactionService, final PlatformService platformService) throws Exception {
        transactionService.begin();
        try {
            List<STenant> existingTenants;
            existingTenants = platformService.getTenants(QueryOptions.defaultQueryOptions());
            if (existingTenants.size() > 0) {
                for (STenant sTenant : existingTenants) {
                    long tenantId = sTenant.getId();
                    platformService.deactiveTenant(tenantId);
                    platformService.deleteTenant(tenantId);
                }
            }
            platformService.deletePlatform();
        } finally {
            transactionService.complete();
        }
        //transactionService.begin();
        try {
            platformService.deleteTenantTables();
            platformService.deletePlatformTables();
        } finally {
            //transactionService.complete();
        }
    }

    public static String getDefaultTenantName() {
        return DEFAULT_TENANT_NAME;
    }

    public static long getDefaultTenantId(final PlatformService platformService) throws STenantNotFoundException {
        return platformService.getDefaultTenant().getId();
    }

    public static String getDefaultCreatedBy() {
        return DEFAULT_CREATED_BY;
    }

    public static long createDefaultTenant(final TransactionService transactionService, final PlatformService platformService,
            final STenantBuilder tenantBuilder) throws Exception {
        return createDefaultTenant(transactionService, platformService, tenantBuilder, DEFAULT_TENANT_NAME, DEFAULT_CREATED_BY, DEFAULT_TENANT_STATUS);
    }

    public static void deleteDefaultTenant(final TransactionService transactionService, final PlatformService platformService,
            final SessionAccessor sessionAccessor, final SessionService sessionService) throws Exception {
        transactionService.begin();
        final long tenantId = platformService.getTenantByName(DEFAULT_TENANT_NAME).getId();
        TestUtil.createSessionOn(sessionAccessor, sessionService, tenantId);

        // delete tenant objects
        platformService.deactiveTenant(tenantId);
        platformService.deleteTenantObjects(tenantId);
        transactionService.complete();

        // delete tenant
        transactionService.begin();
        platformService.deleteTenant(tenantId);
        transactionService.complete();
    }
}
