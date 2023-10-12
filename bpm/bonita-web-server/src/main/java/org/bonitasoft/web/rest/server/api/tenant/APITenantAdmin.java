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
package org.bonitasoft.web.rest.server.api.tenant;

import java.util.Map;

import org.bonitasoft.web.rest.model.system.TenantAdminDefinition;
import org.bonitasoft.web.rest.model.system.TenantAdminItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.system.TenantAdminDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Reboul
 * @deprecated since 9.0.0, use {{@link org.bonitasoft.web.rest.server.api.system.MaintenanceController}} instead.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public class APITenantAdmin extends ConsoleAPI<TenantAdminItem>
        implements APIHasGet<TenantAdminItem>, APIHasUpdate<TenantAdminItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(TenantAdminDefinition.TOKEN);
    }

    private TenantAdminDatastore getTenantAdminDatastore() {
        return new TenantAdminDatastore(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return "";
    }

    /**
     * update the Tenant State to set it to maintenance mode or set it up. <br/>
     * <br/>
     * This doesn't have any effect when if state doesn't have to be changed
     *
     * @see org.bonitasoft.web.rest.server.framework.API#update(org.bonitasoft.web.toolkit.client.data.APIID,
     *      java.util.Map)
     * @deprecated since 9.0.0, use
     *             {@link org.bonitasoft.web.rest.server.api.system.MaintenanceController#changeMaintenanceState(
     *             org.bonitasoft.web.rest.model.system.MaintenanceDetailsClient, javax.servlet.http.HttpSession)}
     *             instead.
     */
    @Override
    @Deprecated(since = "9.0.0", forRemoval = true)
    public TenantAdminItem update(final APIID id, final Map<String, String> attributes) {
        return getTenantAdminDatastore().update(id, attributes);
    }

    /**
     * get the current Tenant State
     *
     * @see org.bonitasoft.web.rest.server.framework.API#get(org.bonitasoft.web.toolkit.client.data.APIID)
     * @deprecated since 9.0.0, use
     *             {@link org.bonitasoft.web.rest.server.api.system.MaintenanceController#getMaintenanceDetails(
     *             javax.servlet.http.HttpSession)} instead.
     */
    @Override
    @Deprecated(since = "9.0.0", forRemoval = true)
    public TenantAdminItem get(final APIID id) {
        return getTenantAdminDatastore().get(id);
    }

}
