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
import org.bonitasoft.engine.exception.NotFoundException;

/**
 * Handle permissions of users
 *
 * @author Baptiste Mesta
 */
public interface PermissionAPI {

    /**
     * Execute a groovy class, identified by it's class name, stored either in the classpath for default provided
     * scripts, or in database for custom scripts.
     * You can also add a jar containing a class implementing
     * {@link org.bonitasoft.engine.api.permission.PermissionRule} and execute it using its
     * fully qualified class name.
     * <p>
     * The class MUST implements {@link org.bonitasoft.engine.api.permission.PermissionRule}.
     * The class must implement method isAllowed() that returns TRUE to authorize access, or FALSE to forbid access.
     * If the script throws exception, it is up to the calling application to decide if the access should be granted or
     * not.
     * </p>
     * <p>
     * To store your custom class in database, you must use the Setup Tool.
     * Your custom groovy script must be placed in folder platform_conf/current/tenants/&lt;tenant
     * id&gt;/tenant_security_scripts/.
     * For more information on using the setup tool, refer to <a
     * href="https://documentation.bonitasoft.com/?page=BonitaBPM_platform_setup">the Platform setup
     * tool documentation page</a>
     * </p>
     *
     * @param className
     *        the name of the class of the rule
     * @param apiCallContext
     *        the context of the api call
     * @param reload
     *        reload class when calling this method, warning if some class were called with reload set to false, they
     *        will never be reloadable
     * @return true if the user is permitted to make the api call, false otherwise.
     * @throws ExecutionException
     *         If there is an exception while executing the script
     * @throws NotFoundException if the script cannot be found under name <quote>className</quote> neither in the
     *         classpath, nor in the custom script folder.
     * @since 6.4.0
     * @deprecated From version 7.14.0, use {@link #isAuthorized(APICallContext)} instead
     */
    @Deprecated(forRemoval = true, since = "7.14.0")
    boolean checkAPICallWithScript(String className, APICallContext apiCallContext, boolean reload)
            throws ExecutionException, NotFoundException;

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
