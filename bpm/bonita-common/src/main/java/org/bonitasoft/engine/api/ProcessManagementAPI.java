/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.actor.ActorMappingExportException;
import org.bonitasoft.engine.actor.ActorMappingImportException;
import org.bonitasoft.engine.actor.ActorMemberCreationException;
import org.bonitasoft.engine.actor.ActorMemberDeletionException;
import org.bonitasoft.engine.actor.ActorUpdateDescriptor;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.model.ActorInstance;
import org.bonitasoft.engine.bpm.model.ActorMember;
import org.bonitasoft.engine.bpm.model.Category;
import org.bonitasoft.engine.bpm.model.CategoryCriterion;
import org.bonitasoft.engine.bpm.model.Comment;
import org.bonitasoft.engine.bpm.model.ConnectorInstance;
import org.bonitasoft.engine.bpm.model.ConnectorState;
import org.bonitasoft.engine.bpm.model.FlowNodeInstance;
import org.bonitasoft.engine.bpm.model.FlowNodeType;
import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.MemberType;
import org.bonitasoft.engine.bpm.model.Problem;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionCriterion;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfoUpdateDescriptor;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedComment;
import org.bonitasoft.engine.bpm.model.archive.ArchivedFlowElementInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.model.data.DataDefinition;
import org.bonitasoft.engine.bpm.model.privilege.ActorPrivilege;
import org.bonitasoft.engine.bpm.model.privilege.LevelRight;
import org.bonitasoft.engine.bpm.model.privilege.Privilege;
import org.bonitasoft.engine.connector.ConnectorCriterion;
import org.bonitasoft.engine.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.exception.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ActivityInstanceReadException;
import org.bonitasoft.engine.exception.ActorMemberNotFoundException;
import org.bonitasoft.engine.exception.ActorNotFoundException;
import org.bonitasoft.engine.exception.ActorPrivilegeInsertException;
import org.bonitasoft.engine.exception.ActorPrivilegeNotFoundException;
import org.bonitasoft.engine.exception.ActorPrivilegeRemoveException;
import org.bonitasoft.engine.exception.ActorPrivilegeUpdateException;
import org.bonitasoft.engine.exception.ActorUpdateException;
import org.bonitasoft.engine.exception.BonitaReadException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CategoryAlreadyExistException;
import org.bonitasoft.engine.exception.CategoryCreationException;
import org.bonitasoft.engine.exception.CategoryDeletionException;
import org.bonitasoft.engine.exception.CategoryGettingException;
import org.bonitasoft.engine.exception.CategoryMappingException;
import org.bonitasoft.engine.exception.CategoryNotFoundException;
import org.bonitasoft.engine.exception.CategoryUpdateException;
import org.bonitasoft.engine.exception.CommentAddException;
import org.bonitasoft.engine.exception.CommentReadException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.ConnectorNotFoundException;
import org.bonitasoft.engine.exception.DataNotFoundException;
import org.bonitasoft.engine.exception.DeletingEnabledProcessException;
import org.bonitasoft.engine.exception.GroupNotFoundException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.NoSuchActivityDefinitionException;
import org.bonitasoft.engine.exception.ObjectCreationException;
import org.bonitasoft.engine.exception.ObjectDeletionException;
import org.bonitasoft.engine.exception.ObjectModificationException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.exception.ObjectReadException;
import org.bonitasoft.engine.exception.OperationExecutionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.PrivilegeInsertException;
import org.bonitasoft.engine.exception.PrivilegeNotFoundException;
import org.bonitasoft.engine.exception.PrivilegeRemoveException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDefinitionReadException;
import org.bonitasoft.engine.exception.ProcessDeletionException;
import org.bonitasoft.engine.exception.ProcessDeployException;
import org.bonitasoft.engine.exception.ProcessDeploymentInfoUpdateException;
import org.bonitasoft.engine.exception.ProcessDisablementException;
import org.bonitasoft.engine.exception.ProcessEnablementException;
import org.bonitasoft.engine.exception.ProcessInstanceCreationException;
import org.bonitasoft.engine.exception.ProcessInstanceDeletionException;
import org.bonitasoft.engine.exception.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.ProcessInstanceReadException;
import org.bonitasoft.engine.exception.ProcessResourceException;
import org.bonitasoft.engine.exception.RoleNotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UserNotFoundException;
import org.bonitasoft.engine.process.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.api.ParameterSorting;
import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.exception.InvalidParameterValueException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 */
public interface ProcessManagementAPI {

