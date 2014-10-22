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

package org.bonitasoft.engine.api;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;

/**
 * Handle permissions of users
 *
 * @author Baptiste Mesta
 */
public interface PermissionAPI {

    /**
     * Execute a groovy script stored in bonita-home/server/tenants/<tenant id>/conf/security-scripts/<scriptName>.groovy
     * <p>
     * If the script is executed without exceptions it means that the user is authorized to access the resource.
     * The script must be put by hand in the bonita home folder bonita-home/server/tenants/<tenant id>/conf/security-scripts/<scriptName>.groovy
     * </p>
     *
     * @param scriptName
     *        the name of the script stored in the bonita home to call (without the .groovy extension)
     * @param apiCallContext
     *        the context of the api call
     * @return true if the user is permitted to make the api call
     * @throws ExecutionException
     *         If there is an exception while executing the script
     * @since 6.4.0
     */
    boolean checkAPICallWithScript(String scriptName, APICallContext apiCallContext) throws ExecutionException, NotFoundException;
}
