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

import java.util.Collection;
import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.platform.exception.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.exception.SPlatformAlreadyExistException;
import org.bonitasoft.engine.platform.exception.SPlatformCreationException;
import org.bonitasoft.engine.platform.exception.SPlatformDeletionException;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantAlreadyExistException;
import org.bonitasoft.engine.platform.exception.STenantCreationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.exception.STenantDeletionException;
import org.bonitasoft.engine.platform.exception.STenantException;
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

    // IMPORTANT: database platform (not tables) MUST BE CREATED before using this service

    // IMPORTANT: any access to this service MAY BE PROTECTED by a platform login (it is in the default implementation)

    // This service MUST BE re implemented in case multi tenancy is managed in a different way. To illustrate this, let's take 2 examples:
    // 1 - multi tenancy based on a discriminator column (same DB, same schema, same tables)
    // 2 - multi tenancy done using different schemas

    String TENANT = "TENANT";

    /**
     * Retrieve the platform from the cache
     * No need to be in a transaction
     *
     * @return sPlatform
     * @throws SPlatformNotFoundException
     *             occurs when the identifier does not refer to an existing sPlatform
     * @since 6.0
     */
    SPlatform getPlatform() throws SPlatformNotFoundException;

    // create platform tables (platform only, not tenant related tables)
    // insert the SPlatform row
    void createTables() throws SPlatformCreationException, SPlatformAlreadyExistException;

    /**
     * Create a sPlatform
     *
     * @param platform
     *            sPlatform
     * @throws SPlatformCreationException
     *             occurs when an exception is thrown during sPlatform creation
     * @since 6.0
     */
    void createPlatform(SPlatform platform) throws SPlatformCreationException;

    /**
     * Delete the current sPlatform
     *
     * @throws SPlatformDeletionException
     *             occurs when an exception is thrown during sPlatform deletion
     * @throws SPlatformNotFoundException
     *             occurs when the identifier does not refer to an existing sPlatform
     * @since 6.0
     */
    void deletePlatform() throws SPlatformDeletionException, SPlatformNotFoundException;

    void deleteTables() throws SPlatformDeletionException;

    /**
     * Update a sPlatform from given sPlatform and new content.
     *
     * @param platform
     *            sPlatform
     * @param descriptor
     *            the update descriptor
     * @throws SPlatformUpdateException
     *             occurs when an exception is thrown during sPlatform update
     * @since 6.0
     */
    void updatePlatform(SPlatform platform, EntityUpdateDescriptor descriptor) throws SPlatformUpdateException;

    /**
     * insert a new row in the Tenant table
     * case 1 - create tenants tables if not already exists + insert rows where necessary (sequences for example) + insert default users (defined in
     * configuration)
     * case 2 - create tenants tables + insert rows where necessary (sequences for example) + insert default users (defined in configuration)
     *
     * @param tenant
     *            sTenant
     * @return id of new created tenant
     * @throws STenantCreationException
     *             occurs when an exception is thrown during sTenant creation
     * @throws STenantAlreadyExistException
     *             occurs when the sTenant has already been taken
     * @since 6.0
     */
    long createTenant(STenant tenant) throws STenantCreationException, STenantAlreadyExistException;

    /**
     * case 1 - Remove tenant specific tenant tables if no mor etenant in the db
     * case 2 - Remove tenant specific tables
     *
     * @param tenantId
     * @throws STenantDeletionException
     *             occurs when an exception is thrown during sTenant deletion
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @throws SDeletingActivatedTenantException
     *             occurs when an exception is thrown during deleting an activated sTenant
     * @since 6.0
     */
    void deleteTenant(long tenantId) throws STenantDeletionException, STenantNotFoundException, SDeletingActivatedTenantException;

    /**
     * Remove all rows from all tables where the tenantId matches
     *
     * @param tenantId
     * @throws STenantDeletionException
     * @throws STenantNotFoundException
     * @throws SDeletingActivatedTenantException
     */
    void deleteTenantObjects(long tenantId) throws STenantDeletionException, STenantNotFoundException, SDeletingActivatedTenantException;

    /**
     * Update a sTenant from given sTenant and new content.
     *
     * @param tenant
     *            sTenant
     * @param descriptor
     *            new content
     * @throws STenantUpdateException
     *             occurs when an exception is thrown during sTenant update
     * @since 6.0
     */
    void updateTenant(STenant tenant, EntityUpdateDescriptor descriptor) throws STenantUpdateException;

    /**
     * Get tenant by its id
     *
     * @param id
     *            tenant id
     * @return sTenant
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @since 6.0
     */
    STenant getTenant(long id) throws STenantNotFoundException;

    /**
     * Get tenant by its name
     *
     * @param name
     *            tenant's name
     * @return sTenant
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @since 6.0
     */
    STenant getTenantByName(String name) throws STenantNotFoundException;

    /**
     * Get default tenant
     *
     * @return sTenant
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @since 6.0
     */
    STenant getDefaultTenant() throws STenantNotFoundException;

    /**
     * Get tenants which ids belong to given collection
     *
     * @param ids
     *            a collection of ids
     * @param queryOptions
     *            The criterion used to search tenants
     * @return a list of sTenant
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @throws STenantException
     * @since 6.0
     */
    List<STenant> getTenants(Collection<Long> ids, QueryOptions queryOptions) throws STenantNotFoundException, STenantException;

    /**
     * Set status of the tenant into activated
     *
     * @param tenantId
     * @return TODO
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @throws STenantActivationException
     *             occurs when an exception is thrown during activating sTenant
     * @since 6.0
     */
    boolean activateTenant(long tenantId) throws STenantNotFoundException, STenantActivationException;

    /**
     * Set status of the tenant into deactivated
     *
     * @param tenantId
     * @throws STenantNotFoundException
     *             occurs when the identifier does not refer to an existing sTenant
     * @throws STenantDeactivationException
     *             occurs when an exception is thrown during deactivating sTenant
     * @since 6.0
     */
    void deactiveTenant(long tenantId) throws STenantNotFoundException, STenantDeactivationException;

    /**
     * Get the total number of sTenants
     *
     * @return the total number of sTenants
     * @throws STenantException
     * @since 6.0
     */
    int getNumberOfTenants() throws STenantException;

    /**
     * Return true if the platform is created, else return false.
     *
     * @return true or false
     * @since 6.0
     */
    boolean isPlatformCreated();

    /**
     * Return a list of tenants by the given conditions, as one part of SearchResult that is search method's return value in platformApi
     *
     * @param options
     * @return a list of tenants by the given conditions
     * @throws SBonitaReadException
     */
    List<STenant> searchTenants(QueryOptions options) throws SBonitaReadException;

    /**
     * Get all tenants
     *
     * @param queryOptions
     *            The criterion used to search tenants
     * @return a list of sTenant
     * @throws STenantException
     * @since 6.0
     */
    List<STenant> getTenants(QueryOptions queryOptions) throws STenantException;

    /**
     * Return a number of tenants by the given conditions, as one part of SearchResult that is search method's return value in platformApi
     *
     * @param options
     * @return a number of tenants by the given conditions
     * @throws SBonitaReadException
     */
    long getNumberOfTenants(QueryOptions options) throws SBonitaReadException;

    void cleanTenantTables() throws STenantUpdateException;

    /**
     * @throws SPlatformCreationException
     */
    void initializePlatformStructure() throws SPlatformCreationException;

    /**
     * Return the platform properties
     *
     * @return The platform properties
     * @since 6.1
     */
    SPlatformProperties getSPlatformProperties();

}
