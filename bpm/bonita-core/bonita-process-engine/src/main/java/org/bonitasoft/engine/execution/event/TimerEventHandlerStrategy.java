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
package org.bonitasoft.engine.execution.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.job.JobNameBuilder;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.jobs.TriggerTimerEventJob;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger.MisfireRestartPolicy;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class TimerEventHandlerStrategy extends EventHandlerStrategy {

    private static final OperationsWithContext EMPTY = new OperationsWithContext(null, null);

    private final SchedulerService schedulerService;

    private final ExpressionResolverService expressionResolverService;

    private final EventInstanceService eventInstanceService;

    private final TechnicalLoggerService logger;

    public TimerEventHandlerStrategy(final ExpressionResolverService expressionResolverService,
            final SchedulerService schedulerService,
            final EventInstanceService eventInstanceService, final TechnicalLoggerService logger) {
        this.schedulerService = schedulerService;
        this.expressionResolverService = expressionResolverService;
        this.eventInstanceService = eventInstanceService;
        this.logger = logger;
    }

    @Override
    public void handleCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SCatchEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final String jobName = JobNameBuilder.getTimerEventJobName(processDefinition.getId(), eventDefinition,
                eventInstance);
        final SJobDescriptor jobDescriptor = getJobDescriptor(jobName);
        final List<SJobParameter> jobParameters = getJobParameters(processDefinition, eventDefinition, eventInstance);
        STimerEventTriggerDefinition timerEventTriggerDefinition = (STimerEventTriggerDefinition) sEventTriggerDefinition;
        final Object timerCondition = evaluateTimerCondition(timerEventTriggerDefinition, processDefinition.getId(),
                eventInstance != null ? eventInstance.getParentProcessInstanceId() : null);
        Trigger trigger = scheduleJob(timerEventTriggerDefinition, jobDescriptor, jobParameters, timerCondition);
        if (timerEventTriggerDefinition.getTimerType() != STimerType.CYCLE && eventInstance != null) {
            final STimerEventTriggerInstance sEventTriggerInstance = new STimerEventTriggerInstance(
                    eventInstance.getId(), eventInstance.getName(), trigger.getStartDate().getTime(),
                    trigger.getName());
            eventInstanceService.createTimerEventTriggerInstance(sEventTriggerInstance);
        }
    }

    protected Trigger getTrigger(final STimerEventTriggerDefinition timerTrigger, Object timerCondition)
            throws SBonitaException {
        Date startDate;
        Trigger trigger;
        switch (timerTrigger.getTimerType()) {
            case DURATION:
                startDate = new Date(System.currentTimeMillis() + (Long) timerCondition);
                trigger = new OneShotTrigger("OneShotTrigger" + UUID.randomUUID().getLeastSignificantBits(), startDate);
                break;
            case DATE:
                startDate = (Date) timerCondition;
                trigger = new OneShotTrigger("OneShotTrigger" + UUID.randomUUID().getLeastSignificantBits(), startDate);
                break;
            case CYCLE:
                startDate = new Date();
                trigger = new UnixCronTrigger("UnixCronTrigger" + UUID.randomUUID().getLeastSignificantBits(),
                        startDate, (String) timerCondition,
                        MisfireRestartPolicy.ALL);
                break;
            default:
                throw new IllegalStateException();
        }
        return trigger;
    }

    private Object evaluateTimerCondition(STimerEventTriggerDefinition timerTrigger, long processDefinitionId,
            Long processInstanceId)
            throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        final SExpressionContext expressionContext = getEvaluationContext(processDefinitionId, processInstanceId);
        final SExpression timerExpression = timerTrigger.getTimerExpression();
        final Object result = expressionResolverService.evaluate(timerExpression, expressionContext);
        if (result == null) {
            throw new SInvalidExpressionException("The duration cannot be null.", timerExpression.getName());
        }
        return result;
    }

    private SExpressionContext getEvaluationContext(long processDefinitionId, Long processInstanceId) {
        final SExpressionContext expressionContext;
        if (processInstanceId != null) {
            expressionContext = new SExpressionContext(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name(),
                    processDefinitionId);
        } else {
            expressionContext = new SExpressionContext();
            expressionContext.setProcessDefinitionId(processDefinitionId);
        }
        return expressionContext;
    }

    @Override
    public void handleThrowEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SThrowEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) {
    }

    @Override
    public OperationsWithContext getOperations(final SWaitingEvent waitingEvent, final Long triggeringElementID) {
        return EMPTY;
    }

    @Override
    public void handleEventSubProcess(final SProcessDefinition processDefinition,
            final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId,
            final SProcessInstance parentProcessInstance)
            throws SBonitaException {
        final String jobName = JobNameBuilder.getTimerEventJobName(processDefinition.getId(), eventDefinition,
                parentProcessInstance.getId(), subProcessId);
        final SJobDescriptor jobDescriptor = getJobDescriptor(jobName);
        final List<SJobParameter> jobParameters = getJobParameters(processDefinition, eventDefinition, null,
                subProcessId, parentProcessInstance);

        STimerEventTriggerDefinition timerEventTriggerDefinition = (STimerEventTriggerDefinition) sEventTriggerDefinition;
        final Object timerCondition = evaluateTimerCondition(timerEventTriggerDefinition, processDefinition.getId(),
                parentProcessInstance.getId());
        scheduleJob(timerEventTriggerDefinition, jobDescriptor, jobParameters, timerCondition);
    }

    private Trigger scheduleJob(final STimerEventTriggerDefinition sEventTriggerDefinition,
            final SJobDescriptor jobDescriptor,
            final List<SJobParameter> jobParameters, Object timerCondition)
            throws SBonitaException {
        final Trigger trigger = getTrigger(sEventTriggerDefinition, timerCondition);
        schedulerService.schedule(jobDescriptor, jobParameters, trigger);
        return trigger;
    }

    private List<SJobParameter> getJobParameters(final SProcessDefinition processDefinition,
            final SEventDefinition eventDefinition,
            final SCatchEventInstance eventInstance) {
        final List<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
        jobParameters.add(SJobParameter.builder()
                .key("processDefinitionId")
                .value(processDefinition.getId()).build());
        jobParameters.add(SJobParameter.builder()
                .key("containerType")
                .value(SFlowElementsContainerType.PROCESS.name())
                .build());
        jobParameters.add(SJobParameter.builder()
                .key("eventType")
                .value(eventDefinition.getType().name()).build());
        jobParameters.add(SJobParameter.builder()
                .key("targetSFlowNodeDefinitionId")
                .value(eventDefinition.getId())
                .build());
        if (SFlowNodeType.START_EVENT.equals(eventDefinition.getType())) {
            final SStartEventDefinition startEvent = (SStartEventDefinition) eventDefinition;
            jobParameters.add(SJobParameter.builder()
                    .key("isInterrupting")
                    .value(startEvent.isInterrupting()).build());
        }
        if (eventInstance != null) {
            jobParameters.add(SJobParameter.builder()
                    .key("flowNodeInstanceId")
                    .value(eventInstance.getId()).build());
        }
        return jobParameters;
    }

    private List<SJobParameter> getJobParameters(final SProcessDefinition processDefinition,
            final SEventDefinition eventDefinition,
            final SCatchEventInstance eventInstance, final long subProcessId,
            final SProcessInstance parentProcessInstance) {
        final List<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
        jobParameters.addAll(getJobParameters(processDefinition, eventDefinition, eventInstance));
        jobParameters.add(SJobParameter.builder()
                .key("subProcessId")
                .value(subProcessId).build());
        jobParameters.add(SJobParameter.builder()
                .key("processInstanceId")
                .value(parentProcessInstance.getId()).build());
        jobParameters.add(SJobParameter.builder()
                .key("rootProcessInstanceId")
                .value(parentProcessInstance.getRootProcessInstanceId()).build());
        return jobParameters;
    }

    private SJobDescriptor getJobDescriptor(final String jobName) {
        return SJobDescriptor.builder()
                .jobClassName(TriggerTimerEventJob.class.getName())
                .jobName(jobName)
                .build();
    }

    @Override
    public void unregisterCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId,
            final SProcessInstance parentProcessInstance)
            throws SBonitaException {
        final String jobName = JobNameBuilder.getTimerEventJobName(processDefinition.getId(), eventDefinition,
                parentProcessInstance.getId(), subProcessId);
        final boolean delete = schedulerService.delete(jobName);
        if (!delete) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                final StringBuilder stb = new StringBuilder();
                stb.append("No job found with name '");
                stb.append(jobName);
                stb.append("' when interrupting timer catch event named '");
                stb.append(eventDefinition.getName());
                stb.append(". In process definition [name = <");
                stb.append(processDefinition.getName());
                stb.append(">, version = <");
                stb.append(processDefinition.getVersion());
                stb.append(">]");
                stb.append("'. It was probably already triggered.");
                final String message = stb.toString();
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, message);
            }
        }

    }
}
