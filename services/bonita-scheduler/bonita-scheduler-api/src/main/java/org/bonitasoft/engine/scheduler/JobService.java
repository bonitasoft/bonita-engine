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
package org.bonitasoft.engine.scheduler;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.exception.failedJob.SFailedJobReadException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
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

/**
 * @author Celine Souchet
 * @since 6.1
 */
public interface JobService {

    String JOB_DESCRIPTOR = "JOB_DESCRIPTOR";

    String JOB_PARAMETER = "JOB_PARAMETER";

    String JOB_LOG = "JOB_LOG";

    /**
     * Create a new job descriptor for a specific tenant
     *
     * @param sJobDescriptor
     *        JobDescriptor to create
     * @param tenantId
     *        Identifier of tenant
     * @return The created jobDescriptor
     * @throws SJobDescriptorCreationException
     * @since 6.1
     */
    SJobDescriptor createJobDescriptor(SJobDescriptor sJobDescriptor, long tenantId) throws SJobDescriptorCreationException;

    /**
     * Delete the specified job descriptor
     *
     * @param id
     *        Identifier of job descriptor to delete
     * @throws SJobDescriptorReadException
     * @throws SJobDescriptorNotFoundException
     * @throws SJobDescriptorDeletionException
     * @since 6.1
     */
    void deleteJobDescriptor(long id) throws SJobDescriptorNotFoundException, SJobDescriptorReadException, SJobDescriptorDeletionException;

    /**
     * Delete the specified job descriptor
     *
     * @param sJobDescriptor
     *        JobDescriptor to delete
     * @throws SJobDescriptorDeletionException
     * @since 6.1
     */
    void deleteJobDescriptor(SJobDescriptor sJobDescriptor) throws SJobDescriptorDeletionException;

    /**
     * Delete all job descriptors for a specific tenant
     *
     * @throws SJobDescriptorDeletionException
     * @since 6.4
     */
    void deleteAllJobDescriptors() throws SJobDescriptorDeletionException;

    /**
     * Delete a job descriptor corresponding to the given job name
     *
     * @param jobName name of job we want the jobDsecriptor to be deleted
     * @since 6.3
     */
    void deleteJobDescriptorByJobName(String jobName) throws SJobDescriptorDeletionException;

    /**
     * Get a specific job descriptor
     *
     * @param id
     *        Identifier of job descriptor
     * @return Null if the job descriptor doesn't exist, else the {@link SJobDescriptor} corresponding to the identifier
     * @throws SJobDescriptorReadException
     * @since 6.1
     */
    SJobDescriptor getJobDescriptor(long id) throws SJobDescriptorReadException;

