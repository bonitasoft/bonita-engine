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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.service.PermissionService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;

/**
 * @author Baptiste Mesta
 */
public class PermissionAPIImpl implements PermissionAPI {

    public static final String SECURITY_SCRIPTS = "security-scripts";

    @Override
    public boolean checkAPICallWithScript(String scriptName, APICallContext context) throws ExecutionException, NotFoundException {
        TenantServiceAccessor serviceAccessor = getTenantServiceAccessor();
        PermissionService permissionService = serviceAccessor.getPermissionService();
        File file = getScriptFile(scriptName, serviceAccessor);
        if (!file.exists()) {
            throw new NotFoundException("Unable to execute the security script because the file is not found, path=" + file.getPath());
        }
        try {
            return permissionService.checkAPICallWithScript(readFile(file), context);
        } catch (SExecutionException e) {
            throw new ExecutionException("Unable to execute the security script " + scriptName + " for the api call " + context, e);
        }
    }

    TenantServiceAccessor getTenantServiceAccessor() {
        return TenantServiceSingleton.getInstance();
    }

    String readFile(File file) throws ExecutionException {
        String scriptContent;
        try {
            scriptContent = FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new ExecutionException("Unable to execute the security script because the file is not readable, path=" + file.getPath(), e);
        }
        return scriptContent;
    }

    File getScriptFile(String scriptName, TenantServiceAccessor serviceAccessor) {
        File file;
        try {
            file = new File(new File(BonitaHomeServer.getInstance().getTenantConfFolder(serviceAccessor.getTenantId()), SECURITY_SCRIPTS), scriptName
                    + ".groovy");
        } catch (BonitaHomeNotSetException e) {
            throw new RuntimeException("Unable to execute the script", e);
        }
        return file;
    }
}
