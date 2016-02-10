/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.search.profile;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchProfileDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Celine Souchet
 */
public class SearchProfiles extends AbstractSearchEntity<Profile, SProfile> {

    private final ProfileService profileService;

    public SearchProfiles(final ProfileService profileService, final SearchProfileDescriptor searchProfileDescriptor, final SearchOptions options) {
        super(searchProfileDescriptor, options);
        this.profileService = profileService;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        return profileService.getNumberOfProfiles(queryOptions);
    }

    @Override
    public List<SProfile> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return profileService.searchProfiles(queryOptions);
    }

    @Override
    public List<Profile> convertToClientObjects(final List<SProfile> serverObjects) {
        return ModelConvertor.toProfiles(serverObjects);
    }

}
