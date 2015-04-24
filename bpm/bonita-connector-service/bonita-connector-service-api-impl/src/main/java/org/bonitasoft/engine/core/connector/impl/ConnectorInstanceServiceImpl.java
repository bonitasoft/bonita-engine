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
package org.bonitasoft.engine.core.connector.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceCreationException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceDeletionException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceModificationException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceNotFoundException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceWithFailureInfoBuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
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
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ConnectorInstanceServiceImpl implements ConnectorInstanceService {

    /**
     * Length maximum of the message of the exception
     */
    private static final int MAX_MESSAGE_LENGTH = 255;

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final EventService eventService;

    private final ArchiveService archiveService;

    public ConnectorInstanceServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final ArchiveService archiveService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.archiveService = archiveService;
        this.eventService = eventService;
    }

    @Override
    public void setState(final SConnectorInstance sConnectorInstance, final String state) throws SConnectorInstanceModificationException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(BuilderFactory.get(SConnectorInstanceBuilderFactory.class).getStateKey(), state);

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sConnectorInstance, entityUpdateDescriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(CONNECTOR_INSTANCE_STATE, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(CONNECTOR_INSTANCE_STATE)
                    .setObject(sConnectorInstance).done();
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceModificationException(e);
        }
    }

    @Override
    public void setConnectorInstanceFailureException(final SConnectorInstanceWithFailureInfo connectorInstanceWithFailure, final Throwable throwable)
            throws SConnectorInstanceModificationException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(BuilderFactory.get(SConnectorInstanceWithFailureInfoBuilderFactory.class).getExceptionMessageKey(),
                getExceptionMessage(throwable));
        try {
            entityUpdateDescriptor.addField(BuilderFactory.get(SConnectorInstanceWithFailureInfoBuilderFactory.class).getStackTraceKey(),
                    getStringStackTrace(throwable));
        } catch (final IOException e) {
            throw new SConnectorInstanceModificationException(e);
        }

        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(connectorInstanceWithFailure, entityUpdateDescriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(CONNECTOR_INSTANCE, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(CONNECTOR_INSTANCE)
                    .setObject(connectorInstanceWithFailure).done();
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceModificationException(e);
        }
    }

    private String getExceptionMessage(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message != null && message.length() > MAX_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_MESSAGE_LENGTH);
        }
        return message;
    }

    private static String getStringStackTrace(final Throwable throwable) throws IOException {
        if (throwable == null) {
            return null;
        }
        final StringWriter writer = new StringWriter();
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(writer);
            throwable.printStackTrace(printer);
            return writer.toString();
        } finally {
            if (printer != null) {
                printer.close();
            }
            writer.close();
        }
    }

    @Override
    public void createConnectorInstance(final SConnectorInstance connectorInstance) throws SConnectorInstanceCreationException {
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(CONNECTOR_INSTANCE, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(CONNECTOR_INSTANCE).setObject(connectorInstance)
                    .done();
        }
        final InsertRecord insertRecord = new InsertRecord(connectorInstance);
        try {
            recorder.recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceCreationException(e);
        }
    }

    @Override
    public List<SConnectorInstance> getConnectorInstances(final long containerId, final String containerType, final ConnectorEvent activationEvent,
            final int from, final int numberOfResult, final String state) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(4);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        inputParameters.put("activationEvent", activationEvent);
        inputParameters.put("state", state);
        final SelectListDescriptor<SConnectorInstance> selectListDescriptor = new SelectListDescriptor<SConnectorInstance>("getConnectorInstancesWithState",
                inputParameters, SConnectorInstance.class, new QueryOptions(from, numberOfResult, SConnectorInstance.class, "id", OrderByType.ASC));
        try {
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public List<SConnectorInstance> getConnectorInstances(final long containerId, final String containerType, final int from, final int numberOfResult,
            final String fieldName, final OrderByType orderByType) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        final SelectListDescriptor<SConnectorInstance> selectListDescriptor = new SelectListDescriptor<SConnectorInstance>("getConnectorInstances",
                inputParameters, SConnectorInstance.class, new QueryOptions(from, numberOfResult, SConnectorInstance.class, fieldName, orderByType));
        try {
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    private List<SConnectorInstance> getConnectorInstancesOrderedById(final long containerId, final String containerType, final int from,
            final int numberOfResult) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        final SelectListDescriptor<SConnectorInstance> selectListDescriptor = new SelectListDescriptor<SConnectorInstance>("getConnectorInstancesOrderedById",
                inputParameters, SConnectorInstance.class, new QueryOptions(from, numberOfResult));
        try {
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public SConnectorInstance getNextExecutableConnectorInstance(final long containerId, final String containerType, final ConnectorEvent activationEvent)
            throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(3);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        inputParameters.put("activationEvent", activationEvent);
        final SelectListDescriptor<SConnectorInstance> selectOneDescriptor = new SelectListDescriptor<SConnectorInstance>("getNextExecutableConnectorInstance",
                inputParameters, SConnectorInstance.class, new QueryOptions(0, 1));
        try {
            final List<SConnectorInstance> selectList = persistenceService.selectList(selectOneDescriptor);
            if (selectList.size() == 1) {
                return selectList.get(0);
            }
            return null;
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public long getNumberOfConnectorInstances(final long containerId, final String containerType) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(2);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        final SelectOneDescriptor<Long> selectListDescriptor = new SelectOneDescriptor<Long>("getNumberOfConnectorInstances", inputParameters,
                SConnectorInstance.class);
        try {
            return persistenceService.selectOne(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public SConnectorInstance getConnectorInstance(final long connectorInstanceId) throws SConnectorInstanceReadException, SConnectorInstanceNotFoundException {
        final SelectByIdDescriptor<SConnectorInstance> selectByIdDescriptor = new SelectByIdDescriptor<SConnectorInstance>("getConnectorInstance",
                SConnectorInstance.class, connectorInstanceId);
        try {
            final SConnectorInstance connectorInstance = persistenceService.selectById(selectByIdDescriptor);
            if (connectorInstance == null) {
                throw new SConnectorInstanceNotFoundException(connectorInstanceId);
            }
            return connectorInstance;
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public SConnectorInstanceWithFailureInfo getConnectorInstanceWithFailureInfo(final long connectorInstanceId) throws SConnectorInstanceReadException,
            SConnectorInstanceNotFoundException {
        final SelectByIdDescriptor<SConnectorInstanceWithFailureInfo> selectByIdDescriptor = new SelectByIdDescriptor<SConnectorInstanceWithFailureInfo>(
                "getConnectorInstanceWithFailureInfo", SConnectorInstanceWithFailureInfo.class, connectorInstanceId);
        try {
            final SConnectorInstanceWithFailureInfo connectorInstance = persistenceService.selectById(selectByIdDescriptor);
            if (connectorInstance == null) {
                throw new SConnectorInstanceNotFoundException(connectorInstanceId);
            }
            return connectorInstance;
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public List<SConnectorInstanceWithFailureInfo> getConnectorInstancesWithFailureInfo(final long containerId, final String containerType, final String state,
            final int from, final int maxResults) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(3);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        inputParameters.put("state", state);
        final SelectListDescriptor<SConnectorInstanceWithFailureInfo> selectListDescriptor = new SelectListDescriptor<SConnectorInstanceWithFailureInfo>(
                "getConnectorInstancesWithFailureInfoInState",
                inputParameters, SConnectorInstanceWithFailureInfo.class, new QueryOptions(from, maxResults, SConnectorInstanceWithFailureInfo.class, "id",
                        OrderByType.ASC));
        try {
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public long getNumberOfConnectorInstances(final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SConnectorInstance.class, searchOptions, null);
    }

    // charles
    @Override
    public List<SConnectorInstance> searchConnectorInstances(final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(SConnectorInstance.class, searchOptions, null);
    }

    @Override
    public void archiveConnectorInstance(final SConnectorInstance connectorInstance, final long archiveDate) throws SConnectorInstanceCreationException {
        if (connectorInstance != null) {
            final SAConnectorInstance saConnectorInstance = BuilderFactory.get(SAConnectorInstanceBuilderFactory.class)
                    .createNewArchivedConnectorInstance(connectorInstance).done();
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saConnectorInstance);
            try {
                archiveService.recordInsert(archiveDate, insertRecord);
            } catch (final SBonitaException e) {
                throw new SConnectorInstanceCreationException("Unable to archive the connectorInstance instance with id " + connectorInstance.getId(), e);
            }
        }
    }

    @Override
    public void deleteConnectorInstance(final SConnectorInstance connectorInstance) throws SConnectorInstanceDeletionException {
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(CONNECTOR_INSTANCE, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(CONNECTOR_INSTANCE).setObject(connectorInstance)
                    .done();
        }
        final DeleteRecord deleteRecord = new DeleteRecord(connectorInstance);
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceDeletionException(e);
        }

    }

    @Override
    public long getNumberArchivedConnectorInstance(final QueryOptions searchOptions, final ReadPersistenceService persistenceService)
            throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SAConnectorInstance.class, searchOptions, null);
    }

    @Override
    public List<SAConnectorInstance> searchArchivedConnectorInstance(final QueryOptions searchOptions, final ReadPersistenceService persistenceService)
            throws SBonitaReadException {
        return persistenceService.searchEntity(SAConnectorInstance.class, searchOptions, null);
    }

    @Override
    public void deleteArchivedConnectorInstance(final SAConnectorInstance sConnectorInstance) throws SConnectorInstanceDeletionException {
        final SDeleteEvent deleteEvent = null;
        final DeleteRecord deleteRecord = new DeleteRecord(sConnectorInstance);
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceDeletionException(e);
        }
    }

    @Override
    public void deleteArchivedConnectorInstances(final long containerId, final String containerType) throws SBonitaReadException,
            SConnectorInstanceDeletionException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final List<FilterOption> filters = buildFiltersForConnectors(containerId, containerType, true);
        final OrderByOption orderBy = new OrderByOption(SAConnectorInstance.class, BuilderFactory.get(SConnectorInstanceBuilderFactory.class).getIdKey(),
                OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, 100, Collections.singletonList(orderBy), filters, null);
        List<SAConnectorInstance> connectorInstances;
        do {
            connectorInstances = searchArchivedConnectorInstance(queryOptions, persistenceService);
            for (final SAConnectorInstance sConnectorInstance : connectorInstances) {
                deleteArchivedConnectorInstance(sConnectorInstance);
            }
        } while (!connectorInstances.isEmpty());

    }

    @Override
    public void deleteConnectors(final long containerId, final String containerType) throws SConnectorInstanceReadException,
            SConnectorInstanceDeletionException {
        List<SConnectorInstance> connetorInstances;
        do {
            // the QueryOptions always will use 0 as start index because the retrieved results will be deleted
            connetorInstances = getConnectorInstancesOrderedById(containerId, containerType, 0, 100);
            for (final SConnectorInstance sConnectorInstance : connetorInstances) {
                deleteConnectorInstance(sConnectorInstance);
            }
        } while (!connetorInstances.isEmpty());
    }

    private List<FilterOption> buildFiltersForConnectors(final long containerId, final String containerType, final boolean archived) {
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        Class<? extends PersistentObject> persistentClass;
        if (archived) {
            persistentClass = SAConnectorInstance.class;
        } else {
            persistentClass = SConnectorInstance.class;
        }
        filters.add(new FilterOption(persistentClass, BuilderFactory.get(SConnectorInstanceBuilderFactory.class).getContainerIdKey(), containerId));
        filters.add(new FilterOption(persistentClass, BuilderFactory.get(SConnectorInstanceBuilderFactory.class).getContainerTypeKey(), containerType));
        return filters;
    }
}
