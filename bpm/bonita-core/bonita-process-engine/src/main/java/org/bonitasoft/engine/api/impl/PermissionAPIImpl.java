/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
    public boolean checkAPICallWithScript(String className, APICallContext context) throws ExecutionException, NotFoundException {
        TenantServiceAccessor serviceAccessor = getTenantServiceAccessor();
        PermissionService permissionService = serviceAccessor.getPermissionService();
        try {
            return permissionService.checkAPICallWithScript(className, context);
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
