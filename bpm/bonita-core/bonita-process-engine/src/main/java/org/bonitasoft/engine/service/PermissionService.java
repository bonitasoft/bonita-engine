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
package org.bonitasoft.engine.service;

import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;

/**
 * @author Baptiste Mesta
 */
public interface PermissionService extends TenantLifecycleService {

    /**
     * execute the {@link org.bonitasoft.engine.api.permission.PermissionRule} having the class name in parameter using the given context
     * 
     * @param className
     *        the class name of the rule to execute
     * @param context
     *        the context of the api call to check
     * @param reload
     *        reload class when calling this method, warning if some class were called with reload set to false, they will never be reloadable
     * @return true if the security script allows the user to make the api call
     */
    boolean checkAPICallWithScript(String className, APICallContext context, boolean reload) throws SExecutionException, ClassNotFoundException;
}
