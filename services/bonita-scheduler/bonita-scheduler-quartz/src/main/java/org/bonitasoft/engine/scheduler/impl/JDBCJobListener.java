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
 ** 
 * @since 6.1
 */
package org.bonitasoft.engine.scheduler.impl;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * 
 * @author Celine Souchet
 * 
 */
public class JDBCJobListener implements JobListener {

    public JDBCJobListener() {
    }

    @Override
    public String getName() {
        return "JDBCJobListener";
    }

    @SuppressWarnings("unused")
    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
    }

    @SuppressWarnings("unused")
    @Override
    public void jobExecutionVetoed(final JobExecutionContext context) {
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final JobDetail jobDetail = context.getJobDetail();
        if (jobException != null) {
            // Job failed

            // TODO : Store / Update jobLog

            final Object[] args = new Object[] {
                    jobDetail.getJobDataMap().getWrappedMap().get("jobId"), new java.util.Date(), jobException.getMessage()
            };
        } else {
            // Job completed

            // TODO : Remove jobDescriptor, jobParam, jobLog
        }
    }

}
