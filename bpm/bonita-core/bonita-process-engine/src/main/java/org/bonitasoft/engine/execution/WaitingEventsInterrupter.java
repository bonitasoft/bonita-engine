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

package org.bonitasoft.engine.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingEventKeyProviderBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.execution.job.JobNameBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;

/**
 * @author Elias Ricken de Medeiros
 */
public class WaitingEventsInterrupter {

    private final EventInstanceService eventInstanceService;
    private final SchedulerService schedulerService;
    private final TechnicalLoggerService logger;

    private static final int MAX_NUMBER_OF_RESULTS = 100;

    public WaitingEventsInterrupter(final EventInstanceService eventInstanceService, final SchedulerService schedulerService, final TechnicalLoggerService logger) {
        this.eventInstanceService = eventInstanceService;
        this.schedulerService = schedulerService;
        this.logger = logger;
    }

    public void interruptWaitingEvents(final SProcessDefinition processDefinition, final SCatchEventInstance catchEventInstance,
                                       final SCatchEventDefinition catchEventDef) throws SBonitaException {
        interruptTimerEvent(processDefinition, catchEventInstance, catchEventDef);
        // message, signal and error
        interruptWaitingEvents(catchEventInstance.getId(), catchEventDef);
    }

    private void interruptWaitingEvents(final long instanceId, final SCatchEventDefinition catchEventDef)
            throws SBonitaReadException, SWaitingEventModificationException {
        if (!catchEventDef.getEventTriggers().isEmpty()) {
            interruptWaitingEvents(instanceId, SWaitingEvent.class);
        }
    }

    public void interruptWaitingEvents(final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        if (flowNodeInstance instanceof SReceiveTaskInstance || flowNodeInstance instanceof SIntermediateCatchEventInstance
                || flowNodeInstance instanceof SBoundaryEventInstance) {
            interruptWaitingEvents(flowNodeInstance.getId(), SWaitingEvent.class);
        }
    }

    private <T extends SWaitingEvent> void interruptWaitingEvents(final long instanceId, final Class<T> waitingEventClass)
            throws SBonitaReadException, SWaitingEventModificationException {
        final QueryOptions queryOptions = getWaitingEventsQueryOptions(instanceId, waitingEventClass);
        final QueryOptions countOptions = getWaitingEventsCountOptions(instanceId, waitingEventClass);
        long count = 0;
        List<T> waitingEvents;
        do {
            waitingEvents = eventInstanceService.searchWaitingEvents(waitingEventClass, queryOptions);
            count = eventInstanceService.getNumberOfWaitingEvents(waitingEventClass, countOptions);
            deleteWaitingEvents(waitingEvents);
        } while (count > waitingEvents.size());
    }

    private void deleteWaitingEvents(final List<? extends SWaitingEvent> waitingEvents) throws SWaitingEventModificationException {
        for (final SWaitingEvent sWaitingEvent : waitingEvents) {
            eventInstanceService.deleteWaitingEvent(sWaitingEvent);
        }
    }

    private QueryOptions getWaitingEventsCountOptions(final long instanceId, final Class<? extends SWaitingEvent> waitingEventClass) {
        final List<FilterOption> filters = getFilterForWaitingEventsToInterrupt(instanceId, waitingEventClass);
        return new QueryOptions(filters, null);
    }

    private QueryOptions getWaitingEventsQueryOptions(final long instanceId, final Class<? extends SWaitingEvent> waitingEventClass) {
        final OrderByOption orderByOption = new OrderByOption(waitingEventClass, BuilderFactory.get(SWaitingEventKeyProviderBuilderFactory.class).getIdKey(),
                OrderByType.ASC);
        final List<FilterOption> filters = getFilterForWaitingEventsToInterrupt(instanceId, waitingEventClass);
        return new QueryOptions(0, MAX_NUMBER_OF_RESULTS, Collections.singletonList(orderByOption), filters, null);
    }

    private List<FilterOption> getFilterForWaitingEventsToInterrupt(final long instanceId, final Class<? extends SWaitingEvent> waitingEventClass) {
        final SWaitingEventKeyProviderBuilderFactory waitingEventKeyProvider = BuilderFactory.get(SWaitingEventKeyProviderBuilderFactory.class);
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(waitingEventClass, waitingEventKeyProvider.getFlowNodeInstanceIdKey(), instanceId));
        filters.add(new FilterOption(waitingEventClass, waitingEventKeyProvider.getActiveKey(), true));
        return filters;
    }

    private void interruptTimerEvent(final SProcessDefinition processDefinition, final SCatchEventInstance catchEventInstance,
                                     final SCatchEventDefinition catchEventDef) throws SSchedulerException {
        // FIXME to support multiple events change this code
        if (!catchEventDef.getTimerEventTriggerDefinitions().isEmpty()) {
            final String jobName = JobNameBuilder.getTimerEventJobName(processDefinition.getId(), catchEventDef, catchEventInstance);
            final boolean delete = schedulerService.delete(jobName);
            if (!delete) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "No job found with name '" + jobName
                            + "' when interrupting timer catch event named '" + catchEventDef.getName() + "' and id '" + catchEventInstance.getId()
                            + "'. It was probably already triggered.");
                }
            }
        }
    }

}
