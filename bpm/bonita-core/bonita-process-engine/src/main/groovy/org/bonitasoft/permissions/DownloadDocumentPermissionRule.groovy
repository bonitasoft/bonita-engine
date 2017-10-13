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
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException
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

    public static final String DOCUMENT_ID_PARAM = "document";

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();

        if (apiCallContext.isGET()) {
            def documentId = apiCallContext.getParameters().get(DOCUMENT_ID_PARAM);
            if (documentId != null) {
                return checkMethodWithResourceId(documentId, apiAccessor, currentUserId)
            }
            return true;
        }
        
        return false
    }

    private boolean checkMethodWithResourceId(String resourceId, APIAccessor apiAccessor, long currentUserId) {
        def processAPI = apiAccessor.getProcessAPI()
        try {
            long documentId = Long.valueOf(resourceId)
            def processInstanceId = processAPI.getDocument(documentId).getProcessInstanceId()
            return isInvolved(processAPI, currentUserId, processInstanceId) ||
                    isSupervisor(processAPI, currentUserId, processInstanceId)
        }
        catch (NumberFormatException e) {
            return true
        }
    }


    private boolean isInvolved(ProcessAPI processAPI, long currentUserId, long processInstanceId) {
        try {
            return processAPI.isInvolvedInProcessInstance(currentUserId, processInstanceId) || processAPI.isManagerOfUserInvolvedInProcessInstance(currentUserId, processInstanceId)
        } catch (BonitaException e) {
            return true
        }
    }
    
    private boolean isSupervisor(ProcessAPI processAPI, long currentUserId, long processInstanceId) {
        def processDefinitionId
        try {
            processDefinitionId = processAPI.getProcessInstance(processInstanceId).getProcessDefinitionId()
        } catch (NotFoundException e) {
            try {
                processDefinitionId = processAPI.getFinalArchivedProcessInstance(processInstanceId).getProcessDefinitionId()
            } catch (NotFoundException e1) {
                return true
            }
        }
        return processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
    }
    
}
