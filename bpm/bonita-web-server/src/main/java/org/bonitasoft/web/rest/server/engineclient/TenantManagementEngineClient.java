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
package org.bonitasoft.web.rest.server.engineclient;

import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;

/**
 * @author Colin PUY
 */
public class TenantManagementEngineClient {

    private final TenantAdministrationAPI tenantAdministrationAPI;

    public TenantManagementEngineClient(final TenantAdministrationAPI tenantManagementAPI) {
        this.tenantAdministrationAPI = tenantManagementAPI;
    }

    public boolean isTenantPaused() {
        return tenantAdministrationAPI.isPaused();
    }

    public void pauseTenant() {
        if (!isTenantPaused()) {
            pause();
        }
    }

    private void pause() {
        try {
            tenantAdministrationAPI.pause();
        } catch (final UpdateException e) {
            throw new APIException(new T_("Error when pausing BPM services"), e);
        }
    }

    public void resumeTenant() {
        if (isTenantPaused()) {
            resume();
        }
    }

    private void resume() {
        try {
            tenantAdministrationAPI.resume();
        } catch (final UpdateException e) {
            throw new APIException(new T_("Error when resuming BPM services"), e);
        }
    }
}