    /**
     * Get a process definition by process definition UUID
     * If no process definition to the parameter, it will throw an ProcessDefinitionNotFoundException.
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @return the matching of ProcessDefinition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the value of processDefinitionUUID parameter.
     * @throws ProcessDefinitionReadException
     *             Generic exception thrown if some processDefinition object cannot be instantiated and then returned to caller.
     */
    ProcessDefinition getProcessDefinition(Long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDefinitionReadException;

    /**
     * Delete process having the id in parameter
     * If process having the id is not found, it will throw ProcessDefinitionNotFoundException
     * If process having the id is enabled, it will throw DeletingEnabledProcessException
     * 
     * @param processId
     *            the id of the process
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the value of processId parameter.
     * @throws ProcessDeletionException
     *             Generic exception thrown if some processDefinition object cannot be deleted and then returned to caller.
     * @throws DeletingEnabledProcessException
     *             Generic exception thrown if some processDefinition object is still Enabled and then returned to caller.
     */
    // FIXME it's the processDef id here not the process definition deploy info,
    // what to do?
    void deleteProcess(long processId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeletionException,
            DeletingEnabledProcessException;

    /**
     * delete multiple processes by given processIds
     * If process having the id is not found, it will thrown ProcessDefinitionNotFoundException
     * If process having the id is enabled, it will thrown DeletingEnabledProcessException
     * 
     * @param processIds
     *            A list having processId
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the value of processIds parameter.
     * @throws ProcessDeletionException
     *             Generic exception thrown if some processDefinition object cannot be deleted and then returned to caller.
     * @throws DeletingEnabledProcessException
     *             Generic exception thrown if some processDefinition object is still Enabled and then returned to caller.
     */
    void deleteProcesses(List<Long> processIds) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeletionException,
            DeletingEnabledProcessException;

    /**
     * Delete process instances by its process definition id
     * If process having the id is not found, it will thrown ProcessDefinitionNotFoundException
     * If process having the id is enabled, it will thrown DeletingEnabledProcessException
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the value of processDefinitionId parameter.
     * @throws ProcessDeletionException
     *             Generic exception thrown if some processDefinition object cannot be deleted and then returned to caller.
     */
    void deleteProcessInstances(long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeletionException;

    /**
     * Delete the process instance having the identifier in parameter
     * 
     * @param processInstanceId
     *            identifier of the process instance to delete
     * @throws InvalidSessionException
     * @throws ProcessInstanceNotFoundException
     *             thrown if the process instance is not found
     * @throws ProcessInstanceDeletionException
     *             thrown if the process instance could not be deleted
     */
    void deleteProcessInstance(long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException, ProcessInstanceDeletionException;

    /**
     * Deploying, return process definition by given a business archive
     * 
     * @param businessArchive
     *            A source having process definition property
     * @return ProcessDefinition Process definition by given a business archive
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDeployException
     *             Generic exception thrown if this process definition has any exceptions
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the value of processDefinitionId parameter.
     */
    ProcessDefinition deploy(BusinessArchive businessArchive) throws InvalidSessionException, ProcessDeployException, ProcessDefinitionNotFoundException;

    /**
     * Returns a list of problems if the process is misconfigured.
     * 
     * @param processId
     *            the process definition identifier
     * @return a list of problems or an empty list
     * @throws InvalidSessionException
     *             occurs if the session is invalid, e.g session expiration
     * @throws ProcessDefinitionNotFoundException
     *             occurs if the given id does not refer to any process definition
     * @throws ProcessResourceException
     *             occurs if something wrong happens during method evaluation
     */
    List<Problem> getProcessResolutionProblems(long processId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessResourceException;

    /**
     * Disables the process by a given processId
     * If the process by a given processId is not found, it will throw processDefinitionNotFountExcetion
     * If some process definition is cannot be disabled, it will throw ProcessDisablementException
     * 
     * @param processId
     *            Identifier of the processDefinition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the value of processId parameter.
     * @throws ProcessDisablementException
     *             Generic exception thrown if has exception during the process to disable.
     */
    void disableProcess(long processId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDisablementException;

    /**
     * enable the process by a given processId
     * If the process by a given processId is not found, it will throw processDefinitionNotFountExcetion
     * If some process definition is cannot be enabled, it will throw ProcessEnablementException
     * 
     * @param processId
     *            Identifier of the processDefinition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the value of processId parameter.
     * @throws ProcessEnablementException
     *             Generic exception thrown if has exception during the process to enable.
     */
    void enableProcess(long processId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessEnablementException;

    /**
     * retrieve all the tasks belong to the given process and are accessible by the given user
     * If the process by a given processId is not found, it will throw processDefinitionNotFountExcetion
     * If the user by a give userId is not found, it will throw UserNotFoundException
     * 
     * @param userId
     *            Identifier of the user
     * @param processId
     *            Identifier of the processDefinition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param pagingCriterion
     *            The criterion to sort the retrieved tasks
     * @return The list of TaskInstance Objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    List<ActivityInstance> getActivitiesOfProcess(long userId, long processId, int pageIndex, int numberPerPage, ActivityInstanceCriterion pagingCriterion)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, UserNotFoundException, PageOutOfRangeException;

    /**
     * Get the number of processes
     * If the Session is invalid, it will throw InvalidSessionException
     * 
     * @return The number of processes with long type
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionReadException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    long getNumberOfProcesses() throws InvalidSessionException, ProcessDefinitionReadException;

    /**
     * Get process deployment information by process definition id
     * If the process by a given processDefinitionId is not found, it will throw processDefinitionNotFountExcetion
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @return Deployment information for process definition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ProcessDefinitionReadException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    ProcessDeploymentInfo getProcessDeploymentInfo(long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDefinitionReadException;

    /**
     * Update process deployment information by its id
     * If the process by a given processId is not found, it will throw processDefinitionNotFountExcetion
     * 
     * @param processId
     *            Identifier of the processDefinition
     * @param processDeploymentInfoUpdateDescriptor
     *            The description for update process deployment information
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ProcessDeploymentInfoUpdateException
     *             Error thrown if has exceptions while try to update a process definition
     */
    void updateProcessDeploymentInfo(long processId, ProcessDeploymentInfoUpdateDescriptor processDeploymentInfoUpdateDescriptor)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeploymentInfoUpdateException;

    /**
     * Get a list of all processes deployment information, the returned list is paginated
     * 
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param pagingCriterion
     *            The criterion to sort the result
     * @return The list of ProcessDefinition Objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws ProcessDefinitionReadException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    List<ProcessDeploymentInfo> getProcesses(int pageIndex, int numberPerPage, ProcessDefinitionCriterion pagingCriterion) throws InvalidSessionException,
            PageOutOfRangeException, ProcessDefinitionReadException;

    /**
     * Start the process on behalf of a given user name
     * 
     * @param userId
     *            the user id of the user starting the process
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @return The ProcessInstance Objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ProcessInstanceCreationException
     *             Error thrown if cannot create a process to the parameter.
     * @throws ProcessDefinitionReadException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ProcessDefinitionNotEnabledException
     *             Error thrown if the processDefinition cannot be enabled
     */
    ProcessInstance startProcess(long userId, long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDefinitionNotFoundException, ProcessInstanceCreationException, ProcessDefinitionReadException, ProcessDefinitionNotEnabledException;

    /**
     * Gets how many parameters the process definition contains.
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @return the number of parameters of a process definition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    int getNumberOfParameterInstances(long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Get a parameter instance by process definition UUID
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @param parameterName
     *            The parameter name for get ParameterInstance
     * @return the ParameterInstance of the process with processDefinitionUUID and name parameterName
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     */
    ParameterInstance getParameterInstance(long processDefinitionUUID, String parameterName) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ParameterNotFoundException;

    /**
     * Returns the parameters of a process definition or an empty map if the process does not contain any parameter.
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion to sort the result
     * @return The ordered list of parameter instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    List<ParameterInstance> getParameterInstances(long processDefinitionUUID, int pageIndex, int numberPerPage, ParameterSorting sort)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException;

    /**
     * Update an existing parameter of a process definition.
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @param parameterName
     *            the parameter name
     * @param parameterValue
     *            the new value of the parameter
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     * @throws InvalidParameterValueException
     *             Error thrown if the given parameter is invalid.
     */
    void updateParameterInstanceValue(long processDefinitionUUID, String parameterName, String parameterValue) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ParameterNotFoundException, InvalidParameterValueException;

    /**
     * Get a number of actors of the process definition
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @return The total number of actors of the process
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    int getNumberOfActors(long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Retrieves paginated actors associated with a process, the returned list is paginated
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion to sort the result
     * @return the list of Actor objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    List<ActorInstance> getActors(long processDefinitionId, int pageIndex, int numberPerPage, ActorSorting sort) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, PageOutOfRangeException;

    /**
     * Retrieves paginated actor members defined for an actor, the returned list is paginated
     * 
     * @param actorId
     *            Identifier of the actor
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param sort
     *            the criterion used to sort the retried actor members
     * @return the list of Actor members
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    List<ActorMember> getActorMembers(long actorId, int pageIndex, int numberPerPage, ActorMemberSorting sort) throws InvalidSessionException,
            ActorNotFoundException, PageOutOfRangeException;

    /**
     * get the total number of actor members defined for an actor
     * 
     * @param actorId
     *            Identifier of the actor
     * @return the total number of actors members defined for an actor
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfActorMembers(long actorId) throws InvalidSessionException;

    /**
     * Get an actor by its id
     * 
     * @param actorId
     *            Identifier of the actor
     * @return An actor instance
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     */
    ActorInstance getActor(long actorId) throws InvalidSessionException, ActorNotFoundException;

    /**
     * Update the actor by its id
     * 
     * @param actorId
     *            Identifier of the actor
     * @param updateDescriptor
     *            The description for update actor
     * @return actor instance
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws ActorUpdateException
     *             Error thrown if cannot update a actor to the parameter.
     */
    ActorInstance updateActor(long actorId, ActorUpdateDescriptor updateDescriptor) throws InvalidSessionException, ActorNotFoundException,
            ActorUpdateException;

    /**
     * Add a user to the actor
     * 
     * @param actorId
     *            Identifier of the actor
     * @param userId
     *            Identifier of the user
     * @return actor member
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the parameter.
     * @throws ActorMemberCreationException
     *             Error thrown if has exceptions during the process to add actor member.
     */
    ActorMember addUserToActor(long actorId, long userId) throws InvalidSessionException, ActorNotFoundException, UserNotFoundException,
            ActorMemberCreationException;

    /**
     * Add a group to the actor
     * 
     * @param actorId
     *            Identifier of the actor
     * @param groupId
     *            Identifier of the group
     * @return An actor member object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws GroupNotFoundException
     *             Error thrown if no group have an id corresponding to the parameter.
     * @throws ActorMemberCreationException
     *             Error thrown if has exceptions during the process to add actor member.
     */
    ActorMember addGroupToActor(long actorId, long groupId) throws InvalidSessionException, ActorNotFoundException, GroupNotFoundException,
            ActorMemberCreationException;

    /**
     * Add a role to the actor
     * 
     * @param actorId
     *            Identifier of the actor
     * @param roleId
     *            Identifier of the role
     * @return An actor member object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws RoleNotFoundException
     *             Error thrown if no role have an id corresponding to the parameter.
     * @throws ActorMemberCreationException
     *             Error thrown if has exceptions during the process to add actor member.
     */
    ActorMember addRoleToActor(long actorId, long roleId) throws InvalidSessionException, ActorNotFoundException, RoleNotFoundException,
            ActorMemberCreationException;

    /**
     * Add a role and a group to the actor
     * 
     * @param actorId
     *            Identifier of the actor
     * @param roleId
     *            Identifier of the role
     * @param groupId
     *            Identifier of the group
     * @return An actor member object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws RoleNotFoundException
     *             Error thrown if no role have an id corresponding to the parameter.
     * @throws GroupNotFoundException
     *             Error thrown if no group have an id corresponding to the parameter.
     * @throws ActorMemberCreationException
     *             Error thrown if has exceptions during the process to add actor member.
     */
    ActorMember addRoleAndGroupToActor(long actorId, long roleId, long groupId) throws InvalidSessionException, ActorNotFoundException, RoleNotFoundException,
            GroupNotFoundException, ActorMemberCreationException;

    /**
     * Delete the actor member by its id
     * 
     * @param actorMemberId
     *            Identifier of the actor member
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorMemberNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws ActorMemberDeletionException
     *             Error thrown if has exceptions during the process to remove actor member.
     */
    void removeActorMember(long actorMemberId) throws InvalidSessionException, ActorMemberNotFoundException, ActorMemberDeletionException;

    /**
     * Import actor mapping by a String xmlContent
     * 
     * @param processDefinitionId
     *            Identifier of the process definition
     * @param xmlContent
     *            the content with XML format.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorMappingImportException
     *             Error thrown if has exceptions during the process to import actor member.
     */
    void importActorMapping(long processDefinitionId, String xmlContent) throws InvalidSessionException, ActorMappingImportException;

    /**
     * Export actor mapping, return its xmlContent
     * 
     * @param processDefinitionId
     *            Identifier of the process definition
     * @return String xmlContent the content with XML format.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorMappingExportException
     *             Error thrown if has exceptions during the process to export actor member.
     */
    String exportActorMapping(long processDefinitionId) throws InvalidSessionException, ActorMappingExportException;

    /**
     * Get all process instances, the returned list is paginated
     * 
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of documents per page. Maximum number of documents returned.
     * @param pagingCriterion
     *            the criterion for sort result.
     * @return The list of process instance
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws ProcessInstanceReadException
     *             Error thrown if has exceptions during the process to read process instance.
     */
    List<ProcessInstance> getProcessInstances(int pageIndex, int numberPerPage, ProcessInstanceCriterion pagingCriterion) throws PageOutOfRangeException,
            InvalidSessionException, ProcessInstanceReadException;

    /**
     * Get all archived process instances, the returned list is paginated
     * 
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param pagingCriterion
     *            the criterion for sort result.
     * @return The list of archived process instances
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceReadException
     *             Error thrown if has exceptions during the process to read process instance.
     */
    List<ArchivedProcessInstance> getArchivedProcessInstances(int pageIndex, int numberPerPage, ProcessInstanceCriterion pagingCriterion)
            throws PageOutOfRangeException, InvalidSessionException, ProcessInstanceReadException;

    /**
     * Get the number of running process instances
     * 
     * @return The process instance number
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfProcessInstances() throws InvalidSessionException;

    /**
     * Get the number of distinct archived process instances. "Archived" means in the definitive archive.
     * Only state Process Instances are retrieved.
     * 
     * @return The number of archived process instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceReadException
     *             Error thrown if has exceptions during the process to read process instance.
     */
    long getNumberOfArchivedProcessInstances() throws InvalidSessionException, ProcessInstanceReadException;

    /**
     * Retrieve a list of open activities for a given process instance, the returned list is paginated
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param pagingCriterion
     *            paging criterion to sort the results
     * @return The list of activity instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no result have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws ActivityInstanceReadException
     *             Error thrown if has exceptions during the process to read activity instance.
     */
    List<ActivityInstance> getOpenedActivityInstances(long processInstanceId, int pageIndex, int numberPerPage, ActivityInstanceCriterion pagingCriterion)
            throws InvalidSessionException, ProcessInstanceNotFoundException, PageOutOfRangeException, ActivityInstanceReadException;

    /**
     * Get total number of open activity instances by its id
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @return Number of open activity instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             Error thrown if has exceptions during the process to read activity instance.
     */
    int getNumberOfOpenedActivityInstances(long processInstanceId) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Get a list of archived activity instances, the returned list is paginated
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param pagingCriterion
     *            aging criterion to sort the results
     * @return this list of activities from the first definitive archive
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws ActivityInstanceReadException
     *             Error thrown if has exceptions during the process to read archived activity instance.
     */
    List<ArchivedActivityInstance> getArchivedActivityInstances(long processInstanceId, int pageIndex, int numberPerPage,
            ActivityInstanceCriterion pagingCriterion) throws InvalidSessionException, PageOutOfRangeException, ActivityInstanceReadException;

    /**
     * Get total number of archived activity instances
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @return Number of archived activity instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             Error thrown if has exceptions during the process to read archived activity instance.
     */
    int getNumberOfArchivedActivityInstances(long processInstanceId) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Add a new category
     * 
     * @param name
     *            The name of category
     * @param description
     *            The description of category
     * @return An object of category
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryAlreadyExistException
     *             Error thrown if category is already exist
     * @throws CategoryCreationException
     *             Error thrown if has exceptions during the process to create a category.
     */
    Category createCategory(String name, String description) throws InvalidSessionException, CategoryAlreadyExistException, CategoryCreationException;

    /**
     * Get the total number of categories
     * 
     * @return The total number of Categories
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfCategories() throws InvalidSessionException;

    /**
     * Retrieves a paginated list of categories, The returned list is paginated
     * 
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of categories per page. Maximum number of categories returned.
     * @param pagingCriterion
     *            the criterion used to sort the retried categories
     * @return The list of category objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    List<Category> getCategories(int pageIndex, int numberPerPage, CategoryCriterion pagingCriterion) throws InvalidSessionException, PageOutOfRangeException;

    /**
     * Get category by its id
     * 
     * @param categoryId
     *            Identifier of the category
     * @return An category object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     */
    Category getCategory(long categoryId) throws InvalidSessionException, CategoryNotFoundException;

    /**
     * Add a process definition to a category
     * 
     * @param categoryId
     *            Identifier of the category
     * @param processDefinitionId
     *            Identifier of the process definition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no process definition have an id corresponding to the parameter.
     * @throws CategoryMappingException
     *             Error thrown if has exceptions during the process to add a process definition to category
     */
    void addProcessDefinitionToCategory(long categoryId, long processDefinitionId) throws InvalidSessionException, CategoryNotFoundException,
            ProcessDefinitionNotFoundException, CategoryMappingException;

    /**
     * Add process definitions to a category
     * 
     * @param categoryId
     *            Identifier of the category
     * @param processDefinitionIds
     *            Identifier of the process definition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no process definition have an id corresponding to the parameter.
     * @throws CategoryMappingException
     *             Error thrown if has exceptions during the process to add a process definition to category
     */
    void addProcessDefinitionsToCategory(long categoryId, List<Long> processDefinitionIds) throws InvalidSessionException, CategoryNotFoundException,
            ProcessDefinitionNotFoundException, CategoryMappingException;

    /**
     * Get number of categories of the specific process definition
     * 
     * @param processDefinitionId
     *            Identifier of the process definition
     * @return Number of categories
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no process definition have an id corresponding to the parameter.
     * @throws CategoryGettingException
     *             error thrown if has exceptions during the process to get a category
     */
    long getNumberOfCategories(long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException, CategoryGettingException;

    /**
     * get the total number of processes in a given category
     * 
     * @param categoryId
     *            Identifier of the category
     * @return The total number of processes meet the criteria
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     */
    long getNumberOfProcessesInCategory(long categoryId) throws InvalidSessionException, CategoryNotFoundException;

    /**
     * Get processDeplymentInfos of a category
     * 
     * @param categoryId
     *            Identifier of the category
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion used to sort the retried categories
     * @return The list of ProcessDeploymentInfo
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    List<ProcessDeploymentInfo> getProcessDeploymentInfosOfCategory(long categoryId, int pageIndex, int numberPerPage, ProcessDefinitionCriterion sort)
            throws InvalidSessionException, CategoryNotFoundException, PageOutOfRangeException;

    /**
     * Get categories from process definition
     * 
     * @param processDefinitionId
     *            Identifier of the process definition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion used to sort the retried categories
     * @return The matching list of category
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no process definition have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    List<Category> getCategoriesOfProcessDefinition(long processDefinitionId, int pageIndex, int numberPerPage, CategoryCriterion sort)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException;

    /**
     * Update a category by its id
     * 
     * @param categoryId
     *            Identifier of the category
     * @param category
     *            An category object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     * @throws CategoryUpdateException
     *             Error thrown if has exception during the process to update category
     */
    void updateCategory(long categoryId, Category category) throws InvalidSessionException, CategoryNotFoundException, CategoryUpdateException;

    /**
     * Delete a category by its id
     * 
     * @param categoryId
     *            Identifier of the category
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     * @throws CategoryDeletionException
     *             Error thrown if has exception during the process to delete category
     */
    void deleteCategory(long categoryId) throws InvalidSessionException, CategoryNotFoundException, CategoryDeletionException;

    /**
     * Delete categorized process definitions
     * 
     * @param categoryId
     *            Identifier of the category
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryNotFoundException
     *             Error thrown if no category have an id corresponding to the parameter.
     * @throws CategoryMappingException
     *             Error thrown if has exception during the process to mapping category
     */
    void removeProcessDefinitionsOfCategory(long categoryId) throws InvalidSessionException, CategoryNotFoundException, CategoryMappingException;

    /**
     * Get the number of uncategorized process definitions
     * 
     * @return Number of uncategorized process definitions
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfUncategorizedProcessDefinitions() throws InvalidSessionException;

    /**
     * Get uncategorized process deployment informations, the returned list is paginated
     * 
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param pagingCriterion
     *            The criterion used to sort the retried process deployment information
     * @return The list of process deployment information
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    List<ProcessDeploymentInfo> getUncategorizedProcessDeploymentInfos(int pageIndex, int numberPerPage, ProcessDefinitionCriterion pagingCriterion)
            throws PageOutOfRangeException, InvalidSessionException;

    /**
     * Remove the record of processDefinition in all categories
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    void removeProcessDefinitionFromCategory(long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Get activity data definitions by process definition id, the returned list is paginated
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param activityName
     *            The name of the activity
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @return The list of data definitions
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws NoSuchActivityDefinitionException
     *             Error thrown if no ActivityDefinition have an id corresponding to the parameter.
     * @throws DataNotFoundException
     *             Error thrown if no data have an id corresponding to the parameter.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    List<DataDefinition> getActivityDataDefinitions(long processDefinitionId, String activityName, int pageIndex, int numberPerPage)
            throws InvalidSessionException, NoSuchActivityDefinitionException, DataNotFoundException, ProcessDefinitionNotFoundException;

    /**
     * Get number of activity data definitions by process definition id
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param activityName
     *            The name of the activity
     * @return Number of activity data definitions
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    int getNumberOfActivityDataDefinitions(long processDefinitionId, String activityName) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Get a list of all process data definitions by process definition id, the returned list is paginated
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @return The list of data definitions
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws DataNotFoundException
     *             Error thrown if no data have an id corresponding to the parameter.
     */
    List<DataDefinition> getProcessDataDefinitions(long processDefinitionId, int pageIndex, int numberPerPage) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, DataNotFoundException;

    /**
     * Get the number of process data definitions
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @return Number of process data definitions
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    int getNumberOfProcessDataDefinitions(long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Get all process resources by process definition id
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param filenamesPattern
     *            The pattern to retrieve the resources. The format must be relative to the root of the business archive, without '^' character, nor '/'. Must
     *            also use forward slashes.
     * @return The map containing the pairs (name, content) of the matching files.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessResourceException
     *             Error thrown if has exceptions during the process to get process resources
     */
    Map<String, byte[]> getProcessResources(long processDefinitionId, String filenamesPattern) throws InvalidSessionException, ProcessResourceException;

    /**
     * Get the most recent version of a process definition
     * 
     * @param processName
     *            The process definition name
     * @return A process definition id
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     */
    long getLatestProcessDefinitionId(String processName) throws ProcessDefinitionNotFoundException, InvalidSessionException;

    /**
     * Get the children instances (subprocess or call activity) of a process instance. The returned list is paginated.
     * 
     * @param processInstanceId
     *            Identifier of the processDefinition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param maxResults
     *            Number of result per page. Maximum number of result returned.
     * @param criterion
     *            The criterion used to sort the result
     * @return The list of children instance id
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no processInstance have an id corresponding to the parameter.
     */
    List<Long> getChildrenInstanceIdsOfProcessInstance(long processInstanceId, int pageIndex, int maxResults, ProcessInstanceCriterion criterion)
            throws ProcessInstanceNotFoundException, InvalidSessionException, PageOutOfRangeException;

    /**
     * Whether or not a user is involved in an instance of process (has tasks pending?)
     * 
     * @param userId
     *            Identifier of the user
     * @param processInstanceId
     *            Identifier of the processDefinition
     * @return An boolean type to get if a process instance is involved with given user
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no processInstance have an id corresponding to the parameter.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the parameter.
     */
    boolean isInvolvedInProcessInstance(long userId, long processInstanceId) throws ProcessInstanceNotFoundException, InvalidSessionException,
            UserNotFoundException;

    /**
     * Get process instance id from its activity instance id
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return process instance id
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no processInstance have an id corresponding to the parameter.
     */
    long getProcessInstanceIdFromActivityInstanceId(long activityInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException;

    /**
     * Get process definition id from its activity instance id
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return process definition id
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no ProcessDefinition have an id corresponding to the parameter.
     */
    long getProcessDefinitionIdFromActivityInstanceId(long activityInstanceId) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Get process definition id from its process instance id
     * 
     * @param processInstanceId
     *            Identifier of the activity instance
     * @return process definition id
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no ProcessDefinition have an id corresponding to the parameter.
     */
    long getProcessDefinitionIdFromProcessInstanceId(long processInstanceId) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Get all supported states form flowNodeType
     * 
     * @param nodeType
     *            The FlowNodeType
     * @return The set of states
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    Set<String> getSupportedStates(FlowNodeType nodeType) throws InvalidSessionException;

    /**
     * Get the processDefinitionId by name and version
     * 
     * @param name
     *            the name of the process definition
     * @param version
     *            the version of the process definition
     * @return process definition id
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no ProcessDefinition have an id corresponding to the parameter.
     */
    long getProcessDefinitionId(String name, String version) throws ProcessDefinitionNotFoundException, InvalidSessionException;

    /**
     * Get startable processes from a set of actor id
     * 
     * @param actorIds
     *            Identifier of the actor
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @return The list of process definition deployment information
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     */
    public List<ProcessDeploymentInfo> getStartableProcessesForActors(Set<Long> actorIds, int pageIndex, int numberPerPage) throws InvalidSessionException,
            ActorNotFoundException;

    /**
     * Get boolean whether allow to start a process
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param actorIds
     *            Identifier of the actor
     * @return An boolean type to know the process if allowed to start
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     */
    public boolean isAllowedToStartProcess(long processDefinitionId, Set<Long> actorIds) throws InvalidSessionException, ActorNotFoundException;

    /**
     * get actor initiator
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @return An ActorInstance object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     */
    public ActorInstance getActorInitiator(long processDefinitionId) throws InvalidSessionException, ActorNotFoundException;

    /**
     * Start a process by process definition id
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param operations
     *            The operations to start process
     * @return a ProcessInstance object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ProcessDefinitionNotEnabledException
     *             Error thrown if process definition is disabled.
     * @throws ProcessInstanceCreationException
     *             Error thrown if has exception during the process to create a process instance.
     * @throws OperationExecutionException
     *             Error thrown if has exception during the process to execute a operating
     */
    public ProcessInstance startProcess(long processDefinitionId, Map<Operation, Map<String, Serializable>> operations) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ProcessDefinitionNotEnabledException, ProcessInstanceCreationException, OperationExecutionException;

    /**
     * Get number of all activity data instances by id
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return Number of all activity data instances
     * @throws ActivityInstanceNotFoundException
     *             Error thrown if no activity instance have an id corresponding to the parameter.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfActivityDataInstances(long activityInstanceId) throws ActivityInstanceNotFoundException, InvalidSessionException;

    /**
     * Get number of all process data instances by id
     * 
     * @param processInstanceId
     *            Identifier of the activity instance
     * @return Number of process data instances
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no ProcessInstance have an id corresponding to the parameter.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfProcessDataInstances(long processInstanceId) throws ProcessInstanceNotFoundException, InvalidSessionException;

    /**
     * Create a privilege with scope and level.
     * 
     * @param scope
     *            The scope of the privilege
     * @param level
     *            The level of the privilege
     * @return A privilege object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PrivilegeInsertException
     *             Error thrown if has exception during the process to create privilege
     * @throws PrivilegeNotFoundException
     *             Error thrown if no privilege have an id corresponding to the parameter.
     */
    @Deprecated
    Privilege createAddPrivilege(String scope, String level) throws InvalidSessionException, PrivilegeInsertException, PrivilegeNotFoundException;

    @Deprecated
    /**
     *  use {@link #grantAllDefaultToActors(long)} instead
     *             replaced by <code>grantAllDefaultToActors(long)</code>.
     */
    ActorPrivilege createAddActorPrivilege(long actorId, long privilegeId, int type) throws InvalidSessionException, ActorPrivilegeInsertException,
            ActorPrivilegeNotFoundException;

    /**
     * List all open process instances
     * 
     * @param searchOptions
     *            The criterion used to search process instance
     * @return A processInstance object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceReadException
     *             Error thrown if no process instance have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ProcessInstance> searchOpenProcessInstances(SearchOptions searchOptions) throws InvalidSessionException, ProcessInstanceReadException,
            PageOutOfRangeException;

    /**
     * List all open process instances supervised by a user
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search process instance
     * @return A processInstance object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if the user is not found
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, PageOutOfRangeException;

    /**
     * List all process definitions which is started recently
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search process deployment information
     * @return ProcessDeploymentInfo objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionReadException
     *             Error thrown if has exceptions during the process to read process deployment information.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws UserNotFoundException
     *             Error thrown if the user is not found
     */
    SearchResult<ProcessDeploymentInfo> searchRecentlyStartedProcessDefinitions(long userId, SearchOptions searchOptions) throws InvalidSessionException,
            ProcessDefinitionReadException, PageOutOfRangeException, UserNotFoundException;

    /**
     * List all process deployment information by user id
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search process deployment information
     * @return ProcessDeploymentInfo objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionReadException
     *             Error thrown if has exceptions during the process to read process deployment information.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDefinitions(long userId, SearchOptions searchOptions) throws InvalidSessionException,
            ProcessDefinitionReadException, PageOutOfRangeException;

    /**
     * List all privileges by search options
     * 
     * @param searchOptions
     *            The criterion used to search privileges
     * @return Privilege objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<Privilege> searchPrivileges(SearchOptions searchOptions) throws InvalidSessionException, PageOutOfRangeException;

    /**
     * List all actor privileges by search options
     * 
     * @param searchOptions
     *            The criterion used to search privileges
     * @return ActorPrivilege objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ActorPrivilege> searchActorPrivileges(SearchOptions searchOptions) throws InvalidSessionException, ActorNotFoundException,
            PageOutOfRangeException;

    /**
     * Get actor privilege by id
     * 
     * @param actorPrivilegeId
     *            Identifier of the actorPrivilege
     * @return a actorPrivilege object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorPrivilegeNotFoundException
     *             Error thrown if no actorPrivilege have an id corresponding to the parameter.
     */
    ActorPrivilege getActorPrivilege(long actorPrivilegeId) throws InvalidSessionException, ActorPrivilegeNotFoundException;

    /**
     * Update actor privilege
     * 
     * @param map
     *            A map contains actor privilege property
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorPrivilegeNotFoundException
     *             Error thrown if no actorPrivilege have an id corresponding to the parameter.
     * @throws ActorPrivilegeUpdateException
     *             Error thrown if has exception during the process to update actor privilege
     */
    void updateActorPrivilege(Map<Long, String> map) throws InvalidSessionException, ActorPrivilegeNotFoundException, ActorPrivilegeUpdateException;

    /**
     * Grant all default privileges to actors
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorNotFoundException
     *             Error thrown if no actor have an id corresponding to the parameter.
     * @throws PrivilegeNotFoundException
     *             Error thrown if no privilege have an id corresponding to the parameter.
     * @throws ActorPrivilegeInsertException
     *             Error thrown if has exception during the process to grant all default to actor
     */
    void grantAllDefaultToActors(long processDefinitionId) throws InvalidSessionException, ActorNotFoundException, PrivilegeNotFoundException,
            ActorPrivilegeInsertException;

    /**
     * Get value of actor privilege by actor id and privilege id
     * 
     * @param actorId
     *            Identifier of the actor
     * @param privilegeId
     *            Identifier of the privilege
     * @return level of read or write
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorPrivilegeNotFoundException
     *             Error thrown if no actorPrivilege have an id corresponding to the parameter.
     */
    LevelRight getActorPrivilegeValue(long actorId, long privilegeId) throws InvalidSessionException, ActorPrivilegeNotFoundException;

    /**
     * Confirm the right of actor privilege
     * 
     * @param ActorId
     *            Identifier of the actor
     * @param PrivilegeId
     *            Identifier of the privilege
     * @param value
     *            what detail right for the operation ,like read-only, read-write and so on
     * @return return a flag if the actor with given actorId has right to some work that's already defined by given privilegeId
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorPrivilegeNotFoundException
     *             Error thrown if no actorPrivilege have an id corresponding to the parameter.
     * @throws PrivilegeNotFoundException
     *             confirm the privilege with given privilegeId was defined in privilege table
     * @throws ActorNotFoundException
     *             before check this operation's allowed, it has to check the actor with given actorId already existed or not
     * @Deprecated use {@link #getActorPrivilegeValue(long, long)} instead
     *             replaced by <code>getActorPrivilegeValue(long, long)</code>.
     */
    boolean isAllowed(long ActorId, long PrivilegeId, LevelRight value) throws InvalidSessionException, ActorPrivilegeNotFoundException,
            PrivilegeNotFoundException, ActorNotFoundException;

    /**
     * Delete all privileges with actor by a list of privilege id
     * 
     * @param actorPrivilegeIds
     *            Identifier of the actorPrivilege
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorPrivilegeRemoveException
     *             Error thrown if has exception during the process to remove the actor privilege
     */
    void removeActorPrivileges(List<Long> actorPrivilegeIds) throws InvalidSessionException, ActorPrivilegeRemoveException;

    /**
     * Remove privileges that we want to delete,but this method is just for API level test.so no replacement method.
     * 
     * @param privilegeIds
     *            Identifier of the privilege
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PrivilegeRemoveException
     *             Error thrown if has exception during the process to remove the privilege
     */
    @Deprecated
    void removePrivileges(List<Long> privilegeIds) throws InvalidSessionException, PrivilegeRemoveException;

    /**
     * Get all privileges by a list of privilege id
     * 
     * @param privilegeIds
     *            Identifier of the privilege
     * @return The list of privileges
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws PrivilegeNotFoundException
     *             Error thrown if no privilege have an id corresponding to the parameter.
     */
    List<Privilege> getPrivileges(List<Long> privilegeIds) throws InvalidSessionException, PrivilegeNotFoundException;

    /**
     * List all open process instances that the user can access
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search ProcessInstance
     * @return The instance of process
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, PageOutOfRangeException;

    /**
     * List all archived process instances
     * 
     * @param searchOptions
     *            The criterion used to search ProcessInstance
     * @return List all archived process instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceReadException
     *             Error thrown if has exceptions during the process to read process instance
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances(SearchOptions searchOptions) throws InvalidSessionException,
            ProcessInstanceReadException, PageOutOfRangeException;

    /**
     * List all archived process instances supervised by a user
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search ArchivedProcessInstance
     * @return All archived process instances supervised by a user
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if user is not found.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, PageOutOfRangeException;

    /**
     * List all the archived process instances that the user can access
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search ArchivedProcessInstance
     * @return All the archived process instances that the user can access
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if user is not found.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, PageOutOfRangeException;

    /**
     * List all archived humanTask instances managed by a user
     * 
     * @param managerUserId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search ArchivedHumanTaskInstance
     * @return All archived humanTask instances managed by a user
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if user is not found.
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException;

    /**
     * List all comments related to the specified Process Instance.
     * 
     * @param searchOptions
     *            The criterion used to search Comment
     * @return All comments related to the specified Process Instance.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if ProcessInstance is not found.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<Comment> searchComments(SearchOptions searchOptions) throws InvalidSessionException, ProcessInstanceNotFoundException, PageOutOfRangeException;

    /**
     * Add a comment on process instance
     * 
     * @param processInstanceId
     *            Identifier of the processInstance
     * @param comment
     *            The content of comment
     * @return An Comment object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommentAddException
     *             Error throws if has exception during the process to add comment.
     */
    Comment addComment(long processInstanceId, String comment) throws InvalidSessionException, CommentAddException;

    /**
     * Get all comments by its process instance id
     * 
     * @param processInstanceId
     *            Identifier of the processInstance
     * @return The list of comment
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommentAddException
     *             Error throws if has exception during the process to add comment.
     */
    List<Comment> getComments(long processInstanceId) throws InvalidSessionException, CommentAddException;

    /**
     * List all comments managed by a user
     * 
     * @param managerUserId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search Comment
     * @return All comments managed by a user
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CommentReadException
     *             Error thrown if has exceptions to read comment
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<Comment> searchCommentsManagedBy(long managerUserId, SearchOptions searchOptions) throws InvalidSessionException, CommentReadException,
            PageOutOfRangeException;

    /**
     * List all the comments on process instants that the user can access
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search Comment
     * @return All the comments on process instants that the user can access
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if user is not found.
     * @throws CommentReadException
     *             Error thrown if has exceptions to read comment
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<Comment> searchCommentsInvolvingUser(long userId, SearchOptions searchOptions) throws InvalidSessionException, UserNotFoundException,
            CommentReadException, PageOutOfRangeException;

    /**
     * List all process definitions
     * 
     * @param searchOptions
     *            The criterion used to search ProcessDeploymentInfo
     * @return Information of process deployment
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionReadException
     *             Error thrown if has exceptions to read process definitions
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDefinitions(SearchOptions searchOptions) throws InvalidSessionException, ProcessDefinitionReadException,
            PageOutOfRangeException;

    /**
     * List all humanTask instances
     * 
     * @param searchOptions
     *            The criterion used to search HumanTaskInstance
     * @return The instance of humanTask
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             Error thrown if has exceptions during the process to search human task
     */
    SearchResult<HumanTaskInstance> searchHumanTaskInstances(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Add categories to process definition
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param categoryIds
     *            Identifier of the category
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws CategoryMappingException
     *             Error thrown if has exception to mapping category
     * @throws CategoryNotFoundException
     *             Error thrown if category is not found
     */
    void addCategoriesToProcess(long processDefinitionId, List<Long> categoryIds) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            CategoryMappingException, CategoryNotFoundException;

    /**
     * Delete all categories to process definition
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param categoryIds
     *            Identifier of the category
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws CategoryMappingException
     *             Error thrown if has exception to during the process to remove category from process
     */
    void removeCategoriesToProcess(long processDefinitionId, List<Long> categoryIds) throws InvalidSessionException, CategoryMappingException;

    /**
     * Search all uncategorized process definitions
     * 
     * @param searchOptions
     *            The criterion used to search ProcessDeploymentInfo
     * @return ProcessDeploymentInfo objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionReadException
     *             Error thrown if has exception to during the process to search ProcessDeploymentInfo
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDefinitions(SearchOptions searchOptions) throws InvalidSessionException,
            ProcessDefinitionReadException, PageOutOfRangeException;

    /**
     * Search uncategorized process definitions with supervisor
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search ProcessDeploymentInfo
     * @return ProcessDeploymentInfo objects
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionReadException
     *             Error thrown if has exception to during the process to search ProcessDeploymentInfo
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDefinitionsSupervisedBy(long userId, SearchOptions searchOptions)
            throws InvalidSessionException, ProcessDefinitionReadException, PageOutOfRangeException;

    /**
     * clear: remove the old .impl file; put the new .impl file in the connector directory
     * reload the cache, connectorId and connectorVersion are used here.
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param connectorId
     *            Identifier of the connector
     * @param connectorVersion
     *            The version of connector
     * @param connectorImplementationArchive
     *            byte[] is a zip file exported from studio
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ConnectorException
     *             Error thrown if has exceptions during the process to set connector implementation
     */
    public void setConnectorImplementation(long processDefinitionId, String connectorId, String connectorVersion, byte[] connectorImplementationArchive)
            throws InvalidSessionException, ConnectorException;

    /**
     * Get process definitions from a list contained id.
     * 
     * @param processDefinitionIds
     *            Identifier of the processDefinition
     * @return A map of information with process deployment
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ProcessDefinitionReadException
     *             Error thrown if has exception to during the process to search ProcessDeploymentInfo
     */
    Map<Long, ProcessDeploymentInfo> getProcessDefinitionsFromIds(List<Long> processDefinitionIds) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ProcessDefinitionReadException;

    /**
     * Search uncategorized process definitions which user can start
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search ProcessDeploymentInfo
     * @return information of process deployment
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if user is not found
     * @throws ProcessDefinitionReadException
     *             Error thrown if has exception to during the process to search ProcessDeploymentInfo
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     */
    public SearchResult<ProcessDeploymentInfo> searchUncategorizedProcessDefinitionsUserCanStart(long userId, SearchOptions searchOptions)
            throws InvalidSessionException, UserNotFoundException, ProcessDefinitionReadException, PageOutOfRangeException;

    /**
     * Returns the paginated list of connector implementation descriptors according to the sort criterion.
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param pagingCriterion
     *            Paging Criterion for sort result
     * @return The list of ConnectorImplementationDescriptor
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @throws ConnectorException
     *             Error thrown if has exceptions during the process to get connector implementations
     */
    List<ConnectorImplementationDescriptor> getConnectorImplementations(long processDefinitionId, int pageIndex, int numberPerPage,
            ConnectorCriterion pagingCriterion) throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException, ConnectorException;

    /**
     * Get the number of profile members, return A map of <profileId, NumberOfMemberForThatProfile>
     * 
     * @param profileIds
     *            Identifier of the profile
     * @return A map of <profileId, NumberOfMemberForThatProfile>
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             Error thrown if has exceptions during the process get number of profile members
     */
    Map<Long, Long> getNumberOfProfileMembers(List<Long> profileIds) throws InvalidSessionException, SearchException;

    /**
     * Search the current connector implementation from its ID (connectorId, connectorVersion) in a specified process
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param connectorId
     *            Identifier of the connector
     * @param connectorVersion
     *            version of connector definition
     * @return ConnectorImplementationDescriptor
     *         The description for connector implementation
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ConnectorNotFoundException
     *             Error thrown if no connector have an id corresponding to the parameter.
     */
    ConnectorImplementationDescriptor getConnectorImplementation(long processDefinitionId, String connectorId, String connectorVersion)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ConnectorNotFoundException;

    /**
     * Get process deployment information from a list of processInstance id
     * 
     * @param processInstantsIds
     *            Identifier of the processInstance
     * @return A map of <processInstantsIds,ProcessDeploymentInfos>
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             Error thrown if has exceptions during the process to get process deployment information
     */
    Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfoFromProcessInstanceIds(List<Long> processInstantsIds) throws InvalidSessionException,
            SearchException;

    /**
     * Search archived activity instances
     * 
     * @param searchOptions
     *            The criterion used to archived activity instance
     * @return A {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             Error thrown if has exceptions during the process to search archived activity instance
     */
    SearchResult<ArchivedActivityInstance> searchArchivedActivities(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Search activity instances
     * 
     * @param searchOptions
     *            The criterion used to activity instance
     * @return A {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             Error thrown if has exceptions during the process to search activity instance
     */
    SearchResult<ActivityInstance> searchActivities(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Search flow node instances (activities, gateways and events)
     * 
     * @param searchOptions
     *            The criterion used during the search
     * @return A {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             If API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             if an exception occurs while performing the search
     * @since 6.0
     */
    SearchResult<FlowNodeInstance> searchFlowNodeInstances(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Search archived flow node instances (activities, gateways and events)
     * 
     * @param searchOptions
     *            The criterion used during the search
     * @return A {@link SearchResult} containing the found archived flown node instances.
     * @throws InvalidSessionException
     *             If API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             if an exception occurs while performing the search
     * @see {@link ArchivedFlowNodeInstance}
     * @since 6.0
     */
    SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstances(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Search archived flow element instances (transitions)
     * 
     * @param searchOptions
     *            The criterion to use for the search
     * @return A {@link SearchResult} containing the found archived flown element instances.
     * @throws InvalidSessionException
     *             If API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             if an exception occurs while performing the search
     * @see {@link ArchivedFlowElementInstance}
     * @since 6.0
     */
    SearchResult<ArchivedFlowElementInstance> searchArchivedFlowElementInstances(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Import the actor mapping by a processDefinition id and an array byte of parametersXML
     * 
     * @param pDefinitionId
     *            Identifier of the processDefinition
     * @param actorMappingXML
     *            The mapping of the actor with XML format.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActorMappingImportException
     *             Error thrown if has exceptions during the process to import actor mapping
     */
    void importActorMapping(long pDefinitionId, byte[] actorMappingXML) throws InvalidSessionException, ActorMappingImportException;

    /**
     * Import the parameters by a processDefinition id and an array byte of parametersXML
     * 
     * @param pDefinitionId
     *            Identifier of the processDefinition
     * @param parametersXML
     *            The parameter with XML format.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws InvalidParameterValueException
     *             Error thrown if is value in the parameter is invalid
     */
    void importParameters(long pDefinitionId, byte[] parametersXML) throws InvalidSessionException, InvalidParameterValueException;

    /**
     * Export processes of bar under home by a processDefinition id
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @return An array of byte
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws BonitaRuntimeException
     *             Error thrown if has exceptions during the process to export bar process content under home
     * @throws IOException
     *             Error thrown if has exceptions from IO class
     */
    byte[] exportBarProcessContentUnderHome(long processDefinitionId) throws BonitaRuntimeException, IOException, InvalidSessionException;

    /**
     * Search archived comments
     * 
     * @param searchOptions
     *            The criterion used to archived comment
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @return An archived comment object
     * @throws SearchException
     *             Error thrown if has exceptions during the process to search archived comment
     */
    SearchResult<ArchivedComment> searchArchivedComments(SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Retrieve actorInstances corresponding the given a list of actor ids, they're stored in a map as a result
     * 
     * @param actorIds
     *            the list of actor ids
     * @return a map of mapping with key actorId and value actorInstance
     * @throws InvalidSessionException
     * @throws BonitaReadException
     */
    Map<Long, ActorInstance> getActorsFromActorIds(List<Long> actorIds) throws InvalidSessionException, BonitaReadException;

    /**
     * Retrieve actorPrivileges corresponding the given a list of actorPrivilege ids, they're stored in a map as a result
     * 
     * @param actorPrivilegeIds
     *            the list of actor privilege ids
     * @return a map of mapping with key actorPrivilegeId and value actorPrivilege
     * @throws InvalidSessionException
     * @throws BonitaReadException
     */
    Map<Long, ActorPrivilege> getActorPrivilegesFromActorPrivilegeIds(List<Long> actorPrivilegeIds) throws InvalidSessionException, BonitaReadException;

    /**
     * Get process deployment information from a list of archived processInstance ids
     * 
     * @param archivedProcessInstantsIds
     *            Identifier of the archived process instance
     * @return A map of <archivedProcessInstantsIds,ProcessDeploymentInfos>
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             Error thrown if has exceptions during the process to get process deployment information
     */
    Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfoFromArchivedProcessInstanceIds(List<Long> archivedProcessInstantsIds)
            throws InvalidSessionException, SearchException;

    /**
     * Search for the hidden tasks for the specified user. Only searches for pending tasks for the current user: if a hidden task has been assigned,
     * executed, ... it will not be retrieved.
     * 
     * @param userId
     *            the ID of the user for whom to retrieve the hidden tasks
     * @param searchOptions
     *            the search options parameters
     * @return the list of hidden tasks for the specified user
     * @throws InvalidSessionException
     *             if the current session is invalid
     * @throws SearchException
     *             in case a search problem occurs
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingHiddenTasks(long userId, SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * Search, for a specific user, all tasks pending for that user, or already assigned to that user. (=Available)
     * Hidden tasks are not retrieved.
     * 
     * @param userId
     *            the user for whom to retrieve the tasks
     * @param searchOptions
     *            the search options parameters
     * @return the list of tasks matching the provided criteria
     * @throws InvalidSessionException
     *             if the current session is invalid
     * @throws SearchException
     *             in case a search problem occurs
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchMyAvailableHumanTasks(long userId, SearchOptions searchOptions) throws InvalidSessionException, SearchException;

    /**
     * @param userId
     * @param searchOptions
     * @return
     * @throws InvalidSessionException
     * @throws ProcessDefinitionReadException
     * @throws PageOutOfRangeException
     * @throws SearchException
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDefinitionsSupervisedBy(long userId, SearchOptions searchOptions) throws InvalidSessionException,
            PageOutOfRangeException, SearchException;

    /**
     * @param supervisorId
     * @param searchOptions
     * @return
     * @throws InvalidSessionException
     * @throws SearchException
     * @throws PageOutOfRangeException
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws InvalidSessionException,
            SearchException, PageOutOfRangeException;

    /**
     * @param supervisorId
     * @param searchOptions
     * @return
     * @throws InvalidSessionException
     * @throws SearchException
     * @throws PageOutOfRangeException
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws InvalidSessionException,
            SearchException, PageOutOfRangeException;

    /**
     * @param processDefinitionId
     * @param userId
     * @return
     * @throws InvalidSessionException
     * @throws ObjectCreationException
     * @throws ObjectNotFoundException
     */
    ProcessSupervisor createProcessSupervisorForUser(long processDefinitionId, long userId) throws InvalidSessionException, ObjectCreationException,
            ObjectNotFoundException;

    /**
     * @param processDefinitionId
     * @param roleId
     * @return
     * @throws InvalidSessionException
     * @throws ObjectCreationException
     * @throws ObjectNotFoundException
     */
    ProcessSupervisor createProcessSupervisorForRole(long processDefinitionId, long roleId) throws InvalidSessionException, ObjectCreationException,
            ObjectNotFoundException;

    /**
     * @param processDefinitionId
     * @param groupId
     * @return
     * @throws InvalidSessionException
     * @throws ObjectCreationException
     * @throws ObjectNotFoundException
     */
    ProcessSupervisor createProcessSupervisorForGroup(long processDefinitionId, long groupId) throws InvalidSessionException, ObjectCreationException,
            ObjectNotFoundException;

    /**
     * @param processDefinitionId
     * @param groupId
     * @param roleId
     * @return
     * @throws InvalidSessionException
     * @throws ObjectCreationException
     * @throws ObjectNotFoundException
     */
    ProcessSupervisor createProcessSupervisorForMembership(long processDefinitionId, long groupId, long roleId) throws InvalidSessionException,
            ObjectCreationException, ObjectNotFoundException;

    /**
     * @param supervisorId
     * @param memberType
     * @return
     * @throws InvalidSessionException
     * @throws ObjectReadException
     * @throws ObjectNotFoundException
     */
    ProcessSupervisor getSupervisor(long supervisorId, MemberType memberType) throws InvalidSessionException, ObjectReadException, ObjectNotFoundException;

    /**
     * @param processDefinitionId
     * @param userId
     * @return
     * @throws InvalidSessionException
     * @throws ObjectReadException
     */
    boolean isUserProcessSupervisor(long processDefinitionId, long userId) throws InvalidSessionException, ObjectReadException;

    /**
     * @param id
     * @throws InvalidSessionException
     * @throws ObjectNotFoundException
     * @throws ObjectDeletionException
     */
    void deleteSupervisor(long id) throws InvalidSessionException, ObjectNotFoundException, ObjectDeletionException;

    /**
     * @param supervisorId
     * @param memberType
     * @param searchOptions
     * @return
     * @throws InvalidSessionException
     * @throws SearchException
     * @throws PageOutOfRangeException
     */
    SearchResult<ProcessSupervisor> searchProcessSupervisors(MemberType memberType, SearchOptions searchOptions) throws InvalidSessionException,
            SearchException, PageOutOfRangeException;
    /**
     * Retrieve the list of connector instances on an activity instance
     * 
     * @param activityInstanceId
     *            the id of the element on which we want the connector instances
     * @param pageNumber
     * @param numberPerPage
     * @param order
     * @return
     *         the list of connector instance on this element
     * @throws PageOutOfRangeException
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfActivity(long activityInstanceId, int pageNumber, int numberPerPage, ConnectorInstanceCriterion order)
            throws InvalidSessionException, ObjectReadException, PageOutOfRangeException;

    /**
     * Retrieve the list of connector instances on a process instance
     * 
     * @param processInstanceId
     *            the id of the element on which we want the connector instances
     * @param pageNumber
     * @param numberPerPage
     * @param order
     * @return
     *         the list of connector instance on this element
     * @throws PageOutOfRangeException
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfProcess(long processInstanceId, int pageNumber, int numberPerPage, ConnectorInstanceCriterion order)
            throws InvalidSessionException, ObjectReadException, PageOutOfRangeException;

    /**
     * set this instance of connector in the specified state
     * 
     * @param connectorInstanceId
     *            the id of the connector to change
     * @param state
     *            the state to set on the connector
     * @since 6.0
     */
    void setConnectorInstanceState(long connectorInstanceId, ConnectorState state) throws InvalidSessionException, ObjectReadException,
            ObjectNotFoundException, ObjectModificationException;
}
