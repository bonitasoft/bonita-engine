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
package org.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0
 */
public interface ProcessInstanceService {

    String PROCESSINSTANCE = "PROCESSINSTANCE";

    String PROCESSINSTANCE_STATE = "PROCESSINSTANCE_STATE";

    String MIGRATION_PLAN = "MIGRATION_PLAN";

    String PROCESS_INSTANCE_CATEGORY_STATE = "PROCESS_INSTANCE_CATEGORY_STATE";

    String PROCESSINSTANCE_STATE_UPDATED = "PROCESSINSTANCE_STATE_UPDATED";

    String PROCESSINSTANCE_TOKEN_COUNT = "ACTIVITY_INSTANCE_TOKEN_COUNT";

    String EVENT_TRIGGER_INSTANCE = "EVENT_TRIGGER_INSTANCE";

    /**
     * Create process instance in DB according to the given process instance object
     *
     * @param processInstance
     *        the processInstance
     * @throws SProcessInstanceCreationException
     */
    void createProcessInstance(SProcessInstance processInstance) throws SProcessInstanceCreationException;

    /**
     * Delete the id specified process instance
     *
     * @param processInstanceId
     *        identifier of process instance
     * @throws SProcessInstanceNotFoundException
     * @throws SProcessInstanceReadException
     * @throws SFlowNodeReadException
     * @throws SProcessInstanceModificationException
     * @throws SProcessInstanceHierarchicalDeletionException
     */
    void deleteProcessInstance(long processInstanceId) throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SFlowNodeReadException,
    SProcessInstanceModificationException, SProcessInstanceHierarchicalDeletionException;

    /**
     * Delete the specified process instance
     *
     * @param processInstance
     *        the process instance
     * @throws SFlowNodeReadException
     * @throws SProcessInstanceModificationException
     * @throws SProcessInstanceHierarchicalDeletionException
     * @since 6.0
     */
    void deleteProcessInstance(SProcessInstance processInstance) throws SFlowNodeReadException, SProcessInstanceModificationException,
    SProcessInstanceHierarchicalDeletionException;

    /**
     * Delete the specified process instances with id, and their elements archived and not, if are not a subProcess
     *
     * @param sProcessInstances
     *        list of process instances to deleted
     * @return Number of deleted process instances
     * @since 6.1
     */
    long deleteParentProcessInstanceAndElements(List<SProcessInstance> sProcessInstances) throws SFlowNodeReadException,
    SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException;

    /**
     * Delete the specified process instance with id, and its elements archived and not, if are not a subProcess
     *
     * @param processInstanceId
     *        identifier of process instance
     * @throws SProcessInstanceReadException
     * @throws SProcessInstanceNotFoundException
     * @throws SFlowNodeReadException
     * @throws SProcessInstanceHierarchicalDeletionException
     * @throws SProcessInstanceModificationException
     * @since 6.1
     */
    void deleteParentProcessInstanceAndElements(long processInstanceId) throws SProcessInstanceReadException, SProcessInstanceNotFoundException,
    SFlowNodeReadException, SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException;

    /**
     * Delete the specified process instance, and its elements archived and not, if are not a subProcess
     *
     * @param processInstance
     *        The {@link SProcessInstance} to delete
     * @since 6.4.0
     */
    void deleteParentProcessInstanceAndElements(SProcessInstance processInstance) throws SFlowNodeReadException, SProcessInstanceHierarchicalDeletionException,
    SProcessInstanceModificationException;

    /**
     * Get process instance by its id
     *
     * @param processInstanceId
     *        identifier of process instance
     * @return the process instance object
     * @throws SProcessInstanceNotFoundException
     * @throws SProcessInstanceReadException
     */
    SProcessInstance getProcessInstance(long processInstanceId) throws SProcessInstanceNotFoundException, SProcessInstanceReadException;

    /**
     * Set state for the processInstance
     *
     * @param processInstance
     *        the process instance will be updated
     * @param state
     *        the state will be set to the process instance
     * @throws SProcessInstanceNotFoundException
     * @throws SProcessInstanceModificationException
     */
    void setState(SProcessInstance processInstance, ProcessInstanceState state) throws SProcessInstanceNotFoundException, SProcessInstanceModificationException;

