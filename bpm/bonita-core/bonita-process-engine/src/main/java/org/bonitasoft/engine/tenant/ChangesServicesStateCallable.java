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
package org.bonitasoft.engine.tenant;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.service.TenantServiceSingleton;

/**
 * @author Emmanuel Duchastenier
 */
class ChangesServicesStateCallable implements Callable<Void>, Serializable {

    private TenantServicesManager.ServiceAction action;
    private Long tenantId;

    public ChangesServicesStateCallable(TenantServicesManager.ServiceAction action, Long tenantId) {
        this.action = action;
        this.tenantId = tenantId;
    }

    @Override
    public Void call() throws Exception {
        TenantServicesManager tenantServicesManager = TenantServiceSingleton.getInstance(tenantId)
                .getTenantServicesManager();
        switch (action) {
            case START:
                tenantServicesManager.start();
                break;
            case STOP:
                tenantServicesManager.stop();
                break;
            case PAUSE:
                tenantServicesManager.pause();
                break;
            case RESUME:
                tenantServicesManager.resume();
                break;
        }
        return null;
    }
}
