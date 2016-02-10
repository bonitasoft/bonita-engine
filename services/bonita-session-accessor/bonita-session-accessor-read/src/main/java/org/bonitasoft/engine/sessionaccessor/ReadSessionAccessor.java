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
package org.bonitasoft.engine.sessionaccessor;

/**
 * @author Yanyan Liu
 */
public interface ReadSessionAccessor {

    /**
     * Get current session id
     * 
     * @return the identifier of current session
     * @throws SessionIdNotSetException
     *             if no session exists for the given id, throw exception
     * @since 6.0
     */
    long getSessionId() throws SessionIdNotSetException;

    /**
     * Get the tenant id
     * 
     * @return the identifier of the tenant
     * @throws STenantIdNotSetException
     *             if no tenant exists for the given id, throw exception
     * @since 6.0
     */
    long getTenantId() throws STenantIdNotSetException;

}
