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

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.events.model.SFireEventException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;

/**
 * Interface of a scheduled job. A job is classified using a name and a group name. A job has a unique name and group name. It
 * fires the following events :
 * <ul>
 * <li>JOB_EXECUTING = "JOB_EXECUTING"</li>
 * <li>JOB_COMPLETED = "JOB_COMPLETED"</li>
 * </ul>
 * 
 * @author Matthieu Chaffotte
 */
public interface StatelessJob extends Serializable {

    String JOB_EXECUTING = "JOB_EXECUTING";

    String JOB_COMPLETED = "JOB_COMPLETED";

    String JOB_DESCRIPTOR_ID = "JOB_DESCRIPTOR_ID";

    /**
     * Gets the job name.
     * 
     * @return the job name
     * @since 6.0
     */
    String getName();

    /**
     * Gets the description of the job.
     * 
     * @return the job description
     * @since 6.0
     */
    String getDescription();

    /**
     * Execute the content of the job.
     * 
     * @throws SJobExecutionException
     *             if an exception occurs
     * @throws SFireEventException
     * @since 6.0
     */
    void execute() throws SJobExecutionException, SFireEventException;

    /**
     * This method is called by the scheduler service before the execution of the job
     * 
     * @param attributes
     *            key is the name of the attribute
     *            value is the value of the attribute
     * @throws SJobConfigurationException
     * @since 6.0
     */
    void setAttributes(Map<String, Serializable> attributes) throws SJobConfigurationException;

}
