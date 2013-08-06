/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActorsOfUserCanStartProcessDefinitions;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSUser;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Specific Command to access the actor Id list for a specific Process Definition and a specific actor ids.
 * The mandatory keys to set as parameters are "PROCESS_DEFINITION_ID_KEY" and "ACTOR_IDS_KEY".
 * 
 * @author Zhao Na
 */
public class IsAllowedToStartProcesses extends TenantCommand {

    private TenantServiceAccessor serviceAccessor;

    private static final String PROCESSDEFINITION_IDS_KEY = "PROCESSDEFINITION_IDS_KEY";

    private static final String USER_ID_KEY = "USER_ID_KEY";

    /**
     * @return true if the given user is allowed to start the given processDefinition respectively.
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.serviceAccessor = serviceAccessor;
        ActorMappingService actorMappingService = this.serviceAccessor.getActorMappingService();
        Map<Long, Boolean> resMap = new HashMap<Long, Boolean>();

        List<Long> processDefinitionIds = (List<Long>) parameters.get(PROCESSDEFINITION_IDS_KEY);
        if (processDefinitionIds == null) {
            throw new SCommandParameterizationException("Mandatory parameter " + PROCESSDEFINITION_IDS_KEY + " is missing or not convertible to List<Long>.");
        }
        long userId = (Long) parameters.get(USER_ID_KEY);
        if (userId == 0) {
            throw new SCommandParameterizationException("Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to Long.");
        }

        final IdentityService identityService = this.serviceAccessor.getIdentityService();
        final GetSUser getSUser = new GetSUser(identityService, userId);
        try {
            getSUser.execute();
        } catch (SBonitaException e) {
            throw new SCommandParameterizationException("No such user refer to this userId:" + userId, e);
        }
        getSUser.getResult();

        if (processDefinitionIds.size() > 0) {
            for (Long pid : processDefinitionIds) {
                final GetActorsOfUserCanStartProcessDefinitions checker = new GetActorsOfUserCanStartProcessDefinitions(actorMappingService, pid, userId);
                try {
                    checker.execute();;
                } catch (SBonitaException e) {
                    throw new SCommandExecutionException("No actor of user who can start the processDefinition with id:" + pid, e);
                }
                List<SActor> ckRes = checker.getResult();
                if (ckRes != null && ckRes.size() == 1) {
                    resMap.put(pid, true);
                } else {
                    resMap.put(pid, false);
                }
            }
        }
        return (Serializable) resMap;
    }
}
