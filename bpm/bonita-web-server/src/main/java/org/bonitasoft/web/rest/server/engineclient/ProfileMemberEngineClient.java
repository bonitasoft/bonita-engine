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
package org.bonitasoft.web.rest.server.engineclient;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.profile.ProfileMemberNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.portal.profile.ProfileMemberDefinition;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APISessionInvalidException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;

/**
 * @author Vincent Elcrin
 */
public class ProfileMemberEngineClient {

    private final ProfileAPI profileApi;

    protected ProfileMemberEngineClient(ProfileAPI profileApi) {
        this.profileApi = profileApi;
    }

    public SearchResult<ProfileMember> searchProfileMembers(String memberType, SearchOptions searchOptions) {
        try {
            return profileApi.searchProfileMembers(memberType, searchOptions);
        } catch (InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (SearchException e) {
            throw new APIException(e);
        }
    }

    public ProfileMember createProfileMember(Long profileId, Long userId, Long groupId, Long roleId) {
        try {
            return profileApi.createProfileMember(profileId, userId, groupId, roleId);
        } catch (InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (AlreadyExistsException e) {
            throw new APIForbiddenException(new T_("Profile member already exists"), e);
        } catch (CreationException e) {
            throw new APIException(e);
        }
    }

    public void deleteProfileMember(Long id) {
        try {
            profileApi.deleteProfileMember(id);
        } catch (InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (DeletionException e) {
            if (e.getCause() instanceof ProfileMemberNotFoundException) {
                throw new APIItemNotFoundException(ProfileMemberDefinition.TOKEN);
            } else {
                throw new APIException(e);
            }
        }
    }

}
