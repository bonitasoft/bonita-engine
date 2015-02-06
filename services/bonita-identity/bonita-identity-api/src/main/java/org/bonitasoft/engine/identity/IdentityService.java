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
package org.bonitasoft.engine.identity;

import java.util.List;

import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * This service allow to manage the users and organizations and membership and the relations between them.
 *
 * @author Anthony Birembaut, Baptiste Mesta, Matthieu Chaffotte
 * @since 6.0
 */
public interface IdentityService {

    static final String GROUP = "GROUP";

    static final String CUSTOM_USER_INFO_DEFINITION = "CUSTOM_USER_INFO_DEFINITION";

    static final String CUSTOM_USER_INFO_VALUE = "CUSTOM_USER_INFO_VALUE";

    static final String ROLE = "ROLE";

    static final String USER = "USER";

    static final String USER_CONTACT_INFO = "USER_CONTACT_INFO";

    static final String USERMEMBERSHIP = "USERMEMBERSHIP";

    /**
     * Gets the {@link SRole} given by its identifier.
     *
     * @param roleId
     *        the role identifier
     * @return the role of the given id
     * @throws SRoleNotFoundException
     *         occurs when the roleId does not refer to any role.
     */
    SRole getRole(long roleId) throws SRoleNotFoundException;

    /**
     * Get the {@link SRole} given by its name.
     *
     * @param roleName
     *        The name of role
     * @return the role of the given name
     * @throws SRoleNotFoundException
     *         occurs when the roleName does not refer to any role.
     */
    SRole getRoleByName(String roleName) throws SRoleNotFoundException;

    /**
     * Get total number of {@link SRole} for this tenant
     *
     * @return the total number of roles for this tenant
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfRoles() throws SIdentityException;

    /**
     * Get a {@link List} of {@link SRole} by their identifiers
     *
     * @param roleIds
     *        the role identifiers
     * @return a list of {@link SRole} objects
     * @throws SRoleNotFoundException
     *         Occurs when roleId does not refer to any role.
     */
    List<SRole> getRoles(List<Long> roleIds) throws SRoleNotFoundException;

    /**
     * Get a {@link List} of {@link SRole} in specific interval
     * <p>
     * If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfRoles
     *        Number of result to retrieve
     * @return a list of SRole objects
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SRole> getRoles(int fromIndex, int numberOfRoles) throws SIdentityException;

    /**
     * <p>
     * Get a {@link List} of {@link SRole} in specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType} <i>order</i>.
     * <p> For instance, getRoles(0,10,"displayName", OrderByType.DESC) returns the 10 first roles sorted by &quot;displayName&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SRoleBuilderFactory} to get a list of available field keys to sort
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfRoles
     *        Number of result to retrieve
     * @param field
     *        The field used by the sort
     * @param order
     *        ASC or DESC
     * @return a list of paginated SRole objects
     * @see SRoleBuilderFactory to get a list of available field keys to use
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SRole> getRoles(int fromIndex, int numberOfRoles, String field, OrderByType order) throws SIdentityException;

    /**
     * Get the {@link SGroup} by its path
     *
     * @param groupPath
     *        The group path
     * @return the group
     * @throws SGroupNotFoundException
     *         Occurs when the groupPath does not refer to any group.
     */
    SGroup getGroupByPath(String groupPath) throws SGroupNotFoundException;

    /**
     * Get {@link SGroup} by its identifier
     *
     * @param groupId
     *        The group identifier
     * @return the group
     * @throws SGroupNotFoundException
     *         occurs when the groupId does not refer to any group.
     */
    SGroup getGroup(long groupId) throws SGroupNotFoundException;

    /**
     * Get total number of {@link SGroup} in the current tenant
     *
     * @return the total number of {@link SGroup}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfGroups() throws SIdentityException;

    /**
     * Get a {@link List} of {@link SGroup} for specific groupIds
     *
     * @param groupIds
     *        The group identifiers
     * @return a {@link List} of {@link SGroup} object
     * @throws SGroupNotFoundException occurs when no given group identifiers refer to any group.
     */
    List<SGroup> getGroups(List<Long> groupIds) throws SGroupNotFoundException;

