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
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user manage process connectors and parameters only if he is process supervisor
 *
 * <ul>
 *     <li>bpm/processConnector</li>
 *     <li>bpm/processParameter</li>
 * </ul>
 *
 *
 *
 * @author Anthony Birembaut
 */
class ProcessConfigurationPermissionRule implements PermissionRule {

    public static final String PROCESS_ID = "process_id"

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId()
        if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId, logger)
        } else if (apiCallContext.isPUT()) {
            return checkPutMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }
        return true
    }

    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def resourceIds = apiCallContext.getCompoundResourceId()
        if (resourceIds.isEmpty()) {
            def filters = apiCallContext.getFilters()
            if(filters.containsKey(PROCESS_ID)){
                def processAPI = apiAccessor.getProcessAPI()
                return processAPI.isUserProcessSupervisor(Long.valueOf(filters.get(PROCESS_ID)),currentUserId)
            }
            return false
        } else {
            return isProcessOwnerOfTheProcess(apiAccessor, resourceIds, currentUserId)
        }
    }

    private boolean checkPutMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def resourceIds = apiCallContext.getCompoundResourceId()
        return isProcessOwnerOfTheProcess(apiAccessor, resourceIds, currentUserId)
    }

    private isProcessOwnerOfTheProcess(APIAccessor apiAccessor, List<String> resourceIds, long currentUserId) {
        def processAPI = apiAccessor.getProcessAPI()
        def processID = Long.parseLong(resourceIds.get(0))
        return processAPI.isUserProcessSupervisor(processID, currentUserId)
    }
}
