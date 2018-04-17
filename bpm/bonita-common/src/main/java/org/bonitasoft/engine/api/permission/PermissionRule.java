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
package org.bonitasoft.engine.api.permission;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.Logger;
import org.bonitasoft.engine.session.APISession;

/**
 * Class to extend when implementing permission rule to be checked by {@link org.bonitasoft.engine.api.PermissionAPI}
 *
 * @author Baptiste Mesta
 */
public interface PermissionRule {

    /**
     * Called by the engine when using {@link org.bonitasoft.engine.api.PermissionAPI#checkAPICallWithScript(String, APICallContext, boolean)}
     *
     * @param apiSession
     *        the api session from the user doing the api call
     * @param apiCallContext
     *        the context of the api call
     * @param apiAccessor
     *        an accessor to call apis
     * @param logger
     *        a logger that use the engine logger
     * @return
     *         true if the user is allowed to access the api or false otherwise
     */
    boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger);
}
