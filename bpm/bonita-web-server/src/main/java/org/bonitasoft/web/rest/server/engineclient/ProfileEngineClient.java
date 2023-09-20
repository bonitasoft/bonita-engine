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

import java.util.List;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileCriterion;
import org.bonitasoft.engine.profile.ProfileNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.model.portal.profile.ProfileDefinition;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Vincent Elcrin
 */
public class ProfileEngineClient {

    private final ProfileAPI profileApi;

    public ProfileEngineClient(ProfileAPI profileApi) {
        this.profileApi = profileApi;
    }

    public Profile getProfile(Long id) {
        try {
            return profileApi.getProfile(id);
        } catch (RetrieveException e) {
            throw new APIException(e);
        } catch (ProfileNotFoundException e) {
            throw new APIItemNotFoundException(ProfileDefinition.TOKEN, APIID.makeAPIID(id));
        }
    }

    public SearchResult<Profile> searchProfiles(SearchOptions options) {
        try {
            return profileApi.searchProfiles(options);
        } catch (SearchException e) {
            throw new APIException(e);
        }
    }

    public List<Profile> listProfilesForUser(long userId) {
        try {
            return profileApi.getProfilesForUser(userId, 0, Integer.MAX_VALUE, ProfileCriterion.ID_ASC);
        } catch (RetrieveException e) {
            throw new APIException(e);
        }
    }

}
