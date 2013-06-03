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

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;

/**
 * @author Baptiste Mesta
 */
public class DeleteProcess extends DeleteArchivedProcessInstances {

    private final ProcessDefinitionService processDefinitionService;

    private final SProcessDefinition serverProcessDefinition;

    private final ActorMappingService actorMappingService;

    public DeleteProcess(final ProcessDefinitionService processDefinitionService, final SProcessDefinition serverProcessDefinition,
            final ProcessInstanceService processInstanceService, final ArchiveService archiveService, final ActorMappingService actorMappingService) {
        super(processInstanceService, serverProcessDefinition.getId(), archiveService);
        this.processDefinitionService = processDefinitionService;
        this.serverProcessDefinition = serverProcessDefinition;
        this.actorMappingService = actorMappingService;
    }

    @Override
    public void execute() throws SBonitaException {
        super.execute();
        actorMappingService.deleteActors(serverProcessDefinition.getId());
        processDefinitionService.delete(serverProcessDefinition.getId());
    }

}