    /**
     * Get total number of job descriptors
     *
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return total number of job logs
     * @throws SBonitaReadException
     */
    long getNumberOfJobDescriptors(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all job descriptors according to specific criteria
     *
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return A list of SJobParameter objects
     * @throws SBonitaReadException
     * @since 6.1
     */
    List<SJobDescriptor> searchJobDescriptors(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Create new job parameters for a specific tenant
     *
     * @param parameters
     *        JobParameters to create
     * @param tenantId
     *        Identifier of tenant
     * @param jobDescriptorId
     *        Identifier of job descriptor
     * @return
     * @throws SJobParameterCreationException
     * @since 6.2
     */
    List<SJobParameter> createJobParameters(List<SJobParameter> parameters, long tenantId, long jobDescriptorId) throws SJobParameterCreationException;

    /**
     * Delete jobs parameters corresponding to tenant and job descriptor, if exist. After, create new job parameters for a specific tenant
     *
     * @param tenantId
     * @param jobDescriptorId
     * @param parameters
     * @return A list of new SJobParameter objects
     * @throws SJobParameterCreationException
     * @since 6.1
     */
    List<SJobParameter> setJobParameters(final long tenantId, long jobDescriptorId, List<SJobParameter> parameters) throws SJobParameterCreationException;

    /**
     * Create a new job parameter for a specific tenant
     *
     * @param sJobParameter
     *        JobParameter to create
     * @param tenantId
     *        Identifier of tenant
     * @param jobDescriptorId
     *        Identifier of job descriptor
     * @return
     * @throws SJobParameterCreationException
     * @since 6.2
     */
    SJobParameter createJobParameter(SJobParameter sJobParameter, long tenantId, long jobDescriptorId) throws SJobParameterCreationException;

    /**
     * Delete the specified job parameter
     *
     * @param id
     *        Identifier of job parameter to delete
     * @throws SJobParameterReadException
     * @throws SJobParameterNotFoundException
     * @throws SJobParameterDeletionException
     * @since 6.1
     */
    void deleteJobParameter(long id) throws SJobParameterNotFoundException, SJobParameterReadException, SJobParameterDeletionException;

    /**
     * Delete the specified job parameter
     *
     * @param sJobParameter
     *        JobParameter to delete
     * @throws SJobParameterDeletionException
     * @since 6.1
     */
    void deleteJobParameter(SJobParameter sJobParameter) throws SJobParameterDeletionException;

    /**
     * Get a specific job parameter
     *
     * @param id
     *        Identifier of job parameter
     * @return
     * @throws SJobParameterReadException
     * @throws SJobParameterNotFoundException
     * @since 6.1
     */
    SJobParameter getJobParameter(long id) throws SJobParameterNotFoundException, SJobParameterReadException;

    /**
     * Search all job parameters according to specific criteria
     *
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return A list of SJobParameter objects
     * @throws SBonitaReadException
     * @since 6.1
     */
    List<SJobParameter> searchJobParameters(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Create a new job log for a specific tenant
     *
     * @param sJobLog
     *        JobLog to create
     * @return
     * @throws SJobLogCreationException
     * @since 6.2
     */
    SJobLog createJobLog(SJobLog sJobLog) throws SJobLogCreationException;

    /**
     * Delete the specified job log
     *
     * @param id
     *        Identifier of job log to delete
     * @throws SBonitaReadException
     * @throws SJobLogDeletionException
     * @since 6.1
     */
    void deleteJobLog(long id) throws SJobLogDeletionException, SBonitaReadException;

    /**
     * Delete the specified job log
     *
     * @param sJobLog
     *        JobLog to delete
     * @throws SJobLogDeletionException
     * @since 6.1
     */
    void deleteJobLog(SJobLog sJobLog) throws SJobLogDeletionException;

    /**
     * Get a specific job log
     *
     * @param id
     *        Identifier of job log
     * @return
     * @throws SBonitaReadException
     * @since 6.1
     */
    SJobLog getJobLog(long id) throws SBonitaReadException;

    /**
     * Get total number of job logs
     *
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return total number of job logs
     * @throws SBonitaReadException
     */
    long getNumberOfJobLogs(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all job logs according to specific criteria
     *
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return A list of SJobLog objects
     * @throws SBonitaReadException
     * @since 6.1
     */
    List<SJobLog> searchJobLogs(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get list of failed jobs
     *
     * @param startIndex
     * @param maxResults
     * @return A list of SFailedJob objects
     * @throws SFailedJobReadException
     * @since 6.2
     */
    List<SFailedJob> getFailedJobs(int startIndex, int maxResults) throws SFailedJobReadException;

    /**
     * Update a {@link SJobLog}
     *
     * @param jobLog
     *        The log to update
     * @param descriptor
     * @since 6.4.0
     */
    void updateJobLog(SJobLog jobLog, EntityUpdateDescriptor descriptor) throws SJobLogUpdatingException;

    /**
     * Delete all {@link SJobLog} of a specific {@link SJobDescriptor}
     *
     * @param jobDescriptorId
     *        The identifier of the {@link SJobDescriptor}
     * @throws SBonitaReadException
     * @throws SJobLogDeletionException
     * @since 6.4.0
     */
    void deleteJobLogs(long jobDescriptorId) throws SJobLogDeletionException, SBonitaReadException;

    /**
     * Get all {@link SJobLog} of a specific {@link SJobDescriptor}
     *
     * @param jobDescriptorId
     *        The identifier of the {@link SJobDescriptor}
     * @param fromIndex
     *        The index of the first element of the list
     * @param maxResults
     *        The nulber max of elements of the list
     * @return A list of {@link SJobLog}
     * @throws SBonitaReadException
     * @since 6.4.0
     */
    List<SJobLog> getJobLogs(long jobDescriptorId, int fromIndex, int maxResults) throws SBonitaReadException;

}
