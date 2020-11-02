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
package org.bonitasoft.engine.core.connector.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.bonitasoft.engine.core.process.instance.model.SAbstractConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.OrderByType;
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
    public void setState(final SAbstractConnectorInstance sConnectorInstance, final String state)
            throws SConnectorInstanceModificationException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SConnectorInstance.STATE_KEY, state);
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(sConnectorInstance, entityUpdateDescriptor),
                    CONNECTOR_INSTANCE_STATE);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceModificationException(e);
        }
    }

    @Override
    public void setConnectorInstanceFailureException(
            final SConnectorInstanceWithFailureInfo connectorInstanceWithFailure, final Throwable throwable)
            throws SConnectorInstanceModificationException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SConnectorInstanceWithFailureInfo.EXCEPTION_MESSAGE,
                getExceptionMessage(throwable));
        entityUpdateDescriptor.addField(SConnectorInstanceWithFailureInfo.STACK_TRACE,
                getStringStackTrace(throwable));
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(connectorInstanceWithFailure, entityUpdateDescriptor),
                    CONNECTOR_INSTANCE);
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

    private static String getStringStackTrace(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        try (StringWriter writer = new StringWriter(); PrintWriter printer = new PrintWriter(writer)) {
            throwable.printStackTrace(printer);
            return writer.toString();
        } catch (Throwable e) {
            //Unable to print exception to the Writer, simply retrieve all exceptions as stacktrace
            try {
                Throwable current = throwable;
                StringBuilder stacktrace = new StringBuilder();
                stacktrace.append("Unable to retrieve stacktrace, exceptions were:\n");
                do {
                    stacktrace.append(" * ").append(current.getClass().getName()).append(": ")
                            .append(current.getMessage()).append("\n");
                    current = current.getCause();
                } while (current != null);
                return stacktrace.toString();
            } catch (Throwable e1) {
                return "Unable to retrieve exception stacktrace";
            }
        }
    }

    @Override
    public void createConnectorInstance(final SConnectorInstance connectorInstance)
            throws SConnectorInstanceCreationException {
        try {
            recorder.recordInsert(new InsertRecord(connectorInstance), CONNECTOR_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceCreationException(e);
        }
    }

    @Override
    public List<SConnectorInstance> getConnectorInstances(final long containerId, final String containerType,
            final ConnectorEvent activationEvent,
            final int from, final int numberOfResult, final String state) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<>(4);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        inputParameters.put("activationEvent", activationEvent);
        inputParameters.put("state", state);
        final SelectListDescriptor<SConnectorInstance> selectListDescriptor = new SelectListDescriptor<>(
                "getConnectorInstancesWithState",
                inputParameters, SConnectorInstance.class,
                new QueryOptions(from, numberOfResult, SConnectorInstance.class, "id", OrderByType.ASC));
        try {
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public List<SConnectorInstance> getConnectorInstances(final long containerId, final String containerType,
            final int from, final int numberOfResult,
            final String fieldName, final OrderByType orderByType) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        final SelectListDescriptor<SConnectorInstance> selectListDescriptor = new SelectListDescriptor<>(
                "getConnectorInstances",
                inputParameters, SConnectorInstance.class,
                new QueryOptions(from, numberOfResult, SConnectorInstance.class, fieldName, orderByType));
        try {
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    private List<SConnectorInstance> getConnectorInstancesOrderedById(final long containerId,
            final String containerType, final int from,
            final int numberOfResult) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        final SelectListDescriptor<SConnectorInstance> selectListDescriptor = new SelectListDescriptor<>(
                "getConnectorInstancesOrderedById",
                inputParameters, SConnectorInstance.class, new QueryOptions(from, numberOfResult));
        try {
            return persistenceService.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public SConnectorInstance getNextExecutableConnectorInstance(final long containerId, final String containerType,
            final ConnectorEvent activationEvent)
            throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<>(3);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        inputParameters.put("activationEvent", activationEvent);
        final SelectListDescriptor<SConnectorInstance> selectOneDescriptor = new SelectListDescriptor<>(
                "getNextExecutableConnectorInstance",
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
    public long getNumberOfConnectorInstances(final long containerId, final String containerType)
            throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<>(2);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        final SelectOneDescriptor<Long> selectListDescriptor = new SelectOneDescriptor<>(
                "getNumberOfConnectorInstances", inputParameters,
                SConnectorInstance.class);
        try {
            return persistenceService.selectOne(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public SConnectorInstance getConnectorInstance(final long connectorInstanceId)
            throws SConnectorInstanceReadException, SConnectorInstanceNotFoundException {
        final SelectByIdDescriptor<SConnectorInstance> selectByIdDescriptor = new SelectByIdDescriptor<>(
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
    public SConnectorInstanceWithFailureInfo getConnectorInstanceWithFailureInfo(final long connectorInstanceId)
            throws SConnectorInstanceReadException,
            SConnectorInstanceNotFoundException {
        final SelectByIdDescriptor<SConnectorInstanceWithFailureInfo> selectByIdDescriptor = new SelectByIdDescriptor<>(
                SConnectorInstanceWithFailureInfo.class, connectorInstanceId);
        try {
            final SConnectorInstanceWithFailureInfo connectorInstance = persistenceService
                    .selectById(selectByIdDescriptor);
            if (connectorInstance == null) {
                throw new SConnectorInstanceNotFoundException(connectorInstanceId);
            }
            return connectorInstance;
        } catch (final SBonitaReadException e) {
            throw new SConnectorInstanceReadException(e);
        }
    }

    @Override
    public List<SConnectorInstanceWithFailureInfo> getConnectorInstancesWithFailureInfo(final long containerId,
            final String containerType, final String state,
            final int from, final int maxResults) throws SConnectorInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<>(3);
        inputParameters.put("containerId", containerId);
        inputParameters.put("containerType", containerType);
        inputParameters.put("state", state);
        final SelectListDescriptor<SConnectorInstanceWithFailureInfo> selectListDescriptor = new SelectListDescriptor<>(
                "getConnectorInstancesWithFailureInfoInState",
                inputParameters, SConnectorInstanceWithFailureInfo.class,
                new QueryOptions(from, maxResults, SConnectorInstanceWithFailureInfo.class, "id",
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
    public List<SConnectorInstance> searchConnectorInstances(final QueryOptions searchOptions)
            throws SBonitaReadException {
        return persistenceService.searchEntity(SConnectorInstance.class, searchOptions, null);
    }

    @Override
    public void archiveConnectorInstance(final SConnectorInstance connectorInstance, final long archiveDate)
            throws SConnectorInstanceCreationException {
        if (connectorInstance != null) {
            final SAConnectorInstance saConnectorInstance = BuilderFactory.get(SAConnectorInstanceBuilderFactory.class)
                    .createNewArchivedConnectorInstance(connectorInstance).done();
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saConnectorInstance);
            try {
                archiveService.recordInsert(archiveDate, insertRecord);
            } catch (final SBonitaException e) {
                throw new SConnectorInstanceCreationException(
                        "Unable to archive the connectorInstance instance with id " + connectorInstance.getId(), e);
            }
        }
    }

    @Override
    public void deleteConnectorInstance(final SConnectorInstance connectorInstance)
            throws SConnectorInstanceDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(connectorInstance), CONNECTOR_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceDeletionException(e);
        }

    }

    @Override
    public long getNumberArchivedConnectorInstance(final QueryOptions searchOptions,
            final ReadPersistenceService persistenceService)
            throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SAConnectorInstance.class, searchOptions, null);
    }

    @Override
    public List<SAConnectorInstance> searchArchivedConnectorInstance(final QueryOptions searchOptions,
            final ReadPersistenceService persistenceService)
            throws SBonitaReadException {
        return persistenceService.searchEntity(SAConnectorInstance.class, searchOptions, null);
    }

    @Override
    public void deleteArchivedConnectorInstances(List<Long> containerIds, String containerType)
            throws SBonitaException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("containerIds", containerIds);
        map.put("containerType", containerType);
        archiveService.deleteFromQuery("deleteArchivedConnectorInstances", map);
    }

    @Override
    public void deleteConnectors(final long containerId, final String containerType)
            throws SConnectorInstanceReadException,
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
}
