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
import org.bonitasoft.engine.exception.NotFoundException
import org.bonitasoft.engine.session.APISession

/**
 *
 * Let a user add an actorMember only if he is process owner
 *
 * <ul>
 *     <li>bpm/actorMember</li>
 *     <li>bpm/delegation</li>
 * </ul>
 *
 *
 *
 * @author Baptiste Mesta
 */
class ActorMemberPermissionRule implements PermissionRule {

    public static final String ACTOR_ID = "actor_id"

    @Override
    public boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        long currentUserId = apiSession.getUserId();
        if (apiCallContext.isPOST()) {
            return checkPostMethod(apiCallContext, apiAccessor, currentUserId, logger)
        } else if (apiCallContext.isGET()) {
            return checkGetMethod(apiCallContext, apiAccessor, currentUserId, logger)
        } else if (apiCallContext.isDELETE()) {
            //TODO unable to find an actor member with the API!
            return false
        }
        //it's ok to read
        return true
    }

    private boolean checkPostMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {

        ObjectMapper mapper = new ObjectMapper();
        def list = mapper.readValue(apiCallContext.getBody(), List.class)

        for (int i = 0; i < list.size(); i++) {
            def object = list.get(i)

            def get = object.get(ACTOR_ID)
            if(get == null){
                continue
            }
            def actorId = Long.valueOf(get.toString())
            if (actorId <= 0) {
                continue
            }
            def processAPI = apiAccessor.getProcessAPI()
            try {
                def actor = processAPI.getActor(actorId)
                def processDefinitionId = actor.getProcessDefinitionId()
                if (!processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)) {
                    return false
                }
            } catch (NotFoundException e) {
                return true
            }
        }
        return true
    }

    private boolean checkGetMethod(APICallContext apiCallContext, APIAccessor apiAccessor, long currentUserId, Logger logger) {
        try {
            def filters = apiCallContext.getFilters()
            if (filters.containsKey(ACTOR_ID)) {
                def processAPI = apiAccessor.getProcessAPI()
                def actor = processAPI.getActor(Long.parseLong(filters.get(ACTOR_ID)))
                def processDefinitionId = actor.getProcessDefinitionId()
                return processAPI.isUserProcessSupervisor(processDefinitionId, currentUserId)
            }
            return true
        } catch (NotFoundException e) {
            return true
        }
    }
}
