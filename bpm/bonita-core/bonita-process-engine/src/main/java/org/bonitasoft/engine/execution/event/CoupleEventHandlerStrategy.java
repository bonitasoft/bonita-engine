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
package org.bonitasoft.engine.execution.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingEventKeyProviderBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public abstract class CoupleEventHandlerStrategy extends EventHandlerStrategy {

    private final EventInstanceService eventInstanceService;

    private static final int MAX_NUMBER_OF_RESULTS = 100;

    public CoupleEventHandlerStrategy(final EventInstanceService eventInstanceService) {
        this.eventInstanceService = eventInstanceService;
    }

    @Override
    public void unregisterCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId, final SProcessInstance parentProcessIsnstance)
            throws SBonitaException {
        if (!eventDefinition.getEventTriggers().isEmpty()) {
            unregisterWaitingEvents(SWaitingEvent.class, subProcessId, parentProcessIsnstance);
        }
    }

    private QueryOptions getWaitingEventsQueryOptions(final Class<? extends SWaitingEvent> waitingEventClass, final long subProcessId,
            final SProcessInstance parentProcessInstance) {
        final OrderByOption orderByOption = new OrderByOption(waitingEventClass, BuilderFactory.get(SWaitingEventKeyProviderBuilderFactory.class).getIdKey(),
                OrderByType.ASC);
        final List<FilterOption> filters = getFilterForWaitingEventsToUnregister(waitingEventClass, subProcessId,
                parentProcessInstance);
        return new QueryOptions(0, MAX_NUMBER_OF_RESULTS, Collections.singletonList(orderByOption), filters, null);
    }

    private List<FilterOption> getFilterForWaitingEventsToUnregister(final Class<? extends SWaitingEvent> waitingEventClass, final long subProcessId,
            final SProcessInstance parentProcessInstance) {
        final SWaitingEventKeyProviderBuilderFactory waitingEventKeyProvider = BuilderFactory.get(SWaitingEventKeyProviderBuilderFactory.class);
        final List<FilterOption> filters = new ArrayList<FilterOption>(3);
        filters.add(new FilterOption(waitingEventClass, waitingEventKeyProvider.getSubProcessIdKey(), subProcessId));
        filters.add(new FilterOption(waitingEventClass, waitingEventKeyProvider.getParentProcessInstanceIdKey(), parentProcessInstance.getId()));
        filters.add(new FilterOption(waitingEventClass, waitingEventKeyProvider.getActiveKey(), true));
        return filters;
    }

    private <T extends SWaitingEvent> void unregisterWaitingEvents(final Class<T> waitingEventClass,
            final long subProcessId, final SProcessInstance parentProcessInstance) throws SBonitaReadException, SWaitingEventModificationException {
        final QueryOptions queryOptions = getWaitingEventsQueryOptions(waitingEventClass, subProcessId, parentProcessInstance);
        List<T> waitingEvents;
        do {
            waitingEvents = eventInstanceService.searchWaitingEvents(waitingEventClass, queryOptions);
            deleteWaitingEvents(waitingEvents);
        } while (waitingEvents.size() > 0);
    }

    private void deleteWaitingEvents(final List<? extends SWaitingEvent> waitingEvents) throws SWaitingEventModificationException {
        for (final SWaitingEvent sWaitingEvent : waitingEvents) {
            eventInstanceService.deleteWaitingEvent(sWaitingEvent);
        }
    }

    protected EventInstanceService getEventInstanceService() {
        return eventInstanceService;
    }

}
