/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.profile.member;

import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.web.rest.model.portal.profile.ProfileMemberItem;
import org.bonitasoft.web.rest.server.engineclient.ProfileMemberEngineClient;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Vincent Elcrin
 */
public class AddProfileMemberHelper implements DatastoreHasAdd<ProfileMemberItem> {

    private static final Long UNSET = null;

    private final ProfileMemberEngineClient engineClient;

    public AddProfileMemberHelper(ProfileMemberEngineClient engineClient) {
        this.engineClient = engineClient;
    }

    @Override
    public ProfileMemberItem add(ProfileMemberItem item) {
        ProfileMember addedProfileMember = addProfileMember(item.getProfileId(), item.getUserId(), item.getGroupId(),
                item.getRoleId());
        return new ProfileMemberItemConverter().convert(addedProfileMember);
    }

    private ProfileMember addProfileMember(APIID profileId, APIID userId, APIID groupId, APIID roleId) {
        return engineClient.createProfileMember(toLong(profileId), toLong(userId), toLong(groupId), toLong(roleId));

    }

    private Long toLong(APIID apiId) {
        if (apiId == null || !apiId.isValidLongID()) {
            return UNSET;
        }
        return apiId.toLong();
    }

}
