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
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.ContactData;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.identity.UserWithContactData;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * UserAPI forms part of the {@link OrganizationAPI} and gives access to all the Administration operations available on Users: creation, deletion, certain
 * specific getXXX() methods, generic search methods, etc...
 * It also to retrieve user ContactData.
 * 
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see ContactData
 * @see SearchResult SearchResult for general knowledege on Search mechanism in Bonita BPM.
 */
public interface UserAPI {

    /**
     * Creates a user.
     * The password can't be empty.
     * 
     * @param userName
     *        The name of the user
     * @param password
     *        The password of the user
     * @return The created user
     * @throws AlreadyExistsException
     *         If the name is already taken by an existing user
     * @throws CreationException
     *         If an exception occurs during the user creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    User createUser(String userName, String password) throws AlreadyExistsException, CreationException;

    /**
     * Creates a user.
     * The password can't be empty.
     *
     * @param userName
     *        The name of the user
     * @param password
     *        The password of the user
     * @param firstName
     *        The first name of the user
     * @param lastName
     *        The last name of the user
     * @return The created user
     * @throws AlreadyExistsException
     *         If the name is already taken by an existing user
     * @throws CreationException
     *         If an exception occurs during the user creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    User createUser(String userName, String password, String firstName, String lastName) throws AlreadyExistsException, CreationException;

    /**
     * Creates a user.
     * It takes the values of the creator in order to create the user.
     *
     * @param creator
     *        The user to create
     * @return The created user
     * @throws AlreadyExistsException
     *         If the name is already taken by an existing user
     * @throws CreationException
     *         If an exception occurs during the user creation
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    User createUser(UserCreator creator) throws AlreadyExistsException, CreationException;

    /**
     * Updates the user according to the updater values.
     *
     * @param userId
     *        The identifier of the user
     * @param updater
     *        The user updater
     * @return The updated user
     * @throws UserNotFoundException
     *         If the user identifier does not refer to an existing user
     * @throws UpdateException
     *         If an exception occurs during the user update
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    User updateUser(long userId, UserUpdater updater) throws UserNotFoundException, UpdateException;

    /**
     * Deletes the user.
     *
     * @param userId
     *        The identifier of the user
     * @throws DeletionException
     *         If an exception occurs during the user deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteUser(long userId) throws DeletionException;

    /**
     * Deletes the user.
     *
     * @param userName
     *        The name of the user
     * @throws DeletionException
     *         If an exception occurs during the user deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteUser(String userName) throws DeletionException;

    /**
     * Deletes the users.
     *
     * @param userIds
     *        The identifiers of the users
     * @throws DeletionException
     *         If an exception occurs during the user deletion
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void deleteUsers(List<Long> userIds) throws DeletionException;

    /**
     * Retrieves the user.
     * It throws a {@link UserNotFoundException} if the user identifier equals the technical user identifier (-1).
     *
     * @param userId
     *        The identifier of the user
     * @return The searched user
     * @throws UserNotFoundException
     *         If the user identifier does not refer to an existing user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the user retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    User getUser(long userId) throws UserNotFoundException;

    /**
     * Retrieves the user.
     *
     * @param userName
     *        The name of the user
     * @return The role
     * @throws UserNotFoundException
     *         If the user name does not refer to an existing user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    User getUserByUserName(String userName) throws UserNotFoundException;

    /**
     * Retrieves the professional details of the user.
     *
     * @param userId
     *        The identifier of the user
     * @return The user and the professional details
     * @throws UserNotFoundException
     *         If the user identifier does not refer to an existing user, or is -1 (the technical user identifier)
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs while retrieving the user
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.1
     */
    UserWithContactData getUserWithProfessionalDetails(long userId) throws UserNotFoundException;

    /**
     * Retrieves the contact data (personal or professional) of the user.
     *
     * @param userId
     *        The identifier of the user
     * @param personal
     *        true if the contact data is the personal one
     * @return The contact data
     * @throws UserNotFoundException
     *         If the user name does not refer to an existing user
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    ContactData getUserContactData(long userId, boolean personal) throws UserNotFoundException;

    /**
     * Returns the total number of users.
     *
     * @return The total number of users
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    long getNumberOfUsers();

    /**
     * Retrieves the paginated list of users. It retrieves from the startIndex to the startIndex + maxResults.
     *
     * @param startIndex
     *        The start index
     * @param maxResults
     *        The max number of users
     * @param criterion
     *        The sorting criterion
     * @return The paginated list of users
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the user retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<User> getUsers(int startIndex, int maxResults, UserCriterion criterion);

    /**
     * Retrieves the users. The map contains the couples userId/User.
     * If a user does not exists, no exception is thrown and no value is added in the map.
     *
     * @param userIds
     *        The identifiers of the users
     * @return The users
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the user retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Map<Long, User> getUsers(List<Long> userIds);

    /**
     * Retrieves the identifiers of the named users. The map contains the couples userName/User.
     * If a user does not exists, no exception is thrown and no value is added in the map.
     *
     * @param userNames
     *        The names of the users
     * @return The users ordered by the user name ascending
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the user retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.1
     */
    Map<String, User> getUsersByUsernames(List<String> userNames);

    /**
     * Searches users according to the criteria containing in the options.
     *
     * @param options
     *        The search criteria
     * @return The search result
     * @throws SearchException
     *         If an exception occurs during the user searching
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    SearchResult<User> searchUsers(SearchOptions options) throws SearchException;

    /**
     * Returns the total number of users of the role.
     *
     * @param roleId
     *        The identifier of the role
     * @return The total number of users of the role
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    long getNumberOfUsersInRole(long roleId);

    /**
     * Retrieves the paginated list of roles.
     * It retrieves from the startIndex to the startIndex + maxResults.
     *
     * @param roleId
     *        The identifier of the role
     * @param startIndex
     *        The start index
     * @param maxResults
     *        The max number of roles
     * @param criterion
     *        The sorting criterion
     * @return The paginated list of roles
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<User> getUsersInRole(long roleId, int startIndex, int maxResults, UserCriterion criterion);

    /**
     * Returns the total number of users of the group.
     *
     * @param groupId
     *        The identifier of the group
     * @return The total number of users of the group
     * @throws BonitaException
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    long getNumberOfUsersInGroup(long groupId) throws BonitaException;

    /**
     * Retrieves the paginated list of groups.
     * It retrieves from the startIndex to the startIndex + maxResults.
     *
     * @param groupId
     *        The identifier of the group
     * @param startIndex
     *        The start index
     * @param maxResults
     *        The max number of groups
     * @param criterion
     *        The sorting criterion
     * @return The paginated list of groups
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs during the group retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<User> getUsersInGroup(long groupId, int startIndex, int maxResults, UserCriterion criterion);

    /**
     * Retrieves the list of user identifiers containing the chosen custom user information with the given value.
     * 
     * @param infoName
     *        The custom user information name
     * @param infoValue
     *        The custom user information value
     * @param usePartialMatch
     *        Defines whether the custom user information value should use a partial match.
     * @param startIndex
     *        The start index (the first valid value is zero)
     * @param maxResults
     *        The max number of user identifiers to be retrieved
     * @return the list of user identifiers containing the chosen custom user information with the given value.
     * @since 6.3.2
     */
    List<Long> getUserIdsWithCustomUserInfo(String infoName, String infoValue, boolean usePartialMatch, int startIndex, int maxResults);

}
