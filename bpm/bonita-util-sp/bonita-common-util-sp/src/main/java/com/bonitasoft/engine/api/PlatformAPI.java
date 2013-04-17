/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.PlatformNotStartedException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.exception.TenantActivationException;
import com.bonitasoft.engine.exception.TenantAlreadyExistException;
import com.bonitasoft.engine.exception.TenantCreationException;
import com.bonitasoft.engine.exception.TenantDeactivationException;
import com.bonitasoft.engine.exception.TenantDeletionException;
import com.bonitasoft.engine.exception.TenantNotFoundException;
import com.bonitasoft.engine.exception.TenantUpdateException;
import com.bonitasoft.engine.platform.LicenseInfo;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantCriterion;
import com.bonitasoft.engine.platform.TenantUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 */
public interface PlatformAPI extends org.bonitasoft.engine.api.PlatformAPI {

    /**
     * Create a tenant
     * Return its tenant id
     * 
     * @param tenantName
     *            the tenant name
     * @param description
     *            the tenant description
     * @param iconName
     *            the tenant iconName
     * @param iconPath
     *            the tenant iconPath
     * @param userName
     *            the user name
     * @param password
     *            the user password
     * @return the tenant Id
     *         the tenant identifier
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantCreationException
     *             occurs when an exception is thrown during tenant creation
     * @throws TenantAlreadyExistException
     *             occurs when the tenant has already been taken
     *             since 6.0
     */
    long createTenant(final String tenantName, final String description, final String iconName, final String iconPath, final String userName,
            final String password) throws InvalidSessionException, PlatformNotStartedException, TenantCreationException, TenantAlreadyExistException;

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
     * @throws TenantDeletionException
     *             occurs when an exception is thrown during tenant deletion
     *             since 6.0
     */
    void deleteTenant(long tenantId) throws InvalidSessionException, PlatformNotStartedException, TenantNotFoundException, TenantDeletionException;

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
     *             since 6.0
     */
    void activateTenant(long tenantId) throws InvalidSessionException, PlatformNotStartedException, TenantNotFoundException, TenantActivationException;

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
     *             since 6.0
     */
    void deactiveTenant(long tenantId) throws InvalidSessionException, PlatformNotStartedException, TenantNotFoundException, TenantDeactivationException;

    /**
     * Get a list of tenants.
     * If no tenants existed, return empty.
     * 
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @return the list of Tenant objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     *             since 6.0
     */
    List<Tenant> getTenants(final int pageIndex, final int numberPerPage) throws InvalidSessionException, PlatformNotStartedException;

    /**
     * Get a list of tenants.
     * 
     * @param pageIndex
     *            the starting point, the first page is 1
     * @param numberPerPage
     *            the number of Tenants to be retrieved
     * @param pagingCriterion
     *            the criterion used to sort the retrieved tenants
     * @return the list of Tenant objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws PageOutOfRangeException
     *             when the pageIndex is 0 and other out of range scenarios
     * @throws BonitaException
     *             since 6.0
     */
    List<Tenant> getTenants(int pageIndex, int numberPerPage, TenantCriterion pagingCriterion) throws InvalidSessionException, PlatformNotStartedException,
            PageOutOfRangeException, BonitaException;

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
     *             since 6.0
     */
    Tenant getTenantByName(String tenantName) throws InvalidSessionException, PlatformNotStartedException, TenantNotFoundException;

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
     *             since 6.0
     */
    Tenant getDefaultTenant() throws InvalidSessionException, PlatformNotStartedException, TenantNotFoundException;

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
     *             since 6.0
     */
    Tenant getTenantById(long tenantId) throws InvalidSessionException, PlatformNotStartedException, TenantNotFoundException;

    /**
     * Get the total number of tenants.
     * If no tenants existed, return 0.
     * 
     * @return the total number of tenants
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     *             since 6.0
     */
    int getNumberOfTenants() throws InvalidSessionException, PlatformNotStartedException;

    /**
     * Update a tenant with its tenantId and new content.
     * 
     * @param tenantId
     *            the tenant identifier
     * @param udpateDescriptor
     *            the update descriptor
     * @return Tenant object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotStartedException
     *             occurs when an exception is thrown if the platform is not started
     * @throws TenantUpdateException
     *             occurs when an exception is thrown during tenant updated
     *             since 6.0
     */
    Tenant updateTenant(long tenantId, TenantUpdateDescriptor udpateDescriptor) throws InvalidSessionException, PlatformNotStartedException,
            TenantUpdateException;

    /**
     * Search tenant under the given condition,including pagination,term,filter,sort.
     * 
     * @param searchOptions
     * @return searchOptions
     *         The criterion used to search tenants
     * @throws InvalidSessionException
     * @throws SearchException
     *             since 6.0
     */
    SearchResult<Tenant> searchTenants(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * @return
     */
    LicenseInfo getLicenseInfo();

}
