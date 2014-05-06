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

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.api.impl.transaction.identity.GetUsersByManager;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.identity.model.SUser;
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
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();

        // Let's retrieve the list of all users of whom managerId is a manager:
        final TransactionContentWithResult<List<SUser>> getUsersByManager = new GetUsersByManager(serviceAccessor.getIdentityService(), managerId);
        getUsersByManager.execute();
        final List<SUser> users = getUsersByManager.getResult();

        // Let's construct the list of all user Ids:
        final List<Long> userIds = new ArrayList<Long>(users.size() + 1);
        for (final SUser sUser : users) {
            userIds.add(sUser.getId());
        }
        // Then finally add managerId to the list:
        userIds.add(managerId);

        final List<Long> processDefinitionIds = processDefinitionService.getProcessDefinitionIds(0, Integer.MAX_VALUE);
        final Map<Long, Set<Long>> map = new HashMap<Long, Set<Long>>(processDefinitionIds.size());

        // For each process definition:
        for (final Long processDefinition : processDefinitionIds) {
            final Set<Long> actorIdsForProcessDef = new HashSet<Long>();
            // for each user (manager included):
            for (final long userId : userIds) {
                final GetActors getActors = new GetActors(processDefinition, userId, serviceAccessor.getActorMappingService());
                getActors.execute();
                final List<Long> actorIds = getActors.getResult();
                actorIdsForProcessDef.addAll(actorIds);
            }
            // Let's fill in the final result:
            map.put(processDefinition, actorIdsForProcessDef);
        }

        return map;
    }

    /**
     * Transaction content class for ActorMappingService.getActors() method feature.
     * 
     * @author Emmanuel Duchastenier
     */
    class GetActors implements TransactionContentWithResult<List<Long>> {

        private final long processDefinitionId;

        private final long userId;

        private final ActorMappingService actorMappingService;

        private List<Long> actorIds = null;

        private GetActors(final long processDefinitionId, final long userId, final ActorMappingService actorMappingService) {
            this.processDefinitionId = processDefinitionId;
            this.userId = userId;
            this.actorMappingService = actorMappingService;
        }

        @Override
        public void execute() throws SBonitaException {
            final List<SActor> actors = actorMappingService.getActors(Collections.singleton(processDefinitionId), userId);
            actorIds = new ArrayList<Long>(actors.size());
            for (final SActor actor : actors) {
                actorIds.add(actor.getId());
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Long> getResult() {
            return (List<Long>) (actorIds != null ? actorIds : Collections.emptyList());
        }
    }

}
