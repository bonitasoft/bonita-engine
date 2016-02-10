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
package org.bonitasoft.engine.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.execution.work.WorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.JobParameter;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class BPMEventHandlingJob extends InternalJob {

    private static final long serialVersionUID = 8929044925208984537L;

    private int maxCouples = 1000;

    private transient EventInstanceService eventInstanceService;

    private transient WorkService workService;

    private transient UserTransactionService transactionService;

    private transient JobService jobService;

    private transient SchedulerService schedulerService;

    private transient TechnicalLoggerService loggerService;
    private ProcessDefinitionService processDefinitionService;

    @Override
    public String getName() {
        return "BPMEventHandling";
    }

    @Override
    public String getDescription() {
        return "Handle BPM events";
    }

    @Override
    public void execute() throws SJobExecutionException {
        try {
            final List<SMessageEventCouple> potentialMessageCouples = getMessageEventCouples();
            final List<SMessageEventCouple> uniqueCouples = getMessageUniqueCouples(potentialMessageCouples);
            executeUniqueMessageCouplesWork(uniqueCouples);
            if (potentialMessageCouples.size() == maxCouples) {
                rescheduleJob();
            }
        } catch (final Exception e) {
            throw new SJobExecutionException(e);
        }
    }

    private void executeUniqueMessageCouplesWork(final List<SMessageEventCouple> uniqueCouples) throws SBonitaException {
        for (final SMessageEventCouple couple : uniqueCouples) {
            final long messageInstanceId = couple.getMessageInstanceId();
            final long waitingMessageId = couple.getWaitingMessageId();
            final SBPMEventType waitingMessageEventType = couple.getWaitingMessageEventType();

            // Mark messages that will be treated as "treatment in progress":
            final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
            final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
            markMessageAsInProgress(messageInstance);

            //EVENT_SUB_PROCESS of type non-interrupted should be considered as well, as soon as we support them
            if (!SBPMEventType.START_EVENT.equals(waitingMessageEventType)) {
                markWaitingMessageAsInProgress(waitingMsg);
            }
            long processDefinitionId = waitingMsg.getProcessDefinitionId();
            SProcessDefinitionDeployInfo processDeploymentInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);
            workService.registerWork(WorkFactory.createExecuteMessageCoupleWork(messageInstance, waitingMsg));
        }
    }

    /**
     * From a list of couples that may contain duplicate waiting message candidates, select only one waiting message for each message instance: the first
     * matching waiting message is arbitrary chosen.
     * In the case of <code>SWaitingMessageEvent</code> of types {@link SBPMEventType#START_EVENT} or {@link SBPMEventType#EVENT_SUB_PROCESS}, it can be
     * selected several times to trigger multiple instances.
     *
     * @param potentialMessageCouples
     *        all the possible couples that match the potential correlation.
     * @return the reduced list of couple, where we insure that a unique message instance is associated with a unique waiting message.
     */
    protected List<SMessageEventCouple> getMessageUniqueCouples(List<SMessageEventCouple> potentialMessageCouples) throws SEventTriggerInstanceReadException {
        final List<Long> takenMessages = new ArrayList<Long>();
        final List<Long> takenWaitings = new ArrayList<Long>();
        final List<SMessageEventCouple> uniqueMessageCouples = new ArrayList<SMessageEventCouple>();

        for (final SMessageEventCouple couple : potentialMessageCouples) {
            final long messageInstanceId = couple.getMessageInstanceId();
            final long waitingMessageId = couple.getWaitingMessageId();
            final SBPMEventType waitingMessageEventType = couple.getWaitingMessageEventType();
            if (!takenMessages.contains(messageInstanceId) && !takenWaitings.contains(waitingMessageId)) {
                takenMessages.add(messageInstanceId);
                // Starting events and Starting event sub-processes must not be considered as taken if they appear several times
                //EVENT_SUB_PROCESS of type non-interrupted should be considered as well, as soon as we support them
                if (!SBPMEventType.START_EVENT.equals(waitingMessageEventType)) {
                    takenWaitings.add(waitingMessageId);
                }
                uniqueMessageCouples.add(couple);
            }
        }
        return uniqueMessageCouples;
    }

    List<SMessageEventCouple> getMessageEventCouples() throws SEventTriggerInstanceReadException {
        return eventInstanceService.getMessageEventCouples(0, maxCouples);
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        setAttributes(tenantServiceAccessor, attributes);
    }

    void setAttributes(final TenantServiceAccessor tenantServiceAccessor, final Map<String, Serializable> attributes) {
        eventInstanceService = tenantServiceAccessor.getEventInstanceService();
        processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        workService = tenantServiceAccessor.getWorkService();
        transactionService = tenantServiceAccessor.getUserTransactionService();
        jobService = tenantServiceAccessor.getJobService();
        schedulerService = tenantServiceAccessor.getSchedulerService();
        loggerService = tenantServiceAccessor.getTechnicalLoggerService();

        final Integer batchSize = (Integer) attributes.get(JobParameter.BATCH_SIZE.name());
        if (batchSize != null) {
            maxCouples = batchSize;
        }
    }

    private void markMessageAsInProgress(final SMessageInstance messageInstance) throws SMessageModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SMessageInstanceBuilderFactory.class).getHandledKey(), true);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    private void markWaitingMessageAsInProgress(final SWaitingMessageEvent waitingMsg) throws SWaitingEventModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).getProgressKey(),
                SWaitingMessageEventBuilderFactory.PROGRESS_IN_TREATMENT_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    private void rescheduleJob() throws Exception {
        final ExecuteAgainJobSynchronization jobSynchronization = new ExecuteAgainJobSynchronization(getName(), jobService, schedulerService, loggerService);
        transactionService.registerBonitaSynchronization(jobSynchronization);
    }

    protected int getMaxCouples() {
        return maxCouples;
    }

}
