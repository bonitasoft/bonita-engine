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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
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
}