    /**
     * Get a {@link List} of {@link SGroup} in a specific interval, this is used for pagination
     * <p>
     * If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfGroups
     *        Number of result to retrieve
     * @return a {@link List} of {@link SGroup}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SGroup> getGroups(int fromIndex, int numberOfGroups) throws SIdentityException;

    /**
     * <p>
     * Get a {@link List} of {@link SGroup} in specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType} <i>order</i>.
     * <p> For instance, getGroups(0,10,"displayName", OrderByType.DESC) returns the 10 first {@link SGroup} sorted by &quot;displayName&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SGroupBuilderFactory} to get a list of available field keys to sort
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfGroups
     *        Number of result to retrieve
     * @param field
     *        The field used to sort
     * @param order
     *        ASC or DESC
     * @return a {@link List} of paginated {@link SGroup} objects
     * @see SGroupBuilderFactory
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SGroup> getGroups(int fromIndex, int numberOfGroups, String field, OrderByType order) throws SIdentityException;

    /**
     * Get number of child groups for the group identified by the given id
     *
     * @param parentGroupId
     *        The parent group identifier
     * @return the number of child groups
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfGroupChildren(long parentGroupId) throws SIdentityException;

    /**
     * Get a {@link List} child {@link SGroup} in a specific interval for specific group. This is used for pagination
     * If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param parentGroupId
     *        The parent group identifier
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfGroups
     *        Number of result to retrieve
     * @return a {@link List} of child {@link SGroup}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SGroup> getGroupChildren(long parentGroupId, int fromIndex, int numberOfGroups) throws SIdentityException;

    /**
     * Get a {@link List} of child {@link SGroup} in specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType} <i>order</i>.
     * <p> For instance, getGroupChildren(0,10,"displayName", OrderByType.DESC) returns the 10 first child {@link SGroup} of the parent {@link SGroup}
     * identified by <i>parentGroupId</i> sorted by &quot;displayName&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SGroupBuilderFactory} to get a list of available field keys to sort
     *
     * @param parentGroupId
     *        The parent group identifier
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfGroups
     *        Number of result to retrieve
     * @param field
     *        The field used to sort
     * @param order
     *        ASC or DESC
     * @return a {@link List} of child {@link SGroup}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SGroup> getGroupChildren(long parentGroupId, int fromIndex, int numberOfGroups, String field, OrderByType order) throws SIdentityException;

    /**
     * Get {@link SUser} by its id
     *
     * @param userId
     *        The user identifier
     * @return the user
     * @throws SUserNotFoundException
     *         occurs when the userId does not refer to any user.
     */
    SUser getUser(long userId) throws SUserNotFoundException;

    /**
     * Checks whether the couple user/password is valid.
     *
     * @param user
     *        the user
     * @param password
     *        the password
     * @return true if the couple user/password is valid; false otherwise
     */
    boolean chechCredentials(SUser user, String password);

    /**
     * Get {@link SUser} by its name
     *
     * @param username
     *        The user name
     * @return the user
     * @throws SUserNotFoundException
     *         occurs when the user name does not refer to any user.
     */
    SUser getUserByUserName(String username) throws SUserNotFoundException;

    /**
     * Get total number of {@link SUser} on the current tenant
     *
     * @return the total number of users
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfUsers() throws SIdentityException;

    /**
     * Get {@link SUser} by their userIds
     *
     * @param userIds
     *        A {@link List} of user identifiers
     * @return a {@link List} of {@link SUser}
     * @throws SUserNotFoundException
     *         occurs when none of the given userIds refer to any user.
     */
    List<SUser> getUsers(List<Long> userIds) throws SUserNotFoundException;

