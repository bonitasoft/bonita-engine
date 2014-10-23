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
     * Called by the engine when using {@link org.bonitasoft.engine.api.PermissionAPI#checkAPICallWithScript(String, APICallContext)}
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
    boolean check(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger);
}
