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
 * Let the user access and modify only himself
 *
 * can be added to
 * <ul>
 *     <li>identity/user</li>
 *     <li>identity/professionalcontactdata</li>
 *     <li>identity/personalcontactdata</li>
 * </ul>
 *
 * @author Baptiste Mesta
 */
class UserPermissionRule implements PermissionRule {


    @Override
    boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        APISession session = apiSession;
        long currentUserId = session.getUserId();
        if (apiCallContext.getResourceId() != null) {
            def resourceId = Long.valueOf(apiCallContext.getResourceId())
            if (resourceId.equals(currentUserId)) {
                return true
            }
            return false
        } else {
            if (apiCallContext.getQueryString().contains("d=professional_data") || apiCallContext.getQueryString().contains("d=personnal_data")) {
                return false
            }
            def filters = apiCallContext.getFilters()
            //search by task id for the do for
            if (filters.containsKey("task_id")) {
                def taskId = Long.valueOf(filters.get("task_id"))
                def processAPI = apiAccessor.getProcessAPI()
                try {
                    def flowNodeInstance = processAPI.getFlowNodeInstance(taskId)
                    return processAPI.isUserProcessSupervisor(flowNodeInstance.getProcessDefinitionId(), currentUserId)
                } catch (NotFoundException e) {
                    return true
                }
            }
            if (filters.containsKey("process_id")) {
                def processId = Long.valueOf(filters.get("process_id"))
                def processAPI = apiAccessor.getProcessAPI()
                return processAPI.isUserProcessSupervisor(processId, currentUserId)
            }
            return false
        }
    }
}
