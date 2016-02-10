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
package org.bonitasoft.engine.api;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleCriterion;
import org.bonitasoft.engine.identity.RoleNotFoundException;
import org.bonitasoft.engine.identity.RoleUpdater;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * RoleAPI forms part of the {@link OrganizationAPI} and gives access to all the Administration operations available on Roles: creation, deletion,
 * search, etc...
 * 
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Emmanuel Duchastenier
 * @see SearchResult SearchResult for general knowledege on Search mechanism in Bonita BPM.
 * @see Role
 */
public interface RoleAPI {

    /**
     * Creates a role.
     * 
     * @param roleName
     *            the name of the role
     * @return the created role
     * @throws AlreadyExistsException
     *             If the name is already taken by an existing role
     * @throws CreationException
     *             If an exception occurs during the role creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Role createRole(String roleName) throws AlreadyExistsException, CreationException;

    /**
     * Create the role.
     * <p>
     * It takes the values of the creator in order to create the role.
     * </p>
     * @param creator
     *            the role creator
     * @return the created role.
     * @throws AlreadyExistsException
     *             If the name is already taken by an existing role
     * @throws CreationException
     *             If an exception occurs during the role creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Role createRole(RoleCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * Updates the group according to the updater values.
     * 
     * @param roleId
     *            the identifier of the role
     * @param updater
     *            the role updater
     * @return the updated role
     * @throws RoleNotFoundException
     *             If the role identifier does not refer to an existing role
     * @throws UpdateException
     *             If an exception occurs during the role update
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Role updateRole(long roleId, RoleUpdater updater) throws RoleNotFoundException, UpdateException;

    /**
     * Deletes the role.
     * 
     * @param roleId
     *            the role identifier
     * @throws DeletionException
     *             If an exception occurs during the role deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteRole(long roleId) throws DeletionException;

    /**
     * Deletes the roles.
     * 
     * @param roleIds
     *            the list of role identifiers
     * @throws DeletionException
     *             If an exception occurs during the role deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteRoles(List<Long> roleIds) throws DeletionException;

    /**
     * Retrieves the role.
     * 
     * @param roleId
     *            the identifier of the role
     * @return the role
     * @throws RoleNotFoundException
     *             If the role identifier does not refer to an existing role
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Role getRole(long roleId) throws RoleNotFoundException;

    /**
     * Retrieves the role.
     * 
     * @param roleName
     *            the name of the role.
     * @return the role.
     * @throws RoleNotFoundException
     *             If the role name does not refer to an existing role
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Role getRoleByName(String roleName) throws RoleNotFoundException;

    /**
     * Returns the total number of roles.
     * 
     * @return the total number of roles
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    long getNumberOfRoles();

    /**
     * Retrieves the paginated list of roles.
     * <p>
     * It retrieves from the startIndex to the startIndex + maxResults.
     * </p>
     * 
     * @param startIndex
     *            the start index
     * @param maxResults
     *            the max number of roles
     * @param criterion
     *            the sorting criterion
     * @return the paginated list of roles
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<Role> getRoles(int startIndex, int maxResults, RoleCriterion criterion);

    /**
     * Retrieves the roles. The map contains the couples roleId/Role.
     * <p>
     * If a role does not exists, no exception is thrown and no value is added in the map.
     * </p>
     *
     * @param roleIds
     *            the identifiers of the roles
     * @return the roles
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Map<Long, Role> getRoles(List<Long> roleIds);

    /**
     * Searches roles according to the criteria containing in the options.
     * 
     * @param options
     *            the search criteria
     * @return the search result
     * @throws SearchException
     *             If an exception occurs during the role searching
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    SearchResult<Role> searchRoles(SearchOptions options) throws SearchException;

}
