/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.scheduler.impl;

import java.util.List;

import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.builder.SJobLogQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilder;
import org.bonitasoft.engine.scheduler.builder.SJobParameterQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SJobQueriableLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SSchedulerBuilderAccessor;
import org.bonitasoft.engine.scheduler.builder.impl.SJobParameterBuilderImpl;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogReadException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterCreationException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterReadException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.model.impl.SJobDescriptorImpl;
import org.bonitasoft.engine.scheduler.model.impl.SJobParameterImpl;
import org.bonitasoft.engine.scheduler.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Celine Souchet
 */
public class JobServiceImpl implements JobService {

    private final SSchedulerBuilderAccessor builderAccessor;

    private final QueriableLoggerService queriableLogService;

    private final EventService eventService;

    private final Recorder recorder;

    private final ReadPersistenceService readPersistenceService;

    public JobServiceImpl(final SSchedulerBuilderAccessor builderAccessor, final QueriableLoggerService queriableLogService, final EventService eventService,
            final Recorder recorder, final ReadPersistenceService readPersistenceService) {
        this.builderAccessor = builderAccessor;
        this.queriableLogService = queriableLogService;
        this.readPersistenceService = readPersistenceService;
        this.eventService = eventService;
        this.recorder = recorder;
    }

    @Override
    public SJobDescriptor createJobDescriptor(final SJobDescriptor sJobDescriptor, final long tenantId) throws SJobDescriptorCreationException {
        final SJobQueriableLogBuilder logBuilder = getJobQueriableLogBuilder(ActionType.CREATED, "Adding a new job descriptor");
        if (sJobDescriptor == null) {
            throw new SJobDescriptorCreationException("The job is null");
        } else if (sJobDescriptor.getJobName() == null) {
            throw new SJobDescriptorCreationException("The job name is null");
        }

        // Set the tenant manually on the object because it will be serialized
        final SJobDescriptorImpl sJobDescriptorToRecord = new SJobDescriptorImpl(sJobDescriptor.getJobClassName(), sJobDescriptor.getJobName(),
                sJobDescriptor.getDescription());
        sJobDescriptorToRecord.setTenantId(tenantId);

        try {
            create(sJobDescriptorToRecord, JOB_DESCRIPTOR);
            initiateLogBuilder(sJobDescriptorToRecord.getId(), SQueriableLog.STATUS_OK, logBuilder, "createJobDescriptor");
        } catch (final SRecorderException sre) {
            initiateLogBuilder(sJobDescriptorToRecord.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createJobDescriptor");
            throw new SJobDescriptorCreationException(sre);
        }
        return sJobDescriptorToRecord;
    }

    @Override
    public void deleteJobDescriptor(final long id) throws SJobDescriptorNotFoundException, SJobDescriptorReadException, SJobDescriptorDeletionException {
        final SJobDescriptor sJobDescriptor = getJobDescriptor(id);
        deleteJobDescriptor(sJobDescriptor);
    }

    @Override
    public void deleteJobDescriptor(final SJobDescriptor sJobDescriptor) throws SJobDescriptorDeletionException {
        final SJobQueriableLogBuilder logBuilder = getJobQueriableLogBuilder(ActionType.DELETED, "Deleting a job descriptor");
        try {
            delete(sJobDescriptor, JOB_DESCRIPTOR);
            initiateLogBuilder(sJobDescriptor.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteJobDescriptor");
        } catch (final SBonitaException e) {
            initiateLogBuilder(sJobDescriptor.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteJobDescriptor");
            throw new SJobDescriptorDeletionException(e);
        }
    }

    @Override
    public SJobDescriptor getJobDescriptor(final long id) throws SJobDescriptorNotFoundException, SJobDescriptorReadException {
        try {
            final SJobDescriptor sJobDescriptor = readPersistenceService
                    .selectById(SelectDescriptorBuilder.getElementById(SJobDescriptor.class, "SJobDescriptor", id));
            if (sJobDescriptor == null) {
                throw new SJobDescriptorNotFoundException(id);
            }
            return sJobDescriptor;
        } catch (final SBonitaReadException sbre) {
            throw new SJobDescriptorReadException(sbre);
        }
    }

    @Override
    public long getNumberOfJobDescriptors(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return readPersistenceService.getNumberOfEntities(SJobDescriptor.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SJobDescriptor> searchJobDescriptors(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return readPersistenceService.searchEntity(SJobDescriptor.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void createJobParameters(final List<SJobParameter> sJobParameters, final long tenantId, long jobDescriptorId)
            throws SJobParameterCreationException {
        if (sJobParameters != null) {
            for (final SJobParameter sJobParameter : sJobParameters) {
                createJobParameter(sJobParameter, tenantId, jobDescriptorId);
            }
        }
    }

    @Override
    public void createJobParameter(final SJobParameter sJobParameter, final long tenantId, final long jobDescriptorId) throws SJobParameterCreationException {
        final SJobParameterQueriableLogBuilder logBuilder = getJobParameterQueriableLogBuilder(ActionType.CREATED, "Adding a parameter to the job",
                jobDescriptorId);

        // Set the tenant manually on the object because it will be serialized
        final SJobParameterImpl sJobParameterToRecord = (SJobParameterImpl) getJobParameterBuilder()
                .createNewInstance(sJobParameter.getKey(), sJobParameter.getValue()).setJobDescriptorId(jobDescriptorId).done();
        sJobParameterToRecord.setTenantId(tenantId);

        try {
            create(sJobParameterToRecord, JOB_PARAMETER);
            initiateLogBuilder(sJobParameterToRecord.getId(), SQueriableLog.STATUS_OK, logBuilder, "createJobParameter");
        } catch (final SRecorderException sre) {
            initiateLogBuilder(sJobParameterToRecord.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createJobParameter");
            throw new SJobParameterCreationException(sre);
        }
    }

    @Deprecated
    @Override
    public List<SJobParameter> searchJobParameters(final long jobDescriptorId) throws SBonitaSearchException {
        try {
            return readPersistenceService.selectList(new SelectListDescriptor<SJobParameter>("getSJobParameterImplByJobId", CollectionUtil.buildSimpleMap(
                    "jobDescriptorId", jobDescriptorId), SJobParameterImpl.class));
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void deleteJobParameter(final long id) throws SJobParameterNotFoundException, SJobParameterReadException, SJobParameterDeletionException {
        final SJobParameter sJobParameter = getJobParameter(id);
        deleteJobParameter(sJobParameter);
    }

    @Override
    public void deleteJobParameter(final SJobParameter sJobParameter) throws SJobParameterDeletionException {
        final SJobParameterQueriableLogBuilder logBuilder = getJobParameterQueriableLogBuilder(ActionType.DELETED, "Deleting a job parameter",
                sJobParameter.getJobDescriptorId());
        try {
            delete(sJobParameter, JOB_PARAMETER);
            initiateLogBuilder(sJobParameter.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteJobParameter");
        } catch (final SBonitaException e) {
            initiateLogBuilder(sJobParameter.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteJobParameter");
            throw new SJobParameterDeletionException(e);
        }
    }

    @Override
    public SJobParameter getJobParameter(final long id) throws SJobParameterNotFoundException, SJobParameterReadException {
        try {
            final SJobParameter sJobParameter = readPersistenceService
                    .selectById(SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter", id));
            if (sJobParameter == null) {
                throw new SJobParameterNotFoundException(id);
            }
            return sJobParameter;
        } catch (final SBonitaReadException sbre) {
            throw new SJobParameterReadException(sbre);
        }
    }

    @Override
    public List<SJobParameter> searchJobParameters(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return readPersistenceService.searchEntity(SJobParameter.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void createJobLog(final SJobLog sJobLog) throws SJobLogCreationException {
        final SJobLogQueriableLogBuilder logBuilder = getJobLogQueriableLogBuilder(ActionType.CREATED, "Creating a new log for a job",
                sJobLog.getJobDescriptorId());
        try {
            create(sJobLog, JOB_LOG);
            initiateLogBuilder(sJobLog.getId(), SQueriableLog.STATUS_OK, logBuilder, "createJobLog");
        } catch (final SRecorderException sre) {
            initiateLogBuilder(sJobLog.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createJobLog");
            throw new SJobLogCreationException(sre);
        }
    }

    @Override
    public void deleteJobLog(final long id) throws SJobLogNotFoundException, SJobLogReadException, SJobLogDeletionException {
        final SJobLog sJobLog = getJobLog(id);
        deleteJobLog(sJobLog);
    }

    @Override
    public void deleteJobLog(final SJobLog sJobLog) throws SJobLogDeletionException {
        final SJobLogQueriableLogBuilder logBuilder = getJobLogQueriableLogBuilder(ActionType.DELETED, "Deleting a job log", sJobLog.getJobDescriptorId());
        try {
            delete(sJobLog, JOB_LOG);
            initiateLogBuilder(sJobLog.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteJobLog");
        } catch (final SBonitaException e) {
            initiateLogBuilder(sJobLog.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteJobLog");
            throw new SJobLogDeletionException(e);
        }
    }

    @Override
    public SJobLog getJobLog(final long id) throws SJobLogNotFoundException, SJobLogReadException {
        try {
            final SJobLog sJobLog = readPersistenceService
                    .selectById(SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", id));
            if (sJobLog == null) {
                throw new SJobLogNotFoundException(id);
            }
            return sJobLog;
        } catch (final SBonitaReadException sbre) {
            throw new SJobLogReadException(sbre);
        }
    }

    @Override
    public long getNumberOfJobLogs(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return readPersistenceService.getNumberOfEntities(SJobLog.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SJobLog> searchJobLogs(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return readPersistenceService.searchEntity(SJobLog.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SJobQueriableLogBuilder getJobQueriableLogBuilder(final ActionType actionType, final String message) {
        final SJobQueriableLogBuilder logBuilder = builderAccessor.getSJobQueriableLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SJobParameterQueriableLogBuilder getJobParameterQueriableLogBuilder(final ActionType actionType, final String message, final long jobId) {
        final SJobParameterQueriableLogBuilder logBuilder = builderAccessor.getSJobParameterQueriableLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.jogDescriptorId(jobId);
        return logBuilder;
    }

    private SJobLogQueriableLogBuilder getJobLogQueriableLogBuilder(final ActionType actionType, final String message, final long jobId) {
        final SJobLogQueriableLogBuilder logBuilder = builderAccessor.getSJobLogQueriableLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.jogDescriptorId(jobId);
        return logBuilder;
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLogService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLogService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    private SJobParameterBuilder getJobParameterBuilder() {
        return new SJobParameterBuilderImpl();
    }

    private void delete(final PersistentObject persistentObject, final String eventType) throws SRecorderException {
        final DeleteRecord deleteRecord = new DeleteRecord(persistentObject);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(eventType, EventActionType.DELETED)) {
            final SEventBuilder eventBuilder = eventService.getEventBuilder();
            deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(eventType).setObject(persistentObject).done();
        }
        recorder.recordDelete(deleteRecord, deleteEvent);
    }

    private void create(final PersistentObject persistentObject, final String eventType) throws SRecorderException {
        final InsertRecord insertRecord = new InsertRecord(persistentObject);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(eventType, EventActionType.CREATED)) {
            final SEventBuilder eventBuilder = eventService.getEventBuilder();
            insertEvent = (SInsertEvent) eventBuilder.createInsertEvent(eventType).setObject(persistentObject).done();
        }
        recorder.recordInsert(insertRecord, insertEvent);
    }
}
