/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.system;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.system.TenantAdminItem;
import org.bonitasoft.web.rest.server.engineclient.EngineAPIAccessor;
import org.bonitasoft.web.rest.server.engineclient.EngineClientFactory;
import org.bonitasoft.web.rest.server.engineclient.TenantManagementEngineClient;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Julien Mege
 * @deprecated since Bonita 9.0 Use {@link org.bonitasoft.web.rest.server.api.system.MaintenanceController} instead
 */
@Slf4j
@Deprecated
public class TenantAdminDatastore extends Datastore
        implements DatastoreHasUpdate<TenantAdminItem>, DatastoreHasGet<TenantAdminItem> {

    protected final APISession apiSession;

    private static boolean hasShownDeprectedLog = false;

    public TenantAdminDatastore(final APISession apiSession) {
        this.apiSession = apiSession;
    }

    @Override
    public TenantAdminItem update(final APIID unusedId, final Map<String, String> attributes) {
        logDeprecatedAPIUsage();
        final TenantAdminItem tenantAdminItem = new TenantAdminItem();
        final boolean doPause = Boolean.parseBoolean(attributes.get(TenantAdminItem.ATTRIBUTE_IS_PAUSED));
        if (!doPause) {
            getTenantManagementEngineClient().resumeTenant();
        } else {
            getTenantManagementEngineClient().pauseTenant();
        }
        tenantAdminItem.setIsPaused(doPause);
        return tenantAdminItem;
    }

    @Override
    public TenantAdminItem get(final APIID id) {
        logDeprecatedAPIUsage();
        final TenantAdminItem tenantAdminItem = new TenantAdminItem();
        final boolean tenantPaused = getTenantManagementEngineClient().isTenantPaused();
        tenantAdminItem.setIsPaused(tenantPaused);
        return tenantAdminItem;
    }

    protected void logDeprecatedAPIUsage() {
        if (!hasShownDeprectedLog && log.isWarnEnabled()) {
            log.warn(
                    "API system/tenant is deprecated and will be removed in a future release. Please use API system/maintenance instead.");
            hasShownDeprectedLog = true;
        }
    }

    protected TenantManagementEngineClient getTenantManagementEngineClient() {
        return new EngineClientFactory(new EngineAPIAccessor(apiSession)).createTenantManagementEngineClient();
    }
}
