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
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user access only document on cases that he is involved in
 *
 * <ul>
 *     <li>bpm/document</li>
 *     <li>bpm/archivedDocument</li>
 *     <li>bpm/caseDocument</li>
 * </ul>
 *
 *
 *
 * @author Baptiste Mesta
 * @author Truc Nguyen
 */
class DocumentPermissionRule implements PermissionRule {

    public static final String CASE_ID = "caseId"
    public static final String ARCHIVED_CASE_ID = "archivedCaseId"

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        
        def resourceId = apiCallContext.getResourceId()
        if (resourceId != null) {
            return checkMethodWithResourceId(resourceId, apiAccessor, currentUserId)
        }
        
        if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId)
        } else if (apiCallContext.isPOST()) {
            return checkPostMethod(apiCallContext, apiAccessor, currentUserId)
        }
        
        return false
    }

    private boolean checkMethodWithResourceId(String resourceId, APIAccessor apiAccessor, long currentUserId) {
        def processAPI = apiAccessor.getProcessAPI()
        try {
            long documentId = Long.valueOf(resourceId)
            def processInstanceId = processAPI.getDocument(documentId).getProcessInstanceId()
            return isInvolved(processAPI, currentUserId, processInstanceId) ||
                    isSupervisor(processAPI, currentUserId, processInstanceId)
        }
        catch (NumberFormatException e) {
            return true
        }
    }
    
    private boolean checkPostMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId) {

        ObjectMapper mapper = new ObjectMapper();
        def map = mapper.readValue(apiCallContext.getBody(), Map.class)

        def processInstanceIdAsString = map.get(CASE_ID)
        if (processInstanceIdAsString == null || processInstanceIdAsString.toString().isEmpty()) {
            return true;
        }
        def processInstanceId = Long.valueOf(processInstanceIdAsString.toString())
        if (processInstanceId <= 0) {
            return true;
        }
        try {
            def processAPI = apiAccessor.getProcessAPI()
            def processDefinitionId = processAPI.getProcessInstance(processInstanceId).getProcessDefinitionId()
            return isInvolved(processAPI, currentUserId, processInstanceId) ||
                    processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
        } catch (NotFoundException e) {
            return true
        }
    }

    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId) {
        def filters = apiCallContext.getFilters()
        def processAPI = apiAccessor.getProcessAPI()
        
        long processInstanceId = -1
        long processDefinitionId = -1
        
        def archivedCaseIdAsString = filters.get(ARCHIVED_CASE_ID)
        if (archivedCaseIdAsString != null) {
            def archivedCaseId = Long.valueOf(archivedCaseIdAsString)
            processInstanceId = processAPI.getArchivedProcessInstance(archivedCaseId).getSourceObjectId()
            processDefinitionId = processAPI.getFinalArchivedProcessInstance(processInstanceId).getProcessDefinitionId()
        }
        else {
            def processInstanceIdAsString = filters.get(CASE_ID)
            if (processInstanceIdAsString != null) {
                processInstanceId = Long.valueOf(processInstanceIdAsString)
                processDefinitionId = processAPI.getProcessInstance(processInstanceId).getProcessDefinitionId()
            }
        }
        
        if (processInstanceId > 0 && processDefinitionId > 0) {
            return isInvolved(processAPI, currentUserId, processInstanceId) ||
                    processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
        }
        
        return false;
    }


    private boolean isInvolved(ProcessAPI processAPI, long currentUserId, long processInstanceId) {
        try {
            return processAPI.isInvolvedInProcessInstance(currentUserId, processInstanceId) || processAPI.isManagerOfUserInvolvedInProcessInstance(currentUserId, processInstanceId)
        } catch (BonitaException e) {
            return true
        }
    }
    
    private boolean isSupervisor(ProcessAPI processAPI, long currentUserId, long processInstanceId) {
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