    /**
     * Set process state category for the given process instance
     *
     * @param processInstance
     *        process instance to update
     * @param stateCatetory
     *        new category state for the process instance
     * @throws SProcessInstanceNotFoundException
     * @throws SProcessInstanceModificationException
     * @since 6.0
     */
    void setStateCategory(SProcessInstance processInstance, SStateCategory stateCatetory) throws SProcessInstanceNotFoundException,
    SProcessInstanceModificationException;

    /**
     * Delete specified archived process instance
     *
     * @param archivedProcessInstance
     *        the archived process instance
     * @throws SProcessInstanceModificationException
     * @throws SFlowNodeReadException
     * @since 6.0
     */
    void deleteArchivedProcessInstance(SAProcessInstance archivedProcessInstance) throws SProcessInstanceModificationException, SFlowNodeReadException;

    /**
     * Delete specified archived process instances
     *
     * @param saProcessInstances
     *        List of archived process instances to delete
     * @return Number of deleted archived process instances
     * @throws SProcessInstanceModificationException
     * @throws SProcessInstanceHierarchicalDeletionException
     * @throws SFlowNodeReadException
     * @since 6.1
     */
    long deleteArchivedParentProcessInstancesAndElements(List<SAProcessInstance> saProcessInstances) throws SFlowNodeReadException,
    SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException;

    /**
     * Delete the specified archived process instance
     *
     * @param saProcessInstance
     *        The archived process instances to delete
     * @throws SProcessInstanceModificationException
     * @throws SProcessInstanceHierarchicalDeletionException
     * @throws SFlowNodeReadException
     * @since 6.4.0
     */
    void deleteArchivedParentProcessInstanceAndElements(SAProcessInstance saProcessInstance) throws SFlowNodeReadException,
    SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException;

    /**
     * Delete all archived elements related to the specified process instance, even the archived process instances
     *
     * @param processInstanceId
     *        the process instance id
     * @throws SFlowNodeReadException
     * @throws SProcessInstanceModificationException
     * @since 6.0
     */
    void deleteArchivedProcessInstanceElements(long processInstanceId, final long processDefinitionId) throws SFlowNodeReadException,
    SProcessInstanceModificationException;

    /**
     * Get child instance identifiers for specific process instance, this can be used for pagination
     *
     * @param processInstanceId
     *        identifier of process instance
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param maxResults
     *        Number of result we want to get. Maximum number of result returned
     * @param sortingField
     *        the field used to do order
     * @param sortingOrder
     *        ASC or DESC
     * @return a list of identifiers
     * @throws SProcessInstanceReadException
     */
    List<Long> getChildInstanceIdsOfProcessInstance(long processInstanceId, int fromIndex, int maxResults, String sortingField, OrderByType sortingOrder)
            throws SProcessInstanceReadException;

    /**
     * Get child process instance for the specific call activity or subprocess activity
     *
     * @param activityInstId
     *        identifier of call activity or subprocess activity
     * @return an SProcessInstance object
     * @throws SProcessInstanceNotFoundException
     * @throws SBonitaReadException
     */
    SProcessInstance getChildOfActivity(long activityInstId) throws SProcessInstanceNotFoundException, SBonitaReadException;

    /**
     * Get total number of child instance for specific process instance
     *
     * @param processInstanceId
     *        identifier of process instance
     * @return number of child instance for the process instance
     * @throws SProcessInstanceReadException
     */
    long getNumberOfChildInstancesOfProcessInstance(long processInstanceId) throws SProcessInstanceReadException;

    /**
     * Get the archived process instances corresponding to the identifiers
     *
     * @param archivedProcessInstanceIds
     *        Identifier of the {@link SAProcessInstance}s
     * @return The list of {@link SAProcessInstance}
     * @throws SProcessInstanceReadException
     * @since 6.4.0
     */
    List<SAProcessInstance> getArchivedProcessInstancesInAllStates(List<Long> processInstanceIds) throws SProcessInstanceReadException;

