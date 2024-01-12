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
package org.bonitasoft.engine.job;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.bonitasoft.engine.command.RuntimeCommand;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.service.ServiceAccessor;

public class AddJobCommand extends RuntimeCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final ServiceAccessor serviceAccessor)
            throws SCommandExecutionException {
        final SchedulerService schedulerService = serviceAccessor.getSchedulerService();
        final Trigger trigger = new OneShotTrigger("OneShot", new Date());
        final SJobDescriptor jobDescriptor = SJobDescriptor.builder()
                .jobClassName(ThrowsExceptionJob.class.getName())
                .jobName("ThrowsExceptionJob")
                .description("Throw an exception when 'throwException'=true")
                .build();
        try {
            schedulerService.schedule(jobDescriptor, trigger);
            return null;
        } catch (final SSchedulerException sse) {
            throw new SCommandExecutionException(sse);
        }
    }

}
