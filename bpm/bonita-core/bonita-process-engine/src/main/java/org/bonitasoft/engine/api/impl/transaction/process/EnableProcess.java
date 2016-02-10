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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class EnableProcess implements TransactionContent {

    private final ProcessDefinitionService processDefinitionService;

    private final long processId;

    private final EventsHandler eventsHandler;

    private final TechnicalLoggerService logger;

    private final String userName;

    public EnableProcess(final ProcessDefinitionService processDefinitionService, final long processId, final EventsHandler eventsHandler,
            final TechnicalLoggerService logger, final String userName) {
        this.processDefinitionService = processDefinitionService;
        this.processId = processId;
        this.eventsHandler = eventsHandler;
        this.logger = logger;
        this.userName = userName;
    }

    @Override
    public void execute() throws SBonitaException {
        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processId);
        handleStartEvents(sProcessDefinition);
        processDefinitionService.enableProcessDeploymentInfo(processId);

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The user <" + userName + "> has enabled process <" + sProcessDefinition.getName()
                    + "> in version <" + sProcessDefinition.getVersion() + "> with id <" + sProcessDefinition.getId() + ">");
        }
    }

    private void handleStartEvents(final SProcessDefinition sProcessDefinition) throws SBonitaException {
        final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
        for (final SStartEventDefinition sStartEventDefinition : processContainer.getStartEvents()) {
            eventsHandler.handleCatchEvent(sProcessDefinition, sStartEventDefinition, null);
        }
    }

}