    /**
     * Get total number of archived process instances according to specific criteria
     *
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return number of archived process instances
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedProcessInstances(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all archived process instance according to specific criteria
     *
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return A list of all archived process instance according to specific criteria
     * @throws SBonitaReadException
     */
    List<SAProcessInstance> searchArchivedProcessInstances(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get the latest archived process instance object for the specific process instance
     *
     * @param archivedProcessInstanceId
     *        identifier of the archived process instance (not the process instance)
     * @param persistenceService
     * @return an SAProcessInstance object
     * @throws SProcessInstanceReadException
     */
    SAProcessInstance getArchivedProcessInstance(long archivedProcessInstanceId) throws SProcessInstanceReadException;

    /**
     * Get total number of process instances
     *
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return total number of process instances
     * @throws SBonitaReadException
     */
    long getNumberOfProcessInstances(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all process instance according to specific criteria
     *
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return a list of SProcessInstance objects
     * @throws SBonitaReadException
     */
    List<SProcessInstance> searchProcessInstances(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of open process instances for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return number of open process instance for the specific supervisor
     * @throws SBonitaReadException
     */
    long getNumberOfOpenProcessInstancesSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of {@link SProcessInstance} with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR}
     * state for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return The number of the {@link SProcessInstance} with at least one failed task or the
     *         {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR} state for the specific supervisor
     * @throws SBonitaReadException
     * @since 7.0
     */
    long getNumberOfFailedProcessInstancesSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all {@link SProcessInstance} with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR} state
     * for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return The list of {@link SProcessInstance} with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR}
     *         state for the specific supervisor
     * @throws SBonitaReadException
     * @since 7.0
     */
    List<SProcessInstance> searchFailedProcessInstancesSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all open process instances for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return a list of SProcessInstance objects
     * @throws SBonitaReadException
     */
    List<SProcessInstance> searchOpenProcessInstancesSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of open process instance involving the specific user
     *
     * @param userId
     *        identifier of user who can perform or be assigned to tasks in process instance.
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return number of open process instance for the specific user
     * @throws SBonitaReadException
     */
    long getNumberOfOpenProcessInstancesInvolvingUser(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all open process instance involving the specific user
     *
     * @param userId
     *        identifier of user who can perform or be assigned to tasks in process instance.
     * @param queryOptions
     * @return a list of SProcessInstance objects
     * @throws SBonitaReadException
     */
    List<SProcessInstance> searchOpenProcessInstancesInvolvingUser(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of open process instance involving all users of the specific manager
     *
     * @param managerUserId
     * @param queryOptions
     * @return
     * @throws SBonitaReadException
     */
    long getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(long managerUserId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all open process instance involving all users of the specific manager
     *
     * @param managerUserId
     * @param queryOptions
     * @return
     * @throws SBonitaReadException
     */
    List<SProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(long managerUserId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get the list of sourceObjectIds for archived process instances children of process instance identified by rootProcessIntanceId
     *
     * @param rootProcessIntanceId
     *        the root process instance id
     * @param fromIndex
     *        index of first result to be retried
     * @param maxResults
     *        max number of results to be retrieved
     * @param sortingOrder
     *        the searching order (ASC or DESC)
     * @return the list of sourceObjectIds for archived process instances children of process instance identified by rootProcessIntanceId
     * @throws SBonitaReadException
     * @since 6.0
     */
    List<Long> getArchivedChildrenSourceObjectIdsFromRootProcessInstance(long rootProcessIntanceId, int fromIndex, int maxResults, OrderByType sortingOrder)
            throws SBonitaReadException;

    /**
     * Get total number of archived process instance according to the search criteria
     *
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return number of archived process instance satisfied to the search criteria
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedProcessInstancesWithoutSubProcess(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all archived process instance according to the search criteria
     *
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return a list of SAProcessInstance objects
     * @throws SBonitaReadException
     */
    List<SAProcessInstance> searchArchivedProcessInstancesWithoutSubProcess(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of archived process instance for the specific supervisor
     *
     * @param userId
     *        identifier of user who is the supervisor of archived process instance.
     * @param countOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return number of archived process instance for the specific supervisor
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedProcessInstancesSupervisedBy(long userId, QueryOptions countOptions) throws SBonitaReadException;

    /**
     * Search all archived process instance for the specific supervisor
     *
     * @param userId
     *        identifier of user who is the supervisor of archived process instance.
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return a list of SAProcessInstance objects
     * @throws SBonitaReadException
     */
    List<SAProcessInstance> searchArchivedProcessInstancesSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get total number of archived process instance involving the specific user
     *
     * @param userId
     *        the identifier of user who is assignee of tasks of process instance
     * @param countOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return number of archived process instance involving the specific user
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedProcessInstancesInvolvingUser(long userId, QueryOptions countOptions) throws SBonitaReadException;

    /**
     * Search all archived process instance involving the specific user
     *
     * @param userId
     *        the identifier of user who is assignee of tasks of process instance
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return a list of SAProcessInstance objects
     * @throws SBonitaReadException
     */
    List<SAProcessInstance> searchArchivedProcessInstancesInvolvingUser(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Update the specific process instance
     *
     * @param processInstance
     *        the processInstance will be updated
     * @param descriptor
     *        update description
     * @throws SProcessInstanceModificationException
     */
    void updateProcess(SProcessInstance processInstance, EntityUpdateDescriptor descriptor) throws SProcessInstanceModificationException;

    /**
     * set reference to the migration plan on the process instance
     *
     * @param processInstance
     * @param migrationPlanId
     * @throws SProcessInstanceModificationException
     */
    void setMigrationPlanId(SProcessInstance processInstance, long migrationPlanId) throws SProcessInstanceModificationException;

    /**
     * @param flowNodeInstance
     * @param processDefinition
     * @throws SFlowNodeReadException
     * @throws SProcessInstanceModificationException
     */
    void deleteFlowNodeInstance(SFlowNodeInstance flowNodeInstance, SProcessDefinition processDefinition) throws SFlowNodeReadException,
    SProcessInstanceModificationException;

    /**
     * @param processDefinitionId
     * @param fromIndex
     * @param maxResults
     * @param sortingOrder
     * @return
     * @throws SProcessInstanceReadException
     */
    List<Long> getSourceProcesInstanceIdsOfArchProcessInstancesFromDefinition(long processDefinitionId, int fromIndex, int maxResults, OrderByType sortingOrder)
            throws SProcessInstanceReadException;

    /**
     * @param queryOptions
     * @param initializing
     * @return
     */
    List<SProcessInstance> getProcessInstancesInState(QueryOptions queryOptions, ProcessInstanceState state) throws SProcessInstanceReadException;

    /**
     * Returns the list of <code>SProcessInstance</code>s in a given list of states.
     *
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @param states
     *        the list of valid states to retrieve.
     * @return the list of matching <code>SProcessInstance</code>s
     * @throws SProcessInstanceReadException
     *         if a read error occurs
     */
    List<SProcessInstance> getProcessInstancesInStates(final QueryOptions queryOptions, final ProcessInstanceState... states)
            throws SProcessInstanceReadException;

    /**
     * @param processInstanceId
     * @throws SBonitaException
     * @since 6.1
     */
    void deleteArchivedProcessInstancesOfProcessInstance(long processInstanceId) throws SBonitaException;

    /**
     * @param sourceObjectProcessInstanceId
     *        The source identifier of the process instance
     * @return The last archived process instance
     * @since 6.3
     */
    SAProcessInstance getLastArchivedProcessInstance(long sourceObjectProcessInstanceId) throws SBonitaReadException;

    /**
     * Get the number of the {@link SProcessInstance} with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR}
     * state.
     *
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return The number of the {@link SProcessInstance} with at least one failed task or the
     *         {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR} state.
     * @throws SBonitaException
     * @since 6.4.0
     */
    long getNumberOfFailedProcessInstances(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * List all {@link SProcessInstance} with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR} state.
     *
     * @param queryOptions
     *        the search criteria containing a map of specific parameters of a query
     * @return The list of {@link SProcessInstance} with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR}
     *         state.
     * @throws SBonitaException
     * @since 6.4.0
     */
    List<SProcessInstance> searchFailedProcessInstances(QueryOptions queryOptions) throws SBonitaReadException;

    long getNumberOfProcessInstances(long processDefinitionId) throws SBonitaReadException;

}
