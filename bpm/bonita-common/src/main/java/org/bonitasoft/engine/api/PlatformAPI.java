/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.PlatformCreationException;
import org.bonitasoft.engine.exception.PlatformDeletionException;
import org.bonitasoft.engine.exception.PlatformNotExistException;
import org.bonitasoft.engine.exception.PlatformNotStartedException;
import org.bonitasoft.engine.exception.PlatformStartingException;
import org.bonitasoft.engine.exception.PlatformStoppingException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.exception.TenantActivationException;
import com.bonitasoft.engine.exception.TenantAlreadyExistException;
import com.bonitasoft.engine.exception.TenantCreationException;
import com.bonitasoft.engine.exception.TenantDeactivationException;
import com.bonitasoft.engine.exception.TenantDeletionException;
import com.bonitasoft.engine.exception.TenantNotFoundException;
import com.bonitasoft.engine.exception.TenantUpdateException;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantCriterion;
import com.bonitasoft.engine.platform.TenantUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros,
 * @author Lu Kai
 * @author Zhang Bole
 */
public interface PlatformAPI {

    /**
     * Create a platform.
     * 
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformCreationException
     *             occurs when an exception is thrown during platform creation
     */
    void createPlatform() throws InvalidSessionException, PlatformCreationException;

    /**
     * Start a platform.
     * 
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformStartingException
     *             occurs when an exception is thrown during starting a platform
     */
    void startPlatform() throws InvalidSessionException, PlatformStartingException;

    /**
     * Stop a platform.
     * 
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformStoppingException
     *             occurs when an exception is thrown during platform stopping
     */
    void stopPlatform() throws InvalidSessionException, PlatformStoppingException;

    /**
     * Delete a platform.
     * 
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformDeletionException
     *             occurs when an exception is thrown during platform deletion
     */
    void deletePlaftorm() throws InvalidSessionException, PlatformDeletionException;

    /**
     * Get the platform.
     * 
     * @return the Platform object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotExistException
     *             occurs when the identifier does not refer to an existing platform
     */
    @Deprecated
    Platform getPlatform() throws InvalidSessionException, PlatformNotExistException;

    /**
     * Check if the platform created or not.
     * 
     * @return true if the platform existed
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotExistException
     *             occurs when the identifier does not refer to an existing platform
     */
    boolean isPlatformCreated() throws InvalidSessionException, PlatformNotExistException;

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
     */
    int getNumberOfTenants() throws InvalidSessionException, PlatformNotStartedException;

    /**
     * Get this platform state.
     * 
     * @return PlatformState object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PlatformNotExistException
     *             occurs when the identifier does not refer to an existing platform
     */
    PlatformState getPlatformState() throws InvalidSessionException, PlatformNotExistException;

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
     */
    SearchResult<Tenant> searchTenants(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

}
