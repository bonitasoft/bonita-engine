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
     * Execute a groovy class stored in bonita-home/server/tenants/&lt;tenant id&gt;/conf/security-scripts/ using it's class name
     * <p>
     * The class MUST implements {@link org.bonitasoft.engine.api.permission.PermissionRule} If the script is executed without exceptions it means that the user
     * is authorized to access the resource.
     * The class must be put by hand in the bonita home folder bonita-home/server/tenants/&lt;tenant id&gt;/conf/security-scripts/&lt;scriptName&gt;.groovy
     * You can also add jar containing class implementing {@link org.bonitasoft.engine.api.permission.PermissionRule} and execute them using their class name.
     * </p>
     *
     * @param className
     *        the name of the class of the rule
     * @param apiCallContext
     *        the context of the api call
     * @param reload
     *        reload class when calling this method, warning if some class were called with reload set to false, they will never be reloadable
     * @return true if the user is permitted to make the api call
     * @throws ExecutionException
     *         If there is an exception while executing the script
     * @since 6.4.0
     */
    boolean checkAPICallWithScript(String className, APICallContext apiCallContext, boolean reload) throws ExecutionException, NotFoundException;
}
