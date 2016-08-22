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




import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let the user do get only on processes he deployed or that he supervised
 *
 *
 * can be added to
 * <ul>
 *     <li>bpm/process</li>
 * </ul>
 *
 *
 *
 * @author Baptiste Mesta
 */
class ProcessPermissionRule implements PermissionRule {


    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }
        if (apiCallContext.isPUT()) {
            return checkPutMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }
        return false
    }


    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def processAPI = apiAccessor.getProcessAPI()
        def filters = apiCallContext.getFilters()
        def resourceIds = apiCallContext.getCompoundResourceId()
        if (!resourceIds.isEmpty()) {
            def processId = Long.parseLong(resourceIds.get(0))
            def processDefinition = processAPI.getProcessDeploymentInfo(processId);
            def deployedByUser = processDefinition.getDeployedBy() == currentUserId
            if(deployedByUser){
                logger.debug("deployed by the current user")
                return true;
            }
            def canStart = processAPI.searchProcessDeploymentInfosCanBeStartedBy(currentUserId, new SearchOptionsBuilder(0, 1).filter(ProcessDeploymentInfoSearchDescriptor.PROCESS_ID, processDefinition.getProcessId()).done())
            if(canStart.getCount()==1){
                logger.debug("can start process, so can get")
                return true
            }
            def isSupervisor = processAPI.isUserProcessSupervisor(processId, currentUserId)
            if(isSupervisor){
                logger.debug("is supervisor of the process")
                return true
            }
            return false
        } else {
            def stringUserId = String.valueOf(currentUserId)
            if (stringUserId.equals(filters.get("team_manager_id")) || stringUserId.equals(filters.get("supervisor_id")) || stringUserId.equals(filters.get("user_id"))) {
                logger.debug("allowed because searching filters contains user id")
                return true
            }
        }
        return false;
    }
    private boolean checkPutMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def resourceIds = apiCallContext.getCompoundResourceId()
        if (!resourceIds.isEmpty()) {
            def processId = Long.parseLong(resourceIds.get(0))
            def processAPI = apiAccessor.getProcessAPI()
            def isSupervisor = processAPI.isUserProcessSupervisor(processId, currentUserId)
            if(isSupervisor){
                logger.debug("is supervisor of the process")
                return true
            }
            return false
        }
        return true
    }
}
