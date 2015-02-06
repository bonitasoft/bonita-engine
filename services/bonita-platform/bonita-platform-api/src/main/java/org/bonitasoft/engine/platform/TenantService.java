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
package org.bonitasoft.engine.platform;

import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantUpdateException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Celine Souchet
 */
public interface TenantService {

    /**
     * Update the tenant where you are logged with the new content, only if you are the technical user.
     * 
     * @param updater
     *            new content
     * @throws STenantUpdateException
     *             occurs when an exception is thrown during sTenant update
     * @since 6.4.0
     */
    void updateTenant(EntityUpdateDescriptor descriptor) throws STenantUpdateException;

    /**
     * Get the logged tenant
     * 
     * @return sTenant
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @since 6.0
     */
    STenant getTenant() throws STenantNotFoundException;

}
