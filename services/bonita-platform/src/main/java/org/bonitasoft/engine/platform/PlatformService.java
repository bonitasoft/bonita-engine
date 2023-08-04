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
package org.bonitasoft.engine.platform;

import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.exception.STenantUpdateException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface PlatformService {

    String TENANT = "TENANT";

    /**
     * Retrieve the platform from the cache
     * No need to be in a transaction
     *
     * @return sPlatform
     * @throws SPlatformNotFoundException
     *         occurs when the identifier does not refer to an existing sPlatform
     * @since 6.0
     */
    SPlatform getPlatform() throws SPlatformNotFoundException;

    /**
     * Update a sTenant from given sTenant and new content.
     *
     * @param tenant
     *        sTenant
     * @param descriptor
     *        new content
     * @throws STenantUpdateException
     *         occurs when an exception is thrown during sTenant update
     * @since 6.0
     */
    void updateTenant(STenant tenant, EntityUpdateDescriptor descriptor) throws STenantUpdateException;

    /**
     * Get tenant by its id
     *
     * @param id
     *        tenant id
     * @return sTenant
     * @throws STenantNotFoundException
     *         occurs when the identifier does not refer to an existing sTenant
     * @since 6.0
     * @deprecated There is only one tenant. Use {@link #getDefaultTenant()} instead.
     */
    @Deprecated
    STenant getTenant(long id) throws STenantNotFoundException;

    /**
     * Get default tenant
     *
     * @return sTenant
     * @throws STenantNotFoundException
     *         occurs when the identifier does not refer to an existing sTenant
     * @since 6.0
     */
    STenant getDefaultTenant() throws STenantNotFoundException;

    /**
     * Get the default tenant id
     * !! Internal use only !! Tenant notion should be removed soon.
     *
     * @return the default tenant id
     * @throws STenantNotFoundException if cannot retrieve tenant from database
     */
    long getDefaultTenantId() throws STenantNotFoundException;

    /**
     * Is the default tenant already created?
     *
     * @return true if the default tenant exists, false otherwise.
     * @throws SBonitaReadException when we cannot determine if default tenant is created
     * @since 7.3
     */
    boolean isDefaultTenantCreated() throws SBonitaReadException;

    /**
     * Set status of the tenant into activated
     *
     * @param tenantId
     * @throws STenantNotFoundException
     *         occurs when the identifier does not refer to an existing sTenant
     * @throws STenantActivationException
     *         occurs when an exception is thrown during activating sTenant
     * @since 6.0
     */
    void activateTenant(long tenantId) throws STenantNotFoundException, STenantActivationException;

    /**
     * Set status of the tenant into deactivated
     *
     * @param tenantId
     * @throws STenantNotFoundException
     *         occurs when the identifier does not refer to an existing sTenant
     * @throws STenantDeactivationException
     *         occurs when an exception is thrown during deactivating sTenant
     * @since 6.0
     */
    void deactivateTenant(long tenantId) throws STenantNotFoundException, STenantDeactivationException;

    /**
     * update status of tenant object to PAUSED
     *
     * @param tenantId the id of the tenant to update
     */
    void pauseTenant(long tenantId) throws STenantUpdateException, STenantNotFoundException;

    /**
     * Return true if the platform is created, else return false.
     *
     * @return true or false
     * @since 6.0
     */
    boolean isPlatformCreated();

    /**
     * Return the platform properties
     *
     * @return The platform properties
     * @since 6.1
     */
    SPlatformProperties getSPlatformProperties();
}
