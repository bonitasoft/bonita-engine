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
package org.bonitasoft.engine.core.process.instance.impl;

import static java.util.Collections.singletonMap;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstanceStateCounter;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAManualTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;

/**
 * @author Elias Ricken de Medeiros
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public abstract class FlowNodeInstancesServiceImpl implements FlowNodeInstanceService {

    private static final String SUPERVISED_BY = "SupervisedBy";

    private final SUserTaskInstanceBuilderFactory activityInstanceKeyProvider;

    private final EventService eventService;

    private final Recorder recorder;

    private final PersistenceService persistenceService;

    private final ArchiveService archiveService;

    private final TechnicalLoggerService logger;

    public FlowNodeInstancesServiceImpl(final Recorder recorder, final PersistenceService persistenceService,
            final EventService eventService,
            final TechnicalLoggerService logger, final ArchiveService archiveService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.logger = logger;
        activityInstanceKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        this.eventService = eventService;
        this.archiveService = archiveService;
    }

    public ArchiveService getArchiveService() {
        return archiveService;
    }

    @Override
    public void setState(final SFlowNodeInstance flowNodeInstance, final FlowNodeState state)
            throws SFlowNodeModificationException {
        final long now = System.currentTimeMillis();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getPreviousStateIdKey(), flowNodeInstance.getStateId());
        descriptor.addField(activityInstanceKeyProvider.getStateIdKey(), state.getId());
        descriptor.addField(activityInstanceKeyProvider.getStateNameKey(), state.getName());
        descriptor.addField(activityInstanceKeyProvider.getStableKey(), state.isStable());
        descriptor.addField(activityInstanceKeyProvider.getTerminalKey(), state.isTerminal());
        descriptor.addField(activityInstanceKeyProvider.getReachStateDateKey(), now);
        descriptor.addField(activityInstanceKeyProvider.getLastUpdateDateKey(), now);
        descriptor.addField(activityInstanceKeyProvider.getStateExecutingKey(), false);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(
                    getClass(),
                    TechnicalLogSeverity.DEBUG,
                    MessageFormat.format("[{0} with id {1}] changed state {2}->{3}(new={4})",
                            flowNodeInstance.getClass().getSimpleName(),
                            flowNodeInstance.getId(), flowNodeInstance.getStateId(), state.getId(),
                            state.getClass().getSimpleName()));
        }

        updateOneField(flowNodeInstance, ACTIVITYINSTANCE_STATE, descriptor);
    }

    @Override
    public void setExecuting(final SFlowNodeInstance flowNodeInstance) throws SFlowNodeModificationException {
        final long now = System.currentTimeMillis();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getStateExecutingKey(), true);
        descriptor.addField(activityInstanceKeyProvider.getLastUpdateDateKey(), now);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(
                    getClass(),
                    TechnicalLogSeverity.DEBUG,
                    MessageFormat.format("[{0} with id {1}] have executing flag set to true",
                            flowNodeInstance.getClass().getSimpleName(),
                            flowNodeInstance.getId()));
        }

        updateOneField(flowNodeInstance, ACTIVITYINSTANCE_STATE, descriptor);
    }

    @Override
    public void updateDisplayName(final SFlowNodeInstance flowNodeInstance, final String displayName)
            throws SFlowNodeModificationException {
        if (displayName != null && !displayName.equals(flowNodeInstance.getDisplayName())) {
            final String key = activityInstanceKeyProvider.getDisplayNameKey();
            final String event = ACTIVITYINSTANCE_DISPLAY_NAME;
            updateOneField(flowNodeInstance, key, displayName, 255, event);
        }
    }

    private String getTruncated(final String value, final int maxLengh, final SFlowNodeInstance flowNodeInstance,
            final String key) {
        if (value.length() > maxLengh) {
            final String truncatedValue = value.substring(0, maxLengh);
            logTruncationWarning(value, truncatedValue, maxLengh, flowNodeInstance, key);
            return truncatedValue;
        }
        return value;
    }

    private void logTruncationWarning(final String value, final String truncatedValue, final int maxLengh,
            final SFlowNodeInstance flowNodeInstance,
            final String key) {
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
            final StringBuilder stb = new StringBuilder();
            stb.append("The field ");
            stb.append(key);
            stb.append(" is too long in the flow node instance [id: ");
            stb.append(flowNodeInstance.getId());
            stb.append(", name: ");
            stb.append(flowNodeInstance.getName());
            stb.append(", process instance id: ");
            stb.append(flowNodeInstance.getParentProcessInstanceId());
            stb.append(", root process instance id: ");
            stb.append(flowNodeInstance.getRootProcessInstanceId());
            stb.append("] and will be truncated to the max lengh (");
            stb.append(maxLengh);
            stb.append("). The truncated value is: '");
            stb.append(truncatedValue);
            stb.append("'. The original value was: '");
            stb.append(value);
            stb.append("'.");
            final String message = stb.toString();
            logger.log(getClass(), TechnicalLogSeverity.WARNING, message);
        }
    }

    private void updateOneField(final SFlowNodeInstance flowNodeInstance, final String attributeKey,
            final String attributeValue, final int maxLength,
            final String event) throws SFlowNodeModificationException {
        final String truncatedValue = getTruncated(attributeValue, maxLength, flowNodeInstance, attributeKey);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(attributeKey, truncatedValue);

        updateOneField(flowNodeInstance, event, descriptor);
    }

    private void updateOneField(final SFlowNodeInstance flowNodeInstance, final String attributeKey,
            final Long attributeValue, final String event)
            throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(attributeKey, attributeValue);
        updateOneField(flowNodeInstance, event, descriptor);
    }

    private void updateOneField(SFlowNodeInstance flowNodeInstance, String event, EntityUpdateDescriptor descriptor)
            throws SFlowNodeModificationException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(flowNodeInstance, descriptor), event);
        } catch (final SRecorderException e) {
            throw new SFlowNodeModificationException(e);
        }
    }

    @Override
    public void updateDisplayDescription(final SFlowNodeInstance flowNodeInstance, final String displayDescription)
            throws SFlowNodeModificationException {
        if (displayDescription != null && !displayDescription.equals(flowNodeInstance.getDisplayDescription())) {
            final String event = ACTIVITYINSTANCE_DISPLAY_DESCRIPTION;
            final String key = activityInstanceKeyProvider.getDisplayDescriptionKey();
            updateOneField(flowNodeInstance, key, displayDescription, 255, event);
        }
    }

    @Override
    public void setTaskPriority(final SFlowNodeInstance flowNodeInstance, final STaskPriority priority)
            throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getPriorityKey(), priority);
        updateOneField(flowNodeInstance, ACTIVITYINSTANCE_STATE, descriptor);
    }

    @Override
    public SFlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId)
            throws SFlowNodeNotFoundException, SFlowNodeReadException {
        SFlowNodeInstance selectOne;
        try {
            selectOne = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SFlowNodeInstance.class,
                    "SFlowNodeInstance", flowNodeInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        if (selectOne == null) {
            throw new SFlowNodeNotFoundException(flowNodeInstanceId);
        }
        return selectOne;
    }

    @Override
    public List<SFlowNodeInstance> getAllChildrenOfProcessInstance(final long parentProcessInstanceId,
            final int fromIndex, final int maxResults) throws SBonitaReadException {
        return getUnmodifiableList(
                getPersistenceService().selectList(new SelectListDescriptor<>("getAllChildrenOfProcessInstance",
                        singletonMap("parentProcessInstanceId", parentProcessInstanceId),
                        SFlowNodeInstance.class, new QueryOptions(fromIndex, maxResults))));
    }

    @Override
    public List<SFlowNodeInstance> getDirectChildrenOfProcessInstance(final long parentProcessInstanceId,
            final int fromIndex, final int maxResults) throws SBonitaReadException {
        return getUnmodifiableList(getPersistenceService().selectList(new SelectListDescriptor<>(
                "getDirectChildrenOfProcessInstance",
                singletonMap("parentProcessInstanceId", parentProcessInstanceId),
                SFlowNodeInstance.class, new QueryOptions(fromIndex, maxResults))));
    }

    @Override
    public List<SFlowNodeInstance> getDirectChildrenOfActivityInstance(long parentActivityInstanceId, int fromIndex,
            int maxResults) throws SBonitaReadException {
        List<SFlowNodeInstance> selectList;
        final Map<String, Object> parameters = singletonMap("parentActivityInstanceId",
                parentActivityInstanceId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults);
        selectList = getPersistenceService()
                .selectList(new SelectListDescriptor<>("getDirectChildrenOfActivityInstance",
                        parameters, SFlowNodeInstance.class, queryOptions));
        return getUnmodifiableList(selectList);
    }

    @Override
    public List<SAFlowNodeInstance> getArchivedFlowNodeInstances(final long rootContainerId, final int fromIndex,
            final int maxResults)
            throws SFlowNodeReadException {
        List<SAFlowNodeInstance> selectList;
        try {
            selectList = getPersistenceService().selectList(
                    SelectDescriptorBuilder.getArchivedFlowNodesFromProcessInstance(rootContainerId, fromIndex,
                            maxResults));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        return getUnmodifiableList(selectList);
    }

    @Override
    public Set<Long> getSourceObjectIdsOfArchivedFlowNodeInstances(List<Long> sourceProcessInstanceIds)
            throws SBonitaReadException {
        return new HashSet<>(getPersistenceService()
                .selectList(new SelectListDescriptor<>("getSourceObjectIdsOfArchivedFlowNodeInstances",
                        singletonMap("sourceProcessInstanceIds", sourceProcessInstanceIds),
                        SAFlowNodeInstance.class, QueryOptions.countQueryOptions())));
    }

    @Override
    public void deleteArchivedFlowNodeInstances(List<Long> sourceObjectIds) throws SBonitaException {
        archiveService.deleteFromQuery("deleteArchivedFlowNodeInstances",
                singletonMap("sourceObjectIds", sourceObjectIds));
    }

    @Override
    public SAFlowNodeInstance getArchivedFlowNodeInstance(final long archivedFlowNodeInstanceId)
            throws SFlowNodeReadException, SFlowNodeNotFoundException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        SAFlowNodeInstance selectOne;
        try {
            selectOne = persistenceService.selectById(
                    SelectDescriptorBuilder.getElementById(SAFlowNodeInstance.class, "SArchivedFlowNodeInstance",
                            archivedFlowNodeInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SFlowNodeReadException(e);
        }
        if (selectOne == null) {
            throw new SFlowNodeNotFoundException(archivedFlowNodeInstanceId);
        }
        return selectOne;
    }

    @Override
    public <T extends SAFlowNodeInstance> T getLastArchivedFlowNodeInstance(final Class<T> entityClass,
            final long sourceObjectFlowNodeInstanceId)
            throws SBonitaReadException {
        final SAManualTaskInstanceBuilderFactory builderFactory = BuilderFactory
                .get(SAManualTaskInstanceBuilderFactory.class);
        final FilterOption filterOption = new FilterOption(entityClass, builderFactory.getSourceObjectIdKey(),
                sourceObjectFlowNodeInstanceId);
        final List<OrderByOption> orderByOptions = new ArrayList<>();
        orderByOptions.add(new OrderByOption(entityClass, builderFactory.getArchivedDateKey(), OrderByType.DESC));
        orderByOptions.add(new OrderByOption(entityClass, builderFactory.getLastUpdateKey(), OrderByType.DESC));
        final QueryOptions queryOptions = new QueryOptions(0, 1, orderByOptions,
                Collections.singletonList(filterOption), null);
        final List<T> saFlowNodeInstances = searchArchivedFlowNodeInstances(entityClass, queryOptions);
        if (!saFlowNodeInstances.isEmpty()) {
            return saFlowNodeInstances.get(0);
        }
        return null;
    }

    @Override
    public void setStateCategory(final SFlowNodeInstance flowElementInstance, final SStateCategory stateCategory)
            throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getStateCategoryKey(), stateCategory);

        try {
            getRecorder().recordUpdate(UpdateRecord.buildSetFields(flowElementInstance, descriptor), STATE_CATEGORY);
        } catch (final SRecorderException sre) {
            throw new SFlowNodeModificationException(sre);
        }

    }

    @Override
    public void setExecutedBy(final SFlowNodeInstance flowNodeInstance, final long userId)
            throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getExecutedBy(), userId);
        updateFlowNode(flowNodeInstance, EXECUTED_BY_MODIFIED, descriptor);
    }

    @Override
    public void setExecutedBySubstitute(final SFlowNodeInstance flowNodeInstance, final long executerSubstituteId)
            throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getExecutedBySubstitute(), executerSubstituteId);
        updateFlowNode(flowNodeInstance, EXECUTED_BY_SUBSTITUTE_MODIFIED, descriptor);
    }

    @Override
    public void setExpectedEndDate(final SFlowNodeInstance flowNodeInstance, final Long dueDate)
            throws SFlowNodeModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(activityInstanceKeyProvider.getExpectedEndDateKey(), dueDate);
        updateFlowNode(flowNodeInstance, EXPECTED_END_DATE_MODIFIED, descriptor);
    }

    protected void updateFlowNode(final SFlowNodeInstance flowNodeInstance, final String eventName,
            final EntityUpdateDescriptor descriptor)
            throws SFlowNodeModificationException {
        try {
            getRecorder().recordUpdate(UpdateRecord.buildSetFields(flowNodeInstance, descriptor), eventName);
        } catch (final SRecorderException sre) {
            throw new SFlowNodeModificationException(sre);
        }
    }

    protected <T> List<T> getUnmodifiableList(List<T> selectList) {
        if (selectList == null) {
            selectList = new ArrayList<>();
        }
        return Collections.unmodifiableList(selectList);
    }

    @Override
    public long getNumberOfFlowNodeInstances(final Class<? extends SFlowNodeInstance> entityClass,
            final QueryOptions countOptions)
            throws SBonitaReadException {
        return getPersistenceService().getNumberOfEntities(entityClass, countOptions, null);
    }

    @Override
    public <T extends SFlowNodeInstance> List<T> searchFlowNodeInstances(final Class<T> entityClass,
            final QueryOptions searchOptions)
            throws SBonitaReadException {
        return getPersistenceService().searchEntity(entityClass, searchOptions, null);
    }

    @Override
    public long getNumberOfFlowNodeInstancesSupervisedBy(final Long supervisorId,
            final Class<? extends SFlowNodeInstance> entityClass,
            final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = singletonMap("supervisorId", (Object) supervisorId);
        return getPersistenceService().getNumberOfEntities(entityClass, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public <T extends SFlowNodeInstance> List<T> searchFlowNodeInstancesSupervisedBy(final Long supervisorId,
            final Class<T> entityClass,
            final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = singletonMap("supervisorId", (Object) supervisorId);
        return getPersistenceService().searchEntity(entityClass, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public List<SFlowNodeInstanceStateCounter> getNumberOfFlownodesInAllStates(final long parentProcessInstanceId)
            throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<>(2);
        parameters.put("parentProcessInstanceId", parentProcessInstanceId);
        final List<SFlowNodeInstanceStateCounter> result = persistenceService.selectList(new SelectListDescriptor<>(
                "getNumberOfFlowNodesInAllStates", parameters, SFlowNodeInstance.class,
                new QueryOptions(0, Integer.MAX_VALUE)));
        if (result != null && result.size() > 0) {
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public List<SFlowNodeInstanceStateCounter> getNumberOfArchivedFlownodesInAllStates(
            final long parentProcessInstanceId) throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<>(2);
        parameters.put("parentProcessInstanceId", parentProcessInstanceId);
        final List<SFlowNodeInstanceStateCounter> result = persistenceService.selectList(new SelectListDescriptor<>(
                "getNumberOfArchivedFlowNodesInAllStates", parameters, SAFlowNodeInstance.class,
                new QueryOptions(0, Integer.MAX_VALUE)));
        if (result != null && result.size() > 0) {
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public long getNumberOfArchivedFlowNodeInstances(final Class<? extends SAFlowNodeInstance> entityClass,
            final QueryOptions countOptions)
            throws SBonitaReadException {
        return getPersistenceService().getNumberOfEntities(entityClass, countOptions, null);
    }

    @Override
    public <T extends SAFlowNodeInstance> List<T> searchArchivedFlowNodeInstances(final Class<T> entityClass,
            final QueryOptions searchOptions)
            throws SBonitaReadException {
        return getPersistenceService().searchEntity(entityClass, searchOptions, null);
    }

    @Override
    public long getNumberOfArchivedFlowNodeInstancesSupervisedBy(final long supervisorId,
            final Class<? extends SAFlowNodeInstance> entityClass,
            final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = singletonMap("supervisorId", (Object) supervisorId);
        return getPersistenceService().getNumberOfEntities(entityClass, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public <T extends SAFlowNodeInstance> List<T> searchArchivedFlowNodeInstancesSupervisedBy(final long supervisorId,
            final Class<T> entityClass,
            final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = singletonMap("supervisorId", (Object) supervisorId);
        return getPersistenceService().searchEntity(entityClass, SUPERVISED_BY, queryOptions, parameters);
    }

    protected Recorder getRecorder() {
        return recorder;
    }

    protected PersistenceService getPersistenceService() {
        return persistenceService;
    }

    protected TechnicalLoggerService getLogger() {
        return logger;
    }

    @Override
    public void deleteFlowNodeInstance(final SFlowNodeInstance sFlowNodeInstance) throws SFlowNodeDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(sFlowNodeInstance), FLOWNODE_INSTANCE);
        } catch (final SBonitaException e) {
            throw new SFlowNodeDeletionException(e);
        }
    }

    @Override
    public List<Long> getFlowNodeInstanceIdsToRecover(Duration considerElementsOlderThan, QueryOptions queryOptions)
            throws SBonitaReadException {
        final List<Long> selectList = getPersistenceService().selectList(
                new SelectListDescriptor<>("getFlowNodeInstanceIdsToRecover",
                        singletonMap("maxLastUpdate",
                                System.currentTimeMillis() - considerElementsOlderThan.toMillis()),
                        SFlowNodeInstance.class,
                        queryOptions));
        return getUnmodifiableList(selectList);
    }

    @Override
    public List<Long> getGatewayInstanceIdsToRecover(Duration considerElementsOlderThan,
            final QueryOptions queryOptions) throws SBonitaReadException {
        final List<Long> selectList = getPersistenceService().selectList(
                new SelectListDescriptor<>("getGatewayInstanceIdsToRecover",
                        singletonMap("maxLastUpdate",
                                System.currentTimeMillis() - considerElementsOlderThan.toMillis()),
                        SGatewayInstance.class,
                        queryOptions));
        return getUnmodifiableList(selectList);
    }

    @Override
    public List<SFlowNodeInstance> getFlowNodeInstancesByIds(List<Long> ids) throws SBonitaReadException {
        return getUnmodifiableList(getPersistenceService().selectList(
                new SelectListDescriptor<>("getFlowNodeInstancesByIds", singletonMap("ids", ids),
                        SFlowNodeInstance.class, QueryOptions.ALL_RESULTS)));
    }

    @Override
    public int getNumberOfFlowNodes(final long parentProcessInstanceId) throws SBonitaReadException {
        return getPersistenceService().selectOne(SelectDescriptorBuilder.getNumberOfFlowNode(parentProcessInstanceId))
                .intValue();
    }

    @Override
    public List<SFlowNodeInstance> getFlowNodeInstancesByNameAndParentContainerId(String name, Long parentContainerId)
            throws SBonitaReadException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("parentContainerId", parentContainerId);
        return getPersistenceService().selectList(
                new SelectListDescriptor<>("getFlowNodeInstancesByNameAndParentContainerId", parameters,
                        SFlowNodeInstance.class, QueryOptions.ALL_RESULTS));
    }
}
