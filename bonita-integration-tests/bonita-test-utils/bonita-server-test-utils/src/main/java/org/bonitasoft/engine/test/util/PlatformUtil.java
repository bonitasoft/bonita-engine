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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantDeletionException;
import org.bonitasoft.engine.platform.exception.STenantException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilderFactory;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

public class PlatformUtil {

    public static final String DEFAULT_TENANT_NAME = "default";

    public static final String DEFAULT_CREATED_BY = "admin";

    public static final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    public static long createTenant(final TransactionService transactionService, final PlatformService platformService,
            final String tenantName, final String createdBy, final String status) throws Exception {
        try {
            transactionService.begin();
            final long created = System.currentTimeMillis();

            final STenantBuilder tenantBuilder = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenantName, createdBy, created, status,
                    false);
            final STenant tenant = tenantBuilder.done();
            platformService.createTenant(tenant);
            platformService.activateTenant(tenant.getId());
            return tenant.getId();
        } finally {
            transactionService.complete();
        }
    }

    public static long createDefaultTenant(final TransactionService transactionService, final PlatformService platformService,
            final String tenantName, final String createdBy, final String status) throws Exception {
        try {
            transactionService.begin();
            final long created = System.currentTimeMillis();

            final STenantBuilder tenantBuilder = BuilderFactory.get(STenantBuilderFactory.class)
                    .createNewInstance(tenantName, createdBy, created, status, true);
            final STenant tenant = tenantBuilder.done();
            platformService.createTenant(tenant);
            platformService.activateTenant(tenant.getId());
            return tenant.getId();
        } finally {
            transactionService.complete();
        }
    }

    public static boolean isPlatformCreated(final TransactionService transactionService, final PlatformService platformService) throws Exception {
        try {
            transactionService.begin();
            return platformService.isPlatformCreated();
        } finally {
            transactionService.complete();
        }
    }

    public static void deleteTenant(final TransactionService transactionService, final PlatformService platformService, final long tenantId) throws Exception {
        transactionService.begin();
        // delete tenant objects
        try {
            platformService.deactiveTenant(tenantId);
            platformService.deleteTenantObjects(tenantId);
        } finally {
            transactionService.complete();
        }
        // delete tenant
        try {
            transactionService.begin();
            platformService.deleteTenant(tenantId);
        } finally {
            transactionService.complete();
        }
    }

    public static void createPlatform(final TransactionService transactionService, final PlatformService platformService)
            throws Exception {
        final String version = "myVersion";
        final String previousVersion = "previousVersion";
        final String initialVersion = "initialVersion";
        final String createdBy = "mycreatedBy";
        final long created = System.currentTimeMillis();

        platformService.createTables();

        try {
            transactionService.begin();
            platformService.initializePlatformStructure();
        } finally {
            transactionService.complete();
        }

        final SPlatform platform = BuilderFactory.get(SPlatformBuilderFactory.class)
                .createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
        try {
            transactionService.begin();
            platformService.createPlatform(platform);
        } finally {
            transactionService.complete();
        }
    }

    public static void deletePlatform(final TransactionService transactionService, final PlatformService platformService) throws Exception {
        try {
            transactionService.begin();
            deactiveAndDeleteAllTenants(platformService);
            platformService.deletePlatform();
        } finally {
            transactionService.complete();
        }
        platformService.deleteTables();
    }

    private static void deactiveAndDeleteAllTenants(final PlatformService platformService) throws STenantException, STenantNotFoundException,
    STenantDeactivationException, STenantDeletionException, SDeletingActivatedTenantException {
        List<STenant> existingTenants;
        do {
            existingTenants = platformService.getTenants(new QueryOptions(0, 100, STenant.class, "id", OrderByType.ASC));
            for (final STenant sTenant : existingTenants) {
                final long tenantId = sTenant.getId();
                platformService.deactiveTenant(tenantId);
                platformService.deleteTenant(tenantId);
            }
        } while (existingTenants.size() == 100);
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

    public static long createDefaultTenant(final TransactionService transactionService, final PlatformService platformService) throws Exception {
        return createDefaultTenant(transactionService, platformService, DEFAULT_TENANT_NAME, DEFAULT_CREATED_BY, DEFAULT_TENANT_STATUS);
    }

    public static void deleteDefaultTenant(final TransactionService transactionService, final PlatformService platformService,
            final SessionAccessor sessionAccessor, final SessionService sessionService) throws Exception {
        long tenantId = 0;
        try {
            transactionService.begin();
            tenantId = platformService.getTenantByName(DEFAULT_TENANT_NAME).getId();
            TestUtil.createSessionOn(sessionAccessor, sessionService, tenantId);

            // delete tenant objects
            platformService.deactiveTenant(tenantId);
            platformService.deleteTenantObjects(tenantId);
        } finally {
            transactionService.complete();
        }

        // delete tenant
        try {
            transactionService.begin();
            platformService.deleteTenant(tenantId);
        } finally {
            transactionService.complete();
        }
    }
}
