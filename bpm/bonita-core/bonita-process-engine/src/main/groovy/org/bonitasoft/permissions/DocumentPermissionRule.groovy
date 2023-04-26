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
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor
import org.bonitasoft.engine.exception.BonitaException
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.search.SearchOptionsBuilder
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
        long currentUserId = apiSession.getUserId()

        def resourceId = apiCallContext.getResourceId()
        if (resourceId != null) {
            return checkMethodWithResourceId(resourceId, apiAccessor, currentUserId, logger)
        }

        if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId, logger)
        } else if (apiCallContext.isPOST()) {
            return checkPostMethod(apiCallContext, apiAccessor, currentUserId, logger)
        }

        return false
    }

    private boolean checkMethodWithResourceId(String resourceId, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def processAPI = apiAccessor.getProcessAPI()
        try {
            long documentId = Long.valueOf(resourceId)
            def processInstanceId = processAPI.getDocument(documentId).getProcessInstanceId()
            return isInvolved(processAPI, currentUserId, processInstanceId, logger) ||
                    isSupervisor(processAPI, currentUserId, processInstanceId, logger)
        }
        catch (NumberFormatException e) {
            logger.debug("documentId " + documentIdStr + " is not a number")
            return false
        }
    }

    private boolean checkPostMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {

        ObjectMapper mapper = new ObjectMapper()
        def map = mapper.readValue(apiCallContext.getBody(), Map.class)

        def processInstanceIdAsString = map.get(CASE_ID)
        if (processInstanceIdAsString == null || processInstanceIdAsString.toString().isEmpty()) {
            return true
        }
        def processInstanceId = Long.valueOf(processInstanceIdAsString.toString())
        if (processInstanceId <= 0) {
            return true
        }
        try {
            def processAPI = apiAccessor.getProcessAPI()
            def processDefinitionId = processAPI.getProcessInstance(processInstanceId).getProcessDefinitionId()
            return isInvolved(processAPI, currentUserId, processInstanceId, logger) ||
                    processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
        } catch (NotFoundException e) {
            logger.debug("Process instance of document not found.")
            return true
        }
    }

    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
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
            return isInvolved(processAPI, currentUserId, processInstanceId, logger) ||
                    processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
        }

        return false
    }


    private boolean isInvolved(ProcessAPI processAPI, long currentUserId, long processInstanceId, Logger logger) {
        try {
            return processAPI.isInvolvedInProcessInstance(currentUserId, processInstanceId) ||
                    processAPI.isManagerOfUserInvolvedInProcessInstance(currentUserId, processInstanceId) ||
                    hasPendingTaskInCurrentSubprocess(processAPI, currentUserId, processInstanceId, logger)
        } catch (BonitaException e) {
            logger.debug("Error checking if user is involved in process instance of document.", e)
            return false
        }
    }

    private boolean isSupervisor(ProcessAPI processAPI, long currentUserId, long processInstanceId, Logger logger) {
        def processDefinitionId
        try {
            processDefinitionId = processAPI.getProcessInstance(processInstanceId).getProcessDefinitionId()
        } catch (NotFoundException e) {
            try {
                processDefinitionId = processAPI.getFinalArchivedProcessInstance(processInstanceId).getProcessDefinitionId()
            } catch (NotFoundException e1) {
                logger.debug("Process instance of document not found.")
                return false
            }
        }
        return processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
    }

    private boolean hasPendingTaskInCurrentSubprocess(ProcessAPI processAPI, long currentUserId, long processInstanceIdAsSubprocess, Logger logger) {
        try {
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 0)
            builder.filter(HumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstanceIdAsSubprocess)
            def taskSearchResult = processAPI.searchMyAvailableHumanTasks(currentUserId, builder.done())
            return taskSearchResult.getCount() > 0
        } catch (BonitaException e) {
            logger.debug("Error checking if user is involved in process instance of document.", e)
            return false
        }
    }
}
