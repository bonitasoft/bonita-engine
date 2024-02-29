/**
 * Copyright (C) 2017 BonitaSoft S.A.
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

package org.bonitasoft.permissions

import com.fasterxml.jackson.databind.ObjectMapper

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException
import org.bonitasoft.engine.bpm.document.ArchivedDocumentNotFoundException
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
 *     <li>portal/documentDownload</li>
 *     <li>portal/downloadDocument</li>
 *     <li>portal/formsDocumentDownload</li>
 * </ul>
 *
 *
 *
 * @author Anthony Birembaut
 */
class DownloadDocumentPermissionRule implements PermissionRule {

    public static final String DOCUMENT_ID_PARAM = "document"

    public static final String CONTENT_STORAGE_ID_PARAM = "contentStorageId"

    public static final String FILE_NAME_PARAM = "fileName"

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId()

        if (apiCallContext.isGET()) {
            def processInstanceId = null
            def contentStorageId = apiCallContext.getParameters().get(CONTENT_STORAGE_ID_PARAM)
            def fileName = apiCallContext.getParameters().get(FILE_NAME_PARAM)
            def documentId = apiCallContext.getParameters().get(DOCUMENT_ID_PARAM)
            // Download servlets requires either contentStorageId + fileName parameters or a document parameter
            if (fileName != null && contentStorageId != null && contentStorageId.length > 0) {
                processInstanceId = getProcessInstanceIdFromContentStorageId(contentStorageId[0], apiAccessor, logger)
            } else if (documentId != null && documentId.length > 0) {
                try {
                    long longDocumentId = Long.valueOf(documentId[0])
                    processInstanceId = getProcessInstanceIdFromDocumentId(longDocumentId, apiAccessor, logger)
                } catch (NumberFormatException e) {
                    logger.debug("documentId " + documentId + " is not a number")
                    return false
                }
            }
            if (processInstanceId != -1l) {
                return checkInvolvement(processInstanceId, apiAccessor, currentUserId, logger)
            }
            return true
        }
        return false
    }

    private long getProcessInstanceIdFromDocumentId(long documentId, APIAccessor apiAccessor, Logger logger) {
        def processInstanceId = -1l
        def processAPI = apiAccessor.getProcessAPI()
        try {
            processInstanceId = processAPI.getDocument(documentId).getProcessInstanceId()
        } catch (DocumentNotFoundException dnfe) {
            try {
                processInstanceId = processAPI.getArchivedVersionOfProcessDocument(documentId).getProcessInstanceId()
            } catch (ArchivedDocumentNotFoundException e) {
                logger.debug("No document or archived document found with Id " + documentId)
            }
        }
        return processInstanceId
    }

    private long getProcessInstanceIdFromContentStorageId(String contentStorageIdStr, APIAccessor apiAccessor, Logger logger) {
        def processInstanceId = -1l
        def processAPI = apiAccessor.getProcessAPI()
        try {
            long contentStorageId = Long.valueOf(contentStorageIdStr)
            SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1)
            builder.filter(DocumentsSearchDescriptor.CONTENT_STORAGE_ID, contentStorageId)
            def documentSearchResult = processAPI.searchDocuments(builder.done())
            if (documentSearchResult.getCount() > 0) {
                processInstanceId = documentSearchResult.getResult().get(0).getProcessInstanceId()
            } else {
                def archivedDocumentSearchResult = processAPI.searchArchivedDocuments(builder.done())
                if (archivedDocumentSearchResult.getCount() > 0) {
                    processInstanceId = archivedDocumentSearchResult.getResult().get(0).getProcessInstanceId()
                }
            }
            if (processInstanceId == -1l) {
                logger.debug("No document or archived document found for contentStorageId " + contentStorageIdStr)
            }
        }
        catch (NumberFormatException e) {
            logger.debug("contentStorageId is not a number")
        }
        return processInstanceId
    }

    private boolean checkInvolvement(long processInstanceId, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        def processAPI = apiAccessor.getProcessAPI()
        return isInvolved(processAPI, currentUserId, processInstanceId, logger) ||
                isSupervisor(processAPI, currentUserId, processInstanceId, logger)
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
