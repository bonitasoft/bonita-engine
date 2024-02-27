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
package org.bonitasoft.engine.api;

import java.util.Set;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.exception.ExecutionException;

/**
 * Handle permissions of users
 *
 * @author Baptiste Mesta
 */
public interface PermissionAPI {

    /**
     * Checks if the REST API request defined in the {@link APICallContext} is authorized for the logged in user
     *
     * @param apiCallContext
     *        contains all the attributes of the request
     * @return true or false depending if the user it authorized to make the call or not
     * @throws ExecutionException if there was an error while executing the authorization checks
     */
    boolean isAuthorized(APICallContext apiCallContext) throws ExecutionException;

    /**
     * Returns the REST permissions required to access a REST resource (the expected format for the resource key is
     * <HTTP method>|<API name>/<resource name> e.g. GET|identity/user)
     *
     * @param resourceKey
     *        the resource identifier. The expected format is <HTTP method>|<API name>/<resource name> (e.g.
     *        GET|identity/user)
     * @return a Set of permissions, as Strings. e.g. ["organization_visualization"]
     */
    Set<String> getResourcePermissions(String resourceKey);
}
