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
