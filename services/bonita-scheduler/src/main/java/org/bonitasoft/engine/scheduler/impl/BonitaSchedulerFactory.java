/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 */
@Component
@ConditionalOnSingleCandidate(BonitaSchedulerFactory.class)
public class BonitaSchedulerFactory extends StdSchedulerFactory {

    private SchedulerServiceImpl schedulerService;

    public BonitaSchedulerFactory(@Qualifier("quartzProperties") Properties props) throws SchedulerException {
        super(props);
    }

    @Override
    public Scheduler getScheduler() throws SchedulerException {
        final Scheduler scheduler = super.getScheduler();
        scheduler.setJobFactory(new TransactionalSimpleJobFactory(schedulerService));
        return scheduler;
    }

    public void setBOSSchedulerService(final SchedulerServiceImpl schedulerService) {
        this.schedulerService = schedulerService;
    }

}
