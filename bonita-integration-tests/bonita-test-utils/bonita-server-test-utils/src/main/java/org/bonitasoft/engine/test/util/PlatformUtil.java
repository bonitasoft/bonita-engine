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
package org.bonitasoft.engine.test.util;

import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

public class PlatformUtil {

    public static final String DEFAULT_TENANT_NAME = "default";

    public static final String DEFAULT_CREATED_BY = "admin";

    public static final String DEFAULT_TENANT_STATUS = "DEACTIVATED";

    public static final String TENANT_STATUS_ACTIVATED = "ACTIVATED";

    public static long createTenant(final TransactionService transactionService, final PlatformService platformService,
            final String tenantName, final String createdBy, final String status) throws Exception {
        try {
            transactionService.begin();
            final long created = System.currentTimeMillis();

            final STenantBuilder tenantBuilder = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance(tenantName, createdBy, created, status,
                    false);
            final STenant tenant = tenantBuilder.done();
            platformService.createTenant(tenant);
            BonitaHomeServer.getInstance().createTenant(tenant.getId());
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
            BonitaHomeServer.getInstance().createTenant(tenant.getId());
            platformService.activateTenant(tenant.getId());
            return tenant.getId();
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

    public static void createPlatform() throws CreationException {
        new PlatformAPIImpl().createPlatform();
    }

    public static void deletePlatform() throws DeletionException {
        new PlatformAPIImpl().deletePlatform();
    }

    public static long getDefaultTenantId(final PlatformService platformService) throws STenantNotFoundException {
        return platformService.getDefaultTenant().getId();
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
