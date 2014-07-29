/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class DeleteProcess extends DeleteArchivedProcessInstances {

    private final ProcessDefinitionService processDefinitionService;
    private final ActorMappingService actorMappingService;
    private final ClassLoaderService classLoaderService;

    public DeleteProcess(final TenantServiceAccessor tenantAccessor, final long processDefinitionId) {
        super(tenantAccessor, processDefinitionId);
        processDefinitionService = tenantAccessor.getProcessDefinitionService();
        actorMappingService = tenantAccessor.getActorMappingService();
        classLoaderService = tenantAccessor.getClassLoaderService();
    }

    @Override
    public void execute() throws SBonitaException {
        super.execute();
        actorMappingService.deleteActors(getProcessDefinitionId());
        try {
            processDefinitionService.delete(getProcessDefinitionId());
        } catch (final SProcessDefinitionNotFoundException spdnfe) {
            // ignore
        }
        classLoaderService.removeLocalClassLoader(ScopeType.PROCESS.name(), getProcessDefinitionId());
    }

}
