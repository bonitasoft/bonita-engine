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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.execution.job.JobNameBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class DisableProcess implements TransactionContent {

    private final ProcessDefinitionService processDefinitionService;
    private final EventInstanceService eventInstanceService;
    private final long processDefinitionId;
    private final SchedulerService scheduler;
    private final TechnicalLoggerService logger;
    private final String username;

    public DisableProcess(final ProcessDefinitionService processDefinitionService, final long processId, final EventInstanceService eventInstanceService,
            final SchedulerService scheduler, final TechnicalLoggerService logger, final String username) {
        this.processDefinitionService = processDefinitionService;
        this.eventInstanceService = eventInstanceService;
        processDefinitionId = processId;
        this.scheduler = scheduler;
        this.logger = logger;
        this.username = username;
    }

    @Override
    public void execute() throws SBonitaException {
        processDefinitionService.disableProcessDeploymentInfo(processDefinitionId);
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        disableStartEvents(processDefinition);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The user <" + username + "> has disabled process <" + processDefinition.getName()
                    + "> in version <" + processDefinition.getVersion() + "> with id <" + processDefinition.getId() + ">");
        }
    }

    private void disableStartEvents(final SProcessDefinition processDefinition) throws SBonitaException {
        deleteWaitingEvents();
        deleteJobs(processDefinition);
    }

    private void deleteJobs(final SProcessDefinition processDefinition) throws SSchedulerException {
        final List<SStartEventDefinition> startEvents = processDefinition.getProcessContainer().getStartEvents();
        for (final SStartEventDefinition startEvent : startEvents) {
            if (!startEvent.getTimerEventTriggerDefinitions().isEmpty()) {
                scheduler.delete(JobNameBuilder.getTimerEventJobName(processDefinitionId, startEvent, null));
            }
        }
    }

    private void deleteWaitingEvents() throws SWaitingEventModificationException, SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        List<SWaitingEvent> waitingEvents = eventInstanceService.searchStartWaitingEvents(processDefinitionId, queryOptions);
        while (!waitingEvents.isEmpty()) {
            for (final SWaitingEvent startEvent : waitingEvents) {
                eventInstanceService.deleteWaitingEvent(startEvent);
            }
            waitingEvents = eventInstanceService.searchStartWaitingEvents(processDefinitionId, queryOptions);
        }
    }

}
