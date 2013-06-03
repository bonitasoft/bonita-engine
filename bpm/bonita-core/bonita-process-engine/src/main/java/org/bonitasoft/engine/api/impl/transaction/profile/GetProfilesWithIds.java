/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
 */
package org.bonitasoft.engine.api.impl.transaction.profile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class GetProfilesWithIds implements TransactionContentWithResult<List<HashMap<String, Serializable>>> {

    private final ProfileService profileService;

    private List<HashMap<String, Serializable>> profiles = null;

    private final List<Long> profileIds;

    public GetProfilesWithIds(final ProfileService profileService, final List<Long> profileIds) {
        super();
        this.profileService = profileService;
        this.profileIds = profileIds;
    }

    @Override
    public void execute() throws SBonitaException {
        // profiles = ModelConvertor.toProfiles(profileService.getProfiles(profileIds));

        profiles = ProfileUtils.sProfilesToMap(profileService.getProfiles(profileIds));
    }

    @Override
    public List<HashMap<String, Serializable>> getResult() {
        return profiles;
    }

}
