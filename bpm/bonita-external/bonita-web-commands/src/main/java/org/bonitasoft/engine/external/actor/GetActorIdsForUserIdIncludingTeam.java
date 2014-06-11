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
package org.bonitasoft.engine.external.actor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Specific Command to access the actor Id list for a specific Process Definition and a specific user id.
 * The mandatory key to set as parameter is "USER_ID_KEY".
 * 
 * @author Emmanuel Duchastenier
 */
public class GetActorIdsForUserIdIncludingTeam extends TenantCommand {

    private static final String USER_ID_KEY = "USER_ID_KEY";

    private TenantServiceAccessor serviceAccessor;

    /**
     * @return a Set<Long> representing the matching actor Ids
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {

        this.serviceAccessor = serviceAccessor;

        final Serializable sUserId = parameters.get(USER_ID_KEY);
        long userId;
        try {
            // Cast will fail if value is null (not in the Map parameter), or if value cannot be cast to Long:
            userId = (Long) sUserId;
        } catch (final Exception e) {
            throw new SCommandParameterizationException("Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to long.");
        }
        try {
            return (Serializable) getActorIdsForUserIdIncludingTeam(userId);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'Map<Long, List<Long>> getActorIdsForUserIdIncludingTeam(long userId)'", e);
        }
    }

    private Map<Long, Set<Long>> getActorIdsForUserIdIncludingTeam(final long managerId) throws SBonitaException {
        final Map<Long, Set<Long>> map = new HashMap<Long, Set<Long>>();

        // Let's retrieve the list of all users of whom managerId is a manager:
        int index = 0;
        List<SUser> users = get100UsersByManager(managerId, index);
        while (!users.isEmpty()) {
            getActorIdsByProcessDefinitionId(managerId, map, users);
            index++;
            users = get100UsersByManager(managerId, index);
        }

        return map;
    }

    private List<SUser> get100UsersByManager(final long managerId, int index) throws SIdentityException {
        return serviceAccessor.getIdentityService().getUsersByManager(managerId, 100 * index, 100);
    }

    private void getActorIdsByProcessDefinitionId(final long managerId, final Map<Long, Set<Long>> map, final List<SUser> users)
            throws SProcessDefinitionReadException, SBonitaReadException {
        int index = 0;
        List<Long> processDefinitionIds = get100ProcessDefinitionIds(index);
        // For each process definition:
        while (!processDefinitionIds.isEmpty()) {
            for (final Long processDefinitionId : processDefinitionIds) {
                final Set<Long> actorIdsForProcessDef = new HashSet<Long>();
                // for each user (manager included):
                for (final SUser sUser : users) {
                    actorIdsForProcessDef.addAll(getActors(processDefinitionId, sUser.getId()));
                }

                // Idem for the manager
                actorIdsForProcessDef.addAll(getActors(processDefinitionId, managerId));

                // Let's fill in the final result:
                map.put(processDefinitionId, actorIdsForProcessDef);
            }
            index++;
            processDefinitionIds = get100ProcessDefinitionIds(index);
        }
    }

    private List<Long> get100ProcessDefinitionIds(final int index) throws SProcessDefinitionReadException {
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        return processDefinitionService.getProcessDefinitionIds(100 * index, 100);
    }

    private List<Long> getActors(Long processDefinitionId, long userId) throws SBonitaReadException {
        final List<SActor> actors = serviceAccessor.getActorMappingService().getActors(Collections.singleton(processDefinitionId), userId);
        final List<Long> actorIds = new ArrayList<Long>(actors.size());
        for (final SActor actor : actors) {
            actorIds.add(actor.getId());
        }
        return actorIds;
    }

}
