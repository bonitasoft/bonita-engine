/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.recorder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class SelectDescriptorBuilder {

    // FIXME put in a common model
    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz,
            final String elementName, final long id) {
        return new SelectByIdDescriptor<>(clazz, id);
    }

    public static SelectListDescriptor<SAFlowNodeInstance> getArchivedFlowNodesFromProcessInstance(
            final long rootContainerId, final int fromIndex,
            final int maxResults) {
        final Map<String, Object> parameters = Collections.singletonMap("rootContainerId", rootContainerId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        return new SelectListDescriptor<>("getArchivedFlowNodesFromProcessInstance", parameters,
                SAFlowNodeInstance.class, queryOptions);
    }

    public static SelectListDescriptor<SAActivityInstance> getArchivedActivitiesFromProcessInstance(
            final long rootContainerId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("rootContainerId", rootContainerId);
        return new SelectListDescriptor<>("getAActivitiesFromProcessInstance", parameters, SAActivityInstance.class,
                queryOptions);
    }

    public static SelectListDescriptor<Long> getSourceProcesInstanceIdsOfArchProcessInstancesFromDefinition(
            final long processDefinitionId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("processDefinitionId",
                processDefinitionId);
        return new SelectListDescriptor<>("getSourceProcessInstanceIdsByProcessDefinitionId", parameters,
                SAProcessInstance.class, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getSpecificQueryWithParameters(
            final Class<T> clazz, final String queryName,
            final Map<String, Object> parameters, final QueryOptions queryOptions) {
        return new SelectListDescriptor<>(queryName, parameters, clazz, queryOptions);
    }

    public static SelectListDescriptor<SHumanTaskInstance> getAssignedUserTasks(final long userId, final int fromIndex,
            final int maxResults,
            final String sortFieldName, final OrderByType order) {
        final Map<String, Object> parameters = Collections.singletonMap("assigneeId", userId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SActivityInstance.class,
                sortFieldName, order);
        return new SelectListDescriptor<>("getAssignedUserTasks", parameters, SHumanTaskInstance.class, queryOptions);
    }

    public static SelectListDescriptor<SHumanTaskInstance> getPendingUserTasks(final long userId,
            final Set<Long> actorIds, final int fromIndex,
            final int maxResults, final String sortFieldName, final OrderByType order) {
        final Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("actorIds", actorIds);
        parameters.put("userId", userId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SActivityInstance.class,
                sortFieldName, order);
        return new SelectListDescriptor<>("getPendingUserTasks", parameters, SHumanTaskInstance.class, queryOptions);
    }

    public static SelectListDescriptor<SHumanTaskInstance> getPendingUserTasks(final long userId, final int fromIndex,
            final int maxResults,
            final String sortFieldName, final OrderByType order) {
        final Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("userId", userId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SActivityInstance.class,
                sortFieldName, order);
        return new SelectListDescriptor<>("getPendingUserTasksWithoutActorIds", parameters, SHumanTaskInstance.class,
                queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfProcessInstances() {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return new SelectOneDescriptor<>("getNumberOfProcessInstances", emptyMap, SProcessInstance.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfArchivedProcessInstances() {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return new SelectOneDescriptor<>("getNumberOfArchivedProcessInstances", emptyMap, SAProcessInstance.class,
                Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfOpenActivities(final long rootContainerId) {
        final Map<String, Object> parameters = Collections.singletonMap("rootContainerId", rootContainerId);
        return new SelectOneDescriptor<>("getNumberOfOpenActivities", parameters, SActivityInstance.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfAssignedHumanTaskInstances(final long userId) {
        final Map<String, Object> parameters = Collections.singletonMap("assigneeId", userId);
        return new SelectOneDescriptor<>("getNumberOfAssignedUserTaskInstances", parameters, SHumanTaskInstance.class,
                Long.class);
    }

    public static SelectOneDescriptor<SGatewayInstance> getActiveGatewayInstanceOfProcess(
            final long parentProcessInstanceId, final String name) {
        final Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("parentProcessInstanceId", parentProcessInstanceId);
        parameters.put("name", name);
        return new SelectOneDescriptor<>("getActiveGatewayInstanceOfProcess", parameters, SGatewayInstance.class);
    }

    public static SelectListDescriptor<SActivityInstance> getActivitiesFromProcessInstance(final long rootContainerId,
            final int fromIndex,
            final int maxResults) {
        final Map<String, Object> parameters = Collections.singletonMap("rootContainerId", rootContainerId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        return new SelectListDescriptor<>("getActivitiesFromProcessInstance", parameters, SActivityInstance.class,
                queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfActivitiesFromProcessInstance(final long rootContainerId) {
        final Map<String, Object> parameters = Collections.singletonMap("rootContainerId", rootContainerId);
        return new SelectOneDescriptor<>("getNumberOfActivitiesFromProcessInstance", parameters,
                SFlowNodeInstance.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfFlowNode(final long parentProcessInstanceId) {
        final Map<String, Object> parameters = Collections.singletonMap("parentProcessInstanceId",
                parentProcessInstanceId);
        return new SelectOneDescriptor<>("getNumberOfFlowNode", parameters, SFlowNodeInstance.class, Long.class);
    }

    public static SelectListDescriptor<SEventInstance> getEventsFromRootContainer(final long rootContainerId,
            final int fromIndex, final int maxResults,
            final String field, final OrderByType orderByType) {
        final Map<String, Object> parameters = Collections.singletonMap("rootContainerId", rootContainerId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SEventInstance.class, field,
                orderByType);
        return new SelectListDescriptor<>("getEventInstancesFromRootContainer", parameters, SEventInstance.class,
                queryOptions);
    }

    public static SelectListDescriptor<SBoundaryEventInstance> getActivityBoundaryEvents(final long activityInstanceId,
            final int fromIndex,
            final int maxResults) {
        final Map<String, Object> parameters = Collections.singletonMap("activityInstanceId",
                activityInstanceId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        return new SelectListDescriptor<>("getActivityBoundaryEventInstances", parameters, SBoundaryEventInstance.class,
                queryOptions);
    }

    public static SelectListDescriptor<STimerEventTriggerInstance> getEventTriggers(final long eventInstanceId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("eventInstanceId", eventInstanceId);
        return new SelectListDescriptor<>("getEventTriggerInstances", parameters, STimerEventTriggerInstance.class,
                queryOptions);
    }

    public static SelectListDescriptor<Long> getChildInstanceIdsOfProcessInstance(final Class<SProcessInstance> class1,
            final long processInstanceId,
            final QueryOptions queryOptions) {
        final Map<String, Object> map = Collections.singletonMap("processInstanceId", processInstanceId);
        return new SelectListDescriptor<>("getChildInstanceIdsOfProcessInstance", map, class1, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfChildInstancesOfProcessInstance(final long processInstanceId) {
        final Map<String, Object> parameters = Collections.singletonMap("processInstanceId",
                processInstanceId);
        return new SelectOneDescriptor<>("getNumberOfChildInstancesOfProcessInstance", parameters,
                SProcessInstance.class, Long.class);
    }

    public static SelectListDescriptor<SWaitingErrorEvent> getCaughtError(final long relatedActivityInstanceId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("relatedActivityInstanceId", relatedActivityInstanceId);
        return new SelectListDescriptor<>("getCaughtErrorByRelatedActivityAndAnyErrorCode", parameters,
                SWaitingErrorEvent.class,
                queryOptions);
    }

    public static SelectListDescriptor<SWaitingErrorEvent> getCaughtError(final long relatedActivityInstanceId,
            final String errorCode,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("relatedActivityInstanceId", relatedActivityInstanceId);
        parameters.put("errorCode", errorCode);
        return new SelectListDescriptor<>("getCaughtErrorByRelatedActivityAndErrorCode", parameters,
                SWaitingErrorEvent.class, queryOptions);
    }

    public static SelectListDescriptor<SWaitingSignalEvent> getListeningSignals(final String signalName,
            final int fromIndex, final int maxResults) {
        final Map<String, Object> parameters = Collections.singletonMap("signalName", signalName);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        return new SelectListDescriptor<>("getListeningSignals", parameters, SWaitingSignalEvent.class, queryOptions);
    }

    public static SelectListDescriptor<SMessageEventCouple> getMessageEventCouples(final int fromIndex,
            final int maxResults) {
        final Map<String, Object> parameters = Collections.emptyMap();
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        return new SelectListDescriptor<>("getMessageEventCouples", parameters, SMessageEventCouple.class,
                queryOptions);
    }

    public static SelectOneDescriptor<SAActivityInstance> getArchivedActivityInstanceWithActivityIdAndStateId(
            final long activityInstanceId,
            final int stateId) {
        final Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("activityInstanceId", activityInstanceId);
        parameters.put("stateId", stateId);
        return new SelectOneDescriptor<>("getAActivityInstanceByActivityInstanceIdAndStateId", parameters,
                SAActivityInstance.class);
    }

    public static SelectOneDescriptor<SAActivityInstance> getMostRecentArchivedActivityInstance(
            final long activityInstanceId) {
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("activityInstanceId", activityInstanceId);
        return new SelectOneDescriptor<>("getMostRecentArchivedActivityInstance", parameters, SAActivityInstance.class);
    }

    public static SelectListDescriptor<SHumanTaskInstance> searchAssignedTasksSupervisedBy(final long supervisorId,
            final int fromIndex, final int maxResults) {
        final Map<String, Object> parameters = Collections.singletonMap("supervisorId", supervisorId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        return new SelectListDescriptor<>("searchAssignedTasksSupervisedBy", parameters, SHumanTaskInstance.class,
                queryOptions);
    }

    public static SelectListDescriptor<Map<String, Long>> getNumbersOfAssignedOpenTasks(final List<Long> userIds) {
        final QueryOptions queryOptions = new QueryOptions(0, userIds.size());
        final Map<String, Object> parameters = Collections.singletonMap("assigneeIds", userIds);
        return new SelectListDescriptor<>("getNumbersOfOpenTasksForUsers", parameters, SHumanTaskInstance.class,
                queryOptions);
    }

    public static SelectListDescriptor<Map<Long, Long>> getNumbersOfAssignedOverdueOpenTasks(final List<Long> userIds) {
        final QueryOptions queryOptions = new QueryOptions(0, userIds.size());
        final Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("assigneeIds", userIds);
        parameters.put("currentTime", System.currentTimeMillis());
        return new SelectListDescriptor<>("getNumbersOfAssignedOverdueTasksForUsers", parameters,
                SHumanTaskInstance.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfPendingOverdueOpenTasksForUser(final Long userId) {
        final Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("userId", userId);
        parameters.put("currentTime", System.currentTimeMillis());
        return new SelectOneDescriptor<>("getNumberOfPendingOverdueTasksForUser", parameters, SHumanTaskInstance.class,
                Long.class);
    }

    public static SelectListDescriptor<Long> deleteMessageInstanceByIds(List<Long> ids, final int fromIndex,
            final int maxResults) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("ids", ids);

        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        return new SelectListDescriptor<>("deleteMessageInstanceByIds", parameters,
                SMessageEventCouple.class, queryOptions);
    }

    public static SelectListDescriptor<Long> getMessageInstanceIdOlderThanCreationDate(long creationDate,
            QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("creationDate", creationDate);
        return new SelectListDescriptor<>("getMessageInstanceIdOlderThanCreationDate", parameters,
                SMessageInstance.class, queryOptions);
    }

}
