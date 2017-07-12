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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Wraps a Bonita job.
 *
 * @author Matthieu Chaffotte
 * @author Baptsite Mesta
 * @author Celine Souchet
 */
public abstract class AbstractQuartzJob implements org.quartz.Job {

    private StatelessJob bosJob;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            if (bosJob != null) {
                bosJob.execute();
            }
        } catch (final SBonitaException e) {
            JobExecutionException jobExecutionException = new JobExecutionException(e);
            jobExecutionException.setUnscheduleFiringTrigger(true);//job log will be registered
            throw jobExecutionException;
        }
    }

    public StatelessJob getBosJob() {
        return bosJob;
    }

    public void setBosJob(final StatelessJob bosJob) {
        this.bosJob = bosJob;
    }

}
