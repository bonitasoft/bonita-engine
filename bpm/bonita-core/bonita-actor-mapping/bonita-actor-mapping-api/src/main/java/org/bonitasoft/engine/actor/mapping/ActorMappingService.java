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
package org.bonitasoft.engine.actor.mapping;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @since 6.0
 */
public interface ActorMappingService {

    String ACTOR = "ACTOR";

    String ACTOR_MEMBER = "ACTOR_MEMBER";

    /**
     * Create an actor by given actor
     *
     * @param actor
     *        The given actor without id
     * @return the new created actor with id
     * @throws SActorCreationException
     */
    SActor addActor(SActor actor) throws SActorCreationException;

    /**
     * Create actors by given actors
     *
     * @param actors
     *        The given actors without IDs
     * @return The set of the new created actors
     * @throws SActorCreationException
     */
    Set<SActor> addActors(Set<SActor> actors) throws SActorCreationException;

    /**
     * Get actor by actor id
     * If the actor by a given actorId is not found, it will throw SActorNotFoundException
     *
     * @param actorId
     *        Id of actor
     * @return SActor object response to the given actorId
     * @throws SActorNotFoundException
     * @throws SBonitaReadException
     */
    SActor getActor(long actorId) throws SActorNotFoundException, SBonitaReadException;

    /**
     * Get actor by actor name and scope id
     * If the actor by a given actorName and scopeId is not found, it will throw SActorNotFoundException
     *
     * @param actorName
     *        Name of actor
     * @param scopeId
     *        Id of scope, it can be processDefinitionId
     * @return SActor object corresponding to the given actorName and scopeId
     * @throws SActorNotFoundException
     *         Error thrown if no actor have an id corresponding to the parameter.
     */
    SActor getActor(String actorName, long scopeId) throws SActorNotFoundException;

    /**
     * Get a list of all actors for the id specified user in certain scopes specified by scopeIds
     *
     * @param scopeIds
     *        Ids of scope, it can be processDefinitionId
     * @param userId
     *        Id of user which is added to actor
     * @return The list of SActor Objects
     * @throws SBonitaReadException
     */
    List<SActor> getActors(Set<Long> scopeIds, Long userId) throws SBonitaReadException;

    /**
     * Update actor by its id
     * If the actor by a given actorId is not found, it will throw processDefinitionNotFountExcetion
     *
     * @param actorId
     *        Id of actor
     * @param updateDescriptor
     *        Update description
     * @return the updated actor
     * @throws SActorNotFoundException
     *         Error thrown if no actor have an id corresponding to the parameter actorId.
     * @throws SActorUpdateException
     *         Error thrown if has exceptions while try to update an actor
     * @throws SBonitaReadException
     */
    SActor updateActor(long actorId, EntityUpdateDescriptor updateDescriptor) throws SActorNotFoundException, SActorUpdateException, SBonitaReadException;

    /**
     * Delete actors in the id specified scope
     *
     * @param scopeId
     *        Id of scope, it can be processDefinitionId
     * @throws SActorDeletionException
     *         Error thrown if has exceptions while try to delete actors
     */
    void deleteActors(long scopeId) throws SActorDeletionException;

    /**
     * Add the userId specified user to the actorId specified actor
     *
     * @param actorId
     *        Id of actor
     * @param userId
     *        Id of user
     * @return SActorMember object
     * @throws SActorNotFoundException
     *         Error thrown if no actor have an id corresponding to the parameter actorId.
     * @throws SActorMemberCreationException
     *         Error thrown if has exceptions while try to create the SActorMember object
     */
    SActorMember addUserToActor(long actorId, long userId) throws SActorNotFoundException, SActorMemberCreationException;

    /**
     * Add the groupId specified group to the actorId specified actor
     *
     * @param actorId
     *        Id of actor
     * @param groupId
     *        Id of group
     * @return SActorMember object
     * @throws SActorNotFoundException
     *         Error thrown if no actor have an id corresponding to the parameter actorId.
     * @throws SActorMemberCreationException
     *         Error thrown if has exceptions while try to create the SActorMember object
     */
    SActorMember addGroupToActor(long actorId, long groupId) throws SActorNotFoundException, SActorMemberCreationException;

    /**
     * Add the roleId specified role to the actorId specified actor
     *
     * @param actorId
     *        Id of actor
     * @param roleId
     *        Id of role
     * @return SActorMember object
     * @throws SActorNotFoundException
     *         Error thrown if no actor have an id corresponding to the parameter actorId.
     * @throws SActorMemberCreationException
     *         Error thrown if has exceptions while try to create the SActorMember object
     */
    SActorMember addRoleToActor(long actorId, long roleId) throws SActorNotFoundException, SActorMemberCreationException;

    /**
     * Add the roleId and groupId specified relationship to the actorId specified actor
     *
     * @param actorId
     *        Id of actor
     * @param roleId
     *        Id of role
     * @param groupId
     *        Id of group
     * @return SActorMember object
     * @throws SActorNotFoundException
     *         Error thrown if no actor have an id corresponding to the parameter actorId.
     * @throws SActorMemberCreationException
     *         Error thrown if has exceptions while try to create the SActorMember object
     */
    SActorMember addRoleAndGroupToActor(long actorId, long roleId, long groupId) throws SActorNotFoundException, SActorMemberCreationException;

