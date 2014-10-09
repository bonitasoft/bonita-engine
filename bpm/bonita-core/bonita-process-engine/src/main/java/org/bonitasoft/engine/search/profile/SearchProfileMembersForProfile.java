/**
 * Copyright (C) 2011,2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.search.profile;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class SearchProfileMembersForProfile extends AbstractSearchEntity<ProfileMember, SProfileMember> {

    private final ProfileService profileService;

    private final String querySuffix;

    public SearchProfileMembersForProfile(final String querySuffix, final ProfileService profileService, final SearchEntityDescriptor searchDescriptor,
            final SearchOptions searchOptions) {
        super(searchDescriptor, searchOptions);
        this.querySuffix = querySuffix;
        this.profileService = profileService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return profileService.getNumberOfProfileMembers(querySuffix, searchOptions);
    }

    @Override
    public List<SProfileMember> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return profileService.searchProfileMembers(querySuffix, searchOptions);
    }

    @Override
    public List<ProfileMember> convertToClientObjects(final List<SProfileMember> serverObjects) {
        return ModelConvertor.toProfileMembers(serverObjects);
    }

}
