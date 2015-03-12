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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceLogBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.recorder.SelectBusinessDataDescriptorBuilder;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
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
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Matthieu Chaffotte
 */
public class RefBusinessDataServiceImpl implements RefBusinessDataService {

    private final EventService eventService;

    private final Recorder recorder;

    private final ReadPersistenceService persistenceRead;

    private final QueriableLoggerService queriableLoggerService;

    public RefBusinessDataServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final EventService eventService,
            final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.eventService = eventService;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public SRefBusinessDataInstance getRefBusinessDataInstance(final String name, final long processInstanceId)
            throws SRefBusinessDataInstanceNotFoundException, SBonitaReadException {
        final SelectOneDescriptor<SRefBusinessDataInstance> descriptor = SelectBusinessDataDescriptorBuilder.getSRefBusinessDataInstance(name, processInstanceId);
        final SRefBusinessDataInstance ref = persistenceRead.selectOne(descriptor);
        if (ref == null) {
            throw new SRefBusinessDataInstanceNotFoundException(processInstanceId, name);
        }
        return ref;
    }

    @Override
    public SRefBusinessDataInstance getFlowNodeRefBusinessDataInstance(final String name, final long flowNodeInstanceId)
            throws SRefBusinessDataInstanceNotFoundException, SBonitaReadException {
        final SelectOneDescriptor<SRefBusinessDataInstance> descriptor = SelectBusinessDataDescriptorBuilder.getSFlowNodeRefBusinessDataInstance(name,
                flowNodeInstanceId);
        final SRefBusinessDataInstance ref = persistenceRead.selectOne(descriptor);
        if (ref == null) {
            //FIXME
            throw new SRefBusinessDataInstanceNotFoundException(flowNodeInstanceId, name);
        }
        return ref;
    }

    @Override
    public SRefBusinessDataInstance addRefBusinessDataInstance(final SRefBusinessDataInstance instance) throws SRefBusinessDataInstanceCreationException {
        final SRefBusinessDataInstanceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, NEW_REF_BUISNESS_DATA_INSTANCE_ADDED);
        final InsertRecord insertRecord = new InsertRecord(instance);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(REF_BUSINESS_DATA_INSTANCE, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(REF_BUSINESS_DATA_INSTANCE).setObject(instance)
                    .done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(instance.getId(), SQueriableLog.STATUS_OK, logBuilder, "addRefBusinessDataInstance");
        } catch (final SBonitaException sbe) {
            initiateLogBuilder(instance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addRefBusinessDataInstance");
            throw new SRefBusinessDataInstanceCreationException(sbe);
        }
        return instance;
    }

    protected SRefBusinessDataInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SRefBusinessDataInstanceLogBuilder logBuilder = BuilderFactory.get(SRefBusinessDataInstanceLogBuilderFactory.class).createNewInstance();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    protected <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    protected <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public void updateRefBusinessDataInstance(final SSimpleRefBusinessDataInstance refBusinessDataInstance, final Long dataId)
            throws SRefBusinessDataInstanceModificationException {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("dataId", dataId);
        updateRefBusinessDataInstance(refBusinessDataInstance, fields);
    }

    @Override
    public void updateRefBusinessDataInstance(final SMultiRefBusinessDataInstance refBusinessDataInstance, final List<Long> dataIds)
            throws SRefBusinessDataInstanceModificationException {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("dataIds", dataIds);
        updateRefBusinessDataInstance(refBusinessDataInstance, fields);
    }

    public void updateRefBusinessDataInstance(final SRefBusinessDataInstance refBusinessDataInstance, final Map<String, Object> fields)
            throws SRefBusinessDataInstanceModificationException {
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(refBusinessDataInstance, fields);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(REF_BUSINESS_DATA_INSTANCE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(REF_BUSINESS_DATA_INSTANCE)
                        .setObject(refBusinessDataInstance).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException sre) {
            throw new SRefBusinessDataInstanceModificationException(sre);
        }
    }

    @Override
    public int getNumberOfDataOfMultiRefBusinessData(final String name, final long processInstanceId) throws SBonitaReadException {
        final SelectOneDescriptor<Integer> descriptor = SelectBusinessDataDescriptorBuilder.getNumberOfDataOfMultiRefBusinessData(name,
                processInstanceId);
        return persistenceRead.selectOne(descriptor);
    }

    @Override
    public List<SRefBusinessDataInstance> getRefBusinessDataInstances(final long processInstanceId, final int startIndex, final int maxResults)
            throws SBonitaReadException {
        final SelectListDescriptor<SRefBusinessDataInstance> descriptor = SelectBusinessDataDescriptorBuilder.getSRefBusinessDataInstances(processInstanceId,
                startIndex, maxResults);
        return persistenceRead.selectList(descriptor);
    }

}
