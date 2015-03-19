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
package org.bonitasoft.engine.supervisor.mapping.impl;

import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
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
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorCreationException;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorDeletionException;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorNotFoundException;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorLogBuilder;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorLogBuilderFactory;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SupervisorMappingServiceImpl implements SupervisorMappingService {

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final QueriableLoggerService queriableLoggerService;

    public SupervisorMappingServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final QueriableLoggerService queriableLoggerService) {
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public SProcessSupervisor createProcessSupervisor(final SProcessSupervisor supervisor) throws SSupervisorCreationException {
        final SProcessSupervisorLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Adding a new supervisor");
        final InsertRecord insertRecord = new InsertRecord(supervisor);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(SUPERVISOR, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(SUPERVISOR).setObject(supervisor).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            log(supervisor.getId(), SQueriableLog.STATUS_OK, logBuilder, "createSupervisor");
            return supervisor;
        } catch (final SRecorderException re) {
            log(supervisor.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createSupervisor");
            throw new SSupervisorCreationException(re);
        }
    }

    @Override
    public SProcessSupervisor getProcessSupervisor(final long supervisorId) throws SSupervisorNotFoundException {
        final SelectByIdDescriptor<SProcessSupervisor> selectByIdDescriptor = SelectDescriptorBuilder.getSupervisor(supervisorId);
        try {
            final SProcessSupervisor supervisor = persistenceService.selectById(selectByIdDescriptor);
            if (supervisor == null) {
                throw new SSupervisorNotFoundException(supervisorId + " does not refer to any supervisor");
            }
            return supervisor;
        } catch (final SBonitaReadException bre) {
            throw new SSupervisorNotFoundException(bre);
        }
    }

    @Override
    public void deleteProcessSupervisor(final long supervisorId) throws SSupervisorNotFoundException, SSupervisorDeletionException {
        final SProcessSupervisor sSupervisor = getProcessSupervisor(supervisorId);
        deleteProcessSupervisor(sSupervisor);
    }

    @Override
    public void deleteProcessSupervisor(final SProcessSupervisor supervisor) throws SSupervisorDeletionException {
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(SUPERVISOR, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(SUPERVISOR).setObject(supervisor).done();
        }
        final DeleteRecord record = new DeleteRecord(supervisor);
        final SProcessSupervisorLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting supervisor");
        try {
            recorder.recordDelete(record, deleteEvent);
            log(supervisor.getId(), SQueriableLog.STATUS_OK, logBuilder, "createSupervisor");
        } catch (final SRecorderException e) {
            log(supervisor.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createSupervisor");
            throw new SSupervisorDeletionException("Can't delete process supervisor " + supervisor, e);
        }
    }

    @Override
    public void deleteAllProcessSupervisors() throws SSupervisorDeletionException {
        try {
            final DeleteAllRecord record = new DeleteAllRecord(SProcessSupervisor.class, null);
            recorder.recordDeleteAll(record);
        } catch (final SRecorderException e) {
            throw new SSupervisorDeletionException("Can't delete all process supervisors.", e);
        }
    }

    private SProcessSupervisorLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SProcessSupervisorLogBuilder logBuilder = BuilderFactory.get(SProcessSupervisorLogBuilderFactory.class).createNewInstance();
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

    @Override
    public Boolean isProcessSupervisor(final long processDefinitionId, final long userId) throws SBonitaReadException {
        final SelectOneDescriptor<SProcessSupervisor> descriptor = SelectDescriptorBuilder.getSupervisor(processDefinitionId, userId);
        final SProcessSupervisor supervisor = persistenceService.selectOne(descriptor);
        return supervisor != null;
    }

    @Override
    public List<SProcessSupervisor> searchProcessSupervisors(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(SProcessSupervisor.class, null, queryOptions, null);
    }

    @Override
    public long getNumberOfProcessSupervisors(final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SProcessSupervisor.class, null, searchOptions, null);
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
}
