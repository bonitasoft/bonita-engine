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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;

/**
 * @author Baptiste Mesta
 */
public class PermissionAPIImpl implements PermissionAPI {

    @Override
    public boolean checkAPICallWithScript(String className, APICallContext context, boolean reload) throws ExecutionException, NotFoundException {
        TenantServiceAccessor serviceAccessor = getTenantServiceAccessor();
        PermissionService permissionService = serviceAccessor.getPermissionService();
        try {
            return permissionService.checkAPICallWithScript(className, context, reload);
        } catch (SExecutionException e) {
            throw new ExecutionException("Unable to execute the security rule " + className + " for the api call " + context, e);
        } catch (ClassNotFoundException e) {
            throw new NotFoundException("Unable to execute the security rule " + className + " for the api call " + context + "because the class " + className
                    + " is not found",
                    e);
        }
    }

    TenantServiceAccessor getTenantServiceAccessor() {
        return TenantServiceSingleton.getInstance();
    }
}
