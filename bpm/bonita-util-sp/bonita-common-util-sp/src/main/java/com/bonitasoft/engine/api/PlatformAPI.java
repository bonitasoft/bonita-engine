/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;
import org.bonitasoft.engine.exception.platform.PlatformNotStartedException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.exception.TenantActivationException;
import com.bonitasoft.engine.exception.TenantDeactivationException;
import com.bonitasoft.engine.exception.TenantNotFoundException;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantCriterion;
import com.bonitasoft.engine.platform.TenantUpdater;

/**
 * @author Matthieu Chaffotte
 */
public interface PlatformAPI extends org.bonitasoft.engine.api.PlatformAPI {

    /**
     * Create a new tenant
     * 
     * @param tenantCreator
     *            the creator object with the fields defining new Tenant to create
     * @return
     *         the tenant identifier of the newly created Tenant
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws CreationException
     *             occurs when an exception is thrown during tenant creation
     * @throws AlreadyExistsException
     *             occurs when a tenant with the same name already exists
     * @since 6.0
     */
    long createTenant(final TenantCreator tenantCreator) throws PlatformNotStartedException, CreationException, AlreadyExistsException;

    /**
     * Delete a tenant.
     * 
     * @param tenantId
     *            the tenant identifier
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantNotFoundException
     *             occurs when the identifier does not refer to an existing tenant
     * @throws DeletionException
     *             occurs when an exception is thrown during tenant deletion
     * @since 6.0
     */
    void deleteTenant(long tenantId) throws PlatformNotStartedException, TenantNotFoundException, DeletionException;

    /**
     * Activate a tenant.
     * 
     * @param tenantId
     *            the tenant identifier
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantNotFoundException
     *             occurs when the identifier does not refer to an existing tenant
     * @throws TenantActivationException
     *             occurs when an exception is thrown during tenant activation
     * @since 6.0
     */
    void activateTenant(long tenantId) throws PlatformNotStartedException, TenantNotFoundException, TenantActivationException;

    /**
     * De-activate a tenant.
     * 
     * @param tenantId
     *            the tenant identifier
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantNotFoundException
     *             occurs when the identifier does not refer to an existing tenant
     * @throws TenantDeactivationException
     *             occurs when an exception is thrown during tenant deactivation
     * @since 6.0
     */
    void deactiveTenant(long tenantId) throws PlatformNotStartedException, TenantNotFoundException, TenantDeactivationException;

    /**
     * Get a list of tenants.
     * 
     * @param startIndex
     *            the starting point, the first page is 1
     * @param maxResults
     *            the number of Tenants to be retrieved
     * @param pagingCriterion
     *            the criterion used to sort the retrieved tenants
     * @return the list of Tenant objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @since 6.0
     */
    List<Tenant> getTenants(int startIndex, int maxResults, TenantCriterion pagingCriterion) throws PlatformNotStartedException;

    /**
     * Get a tenant using its name.
     * 
     * @param tenantName
     *            the tenant name
     * @return Tenant object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantNotFoundException
     *             occurs when the identifier does not refer to an existing tenant
     * @since 6.0
     */
    Tenant getTenantByName(String tenantName) throws PlatformNotStartedException, TenantNotFoundException;

    /**
     * Get default tenant.
     * 
     * @return Tenant object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantNotFoundException
     *             occurs when the identifier does not refer to an existing tenant
     * @since 6.0
     */
    Tenant getDefaultTenant() throws PlatformNotStartedException, TenantNotFoundException;

    /**
     * get a tenant using its tenantId
     * 
     * @param tenantId
     *            the tenant identifier
     * @return Tenant object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantNotFoundException
     *             occurs when the identifier does not refer to an existing tenant
     * @since 6.0
     */
    Tenant getTenantById(long tenantId) throws PlatformNotStartedException, TenantNotFoundException;

    /**
     * Return the total number of tenants.
     * 
     * @return the total number of tenants
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @since 6.0
     */
    int getNumberOfTenants() throws PlatformNotStartedException;

    /**
     * Update a tenant with its tenantId and new content.
     * 
     * @param tenantId
     *            the tenant identifier
     * @param udpater
     *            the update descriptor
     * @return Tenant object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws UpdateException
     *             occurs when an exception is thrown during tenant updated
     * @since 6.0
     */
    Tenant updateTenant(long tenantId, TenantUpdater udpater) throws PlatformNotStartedException, UpdateException;

    /**
     * Search tenant under the given condition, including pagination, term, filter, sort.
     * 
     * @param searchOptions
     *            the search options (pagination parameters, filters, sort)
     * @return {@link SearchResult} The criterion used to search tenants
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             if the search could not be fulfilled correcly
     * @since 6.0
     */
    SearchResult<Tenant> searchTenants(SearchOptions searchOptions) throws SearchException;

}
