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

import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
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
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface FlowNodeInstanceService {

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

    String EXPECTED_END_DATE_MODIFIED = "EXPECTED_END_DATE_MODIFIED";

    SFlowNodeInstance getFlowNodeInstance(long flowNodeInstanceId) throws SFlowNodeNotFoundException, SFlowNodeReadException;

    List<SFlowNodeInstance> getFlowNodeInstances(long rootContainerId, int fromIndex, int maxResults) throws SFlowNodeReadException;

    void setState(SFlowNodeInstance flowNodeInstance, FlowNodeState state) throws SFlowNodeModificationException;

    List<SFlowNodeInstance> getActiveFlowNodes(long rootContainerId) throws SFlowNodeReadException;

    void setTaskPriority(SFlowNodeInstance flowNodeInstance, STaskPriority priority) throws SFlowNodeModificationException;

    void updateDisplayDescription(SFlowNodeInstance flowNodeInstance, String displayDescription) throws SFlowNodeModificationException;

    void updateDisplayName(SFlowNodeInstance flowNodeInstance, String displayName) throws SFlowNodeModificationException;

    void setStateCategory(SFlowElementInstance flowElementInstance, SStateCategory stateCategory) throws SFlowNodeModificationException;

    long getNumberOfFlowNodeInstances(Class<? extends PersistentObject> entityClass, QueryOptions countOptions) throws SBonitaSearchException;

    List<SFlowNodeInstance> searchFlowNodeInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * Set execute by for the specific activity instance
     * 
     * @param flowNodeInstance
     *            the flowNodeInstance will be updated
     * @param userId
     *            value of user name
     * @throws SActivityModificationException
     * @since 6.0
     */
    void setExecutedBy(SFlowNodeInstance flowNodeInstance, long userId) throws SActivityModificationException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria.
     * 
     * @param entityClass
     *            the type of the archived FlowNode to search for.
     * @param searchOptions
     *            the search options to filter the results
     * @return the number found, 0 if none matching search criteria
     * @since 6.0
     */
    long getNumberOfArchivedFlowNodeInstances(Class<? extends SAFlowNodeInstance> entityClass, QueryOptions countOptions) throws SBonitaSearchException;

    /**
     * Retrieve the total number of the archived flow nodes matching the given search criteria.
     * 
     * @param entityClass
     *            the type of the archived FlowNode to search for.
     * @param searchOptions
     *            the search options to filter the results
     * @return the list of paginated results, according to the QueryOptions search criteria
     * @since 6.0
     */
    List<SAFlowNodeInstance> searchArchivedFlowNodeInstances(Class<? extends SAFlowNodeInstance> entityClass, QueryOptions searchOptions)
            throws SBonitaSearchException;

    /**
     * @param flowNodeInstance
     * @param dueDate
     * @throws SActivityModificationException
     */
    void setExpectedEndDate(SFlowNodeInstance flowNodeInstance, long dueDate) throws SActivityModificationException;

    /**
     * @param rootContainerId
     * @param fromIndex
     * @param maxResults
     * @return
     * @throws SFlowNodeReadException
     */
    List<SAFlowNodeInstance> getArchivedFlowNodeInstances(long rootContainerId, int fromIndex, int maxResults) throws SFlowNodeReadException;

    SAFlowNodeInstance getArchivedFlowNodeInstance(long archivedFlowNodeInstanceId, ReadPersistenceService persistenceService) throws SFlowNodeReadException,
            SFlowNodeNotFoundException;

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

}
