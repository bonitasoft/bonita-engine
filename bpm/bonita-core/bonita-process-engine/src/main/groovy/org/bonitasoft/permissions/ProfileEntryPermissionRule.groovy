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
import org.bonitasoft.engine.profile.ProfileCriterion
import org.bonitasoft.engine.session.APISession

/**
 *
 * Secure profile related resources
 *
 * can be added to
 * <ul>
 *     <li>portal/profileEntry</li>
 *     <li>userXP/profileEntry</li>
 * </ul>
 * @author Baptiste Mesta
 */
class ProfileEntryPermissionRule implements PermissionRule {

    @Override
    boolean isAllowed(APISession apiSession, APICallContext apiCallContext, APIAccessor apiAccessor, Logger logger) {
       if(apiCallContext.isGET()){
            if(apiCallContext.getResourceId() != null){
                return false
            }else{
                def filters = apiCallContext.getFilters()
                if(filters.containsKey("profile_id")){
                    def profileId = Long.valueOf(filters.get("profile_id"))
                    def index = 0
                    def profile
                    def list = []
                    def profileAPI = apiAccessor.getProfileAPI()
                    while ((list = profileAPI.getProfilesForUser(apiSession.getUserId(),index,100,ProfileCriterion.ID_ASC)).size() == 100 && (profile = list.find{it.getId() == profileId}) == null){
                        index += 100
                    }
                    return profile != null || list.find{it.getId() == profileId} != null
                }
                return apiSession.getUserId().toString().equals(apiCallContext.getFilters().get("user_id"))
            }
        }
        return false
    }

}
