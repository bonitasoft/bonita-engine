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
 **/

import com.fasterxml.jackson.databind.ObjectMapper

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user access only comments on cases that he is involved in
 *
 * <ul>
 *     <li>bpm/comment</li>
 *     <li>bpm/archivedComment</li>
 * </ul>
 *
 *
 *
 * @author Baptiste Mesta
 */
class CommentPermissionRule implements PermissionRule {


    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId)
        } else if (apiCallContext.isPOST()) {
            return checkPostMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }
        return false
    }

    private boolean checkPostMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {

        ObjectMapper mapper = new ObjectMapper();
        def map = mapper.readValue(apiCallContext.getBody(), Map.class)

        def string = map.get("processInstanceId")
        if (string == null || string.toString().isEmpty()) {
            return true;
        }
        def processInstanceId = Long.valueOf(string.toString())
        if (processInstanceId <= 0) {
            return true;
        }
        def processAPI = apiAccessor.getProcessAPI()
        return isInvolved(processAPI, currentUserId, processInstanceId) || isSupervisor(processAPI, processInstanceId, currentUserId)
    }

    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId) {
        def filters = apiCallContext.getFilters()
        def stringUserId = String.valueOf(currentUserId)
        if (stringUserId.equals(filters.get("team_manager_id")) || stringUserId.equals(filters.get("user_id")) || stringUserId.equals(filters.get("supervisor_id"))) {
            return true
        }
        if (filters.containsKey("processInstanceId")) {
            def processInstanceId = Long.valueOf(filters.get("processInstanceId"))

            def processAPI = apiAccessor.getProcessAPI()
            return isInvolved(processAPI, currentUserId, processInstanceId) || isSupervisor(processAPI, processInstanceId, currentUserId)
        }
        return false
    }

    private boolean isInvolved(ProcessAPI processAPI, long currentUserId, long processInstanceId) {
        try {
            return processAPI.isInvolvedInProcessInstance(currentUserId, processInstanceId) || processAPI.isManagerOfUserInvolvedInProcessInstance(currentUserId, processInstanceId)
        } catch (BonitaException e) {
            return true
        }
    }

    private boolean isSupervisor(ProcessAPI processAPI, long processInstanceId, long currentUserId) {
        def processDefinitionId
        try {
            processDefinitionId = processAPI.getProcessInstance(processInstanceId).getProcessDefinitionId()
        } catch (NotFoundException e) {
            try {
                processDefinitionId = processAPI.getFinalArchivedProcessInstance(processInstanceId).getProcessDefinitionId()
            } catch (NotFoundException e1) {
                return true
            }
        }
        return processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
    }
}
