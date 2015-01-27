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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Matthieu Chaffotte
 */
public class ContractDataServiceImpl implements ContractDataService {

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final QueriableLoggerService queriableLoggerService;

    public ContractDataServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final QueriableLoggerService queriableLoggerService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public void addUserTaskData(final long userTaskId, final Map<String, Object> data) throws SContractDataCreationException {
        for (final Entry<String, Object> datum : data.entrySet()) {
            addUserTaskData(new SContractData(datum.getKey(), (Serializable) datum.getValue(), userTaskId));
        }
    }

    protected void addUserTaskData(final SContractData contractData) throws SContractDataCreationException {
        final SContractDataLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new user task contract data");
        final InsertRecord insertRecord = new InsertRecord(contractData);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers("CONTRACT_DATA", EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent("CONTRACT_DATA").setObject(contractData).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(contractData.getId(), SQueriableLog.STATUS_OK, logBuilder, "addUserTaskData");
        } catch (final SRecorderException re) {
            initiateLogBuilder(contractData.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "addUserTaskData");
            throw new SContractDataCreationException(re);
        }
    }

    @Override
    public Object getUserTaskData(final long userTaskId, final String dataName) throws SContractDataNotFoundException, SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", dataName);
        parameters.put("scopeId", userTaskId);
        final SelectOneDescriptor<SContractData> descriptor = new SelectOneDescriptor<SContractData>("getContractDataByUserTaskIdAndDataName", parameters,
                SContractData.class);
        final SContractData contractData = persistenceService.selectOne(descriptor);
        if (contractData == null) {
            throw new SContractDataNotFoundException("No contract data found named: " + dataName + " of user task: " + userTaskId);
        }
        return contractData.getValue();
    }

    @Override
    public void deleteUserTaskData(final long userTaskId) throws SContractDataDeletionException {
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>();
        filterOptions.add(new FilterOption(SContractData.class, "scopeId", userTaskId));
        final DeleteAllRecord record = new DeleteAllRecord(SContractData.class, filterOptions);
        try {
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException re) {
            throw new SContractDataDeletionException(re);
        }
    }

    private SContractDataLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SContractDataLogBuilder logBuilder = new SContractDataLogBuilder();
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

}
