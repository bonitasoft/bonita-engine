package org.bonitasoft.engine.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilder;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.service.TenantServiceAccessor;

public class AddJobCommand extends TenantCommand {

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final SchedulerService schedulerService = serviceAccessor.getSchedulerService();
        final Trigger trigger = new OneShotTrigger("OneShot", new Date());
        final SJobDescriptor jobDescriptor = schedulerService.getJobDescriptorBuilder()
                .createNewInstance(ThrowsExceptionJob.class.getName(), "ThowExceptionJob").setDescription("Throw an exception when 'throwException'=true")
                .done();
        final SJobParameterBuilder parameterBuilder = schedulerService.getJobParameterBuilder();
        Boolean throwException = Boolean.TRUE;
        final Serializable exception = parameters.get("throwException");
        if (exception != null) {
            throwException = (Boolean) exception;
        }
        final SJobParameter parameter = parameterBuilder.createNewInstance("throwException", throwException).done();
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
