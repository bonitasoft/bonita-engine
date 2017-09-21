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
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user get and update a variable of a case only if he is the process owner
 *
 * <ul>
 *     <li>bpm/caseVariable</li>
 * </ul>
 *
 *
 *
 * @author Baptiste Mesta
 */
class CaseVariablePermissionRule implements PermissionRule {


    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        def resourceId = apiCallContext.getResourceId()
        def processAPI = apiAccessor.getProcessAPI()
        try {
            if ((apiCallContext.isPUT() || apiCallContext.isGET()) && resourceId != null) {
                // Resource format: <processInstanceId>/<caseVariableName>
                def caseId = Long.valueOf(resourceId.tokenize("/").first())
                def processInstance = processAPI.getProcessInstance(caseId)
                return processAPI.isUserProcessSupervisor(processInstance.getProcessDefinitionId(), currentUserId)
            }

            def filters = apiCallContext.getFilters()
            if (apiCallContext.isGET() && filters.containsKey("case_id")) {
                def caseId = Long.valueOf(filters.get("case_id"))
                def processInstance = processAPI.getProcessInstance(caseId)
                return processAPI.isUserProcessSupervisor(processInstance.getProcessDefinitionId(), currentUserId)
            }
            return false
        } catch (NotFoundException e) {
            return true
        }
    }
}
