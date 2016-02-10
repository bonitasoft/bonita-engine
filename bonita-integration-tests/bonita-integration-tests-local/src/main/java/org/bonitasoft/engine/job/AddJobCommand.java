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
package org.bonitasoft.engine.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.service.TenantServiceAccessor;

public class AddJobCommand extends TenantCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandExecutionException {
        final SchedulerService schedulerService = serviceAccessor.getSchedulerService();
        final Trigger trigger = new OneShotTrigger("OneShot", new Date());
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(ThrowsExceptionJob.class.getName(), "ThrowsExceptionJob").setDescription("Throw an exception when 'throwException'=true")
                .done();
        Boolean throwException = Boolean.TRUE;
        final Serializable exception = parameters.get("throwException");
        if (exception != null) {
            throwException = (Boolean) exception;
        }
        final SJobParameter parameter = BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("throwException", throwException).done();
        final List<SJobParameter> params = new ArrayList<SJobParameter>(2);
        params.add(parameter);
        try {
            schedulerService.schedule(jobDescriptor, trigger);
            return null;
        } catch (final SSchedulerException sse) {
            throw new SCommandExecutionException(sse);
        }
    }

}
