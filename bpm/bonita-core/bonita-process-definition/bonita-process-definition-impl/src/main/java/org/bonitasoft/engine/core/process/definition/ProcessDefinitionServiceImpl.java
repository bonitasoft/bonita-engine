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
package org.bonitasoft.engine.core.process.definition;

import org.bonitasoft.engine.bpm.bar.ProcessDefinitionBARContribution;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.impl.internal.ExpressionFinder;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.bonitasoft.engine.core.process.definition.exception.SDeletingEnabledProcessException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDeletionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDeploymentInfoUpdateException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDisablementException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessEnablementException;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionLogBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionLogBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.impl.SProcessDefinitionDeployInfoBuilderFactoryImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDeployInfoImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDesignContentImpl;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.io.xml.XMLParseException;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Zhao Na
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 * @author Arthur Freycon
 */
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private final EventService eventService;
    private final SessionService sessionService;
    private final ReadSessionAccessor sessionAccessor;
    private final QueriableLoggerService queriableLoggerService;
    private final DependencyService dependencyService;
    private final CacheService cacheService;
    protected ProcessDefinitionBARContribution processDefinitionBARContribution;

    public ProcessDefinitionServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final EventService eventService, final SessionService sessionService, final ReadSessionAccessor sessionAccessor,
            final QueriableLoggerService queriableLoggerService, final DependencyService dependencyService, CacheService cacheService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.eventService = eventService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.queriableLoggerService = queriableLoggerService;
        this.dependencyService = dependencyService;
        this.cacheService = cacheService;
        processDefinitionBARContribution = new ProcessDefinitionBARContribution();
    }

    @Override
    public void delete(final long processId) throws SProcessDefinitionNotFoundException, SProcessDeletionException, SDeletingEnabledProcessException {
        final SProcessDefinitionLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a Process definition");
        final SProcessDefinitionDeployInfo processDefinitionDeployInfo;
        try {
            processDefinitionDeployInfo = getProcessDeploymentInfo(processId);
            if (ActivationState.ENABLED.name().equals(processDefinitionDeployInfo.getActivationState())) {
                throw new SDeletingEnabledProcessException("Process is enabled.", processDefinitionDeployInfo);
            }
        } catch (final SBonitaReadException e) {
            log(processId, SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new SProcessDeletionException(e, processId);
        }

        try {
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(PROCESSDEFINITION, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PROCESSDEFINITION)
                        .setObject(processDefinitionDeployInfo).done();
            }
            final DeleteRecord deleteRecord = new DeleteRecord(processDefinitionDeployInfo);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(processId, SQueriableLog.STATUS_OK, logBuilder, "delete");
            dependencyService.deleteDependencies(processId, ScopeType.PROCESS);
        } catch (final SRecorderException | SDependencyException e) {
            log(processId, SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new SProcessDeletionException(e, processDefinitionDeployInfo);
        }
    }

    @Override
    public void disableProcessDeploymentInfo(final long processId) throws SProcessDefinitionNotFoundException, SProcessDisablementException {
        SProcessDefinitionDeployInfo processDefinitionDeployInfo;
        try {
            processDefinitionDeployInfo = getProcessDeploymentInfo(processId);
        } catch (final SBonitaReadException e) {
            throw new SProcessDisablementException(e);
        }
        if (ActivationState.DISABLED.name().equals(processDefinitionDeployInfo.getActivationState())) {
            throw new SProcessDisablementException("Process " + processDefinitionDeployInfo.getName() + " with version "
                    + processDefinitionDeployInfo.getVersion() + " is already disabled");
        }

        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getActivationStateKey(), ActivationState.DISABLED.name());

        final UpdateRecord updateRecord = getUpdateRecord(descriptor, processDefinitionDeployInfo);
        final SPersistenceLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "Disabling the process");

        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(PROCESSDEFINITION_IS_DISABLED, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(PROCESSDEFINITION_IS_DISABLED)
                    .setObject(processDefinitionDeployInfo).done();
        }
        try {
            update(processId, processDefinitionDeployInfo, updateRecord, updateEvent);
            log(processDefinitionDeployInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, "disableProcess");
        } catch (final SRecorderException | SCacheException e) {
            log(processDefinitionDeployInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "disableProcess");
            throw new SProcessDisablementException(e);
        }
    }

    void updateSProcessDefinitionTimestampInCache(long processId, SProcessDefinitionDeployInfo processDefinitionDeployInfo) throws SCacheException {
        final Pair<Long, SProcessDefinition> fromCache = getSProcessDefinitionFromCache(processId);
        if (fromCache != null) {
            storeProcessDefinitionInCache(fromCache.getValue(), processDefinitionDeployInfo.getLastUpdateDate());
        }
    }

    @Override
    public void enableProcessDeploymentInfo(final long processId) throws SProcessDefinitionNotFoundException, SProcessEnablementException {
        SProcessDefinitionDeployInfo processDefinitionDeployInfo;
        try {
            processDefinitionDeployInfo = getProcessDeploymentInfo(processId);
        } catch (final SBonitaReadException e) {
            throw new SProcessEnablementException(e);
        }
        if (ActivationState.ENABLED.name().equals(processDefinitionDeployInfo.getActivationState())) {
            throw new SProcessEnablementException("Process " + processDefinitionDeployInfo.getName() + " with version "
                    + processDefinitionDeployInfo.getVersion() + " is already enabled");
        }
        if (ConfigurationState.UNRESOLVED.name().equals(processDefinitionDeployInfo.getConfigurationState())) {
            throw new SProcessEnablementException("Process " + processDefinitionDeployInfo.getName() + " with version "
                    + processDefinitionDeployInfo.getVersion() + " can't be enabled since all dependencies are not resolved yet");
        }
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getActivationStateKey(), ActivationState.ENABLED.name());

        final UpdateRecord updateRecord = getUpdateRecord(descriptor, processDefinitionDeployInfo);
        final SPersistenceLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "Enabling the process");
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(PROCESSDEFINITION_IS_ENABLED, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(PROCESSDEFINITION_IS_ENABLED)
                    .setObject(processDefinitionDeployInfo).done();
        }
        try {
            update(processId, processDefinitionDeployInfo, updateRecord, updateEvent);
            log(processId, SQueriableLog.STATUS_OK, logBuilder, "enableProcess");
        } catch (final SRecorderException | SCacheException e) {
            log(processId, SQueriableLog.STATUS_FAIL, logBuilder, "enableProcess");
            throw new SProcessEnablementException(e);
        }
    }

    private SProcessDefinitionLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SProcessDefinitionLogBuilder logBuilder = BuilderFactory.get(SProcessDefinitionLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfos(final int fromIndex, final int numberPerPage, final String field,
            final OrderByType order) throws SBonitaReadException {
            final Map<String, Object> emptyMap = Collections.emptyMap();
            return persistenceService.selectList(new SelectListDescriptor<SProcessDefinitionDeployInfo>("", emptyMap, SProcessDefinitionDeployInfo.class,
                    new QueryOptions(fromIndex, numberPerPage, SProcessDefinitionDeployInfo.class, field, order)));
        }

    @Override
    public long getNumberOfProcessDeploymentInfos() throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.emptyMap();
        final SelectOneDescriptor<Long> selectDescriptor = new SelectOneDescriptor<>("getNumberOfProcessDefinitions", parameters,
                SProcessDefinitionDeployInfo.class);
            return persistenceService.selectOne(selectDescriptor);
        }

    @Override
    public SProcessDefinition getProcessDefinition(final long processId) throws SProcessDefinitionNotFoundException, SBonitaReadException {
        try {
            //get from database
            final SProcessDefinitionDeployInfo processDeploymentInfo = getProcessDeploymentInfo(processId);
            //get from cache
            final Pair<Long, SProcessDefinition> processWithTimestamp = getSProcessDefinitionFromCache(processId);
            //read SProcessDefinition if needed
            if (isSProcessDefinitionUpToDate(processDeploymentInfo, processWithTimestamp)) {
                return readSProcessDefinitionFromDatabase(processId, processDeploymentInfo);
            } else {
                return processWithTimestamp.getValue();
            }
        } catch (XMLParseException | IOException | SReflectException | SCacheException e) {
            throw new SBonitaReadException(e);
        }
    }

    SProcessDefinition readSProcessDefinitionFromDatabase(long processId, SProcessDefinitionDeployInfo processDeploymentInfo) throws IOException, XMLParseException, SReflectException, SCacheException {
        System.out.println(processDeploymentInfo.getDesignContent()
                        .getContent());
        final DesignProcessDefinition objectFromXML = processDefinitionBARContribution.convertXmlToProcess(processDeploymentInfo.getDesignContent()
                        .getContent());
        SProcessDefinition sProcessDefinition = convertDesignProcessDefinition(objectFromXML);
                setIdOnProcessDefinition(sProcessDefinition, processId);
        storeProcessDefinitionInCache(sProcessDefinition, processDeploymentInfo.getLastUpdateDate());
            return sProcessDefinition;
        }

    boolean isSProcessDefinitionUpToDate(SProcessDefinitionDeployInfo processDeploymentInfo, Pair<Long, SProcessDefinition> processWithTimestamp) {
        return processWithTimestamp == null || processWithTimestamp.getKey() != processDeploymentInfo.getLastUpdateDate();
    }

    @SuppressWarnings("unchecked")
    Pair<Long, SProcessDefinition> getSProcessDefinitionFromCache(long processId) throws SCacheException {
        return (Pair<Long, SProcessDefinition>) cacheService.get(PROCESS_CACHE_NAME, processId);
    }

    @Override
    public SProcessDefinition getProcessDefinitionIfIsEnabled(final long processDefinitionId) throws SBonitaReadException,
            SProcessDefinitionException {
        final SProcessDefinitionDeployInfo deployInfo = getProcessDeploymentInfo(processDefinitionId);
        if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
            throw new SProcessDefinitionException("The process definition is not enabled !!", deployInfo.getProcessId(), deployInfo.getName(),
                    deployInfo.getVersion());
        }
        return getProcessDefinition(processDefinitionId);
    }

    private void setIdOnProcessDefinition(final SProcessDefinition sProcessDefinition, long id) throws SReflectException {
        ClassReflector.invokeSetter(sProcessDefinition, "setId", Long.class, id);
    }

    protected long generateId() {
        return Math.abs(UUID.randomUUID().getLeastSignificantBits());
    }

    @Override
    public SProcessDefinitionDeployInfo getProcessDeploymentInfo(final long processId) throws SProcessDefinitionNotFoundException,
            SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap("processId", (Object) processId);
            final SelectOneDescriptor<SProcessDefinitionDeployInfo> descriptor = new SelectOneDescriptor<>(
                    "getDeployInfoByProcessDefId", parameters, SProcessDefinitionDeployInfo.class);
            final SProcessDefinitionDeployInfo processDefinitionDeployInfo = persistenceService.selectOne(descriptor);
            if (processDefinitionDeployInfo == null) {
                throw new SProcessDefinitionNotFoundException("Unable to find the process definition deployment info.", processId);
            }
            return processDefinitionDeployInfo;
        }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    @Override
    public SProcessDefinition store(final DesignProcessDefinition designProcessDefinition) throws SProcessDefinitionException {
        NullCheckingUtil.checkArgsNotNull(designProcessDefinition);

        // create the runtime process definition
        final SProcessDefinition definition = convertDesignProcessDefinition(designProcessDefinition);

        final SProcessDefinitionLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new Process definition");
        try {
            final String processDefinitionContent = getProcessContent(designProcessDefinition);
            final SProcessDefinitionDesignContentImpl sProcessDefinitionDesignContent = new SProcessDefinitionDesignContentImpl();
            sProcessDefinitionDesignContent.setContent(processDefinitionContent);

            final InsertRecord insertRecordForContent = new InsertRecord(sProcessDefinitionDesignContent);
            recorder.recordInsert(insertRecordForContent, null);

            final long processId = generateId();
            setIdOnProcessDefinition(definition, processId);
            final SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo = createProcessDefinitionDeployInfo(designProcessDefinition, definition,
                    sProcessDefinitionDesignContent, processId);

            final InsertRecord record = new InsertRecord(sProcessDefinitionDeployInfo);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(PROCESSDEFINITION, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(PROCESSDEFINITION)
                        .setObject(sProcessDefinitionDeployInfo).done();
            }
            recorder.recordInsert(record, insertEvent);
            storeProcessDefinitionInCache(definition, sProcessDefinitionDeployInfo.getLastUpdateDate());
            log(definition.getId(), SQueriableLog.STATUS_OK, logBuilder, "store");
        } catch (final Exception e) {
            log(definition.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "store");
            throw new SProcessDefinitionException(e);
        }
        return definition;
    }

    SProcessDefinitionDeployInfoImpl createProcessDefinitionDeployInfo(DesignProcessDefinition designProcessDefinition, SProcessDefinition definition,
            SProcessDefinitionDesignContentImpl sProcessDefinitionDesignContent, long processId) {
            String displayName = designProcessDefinition.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = definition.getName();
            }
            String displayDescription = designProcessDefinition.getDisplayDescription();
            if (displayDescription == null || displayDescription.isEmpty()) {
                displayDescription = definition.getDescription();
            }
            final SProcessDefinitionDeployInfoImpl sProcessDefinitionDeployInfo = new SProcessDefinitionDeployInfoImpl();
            sProcessDefinitionDeployInfo.setName(definition.getName());
            sProcessDefinitionDeployInfo.setVersion(definition.getVersion());
            sProcessDefinitionDeployInfo.setProcessId(processId);
            sProcessDefinitionDeployInfo.setDescription(definition.getDescription());
            sProcessDefinitionDeployInfo.setDeployedBy(getUserId());
            sProcessDefinitionDeployInfo.setDeploymentDate(System.currentTimeMillis());
            sProcessDefinitionDeployInfo.setActivationState(ActivationState.DISABLED.name());
            sProcessDefinitionDeployInfo.setConfigurationState(ConfigurationState.UNRESOLVED.name());
            sProcessDefinitionDeployInfo.setDisplayName(displayName);
            sProcessDefinitionDeployInfo.setDisplayDescription(displayDescription);
            sProcessDefinitionDeployInfo.setDesignContent(sProcessDefinitionDesignContent);
        return sProcessDefinitionDeployInfo;
    }

    void storeProcessDefinitionInCache(SProcessDefinition definition, Long lastUpdateDate) throws SCacheException {
        cacheService.store(PROCESS_CACHE_NAME, definition.getId(), Pair.of(lastUpdateDate, definition));
    }

    String getProcessContent(DesignProcessDefinition designProcessDefinition) throws IOException {
        return processDefinitionBARContribution.convertProcessToXml(designProcessDefinition);
    }

    SProcessDefinition convertDesignProcessDefinition(DesignProcessDefinition designProcessDefinition) {
        return BuilderFactory.get(SProcessDefinitionBuilderFactory.class).createNewInstance(designProcessDefinition).done();
    }

    private long getUserId() {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    @Override
    public void resolveProcess(final long processId) throws SProcessDefinitionNotFoundException, SProcessDisablementException {
        SProcessDefinitionDeployInfo processDefinitionDeployInfo;
        try {
            processDefinitionDeployInfo = getProcessDeploymentInfo(processId);
        } catch (final SBonitaReadException e) {
            throw new SProcessDisablementException(e);
        }
        if (!ConfigurationState.UNRESOLVED.name().equals(processDefinitionDeployInfo.getConfigurationState())) {
            throw new SProcessDisablementException("Process " + processDefinitionDeployInfo.getName() + " with version"
                    + processDefinitionDeployInfo.getVersion() + " is not unresolved");
        }

        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor
                .addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getConfigurationStateKey(), ConfigurationState.RESOLVED.name());

        final UpdateRecord updateRecord = getUpdateRecord(descriptor, processDefinitionDeployInfo);
        final SPersistenceLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "Resolved the process");

        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(PROCESSDEFINITION_IS_RESOLVED, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(PROCESSDEFINITION_IS_RESOLVED)
                    .setObject(processDefinitionDeployInfo).done();
        }
        try {
            update(processId, processDefinitionDeployInfo, updateRecord, updateEvent);
            log(processDefinitionDeployInfo.getId(), SQueriableLog.STATUS_OK, logBuilder, "resolveProcess");
        } catch (final SRecorderException | SCacheException e) {
            log(processDefinitionDeployInfo.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "resolveProcess");
            throw new SProcessDisablementException(e);
        }
    }

    @Override
    public long getNumberOfProcessDeploymentInfosByActivationState(final ActivationState activationState) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("activationState", (Object) activationState.name());
        final SelectOneDescriptor<Long> selectDescriptor = new SelectOneDescriptor<>("getNumberOfProcessDefinitionsInActivationState", parameters,
                SProcessDefinitionDeployInfo.class);
            return persistenceService.selectOne(selectDescriptor);
    }

    @Override
    public List<Long> getProcessDefinitionIds(final ActivationState activationState, final int fromIndex, final int numberOfResults)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("activationState", (Object) activationState.name());
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SProcessDefinitionDeployInfo.class, "id", OrderByType.ASC));
        final SelectListDescriptor<Long> selectDescriptor = new SelectListDescriptor<>("getProcessDefinitionsIdsInActivationState", parameters,
                SProcessDefinitionDeployInfo.class, new QueryOptions(fromIndex, numberOfResults, orderByOptions));
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<Long> getProcessDefinitionIds(final int fromIndex, final int numberOfResults) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.emptyMap();
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SProcessDefinitionDeployInfo.class, "id", OrderByType.ASC));
        final SelectListDescriptor<Long> selectDescriptor = new SelectListDescriptor<>("getProcessDefinitionsIds", parameters,
                SProcessDefinitionDeployInfo.class, new QueryOptions(fromIndex, numberOfResults, orderByOptions));
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public SFlowNodeDefinition getNextFlowNode(final SProcessDefinition definition, final String source) {
        final SFlowElementContainerDefinition processContainer = definition.getProcessContainer();
        final STransitionDefinition sourceNode = processContainer.getTransition(source);
        final long targetId = sourceNode.getTarget();
        return processContainer.getFlowNode(targetId);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfos(final List<Long> processIds, final int fromIndex, final int numberOfProcesses,
            final String field, final OrderByType order) throws SBonitaReadException {
        if (processIds == null || processIds.size() == 0) {
            return Collections.emptyList();
        }
            final Map<String, Object> emptyMap = Collections.singletonMap("processIds", (Object) processIds);
            final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfProcesses, SProcessDefinitionDeployInfo.class, field, order);
            return persistenceService.selectList(new SelectListDescriptor<SProcessDefinitionDeployInfo>(
                    "getSubSetOfProcessDefinitionDeployInfos", emptyMap, SProcessDefinitionDeployInfo.class, queryOptions));
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfos(final List<Long> processIds) throws SBonitaReadException {
        if (processIds == null || processIds.size() == 0) {
            return Collections.emptyList();
        }
            final QueryOptions queryOptions = new QueryOptions(0, processIds.size(), SProcessDefinitionDeployInfo.class, "name", OrderByType.ASC);
            final Map<String, Object> emptyMap = Collections.singletonMap("processIds", (Object) processIds);
            return persistenceService.selectList(new SelectListDescriptor<SProcessDefinitionDeployInfo>("getSubSetOfProcessDefinitionDeployInfos", emptyMap,
                    SProcessDefinitionDeployInfo.class, queryOptions));
    }

    @Override
    public long getLatestProcessDefinitionId(final String processName) throws SBonitaReadException, SProcessDefinitionNotFoundException {
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = getProcessDeploymentInfosOrderByTimeDesc(processName, 0, 1);
        if (sProcessDefinitionDeployInfos.isEmpty()) {
            throw new SProcessDefinitionNotFoundException(processName);
        }
        return sProcessDefinitionDeployInfos.get(0).getProcessId();
    }

    private List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosOrderByTimeDesc(final String processName, final int startIndex, final int maxResults)
            throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(startIndex, maxResults, SProcessDefinitionDeployInfo.class, "deploymentDate", OrderByType.DESC);
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) processName);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "getProcessDefinitionDeployInfosByName", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public long getProcessDefinitionId(final String name, final String version) throws SBonitaReadException, SProcessDefinitionNotFoundException {
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("name", name);
            parameters.put("version", version);
            final Long processDefId = persistenceService.selectOne(new SelectOneDescriptor<>("getProcessDefinitionIdByNameAndVersion", parameters,
                    SProcessDefinitionDeployInfo.class, Long.class));
            if (processDefId == null) {
                final SProcessDefinitionNotFoundException exception = new SProcessDefinitionNotFoundException("Process definition id not found.");
                exception.setProcessDefinitionNameOnContext(name);
                exception.setProcessDefinitionVersionOnContext(version);
                throw exception;
            }
            return processDefId;
    }

    @Override
    public SProcessDefinitionDeployInfo updateProcessDefinitionDeployInfo(final long processId, final EntityUpdateDescriptor descriptor)
            throws SProcessDefinitionNotFoundException, SProcessDeploymentInfoUpdateException {
        final String logMessage = "Updating a processDefinitionDeployInfo";
        return updateProcessDefinitionDeployInfo(processId, descriptor, logMessage);
    }

    SProcessDefinitionDeployInfo updateProcessDefinitionDeployInfo(long processId, EntityUpdateDescriptor descriptor, String logMessage) throws SProcessDefinitionNotFoundException, SProcessDeploymentInfoUpdateException {
        SProcessDefinitionDeployInfo processDefinitionDeployInfo;
        try {
            processDefinitionDeployInfo = getProcessDeploymentInfo(processId);
        } catch (final SBonitaReadException e) {
            throw new SProcessDefinitionNotFoundException(e, processId);
        }
        final SProcessDefinitionLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, truncate(logMessage));
        final UpdateRecord updateRecord = getUpdateRecord(descriptor, processDefinitionDeployInfo);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(PROCESSDEFINITION_DEPLOY_INFO, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(PROCESSDEFINITION_DEPLOY_INFO)
                    .setObject(processDefinitionDeployInfo).done();
        }
        try {
            update(processId, processDefinitionDeployInfo, updateRecord, updateEvent);
            log(processId, SQueriableLog.STATUS_OK, logBuilder, "updateProcessDeploymentInfo");
        } catch (final SRecorderException | SCacheException e) {
            log(processId, SQueriableLog.STATUS_FAIL, logBuilder, "updateProcessDeploymentInfo");
            throw new SProcessDeploymentInfoUpdateException(e);
        }
        return processDefinitionDeployInfo;
    }

    void update(long processId, SProcessDefinitionDeployInfo processDefinitionDeployInfo, UpdateRecord updateRecord, SUpdateEvent updateEvent)
            throws SRecorderException, SCacheException {
        recorder.recordUpdate(updateRecord, updateEvent);
        if (!updateRecord.getFields().containsKey(SProcessDefinitionDeployInfoBuilderFactoryImpl.DESIGN_CONTENT)) {
            updateSProcessDefinitionTimestampInCache(processId, processDefinitionDeployInfo);
        }
    }

    private UpdateRecord getUpdateRecord(final EntityUpdateDescriptor descriptor, final SProcessDefinitionDeployInfo processDefinitionDeployInfo) {
        final long now = System.currentTimeMillis();
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getLastUpdateDateKey(), now);
        return UpdateRecord.buildSetFields(processDefinitionDeployInfo, descriptor);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosStartedBy(final long startedBy, final QueryOptions searchOptions)
            throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap("startedBy", (Object) startedBy);
            return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, STARTED_BY_SUFFIX, searchOptions, parameters);
    }

    @Override
    public long getNumberOfProcessDeploymentInfosStartedBy(final long startedBy, final QueryOptions countOptions) throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap("startedBy", (Object) startedBy);
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, STARTED_BY_SUFFIX, countOptions, parameters);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfos(final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, searchOptions, null);
    }

    @Override
    public long getNumberOfProcessDeploymentInfos(final QueryOptions countOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, countOptions, null);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosCanBeStartedBy(final long userId, final QueryOptions searchOptions)
            throws SBonitaReadException {
            return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, "UserCanStart", searchOptions,
                    Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public long getNumberOfProcessDeploymentInfosCanBeStartedBy(final long userId, final QueryOptions countOptions) throws SBonitaReadException {
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, "UserCanStart", countOptions,
                    Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(final long managerUserId,
            final QueryOptions searchOptions)
            throws SBonitaReadException {
            return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, "UsersManagedByCanStart", searchOptions,
                    Collections.singletonMap("managerUserId", (Object) managerUserId));
    }

    @Override
    public long getNumberOfProcessDeploymentInfosCanBeStartedByUsersManagedBy(final long managerUserId, final QueryOptions countOptions)
            throws SBonitaReadException {
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, "UsersManagedByCanStart", countOptions,
                    Collections.singletonMap("managerUserId", (Object) managerUserId));
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfos(final long userId, final QueryOptions searchOptions, final String querySuffix)
            throws SBonitaReadException {
        return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, querySuffix, searchOptions,
                Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public long getNumberOfProcessDeploymentInfos(final long userId, final QueryOptions countOptions, final String querySuffix) throws SBonitaReadException {
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, querySuffix, countOptions,
                    Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public long getNumberOfUncategorizedProcessDeploymentInfos(final QueryOptions countOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, UNCATEGORIZED_SUFFIX, countOptions, null);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchUncategorizedProcessDeploymentInfos(final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, UNCATEGORIZED_SUFFIX, searchOptions, null);
    }

    @Override
    public long getNumberOfUncategorizedProcessDeploymentInfosSupervisedBy(final long userId, final QueryOptions countOptions) throws SBonitaReadException {
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, UNCATEGORIZED_SUPERVISED_BY_SUFFIX, countOptions,
                    Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchUncategorizedProcessDeploymentInfosSupervisedBy(final long userId, final QueryOptions searchOptions)
            throws SBonitaReadException {
            return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, UNCATEGORIZED_SUPERVISED_BY_SUFFIX, searchOptions,
                    Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchUncategorizedProcessDeploymentInfosCanBeStartedBy(final long userId, final QueryOptions searchOptions)
            throws SBonitaReadException {
            return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, UNCATEGORIZED_USERCANSTART_SUFFIX, searchOptions,
                    Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public long getNumberOfUncategorizedProcessDeploymentInfosCanBeStartedBy(final long userId, final QueryOptions countOptions) throws SBonitaReadException {
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, UNCATEGORIZED_USERCANSTART_SUFFIX, countOptions,
                    Collections.singletonMap(USER_ID, (Object) userId));
    }

    @Override
    public Map<Long, SProcessDefinitionDeployInfo> getProcessDeploymentInfosFromProcessInstanceIds(final List<Long> processInstanceIds)
            throws SBonitaReadException {
        if (processInstanceIds == null || processInstanceIds.size() == 0) {
            return Collections.emptyMap();
        }
            final QueryOptions queryOptions = new QueryOptions(0, processInstanceIds.size(), SProcessDefinitionDeployInfo.class, "name", OrderByType.ASC);
            final Map<String, Object> parameters = Collections.singletonMap("processInstanceIds", (Object) processInstanceIds);
            final List<Map<String, Object>> result = persistenceService.selectList(new SelectListDescriptor<Map<String, Object>>(
                    "getProcessDeploymentInfoFromProcessInstanceIds", parameters, SProcessDefinitionDeployInfo.class, queryOptions));
            if (result != null && result.size() > 0) {
                return getProcessDeploymentInfosFromMap(result);
            }
        return Collections.emptyMap();
    }

    @Override
    public Map<Long, SProcessDefinitionDeployInfo> getProcessDeploymentInfosFromArchivedProcessInstanceIds(final List<Long> archivedProcessInstantsIds)
            throws SBonitaReadException {
        if (archivedProcessInstantsIds == null || archivedProcessInstantsIds.size() == 0) {
            return Collections.emptyMap();
        }
            final QueryOptions queryOptions = new QueryOptions(0, archivedProcessInstantsIds.size(), SProcessDefinitionDeployInfo.class, "name",
                    OrderByType.ASC);
            final Map<String, Object> parameters = Collections.singletonMap("archivedProcessInstanceIds", (Object) archivedProcessInstantsIds);
            final List<Map<String, Object>> result = persistenceService.selectList(new SelectListDescriptor<Map<String, Object>>(
                    "getProcessDeploymentInfoFromArchivedProcessInstanceIds", parameters, SProcessDefinitionDeployInfo.class, queryOptions));
            if (result != null && result.size() > 0) {
                return getProcessDeploymentInfosFromMap(result);
            }
        return Collections.emptyMap();
    }

    private Map<Long, SProcessDefinitionDeployInfo> getProcessDeploymentInfosFromMap(final List<Map<String, Object>> sProcessDeploymentInfos) {
        final Map<Long, SProcessDefinitionDeployInfo> mProcessDeploymentInfos = new HashMap<>();
        for (final Map<String, Object> sProcessDeploymentInfo : sProcessDeploymentInfos) {
            final Long archivedProcessInstanceId = (Long) sProcessDeploymentInfo.get("archivedProcessInstanceId");
            final Long processInstanceId = (Long) sProcessDeploymentInfo.get("processInstanceId");
            final SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo = buildSProcessDefinitionDeployInfo(sProcessDeploymentInfo);
            mProcessDeploymentInfos.put(processInstanceId != null ? processInstanceId : archivedProcessInstanceId, sProcessDefinitionDeployInfo);
        }
        return mProcessDeploymentInfos;
    }

    private SProcessDefinitionDeployInfo buildSProcessDefinitionDeployInfo(final Map<String, Object> sProcessDeploymentInfo) {
        final SProcessDefinitionDeployInfoBuilderFactory builder = BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class);

        final Long id = (Long) sProcessDeploymentInfo.get(builder.getIdKey());
        final Long processId = (Long) sProcessDeploymentInfo.get(builder.getProcessIdKey());
        final String name = (String) sProcessDeploymentInfo.get(builder.getNameKey());
        final String version = (String) sProcessDeploymentInfo.get(builder.getVersionKey());
        final String description = (String) sProcessDeploymentInfo.get(builder.getVersionKey());
        final Long deploymentDate = (Long) sProcessDeploymentInfo.get(builder.getDeploymentDateKey());
        final Long deployedBy = (Long) sProcessDeploymentInfo.get(builder.getDeployedByKey());
        final String activationState = (String) sProcessDeploymentInfo.get(builder.getActivationStateKey());
        final String configurationState = (String) sProcessDeploymentInfo.get(builder.getConfigurationStateKey());
        final String displayName = (String) sProcessDeploymentInfo.get(builder.getDisplayNameKey());
        final Long lastUpdateDate = (Long) sProcessDeploymentInfo.get(builder.getLastUpdateDateKey());
        final String iconPath = (String) sProcessDeploymentInfo.get(builder.getIconPathKey());
        final String displayDescription = (String) sProcessDeploymentInfo.get(builder.getDisplayDescriptionKey());

        return builder.createNewInstance(name, version).setId(id).setDescription(description)
                .setDisplayDescription(displayDescription).setActivationState(activationState).setConfigurationState(configurationState)
                .setDeployedBy(deployedBy).setProcessId(processId).setLastUpdateDate(lastUpdateDate).setDisplayName(displayName)
                .setDeploymentDate(deploymentDate).setIconPath(iconPath).done();
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosUnrelatedToCategory(final long categoryId, final int pageIndex, final int numberPerPage,
            final ProcessDeploymentInfoCriterion pagingCriterion) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("categoryId", (Object) categoryId);
        final QueryOptions queryOptions = createQueryOptions(pageIndex, numberPerPage, pagingCriterion);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "searchSProcessDefinitionDeployInfoUnrelatedToCategory", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public Long getNumberOfProcessDeploymentInfosUnrelatedToCategory(final long categoryId) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("categoryId", (Object) categoryId);
        final SelectOneDescriptor<Long> selectDescriptor = new SelectOneDescriptor<>("getNumberOfSProcessDefinitionDeployInfoUnrelatedToCategory",
                parameters, SProcessDefinitionDeployInfo.class);
            return persistenceService.selectOne(selectDescriptor);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosOfCategory(final long categoryId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("categoryId", (Object) categoryId);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "searchSProcessDeploymentInfosOfCategory", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
        return persistenceService.selectList(selectDescriptor);
    }

    private QueryOptions createQueryOptions(final int pageIndex, final int numberPerPage, final ProcessDeploymentInfoCriterion pagingCriterion) {
        String field;
        OrderByType order;
        final SProcessDefinitionDeployInfoBuilderFactory fact = BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class);
        switch (pagingCriterion) {
            case NAME_ASC:
                field = fact.getNameKey();
                order = OrderByType.ASC;
                break;
            case NAME_DESC:
                field = fact.getNameKey();
                order = OrderByType.DESC;
                break;
            case ACTIVATION_STATE_ASC:
                field = fact.getActivationStateKey();
                order = OrderByType.ASC;
                break;
            case ACTIVATION_STATE_DESC:
                field = fact.getActivationStateKey();
                order = OrderByType.DESC;
                break;
            case CONFIGURATION_STATE_ASC:
                field = fact.getConfigurationStateKey();
                order = OrderByType.ASC;
                break;
            case CONFIGURATION_STATE_DESC:
                field = fact.getConfigurationStateKey();
                order = OrderByType.DESC;
                break;
            case VERSION_ASC:
                field = fact.getVersionKey();
                order = OrderByType.ASC;
                break;
            case VERSION_DESC:
                field = fact.getVersionKey();
                order = OrderByType.DESC;
                break;
            case DEFAULT:
            default:
                field = pagingCriterion.getField();
                order = OrderByType.valueOf(pagingCriterion.getOrder().name());
                break;
        }

        return new QueryOptions(pageIndex, numberPerPage, SProcessDefinitionDeployInfo.class, field, order);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfos(final QueryOptions queryOptions) throws SBonitaReadException {
            return persistenceService.selectList(new SelectListDescriptor<SProcessDefinitionDeployInfo>("getProcessDefinitionDeployInfos", Collections
                    .<String, Object> emptyMap(), SProcessDefinitionDeployInfo.class, queryOptions));
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosWithActorOnlyForGroup(final long groupId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap(GROUP_ID, (Object) groupId);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "getProcessesWithActorOnlyForGroup", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosWithActorOnlyForGroups(final List<Long> groupIds, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("groupIds", (Object) groupIds);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "getProcessesWithActorOnlyForGroups", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosWithActorOnlyForRole(final long roleId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap(ROLE_ID, (Object) roleId);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "getProcessesWithActorOnlyForRole", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosWithActorOnlyForRoles(final List<Long> roleIds, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("roleIds", (Object) roleIds);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "getProcessesWithActorOnlyForRoles", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosWithActorOnlyForUser(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "getProcessesWithActorOnlyForUser", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getProcessDeploymentInfosWithActorOnlyForUsers(final List<Long> userIds, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userIds", (Object) userIds);
        final SelectListDescriptor<SProcessDefinitionDeployInfo> selectDescriptor = new SelectListDescriptor<>(
                "getProcessesWithActorOnlyForUsers", parameters, SProcessDefinitionDeployInfo.class, queryOptions);
            return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public long getNumberOfUsersWhoCanStartProcessDeploymentInfo(final long processDefinitionId, final QueryOptions queryOptions)
            throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap(PROCESS_DEFINITION_ID, (Object) processDefinitionId);
            return persistenceService.getNumberOfEntities(SUser.class, WHOCANSTART_PROCESS_SUFFIX, queryOptions, parameters);
    }

    @Override
    public List<SUser> searchUsersWhoCanStartProcessDeploymentInfo(final long processDefinitionId, final QueryOptions queryOptions)
            throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap(PROCESS_DEFINITION_ID, (Object) processDefinitionId);
            return persistenceService.searchEntity(SUser.class, WHOCANSTART_PROCESS_SUFFIX, queryOptions, parameters);
    }

    @Override
    public long getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, "WithAssignedOrPendingHumanTasksFor", queryOptions,
                    parameters);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(final long userId,
            final QueryOptions queryOptions) throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, "WithAssignedOrPendingHumanTasksFor", queryOptions, parameters);
    }

    @Override
    public long getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, "WithAssignedOrPendingHumanTasksSupervisedBy", queryOptions,
                    parameters);

        }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(final long userId,
            final QueryOptions queryOptions) throws SBonitaReadException {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, "WithAssignedOrPendingHumanTasksSupervisedBy", queryOptions, parameters);
        }

    @Override
    public long getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SProcessDefinitionDeployInfo.class, "WithAssignedOrPendingHumanTasks", queryOptions, null);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks(final QueryOptions queryOptions)
            throws SBonitaReadException {
        return persistenceService.searchEntity(SProcessDefinitionDeployInfo.class, "WithAssignedOrPendingHumanTasks", queryOptions, null);
    }

    @Override
    public DesignProcessDefinition getDesignProcessDefinition(long processDefinitionId) throws SProcessDefinitionNotFoundException,
            SBonitaReadException {
        try {
            return processDefinitionBARContribution.convertXmlToProcess(getProcessDeploymentInfo(processDefinitionId).getDesignContent().getContent());
        } catch (IOException | XMLParseException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public void updateExpressionContent(long processDefinitionId, long expressionDefinitionId, String content) throws SProcessDefinitionNotFoundException,
            SObjectModificationException {
        try {
            final DesignProcessDefinition designProcessDefinition = getDesignProcessDefinition(processDefinitionId);
            final ExpressionImpl expression = (ExpressionImpl) getExpression(designProcessDefinition, expressionDefinitionId);
            if (expression == null) {
                throw new SObjectModificationException("No expression with ID " + expressionDefinitionId + " found on process "
                        + designProcessDefinition.getDisplayName() + " (" + designProcessDefinition.getVersion() + ")");
            }
            if (!isValidExpressionTypeToUpdate(expression.getExpressionType())) {
                throw new SObjectModificationException("Updating an Expression of type " + expression.getExpressionType()
                        + " is not supported. Only Groovy scripts and constants are allowed.");
            }
            final String oldContent = expression.getContent();
            expression.setContent(content);
            final String processDefinitionAsXMLString = getProcessContent(designProcessDefinition);
            final EntityUpdateDescriptor updateDescriptor = BuilderFactory.get(SProcessDefinitionDeployInfoUpdateBuilderFactory.class)
                    .createNewInstance().updateDesignContent(processDefinitionAsXMLString).done();
            updateProcessDefinitionDeployInfo(processDefinitionId, updateDescriptor, "Update expression <" + expressionDefinitionId + ">, old content is <" + oldContent + ">");
        } catch (IOException e) {
            throw new SProcessDefinitionNotFoundException(e, processDefinitionId);
        } catch (SBonitaReadException | SProcessDeploymentInfoUpdateException e) {
            throw new SObjectModificationException(e);
        }
    }

    String truncate(String logMessage) {
        return logMessage.substring(0, Math.min(255, logMessage.length()));
    }

    protected boolean isValidExpressionTypeToUpdate(String type) {
        return ExpressionType.TYPE_READ_ONLY_SCRIPT.name().equals(type) || ExpressionType.TYPE_CONSTANT.name().equals(type);
    }

    protected Expression getExpression(DesignProcessDefinition processDefinition, long expressionDefinitionId) {
        final ExpressionFinder expressionFinder = new ExpressionFinder();
        expressionFinder.find(processDefinition, expressionDefinitionId);
        return expressionFinder.getFoundExpression();
    }

}
