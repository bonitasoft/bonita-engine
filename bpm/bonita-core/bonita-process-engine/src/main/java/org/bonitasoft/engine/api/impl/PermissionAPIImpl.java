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
package org.bonitasoft.engine.api.impl;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;

/**
 * @author Baptiste Mesta
 */
@Slf4j
@AvailableInMaintenanceMode
public class PermissionAPIImpl implements PermissionAPI {

    @Override
    public boolean isAuthorized(APICallContext apiCallContext) throws ExecutionException {
        ServiceAccessor serviceAccessor = getServiceAccessor();
        try {
            return serviceAccessor.getPermissionService().isAuthorized(apiCallContext);
        } catch (SExecutionException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public Set<String> getResourcePermissions(String resourceKey) {
        ServiceAccessor serviceAccessor = getServiceAccessor();
        return serviceAccessor.getPermissionService().getResourcePermissions(resourceKey);
    }

    ServiceAccessor getServiceAccessor() {
        return ServiceAccessorSingleton.getInstance();
    }

}
