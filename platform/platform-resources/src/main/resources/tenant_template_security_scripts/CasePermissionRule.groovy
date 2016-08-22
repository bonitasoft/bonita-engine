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
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user access only cases that he is involved in and start cases that he can start
 *
 * <ul>
 *     <li>bpm/case</li>
 *     <li>bpm/archivedCase</li>
 * </ul>
 *
 *
 *
 * @author Baptiste Mesta
 * @author Anthony Birembaut
 */
class CasePermissionRule implements PermissionRule {


    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId, logger)
        } else if (apiCallContext.isPOST()) {
            return checkPostMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }
        return false
    }

    private boolean checkPostMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {

        ObjectMapper mapper = new ObjectMapper();
        def map = mapper.readValue(apiCallContext.getBody(), Map.class)

        def string = map.get("processDefinitionId")
        if (string == null || string.toString().isEmpty()) {
            return true;
        }
        def processDefinitionId = Long.valueOf(string.toString())
        if (processDefinitionId <= 0) {
            return true;
        }
        def processAPI = apiAccessor.getProcessAPI()
        def identityAPI = apiAccessor.getIdentityAPI()
        User user = identityAPI.getUser(currentUserId);
        SearchOptionsBuilder searchOptionBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionBuilder.filter(UserSearchDescriptor.USER_NAME, user.getUserName());
        SearchResult<User> listUsers = processAPI.searchUsersWhoCanStartProcessDefinition(processDefinitionId, searchOptionBuilder.done());
        logger.debug("RuleCase : nb Result [" + listUsers.getCount() + "] ?");
        def canStart = listUsers.getCount() == 1
        logger.debug("RuleCase : User allowed to start? " + canStart)
        return canStart
    }
    
    private boolean isInvolved(ProcessAPI processAPI, long currentUserId, long processInstanceId) {
        return processAPI.isInvolvedInProcessInstance(currentUserId, processInstanceId) || processAPI.isManagerOfUserInvolvedInProcessInstance(currentUserId, processInstanceId)
    }

    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def processAPI = apiAccessor.getProcessAPI()
        def filters = apiCallContext.getFilters()
        try {
            if (apiCallContext.getResourceId() != null) {
                def processDefinitionId;
                if (apiCallContext.getResourceName().startsWith("archived")) {
                    def archivedProcessInstanceId = Long.valueOf(apiCallContext.getResourceId())
                    def archivedProcessInstance = processAPI.getArchivedProcessInstance(archivedProcessInstanceId)
                    def processInstanceId = archivedProcessInstance.getSourceObjectId()
                    if (isInvolved(processAPI, currentUserId, processInstanceId)) {
                        return true;
                    }
                    processDefinitionId = archivedProcessInstance.getProcessDefinitionId()
                } else {
                    def processInstanceId = Long.valueOf(apiCallContext.getResourceId())
                    if (isInvolved(processAPI, currentUserId, processInstanceId)) {
                        return true;
                    }
                    processDefinitionId = processAPI.getProcessInstance(processInstanceId).getProcessDefinitionId()
                }
                logger.debug("RuleCase : allowed because get on process that user is involved in")
                return processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
            } else {
                def stringUserId = String.valueOf(currentUserId)
                if (stringUserId.equals(filters.get("started_by")) || stringUserId.equals(filters.get("user_id")) || stringUserId.equals(filters.get("supervisor_id"))) {
                    logger.debug("RuleCase : allowed because searching filters contains user id")
                    return true
                }
                if (filters.containsKey("processDefinitionId")) {
                    return processAPI.isUserProcessSupervisor(Long.valueOf(filters.get("processDefinitionId")), currentUserId)
                }
                if ("archivedCase".equals(apiCallContext.getResourceName()) && filters.containsKey("sourceObjectId")) {
                    def sourceCase = Long.valueOf(filters.get("sourceObjectId"))
                    final SearchOptionsBuilder opts = new SearchOptionsBuilder(0, 1);
                    opts.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, sourceCase);
                    def result = processAPI.searchArchivedProcessInstancesInvolvingUser(currentUserId, opts.done())
                    def archivedProcessInstance = processAPI.getFinalArchivedProcessInstance(sourceCase)
                    return result.getCount() == 1 || processAPI.isUserProcessSupervisor(archivedProcessInstance.getProcessDefinitionId(), currentUserId)
                }
            }
        } catch (BonitaException e) {
            //exception, allow user to have the 404 when the rest api will look for the resource:
            return true
        }
        return false;
    }
}
