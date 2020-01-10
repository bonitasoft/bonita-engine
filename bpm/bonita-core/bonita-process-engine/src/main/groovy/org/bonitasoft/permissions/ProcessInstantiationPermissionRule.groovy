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
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor
import org.bonitasoft.engine.exception.BonitaException
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.identity.UserSearchDescriptor
import org.bonitasoft.engine.search.SearchOptionsBuilder
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user access only cases that he is involved in and start cases that he can start
 *
 * <ul>
 *     <li>bpm/process/[id]/contract</li>
 *     <li>bpm/process/[id]/instantiation</li>
 * </ul>
 *
 *
 *
 * @author Anthony Birembaut
 */
class ProcessInstantiationPermissionRule implements PermissionRule {

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId()
        def processDefinitionId = getProcessDefinitionId(apiCallContext)
        if (processDefinitionId <= 0) {
            return true
        }
        try {
            def processAPI = apiAccessor.getProcessAPI()
            def identityAPI = apiAccessor.getIdentityAPI()
            User user = identityAPI.getUser(currentUserId)
            SearchOptionsBuilder searchOptionBuilder = new SearchOptionsBuilder(0, 1)

            if(apiCallContext.getParameters().get("user") != null
            && apiCallContext.getParameters().get("user").length > 0) {
                if (!processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)) {
                    return false
                }
                searchOptionBuilder.filter(UserSearchDescriptor.ID, apiCallContext.getParameters().get("user")[0])
            } else {
                searchOptionBuilder.filter(UserSearchDescriptor.ID, user.getId())
            }

            SearchResult<User> listUsers = processAPI.searchUsersWhoCanStartProcessDefinition(processDefinitionId, searchOptionBuilder.done())
            logger.debug("RuleCase : nb Result [" + listUsers.getCount() + "] ?")
            def canStart = listUsers.getCount() == 1
            logger.debug("RuleCase : User allowed to start? " + canStart)
            return canStart
        } catch (NotFoundException e) {
            //exception, allow user to have the 404 when the rest api will look for the resource:
            return true
        }
    }

    private long getProcessDefinitionId(APICallContext apiCallContext) {
        def compoundResourceId = apiCallContext.getCompoundResourceId()
        if (compoundResourceId == null || compoundResourceId.isEmpty()) {
            return -1L
        }
        return Long.valueOf(compoundResourceId.get(0))
    }
}
