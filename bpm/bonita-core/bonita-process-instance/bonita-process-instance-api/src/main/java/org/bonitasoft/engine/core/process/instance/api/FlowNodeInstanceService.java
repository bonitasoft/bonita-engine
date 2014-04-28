/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface FlowNodeInstanceService {

    static final String FLOWNODE_INSTANCE = "FLOWNODE_INSTANCE";

    static final String ARCHIVED_FLOWNODE_INSTANCE = "ARCHIVED_FLOWNODE_INSTANCE";

    static final String ACTIVITYINSTANCE_STATE = "ACTIVITYINSTANCE_STATE";

    static final String ACTIVITY_INSTANCE_TOKEN_COUNT = "ACTIVITY_INSTANCE_TOKEN_COUNT";

    static final String ACTIVITYINSTANCE_DISPLAY_DESCRIPTION = "ACTIVITYINSTANCE_DISPLAY_DESCRIPTION";

    static final String LOOPINSTANCE_LOOPMAX_MODIFIED = "LOOPINSTANCE_LOOPMAX_MODIFIED";

    static final String MULTIINSTANCE_LOOPCARDINALITY_MODIFIED = "MULTIINSTANCE_LOOPMAX_MODIFIED";

    static final String MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED = "MULTIINSTANCE_LOOPMAX_MODIFIED";

    static final String ACTIVITYINSTANCE_DISPLAY_DESCRIPTION_MODIFIED = "ACTIVITYINSTANCE_DISPLAY_DESCRIPTION_MODIFIED";

    static final String ACTIVITYINSTANCE_DISPLAY_NAME = "ACTIVITYINSTANCE_DISPLAY_NAME";

    static final String STATE_CATEGORY = "STATE_CATEGORY";

    static final String EXECUTED_BY_MODIFIED = "EXECUTED_BY_MODIFIED";

    static final String EXECUTED_BY_SUBSTITUTE_MODIFIED = "EXECUTED_BY_SUBSTITUTE_MODIFIED";

    static final String EXPECTED_END_DATE_MODIFIED = "EXPECTED_END_DATE_MODIFIED";

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
    List<SFlowNodeInstance> getFlowNodeInstances(long rootContainerId, int fromIndex, int maxResults) throws SFlowNodeReadException;

    /**
     * @param flowNodeInstance
     * @param state
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void setState(SFlowNodeInstance flowNodeInstance, FlowNodeState state) throws SFlowNodeModificationException;

    /**
     * @param rootContainerId
     * @return
     * @throws SFlowNodeReadException
     * @since 6.0
     */
    List<SFlowNodeInstance> getActiveFlowNodes(long rootContainerId) throws SFlowNodeReadException;

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
     * @throws SBonitaSearchException
     * @since 6.0
     */
    long getNumberOfFlowNodeInstances(Class<? extends PersistentObject> entityClass, QueryOptions countOptions) throws SBonitaSearchException;

    /**
     * @param entityClass
     * @param countOptions
     * @return
     * @throws SBonitaSearchException
     * @since 6.0
     */
    long getNumberOfFlowNodeInstancesSupervisedBy(Long supervisorId, Class<? extends PersistentObject> entityClass, QueryOptions countOptions)
            throws SBonitaSearchException;

    /**
     * 
     * @param entityClass
     * @param searchOptions
     * @return
     * @throws SBonitaSearchException
     * @since 6.0
     */
    List<SFlowNodeInstance> searchFlowNodeInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * 
     * @param entityClass
     * @param searchOptions
     * @return
     * @throws SBonitaSearchException
     * @since 6.0
     */
    List<SFlowNodeInstance> searchFlowNodeInstancesSupervisedBy(Long supervisorId, Class<? extends PersistentObject> entityClass, QueryOptions searchOptions)
            throws SBonitaSearchException;

    /**
     * Set execute by for the specific flow node instance
     * 
     * @param flowNodeInstance
     *            the flowNodeInstance will be updated
     * @param userId
     *            value for executedBy
     * @throws SFlowNodeModificationException
     * @since 6.0
     */
    void setExecutedBy(SFlowNodeInstance sFlowNodeInstance, long userId) throws SFlowNodeModificationException;

    /**
     * Set execute by delegate for the specific flow node instance
     * 
     * @param flowNodeInstance
     *            the flowNodeInstance will be updated
     * @param executerSubstituteId
     *            value for executedBySubstitute
     * @throws SFlowNodeModificationException
     * @since 6.0.1
     */
    void setExecutedBySubstitute(SFlowNodeInstance sFlowNodeInstance, long executerSubstituteId) throws SFlowNodeModificationException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria.
     * 
     * @param entityClass
     *            The type of the archived flow node to search for
     * @param queryOptions
     *            The search options to filter the results
     * @return The number found, 0 if none matching search criteria
     * @since 6.0
     */
    long getNumberOfArchivedFlowNodeInstances(Class<? extends SAFlowNodeInstance> entityClass, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria, for a specific supervisor.
     * 
     * @param supervisorId
     *            The identifier of the supervisor
     * @param entityClass
     *            The type of the archived flow node to search for
     * @param queryOptions
     *            The search options to filter the results
     * @return The number found, 0 if no matching search criteria
     * @since 6.3
     */
    long getNumberOfArchivedFlowNodeInstancesSupervisedBy(long supervisorId, Class<? extends SAFlowNodeInstance> entityClass, QueryOptions queryOptions)
            throws SBonitaSearchException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria.
     * 
     * @param entityClass
     *            The type of the archived flow node to search for
     * @param queryOptions
     *            The search options to filter the results
     * @return The list of paginated results, according to the QueryOptions search criteria
     * @since 6.0
     */
    public <T extends SAFlowNodeInstance> List<T> searchArchivedFlowNodeInstances(Class<T> entityClass, QueryOptions queryOptions)
            throws SBonitaSearchException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria, for a specific supervisor.
     * 
     * @param supervisorId
     *            The identifier of the supervisor
     * @param entityClass
     *            The type of the archived flow node to search for
     * @param queryOptions
     *            The search options to filter the results
     * @return The list of paginated results, according to the QueryOptions search criteria
     * @since 6.3
     */
    List<SAFlowNodeInstance> searchArchivedFlowNodeInstancesSupervisedBy(long supervisorId, Class<? extends SAFlowNodeInstance> entityClass,
            QueryOptions queryOptions)
            throws SBonitaSearchException;

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
     * @param persistenceService
     * @return
     * @throws SFlowNodeReadException
     * @throws SFlowNodeNotFoundException
     * @since 6.0
     */
    SAFlowNodeInstance getArchivedFlowNodeInstance(long archivedFlowNodeInstanceId) throws SFlowNodeReadException, SFlowNodeNotFoundException;

    /**
     * 
     * @param sourceObjectFlowNodeInstanceId
     *            The source identifier of the flow node instance
     * @return The last archived flow node
     * @since 6.3
     */
    public <T extends SAFlowNodeInstance> T getLastArchivedFlowNodeInstance(final Class<T> entityClass, final long sourceObjectFlowNodeInstanceId)
            throws SBonitaSearchException;

    /**
     * @param flowNodeInstance
     * @throws SFlowNodeModificationException
     */
    void setExecuting(SFlowNodeInstance flowNodeInstance) throws SFlowNodeModificationException;

    /**
     * @param queryOptions
     * @return
     * @throws SFlowNodeReadException
     */
    List<SFlowNodeInstance> getFlowNodeInstancesToRestart(QueryOptions queryOptions) throws SFlowNodeReadException;

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
     *            the ID of the container of the flownode or process to get the process instance ID for.
     * @param containerType
     *            the type of container, can be one of {@link DataInstanceContainer#PROCESS_INSTANCE} or {@link DataInstanceContainer#ACTIVITY_INSTANCE}
     * @return the process instance id found
     * @throws SFlowNodeNotFoundException
     *             if containerType is an ACTIVITY_INSTANCE and the flownode/activity instance is not found with id containerId.
     * @throws SFlowNodeReadException
     *             if a read exception occurs.
     * @since 6.3
     */
    long getProcessInstanceId(final long containerId, final String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException;

}
