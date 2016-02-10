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

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.MembershipNotFoundException;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.UserMembershipCriterion;

/**
 * MembershipAPI forms part of the {@link OrganizationAPI} and gives access to all the Administration operations available on <code>UserMembership</code>s:
 * creation, deletion, updating, retrieval, etc...
 * 
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see UserMembership
 */
public interface MembershipAPI {

    /**
     * Associates the user with the group and the role.
     * The association is called a user membership.
     * 
     * @param userId
     *            the identifier of the user
     * @param groupId
     *            the identifier of the group
     * @param roleId
     *            the identifier of the role
     * @return the user membership
     * @throws AlreadyExistsException
     *             If the triplet userId/groupId/roleId is already taken by an existing user membership
     * @throws CreationException
     *             If an exception occurs during the user membership creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    UserMembership addUserMembership(long userId, long groupId, long roleId) throws AlreadyExistsException, CreationException;

    /**
     * Associates the users with the group and the role.
     * The association is called a user membership.
     * 
     * @param userIds
     *            the identifiers of the users
     * @param groupId
     *            the identifier of the group
     * @param roleId
     *            the identifier of the role
     * @throws AlreadyExistsException
     *             If the triplet userId/groupId/roleId is already taken by an existing user membership
     * @throws CreationException
     *             If an exception occurs during the user membership creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void addUserMemberships(List<Long> userIds, long groupId, long roleId) throws AlreadyExistsException, CreationException;

    /**
     * Changes the association of the user membership.
     * It associates the user membership to the new role and group identifiers.
     * 
     * @param userMembershipId
     *            the identifier of the user membership
     * @param newGroupId
     *            the identifier of the new group
     * @param newRoleId
     *            the identifier of the new role
     * @return the updated user membership
     * @throws MembershipNotFoundException
     *             If the identifier of the user membership does not refer to an existing user membership
     * @throws UpdateException
     *             If an exception occurs during the user membership update
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    UserMembership updateUserMembership(long userMembershipId, long newGroupId, long newRoleId) throws MembershipNotFoundException, UpdateException;

    /**
     * Deletes the user membership.
     * 
     * @param userMembershipId
     *            the identifier of the user membership
     * @throws DeletionException
     *             If an exception occurs during the user membership deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteUserMembership(long userMembershipId) throws DeletionException;

    /**
     * Deletes the user membership.
     * 
     * @param userId
     *            the identifier of the user
     * @param groupId
     *            the identifier of the group
     * @param roleId
     *            the identifier of the role
     * @throws DeletionException
     *             If an exception occurs during the user membership deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteUserMembership(long userId, long groupId, long roleId) throws DeletionException;

    /**
     * Deletes the user memberships.
     * 
     * @param userIds
     *            the identifiers of the users
     * @param groupId
     *            the identifier of the group
     * @param roleId
     *            the identifier of the role
     * @throws DeletionException
     *             If an exception occurs during the user membership deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteUserMemberships(List<Long> userIds, long groupId, long roleId) throws DeletionException;

    /**
     * Retrieves the user membership.
     * 
     * @param membershipId
     *            the identifier of the user membership to retrieve.
     * @return the found <code>UserMembership</code> with the provided id
     * @throws MembershipNotFoundException
     *             If the identifier of the user membership does not refer to an existing user membership
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the user membership retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     */
    UserMembership getUserMembership(long membershipId) throws MembershipNotFoundException;

    /**
     * Returns the total number of memberships of the user.
     * 
     * @param userId
     *            the identifier of the user
     * @return the total number of memberships of the user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     */
    long getNumberOfUserMemberships(long userId);

    /**
     * Retrieves the paginated list of user memberships of the user.
     * It retrieves from the startIndex to the startIndex + maxResults.
     * 
     * @param userId
     *            the identifier of the user
     * @param startIndex
     *            the start index
     * @param maxResults
     *            the max number of user memberships
     * @param criterion
     *            the sorting criterion
     * @return the paginated list of user memberships
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<UserMembership> getUserMemberships(long userId, int startIndex, int maxResults, UserMembershipCriterion criterion);

    /**
     * Retrieves the paginated list of user memberships of the group.
     * It retrieves from the startIndex to the startIndex + maxResults.
     * 
     * @param groupId
     *            the identifier of the group
     * @param startIndex
     *            the start index
     * @param maxResults
     *            the max number of user memberships
     * @return the paginated list of user memberships
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<UserMembership> getUserMembershipsByGroup(long groupId, final int startIndex, final int maxResults);

    /**
     * Retrieves the paginated list of user memberships of the role.
     * It retrieves from the startIndex to the startIndex + maxResults.
     * 
     * @param roleId
     *            the identifier of the role
     * @param startIndex
     *            the start index
     * @param maxResults
     *            the max number of user memberships
     * @return the paginated list of user memberships
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<UserMembership> getUserMembershipsByRole(long roleId, int startIndex, int maxResults);

}
