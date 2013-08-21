/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.scheduler.JobDescriptorBuilder;
import org.bonitasoft.engine.scheduler.JobIdentifier;
import org.bonitasoft.engine.scheduler.JobParameterBuilder;
import org.bonitasoft.engine.scheduler.SJobDescriptor;
import org.bonitasoft.engine.scheduler.SJobParameter;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.Trigger;
import org.bonitasoft.engine.scheduler.builder.JobLogBuilder;
import org.bonitasoft.engine.scheduler.builder.JobParameterLogBuilder;
import org.bonitasoft.engine.scheduler.builder.SchedulerBuilderAccessor;
import org.bonitasoft.engine.scheduler.builder.SchedulerLogBuilder;
import org.bonitasoft.engine.scheduler.impl.model.JobDescriptorBuilderImpl;
import org.bonitasoft.engine.scheduler.impl.model.JobParameterBuilderImpl;
import org.bonitasoft.engine.scheduler.impl.model.SJobDescriptorImpl;
import org.bonitasoft.engine.scheduler.impl.model.SJobParameterImpl;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 */
public class SchedulerImpl implements SchedulerService {

    private final TechnicalLoggerService logger;

    // this recorder must not use async logging else we have an infinite loop.
    private final SchedulerBuilderAccessor builderAccessor;

    private final SchedulerExecutor schedulerExecutor;

    private final QueriableLoggerService queriableLogService;

    private final EventService eventService;

    private final SEvent schedulStarted;

    private final SEvent schedulStopped;

    private final SEvent jobFailed;

    private final SessionAccessor sessionAccessor;

    private final Recorder recorder;

    private final ReadPersistenceService readPersistenceService;

    private final TransactionService transactionService;

    private final JobTruster jobTruster;

    private final SessionService sessionService;

