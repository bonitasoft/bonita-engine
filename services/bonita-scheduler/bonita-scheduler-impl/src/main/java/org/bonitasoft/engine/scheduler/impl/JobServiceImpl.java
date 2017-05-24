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
package org.bonitasoft.engine.scheduler.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.exception.failedJob.SFailedJobReadException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogUpdatingException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterCreationException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterReadException;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.model.impl.SJobDescriptorImpl;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.bonitasoft.engine.scheduler.model.impl.SJobParameterImpl;
import org.bonitasoft.engine.scheduler.recorder.SelectDescriptorBuilder;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class JobServiceImpl implements JobService {

    private final EventService eventService;

    private final Recorder recorder;

    private final ReadPersistenceService readPersistenceService;

    private final TechnicalLoggerService logger;

    public JobServiceImpl(final EventService eventService, final Recorder recorder, final ReadPersistenceService readPersistenceService,
            final TechnicalLoggerService logger) {
        this.readPersistenceService = readPersistenceService;
        this.eventService = eventService;
        this.recorder = recorder;
        this.logger = logger;
    }

    @Override
    public SJobDescriptor createJobDescriptor(final SJobDescriptor sJobDescriptor, final long tenantId) throws SJobDescriptorCreationException {
        if (sJobDescriptor == null) {
            throw new IllegalArgumentException("The job descriptor is null");
        } else if (sJobDescriptor.getJobName() == null) {
            throw new IllegalArgumentException("The job name is null");
        }

        // Set the tenant manually on the object because it will be serialized
        final SJobDescriptorImpl sJobDescriptorToRecord = new SJobDescriptorImpl(sJobDescriptor.getJobClassName(), sJobDescriptor.getJobName(),
                sJobDescriptor.getDescription(), sJobDescriptor.disallowConcurrentExecution());
        sJobDescriptorToRecord.setTenantId(tenantId);

        try {
            create(sJobDescriptorToRecord, JOB_DESCRIPTOR);
        } catch (final SRecorderException sre) {
            throw new SJobDescriptorCreationException(sre);
        }
        return sJobDescriptorToRecord;
    }

    @Override
    public void deleteJobDescriptor(final long id) throws SJobDescriptorReadException, SJobDescriptorDeletionException {
        final SJobDescriptor sJobDescriptor = getJobDescriptor(id);
        if (sJobDescriptor == null) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("jobDescriptor with id");
                stringBuilder.append(id);
                stringBuilder.append(" already deleted, ignore it");
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, stringBuilder.toString());
            }
        } else {
            deleteJobDescriptor(sJobDescriptor);
        }
    }

    @Override
    public void deleteJobDescriptor(final SJobDescriptor sJobDescriptor) throws SJobDescriptorDeletionException {
        if (sJobDescriptor == null) {
            throw new IllegalArgumentException("The job descriptor is null");
        }
        try {
            delete(sJobDescriptor, JOB_DESCRIPTOR);
        } catch (final SBonitaException e) {
            throw new SJobDescriptorDeletionException(e);
        }
    }

    @Override
    public SJobDescriptor getJobDescriptor(final long id) throws SJobDescriptorReadException {
        try {
            final SJobDescriptor sJobDescriptor = readPersistenceService.selectById(SelectDescriptorBuilder.getElementById(SJobDescriptor.class,
                    "SJobDescriptor", id));
            return sJobDescriptor;
        } catch (final SBonitaReadException sbre) {
            throw new SJobDescriptorReadException(sbre);
        }
    }

    @Override
    public long getNumberOfJobDescriptors(final QueryOptions queryOptions) throws SBonitaReadException {
        return readPersistenceService.getNumberOfEntities(SJobDescriptor.class, queryOptions, null);
    }

    @Override
    public List<SJobDescriptor> searchJobDescriptors(final QueryOptions queryOptions) throws SBonitaReadException {
        return readPersistenceService.searchEntity(SJobDescriptor.class, queryOptions, null);
    }

    @Override
    public List<SJobParameter> createJobParameters(final List<SJobParameter> sJobParameters, final long tenantId, final long jobDescriptorId)
            throws SJobParameterCreationException {
        final List<SJobParameter> createdSJobParameters = new ArrayList<SJobParameter>();
        if (sJobParameters != null) {
            for (final SJobParameter sJobParameter : sJobParameters) {
                createdSJobParameters.add(createJobParameter(sJobParameter, tenantId, jobDescriptorId));
            }
        }
        return createdSJobParameters;
    }

    @Override
    public List<SJobParameter> setJobParameters(final long tenantId, final long jobDescriptorId, final List<SJobParameter> parameters)
            throws SJobParameterCreationException {
        deleteAllJobParameters(jobDescriptorId);
        return createJobParameters(parameters, tenantId, jobDescriptorId);
    }

    protected void deleteAllJobParameters(final long jobDescriptorId) throws SJobParameterCreationException {
        try {
            final int limit = 100;
            final List<FilterOption> filters = new ArrayList<FilterOption>(1);

            filters.add(new FilterOption(SJobParameter.class, "jobDescriptorId", jobDescriptorId));
            final List<OrderByOption> orderByOptions = Arrays.asList(new OrderByOption(SJobParameter.class, "id", OrderByType.ASC));
            final QueryOptions options = new QueryOptions(0, limit, orderByOptions, filters, null);
            List<SJobParameter> jobParameters = null;
            do {
                jobParameters = searchJobParameters(options);
                for (final SJobParameter jobParameter : jobParameters) {
                    deleteJobParameter(jobParameter);
                }
            } while (jobParameters.size() == limit);
        } catch (final SBonitaException sbe) {
            throw new SJobParameterCreationException(sbe);
        }
    }

    @Override
    public SJobParameter createJobParameter(final SJobParameter sJobParameter, final long tenantId, final long jobDescriptorId)
            throws SJobParameterCreationException {
        if (sJobParameter == null) {
            throw new IllegalArgumentException("The job descriptor is null");
        }

        // Set the tenant manually on the object because it will be serialized
        final SJobParameterImpl sJobParameterToRecord = (SJobParameterImpl) BuilderFactory.get(SJobParameterBuilderFactory.class)
                .createNewInstance(sJobParameter.getKey(), sJobParameter.getValue()).setJobDescriptorId(jobDescriptorId).done();
        sJobParameterToRecord.setTenantId(tenantId);

        try {
            create(sJobParameterToRecord, JOB_PARAMETER);
        } catch (final SRecorderException sre) {
            throw new SJobParameterCreationException(sre);
        }
        return sJobParameter;
    }

    @Override
    public void deleteJobParameter(final long id) throws SJobParameterNotFoundException, SJobParameterReadException, SJobParameterDeletionException {
        final SJobParameter sJobParameter = getJobParameter(id);
        deleteJobParameter(sJobParameter);
    }

    @Override
    public void deleteJobParameter(final SJobParameter sJobParameter) throws SJobParameterDeletionException {
        try {
            delete(sJobParameter, JOB_PARAMETER);
        } catch (final SBonitaException e) {
            throw new SJobParameterDeletionException(e);
        }
    }

    @Override
    public SJobParameter getJobParameter(final long id) throws SJobParameterNotFoundException, SJobParameterReadException {
        try {
            final SJobParameter sJobParameter = readPersistenceService.selectById(SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter",
                    id));
            if (sJobParameter == null) {
                throw new SJobParameterNotFoundException(id);
            }
            return sJobParameter;
        } catch (final SBonitaReadException sbre) {
            throw new SJobParameterReadException(sbre);
        }
    }

    @Override
    public List<SJobParameter> searchJobParameters(final QueryOptions queryOptions) throws SBonitaReadException {
        return readPersistenceService.searchEntity(SJobParameter.class, queryOptions, null);
    }

    @Override
    public SJobLog createJobLog(final SJobLog sJobLog) throws SJobLogCreationException {
        try {
            create(sJobLog, JOB_LOG);
        } catch (final SRecorderException sre) {
            throw new SJobLogCreationException(sre);
        }
        return sJobLog;
    }

    @Override
    public void deleteJobLog(final long id) throws SJobLogDeletionException, SBonitaReadException {
        final SJobLog sJobLog = getJobLog(id);
        if (sJobLog != null) {
            deleteJobLog(sJobLog);
        }
    }

    @Override
    public void deleteJobLog(final SJobLog sJobLog) throws SJobLogDeletionException {
        try {
            delete(sJobLog, JOB_LOG);
        } catch (final SBonitaException e) {
            throw new SJobLogDeletionException(e);
        }
    }

    @Override
    public void deleteJobLogs(final long jobDescriptorId) throws SJobLogDeletionException, SBonitaReadException {
        List<SJobLog> jobLogs = getJobLogs(jobDescriptorId, 0, 100);
        while (!jobLogs.isEmpty()) {
            deleteJobLogs(jobLogs);
            jobLogs = getJobLogs(jobDescriptorId, 0, 100);
        }
    }

    private void deleteJobLogs(final List<SJobLog> jobLogs) throws SJobLogDeletionException {
        for (final SJobLog sJobLog : jobLogs) {
            deleteJobLog(sJobLog);
        }
    }

    @Override
    public List<SJobLog> getJobLogs(final long jobDescriptorId, final int fromIndex, final int maxResults) throws SBonitaReadException {
        final FilterOption filter = new FilterOption(SJobLog.class, "jobDescriptorId", jobDescriptorId);
        final OrderByOption orderByOption = new OrderByOption(SJobLog.class, "jobDescriptorId", OrderByType.ASC);
        final QueryOptions options = new QueryOptions(fromIndex, maxResults, Arrays.asList(orderByOption), Arrays.asList(filter), null);
        return searchJobLogs(options);
    }

    @Override
    public SJobLog getJobLog(final long id) throws SBonitaReadException {
        return readPersistenceService.selectById(SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", id));
    }

    @Override
    public long getNumberOfJobLogs(final QueryOptions queryOptions) throws SBonitaReadException {
        return readPersistenceService.getNumberOfEntities(SJobLog.class, queryOptions, null);
    }

    @Override
    public List<SJobLog> searchJobLogs(final QueryOptions queryOptions) throws SBonitaReadException {
        return readPersistenceService.searchEntity(SJobLog.class, queryOptions, null);
    }

    private void delete(final PersistentObject persistentObject, final String eventType) throws SRecorderException {
        recorder.recordDelete(new DeleteRecord(persistentObject), eventType);
    }


    private void create(final PersistentObject persistentObject, final String eventType) throws SRecorderException {
        recorder.recordInsert(new InsertRecord(persistentObject), eventType);
    }

    @Override
    public List<SFailedJob> getFailedJobs(final int startIndex, final int maxResults) throws SFailedJobReadException {
        final QueryOptions queryOptions = new QueryOptions(startIndex, maxResults);
        try {
            return readPersistenceService.selectList(SelectDescriptorBuilder.getFailedJobs(queryOptions));
        } catch (final SBonitaReadException sbre) {
            throw new SFailedJobReadException(sbre);
        }
    }

    @Override
    public void deleteJobDescriptorByJobName(final String jobName) throws SJobDescriptorDeletionException {
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(new FilterOption(SJobDescriptor.class, "jobName", jobName));
        final List<OrderByOption> orders = Arrays.asList(new OrderByOption(SJobDescriptor.class, "id", OrderByType.ASC));
        final QueryOptions queryOptions = new QueryOptions(0, 1, orders, filters, null);
        try {
            final List<SJobDescriptor> jobDescriptors = searchJobDescriptors(queryOptions);
            if (!jobDescriptors.isEmpty()) {
                deleteJobDescriptor(jobDescriptors.get(0));
            }
        } catch (final SBonitaReadException e) {
            throw new SJobDescriptorDeletionException("Job " + jobName + " not found, can't delete corresponding job descriptor");
        }
    }

    @Override
    public void deleteAllJobDescriptors() throws SJobDescriptorDeletionException {
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        final QueryOptions queryOptions = new QueryOptions(0, 100, null, filters, null);
        try {
            final List<SJobDescriptor> jobDescriptors = searchJobDescriptors(queryOptions);
            for (final SJobDescriptor sJobDescriptor : jobDescriptors) {
                deleteJobDescriptor(sJobDescriptor);
            }
        } catch (final SBonitaReadException e) {
            throw new SJobDescriptorDeletionException(e);
        }
    }

    @Override
    public void updateJobLog(final SJobLog jobLog, final EntityUpdateDescriptor descriptor) throws SJobLogUpdatingException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(jobLog, descriptor), JOB_LOG);
        } catch (final SRecorderException e) {
            throw new SJobLogUpdatingException(e);
        }
    }

    @Override
    public void logJobError(final Exception jobException, final Long jobDescriptorId) throws SBonitaReadException, SJobLogUpdatingException,
            SJobLogCreationException, SJobDescriptorReadException {
        final List<SJobLog> jobLogs = getJobLogs(jobDescriptorId, 0, 1);
        if (!jobLogs.isEmpty()) {
            final SJobLog jobLog = jobLogs.get(0);
            final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
            final StringWriter exceptionWriter = new StringWriter();
            jobException.printStackTrace(new PrintWriter(exceptionWriter));
            descriptor.addField("lastMessage", exceptionWriter.toString());
            descriptor.addField("lastUpdateDate", System.currentTimeMillis());
            descriptor.addField("retryNumber", jobLog.getRetryNumber() + 1);
            updateJobLog(jobLog, descriptor);
        } else {
            createJobLog(jobException, jobDescriptorId);
        }
    }


    public void createJobLog(final Exception jobException, final Long jobDescriptorId) throws SJobLogCreationException, SJobDescriptorReadException {
        SJobDescriptor jobDescriptor = getJobDescriptor(jobDescriptorId);
        if (jobDescriptor != null) {
            final SJobLogImpl jobLog = new SJobLogImpl(jobDescriptorId);
            jobLog.setLastMessage(getStackTrace(jobException));
            jobLog.setRetryNumber(0L);
            jobLog.setLastUpdateDate(System.currentTimeMillis());
            createJobLog(jobLog);
        } else {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "Impossible to mark the job with id '" + jobDescriptorId
                        + "' as failed because no job was found for this identifier. It was probably removed just after its failure and before this action.");
            }
        }
    }

    private String getStackTrace(final Exception jobException) {
        final StringWriter exceptionWriter = new StringWriter();
        jobException.printStackTrace(new PrintWriter(exceptionWriter));
        return exceptionWriter.toString();
    }
}