    /**
     * retrieve a {@link List} of {@link SUser} from their names.
     *
     * @param userNames
     *        the list of user names
     * @return a {@link List} of {@link SUser}
     * @throws SIdentityException
     *         If an exception occurs when retrieving the users
     */
    List<SUser> getUsersByUsername(List<String> userNames) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUser} from a specific interval.
     * <p>
     * If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfUsers
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUser}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUser> getUsers(int fromIndex, int numberOfUsers) throws SIdentityException;

    /**
     * Get a {@link List} of child {@link SUser} from specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType} <i>order</i> order.
     * <p> For instance, getUsers(0,10,"displayName", OrderByType.DESC) returns the 10 first {@link SUser} sorted by &quot;displayName&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SUserBuilderFactory} to get a list of available field keys to sort
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfUsers
     *        Number of result we want to get. Maximum number of result returned.
     * @param field
     *        The field used by the order
     * @param order
     *        ASC or DESC
     * @return a {@link List} of paginated {@link SUser}
     * @see SUserBuilderFactory
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUser> getUsers(int fromIndex, int numberOfUsers, String field, OrderByType order) throws SIdentityException;

    /**
     * Get all users managed by a specific manager from a specific interval.
     * <p>
     * If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param managerId
     *        The manager identifier, actually it is user identifier
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberMaxOfUsers
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUser}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUser> getUsersByManager(long managerId, int fromIndex, int numberMaxOfUsers) throws SIdentityException;

    /**
     * Get total number of users for the given role on the current tenant
     *
     * @param roleId
     *        The identifier of role
     * @return total number of users related to the given role
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfUsersByRole(long roleId) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUser} from a specific interval for the given {@link SRole} identifier.
     * <p>
     * If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param roleId
     *        The identifier of the {@link SRole} of the {@link SUser} to retrieve
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfUsers
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUser}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUser> getUsersByRole(long roleId, int fromIndex, int numberOfUsers) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUser} for given {@link SRole} from specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType}
     * <i>order</i> order.
     * <p> For instance, getUsersByRole(1,0,10,"displayName", OrderByType.DESC) returns the 10 first {@link SUser} of the {@link SRole} with id '1' sorted by
     * &quot;displayName&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SUserBuilderFactory} to get a list of available field keys to sort
     *
     * @param roleId
     *        The identifier of the {@link SRole}
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfUsers
     *        Number of result to retrieve
     * @param field
     *        The field used to sort
     * @param order
     *        ASC or DESC
     * @return a {@link List} of {@link SUser}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUser> getUsersByRole(long roleId, int fromIndex, int numberOfUsers, String field, OrderByType order) throws SIdentityException;

    /**
     * Get total number of users for the given {@link SGroup}
     *
     * @param groupId
     *        The identifier of the {@link SGroup}
     * @return total number of users in the given group
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfUsersByGroup(long groupId) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUser} of the given group from a specific interval.
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param groupId
     *        Identifier of the {@link SGroup}
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfUsers
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUser}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUser> getUsersByGroup(long groupId, int fromIndex, int numberOfUsers) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUser} for given {@link SGroup} from specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType}
     * <i>order</i> order.
     * <p> For instance, getUsersByGroup(1,0,10,"displayName", OrderByType.DESC) returns the 10 first {@link SUser} of the {@link SGroup} with id '1' sorted by
     * &quot;displayName&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SUserBuilderFactory} to get a list of available field keys to sort
     *
     * @param groupId
     *        Identifier of the {@link SGroup}
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfUsers
     *        Number of result to retrieve
     * @param field
     *        The field used to sort
     * @param order
     *        ASC or DESC
     * @return a {@link List} of {@link SUser}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUser> getUsersByGroup(long groupId, int fromIndex, int numberOfUsers, String field, OrderByType order) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUserMembership} for given group from specific interval
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param groupId
     *        Identifier of the group
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfResults
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUserMembership}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUserMembership> getUserMembershipsOfGroup(long groupId, int fromIndex, int numberOfResults) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUserMembership} for given role from specific interval
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param roleId
     *        Identifier of the role
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfResults
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUserMembership}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUserMembership> getUserMembershipsOfRole(long roleId, int fromIndex, int numberOfResults) throws SIdentityException;

    /**
     * Get {@link SUserMembership} by given id
     *
     * @param userMembershipId
     *        The identifier of userMembership
     * @return the {@link SUserMembership} of the given id
     * @throws SIdentityException occurs on persistence layer access problem or if the {@link SUserMembership} does not exists
     */
    SUserMembership getUserMembership(long userMembershipId) throws SIdentityException;

    /**
     * Get {@link SUserMembership} of specific {@link SUser}, {@link SGroup} and {@link SRole}
     *
     * @param userId
     *        The user's identifier
     * @param groupId
     *        The group's identifier
     * @param roleId
     *        The role's identifier
     * @return the {@link SUserMembership}
     * @throws SIdentityException occurs on persistence layer access problem or if the {@link SUserMembership} does not exists
     */
    SUserMembership getUserMembership(long userId, long groupId, long roleId) throws SIdentityException;

    /**
     * Get the {@link SUserMembership} of the specific user, group and role without userName, groupName and roleName being set.
     *
     * @param userId
     *        The user's identifier
     * @param groupId
     *        The group's identifier
     * @param roleId
     *        The role's identifier
     * @return the lightened {@link SUserMembership}
     * @throws SIdentityException occurs on persistence layer access problem or if the {@link SUserMembership} does not exists
     */
    SUserMembership getLightUserMembership(long userId, long groupId, long roleId) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUserMembership} of the given identifiers if they exists
     *
     * @param userMembershipIds
     *        The {@link SUserMembership}'s identifiers
     * @return a {@link List} of {@link SUserMembership}
     * @throws SIdentityException occurs on persistence layer access problem or if none identifiers match an existing {@link SUserMembership}
     */
    List<SUserMembership> getUserMemberships(List<Long> userMembershipIds) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUserMembership} from specific interval
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfResults
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUserMembership}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUserMembership> getUserMemberships(int fromIndex, int numberOfResults) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUserMembership} from specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType} <i>order</i>
     * order.
     * <p> For instance, getUserMemberships(0,10,"id", OrderByType.DESC) returns the 10 first {@link SUserMembership} sorted by
     * &quot;displayName&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SUserMembershipBuilderFactory} to check available field keys to sort
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfUserMemberships
     *        Number of result to retrieve
     * @param field
     *        The field used to sort
     * @param order
     *        ASC or DESC
     * @return a {@link List} of {@link SUserMembership}
     * @see SUserMembershipBuilderFactory
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUserMembership> getUserMemberships(int fromIndex, int numberOfUserMemberships, OrderByOption orderByOption) throws SIdentityException;

    /**
     * Get {@link SCustomUserInfoDefinition} by its id
     *
     * @param customUserInfoDefinitionId
     *        The {@link SCustomUserInfoDefinition}'s identifier
     * @return the customUserInfoDefinition
     * @throws SIdentityException occurs on persistence layer access problem
     */
    SCustomUserInfoDefinition getCustomUserInfoDefinition(long customUserInfoDefinitionId) throws SIdentityException;

    /**
     * Get {@link SCustomUserInfoValue} by its id
     *
     * @param customUserInfoValueId
     *        The identifier of the custom user info value
     * @return the {@link SCustomUserInfoValue}
     * @throws SCustomUserInfoValueNotFoundException
     *         if no custom user info value is found for the given id
     * @throws SCustomUserInfoValueReadException
     *         if an exception occurs while trying to get the custom user info value
     */
    SCustomUserInfoValue getCustomUserInfoValue(long customUserInfoValueId) throws SCustomUserInfoValueNotFoundException, SCustomUserInfoValueReadException;

    /**
     * Get a {@link SCustomUserInfoDefinition} by its name
     *
     * @param name
     *        The name of the {@link SCustomUserInfoDefinition} to retrieve
     * @return the {@link SCustomUserInfoDefinition} identified by the given name
     * @throws SCustomUserInfoDefinitionNotFoundException
     *         if there is no custom user info definition for the given name
     * @throws SCustomUserInfoDefinitionReadException
     *         if an exception occurs when trying to retrieve the custom user info definition
     */
    SCustomUserInfoDefinition getCustomUserInfoDefinitionByName(String name) throws SCustomUserInfoDefinitionNotFoundException,
            SCustomUserInfoDefinitionReadException;

    /**
     * Verify if there is a {@link SCustomUserInfoDefinition} of the given name
     *
     * @param name
     *        The {@link SCustomUserInfoDefinition}'s name to check
     * @return the {@link SCustomUserInfoDefinition} of the given name
     * @throws SCustomUserInfoDefinitionNotFoundException
     *         if there is no custom user info definition for the given name
     * @throws SCustomUserInfoDefinitionReadException
     *         if an exception occurs when trying to retrieve the custom user info definition
     */
    boolean hasCustomUserInfoDefinition(String name) throws SCustomUserInfoDefinitionReadException;

    /**
     * Get total number of {@link SCustomUserInfoDefinition} of the current tenant
     *
     * @return the total number of {@link SCustomUserInfoDefinition} of the current tenant
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfCustomUserInfoDefinition() throws SIdentityException;

    /**
     * Get total number of {@link SCustomUserInfoValue} of the current tenant matching the criterias of the given {@link QueryOptions}
     *
     * @param options
     *        The {@link QueryOptions} containing filtering conditions
     * @return the total number of custom user info value
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfCustomUserInfoValue(QueryOptions options) throws SBonitaReadException;

    /**
     * Get a {@link List} of {@link SCustomUserInfoDefinition} of the given identifiers
     *
     * @param customUserInfoDefinitionIds
     *        A {@link List} of {@link SCustomUserInfoDefinition} identifiers
     * @return a {@link List} of {@link SCustomUserInfoDefinition}
     * @throws SIdentityException occurs on persistence layer access problem or if none identifiers match an existing {@link SCustomUserInfoDefinition}
     */
    List<SCustomUserInfoDefinition> getCustomUserInfoDefinitions(List<Long> customUserInfoDefinitionIds) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SCustomUserInfoValue} of the given identifiers
     *
     * @param customUserInfoValueIds
     *        A {@link List} of {@link SCustomUserInfoValue} identifiers
     * @return A {@link List} of {@link SCustomUserInfoValue}
     * @throws SIdentityException occurs on persistence layer access problem or if none identifiers match an existing {@link SCustomUserInfoDefinition}
     */
    List<SCustomUserInfoValue> getCustomUserInfoValues(List<Long> customUserInfoValueIds) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SCustomUserInfoDefinition} from specific interval
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param maxResults
     *        Number of result to retrieve
     * @return a {@link List} of {@link SCustomUserInfoDefinition}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SCustomUserInfoDefinition> getCustomUserInfoDefinitions(int fromIndex, int maxResults) throws SIdentityException;

    /**
     * Retrieve a {@link List} of user identifiers from specific interval. The returned user's ids have their associated user's userInfo of the given
     * name <i>userInfoName</i> matches the given value <i>userInfoValue</i> partially or not depending on the <i>usePartialMatch</i> parameter.
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param userInfoName The user's information name.
     * @param userInfoValue The user's information value.
     * @param usePartialMatch Defines whether the custom user information value should use a partial match.
     * @param fromIndex The index of the first record to be retrieved. First record has index 0.
     * @param maxResults The max results to be retrieved.
     * @return the list identifiers of users matching the given user's information name & value.
     * @throws SIdentityException occurs on persistence layer access problem
     * @since 6.3.2
     */
    List<Long> getUserIdsWithCustomUserInfo(String userInfoName, String userInfoValue, boolean usePartialMatch, int fromIndex, int maxResults)
            throws SIdentityException;

    /**
     * Search {@link SCustomUserInfoValue} matching the criterias of the given {@link QueryOptions}
     *
     * @param options
     *        The QueryOptions object containing some query conditions
     * @return a list of SCustomUserInfoValue objects
     * @throws SBonitaReadException occurs on persistence layer access problem or if search parameters are not correct
     */
    List<SCustomUserInfoValue> searchCustomUserInfoValue(QueryOptions options) throws SBonitaReadException;

    /**
     * Get {@link SUserMembership} for a specific interval for a given user
     *
     * @param userId
     *        The user's identifier
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfMemberships
     *        Number of result to retrieve
     * @return a {@link List} of {@link SUserMembership}
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUserMembership> getUserMembershipsOfUser(long userId, int fromIndex, int numberOfMemberships) throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUserMembership} of a given user from specific interval, sorted by <i>field</i> attribute in the given {@link OrderByType}
     * <i>sortOrder</i> order.
     * <p> For instance, getUserMembershipsOfUser(1,0,10,"id", OrderByType.DESC) returns the 10 first {@link SUserMembership} of the given user sorted by
     * &quot;id&quot; in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     * <p> check {@link SUserMembershipBuilderFactory} to check available field keys to sort
     *
     * @param userId
     *        The identifier of user
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfMemberships
     *        Number of result to retrieve
     * @param field
     *        The field user to do order
     * @param sortOrder
     *        ASC or DESC
     * @return a {@link List} of {@link SUserMembership}
     * @see SUserMembershipBuilderFactory
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUserMembership> getUserMembershipsOfUser(long userId, int fromIndex, int numberOfMemberships, String field, OrderByType sortOrder)
            throws SIdentityException;

    /**
     * Get a {@link List} of {@link SUserMembership} of a given user from specific interval, sorted by default field in the given {@link OrderByType}
     * <i>sortOrder</i> order.
     * <p> For instance, getUserMembershipsOfUser(1,0,10, OrderByType.DESC) returns the 10 first {@link SUserMembership} of the given user sorted by
     * the default field in desc order
     * <p> If the number of existing results are lower than the number asked, all results from the given index are retrieved.
     *
     * @param userId
     *        The identifier of user
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberPerPage
     *        Number of result o retrieve
     * @param sortOrder
     *        OrderByOption object containing order information
     * @return a list of SUserMembership objects
     * @throws SIdentityException occurs on persistence layer access problem
     */
    List<SUserMembership> getUserMembershipsOfUser(long userId, int fromIndex, int numberPerPage, OrderByOption sortOrder) throws SIdentityException;

    /**
     * Get total number of {@link SUserMembership} for a specific user
     *
     * @param userId
     *        The user's identifier
     * @return total number of userMemberships for the specific user
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfUserMembershipsOfUser(long userId) throws SIdentityException;

    /**
     * Get total number of userMemberships of the current tenant
     *
     * @return total number of userMemberships
     * @throws SIdentityException occurs on persistence layer access problem
     */
    long getNumberOfUserMemberships() throws SIdentityException;

    /**
     * Create {@link SUser} and store it to persistence layer
     *
     * @param user
     *        The user to create and store
     * @return the created user with its identifier set
     * @throws SUserCreationException occurs on persistence layer access problem
     */
    SUser createUser(SUser user) throws SUserCreationException;

    /**
     * Update {@link SUser} according to the {@link EntityUpdateDescriptor}
     *
     * @param user
     *        The {@link SUser} to update
     * @param descriptor
     *        The {@link SUser} contents to update
     * @throws SUserUpdateException occurs on persistence layer access problem
     */
    void updateUser(SUser user, EntityUpdateDescriptor descriptor) throws SUserUpdateException;

    /**
     * Update {@link SUser} according to the {@link EntityUpdateDescriptor}
     *
     * @param user
     *        The {@link SUser} to update
     * @param descriptor
     *        The {@link SUser} contents to update
     * @param isPasswordEncrypted allow to know if the password given in the {@link EntityUpdateDescriptor} is encrypted or not
     * @throws SUserUpdateException
     */
    @Deprecated
    void updateUser(SUser user, EntityUpdateDescriptor descriptor, boolean isPasswordEncrypted) throws SUserUpdateException;

    /**
     * Create custom user info definition in DB for a server given custom user info definition
     *
     * @param customUserInfo
     *        SCustomUserInfoDefinition object
     * @throws SCustomUserInfoDefinitionAlreadyExistsException
     *         TODO
     * @throws SCustomUserInfoDefinitionCreationException
     *         TODO
     */
    SCustomUserInfoDefinition createCustomUserInfoDefinition(SCustomUserInfoDefinition customUserInfo) throws SCustomUserInfoDefinitionAlreadyExistsException,
            SCustomUserInfoDefinitionCreationException;

    /**
     * Update customUserInfoDefinition according to the descriptor
     *
     * @param customUserInfo
     *        The customUserInfoDefinition will be updated
     * @param descriptor
     *        The update description
     * @throws SIdentityException
     */
    void updateCustomUserInfoDefinition(SCustomUserInfoDefinition customUserInfo, EntityUpdateDescriptor descriptor) throws SIdentityException;

    /**
     * Create profileMetadataValue in DB for give profileMetadataValue object
     *
     * @param customUserInfo
     *        A profileMetadataValue object
     * @throws SIdentityException
     */
    SCustomUserInfoValue createCustomUserInfoValue(SCustomUserInfoValue customUserInfo) throws SIdentityException;

    /**
     * Update profileMetadataValue according to the descriptor
     *
     * @param customUserInfo
     *        The profileMetadataValue will be updated
     * @param descriptor
     *        The update description
     * @throws SIdentityException
     */
    void updateCustomUserInfoValue(SCustomUserInfoValue customUserInfo, EntityUpdateDescriptor descriptor) throws SIdentityException;

    /**
     * Create role in DB for the given role
     *
     * @param role
     *        A role object
     * @throws SIdentityException
     */
    void createRole(SRole role) throws SIdentityException;

    /**
     * Update role according to the descriptor
     *
     * @param role
     *        The role will be updated
     * @param descriptor
     *        The update description
     * @throws SIdentityException
     */
    void updateRole(SRole role, EntityUpdateDescriptor descriptor) throws SIdentityException;

    /**
     * Create group in DB for the given group object
     *
     * @param group
     *        A group object
     * @throws SGroupCreationException
     */
    void createGroup(SGroup group) throws SGroupCreationException;

    /**
     * Update group according to the descriptor
     *
     * @param group
     *        The group will be updated
     * @param descriptor
     *        The update description
     * @throws SIdentityException
     */
    void updateGroup(SGroup group, EntityUpdateDescriptor descriptor) throws SIdentityException;

    /**
     * Create userMembership in DB for the given userMembership object
     *
     * @param userMembership
     *        A userMembership object
     * @throws SUserMembershipCreationException
     */
    void createUserMembership(SUserMembership userMembership) throws SUserMembershipCreationException;

    /**
     * Update userMembership according to the descriptor
     *
     * @param userMembership
     *        The userMembership will be updated
     * @param descriptor
     *        The update description
     * @throws SIdentityException
     */
    void updateUserMembership(SUserMembership userMembership, EntityUpdateDescriptor descriptor) throws SIdentityException;

    /**
     * Delete the specific user
     *
     * @param user
     *        The user will be deleted
     * @throws SUserDeletionException
     */
    void deleteUser(SUser user) throws SUserDeletionException;

    /**
     * Delete user by its id
     *
     * @param userId
     *        The identifier of user
     * @throws SUserDeletionException
     */
    void deleteUser(long userId) throws SUserDeletionException;

    /**
     * Delete all users for the connected tenant
     *
     * @throws SUserDeletionException
     * @since 6.1
     */
    void deleteAllUsers() throws SUserDeletionException;

    /**
     * Delete the specific custom user info
     *
     * @param metadataDefinition
     *        The custom user info object will be deleted
     * @throws SIdentityException
     */
    void deleteCustomUserInfoDefinition(SCustomUserInfoDefinition metadataDefinition) throws SIdentityException;

    /**
     * Delete the id specified custom user info
     *
     * @param customUserInfoDefinitionId
     *        The identifier of custom user info
     * @throws SIdentityException
     */
    void deleteCustomUserInfoDefinition(long customUserInfoDefinitionId) throws SIdentityException;

    /**
     * Delete the specific profileMetadataValue
     *
     * @param customUserInfo
     *        The profileMetadataValue object will be deleted
     * @throws SIdentityException
     */
    void deleteCustomUserInfoValue(SCustomUserInfoValue customUserInfo) throws SIdentityException;

    /**
     * Delete the id specified profileMetadataValue
     *
     * @param customUserInfoValueId
     *        The identifier of profileMetadataValue
     * @throws SIdentityException
     */
    void deleteCustomUserInfoValue(long customUserInfoValueId) throws SIdentityException;

    /**
     * Delete the specific role
     *
     * @param role
     *        The role will be deleted
     * @throws SRoleDeletionException
     */
    void deleteRole(SRole role) throws SRoleDeletionException;

    /**
     * Delete the id specified role
     *
     * @param roleId
     *        The role identifier
     * @throws SRoleNotFoundException
     *         Error occurs when no role found with the specific roleId
     * @throws SRoleDeletionException
     */
    void deleteRole(long roleId) throws SRoleNotFoundException, SRoleDeletionException;

    /**
     * Delete all roles for the connected tenant
     *
     * @throws SRoleDeletionException
     * @since 6.1
     */
    void deleteAllRoles() throws SRoleDeletionException;

    /**
     * Delete the specific group
     *
     * @param group
     *        The group will be deleted
     * @throws SGroupDeletionException
     */
    void deleteGroup(SGroup group) throws SGroupDeletionException;

    /**
     * Delete the id specified group
     *
     * @param groupId
     *        The identifier of group
     * @throws SGroupNotFoundException
     *         Error occurs when no group found with the specific groupId
     * @throws SGroupDeletionException
     */
    void deleteGroup(long groupId) throws SGroupNotFoundException, SGroupDeletionException;

    /**
     * Delete all groups for the connected tenant
     *
     * @throws SGroupDeletionException
     * @since 6.1
     */
    void deleteAllGroups() throws SGroupDeletionException;

    /**
     * Delete the specific userMembership
     *
     * @param userMembership
     *        The userMembership will be deleted
     * @throws SMembershipDeletionException
     */
    void deleteUserMembership(SUserMembership userMembership) throws SMembershipDeletionException;

    /**
     * Delete the specific light userMembership
     *
     * @param userMembership
     * @throws SMembershipDeletionException
     * @since 6.1
     */
    void deleteLightUserMembership(SUserMembership userMembership) throws SMembershipDeletionException;

    /**
     * Delete the id specified userMembership
     *
     * @param userMembershipId
     *        The identifier of userMembership
     * @throws SMembershipDeletionException
     */
    void deleteUserMembership(long userMembershipId) throws SMembershipDeletionException;

    /**
     * Delete all user memberships for the connected tenant
     *
     * @throws SMembershipDeletionException
     * @since 6.1
     */
    void deleteAllUserMemberships() throws SMembershipDeletionException;

    /**
     * Get total number of users according to specific query options
     *
     * @param options
     *        The QueryOptions object containing some query conditions
     * @return the satisfied user number
     * @throws SBonitaReadException
     */
    long getNumberOfUsers(QueryOptions options) throws SBonitaReadException;

    /**
     * Search users according to specific query options
     *
     * @param options
     *        The QueryOptions object containing some query conditions
     * @return a list of SUser objects
     * @throws SBonitaReadException
     */
    List<SUser> searchUsers(QueryOptions options) throws SBonitaReadException;

    /**
     * Get total number of roles according to specific query options
     *
     * @param options
     *        The QueryOptions object containing some query conditions
     * @return the satisfied role number
     * @throws SBonitaReadException
     */
    long getNumberOfRoles(QueryOptions options) throws SBonitaReadException;

    /**
     * Search roles according to specific query options
     *
     * @param options
     *        The QueryOptions object containing some query conditions
     * @return a list of SRole objects
     * @throws SBonitaReadException
     */
    List<SRole> searchRoles(QueryOptions options) throws SBonitaReadException;

    /**
     * Get total number of groups according to specific query options
     *
     * @param options
     *        The QueryOptions object containing some query conditions
     * @return the group number
     * @throws SBonitaReadException
     */
    long getNumberOfGroups(QueryOptions options) throws SBonitaReadException;

    /**
     * Search groups according to specific query options
     *
     * @param options
     *        The QueryOptions object containing some query conditions
     * @return a list of SGroup objects
     * @throws SBonitaReadException
     */
    List<SGroup> searchGroups(QueryOptions options) throws SBonitaReadException;

    /**
     * Get total number of userMemberships contains specific group and role
     *
     * @param groupId
     *        The identifier of group
     * @param roleId
     *        The identifier of role
     * @return the number of userMemberships
     * @throws SIdentityException
     */
    long getNumberOfUsersByMembership(long groupId, long roleId) throws SIdentityException;

    /**
     * Get light userMembership by its id
     *
     * @param userMembershipId
     *        The identifier of userMembership
     * @return a SUserMembership object without userName, groupName and roleName
     * @throws SIdentityException
     */
    SUserMembership getLightUserMembership(long userMembershipId) throws SIdentityException;

    /**
     * Get light userMembership in a specific interval, this is used for pagination
     *
     * @param startIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfElements
     *        Number of result we want to get. Maximum number of result returned.
     * @return a list of SUserMembership objects without userName, groupName and roleName
     * @throws SIdentityException
     */
    List<SUserMembership> getLightUserMemberships(int startIndex, int numberOfElements) throws SIdentityException;

    /**
     * delete children groups of the given group if there is some
     *
     * @param groupId
     *        The index of the group to delete
     * @throws SGroupDeletionException
     * @throws SGroupNotFoundException
     */
    List<Long> deleteChildrenGroup(long groupId) throws SGroupDeletionException, SGroupNotFoundException;

    /**
     * Return the user contact info for a specific user.
     *
     * @param userId
     *        the ID of the user to retrieve the contact info from
     * @param isPersonal
     *        Do we want personal contact information (or professional) ?
     * @return the corresponding SContactInfo, if found
     * @throws SIdentityException
     *         if a Read problem occurred
     */
    SContactInfo getUserContactInfo(long userId, boolean isPersonal) throws SIdentityException;

    /**
     * Create user contact information for given data
     *
     * @param contactInfo
     *        The user contact information object
     * @return
     *         The contact info created
     * @throws SUserCreationException
     */
    SContactInfo createUserContactInfo(SContactInfo contactInfo) throws SUserCreationException;

    /**
     * Update user contact information according to the descriptor
     *
     * @param contactInfo
     *        The user contact information to be updated
     * @param descriptor
     *        The update description
     * @throws SUserUpdateException
     */
    void updateUserContactInfo(SContactInfo contactInfo, EntityUpdateDescriptor descriptor) throws SIdentityException;

    /**
     * Create user in DB for given user and
     *
     * @param user
     *        The user object
     * @throws SUserCreationException
     */
    @Deprecated
    SUser createUserWithoutEncryptingPassword(SUser user) throws SUserCreationException;
}
