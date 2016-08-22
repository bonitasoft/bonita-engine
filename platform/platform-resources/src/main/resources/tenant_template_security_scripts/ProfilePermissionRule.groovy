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
import org.bonitasoft.engine.profile.ProfileCriterion
import org.bonitasoft.engine.session.APISession

/**
 *
 * Secure profile related resources
 *
 * can be added to
 * <ul>
 *     <li>portal/profile</li>
 *     <li>userXP/profile</li>
 * </ul>
 * @author Baptiste Mesta
 */
class ProfilePermissionRule  implements PermissionRule {

    @Override
    boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
        if(apiCallContext.isGET()){
            if(apiCallContext.getResourceId() != null){
                def profileId = Long.valueOf(apiCallContext.getResourceId())
                def processAPI = apiAccessor.getProfileAPI()

                def index = 0
                def profile
                def list = []
                while ((list = processAPI.getProfilesForUser(apiSession.getUserId(),index,100,ProfileCriterion.ID_ASC)).size() == 100 && (profile = list.find{it.getId() == profileId}) == null){
                    index += 100
                }
                return profile != null || list.find{it.getId() == profileId} != null
            }else{
                return apiSession.getUserId().toString().equals(apiCallContext.getFilters().get("user_id"))
            }
        }
        return false
    }

}