    /**
     * Create a new instance of scheduler service. Synchronous
     * QueriableLoggerService must be used to avoid an infinite loop.
     */
    public SchedulerImpl(final SchedulerExecutor schedulerExecutor, final SchedulerBuilderAccessor builderAccessor,
            final QueriableLoggerService queriableLogService, final TechnicalLoggerService logger, final EventService eventService, final Recorder recorder,
            final ReadPersistenceService readPersistenceService, final TransactionService transactionService, final SessionAccessor sessionAccessor,
            final SessionService sessionService, final JobTruster jobTruster) {
        this.builderAccessor = builderAccessor;
        this.schedulerExecutor = schedulerExecutor;
        this.queriableLogService = queriableLogService;
        this.logger = logger;
        this.readPersistenceService = readPersistenceService;
        this.sessionService = sessionService;
        this.jobTruster = jobTruster;
        schedulStarted = eventService.getEventBuilder().createNewInstance(SCHEDULER_STARTED).done();
        schedulStopped = eventService.getEventBuilder().createNewInstance(SCHEDULER_STOPPED).done();
        jobFailed = eventService.getEventBuilder().createNewInstance(JOB_FAILED).done();
        this.eventService = eventService;
        this.recorder = recorder;
        this.transactionService = transactionService;
        this.sessionAccessor = sessionAccessor;
        schedulerExecutor.setBOSSchedulerService(this);
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private JobLogBuilder getJobLogBuilder(final ActionType actionType, final String message) {
        final JobLogBuilder logBuilder = builderAccessor.getJobLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SchedulerLogBuilder getLogBuilder(final ActionType actionType, final String message, final String scope) {
        final SchedulerLogBuilder logBuilder = builderAccessor.getSchedulerLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.actionScope(scope);
        return logBuilder;
    }

    private JobParameterLogBuilder getJobParameterLogBuilder(final ActionType actionType, final String message, final SJobParameter jobParameter) {
        final JobParameterLogBuilder logBuilder = builderAccessor.getJobParameterLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.jogDescriptorId(jobParameter.getSJobDescriptorId());

        return logBuilder;
    }

    @Override
    public void schedule(final SJobDescriptor jobDescriptor, final Trigger trigger) throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "schedule"));
        }
        schedule(jobDescriptor, Collections.<SJobParameter> emptyList(), trigger);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "schedule"));
        }
    }

    @Override
    public void schedule(final SJobDescriptor jobDescriptor, final List<SJobParameter> parameters, final Trigger trigger) throws SSchedulerException {
        if (trigger == null) {
            throw new SSchedulerException("The trigger is null");
        }
        internalSchedule(jobDescriptor, parameters, trigger);
    }

    private void internalSchedule(final SJobDescriptor jobDescriptor, final List<SJobParameter> parameters, final Trigger trigger) throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "schedule"));
        }
        if (jobDescriptor == null) {
            throw new SSchedulerException("The job is null");
        } else if (jobDescriptor.getJobName() == null) {
            throw new SSchedulerException("The job name is null");
        }
        final SEventBuilder eventBuilder = eventService.getEventBuilder();
        final long tenantId = getTenantId();
        final SJobDescriptorImpl sJobDescriptorImpl = new SJobDescriptorImpl(jobDescriptor.getJobClassName(), jobDescriptor.getJobName(),
                jobDescriptor.getDescription());
        sJobDescriptorImpl.setTenantId(tenantId);// set the tenant manually on the object because it will be serialized

        final JobLogBuilder logBuilder = getJobLogBuilder(ActionType.CREATED, "Adding a new job descriptor");
        try {
            InsertRecord insertRecord;

            insertRecord = new InsertRecord(sJobDescriptorImpl);
            SInsertEvent insertEvent = (SInsertEvent) eventBuilder.createInsertEvent(JOB_DESCRIPTOR).setObject(sJobDescriptorImpl).done();
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(sJobDescriptorImpl.getId(), SQueriableLog.STATUS_OK, logBuilder, "internalSchedule");

            if (parameters != null) {
                for (final SJobParameter sJobParameter : parameters) {
                    final SJobParameterImpl sJobParameter2 = (SJobParameterImpl) getJobParameterBuilder()
                            .createNewInstance(sJobParameter.getKey(), sJobParameter.getValue()).setJobDescriptorId(sJobDescriptorImpl.getId()).done();
                    sJobParameter2.setTenantId(tenantId);// set the tenant manually on the object because it will be serialized
                    final JobParameterLogBuilder jobParameterLogBuilder = getJobParameterLogBuilder(ActionType.CREATED, "Adding a parameter to the job",
                            sJobParameter2);

                    insertRecord = new InsertRecord(sJobParameter2);
                    insertEvent = (SInsertEvent) eventBuilder.createInsertEvent(JOB_PARAMETER).setObject(sJobParameter2).done();
                    int status = SQueriableLog.STATUS_OK;
                    try {
                        recorder.recordInsert(insertRecord, insertEvent);
                    } catch (final SRecorderException e) {
                        status = SQueriableLog.STATUS_FAIL;
                        throw new SSchedulerException(e);
                    } finally {
                        initiateLogBuilder(sJobParameter2.getId(), status, jobParameterLogBuilder, "internalSchedule");
                    }
                }
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "schedule", e));
            }
            initiateLogBuilder(sJobDescriptorImpl.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "internalSchedule");
            throw new SSchedulerException(e);
        }

        final JobIdentifier jobIdentifier = new JobIdentifier(sJobDescriptorImpl.getId(), sJobDescriptorImpl.getTenantId(), sJobDescriptorImpl.getJobName());

        final SchedulerLogBuilder schedulingLogBuilder = getLogBuilder(ActionType.SCHEDULED, "Scheduled job with name " + jobDescriptor.getJobName(),
                jobDescriptor.getJobName());
        try {
            if (trigger == null) {
                schedulerExecutor.executeNow(jobIdentifier);
            } else {
                schedulerExecutor.schedule(jobIdentifier, trigger);
            }
            schedulingLogBuilder.actionStatus(SQueriableLog.STATUS_OK);
        } catch (final Throwable e) {
            schedulingLogBuilder.actionStatus(SQueriableLog.STATUS_FAIL);
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
            try {
                eventService.fireEvent(jobFailed);
            } catch (final FireEventException e1) {
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e1);
                }
            }
            throw new SSchedulerException(e);
        } finally {
            final SQueriableLog log = schedulingLogBuilder.done();
            if (queriableLogService.isLoggable(log.getActionType(), log.getSeverity())) {
                queriableLogService.log(this.getClass().getName(), "internalSchedule", log);
            }
        }

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "schedule"));
        }
    }

    private long getTenantId() throws SSchedulerException {
        final long tenantId;
        try {
            tenantId = sessionAccessor.getTenantId();
        } catch (final TenantIdNotSetException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "schedule", e));
            }
            throw new SSchedulerException(e);
        }
        return tenantId;
    }

    @Override
    public void executeNow(final SJobDescriptor jobDescriptor, final List<SJobParameter> parameters) throws SSchedulerException {
        internalSchedule(jobDescriptor, parameters, null);
    }

    @Override
    public boolean isStarted() throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "isStarted"));
        }
        final boolean isStarted = schedulerExecutor.isStarted();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "isStarted"));
        }
        return isStarted;
    }

    @Override
    public boolean isShutdown() throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "isShutdown"));
        }
        final boolean isShutdown = schedulerExecutor.isShutdown();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "isShutdown"));
        }
        return isShutdown;
    }

    @Override
    public void start() throws SSchedulerException, FireEventException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "start"));
        }
        schedulerExecutor.start();
        eventService.fireEvent(schedulStarted);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "start"));
        }
    }

    @Override
    public void shutdown() throws SSchedulerException, FireEventException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "shutdown"));
        }
        schedulerExecutor.shutdown();
        eventService.fireEvent(schedulStopped);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "shutdown"));
        }
    }

    @Override
    public boolean delete(final String jobName) throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "delete"));
        }
        final boolean delete = schedulerExecutor.delete(jobName);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "delete"));
        }
        return delete;
    }

    @Override
    public void deleteJobs() throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteJobs"));
        }
        schedulerExecutor.deleteJobs();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteJobs"));
        }
    }

    @Override
    public List<String> getJobs() throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getJobs"));
        }
        final List<String> list = schedulerExecutor.getJobs();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getJobs"));
        }
        return list;
    }

    @Override
    public List<String> getAllJobs() throws SSchedulerException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getAllJobs"));
        }
        final List<String> list = schedulerExecutor.getAllJobs();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getAllJobs"));
        }
        return list;
    }

    @Override
    public JobDescriptorBuilder getJobDescriptorBuilder() {
        return new JobDescriptorBuilderImpl();
    }

    /**
     * get the persisted job from the database
     * It opens a transaction!
     * 
     * @param jobIdentifier
     * @return the job
     * @throws SSchedulerException
     */
    public StatelessJob getPersistedJob(final JobIdentifier jobIdentifier) throws SSchedulerException {
        final boolean traceEnabled = logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE);
        final boolean errorEnabled = logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR);

        if (traceEnabled) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getPersistedJob"));
        }
        SSession session = null;
        try {
            session = sessionService.createSession(jobIdentifier.getTenantId(), "scheduler");
            sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());

            final Callable<JobWrapper> callable = buildGetPersistedJobCallable(jobIdentifier, traceEnabled);
            return transactionService.executeInTransaction(callable);
        } catch (final Exception e) {
            throw new SSchedulerException("The job class couldn't be instantiated", e);
        } finally {
            if (session != null) {
                try {
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(session.getId());
                } catch (final SSessionNotFoundException e) {
                    if (traceEnabled) {
                        logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getPersistedJob", e));
                    }
                    if (errorEnabled) {
                        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);// FIXME
                    }
                }
            }
        }
    }

    /**
     * @param jobIdentifier
     * @param traceEnabled
     * @return
     */
    private Callable<JobWrapper> buildGetPersistedJobCallable(final JobIdentifier jobIdentifier, final boolean traceEnabled) {
        return new Callable<JobWrapper>() {

            @Override
            public JobWrapper call() throws Exception {
                final SJobDescriptor sJobDescriptor = readPersistenceService.selectById(new SelectByIdDescriptor<SJobDescriptorImpl>(
                        "getSJobDescriptorImplById", SJobDescriptorImpl.class, jobIdentifier.getId()));
                // FIXME do something here if the job does not exist
                if (sJobDescriptor == null) {
                    return null;
                }
                final String jobClassName = sJobDescriptor.getJobClassName();
                final Class<?> jobClass = Class.forName(jobClassName);
                final StatelessJob statelessJob = (StatelessJob) jobClass.newInstance();

                final List<SJobParameterImpl> parameters = readPersistenceService.selectList(new SelectListDescriptor<SJobParameterImpl>(
                        "getSJobParameterImplByJobId", CollectionUtil.buildSimpleMap("jobDescriptorId", jobIdentifier.getId()), SJobParameterImpl.class));
                final HashMap<String, Serializable> parameterMap = new HashMap<String, Serializable>();
                for (final SJobParameterImpl sJobParameterImpl : parameters) {
                    parameterMap.put(sJobParameterImpl.getKey(), sJobParameterImpl.getValue());
                }
                statelessJob.setAttributes(parameterMap);
                final JobWrapper jobWrapper = new JobWrapper(jobIdentifier.getJobName(), queriableLogService, statelessJob, logger,
                        jobIdentifier.getTenantId(), eventService, jobTruster, sessionService, sessionAccessor);
                if (traceEnabled) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getPersistedJob"));
                }
                return jobWrapper;
            }
        };
    }

    @Override
    public JobParameterBuilder getJobParameterBuilder() {
        return new JobParameterBuilderImpl();
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

}
