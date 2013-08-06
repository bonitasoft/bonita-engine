/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class BPMEventHandlingJob extends InternalJob implements Serializable {

    private static final long serialVersionUID = 8929044925208984537L;

    private transient EventInstanceService eventInstanceService;

    private transient BPMInstanceBuilders instanceBuilders;

    private WorkService workService;

    @Override
    public String getName() {
        return "BPMEventHandlingJob";
    }

    @Override
    public String getDescription() {
        return "Handle BPM events";
    }

    @Override
    public void execute() throws JobExecutionException, FireEventException {
        try {
            final List<SMessageEventCouple> potentialMessageCouples = eventInstanceService.getMessageEventCouples();

            //
            final List<SMessageEventCouple> uniqueCouples = makeMessageUniqueCouples(potentialMessageCouples);

            for (final SMessageEventCouple couple : uniqueCouples) {
                final SMessageInstance messageInstance = couple.getMessageInstance();
                final SWaitingMessageEvent waitingMessage = couple.getWaitingMessage();

                // Mark messages that will be treated as "treatment in progress":
                markMessageAsInProgress(messageInstance);
                markWaitingMessageAsInProgress(waitingMessage);
                workService.registerWork(new ExecuteMessageCoupleWork(messageInstance.getId(), waitingMessage.getId()));
            }
        } catch (final SBonitaException e) {
            throw new JobExecutionException(e);
        }
    }

    /**
     * From a list of couples that may contain duplicate waiting message candidates, select only one waiting message for each message instance: the first
     * matching waiting message is arbitrary chosen.
     * 
     * @param messageCouples
     *            all the possible couples that match the potential correlation.
     * @return the reduced list of couple, where we insure that a unique message instance is associated with a unique waiting message.
     */
    protected List<SMessageEventCouple> makeMessageUniqueCouples(final List<SMessageEventCouple> messageCouples) {
        final List<Long> takenMessages = new ArrayList<Long>(messageCouples.size());
        final List<Long> takenWaitings = new ArrayList<Long>(messageCouples.size());
        final List<SMessageEventCouple> pairs = new ArrayList<SMessageEventCouple>();
        for (final SMessageEventCouple couple : messageCouples) {
            final SMessageInstance messageInstance = couple.getMessageInstance();
            final SWaitingMessageEvent waitingMessage = couple.getWaitingMessage();
            if (!takenMessages.contains(messageInstance.getId()) && !takenWaitings.contains(waitingMessage.getId())) {
                takenMessages.add(messageInstance.getId());
                takenWaitings.add(waitingMessage.getId());
                pairs.add(couple);
            }
        }
        return pairs;
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        eventInstanceService = getTenantServiceAccessor().getEventInstanceService();
        instanceBuilders = getTenantServiceAccessor().getBPMInstanceBuilders();
        workService = getTenantServiceAccessor().getWorkService();
    }

    private void markMessageAsInProgress(final SMessageInstance messageInstanceToUpdate) throws SMessageModificationException,
            SMessageInstanceNotFoundException, SMessageInstanceReadException {
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceToUpdate.getId());
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(instanceBuilders.getSMessageInstanceBuilder().getHandledKey(), true);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    private void markWaitingMessageAsInProgress(final SWaitingMessageEvent waitingMessage) throws SWaitingEventModificationException,
            SWaitingEventNotFoundException, SWaitingEventReadException {
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessage.getId());
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(instanceBuilders.getSWaitingMessageEventBuilder().getProgressKey(), SWaitingMessageEventBuilder.PROGRESS_IN_TREATMENT_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

}
