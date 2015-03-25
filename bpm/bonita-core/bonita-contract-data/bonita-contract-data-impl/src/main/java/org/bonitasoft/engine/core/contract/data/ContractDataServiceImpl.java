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
package org.bonitasoft.engine.core.contract.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
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
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Matthieu Chaffotte
 */
public class ContractDataServiceImpl implements ContractDataService {

    private static final String PROCESS_CONTRACT_DATA = "PROCESS_CONTRACT_DATA";
    private static final String USERTASK_CONTRACT_DATA = "USERTASK_CONTRACT_DATA";
    
    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final QueriableLoggerService queriableLoggerService;

    private final ArchiveService archiveService;

    public ContractDataServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final QueriableLoggerService queriableLoggerService, final ArchiveService archiveService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.queriableLoggerService = queriableLoggerService;
        this.archiveService = archiveService;
    }

    @Override
    public void addUserTaskData(final long userTaskId, final Map<String, Serializable> data) throws SContractDataCreationException {
        if (data == null) {
            return;
        }
        for (final Entry<String, Serializable> datum : data.entrySet()) {
            addUserTaskData(new STaskContractData(userTaskId, datum.getKey(), datum.getValue()));
        }
    }

    protected void addUserTaskData(final STaskContractData taskContractData) throws SContractDataCreationException {
        final SContractDataLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new user task contract data", USERTASK_CONTRACT_DATA);
        final InsertRecord insertRecord = new InsertRecord(taskContractData);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(USERTASK_CONTRACT_DATA, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(USERTASK_CONTRACT_DATA).setObject(taskContractData).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(taskContractData.getId(), SQueriableLog.STATUS_OK, logBuilder, "addUserTaskData");
        } catch (final SRecorderException re) {
            initiateLogBuilder(taskContractData.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addUserTaskData");
            throw new SContractDataCreationException(re);
        }
    }

    @Override
    public Serializable getUserTaskDataValue(final long userTaskId, final String dataName) throws SContractDataNotFoundException, SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", dataName);
        parameters.put("scopeId", userTaskId);
        final SelectOneDescriptor<STaskContractData> descriptor = new SelectOneDescriptor<STaskContractData>("getContractDataByUserTaskIdAndDataName",
                parameters, STaskContractData.class);
        final STaskContractData contractData = persistenceService.selectOne(descriptor);
        if (contractData == null) {
            throw new SContractDataNotFoundException("No contract data found named: " + dataName + " of user task: " + userTaskId);
        }
        return contractData.getValue();
    }

    @Override
    public void deleteUserTaskData(final long userTaskId) throws SContractDataDeletionException {
        try {
            final List<STaskContractData> contractData = getContractDataOfUserTask(userTaskId);
            for (final STaskContractData data : contractData) {
                deleteUserTaskData(data);
            }
        } catch (final SBonitaReadException sbre) {
            throw new SContractDataDeletionException(sbre);
        }
    }

    protected void deleteUserTaskData(final STaskContractData taskContractData) throws SContractDataDeletionException {
        final DeleteRecord deleteRecord = new DeleteRecord(taskContractData);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(USERTASK_CONTRACT_DATA, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(USERTASK_CONTRACT_DATA).setObject(taskContractData).done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException sre) {
            throw new SContractDataDeletionException(sre);
        }
    }

    @Override
    public void archiveAndDeleteUserTaskData(final long userTaskId, final long archiveDate) throws SObjectModificationException {
        try {
            final List<STaskContractData> contractData = getContractDataOfUserTask(userTaskId);
            if (!contractData.isEmpty()) {
                final ArchiveInsertRecord[] records = buildArchiveUserTaskRecords(contractData);
                archiveService.recordInserts(archiveDate, records);
                for (STaskContractData taskContractData : contractData) {
                    deleteUserTaskData(taskContractData);
                }
            }
        } catch (final SBonitaException sbe) {
            throw new SObjectModificationException(sbe);
        }
    }

    private ArchiveInsertRecord[] buildArchiveUserTaskRecords(final List<STaskContractData> taskContractData) {
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[taskContractData.size()];
        int i = 0;
        for (final STaskContractData data : taskContractData) {
            if (data != null) {
                final SATaskContractData aData = new SATaskContractData(data);
                records[i] = new ArchiveInsertRecord(aData);
                i++;
            }
        }
        return records;
    }

    private List<STaskContractData> getContractDataOfUserTask(final long userTaskId) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("scopeId", userTaskId);
        final QueryOptions queryOptions = new QueryOptions(0, 10000);
        final SelectListDescriptor<STaskContractData> descriptor = new SelectListDescriptor<STaskContractData>("getContractDataByUserTaskId", parameters,
                STaskContractData.class, queryOptions);
        return persistenceService.selectList(descriptor);
    }

    private SContractDataLogBuilder getQueriableLog(final ActionType actionType, final String message, String contractDataPrefix) {
        final SContractDataLogBuilder logBuilder = new SContractDataLogBuilder(contractDataPrefix);
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
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
    public Serializable getArchivedUserTaskDataValue(final long userTaskId, final String dataName) throws SContractDataNotFoundException, SBonitaReadException {
        final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("scopeId", userTaskId);
        parameters.put("name", dataName);
        final SelectOneDescriptor<SATaskContractData> descriptor = new SelectOneDescriptor<SATaskContractData>(
                "getArchivedContractDataByUserTaskIdAndDataName", parameters, SATaskContractData.class);
        final SATaskContractData contractData = readPersistenceService.selectOne(descriptor);
        if (contractData == null) {
            throw new SContractDataNotFoundException("No contract data found named: " + dataName + " of user task: " + userTaskId);
        }
        return contractData.getValue();
    }

    @Override
    public void addProcessData(final long processInstanceId, final Map<String, Serializable> data) throws SContractDataCreationException {
        if (data == null) {
            return;
        }
        for (final Entry<String, Serializable> datum : data.entrySet()) {
            addProcessData(new SProcessContractData(processInstanceId, datum.getKey(), datum.getValue()));
        }
    }

    protected void addProcessData(SProcessContractData processContractData) throws SContractDataCreationException {
        final SContractDataLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new process contract data", PROCESS_CONTRACT_DATA);
        final InsertRecord insertRecord = new InsertRecord(processContractData);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(PROCESS_CONTRACT_DATA, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(PROCESS_CONTRACT_DATA).setObject(processContractData)
                    .done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(processContractData.getId(), SQueriableLog.STATUS_OK, logBuilder, "addProcessData");
        } catch (final SRecorderException re) {
            initiateLogBuilder(processContractData.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addProcessData");
            throw new SContractDataCreationException(re);
        }
    }

    @Override
    public Serializable getProcessDataValue(final long processInstanceId, final String dataName) throws SContractDataNotFoundException, SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", dataName);
        parameters.put("scopeId", processInstanceId);
        final SelectOneDescriptor<SProcessContractData> descriptor = new SelectOneDescriptor<>("getContractDataByProcessInstanceIdAndDataName", parameters,
                SProcessContractData.class);
        final SProcessContractData contractData = persistenceService.selectOne(descriptor);
        if (contractData == null) {
            throw new SContractDataNotFoundException("No contract data found named: " + dataName + " for process instance with id: " + processInstanceId);
        }
        return contractData.getValue();
    }

    @Override
    public void deleteProcessData(final long processInstanceId) throws SContractDataDeletionException {
        try {
            final List<SProcessContractData> contractData = getContractDataOfProcess(processInstanceId);
            for (final SProcessContractData data : contractData) {
                deleteProcessData(data);
            }
        } catch (final SBonitaReadException sbre) {
            throw new SContractDataDeletionException(sbre);
        }
    }

    protected void deleteProcessData(final SProcessContractData processContractData) throws SContractDataDeletionException {
        final DeleteRecord deleteRecord = new DeleteRecord(processContractData);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(PROCESS_CONTRACT_DATA, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PROCESS_CONTRACT_DATA).setObject(processContractData)
                    .done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException sre) {
            throw new SContractDataDeletionException(sre);
        }
    }

    @Override
    public void archiveAndDeleteProcessData(final long processInstanceId, final long archiveDate) throws SObjectModificationException {
        try {
            final List<SProcessContractData> contractData = getContractDataOfProcess(processInstanceId);
            if (!contractData.isEmpty()) {
                final ArchiveInsertRecord[] records = buildArchiveProcessRecords(contractData);
                archiveService.recordInserts(archiveDate, records);
                for (SProcessContractData processContractData : contractData) {
                    deleteProcessData(processContractData);
                }
            }
        } catch (final SBonitaException sbe) {
            throw new SObjectModificationException(sbe);
        }
    }

    private ArchiveInsertRecord[] buildArchiveProcessRecords(final List<SProcessContractData> processContractData) {
        final ArchiveInsertRecord[] records = new ArchiveInsertRecord[processContractData.size()];
        int i = 0;
        for (final SProcessContractData data : processContractData) {
            if (data != null) {
                final SAProcessContractData aData = new SAProcessContractData(data);
                records[i] = new ArchiveInsertRecord(aData);
                i++;
            }
        }
        return records;
    }

    private List<SProcessContractData> getContractDataOfProcess(final long processInstanceId) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("scopeId", processInstanceId);
        final QueryOptions queryOptions = new QueryOptions(0, 10000);
        final SelectListDescriptor<SProcessContractData> descriptor = new SelectListDescriptor<>("getContractDataByProcessInstanceId",
                parameters, SProcessContractData.class, queryOptions);
        return persistenceService.selectList(descriptor);
    }

    @Override
    public Serializable getArchivedProcessDataValue(final long processInstanceId, final String dataName) throws SContractDataNotFoundException,
            SBonitaReadException {
        final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("scopeId", processInstanceId);
        parameters.put("name", dataName);
        final SelectOneDescriptor<SAProcessContractData> descriptor = new SelectOneDescriptor<>(
                "getArchivedContractDataByProcessInstanceIdAndDataName", parameters, SAProcessContractData.class);
        final SAProcessContractData contractData = readPersistenceService.selectOne(descriptor);
        if (contractData == null) {
            throw new SContractDataNotFoundException("No contract data found named: " + dataName + " of process instance: " + processInstanceId);
        }
        return contractData.getValue();
    }
}
