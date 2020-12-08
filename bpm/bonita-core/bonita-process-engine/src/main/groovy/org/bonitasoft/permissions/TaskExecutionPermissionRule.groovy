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



import com.fasterxml.jackson.databind.ObjectMapper

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.ArchivedManualTaskInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user access only tasks that are assigned or pending to him
 *
 *
 * can be added to
 * <ul>
 *     <li>bpm/archivedUserTask/[id]/context</li>
 *     <li>bpm/userTask/[id]/context</li>
 *     <li>bpm/userTask/[id]/contract</li>
 *     <li>bpm/userTask/[id]/execution</li>
 * </ul>
 *
 *
 * @author Anthony Birembaut
 */
class TaskExecutionPermissionRule implements PermissionRule {

    private static final String USER_PARAM = "user"

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId()
        def userName = apiSession.getUserName()
        def processAPI = apiAccessor.getProcessAPI()
        def identityAPI = apiAccessor.getIdentityAPI()
        try {
            return isTaskAccessibleByUser(processAPI, identityAPI, apiCallContext, logger, currentUserId, userName)
        } catch (NotFoundException e) {
            logger.debug("flow node not found: is allowed")
            return true
        }
    }

    protected boolean isTaskAccessibleByUser(ProcessAPI processAPI, IdentityAPI identityAPI, APICallContext apiCallContext, Logger logger, long currentUserId, String username) throws NotFoundException {
        def taskInstanceId = getTaskInstanceId(apiCallContext)
        if (taskInstanceId <= 0) {
            return true
        }
        if (apiCallContext.getResourceName().startsWith("archived")) {
            return isArchivedFlowNodeAccessible(processAPI, taskInstanceId, currentUserId, username, logger)
        } else {
            def assignedUser = getAssignedUser(apiCallContext, identityAPI, logger)
            return isTaskAccessible(processAPI, taskInstanceId, currentUserId, username, assignedUser, logger)
        }
    }

    private User getAssignedUser(APICallContext apiCallContext, IdentityAPI identityAPI, Logger logger) {
        def assignedId = apiCallContext.getParameters().get(USER_PARAM)
        if (assignedId != null && assignedId.length > 0) {
            try {
                def assignedUserId = Long.valueOf(assignedId[0])
                return identityAPI.getUser(assignedUserId)
            } catch (Exception e) {
                logger.debug("user Id is not a long or does not match an existing user")
            }
        }
        return null
    }

    private boolean isArchivedFlowNodeAccessible(ProcessAPI processAPI, long taskId, long currentUserId, String username, Logger logger) throws NotFoundException {
        def archivedFlowNodeInstance = processAPI.getArchivedFlowNodeInstance(taskId)
        if (FlowNodeType.MANUAL_TASK.equals(archivedFlowNodeInstance.getType()) || FlowNodeType.USER_TASK.equals(archivedFlowNodeInstance.getType())) {
            if (currentUserId == archivedFlowNodeInstance.getExecutedBy()) {
                return true
            }
            //get the last flow node in journal
            if (archivedFlowNodeInstance.getExecutedBy() == 0){
                try {
                    def instance1 = processAPI.getHumanTaskInstance(archivedFlowNodeInstance.getSourceObjectId())
                    if(currentUserId == instance1.getAssigneeId()){
                        return true
                    }
                } catch(NotFoundException e){
                    //do nothing
                }
            }
        }
        if (FlowNodeType.MANUAL_TASK.equals(archivedFlowNodeInstance.getType())) {
            try {
                def parentTask = processAPI.getHumanTaskInstance(archivedFlowNodeInstance.getParentContainerId())
                if (parentTask.assigneeId > 0) {
                    if (parentTask.assigneeId == currentUserId) {
                        return true
                    }
                } else {
                    final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1)
                    builder.filter(UserSearchDescriptor.USER_NAME, username)
                    def searchResult = processAPI.searchUsersWhoCanExecutePendingHumanTask(parentTask.id, builder.done())
                    if (searchResult.getCount() == 1l) {
                        logger.debug("The parent task is pending for user")
                        return true
                    }
                }
            } catch (NotFoundException e) {
                try {
                    def instance = processAPI.getArchivedActivityInstance(archivedFlowNodeInstance.getParentContainerId())
                    //return false because it means the parent is not found, not the element itself
                    if ((FlowNodeType.MANUAL_TASK.equals(instance.getType()) || FlowNodeType.USER_TASK.equals(instance.getType())) &&  instance.assigneeId > 0) {
                        if (instance.assigneeId == currentUserId) {
                            return true
                        }
                    }
                } catch (NotFoundException e1) {
                    //return false because it means the parent is not found, not the element itself
                    return false
                }
            }
        }
        def processDefinitionId = archivedFlowNodeInstance.getProcessDefinitionId()
        return processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
    }

    private boolean isTaskAccessible(ProcessAPI processAPI, long flowNodeId, long currentUserId, String username, User assignedUser, Logger logger) throws NotFoundException {
        def instance = processAPI.getFlowNodeInstance(flowNodeId)
        if (FlowNodeType.MANUAL_TASK.equals(instance.getType()) || FlowNodeType.USER_TASK.equals(instance.getType())) {
            if (instance.assigneeId > 0) {
                if (instance.assigneeId == currentUserId) {
                    return true
                }
            } else {
                final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1)
                builder.filter(UserSearchDescriptor.USER_NAME, username)
                def searchResult = processAPI.searchUsersWhoCanExecutePendingHumanTask(flowNodeId, builder.done())
                if (searchResult.getCount() == 1l) {
                    logger.debug("The task is pending for user")
                    return true
                }
            }
            //we can access the task if we can access the parent of the subtask
            if (FlowNodeType.MANUAL_TASK.equals(instance.getType())) {
                try {

                    def parentTask = processAPI.getHumanTaskInstance(instance.getParentContainerId())
                    if (parentTask.assigneeId > 0) {
                        if (parentTask.assigneeId == currentUserId) {
                            return true
                        }
                    } else {
                        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1)
                        builder.filter(UserSearchDescriptor.USER_NAME, username)
                        def searchResult = processAPI.searchUsersWhoCanExecutePendingHumanTask(parentTask.id, builder.done())
                        if (searchResult.getCount() == 1l) {
                            logger.debug("The parent task is pending for user")
                            return true
                        }
                    }
                } catch (NotFoundException e) {
                    //return false because it means the parent is not found, not the element itself
                    return false
                }
            }
        }
        def processDefinitionId = instance.getProcessDefinitionId()
        if (processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)) {
            if (assignedUser != null){
                return isTaskAccessible(processAPI, flowNodeId, assignedUser.getId(), assignedUser.getUserName(), null, logger)
            }
            return true
        }
        return false
    }

    private long getTaskInstanceId(APICallContext apiCallContext) {
        def compoundResourceId = apiCallContext.getCompoundResourceId()
        if (compoundResourceId == null || compoundResourceId.isEmpty()) {
            return -1L
        }
        return Long.valueOf(compoundResourceId.get(0))
    }
}
