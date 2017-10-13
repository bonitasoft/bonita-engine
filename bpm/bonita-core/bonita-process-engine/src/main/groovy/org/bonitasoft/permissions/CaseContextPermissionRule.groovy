/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.permissions

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.exception.BonitaException
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
 * @author Anthony Birembaut
 */
class CaseContextPermissionRule implements PermissionRule {

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId()
        def processAPI = apiAccessor.getProcessAPI()
        try {
            def caseId = getCaseId(apiCallContext)
            if (caseId <= 0) {
                return true
            }
            def originalCaseId
            if (apiCallContext.getResourceName().startsWith("archived")) {
                originalCaseId = processAPI.getArchivedProcessInstance(caseId).getSourceObjectId()
            } else {
                originalCaseId = caseId
            }
            // isInvolvedInProcessInstance() already checks the archived and non-archived involvement
            def isInvolved = processAPI.isInvolvedInProcessInstance(currentUserId, originalCaseId) || processAPI.isManagerOfUserInvolvedInProcessInstance(currentUserId, originalCaseId)
            if (isInvolved) {
                return true
            }
            def processDefinitionId
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