    /**
     * Remove actorMember for the give actorMemberId
     *
     * @param actorMemberId
     *        Id of actorMember
     * @throws SActorMemberNotFoundException
     *         Error thrown if no actorMember have an id corresponding to the parameter actorMemberId.
     * @throws SActorMemberDeletionException
     *         Error thrown if has exceptions while try to remove the SActorMember object
     */
    SActorMember deleteActorMember(long actorMemberId) throws SActorMemberNotFoundException, SActorMemberDeletionException;

    /**
     * Remove an actor member
     *
     * @param actorMember
     *        the actorMember to remove
     * @throws SActorMemberDeletionException
     *         Error thrown if has exceptions while try to remove the SActorMember object
     */
    void deleteActorMember(final SActorMember actorMember) throws SActorMemberDeletionException;

    /**
     * Get list of SActorMember objects by pagination
     *
     * @param actorId
     *        Id of actor
     * @param index
     *        Index of the record to be retrieved from. First record has pageNumber 0.
     * @param numberOfActorMembers
     *        Number of result we want to get. Maximum number of result returned.
     * @return List of SActorMember objects, ordered by id ascending
     * @throws SBonitaReadException
     */
    List<SActorMember> getActorMembers(long actorId, int index, int numberOfActorMembers) throws SBonitaReadException;

    /**
     * Get number of ActorMembers for give actorId
     *
     * @param actorId
     *        Id of actor
     * @return the number of ActorMembers
     * @throws SBonitaReadException
     */
    long getNumberOfActorMembers(long actorId) throws SBonitaReadException;

    /**
     * Get a list of SActorMember objects for given userId
     *
     * @param userId
     *        Id of user
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has pageNumber 0.
     * @param numberOfActorMembers
     *        Number of result we want to get. Maximum number of result returned.
     * @return List of SActorMember objects, ordered by id ascending
     * @throws SBonitaReadException
     */
    List<SActorMember> getActorMembersOfUser(long userId, int fromIndex, int numberOfActorMembers) throws SBonitaReadException;

    /**
     * Get a list of SActorMember objects for given groupId
     *
     * @param groupId
     *        Id of group
     * @return a list of SActorMember objects, ordered by id ascending
     * @throws SBonitaReadException
     */
    List<SActorMember> getActorMembersOfGroup(long groupId, int index, int numberOfActorMembers) throws SBonitaReadException;

    /**
     * Get a list of SActorMember objects for given roleId
     *
     * @param roleId
     *        Id of role
     * @return a list of SActorMember objects, ordered by id ascending
     * @throws SBonitaReadException
     */
    List<SActorMember> getActorMembersOfRole(long roleId, int fromIndex, int numberOfActorMembers) throws SBonitaReadException;

    /**
     * Is a specified user allowed to start a process?
     *
     * @param userId
     *        Id of user
     * @param processDefinitionId Id of processDefinition
     * @return a list of SActor objects
     * @throws SBonitaReadException
     */
    boolean canUserStartProcessDefinition(long userId, long processDefinitionId) throws SBonitaReadException;

    /**
     * Get a list of actors by the given list of actor ids
     *
     * @param actorIds
     *        the list of actor ids to retrieve
     * @return a list of actors
     * @throws SActorNotFoundException
     * @throws SBonitaReadException
     */
    List<SActor> getActors(List<Long> actorIds) throws SActorNotFoundException, SBonitaReadException;

    /**
     * Get paginated actors
     *
     * @param processDefinitionId
     *        identifier of process definition
     * @return the list of actors
     * @throws SBonitaReadException
     */
    List<SActor> getActors(long processDefinitionId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Return the number of users corresponding to an actor
     *
     * @param actorId
     *        the id of the actor to retrieve the users from
     * @return Number of users mapped to actor
     */
    long getNumberOfUsersOfActor(long actorId);

    /**
     * Get the number of roles of an actor
     *
     * @param actorId
     *        the id corresponding to an actor
     * @return Number of roles mapped to actor
     */
    long getNumberOfRolesOfActor(long actorId);

    /**
     * Get the number of groups corresponding to an actor
     *
     * @param actorId
     *        the id of the actor to retrieve the groups from
     * @return Number of groups mapped to actor
     * @throws RuntimeException
     */
    long getNumberOfGroupsOfActor(long actorId) throws RuntimeException;

    /**
     * Get the number of memberships (role and group) of an actor
     *
     * @param actorId
     *        the id of the actor to retrieve the memberships from
     * @return Number of memberships mapped to actor
     */
    long getNumberOfMembershipsOfActor(long actorId);

    /**
     * Delete all actor members for the connected tenant
     *
     * @throws SActorMemberDeletionException
     * @since 6.1
     */
    void deleteAllActorMembers() throws SActorMemberDeletionException;

    List<Long> getPossibleUserIdsOfActorId(long actorId, int startIndex, int maxResults) throws SBonitaReadException;

    /**
     * Get the actor member
     *
     * @param actorId
     *        The identifier of the actor
     * @param userId
     *        The identifier of the user
     * @param groupId
     *        The identifier of the group
     * @param roleId
     *        The identifier of the role
     * @return The corresponding actor member
     * @throws SBonitaReadException
     * @since 6.3
     */
    SActorMember getActorMember(long actorId, long userId, long groupId, long roleId) throws SBonitaReadException;

}
