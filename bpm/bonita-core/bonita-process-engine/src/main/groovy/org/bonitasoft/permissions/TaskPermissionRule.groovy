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
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeType
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
 *     <li>bpm/humanTask</li>
 *     <li>bpm/userTask</li>
 *     <li>bpm/archivedHumanTask</li>
 *     <li>bpm/archivedUserTask</li>
 *     <li>bpm/activity</li>
 *     <li>bpm/activityReplay</li>
 *     <li>bpm/archivedActivity</li>
 *     <li>bpm/task</li>
 *     <li>bpm/archivedTask</li>
 *     <li>bpm/flowNode</li>
 *     <li>bpm/archivedFlowNode</li>
 *     <li>bpm/manualTask</li>
 *     <li>bpm/archivedManualTask</li>
 *     <li>bpm/archivedTask</li>
 * </ul>
 *
 *
 * @author Baptiste Mesta
 */
class TaskPermissionRule implements PermissionRule {

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId()
        def userName = apiSession.getUserName()
        def processAPI = apiAccessor.getProcessAPI()
        def filters = apiCallContext.getFilters()
        try {
            if (apiCallContext.isGET()) {
                return checkGetMethod(apiCallContext, processAPI, logger, currentUserId, userName, filters)
            } else if (apiCallContext.isPUT() && apiCallContext.getResourceId() != null) {
                def assignUser = getAssignedUser(apiCallContext, apiAccessor, logger)
                return isTaskAccessibleByUser(processAPI, apiCallContext, logger, currentUserId, userName, assignUser)
            } else if (apiCallContext.isPOST()) {
                return checkPostMethod(apiCallContext, currentUserId, processAPI, userName, logger)
            }
        } catch (NotFoundException e) {
            logger.debug("flow node not found: is allowed")
            return true
        }
        return true
    }

    private User getAssignedUser(APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        if (apiCallContext.getBody() != null) {
            ObjectMapper mapper = new ObjectMapper()
            def map = mapper.readValue(apiCallContext.getBody(), Map.class)
            if (map != null) {
                def assignedId = map.get("assigned_id")
                if (assignedId != null && !assignedId.toString().isEmpty()) {
                    try {
                        def assignedUserId = Long.valueOf(assignedId.toString())
                        def identityAPI = apiAccessor.getIdentityAPI()
                        return identityAPI.getUser(assignedUserId)
                    } catch (Exception e) {
                        logger.debug("assigned Id is not a long or does not match an existing user")
                    }
                }
            }
        }
        return null
    }

    private boolean checkGetMethod(APICallContext apiCallContext, ProcessAPI processAPI, Logger logger, long currentUserId, String userName, Map<String, String> filters) {
        if (apiCallContext.getResourceId() != null) {
            return isTaskAccessibleByUser(processAPI, apiCallContext, logger, currentUserId, userName, null)
        } else if (hasFilter(currentUserId, filters, "assigned_id") || hasFilter(currentUserId, filters, "user_id") || hasFilter(currentUserId, filters, "hidden_user_id") || hasFilter(currentUserId, filters, "supervisor_id")) {
            logger.debug("FilterOnUser or FilterOnAssignUser")
            return true
        } else if (filters.containsKey("parentTaskId")) {
            def long parentTaskId = Long.parseLong(filters.get("parentTaskId"))
            try {
                return isTaskAccessible(processAPI, filters.get("parentTaskId"), currentUserId, userName, null, logger)
            } catch (NotFoundException e) {
                return isArchivedFlowNodeAccessible(processAPI, parentTaskId, currentUserId, userName, logger)
            }
        } else if (filters.containsKey("processId")) {
            def long processId = Long.valueOf(filters.get("processId"))
            return processAPI.isUserProcessSupervisor(processId, currentUserId)
        } else if (filters.containsKey("caseId") || filters.containsKey("parentCaseId")) {
            def long caseId = filters.containsKey("caseId") ? Long.parseLong(filters.get("caseId")) : Long.parseLong(filters.get("parentCaseId"))
            return processAPI.isUserProcessSupervisor(processAPI.getProcessInstance(caseId).getProcessDefinitionId(), currentUserId)
        } else {
            return false
        }
    }

    private boolean checkPostMethod(APICallContext apiCallContext, long currentUserId, ProcessAPI processAPI, String userName, Logger logger) {
        if ("manualTask".equals(apiCallContext.getResourceName())) {
            ObjectMapper mapper = new ObjectMapper()
            def map = mapper.readValue(apiCallContext.getBody(), Map.class)

            def string = map.get("parentTaskId").toString()
            if (string == null || string.isEmpty()) {
                return true
            }
            def parentTaskId = Long.valueOf(string)
            def flowNodeInstance = processAPI.getFlowNodeInstance(parentTaskId)
            return flowNodeInstance instanceof HumanTaskInstance && flowNodeInstance.getAssigneeId()
        }
        return false
    }

    private boolean hasFilter(long currentUserId, Map<String, String> filters, String assigned_id) {
        return String.valueOf(currentUserId).equals(filters.get(assigned_id))
    }

    protected boolean isTaskAccessibleByUser(ProcessAPI processAPI, APICallContext apiCallContext, Logger logger, long currentUserId, String username, User assignedUser) throws NotFoundException {
        if (apiCallContext.getResourceName().startsWith("archived")) {
            return isArchivedFlowNodeAccessible(processAPI, Long.valueOf(apiCallContext.getResourceId()), currentUserId, username, logger)
        } else {
            return isTaskAccessible(processAPI, apiCallContext.getResourceId(), currentUserId, username, assignedUser, logger)
        }
    }

    private boolean isArchivedFlowNodeAccessible(ProcessAPI processAPI, long taskId, long currentUserId, String username, Logger logger) throws NotFoundException {
        def archivedFlowNodeInstance = processAPI.getArchivedFlowNodeInstance(taskId)
        if (FlowNodeType.MANUAL_TASK.equals(archivedFlowNodeInstance.getType()) || FlowNodeType.USER_TASK.equals(archivedFlowNodeInstance.getType())) {
            if (currentUserId == archivedFlowNodeInstance.getExecutedBy()) {
                return true
            }
            //get the last flow node in journal
            if(archivedFlowNodeInstance.getExecutedBy() == 0){
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

    private boolean isTaskAccessible(ProcessAPI processAPI, String flowNodeIdAsString, long currentUserId, String username, User assignedUser, Logger logger) throws NotFoundException {
        def long flowNodeId = Long.valueOf(flowNodeIdAsString)
        def instance = processAPI.getFlowNodeInstance(flowNodeId)
        if (instance instanceof HumanTaskInstance) {
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
            if (instance instanceof ManualTaskInstance) {
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
                return isTaskAccessible(processAPI, flowNodeIdAsString, assignedUser.getId(), assignedUser.getUserName(), null, logger)
            }
            return true
        }
        return false
    }
}
