/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.execution.job.JobNameBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class DisableProcess implements TransactionContent {

    private final ProcessDefinitionService processDefinitionService;

    private final EventInstanceService eventInstanceService;

    private final long processId;

    private final SchedulerService scheduler;

    private final TechnicalLoggerService logger;

    private final String userName;

    public DisableProcess(final ProcessDefinitionService processDefinitionService, final long processId, final EventInstanceService eventInstanceService,
            final SchedulerService scheduler, final TechnicalLoggerService logger, final String userName) {
        this.processDefinitionService = processDefinitionService;
        this.eventInstanceService = eventInstanceService;
        this.processId = processId;
        this.scheduler = scheduler;
        this.logger = logger;
        this.userName = userName;
    }

    @Override
    public void execute() throws SBonitaException {
        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processId);
        processDefinitionService.disableProcessDeploymentInfo(processId);
        disableStartEvents(sProcessDefinition);

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The user <" + userName + "> has disabled process <" +
                    sProcessDefinition.getName() + "> in version <" + sProcessDefinition.getVersion() + ">");
        }
    }

    private void disableStartEvents(final SProcessDefinition sProcessDefinition) throws SBonitaException {
        deleteWaitingEvents();
        deleteJobs(sProcessDefinition);
    }

    private void deleteJobs(final SProcessDefinition sProcessDefinition) throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException,
            SSchedulerException {
        final List<SStartEventDefinition> startEvents = sProcessDefinition.getProcessContainer().getStartEvents();
        for (final SStartEventDefinition startEvent : startEvents) {
            if (!startEvent.getTimerEventTriggerDefinitions().isEmpty()) {
                scheduler.delete(JobNameBuilder.getTimerEventJobName(processId, startEvent, null));
            }
        }
    }

    private void deleteWaitingEvents() throws SEventTriggerInstanceReadException, SWaitingEventModificationException {
        final List<SWaitingEvent> waitingEvents = eventInstanceService.getStartWaitingEvents(processId);
        for (final SWaitingEvent startEvent : waitingEvents) {
            eventInstanceService.deleteWaitingEvent(startEvent);
        }
    }

}
