/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.external.permission;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Specific Command to know if a user can start a specific process.
 * The mandatory keys to set as parameter are "USER_ID_KEY" and "PROCESS_INSTANCE_ID_KEY".
 * 
 * @author Celine Souchet
 */
public class CanStartProcessDefinition extends CommandWithParameters {

    private static final String USER_ID_KEY = "USER_ID_KEY";

    private static final String PROCESS_DEFINITION_ID_KEY = "PROCESS_DEFINITION_ID_KEY";

    /**
     * @return a Boolean :
     *         - true, if the user can start the process;
     *         - false, otherwise.
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final ActorMappingService actorMappingService = serviceAccessor.getActorMappingService();

        final long userId = getLongMandadoryParameter(parameters, USER_ID_KEY);
        final long processDefinitionId = getLongMandadoryParameter(parameters, PROCESS_DEFINITION_ID_KEY);

        try {
            return actorMappingService.canUserStartProcessDefinition(userId, processDefinitionId);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'Boolean CanStartProcessDefinition(long userId, long processInstanceId)'", e);
        }
    }

}
