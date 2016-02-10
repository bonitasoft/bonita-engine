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

import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstanceStateCounter;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface FlowNodeInstanceService {

    String FLOWNODE_INSTANCE = "FLOWNODE_INSTANCE";

    String ARCHIVED_FLOWNODE_INSTANCE = "ARCHIVED_FLOWNODE_INSTANCE";

    String ACTIVITYINSTANCE_STATE = "ACTIVITYINSTANCE_STATE";

    String ACTIVITY_INSTANCE_TOKEN_COUNT = "ACTIVITY_INSTANCE_TOKEN_COUNT";

    String ACTIVITYINSTANCE_DISPLAY_DESCRIPTION = "ACTIVITYINSTANCE_DISPLAY_DESCRIPTION";

    String LOOPINSTANCE_LOOPMAX_MODIFIED = "LOOPINSTANCE_LOOPMAX_MODIFIED";

    String MULTIINSTANCE_LOOPCARDINALITY_MODIFIED = "MULTIINSTANCE_LOOPMAX_MODIFIED";

    String MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED = "MULTIINSTANCE_LOOPMAX_MODIFIED";

    String ACTIVITYINSTANCE_DISPLAY_DESCRIPTION_MODIFIED = "ACTIVITYINSTANCE_DISPLAY_DESCRIPTION_MODIFIED";

    String ACTIVITYINSTANCE_DISPLAY_NAME = "ACTIVITYINSTANCE_DISPLAY_NAME";

    String STATE_CATEGORY = "STATE_CATEGORY";

    String EXECUTED_BY_MODIFIED = "EXECUTED_BY_MODIFIED";

    String EXECUTED_BY_SUBSTITUTE_MODIFIED = "EXECUTED_BY_SUBSTITUTE_MODIFIED";

    String EXPECTED_END_DATE_MODIFIED = "EXPECTED_END_DATE_MODIFIED";

    /**
     * @param flowNodeInstanceId
     * @return
     * @throws SFlowNodeNotFoundException
     * @throws SFlowNodeReadException
     * @since 6.0
     */
    SFlowNodeInstance getFlowNodeInstance(long flowNodeInstanceId) throws SFlowNodeNotFoundException, SFlowNodeReadException;

    /**
     * @param rootContainerId
     * @param fromIndex
     * @param maxResults
     * @return
     * @throws SFlowNodeReadException
     * @since 6.0
     */
    List<SFlowNodeInstance> getFlowNodeInstances(long parentContainerId, int fromIndex, int maxResults) throws SFlowNodeReadException;

    /**
     * @param flowNodeInstance
     * @param state
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void setState(SFlowNodeInstance flowNodeInstance, FlowNodeState state) throws SFlowNodeModificationException;

    /**
     * @param flowNodeInstance
     * @param priority
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void setTaskPriority(SFlowNodeInstance flowNodeInstance, STaskPriority priority) throws SFlowNodeModificationException;

    /**
     * @param flowNodeInstance
     * @param displayDescription
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void updateDisplayDescription(SFlowNodeInstance flowNodeInstance, String displayDescription) throws SFlowNodeModificationException;

    /**
     * @param flowNodeInstance
     * @param displayName
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void updateDisplayName(SFlowNodeInstance flowNodeInstance, String displayName) throws SFlowNodeModificationException;

    /**
     * @param flowElementInstance
     * @param stateCategory
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void setStateCategory(SFlowElementInstance flowElementInstance, SStateCategory stateCategory) throws SFlowNodeModificationException;

    /**
     * @param entityClass
     * @param countOptions
     * @return
     * @throws SBonitaReadException
     * @since 6.0
     */
    long getNumberOfFlowNodeInstances(Class<? extends SFlowNodeInstance> entityClass, QueryOptions countOptions) throws SBonitaReadException;

    /**
     * @param entityClass
     * @param countOptions
     * @return
     * @throws SBonitaReadException
     * @since 6.0
     */
    long getNumberOfFlowNodeInstancesSupervisedBy(Long supervisorId, Class<? extends SFlowNodeInstance> entityClass, QueryOptions countOptions)
            throws SBonitaReadException;

    /**
     * @param entityClass
     * @param searchOptions
     * @return
     * @throws SBonitaReadException
     * @since 6.0
     */
    <T extends SFlowNodeInstance> List<T> searchFlowNodeInstances(Class<T> entityClass, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * @param entityClass
     * @param searchOptions
     * @return
     * @throws SBonitaReadException
     * @since 6.0
     */
    <T extends SFlowNodeInstance> List<T> searchFlowNodeInstancesSupervisedBy(Long supervisorId, Class<T> entityClass, QueryOptions searchOptions)
            throws SBonitaReadException;

    /**
     * Counts the number of flownode instances in all states. Only considers flownodes direcly contained in given process instance, not flownodes in
     * sub-process instances. Results are counted per flownode name and per state.
     *
     * @param processInstanceId the ID of the process instance to search flownodes for.
     * @return a map of &lt;flownodename, number of rows with that name&gt;. If no results, returns an empty Map.
     * @throws SBonitaReadException if a read exception occurs.
     */
    List<SFlowNodeInstanceStateCounter> getNumberOfFlownodesInAllStates(final long processInstanceId) throws SBonitaReadException;

    /**
     * Counts the number of archived flownode instances in a specific state. Only considers archived flownodes direcly contained in given process instance, not
     * flownodes in sub-process instances. Results are counted per flownode name and per state.
     *
     * @param processInstanceId the ID of the process instance to search flownodes for. This is the ID of the process instance before it was archived
     *        (corresponding to the sourceObjectId in the archives)
     * @return a map of &lt;flownodename, number of rows with that name&gt;. If no results, returns an empty Map.
     * @throws SBonitaReadException if a read exception occurs.
     */
    public List<SFlowNodeInstanceStateCounter> getNumberOfArchivedFlownodesInAllStates(final long processInstanceId) throws SBonitaReadException;

    /**
     * Set execute by for the specific flow node instance
     *
     * @param sFlowNodeInstance
     *        the flowNodeInstance will be updated
     * @param userId
     *        value for executedBy
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void setExecutedBy(SFlowNodeInstance sFlowNodeInstance, long userId) throws SFlowNodeModificationException;

    /**
     * Set execute by delegate for the specific flow node instance
     *
     * @param sFlowNodeInstance
     *        the flowNodeInstance will be updated
     * @param executerSubstituteId
     *        value for executedBySubstitute
     * @throws SFlowNodeModificationException
     * @since 6.0.1
     */
    void setExecutedBySubstitute(SFlowNodeInstance sFlowNodeInstance, long executerSubstituteId) throws SFlowNodeModificationException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria.
     *
     * @param entityClass
     *        The type of the archived flow node to search for
     * @param queryOptions
     *        The search options to filter the results
     * @return The number found, 0 if none matching search criteria
     * @since 6.0
     */
    long getNumberOfArchivedFlowNodeInstances(Class<? extends SAFlowNodeInstance> entityClass, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria, for a specific supervisor.
     *
     * @param supervisorId
     *        The identifier of the supervisor
     * @param entityClass
     *        The type of the archived flow node to search for
     * @param queryOptions
     *        The search options to filter the results
     * @return The number found, 0 if no matching search criteria
     * @since 6.3
     */
    long getNumberOfArchivedFlowNodeInstancesSupervisedBy(long supervisorId, Class<? extends SAFlowNodeInstance> entityClass, QueryOptions queryOptions)
            throws SBonitaReadException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria.
     *
     * @param entityClass
     *        The type of the archived flow node to search for
     * @param queryOptions
     *        The search options to filter the results
     * @return The list of paginated results, according to the QueryOptions search criteria
     * @since 6.0
     */
    <T extends SAFlowNodeInstance> List<T> searchArchivedFlowNodeInstances(Class<T> entityClass, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria, for a specific supervisor.
     *
     * @param supervisorId
     *        The identifier of the supervisor
     * @param entityClass
     *        The type of the archived flow node to search for
     * @param queryOptions
     *        The search options to filter the results
     * @return The list of paginated results, according to the QueryOptions search criteria
     * @since 6.3
     */
    <T extends SAFlowNodeInstance> List<T> searchArchivedFlowNodeInstancesSupervisedBy(long supervisorId, Class<T> entityClass, QueryOptions queryOptions)
            throws SBonitaReadException;

    /**
     * @param flowNodeInstance
     * @param dueDate
     * @throws SFlowNodeModificationException
     */
    void setExpectedEndDate(SFlowNodeInstance flowNodeInstance, long dueDate) throws SFlowNodeModificationException;

    /**
     * @param rootContainerId
     * @param fromIndex
     * @param maxResults
     * @return
     * @throws SFlowNodeReadException
     */
    List<SAFlowNodeInstance> getArchivedFlowNodeInstances(long rootContainerId, int fromIndex, int maxResults) throws SFlowNodeReadException;

    /**
     * @param archivedFlowNodeInstanceId
     * @return
     * @throws SFlowNodeReadException
     * @throws SFlowNodeNotFoundException
     * @since 6.0
     */
    SAFlowNodeInstance getArchivedFlowNodeInstance(long archivedFlowNodeInstanceId) throws SFlowNodeReadException, SFlowNodeNotFoundException;

    /**
     * @param sourceObjectFlowNodeInstanceId
     *        The source identifier of the flow node instance
     * @return The last archived flow node
     * @since 6.3
     */
    <T extends SAFlowNodeInstance> T getLastArchivedFlowNodeInstance(final Class<T> entityClass, final long sourceObjectFlowNodeInstanceId)
            throws SBonitaReadException;

    /**
     * @param flowNodeInstance
     * @throws SFlowNodeModificationException
     */
    void setExecuting(SFlowNodeInstance flowNodeInstance) throws SFlowNodeModificationException;

    /**
     * @param saFlowNodeInstance
     * @throws SFlowNodeReadException
     * @throws SFlowNodeDeletionException
     * @since 6.1
     */
    void deleteArchivedFlowNodeInstance(SAFlowNodeInstance saFlowNodeInstance) throws SFlowNodeReadException, SFlowNodeDeletionException;

    /**
     * @param sFlowNodeInstance
     * @throws SFlowNodeReadException
     * @throws SFlowNodeDeletionException
     * @since 6.1
     */
    void deleteFlowNodeInstance(SFlowNodeInstance sFlowNodeInstance) throws SFlowNodeReadException, SFlowNodeDeletionException;

    /**
     * Get the process instance ID. It can be itself if containerType is a PROCESS_INSTANCE, or the containing process instance id if containerType is a
     * ACTIVITY_INSTANCE.
     *
     * @param containerId
     *        the ID of the container of the flownode or process to get the process instance ID for.
     * @param containerType
     *        the type of container, can be one of {@link DataInstanceContainer#PROCESS_INSTANCE} or {@link DataInstanceContainer#ACTIVITY_INSTANCE}
     * @return the process instance id found
     * @throws SFlowNodeNotFoundException
     *         if containerType is an ACTIVITY_INSTANCE and the flownode/activity instance is not found with id containerId.
     * @throws SFlowNodeReadException
     *         if a read exception occurs.
     * @since 6.3
     */
    long getProcessInstanceId(final long containerId, final String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException;

    /**
     * retrieve ids of elements that need to be restarted
     * Called on start node to set the flag to tell the engine to restart these flow nodes
     * Should not be called when the engine is started!
     *
     * @param queryOptions
     * @return
     * @throws SBonitaReadException
     */
    List<Long> getFlowNodeInstanceIdsToRestart(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     *
     * get the number of flow node is this root container
     * @param rootContainerId
     * @return the number of flow node
     * @since 6.5
     */
    int getNumberOfFlowNodes(long rootContainerId) throws SBonitaReadException;
}
