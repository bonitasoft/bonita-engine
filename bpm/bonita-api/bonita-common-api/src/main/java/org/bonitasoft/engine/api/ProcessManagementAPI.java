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
import java.util.Set;

import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMappingExportException;
import org.bonitasoft.engine.bpm.actor.ActorMappingImportException;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.actor.ActorUpdater;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.category.CategoryNotFoundException;
import org.bonitasoft.engine.bpm.category.CategoryUpdater;
import org.bonitasoft.engine.bpm.connector.ConnectorCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.connector.ConnectorNotFoundException;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import org.bonitasoft.engine.bpm.parameter.ParameterInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessExportException;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * This API deals with definition objects such as {@link ProcessDefinition}, {@link ProcessDeploymentInfo}, {@link Category}, ...
 * It enables interaction with the lifecycle of the process definition.
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Arthur Freycon
 * @author Emmanuel Duchastenier
 * @version 6.3.5
 * @since 6.0.0
 */
public interface ProcessManagementAPI {

    /**
     * Deploys a {@link BusinessArchive} which contains a {@link DesignProcessDefinition} and its dependencies.
     *
     * @param businessArchive
     *        The archive to deploy.
     * @return The process definition.
     * @throws AlreadyExistsException
     *         If a process with same name and version is already deployed.
     * @throws ProcessDeployException
     *         If an exception occurs when deploying the archive.
     * @see BusinessArchive
     * @since 6.0
     */
    ProcessDefinition deploy(BusinessArchive businessArchive) throws AlreadyExistsException, ProcessDeployException;

    /**
     * Deploys a simple {@link DesignProcessDefinition} (without any dependencies).
     *
     * @param designProcessDefinition
     *        The description of a process definition.
     * @return The process definition corresponding of the description.
     * @throws AlreadyExistsException
     *         If a process with same name and version is already deployed.
     * @throws ProcessDeployException
     *         If an exception occurs when deploying the process.
     * @see #deploy(BusinessArchive)
     * @since 6.0
     */
    ProcessDefinition deploy(DesignProcessDefinition designProcessDefinition) throws AlreadyExistsException, ProcessDeployException;

