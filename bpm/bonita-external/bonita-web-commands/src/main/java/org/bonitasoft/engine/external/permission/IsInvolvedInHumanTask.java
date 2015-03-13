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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Specific Command to know if a user is involved in a specific human task.
 * The mandatory keys to set as parameter are "USER_ID_KEY" and "HUMAN_TASK_INSTANCE_ID_KEY".
 *
 * @author Celine Souchet
 */
public class IsInvolvedInHumanTask extends CommandWithParameters {

    protected static final String USER_ID_KEY = "USER_ID_KEY";

    protected static final String HUMAN_TASK_INSTANCE_ID_KEY = "HUMAN_TASK_INSTANCE_ID_KEY";

    /**
     * @return a Boolean :
     *         - true, if the user having USER_ID is involved in t human task;
     *         - false, otherwise.
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final long userId = getLongMandadoryParameter(parameters, USER_ID_KEY);
        final long humanTaskInstanceId = getLongMandadoryParameter(parameters, HUMAN_TASK_INSTANCE_ID_KEY);

        try {
            return isInvolvedInHumanTask(userId, humanTaskInstanceId, serviceAccessor);
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException("Error executing command 'Boolean IsInvolvedInHumanTask(long userId, long humanTaskInstanceId)'", e);
        }
    }

    private Boolean isInvolvedInHumanTask(final long userId, final long humanTaskInstanceId, final TenantServiceAccessor serviceAccessor)
            throws SActivityInstanceNotFoundException, SActorNotFoundException, SBonitaReadException {
        final ActorMappingService actorMappingService = serviceAccessor.getActorMappingService();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();

        long actorId;
        long assigneeId;
        long processDefinitionId;
        try {
            final SHumanTaskInstance humanTaskInstance = activityInstanceService.getHumanTaskInstance(humanTaskInstanceId);
            actorId = humanTaskInstance.getActorId();
            assigneeId = humanTaskInstance.getAssigneeId();
            processDefinitionId = humanTaskInstance.getProcessDefinitionId();
        } catch (final SActivityInstanceNotFoundException e) {
            final SAActivityInstance archivedActivityInstance = activityInstanceService.getMostRecentArchivedActivityInstance(humanTaskInstanceId);
            if (archivedActivityInstance instanceof SAHumanTaskInstance) {
                final SAHumanTaskInstance saHumanTaskInstance = (SAHumanTaskInstance) archivedActivityInstance;
                actorId = saHumanTaskInstance.getActorId();
                assigneeId = saHumanTaskInstance.getAssigneeId();
                processDefinitionId = saHumanTaskInstance.getProcessDefinitionId();
            } else {
                throw new SActivityInstanceNotFoundException(humanTaskInstanceId);
            }
        }
        if (assigneeId >= 0) {
            //check if the user is the assigned user
            return userId == assigneeId;
        } else {
            //if the task is not assigned check if the user is mapped to the actor of the task
            return isMappedToActor(userId, actorId, actorMappingService, processDefinitionId);
        }
    }

    private boolean isMappedToActor(long userId, long actorId, ActorMappingService actorMappingService, long processDefinitionId) throws SBonitaReadException,
            SActorNotFoundException {
        List<SActor> actors = actorMappingService.getActors(Collections.singleton(processDefinitionId), userId);
        for (SActor actor : actors) {
            if (actor.getId() == actorId) {
                return true;
            }
        }
        return false;
    }

}
