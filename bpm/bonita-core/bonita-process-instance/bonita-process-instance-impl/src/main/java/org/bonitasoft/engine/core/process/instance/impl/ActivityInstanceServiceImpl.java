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
package org.bonitasoft.engine.core.process.instance.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceDeletionException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SPendingActivityMappingBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Emmanuel Duchastenier
 * @author Yanyan Liu
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ActivityInstanceServiceImpl extends FlowNodeInstancesServiceImpl implements ActivityInstanceService {

    private static final String ASSIGNED_AND_PENDING_BY_ROOT_PROCESS = "AssignedAndPendingByRootProcess";

    private static final String ASSIGNED_AND_PENDING_BY_ROOT_PROCESS_FOR = "AssignedAndPendingByRootProcessFor";

    private static final String SUPERVISED_BY = "SupervisedBy";

    private static final String MANAGED_BY = "ManagedBy";

    private static final String PENDING_MANAGED_BY = "PendingManagedBy";

    private static final String PENDING_SUPERVISED_BY = "PendingSupervisedBy";

    private static final String PENDING_FOR_USER = "PendingForUser";

    private static final String PENDING_OR_ASSIGNED = "PendingOrAssigned";

    private static final String HUMAN_TASK_INSTANCE_ASSIGNEE = "HUMAN_TASK_INSTANCE_ASSIGNEE";

    private static final String WHOCANSTART_PENDING_TASK_SUFFIX = "WhoCanStartPendingTask";

    private static final int BATCH_SIZE = 100;

    private final SUserTaskInstanceBuilderFactory sUserTaskInstanceBuilder;

    private final SMultiInstanceActivityInstanceBuilderFactory sMultiInstanceActivityInstanceBuilder;

    private final DataInstanceService dataInstanceService;

    private final ConnectorInstanceService connectorInstanceService;

    public ActivityInstanceServiceImpl(final Recorder recorder, final PersistenceService persistenceService, final ArchiveService archiveService,
            final DataInstanceService dataInstanceService, final ConnectorInstanceService connectorInstanceService, final EventService eventService,
            final TechnicalLoggerService logger) {
        super(recorder, persistenceService, eventService, logger, archiveService);
        this.dataInstanceService = dataInstanceService;
        this.connectorInstanceService = connectorInstanceService;
        sUserTaskInstanceBuilder = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        sMultiInstanceActivityInstanceBuilder = BuilderFactory.get(SMultiInstanceActivityInstanceBuilderFactory.class);
    }

    @Override
    public void createActivityInstance(final SActivityInstance activityInstance) throws SActivityCreationException {
        try {
            final InsertRecord insertRecord = new InsertRecord(activityInstance);
            SInsertEvent insertEvent = null;
            if (getEventService().hasHandlers(ACTIVITYINSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(ACTIVITYINSTANCE).setObject(activityInstance)
                        .done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SActivityCreationException(e);
        }
        if (getLogger().isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("Created ");
            stb.append(activityInstance.getType().getValue());
            stb.append(" <");
            stb.append(activityInstance.getName());
            stb.append("> with id = <");
            stb.append(activityInstance.getId());
            if (activityInstance.getParentActivityInstanceId() > 0) {
                stb.append(">, parent activity instance id = <");
                stb.append(activityInstance.getParentActivityInstanceId());
            }
            stb.append(">, parent process instance id = <");
            stb.append(activityInstance.getParentProcessInstanceId());
            stb.append(">, root process instance id = <");
            stb.append(activityInstance.getRootProcessInstanceId());
            stb.append(">, process definition id = <");
            stb.append(activityInstance.getProcessDefinitionId());
            stb.append(">");
            getLogger().log(this.getClass(), TechnicalLogSeverity.DEBUG, stb.toString());
        }
    }

    @Override
    public void addPendingActivityMappings(final SPendingActivityMapping mapping) throws SActivityCreationException {
        final InsertRecord insertRecord = new InsertRecord(mapping);
        final EventService eventService = getEventService();
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(PENDINGACTIVITYMAPPING, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(PENDINGACTIVITYMAPPING).setObject(mapping).done();
        }
        try {
            getRecorder().recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SActivityCreationException(e);
        }
    }

    @Override
    public void deletePendingMappings(final long humanTaskInstanceId) throws SActivityModificationException {
        try {
            List<SPendingActivityMapping> mappings = null;
            final boolean createEvents = getEventService().hasHandlers(PENDINGACTIVITYMAPPING, EventActionType.DELETED);
            final QueryOptions queryOptions = new QueryOptions(0, BATCH_SIZE, SPendingActivityMapping.class, "id", OrderByType.ASC);
            while (!(mappings = getPendingMappings(humanTaskInstanceId, queryOptions)).isEmpty()) {
                deletePendingMappings(mappings, createEvents);
            }
        } catch (final SBonitaException e) {
            throw new SActivityModificationException(e);
        }
    }

    private void deletePendingMappings(final List<SPendingActivityMapping> mappings, final boolean createEvents) throws SRecorderException {
        for (final SPendingActivityMapping mapping : mappings) {
            deletePendingMapping(mapping, createEvents);
        }
    }

    private void deletePendingMapping(final SPendingActivityMapping mapping, final boolean createEvents) throws SRecorderException {
        SDeleteEvent deleteEvent = null;
        if (createEvents) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PENDINGACTIVITYMAPPING)
                    .setObject(mapping).done();
        }
        getRecorder().recordDelete(new DeleteRecord(mapping), deleteEvent);
    }

    @Override
    public void deleteAllPendingMappings() throws SActivityModificationException {
        try {
            final FilterOption filterOption = new FilterOption(SPendingActivityMapping.class, SPendingActivityMappingBuilderFactory.ACTOR_ID, -1);
            final DeleteAllRecord record = new DeleteAllRecord(SPendingActivityMapping.class, Collections.singletonList(filterOption));
            getRecorder().recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SActivityModificationException("Can't delete all pending mappings not attached to an actor.", e);
        }
    }

    /**
     * @param humanTaskInstanceId
     * @param queryOptions
     * @return
     */
    @Override
    public List<SPendingActivityMapping> getPendingMappings(final long humanTaskInstanceId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("activityId", humanTaskInstanceId);
        return getPersistenceService().selectList(
                new SelectListDescriptor<SPendingActivityMapping>("getPendingMappingsOfTask", parameters, SPendingActivityMapping.class, queryOptions));
    }

    @Override
    public SActivityInstance getActivityInstance(final long activityInstanceId) throws SActivityInstanceNotFoundException, SActivityReadException {
        try {
        final SActivityInstance activity = getPersistenceService().selectById(
                SelectDescriptorBuilder.getElementById(SActivityInstance.class, "SActivityInstance", activityInstanceId));
        if (activity == null) {
            throw new SActivityInstanceNotFoundException(activityInstanceId);
        }
        return activity;
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
    }
    }

    @Override
    public SHumanTaskInstance getHumanTaskInstance(final long activityInstanceId) throws SActivityInstanceNotFoundException, SActivityReadException {
        final SelectByIdDescriptor<SHumanTaskInstance> descriptor = SelectDescriptorBuilder.getElementById(SHumanTaskInstance.class, "SHumanTaskInstance",
                activityInstanceId);
        SHumanTaskInstance humanTask;
        try {
            humanTask = getPersistenceService().selectById(descriptor);
        if (humanTask == null) {
            throw new SActivityInstanceNotFoundException(activityInstanceId);
        }
        return humanTask;
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e.getMessage());
    }

    }

    @Override
    public List<SActivityInstance> getActivitiesWithStates(final long rootContainerId, final Set<Integer> stateIds, final int fromIndex, final int maxResults,
            final String sortingField, final OrderByType sortingOrder) throws SActivityReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("rootContainerId", rootContainerId);
        parameters.put("stateIds", stateIds);
        final SelectListDescriptor<SActivityInstance> elements = SelectDescriptorBuilder.getSpecificQueryWithParameters(SActivityInstance.class,
                "getActivitiesWithStates", parameters, new QueryOptions(fromIndex, maxResults, SActivityInstance.class, sortingField, sortingOrder));
        try {
        return getPersistenceService().selectList(elements);
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
        }
    }

    @Override
    public List<SActivityInstance> getOpenActivityInstances(final long rootContainerId, final int pageIndex, final int maxResults, final String sortingField,
            final OrderByType orderbyType) throws SActivityReadException {
        final Map<String, Object> parameters = Collections.singletonMap("rootContainerId", (Object) rootContainerId);
        final QueryOptions queryOptions = new QueryOptions(pageIndex * maxResults, maxResults, SActivityInstance.class, sortingField, orderbyType);
        final SelectListDescriptor<SActivityInstance> elements = SelectDescriptorBuilder.getSpecificQueryWithParameters(SActivityInstance.class,
                "getOpenActivitiesFromProcessInstance", parameters, queryOptions);
        try {
        return getPersistenceService().selectList(elements);
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
        }
    }

    @Override
    public SAActivityInstance getMostRecentArchivedActivityInstance(final long activityInstanceId) throws SActivityReadException,
            SActivityInstanceNotFoundException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        final SelectOneDescriptor<SAActivityInstance> descriptor = SelectDescriptorBuilder.getMostRecentArchivedActivityInstance(activityInstanceId);
        try {
        final SAActivityInstance activity = persistenceService.selectOne(descriptor);
        if (activity == null) {
            throw new SActivityInstanceNotFoundException(activityInstanceId);
        }
        return activity;
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
        }
    }

    @Override
    public List<SAActivityInstance> getArchivedActivityInstances(final long rootContainerId, final QueryOptions queryOptions) throws SActivityReadException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        try {
        final List<SAActivityInstance> activities = persistenceService.selectList(SelectDescriptorBuilder.getArchivedActivitiesFromProcessInstance(
                rootContainerId, queryOptions));
        return getUnmodifiableList(activities);
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
    }
    }

    @Override
    public List<SHumanTaskInstance> getPendingTasks(final long userId, final Set<Long> actorIds, final int fromIndex, final int maxResults,
            final String sortFieldName, final OrderByType order) throws SActivityReadException {
        try {
        final SelectListDescriptor<SHumanTaskInstance> selectListDescriptor;
        if (actorIds.isEmpty()) {
            selectListDescriptor = SelectDescriptorBuilder.getPendingUserTasks(userId, fromIndex, maxResults, sortFieldName, order);
        } else {
            selectListDescriptor = SelectDescriptorBuilder.getPendingUserTasks(userId, actorIds, fromIndex, maxResults, sortFieldName, order);
        }
        return getPersistenceService().selectList(selectListDescriptor);
        } catch (final SBonitaReadException bre) {
            throw new SActivityReadException(bre);
    }
    }

    @Override
    public List<SHumanTaskInstance> getAssignedUserTasks(final long assigneeId, final int fromIndex, final int maxResults, final String sortFieldName,
            final OrderByType order) throws SActivityReadException {
        try {
        final SelectListDescriptor<SHumanTaskInstance> selectListDescriptor = SelectDescriptorBuilder.getAssignedUserTasks(assigneeId, fromIndex,
                maxResults, sortFieldName, order);
        return getPersistenceService().selectList(selectListDescriptor);
        } catch (final SBonitaReadException bre) {
            throw new SActivityReadException(bre);
    }
    }

    @Override
    public int getNumberOfOpenActivityInstances(final long rootContainerId) throws SActivityReadException {
        try {
        return getPersistenceService().selectOne(SelectDescriptorBuilder.getNumberOfOpenActivities(rootContainerId)).intValue();
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
    }
    }

    @Override
    public List<SActivityInstance> getActivityInstances(final long rootContainerId, final int fromIndex, final int numberOfResults)
            throws SActivityReadException {
        final SelectListDescriptor<SActivityInstance> descriptor = SelectDescriptorBuilder.getActivitiesFromProcessInstance(rootContainerId, fromIndex,
                numberOfResults);
        try {
        final List<SActivityInstance> selectList = getPersistenceService().selectList(descriptor);
        return getUnmodifiableList(selectList);
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
    }
    }

    @Override
    public int getNumberOfActivityInstances(final long rootContainerId) throws SActivityReadException {
        try {
        return getPersistenceService().selectOne(SelectDescriptorBuilder.getNumberOfActivitiesFromProcessInstance(rootContainerId)).intValue();
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
    }
    }

    @Override
    public void assignHumanTask(final long userTaskId, final long userId) throws SFlowNodeNotFoundException, SFlowNodeReadException,
            SActivityModificationException {
        final SFlowNodeInstance flowNodeInstance = getFlowNodeInstance(userTaskId);
        if (flowNodeInstance instanceof SHumanTaskInstance) {
            final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
            descriptor.addField(sUserTaskInstanceBuilder.getAssigneeIdKey(), userId);
            if (userId > 0) {
                // if this action is a Assign action:
                descriptor.addField(sUserTaskInstanceBuilder.getClaimedDateKey(), System.currentTimeMillis());
            } else {
                // if this action is a Release action:
                descriptor.addField(sUserTaskInstanceBuilder.getClaimedDateKey(), 0);
            }

            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(flowNodeInstance, descriptor);

            SUpdateEvent updateEvent = null;
            if (getEventService().hasHandlers(HUMAN_TASK_INSTANCE_ASSIGNEE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(HUMAN_TASK_INSTANCE_ASSIGNEE)
                        .setObject(flowNodeInstance).done();
            }
            try {
                getRecorder().recordUpdate(updateRecord, updateEvent);
            } catch (final SRecorderException e) {
                throw new SActivityModificationException(e);
            }
        } else {
            throw new SActivityReadException("the activity with id " + userTaskId + " is not a user task");
        }
    }

    @Override
    public long getNumberOfAssignedHumanTaskInstances(final long userId) throws SActivityReadException {
        try {
        return getPersistenceService().selectOne(SelectDescriptorBuilder.getNumberOfAssignedHumanTaskInstances(userId));
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
    }
    }

    @Override
    public SAActivityInstance getArchivedActivityInstance(final long activityInstanceId, final int stateId) throws SActivityReadException,
            SActivityInstanceNotFoundException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        SAActivityInstance selectOne;
        try {
            selectOne = persistenceService.selectOne(SelectDescriptorBuilder.getArchivedActivityInstanceWithActivityIdAndStateId(activityInstanceId, stateId));
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
        }
        if (selectOne == null) {
            throw new SActivityInstanceNotFoundException(activityInstanceId, stateId);
        }
        return selectOne;
    }

    @Override
    public long getNumberOfArchivedTasksManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return persistenceService.getNumberOfEntities(SAHumanTaskInstance.class, MANAGED_BY, searchOptions, parameters);
    }

    @Override
    public List<SAHumanTaskInstance> searchArchivedTasksManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return persistenceService.searchEntity(SAHumanTaskInstance.class, MANAGED_BY, searchOptions, parameters);
    }

    @Override
    public long getNumberOfArchivedHumanTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
        return getPersistenceService().getNumberOfEntities(SAHumanTaskInstance.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public long getNumberOfAssignedTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
        queryOptions.getFilters().add(new FilterOption(SHumanTaskInstance.class, "assigneeId", 0, FilterOperationType.GREATER));
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchAssignedTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
        queryOptions.getFilters().add(new FilterOption(SHumanTaskInstance.class, "assigneeId", 0, FilterOperationType.GREATER));
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public long getNumberOfHumanTasks(final QueryOptions queryOptions) throws SBonitaReadException {
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, queryOptions, null);
    }

    @Override
    public List<SHumanTaskInstance> searchHumanTasks(final QueryOptions queryOptions) throws SBonitaReadException {
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, queryOptions, null);
    }

    @Override
    public List<SAHumanTaskInstance> searchArchivedHumanTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
        return getPersistenceService().searchEntity(SAHumanTaskInstance.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public List<SAHumanTaskInstance> searchArchivedTasks(final QueryOptions searchOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        return persistenceService.searchEntity(SAHumanTaskInstance.class, searchOptions, null);
    }

    @Override
    public long getNumberOfArchivedTasks(final QueryOptions searchOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        return persistenceService.getNumberOfEntities(SAHumanTaskInstance.class, searchOptions, null);
    }

    @Override
    public long getNumberOfAssignedTasksManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, MANAGED_BY, searchOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchAssignedTasksManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, MANAGED_BY, searchOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchPendingTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) supervisorId);
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, PENDING_SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public long getNumberOfPendingTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) supervisorId);
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, PENDING_SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public Map<Long, Long> getNumberOfOpenTasksForUsers(final List<Long> userIds) throws SBonitaReadException {
        if (userIds == null || userIds.size() == 0) {
            return Collections.emptyMap();
        }
        // get assigned tasks for each user
        final List<Map<String, Long>> result = getPersistenceService().selectList(SelectDescriptorBuilder.getNumbersOfAssignedOpenTasks(userIds));
        final Map<Long, Long> userTaskNumbermap = new HashMap<Long, Long>();
        for (final Map<String, Long> record : result) {
            userTaskNumbermap.put(record.get("userId"), record.get("numberOfTasks")); // "userId" and "numberOfTasks" are embed in mybatis/hibernate query
                                                                                      // statements named "getNumbersOfOpenTasksForUsers"
        }
        // get number of pending tasks for each user
        for (final Long userId : userIds) {
            final long pendingCount = getNumberOfPendingTasksForUser(userId, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS));
            if (!userTaskNumbermap.containsKey(userId)) {
                userTaskNumbermap.put(userId, pendingCount);
            } else {
                userTaskNumbermap.put(userId, userTaskNumbermap.get(userId) + pendingCount);
            }
        }
        return userTaskNumbermap;
    }

    @Override
    public long searchNumberOfPendingTasksManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, PENDING_MANAGED_BY, searchOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchPendingTasksManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, PENDING_MANAGED_BY, searchOptions, parameters);
    }

    // FIXME synchronized on the object
    @Override
    public void incrementLoopCounter(final SLoopActivityInstance loopInstance) throws SActivityModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField("loopCounter", loopInstance.getLoopCounter() + 1);

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(loopInstance, descriptor);
        SUpdateEvent updateEvent = null;
        if (getEventService().hasHandlers(ACTIVITYINSTANCE_STATE, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ACTIVITYINSTANCE_STATE).setObject(loopInstance)
                    .done();
        }
        try {
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException sre) {
            throw new SActivityModificationException(sre);
        }
    }

    @Override
    public Map<Long, Long> getNumberOfOverdueOpenTasksForUsers(final List<Long> userIds) throws SBonitaReadException {
        if (userIds == null || userIds.size() == 0) {
            return Collections.emptyMap();
        }
        // get assigned overdue open tasks for each user
        final List<Map<Long, Long>> result = getPersistenceService().selectList(SelectDescriptorBuilder.getNumbersOfAssignedOverdueOpenTasks(userIds));
        final Map<Long, Long> userTaskNumbermap = new HashMap<Long, Long>();
        for (final Map<Long, Long> record : result) {
            userTaskNumbermap.put(record.get("userId"), record.get("numberOfTasks")); // "userId" and "numberOfTasks" are embed in mybatis/hibernate query
                                                                                      // statements named "getNumbersOfOpenTasksForUsers"
        }
        // get number of pending overdue open tasks for each user
        for (final Long userId : userIds) {
            final long pendingCount = getPersistenceService().selectOne(SelectDescriptorBuilder.getNumberOfPendingOverdueOpenTasksForUser(userId));
            if (!userTaskNumbermap.containsKey(userId)) {
                userTaskNumbermap.put(userId, pendingCount);
            } else {
                userTaskNumbermap.put(userId, userTaskNumbermap.get(userId) + pendingCount);
            }
        }
        return userTaskNumbermap;
    }

    @Override
    public List<SActivityInstance> getChildrenOfAnActivity(final long parentActivityInstanceId, final int fromIndex, final int numberOfResults)
            throws SActivityReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("parentActivityInstanceId", parentActivityInstanceId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfResults, SFlowNodeInstance.class, "id", OrderByType.ASC);
        final SelectListDescriptor<SActivityInstance> descriptor = new SelectListDescriptor<SActivityInstance>("getChildrenOfAnActivity", parameters,
                SActivityInstance.class, queryOptions);
        try {
        return getPersistenceService().selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
        }
    }

    @Override
    public void setLoopMax(final SLoopActivityInstance loopActivity, final Integer loopMap) throws SActivityModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField("loopMax", loopMap);
        try {
        updateFlowNode(loopActivity, LOOPINSTANCE_LOOPMAX_MODIFIED, descriptor);
        } catch (final SFlowNodeModificationException e) {
            throw new SActivityModificationException(e);
        }
    }

    @Override
    public void setLoopCardinality(final SFlowNodeInstance flowNodeInstance, final int intLoopCardinality) throws SActivityModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(sMultiInstanceActivityInstanceBuilder.getLoopCardinalityKey(), intLoopCardinality);
        try {
        updateFlowNode(flowNodeInstance, MULTIINSTANCE_LOOPCARDINALITY_MODIFIED, descriptor);
        } catch (final SFlowNodeModificationException e) {
            throw new SActivityModificationException(e);
        }
    }

    @Override
    public void addMultiInstanceNumberOfActiveActivities(final SMultiInstanceActivityInstance flowNodeInstance, final int number)
            throws SActivityModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(sMultiInstanceActivityInstanceBuilder.getNumberOfActiveInstancesKey(), flowNodeInstance.getNumberOfActiveInstances() + number);
        try {
        updateFlowNode(flowNodeInstance, MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED, descriptor);
        } catch (final SFlowNodeModificationException e) {
            throw new SActivityModificationException(e);
        }
    }

    @Override
    public void addMultiInstanceNumberOfTerminatedActivities(final SMultiInstanceActivityInstance flowNodeInstance, final int number)
            throws SActivityModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(sMultiInstanceActivityInstanceBuilder.getNumberOfActiveInstancesKey(), flowNodeInstance.getNumberOfActiveInstances() - number);
        descriptor.addField(sMultiInstanceActivityInstanceBuilder.getNumberOfTerminatedInstancesKey(), flowNodeInstance.getNumberOfTerminatedInstances()
                + number);
        try {
        updateFlowNode(flowNodeInstance, MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED, descriptor);
        } catch (final SFlowNodeModificationException e) {
            throw new SActivityModificationException(e);
        }
    }

    @Override
    public void addMultiInstanceNumberOfCompletedActivities(final SMultiInstanceActivityInstance flowNodeInstance, final int number)
            throws SActivityModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(sMultiInstanceActivityInstanceBuilder.getNumberOfActiveInstancesKey(), flowNodeInstance.getNumberOfActiveInstances() - number);
        descriptor
                .addField(sMultiInstanceActivityInstanceBuilder.getNumberOfCompletedInstancesKey(), flowNodeInstance.getNumberOfCompletedInstances() + number);
        try {
        updateFlowNode(flowNodeInstance, MULTIINSTANCE_NUMBEROFINSTANCE_MODIFIED, descriptor);
        } catch (final SFlowNodeModificationException e) {
            throw new SActivityModificationException(e);
        }
    }

    @Override
    public long getNumberOfActivityInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaReadException {
        return getPersistenceService().getNumberOfEntities(entityClass, searchOptions, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SActivityInstance> searchActivityInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaReadException {
        return (List<SActivityInstance>) getPersistenceService().searchEntity(entityClass, searchOptions, null);
    }

    @Override
    public long getNumberOfArchivedActivityInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaReadException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        return persistenceService.getNumberOfEntities(entityClass, searchOptions, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SAActivityInstance> searchArchivedActivityInstances(final Class<? extends PersistentObject> entityClass, final QueryOptions searchOptions)
            throws SBonitaReadException {
        final ReadPersistenceService persistenceService = getArchiveService().getDefinitiveArchiveReadPersistenceService();
        return (List<SAActivityInstance>) persistenceService.searchEntity(entityClass, searchOptions, null);
    }

    @Override
    public void setTokenCount(final SActivityInstance activityInstance, final int tokenCount) throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(sUserTaskInstanceBuilder.getTokenCountKey(), tokenCount);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(activityInstance, descriptor);

        SUpdateEvent updateEvent = null;
        if (getEventService().hasHandlers(ACTIVITY_INSTANCE_TOKEN_COUNT, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(ACTIVITY_INSTANCE_TOKEN_COUNT)
                    .setObject(activityInstance).done();
        }
        try {
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SFlowNodeModificationException(e);
        }
    }

    @Override
    public long getNumberOfPendingTasksForUser(final long userId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, PENDING_FOR_USER, searchOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchPendingTasksForUser(final long userId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, PENDING_FOR_USER, searchOptions, parameters);
    }

    @Override
    public long getNumberOfPendingOrAssignedTasks(final long userId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, PENDING_OR_ASSIGNED, searchOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchPendingOrAssignedTasks(final long userId, final QueryOptions searchOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, PENDING_OR_ASSIGNED, searchOptions, parameters);
    }

    @Override
    public void deleteArchivedPendingMappings(final long flowNodeInstanceId) {
        // FIXME : archived pending mapping... first we need to have archived pending mapping and so on
        // final SEventBuilder eventBuilder = BuilderFactory.get(SEventBuilderFactory.class);
        // List<SPendingActivityMapping> mappings = null;
        // try {
        // while ((mappings = getPendingMappings(flowNodeInstanceId, new QueryOptions(0, BATCH_SIZE))).size() > 0) {
        // for (final SPendingActivityMapping mapping : mappings) {
        // final SPendingActivityMappingLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new pending activity mapping", mapping);
        // final SDeleteEvent deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(PENDINGACTIVITYMAPPING).setObject(mapping).done();
        // try {
        // getRecorder().recordDelete(new DeleteRecord(mapping), deleteEvent);
        // initiateLogBuilder(flowNodeInstanceId, SQueriableLog.STATUS_OK, logBuilder, "deletePendingMappings");
        // } catch (final SRecorderException e) {
        // initiateLogBuilder(flowNodeInstanceId, SQueriableLog.STATUS_FAIL, logBuilder, "deletePendingMappings");
        // throw new SActivityModificationException(e);
        // }
        // }
        // }
        // } catch (final SActivityReadException e) {
        // throw new SActivityModificationException(e);
        // }
    }

    @Override
    public void setAbortedByBoundaryEvent(final SActivityInstance activityInstance, final long boundaryEventId) throws SActivityModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(sUserTaskInstanceBuilder.getAbortedByBoundaryEventIdKey(), boundaryEventId);

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(activityInstance, descriptor);
        final SUpdateEvent updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(STATE_CATEGORY)
                .setObject(activityInstance).done();
        try {
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException sre) {
            throw new SActivityModificationException(sre);
        }
    }

    @Override
    public List<Long> getPossibleUserIdsOfPendingTasks(final long humanTaskInstanceId, final int startIndex, final int maxResults)
            throws SActivityReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("humanTaskInstanceId", humanTaskInstanceId);
        final QueryOptions queryOptions = new QueryOptions(startIndex, maxResults);
        final SelectListDescriptor<Long> elements = new SelectListDescriptor<Long>("getPossibleUserIdsOfPendingTasks", parameters, SActivityInstance.class,
                queryOptions);
        try {
        return getPersistenceService().selectList(elements);
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
        }
    }

    @Override
    public boolean isTaskPendingForUser(final long humanTaskInstanceId, final long userId) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("humanTaskInstanceId", humanTaskInstanceId);
        parameters.put("userId", userId);
        final SelectOneDescriptor<Long> elements = new SelectOneDescriptor<Long>("isTaskPendingForUser", parameters, SActivityInstance.class);
        Long aLong = getPersistenceService().selectOne(elements);
        return aLong == 1;
    }

    @Override
    public long getNumberOfArchivedActivityInstancesSupervisedBy(final long supervisorId, final Class<? extends SAActivityInstance> entityClass,
            final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return getPersistenceService().getNumberOfEntities(entityClass, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SAActivityInstance> searchArchivedActivityInstancesSupervisedBy(final long supervisorId, final Class<? extends SAActivityInstance> entityClass,
            final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return (List<SAActivityInstance>) getPersistenceService().searchEntity(entityClass, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public long getNumberOfUsersWhoCanExecutePendingHumanTaskDeploymentInfo(final long humanTaskInstanceId, final QueryOptions searchOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("humanTaskInstanceId", (Object) humanTaskInstanceId);
        return getPersistenceService().getNumberOfEntities(SUser.class, WHOCANSTART_PENDING_TASK_SUFFIX, searchOptions, parameters);
    }

    @Override
    public List<SUser> searchUsersWhoCanExecutePendingHumanTaskDeploymentInfo(final long humanTaskInstanceId, final QueryOptions searchOptions)
            throws SBonitaReadException {
        try {
        final Map<String, Object> parameters = Collections.singletonMap("humanTaskInstanceId", (Object) humanTaskInstanceId);
        return getPersistenceService().searchEntity(SUser.class, WHOCANSTART_PENDING_TASK_SUFFIX, searchOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaReadException(bre);
    }
    }

    @Override
    public long getNumberOfAssignedAndPendingHumanTasksFor(final long rootProcessDefinitionId, final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("rootProcessDefinitionId", rootProcessDefinitionId);
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, ASSIGNED_AND_PENDING_BY_ROOT_PROCESS_FOR, queryOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchAssignedAndPendingHumanTasksFor(final long rootProcessDefinitionId, final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("rootProcessDefinitionId", rootProcessDefinitionId);
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, ASSIGNED_AND_PENDING_BY_ROOT_PROCESS_FOR, queryOptions, parameters);
    }

    @Override
    public long getNumberOfAssignedAndPendingHumanTasks(final long rootProcessDefinitionId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId", (Object) rootProcessDefinitionId);
        return getPersistenceService().getNumberOfEntities(SHumanTaskInstance.class, ASSIGNED_AND_PENDING_BY_ROOT_PROCESS, queryOptions, parameters);
    }

    @Override
    public List<SHumanTaskInstance> searchAssignedAndPendingHumanTasks(final long rootProcessDefinitionId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("rootProcessDefinitionId", (Object) rootProcessDefinitionId);
        return getPersistenceService().searchEntity(SHumanTaskInstance.class, ASSIGNED_AND_PENDING_BY_ROOT_PROCESS, queryOptions, parameters);
    }

    @Override
    public void deleteArchivedFlowNodeInstances(final long processInstanceId) throws SFlowNodeDeletionException {
        try {
            deleteArchivedFlowNodeInstancesAndElements(processInstanceId);
        } catch (final SFlowNodeDeletionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SFlowNodeDeletionException(e);
        }
    }

    private void deleteArchivedFlowNodeInstancesAndElements(final long processInstanceId) throws SFlowNodeReadException, SBonitaReadException,
            SConnectorInstanceDeletionException, SDataInstanceException, SFlowNodeDeletionException {
        Set<Long> sourceActivityIds = new HashSet<Long>();
        List<SAFlowNodeInstance> saFlowNodeInstances;
        do {
            saFlowNodeInstances = getArchivedFlowNodeInstances(processInstanceId, 0, BATCH_SIZE);
            sourceActivityIds = deleteArchivedFlowNodeInstancesAndElements(sourceActivityIds, saFlowNodeInstances);
        } while (!saFlowNodeInstances.isEmpty());
    }

    private Set<Long> deleteArchivedFlowNodeInstancesAndElements(final Set<Long> sourceActivityIds, final List<SAFlowNodeInstance> saFlowNodeInstances)
            throws SBonitaReadException, SConnectorInstanceDeletionException, SDataInstanceException, SFlowNodeDeletionException {
        Set<Long> newSourceActivityIds = new HashSet<Long>(sourceActivityIds);
        for (final SAFlowNodeInstance saFlowNodeInstance : saFlowNodeInstances) {
            newSourceActivityIds = deleteArchivedFlowNodeInstanceAndElements(newSourceActivityIds, saFlowNodeInstance);
        }
        return newSourceActivityIds;
    }

    private Set<Long> deleteArchivedFlowNodeInstanceAndElements(final Set<Long> sourceActivityIds, final SAFlowNodeInstance saFlowNodeInstance)
            throws SBonitaReadException, SConnectorInstanceDeletionException, SDataInstanceException, SFlowNodeDeletionException {
        final Set<Long> newSourceActivityIds = new HashSet<Long>(sourceActivityIds);
        if (saFlowNodeInstance instanceof SAActivityInstance && !sourceActivityIds.contains(saFlowNodeInstance.getSourceObjectId())) {
            newSourceActivityIds.add(saFlowNodeInstance.getSourceObjectId());
            deleteArchivedFlowNodeInstanceElements((SAActivityInstance) saFlowNodeInstance);
        }
        deleteArchivedFlowNodeInstance(saFlowNodeInstance);
        return newSourceActivityIds;
    }

    private void deleteArchivedFlowNodeInstanceElements(final SAActivityInstance saActivityInstance) throws SBonitaReadException,
            SConnectorInstanceDeletionException, SDataInstanceException {
        dataInstanceService.deleteLocalArchivedDataInstances(saActivityInstance.getSourceObjectId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString());
        connectorInstanceService.deleteArchivedConnectorInstances(saActivityInstance.getSourceObjectId(), SConnectorInstance.FLOWNODE_TYPE);
        if (SFlowNodeType.USER_TASK.equals(saActivityInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(saActivityInstance.getType())) {
            deleteArchivedPendingMappings(saActivityInstance.getSourceObjectId());
        }
    }

    @Override
    public QueryOptions buildQueryOptionsForSubActivitiesInNormalStateAndNotTerminal(final long parentActivityInstanceId, final int numberOfResults) {
        final SUserTaskInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);

        final List<FilterOption> filters = new ArrayList<FilterOption>(3);
        filters.add(new FilterOption(SActivityInstance.class, flowNodeKeyProvider.getParentActivityInstanceKey(), parentActivityInstanceId));
        filters.add(new FilterOption(SActivityInstance.class, flowNodeKeyProvider.getTerminalKey(), false));
        filters.add(new FilterOption(SActivityInstance.class, flowNodeKeyProvider.getStateCategoryKey(), SStateCategory.NORMAL.name()));

        final OrderByOption orderByOption = new OrderByOption(SActivityInstance.class, flowNodeKeyProvider.getNameKey(), OrderByType.ASC);

        return new QueryOptions(0, numberOfResults, Collections.singletonList(orderByOption), filters, null);
    }

    @Override
    public SUserTaskInstance getUserTaskInstance(final long userTaskInstanceId) throws SActivityInstanceNotFoundException, SActivityReadException {
        return getInstance(userTaskInstanceId, SUserTaskInstance.class);
    }

    private <T extends SActivityInstance> T getInstance(final long instanceId, final Class<T> instanceClass)
            throws SActivityInstanceNotFoundException, SActivityReadException {
        final SelectByIdDescriptor<T> descriptor = SelectDescriptorBuilder.getElementById(instanceClass, instanceClass.getSimpleName(),
                instanceId);
        try {
            final T instance = getPersistenceService().selectById(descriptor);
            if (instance == null) {
                throw new SActivityInstanceNotFoundException(instanceId);
            }
            return instance;
        } catch (final SBonitaReadException e) {
            throw new SActivityReadException(e);
        }
    }
}