    /**
     * Enables the process definition.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws ProcessEnablementException
     *         If an exception occurs during the process enablement.
     * @since 6.0
     */
    void enableProcess(long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessEnablementException;

    /**
     * Disables the process definition by giving its identifier. A process can only be disabled if it is enabled.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws ProcessActivationException
     *         If an exception occurs during the process disablement.
     * @since 6.0
     */
    void disableProcess(long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException;

    /**
     * Returns the process definition by giving its identifier.
     * If the identifier is null, a ProcessDefinitionNotFoundException is thrown.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @return The process definition referenced by the identifier.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws RetrieveException
     *         If an exception occurs when getting the process definition.
     * @since 6.0
     */
    ProcessDefinition getProcessDefinition(long processDefinitionId) throws ProcessDefinitionNotFoundException;

    /**
     * Deletes a process definition by giving its identifier. A process can only be deleted if it is disabled.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @throws DeletionException
     *         If an exception occurs during process deletion.
     * @throws org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException
     *         If a process instance cannot be deleted because of a parent that is still active
     * @since 6.0
     * @see #deleteProcessDefinition(long)
     * @deprecated As of release 6.1, replaced by {@link #deleteProcessDefinition(long)}
     */
    @Deprecated
    void deleteProcess(long processDefinitionId) throws DeletionException;

    /**
     * Deletes process definitions by giving their identifiers. If any specified identifier does not refer to a real process definition, or if an exception
     * occurs, no process definition is deleted.
     *
     * @param processDefinitionIds
     *        The list of identifiers of process definitions.
     * @throws DeletionException
     *         If an exception occurs during process deletion.
     * @throws org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException
     *         If a process instance cannot be deleted because of a parent that is still active
     * @see #deleteProcessDefinitions(List)
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #deleteProcessDefinitions(List)}
     */
    @Deprecated
    void deleteProcesses(List<Long> processDefinitionIds) throws DeletionException;

    /**
     * Deletes a process definition by giving its identifier. A process can only be deleted if it is disabled and it has no more existing process instances.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @throws DeletionException
     *         If an exception occurs during process deletion.
     * @since 6.1
     */
    void deleteProcessDefinition(long processDefinitionId) throws DeletionException;

    /**
     * Deletes process definitions by giving their identifiers. If any specified identifier does not refer to a real process definition, or if an exception
     * occurs, no process definition is deleted. All instances of given processes must be deleted prior to calling this operation.
     *
     * @param processDefinitionIds
     *        The list of identifiers of process definitions.
     * @throws DeletionException
     *         If an exception occurs during process deletion.
     * @see #deleteProcess(long)
     * @since 6.1
     */
    void deleteProcessDefinitions(List<Long> processDefinitionIds) throws DeletionException;

    /**
     * Deploys, enables and returns a process.
     *
     * @param designProcessDefinition
     *        The description of a process definition.
     * @return ProcessDefinition the process definition corresponding of the description.
     * @throws AlreadyExistsException
     *         If a process with same name and version was already deployed.
     * @throws ProcessEnablementException
     *         If a process cannot be enabled.
     * @throws InvalidProcessDefinitionException
     *         If the designProcessDefinition is invalid.
     * @throws ProcessDeployException
     *         If an exception occurs when deploying the process.
     * @see #deploy(DesignProcessDefinition)
     * @see #enableProcess(long)
     * @since 6.0
     */
    ProcessDefinition deployAndEnableProcess(DesignProcessDefinition designProcessDefinition) throws ProcessDeployException, ProcessEnablementException,
            AlreadyExistsException, InvalidProcessDefinitionException;

    /**
     * Deploys and enables a process by giving a {@link BusinessArchive}.
     *
     * @param businessArchive
     *        The archive ready to deploy.
     * @return ProcessDefinition Process definition by given a business archive.
     * @throws ProcessDeployException
     *         If an exception occurs when deploying the archive.
     * @throws AlreadyExistsException
     *         If a process with same name and same version already exists.
     * @throws ProcessEnablementException
     *         If a process cannot be enabled.
     */
    ProcessDefinition deployAndEnableProcess(BusinessArchive businessArchive) throws ProcessDeployException, ProcessEnablementException, AlreadyExistsException;

    /**
     * Returns a list of problems if the process is configured incorrectly or the configuration is incomplete.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @return a list of problems or an empty list.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws RetrieveException
     *         If an exception occurs when getting the problems of the process definition.
     * @since 6.0
     */
    List<Problem> getProcessResolutionProblems(long processDefinitionId) throws ProcessDefinitionNotFoundException;

    /**
     * Disables and deletes the process.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws ProcessActivationException
     *         If an exception occurs while disabling the process.
     * @throws DeletionException
     *         If an exception occurs while deleting the process.
     * @see #disableProcess(long)
     * @see #deleteProcess(long)
     * @deprecated As of release 6.1, replaced by {@link #disableAndDeleteProcessDefinition(long)}
     * @since 6.0
     */
    @Deprecated
    void disableAndDelete(long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException, DeletionException;

    /**
     * Gets the current number of process definitions in all states.
     *
     * @return The number of process definitions.
     * @throws RetrieveException
     *         If an exception occurs when getting the number of the process definitions.
     * @since 6.0
     */
    long getNumberOfProcessDeploymentInfos();

    /**
     * Gets the deployment information of a process definition by giving the process definition identifier.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @return The deployment information of the process definition.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws RetrieveException
     *         If an exception occurs when getting the process deployment information.
     * @since 6.0
     */
    ProcessDeploymentInfo getProcessDeploymentInfo(long processDefinitionId) throws ProcessDefinitionNotFoundException;

    /**
     * Updates the process deployment information for a specified process.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @param processDeploymentInfoUpdater
     *        The description which describe how to update the process deployment information.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws UpdateException
     *         If an exception occurs when updating the process deployment information.
     * @since 6.0
     */
    void updateProcessDeploymentInfo(long processDefinitionId, ProcessDeploymentInfoUpdater processDeploymentInfoUpdater)
            throws ProcessDefinitionNotFoundException, UpdateException;

    /**
     * Returns a paged list of process deployment information for a number of processes.
     *
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of process deployment information results per page.
     * @param sortCriterion
     *        The sorting criterion.
     * @return The ordered list of process deployment informations.
     * @throws RetrieveException
     *         If an exception occurs when getting the process deployment informations.
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfos(int startIndex, int maxResults, ProcessDeploymentInfoCriterion sortCriterion);

    /**
     * Returns the number of actors in a process definition.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @return The number of actors in the process.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @since 6.0
     */
    int getNumberOfActors(long processDefinitionId) throws ProcessDefinitionNotFoundException;

    /**
     * Returns the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @return The actor.
     * @throws ActorNotFoundException
     *         If an identifier does not refer to an existing actor.
     * @since 6.0
     */
    ActorInstance getActor(long actorId) throws ActorNotFoundException;

    /**
     * Returns a paged list of actors in a process.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of actors per page.
     * @param sort
     *        The sorting criterion.
     * @return The ordered list of actors.
     * @since 6.0
     */
    List<ActorInstance> getActors(long processDefinitionId, int startIndex, int maxResults, ActorCriterion sort);

    /**
     * Returns a paged list of members of an actor.
     * An actor member can be a user, a role, a group, or a membership. An actor member is created when a user, role, group, or membership is mapped to the
     * actor.
     * No ordering must be assumed on the list of results.
     *
     * @param actorId
     *        The identifier of the actor.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of actor members per page.
     * @return The list of actor members.
     * @since 6.0
     */
    List<ActorMember> getActorMembers(long actorId, int startIndex, int maxResults);

    /**
     * Counts the number of members mapped to the actor.
     * An actor member can be a user, a role, a group, or a membership. An actor member is created when a user, role, group, or membership is mapped to the
     * actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @return The number of actors members of the actor
     * @since 6.0
     */
    long getNumberOfActorMembers(long actorId);

    /**
     * Counts the number of users mapped to the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @return The number of users mapped to the actor.
     * @since 6.0
     */
    long getNumberOfUsersOfActor(long actorId);

    /**
     * Counts the number of roles mapped to the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @return The number of roles mapped to the actor.
     * @since 6.0
     */
    long getNumberOfRolesOfActor(long actorId);

    /**
     * Counts the number of groups mapped to the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @return The number of groups mapped to the actor.
     * @since 6.0
     */
    long getNumberOfGroupsOfActor(long actorId);

    /**
     * Counts the number of memberships mapped to the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @return The total number of user memberships mapped to an actor
     * @since 6.0
     */
    long getNumberOfMembershipsOfActor(long actorId);

    /**
     * Updates the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @param actorUpdater
     *        The descriptor which contains the fields to update.
     * @return The actor.
     * @throws ActorNotFoundException
     *         If an identifier does not refer to an existing actor.
     * @throws UpdateException
     *         If an exception occurs when updating the actor.
     * @since 6.0
     */
    ActorInstance updateActor(long actorId, ActorUpdater actorUpdater) throws ActorNotFoundException, UpdateException;

    /**
     * Maps the user to the actor. The user will be mapped to the actor as an {@link ActorMember}.
     *
     * @param actorId
     *        The identifier of the actor.
     * @param userId
     *        The identifier of the user.
     * @return The couple actor/user as an actor member.
     * @throws CreationException
     *         If an exception occurs when creating the actor mapping.
     * @throws AlreadyExistsException
     *         If the association already exists.
     * @see IdentityAPI#getUser(long)
     * @since 6.0
     */
    ActorMember addUserToActor(long actorId, long userId) throws CreationException, AlreadyExistsException;

    /**
     * Maps a user to the actor of the process definition. The user will be mapped to the actor as an {@link ActorMember}.
     *
     * @param actorName
     *        The name of the actor.
     * @param processDefinition
     *        The process definition.
     * @param userId
     *        The identifier of the user.
     * @return The couple actor/user as an actor member.
     * @throws ActorNotFoundException
     *         If the name does not refer to an existing actor of the process definition.
     * @throws CreationException
     *         If an exception occurs when creating the actor mapping.
     * @throws AlreadyExistsException
     *         If the association already exists.
     * @see IdentityAPI#getUser(long)
     * @since 6.0
     */
    ActorMember addUserToActor(String actorName, ProcessDefinition processDefinition, long userId) throws ActorNotFoundException, CreationException,
            AlreadyExistsException;

    /**
     * Maps the group to the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @param groupId
     *        The identifier of the group.
     * @return The couple actor/group as an actor member.
     * @throws CreationException
     *         If the exception occurs when creating the actor mapping.
     * @throws AlreadyExistsException
     *         If the association already exists.
     * @see IdentityAPI#getGroup(long)
     * @since 6.0
     */
    ActorMember addGroupToActor(long actorId, long groupId) throws CreationException, AlreadyExistsException;

    /**
     * Maps the group to the actor of the process definition.
     *
     * @param actorName
     *        The name of the actor.
     * @param groupId
     *        The identifier of the group.
     * @param processDefinition
     *        The process definition.
     * @return The couple actor/group as an actor member.
     * @throws ActorNotFoundException
     *         If the name does not refer to an existing actor of the process definition.
     * @throws CreationException
     *         If an exception occurs when creating the actor mapping.
     * @throws AlreadyExistsException
     *         If the association already exists.
     * @since 6.0
     */
    ActorMember addGroupToActor(String actorName, long groupId, ProcessDefinition processDefinition) throws ActorNotFoundException, CreationException,
            AlreadyExistsException;

    /**
     * Maps the role to the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @param roleId
     *        The identifier of the role.
     * @return The couple actor/role as an actor member.
     * @throws CreationException
     *         If an exception occurs when creating the actor mapping.
     * @since 6.0
     */
    ActorMember addRoleToActor(long actorId, long roleId) throws CreationException;

    /**
     * Maps the role to the actor of the process definition.
     *
     * @param actorName
     *        The name of the actor.
     * @param processDefinition
     *        The process definition.
     * @param roleId
     *        The identifier of the role.
     * @return The couple actor/role as an actor member.
     * @throws ActorNotFoundException
     *         If the name does not refer to an existing actor of the process definition.
     * @throws CreationException
     *         If an exception occurs when creating the actor mapping.
     * @since 6.0
     */
    ActorMember addRoleToActor(String actorName, ProcessDefinition processDefinition, long roleId) throws ActorNotFoundException, CreationException;

    /**
     * Maps the role and the group to the actor.
     *
     * @param actorId
     *        The identifier of the actor.
     * @param roleId
     *        The identifier of the role.
     * @param groupId
     *        The identifier of the group.
     * @return The tuple actor/role/group as an actor member.
     * @throws CreationException
     *         If an exception occurs when creating the actor mapping.
     * @since 6.0
     */
    ActorMember addRoleAndGroupToActor(long actorId, long roleId, long groupId) throws CreationException;

    /**
     * Maps the role and the group to the actor of the process definition.
     *
     * @param actorName
     *        The name of the actor.
     * @param processDefinition
     *        The process definition.
     * @param roleId
     *        The identifier of the role.
     * @param groupId
     *        The identifier of the role.
     * @return The tuple actor/role/group as an actor member.
     * @throws ActorNotFoundException
     *         If the actor name does not refer to an existing actor in the process definition.
     * @throws CreationException
     *         If an exception occurs when creating the actor mapping.
     * @since 6.0
     */
    ActorMember addRoleAndGroupToActor(String actorName, ProcessDefinition processDefinition, long roleId, long groupId) throws ActorNotFoundException,
            CreationException;

    /**
     * Deletes the actor member. This removes the mapping between the user, group, role, or membership and the actor.
     * The user, group, role, or membership is not removed from the organization.
     *
     * @param actorMemberId
     *        The identifier of the actor member
     * @throws DeletionException
     *         If an exception occurs when deleting the actor mapping.
     * @since 6.0
     */
    void removeActorMember(long actorMemberId) throws DeletionException;

    /**
     * Imports into the process definition an actor mapping in XML format.
     *
     * @param processDefinitionId
     *        The identifier of the process.
     * @param xmlContent
     *        The XML content of the mapping. If null, nothing is performed.
     * @throws ActorMappingImportException
     *         If an exception occurs when importing the actor mapping.
     * @since 6.0
     */
    void importActorMapping(long processDefinitionId, String xmlContent) throws ActorMappingImportException;

    /**
     * Imports to the process definition, the actor mapping in XML format as a byte array.
     *
     * @param processDefinitionId
     *        The identifier of the process.
     * @param actorMappingXML
     *        The XML content of the mapping as a byte array. If null, nothing is performed.
     * @throws ActorMappingImportException
     *         If an exception occurs when importing the actor mapping.
     * @since 6.0
     */
    void importActorMapping(long processDefinitionId, byte[] actorMappingXML) throws ActorMappingImportException;

    /**
     * Exports the actor mapping of the process definition. The result contains the mapping in XML format.
     *
     * @param processDefinitionId
     *        The identifier of the process.
     * @return The XML content of the mapping.
     * @throws ActorMappingExportException
     *         If an exception occurs when exporting the actor mapping.
     * @since 6.0
     */
    String exportActorMapping(long processDefinitionId) throws ActorMappingExportException;

    /**
     * Adds a category.
     * A category is a string that can be assigned to processes, to make it easier
     * to identify sets of related processes. For example, you could have
     * a category called hr to identify all HR processes, or a category called
     * finance to identify all processes used in the purchasing and accounts departments.
     *
     * @param name
     *        The name of the category.
     * @param description
     *        The description of the category.
     * @return The category.
     * @throws AlreadyExistsException
     *         If a category already exists with the given name.
     * @throws CreationException
     *         If an exception occurs when creating the category.
     * @since 6.0
     */
    Category createCategory(String name, String description) throws AlreadyExistsException, CreationException;

    /**
     * Counts the number of categories.
     *
     * @return The number of categories.
     * @since 6.0
     */
    long getNumberOfCategories();

    /**
     * Returns a paged list of categories.
     *
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of categories.
     * @param sortCriterion
     *        The sorting criterion.
     * @return The ordered list of categories.
     * @since 6.0
     */
    List<Category> getCategories(int startIndex, int maxResults, CategoryCriterion sortCriterion);

    /**
     * Returns the category.
     *
     * @param categoryId
     *        The identifier of the category.
     * @return The category.
     * @throws CategoryNotFoundException
     *         If the identifier does not refer to an existing category.
     * @since 6.0
     */
    Category getCategory(long categoryId) throws CategoryNotFoundException;

    /**
     * Associates the process definition with the category.
     *
     * @param categoryId
     *        The identifier of the category.
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @throws AlreadyExistsException
     *         If the association category/process already exists.
     * @throws CreationException
     *         If an exception occurs while adding the process to the category.
     * @since 6.0
     */
    void addProcessDefinitionToCategory(long categoryId, long processDefinitionId) throws AlreadyExistsException, CreationException;

    /**
     * Associates a list of process definitions with the category.
     *
     * @param categoryId
     *        The identifier of the category.
     * @param processDefinitionIds
     *        The identifiers of the process definitions.
     * @throws AlreadyExistsException
     *         If an association category/process already exists.
     * @throws CreationException
     *         If an exception occurs while adding the process to the category.
     * @since 6.0
     */
    void addProcessDefinitionsToCategory(long categoryId, List<Long> processDefinitionIds) throws AlreadyExistsException, CreationException;

    /**
     * Counts the number of categories of the process definition, that is, the number of categories to which the process belongs.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @return The number of categories of the process.
     * @since 6.0
     */
    long getNumberOfCategories(long processDefinitionId);

    /**
     * Counts the number of process deployment information entries of the category.
     * This is the number of deployed processes in the specified category.
     *
     * @param categoryId
     *        The identifier of the category.
     * @return The number of process deployment informations of the category.
     * @since 6.0
     */
    long getNumberOfProcessDefinitionsOfCategory(long categoryId);

    /**
     * Returns the paged list of process deployment information items for the category.
     *
     * @param categoryId
     *        The identifier of the category.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of process deployment information.
     * @param sort
     *        The sorting criterion.
     * @return The ordered list of process deployment informations of the category.
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosOfCategory(long categoryId, int startIndex, int maxResults, ProcessDeploymentInfoCriterion sort);

    /**
     * Get categories from process definition
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of categories.
     * @param sort
     *        The sorting criterion.
     * @return The ordered list of categories of the process definition.
     * @since 6.0
     */
    List<Category> getCategoriesOfProcessDefinition(long processDefinitionId, int startIndex, int maxResults, CategoryCriterion sort);

    /**
     * Updates the category according to the updater values.
     *
     * @param categoryId
     *        The identifier of the category.
     * @param updater
     *        The role updater.
     * @throws CategoryNotFoundException
     *         If the category identifier does not refer to an existing category.
     * @throws UpdateException
     *         If an exception occurs during the category update.
     * @since 6.0
     */
    void updateCategory(long categoryId, CategoryUpdater updater) throws CategoryNotFoundException, UpdateException;

    /**
     * Deletes a category and its associations. It does not delete the associated process definitions.
     *
     * @param categoryId
     *        The identifier of the category.
     * @throws DeletionException
     *         If an exception occurs when deleting the category.
     * @since 6.0
     */
    void deleteCategory(long categoryId) throws DeletionException;

    /**
     * Deletes the associations of all the process definitions related to the category.
     * It does not delete the associated process definitions.
     *
     * @param categoryId
     *        The identifier of the category.
     * @throws DeletionException
     *         If an error occurs while removing the process definitions of category.
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #removeProcessDefinitionsFromCategory(long, int, int)}
     */
    @Deprecated
    void removeAllProcessDefinitionsFromCategory(long categoryId) throws DeletionException;

    /**
     * Deletes the associations of all the process definitions related to the category given as input parameter respecting the pagination parameters.
     * It does not delete the associated process definitions.
     *
     * @param categoryId
     *        The identifier of the category.
     * @param startIndex
     *        The index
     * @param maxResults
     *        The max number of elements to retrieve per page
     * @return The number of elements that have been removed
     * @throws DeletionException
     *         If an error occurs while removing the process definitions of category.
     * @since 6.1
     */
    long removeProcessDefinitionsFromCategory(long categoryId, int startIndex, int maxResults) throws DeletionException;

    /**
     * Deletes the associations of all categories related the process definition.
     * The process definition and categories are not deleted, but there is no longer an
     * association between them.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @throws DeletionException
     *         If an error occurs while removing the process definition from category.
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #removeCategoriesFromProcessDefinition(long, int, int)}
     */
    @Deprecated
    void removeAllCategoriesFromProcessDefinition(long processDefinitionId) throws DeletionException;

    /**
     * Deletes the associations of categories related the process definition given as input parameter respecting the pagination parameters.
     * The process definition and categories are not deleted, but there is no longer an association between them.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param startIndex
     *        The index
     * @param maxResults
     *        The max number of elements to retrieve per page
     * @return The number of elements that have been removed
     * @throws DeletionException
     *         If an error occurs while removing the process definition from category.
     * @since 6.1
     */
    long removeCategoriesFromProcessDefinition(long processDefinitionId, int startIndex, int maxResults) throws DeletionException;

    /**
     * Counts the number of process definitions which have no category.
     *
     * @return The number of process definitions which have no category.
     * @since 6.0
     */
    long getNumberOfUncategorizedProcessDefinitions();

    /**
     * Returns the paged list of process deployment information items which have no category.
     *
     * @param startIndex
     *        The number of the page (the first page number is 0).
     * @param maxResults
     *        The number of categories.
     * @param sortCriterion
     *        The sorting criterion.
     * @return The ordered list of process deployment informations.
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getUncategorizedProcessDeploymentInfos(int startIndex, int maxResults, ProcessDeploymentInfoCriterion sortCriterion);

    /**
     * Returns the paged list of data definitions of the activity of the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param activityName
     *        The name of the activity.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of data definitions.
     * @return The ordered list of data definitions.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws ActivityDefinitionNotFoundException
     *         If the name does not refer to an existing activity.
     * @since 6.0
     */
    List<DataDefinition> getActivityDataDefinitions(long processDefinitionId, String activityName, int startIndex, int maxResults)
            throws ProcessDefinitionNotFoundException, ActivityDefinitionNotFoundException;

    /**
     * Counts the number of data definitions of the activity of the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param activityName
     *        The name of the activity.
     * @return The number of data definitions of the activity of the process definition.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws ActivityDefinitionNotFoundException
     *         If the name does not refer to an existing activity.
     * @since 6.0
     */
    int getNumberOfActivityDataDefinitions(long processDefinitionId, String activityName) throws ProcessDefinitionNotFoundException,
            ActivityDefinitionNotFoundException;

    /**
     * Returns the paged list of data definitions of the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of data definitions.
     * @return The ordered list of data definitions.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @since 6.0
     */
    List<DataDefinition> getProcessDataDefinitions(long processDefinitionId, int startIndex, int maxResults) throws ProcessDefinitionNotFoundException;

    /**
     * Counts the number of data definitions of the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @return The number of data definitions of the process definition.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @since 6.0
     */
    int getNumberOfProcessDataDefinitions(long processDefinitionId) throws ProcessDefinitionNotFoundException;

    /**
     * Retrieves resources inside .bar file representing the deployed process. Resources to retrieved are specified with a file names pattern. The pattern
     * format must be relative to the root of the business archive, without starting with a '^' * or '/' character. The pattern can contain forward slashes
     * after the first character.
     * eg. if you have an image in resources/folder/image.jpg in your bar file, you can call:
     * <pre>
     * processAPI.getProcessResources(processDefinitionId, "resources/folder/image.jpg");
     * </pre>
     * to retrieve a map with one entry: the key is the path of the resource, the value is the binary content of the resource.
     * <pre>
     * eg.
     * processAPI().getProcessResources(processDefinitionId, ".{@literal *}/.*\\.txt")
     * </pre> would retrieve all .txt files in a first-level subfolder. The key in the map is the path for each matching resource.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param filenamesPattern
     *        The pattern to retrieve the resources.
     * @return The map containing the pairs (name, content) of the matching files.
     * @throws RetrieveException
     *         If an exception occurs when getting the resources of the process definition.
     * @since 6.0
     */
    Map<String, byte[]> getProcessResources(long processDefinitionId, String filenamesPattern) throws RetrieveException;

    /**
     * Returns the identifier of the most recently deployed process definition with the given name. This method does not take into consideration the process
     * version, but only its deployment date.
     *
     * @param processName
     *        The process definition name.
     * @return The identifier of the most recently deployed process definition with the given name.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @since 6.0
     */
    long getLatestProcessDefinitionId(String processName) throws ProcessDefinitionNotFoundException;

    /**
     * Returns the states of the flow node type. Flow nodes are activities, gateways, or events.
     *
     * @param nodeType
     *        The flow node type.
     * @return The set of the states of the flow node type.
     * @since 6.0
     */
    Set<String> getSupportedStates(FlowNodeType nodeType);

    /**
     * Returns the identifier of the process definition with the specified name and version.
     *
     * @param name
     *        The name of the process definition.
     * @param version
     *        The version of the process definition.
     * @return The identifier of the process definition.
     * @throws ProcessDefinitionNotFoundException
     *         If the name and version do not refer to an existing process definition.
     * @since 6.0
     */
    long getProcessDefinitionId(String name, String version) throws ProcessDefinitionNotFoundException;

    /**
     * Returns the paged list of process deployment information items that the actors can start.
     *
     * @param actorIds
     *        The identifiers of the actors.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of process deployment informations.
     * @param sortingCriterion
     *        The sort criterion
     * @return The ordered list of process deployment informations.
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getStartableProcessDeploymentInfosForActors(Set<Long> actorIds, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Checks whether the actors are allowed to start the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param actorIds
     *        The identifiers of the actors.
     * @return true if the actors are allowed to start the process definition; false otherwise.
     * @since 6.0
     */
    boolean isAllowedToStartProcess(long processDefinitionId, Set<Long> actorIds);

    /**
     * Returns the actor initiator of the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @return The actor initiator of the process definition.
     * @throws ActorNotFoundException
     *         If the process definition does not have an actor initiator.
     * @throws ProcessDefinitionNotFoundException
     *         If the process definition corresponding to the given identifier is not found
     * @since 6.0
     */
    ActorInstance getActorInitiator(long processDefinitionId) throws ActorNotFoundException, ProcessDefinitionNotFoundException;

    /**
     * Searches for the number and the list of processes which have been recently started by the user.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of processes which have been recently started by the user.
     * @throws SearchException
     *         If an exception occurs when getting the processes.
     * @since 6.0
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosStartedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for the number and the list of processes that the user can start.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of processes that the user can start.
     * @throws SearchException
     *         If an exception occurs when getting the processes.
     * @since 6.0
     * @deprecated since 6.3.3
     * @see ProcessManagementAPI#searchProcessDeploymentInfosCanBeStartedBy(long, SearchOptions)
     */
    @Deprecated
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfos(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for the number and the list of processes that the user can start.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of processes that the user can start.
     * @throws SearchException
     *         If an exception occurs when getting the processes.
     * @since 6.3.3
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosCanBeStartedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for the number and the list of process definitions.
     *
     * @param searchOptions
     *        The criterion used to search ProcessDeploymentInfo.
     * @return matching process deployment information results.
     * @throws SearchException
     *         If an exception occurs when getting the processes.
     * @since 6.0
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfos(SearchOptions searchOptions) throws SearchException;

    /**
     * Associates the categories to the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param categoryIds
     *        The identifiers of the categories.
     * @throws AlreadyExistsException
     *         If the association category/process has already added.
     * @throws CreationException
     *         If an exception occurs when associating the process with the categories.
     * @since 6.0
     */
    void addCategoriesToProcess(long processDefinitionId, List<Long> categoryIds) throws AlreadyExistsException, CreationException;

    /**
     * Dissociates the categories from the process definition. The process definition itself is unchanged.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param categoryIds
     *        The identifiers of the categories.
     * @throws DeletionException
     *         If an exception occurs when dissociating the categories from the process definition.
     * @since 6.0
     */
    void removeCategoriesFromProcess(long processDefinitionId, List<Long> categoryIds) throws DeletionException;

    /**
     * Searches for the number and the list of uncategorized processes.
     *
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of uncategorized processes.
     * @throws SearchException
     *         If an exception occurs when searching the process deployment information.
     * @since 6.0
     */
    SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDeploymentInfos(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for the number and the list of uncategorized processes supervised by the user.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of uncategorized processes.
     * @throws SearchException
     *         If an exception occurs when searching the process deployment information.
     * @since 6.0
     */
    SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDeploymentInfosSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    @Deprecated
    /**
     * Searches the number and the list of processes that the user can start which have no category.
     *
     * @param userId
     *            The identifier of the user.
     * @param searchOptions
     *            The search criteria.
     * @return The number and the list of uncategorized processes that the user can start.
     * @throws SearchException
     *             If an exception occurs when searching the process deployment information.
     * @since 6.0
     * @deprecated since 6.3.3 @see {@link ProcessManagementAPI#searchUncategorizedProcessDeploymentInfosCanBeStartedBy(long, SearchOptions)}
     */
    SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDeploymentInfosUserCanStart(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches the number and the list of processes that the user can start which have no category.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of uncategorized processes that the user can start.
     * @throws SearchException
     *         If an exception occurs when searching the process deployment information.
     * @since 6.3.3
     */
    SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDeploymentInfosCanBeStartedBy(long userId, SearchOptions searchOptions)
            throws SearchException;

    /**
     * Returns the process deployment information of the process definitions.
     *
     * @param processDefinitionIds
     *        The identifiers of the process definitions.
     * @return The process deployment information of the process definitions, order by name ascending.
     * @since 6.0
     */
    Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromIds(List<Long> processDefinitionIds);

    /**
     * Returns the implementation of a connector of the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param connectorName
     *        The name of the connector.
     * @param connectorVersion
     *        The version of the connector.
     * @return The description of the connector implementation.
     * @throws ConnectorNotFoundException
     *         If an exception occurs when getting the connector implementation.
     * @since 6.0
     */
    ConnectorImplementationDescriptor getConnectorImplementation(long processDefinitionId, String connectorName, String connectorVersion)
            throws ConnectorNotFoundException;

    /**
     * Returns a paged list of connector implementation descriptors for the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxsResults
     *        The maximum number of connector implementations.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The ordered list of connector implementation descriptors of the process definition.
     * @since 6.0
     */
    List<ConnectorImplementationDescriptor> getConnectorImplementations(long processDefinitionId, int startIndex, int maxsResults,
            ConnectorCriterion sortingCriterion);

    /**
     * Returns the number of connector implementations of the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @return The number of connector implementation of the process definition.
     * @since 6.0
     */
    long getNumberOfConnectorImplementations(final long processDefinitionId);

    /**
     * Returns the actor instances.
     *
     * @param actorIds
     *        The identifiers of the actors.
     * @return The actor instances. (key=actorID, value=actor instance)
     * @since 6.0
     */
    Map<Long, ActorInstance> getActorsFromActorIds(List<Long> actorIds);

    /**
     * Returns the processes for which a specified group is the only mapped actor.
     * This is be called before deleting a group from the organization, to make sure that
     * there are no processes that would become unresolved as a result of removing the group.
     * A process that has no actor mapping is unresolved and cannot be started.
     *
     * @param groupId
     *        The identifier of the group.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of processes.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The processes that the group is the last actor.
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForGroup(long groupId, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Returns the processes for which one of the listed groups is the only mapped actors.
     * This is be called before deleting a group from the organization, to make sure that
     * there are no processes that would become unresolved as a result of removing one of the listed groups.
     * A process that has no actor mapping is unresolved and cannot be started.
     *
     * @param groupIds
     *        The identifiers of the groups.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of processes.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The processes that the groups are the last actor(s).
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForGroups(List<Long> groupIds, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Returns the processes for which the role is the only mapped actor.
     * This is be called before deleting a role from the organization, to make sure that
     * there are no processes that would become unresolved as a result of removing the role.
     * A process that has no actor mapping is unresolved and cannot be started.
     *
     * @param roleId
     *        The identifier of the role.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of processes.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The processes that the role is the last actor.
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForRole(long roleId, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Returns the processes for which one of the listed roles is the only mapped actors.
     * This is be called before deleting a role from the organization, to make sure that
     * there are no processes that would become unresolved as a result of removing one of the listed roles.
     * A process that has no actor mapping is unresolved and cannot be started.
     *
     * @param roleIds
     *        The identifiers of the roles.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of processes.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The processes that the roles are actor(s).
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForRoles(List<Long> roleIds, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Returns the processes for which the user is the only mapped actor.
     * This is be called before deleting a user from the organization, to make sure that
     * there are no processes that would become unresolved as a result of removing the user.
     * A process that has no actor mapping is unresolved and cannot be started.
     *
     * @param userId
     *        The identifier of the user.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of processes.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The processes that the user is the last actor.
     * @see #getProcessDeploymentInfosWithActorOnlyForUsers(List, int, int, ProcessDeploymentInfoCriterion)
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForUser(long userId, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Returns the processes for which one of the listed users is the only mapped actor.
     *
     * @param userIds
     *        The identifiers of the users.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of processes.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The processes that the users are the last actor(s).
     * @see #getProcessDeploymentInfosWithActorOnlyForUser(long, int, int, ProcessDeploymentInfoCriterion)
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosWithActorOnlyForUsers(List<Long> userIds, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Returns a specific process definition that include informations such as tasks definition, actors...
     *
     * @param processDefinitionId
     *        Identifier of process definition
     * @return The corresponding process definition with informations.
     * @throws ProcessDefinitionNotFoundException
     *         If the process definition doesn't exist.
     * @since 6.1
     */
    DesignProcessDefinition getDesignProcessDefinition(long processDefinitionId) throws ProcessDefinitionNotFoundException;

    /**
     * Searches the number and the list of processes supervised by the user.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criterion.
     * @return The number and the list of processes supervised by the user.
     * @throws SearchException
     *         If an exception occurs when getting the process deployment information.
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for all process definitions that can be started by users who report to the specified manager.
     *
     * @param managerUserId
     *        The identifier of the manager.
     * @param searchOptions
     *        The search crtierion.
     * @return
     *         The list of process definitions that have at least one initiator who is mapped to a user to who reports to the specified manager.
     * @throws SearchException
     *         If an exception occurs when getting the process deployment information.
     * @since 6.0
     * @deprecated 6.3.3
     * @see ProcessManagementAPI#searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(long, SearchOptions)
     */
    @Deprecated
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosUsersManagedByCanStart(long managerUserId, SearchOptions searchOptions)
            throws SearchException;

    /**
     * TODO do not understand the behavior of this
     * Search for all process definitions that can be started by users managed by a specific user.
     *
     * @param managerUserId
     *        The identifier of the manager.
     * @param searchOptions
     *        The search criterion.
     * @return
     *         The list of process definitions that have at least one initiator who is mapped to a user to who reports to the specified manager.
     * @throws SearchException
     *         If an exception occurs when getting the process deployment information.
     * @since 6.3.3
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(long managerUserId, SearchOptions searchOptions)
            throws SearchException;

    /**
     * Adds the user as a supervisor of the process.
     * A supervisor of a process is responsible for what happens to the process. A supervisor can see
     * the tasks in the process, and can carry out process administration. A supervisor is defined in a ProcessSupervisor
     * object as a mapping of users, groups, or roles to the process supervisor (similar to actor mapping).
     * A process has one ProcessSupervisor; however, as this can be mapped to several users, either explicitly or by
     * mapping groups or roles, the process can be supervised by several people.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param userId
     *        The identifier of the user.
     * @return The user as a process supervisor.
     * @throws CreationException
     *         If an exception occurs when creating the process supervisor.
     * @throws AlreadyExistsException
     *         If the user is already the process supervisor.
     * @since 6.0
     */
    ProcessSupervisor createProcessSupervisorForUser(long processDefinitionId, long userId) throws CreationException, AlreadyExistsException;

    /**
     * Adds the role as a supervisor of the process.
     * A supervisor of a process is responsible for what happens to the process. A supervisor can see
     * the tasks in the process, and can carry out process administration. A supervisor is defined in a ProcessSupervisor
     * object as a mapping of users, groups, or roles to the process supervisor (similar to actor mapping).
     * A process has one ProcessSupervisor; however, as this can be mapped to several users, either explicitly or by
     * mapping groups or roles, the process can be supervised by several people.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param roleId
     *        The identifier of the role.
     * @return The role as a supervisor of the process.
     * @throws CreationException
     *         If an exception occurs when creating the process supervisor.
     * @throws AlreadyExistsException
     *         If the provided role is already a supervisor for the provided process.
     * @since 6.0
     */
    ProcessSupervisor createProcessSupervisorForRole(long processDefinitionId, long roleId) throws CreationException, AlreadyExistsException;

    /**
     * Adds the group as a supervisor of the process.
     * A supervisor of a process is responsible for what happens to the process. A supervisor can see
     * the tasks in the process, and can carry out process administration. A supervisor is defined in a ProcessSupervisor
     * object as a mapping of users, groups, or roles to the process supervisor (similar to actor mapping).
     * A process has one ProcessSupervisor; however, as this can be mapped to several users, either explicitly or by
     * mapping groups or roles, the process can be supervised by several people.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param groupId
     *        The identifier of the group.
     * @return The group as a supervisor of the process.
     * @throws CreationException
     *         If an exception occurs when creating the process supervisor.
     * @throws AlreadyExistsException
     *         If the provided group is already a supervisor for the provided process.
     * @since 6.0
     */
    ProcessSupervisor createProcessSupervisorForGroup(long processDefinitionId, long groupId) throws CreationException, AlreadyExistsException;

    /**
     * Adds the membership as a supervisor of the process.
     * A supervisor of a process is responsible for what happens to the process. A supervisor can see
     * the tasks in the process, and can carry out process administration. A supervisor is defined in a ProcessSupervisor
     * object as a mapping of users, groups, or roles to the process supervisor (similar to actor mapping).
     * A process has one ProcessSupervisor; however, as this can be mapped to several users, either explicitly or by
     * mapping groups or roles, the process can be supervised by several people.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param groupId
     *        The identifier of the group.
     * @param roleId
     *        The identifier of the role.
     * @return The membership as a supervisor of the process.
     * @throws CreationException
     *         If an exception occurs when creating the process supervisor.
     * @throws AlreadyExistsException
     *         If the provided membership (group + role) is already a supervisor for the provided process.
     * @since 6.0
     */
    ProcessSupervisor createProcessSupervisorForMembership(long processDefinitionId, long groupId, long roleId) throws CreationException,
            AlreadyExistsException;

    /**
     * Checks whether the user is the process supervisor.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param userId
     *        The identifier of the user.
     * @return true if the user is currently a supervisor of the process; false otherwise.
     * @since 6.0
     */
    boolean isUserProcessSupervisor(long processDefinitionId, long userId);

    /**
     * Deletes a process supervisor.
     *
     * @param supervisorId
     *        The identifier of the {@link ProcessSupervisor}.
     * @throws DeletionException
     *         If an exception occurs when deleting the process supervisor.
     * @since 6.0
     */
    void deleteSupervisor(long supervisorId) throws DeletionException;

    /**
     * Delete the {@link ProcessSupervisor} object that is identified by this processDefinitionId, userId, roleId and groupId.
     * <p>
     * e.g. to delete the process supervisor that is set for userId 12 and process id 255 call deleteSupervisor(255, 12, null, null)
     * <p>
     * be careful if the user is supervisor because he is in e.g. a group of super visor calling this method with the userId will do nothing, you must find the
     * {@link ProcessSupervisor} that link the user to the process
     *
     * @param processDefinitionId
     *        The Identifier of the process definition to delete the supervisor for.
     * @param userId
     *        The Id of the user used as a supervisor for the given process. Can be null.
     * @param roleId
     *        The Id of the role used as a supervisor for the given process. Can be null.
     * @param groupId
     *        The Id of the group used as a supervisor for the given process. Can be null.
     * @throws DeletionException
     *         If a problem occurs while deleting the supervisor for the given process.
     * @since 6.0
     */
    void deleteSupervisor(Long processDefinitionId, Long userId, Long roleId, Long groupId) throws DeletionException;

    /**
     * Searches for the number and the list of processes supervisors.
     *
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of processes supervisors.
     * @throws SearchException
     *         If an exception occurs when getting the processes supervisors.
     * @since 6.0
     */
    SearchResult<ProcessSupervisor> searchProcessSupervisors(SearchOptions searchOptions) throws SearchException;

    /**
     * Returns a paged list of categories that are not associated with the process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param startIndex
     *        The number of the page (the first page number is 0).
     * @param maxResults
     *        The maximum number of categories.
     * @param sortingCriterion
     *        The sort criterion.
     * @return The categories that are not associated with the process definition.
     */
    List<Category> getCategoriesUnrelatedToProcessDefinition(long processDefinitionId, int startIndex, int maxResults, CategoryCriterion sortingCriterion);

    /**
     * Counts the number of process definitions that do not belong to the category.
     *
     * @param categoryId
     *        The identifier of the category.
     * @return The number of process definitions that have not the category.
     */
    long getNumberOfProcessDeploymentInfosUnrelatedToCategory(long categoryId);

    /**
     * Returns the paginated list of process deployment information items of the category.
     *
     * @param categoryId
     *        The identifier of the category.
     * @param startIndex
     *        The number of the page (the first page number is 0).
     * @param maxResults
     *        The number of process deployment informations.
     * @param sortingCriterion
     *        The sort criterion.
     * @return A list of process unrelated to the category which has categoryId as id
     * @since 6.0
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosUnrelatedToCategory(long categoryId, int startIndex, int maxResults,
            ProcessDeploymentInfoCriterion sortingCriterion);

    /**
     * Searches for the number and the list of users who can start the process.
     * Note: managerUserId is a possible filter.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param searchOptions
     *        The search criteria.
     * @return The number and the list of users who can start the process.
     * @throws SearchException
     *         If an exception occurs when getting the users.
     * @since 6.0
     */
    SearchResult<User> searchUsersWhoCanStartProcessDefinition(long processDefinitionId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get process deployment information from a list of processInstance id
     *
     * @param processInstanceIds
     *        Identifier of the processInstance
     * @return A map of &lt;processInstantsIds,ProcessDeploymentInfos&gt;, ordered by the name of the process ascending
     * @since 6.0
     */
    Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromProcessInstanceIds(List<Long> processInstanceIds);

    /**
     * Get process deployment information from a list of archived processInstance ids
     *
     * @param archivedProcessInstantsIds
     *        Identifier of the archived process instance
     * @return A map of &lt;archivedProcessInstantsIds,ProcessDeploymentInfos&gt;
     * @since 6.0
     */
    Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromArchivedProcessInstanceIds(List<Long> archivedProcessInstantsIds);

    /**
     * Export processes of bar under home by a processDefinition id
     *
     * @param processDefinitionId
     *        Identifier of the processDefinition
     * @return An array of byte
     * @throws ProcessExportException
     *         If an export problem occurs
     * @since 6.0
     */
    byte[] exportBarProcessContentUnderHome(long processDefinitionId) throws ProcessExportException;

    /**
     * Disables and deletes the process.
     *
     * @param processDefinitionId
     *        The process definition identifier.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws ProcessActivationException
     *         If an exception occurs while disabling the process.
     * @throws DeletionException
     *         If an exception occurs while deleting the process.
     * @see #disableProcess(long)
     * @see #deleteProcess(long)
     * @since 6.1
     */
    void disableAndDeleteProcessDefinition(long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException, DeletionException;

    /**
     * Lists the possible users (candidates) of the specified human task definition.
     * <br>
     * If the task contains a user filter, it is not executed.
     * Users are ordered by user name.
     * An empty list is returned if:
     * - the task or the process does not exist
     * - the flow node is not a human task
     *
     * @param processDefinitionId
     *        The identifier of process definition
     * @param humanTaskName
     *        The name of the human task
     * @param startIndex
     *        The start index
     * @param maxResults
     *        The list of users
     * @return The list of users.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @throws RetrieveException
     *         If an exception occurs while retrieving the users
     * @since 6.1
     */
    List<User> getPossibleUsersOfHumanTask(long processDefinitionId, String humanTaskName, int startIndex, int maxResults);

    /**
     * Retrieves the list of user identifiers for the given actor and process.
     *
     * @param processDefinitionId
     *        The process definition identifier
     * @param actorName
     *        The actor name
     * @param startIndex
     *        The start index (the first valid value is zero)
     * @param maxResults
     *        The max number of user identifiers to be retrieved
     * @return Retrieves the list of user identifiers for the given actor and process.
     * @since 6.3.2
     */
    List<Long> getUserIdsForActor(long processDefinitionId, String actorName, int startIndex, int maxResults);

    /**
     * Purges the classLoader of the process definition. The process must be disable and no instances should run, otherwise an UpdateException is thrown.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @throws ProcessDefinitionNotFoundException
     *         If the identifier does not refer to an existing process definition.
     * @throws UpdateException
     *         If the process is not disable or if instances are still running
     * @since 6.4.0
     */
    void purgeClassLoader(long processDefinitionId) throws ProcessDefinitionNotFoundException, UpdateException;


    /**
     * Gets how many parameters the process definition contains.
     *
     * @param processDefinitionId
     *            The identifier of the processDefinition
     * @return The number of parameters of a process definition
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 7.0
     */
    int getNumberOfParameterInstances(long processDefinitionId);

    /**
     * Get a parameter instance by process definition UUID
     *
     * @param processDefinitionId
     *            The identifier of the processDefinition
     * @param parameterName
     *            The parameter name for get ParameterInstance
     * @return The ParameterInstance of the process with processDefinitionUUID and name parameterName
     * @throws org.bonitasoft.engine.exception.NotFoundException
     *             Error thrown if the given parameter is not found.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 7.0
     */
    ParameterInstance getParameterInstance(long processDefinitionId, String parameterName) throws NotFoundException;

    /**
     * Returns the parameters of a process definition or an empty map if the process does not contain any parameter.
     *
     * @param processDefinitionId
     *            The identifier of the processDefinition
     * @param startIndex
     *            The index of the page to be returned. First page has index 0.
     * @param maxResults
     *            The number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion to sort the result
     * @return The ordered list of parameter instances
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 7.0
     */
    List<ParameterInstance> getParameterInstances(long processDefinitionId, int startIndex, int maxResults, ParameterCriterion sort);

}
