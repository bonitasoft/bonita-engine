/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
 *     <li>bpm/case/[id]/context</li>
 *     <li>bpm/archivedCase/[id]/context</li>
 * </ul>
 *
 *
 *
 * @author Anthony Birembaut
 */
class CaseContextPermissionRule implements PermissionRule {


    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        def processAPI = apiAccessor.getProcessAPI()
        try {
            def caseId = getCaseId(apiCallContext)
            if (caseId <= 0) {
                return true
            }
            // isInvolvedInProcessInstance() already checks the archived and non-archived involvement:
            def isInvolved = processAPI.isInvolvedInProcessInstance(currentUserId, caseId) || processAPI.isManagerOfUserInvolvedInProcessInstance(currentUserId, caseId)
            if (isInvolved) {
                return true;
            }
            def processDefinitionId;
            if (apiCallContext.getResourceName().startsWith("archived")) {
                processDefinitionId = processAPI.getArchivedProcessInstance(caseId).getProcessDefinitionId()
            } else {
                processDefinitionId = processAPI.getProcessInstance(caseId).getProcessDefinitionId()
            }
            return processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
        } catch (BonitaException e) {
            //exception, allow user to have the 404 when the rest api will look for the resource:
            return true
        }
    }
    
    private long getCaseId(APICallContext apiCallContext) {
        def compoundResourceId = apiCallContext.getCompoundResourceId()
        if (compoundResourceId == null || compoundResourceId.isEmpty()) {
            return -1L
        }
        return Long.valueOf(compoundResourceId.get(0))
    }
}
