/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.impl.profile.ProfileAPIDelegate;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.profile.*;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
@AvailableWhenTenantIsPaused
public class ProfileAPIImpl implements ProfileAPI {

    @VisibleForTesting
    ProfileAPIDelegate getProfileAPIDelegate() {
        return ProfileAPIDelegate.getInstance();
    }

    @Override
    public Profile getProfile(final long id) throws ProfileNotFoundException {
        return getProfileAPIDelegate().getProfile(id);
    }

    @Override
    @Deprecated
    public List<Profile> getProfilesForUser(final long userId) {
        return getProfileAPIDelegate().getProfilesForUser(userId);
    }

    @Override
    public List<Profile> getProfilesForUser(final long userId, final int startIndex, final int maxResults,
            final ProfileCriterion criterion) {
        return getProfileAPIDelegate().getProfilesForUser(userId, startIndex, maxResults, criterion);
    }

    @Override
    public List<Profile> getProfilesWithNavigationForUser(final long userId, final int startIndex, final int maxResults,
            final ProfileCriterion criterion) {
        return Collections.emptyList();
    }

    @Override
    public SearchResult<Profile> searchProfiles(final SearchOptions options) throws SearchException {
        return getProfileAPIDelegate().searchProfiles(options);
    }

    @Override
    public Map<Long, Long> getNumberOfProfileMembers(final List<Long> profileIds) {
        return getProfileAPIDelegate().getNumberOfProfileMembers(profileIds);
    }

    @Override
    public SearchResult<ProfileMember> searchProfileMembers(final String memberType, final SearchOptions options)
            throws SearchException {
        return getProfileAPIDelegate().searchProfileMembers(memberType, options);
    }

    @Override
    public SearchResult<ProfileEntry> searchProfileEntries(final SearchOptions options) throws SearchException {
        return new SearchResultImpl<>(0, Collections.emptyList());
    }

    @Override
    public List<ProfileEntry> getProfileEntries(String profileName) throws ProfileNotFoundException {
        return Collections.emptyList();
    }

    @Override
    public ProfileEntry getProfileEntry(final long id) throws ProfileEntryNotFoundException {
        return null;
    }

    @Override
    public ProfileMember createProfileMember(final Long profileId, final Long userId, final Long groupId,
            final Long roleId) throws CreationException {
        return getProfileAPIDelegate().createProfileMember(profileId, userId, groupId, roleId);

    }

    @Override
    public ProfileMember createProfileMember(final ProfileMemberCreator creator)
            throws CreationException, AlreadyExistsException {
        if (creator == null) {
            throw new CreationException("Unable to create a profile member with a null creator!");
        }

        final SProfileMember sProfileMember = ModelConvertor.constructSProfileMember(creator);
        return createProfileMember(sProfileMember.getProfileId(), sProfileMember.getUserId(),
                sProfileMember.getGroupId(), sProfileMember.getRoleId());
    }

    public MemberType getMemberType(final Long userId, final Long groupId, final Long roleId) throws CreationException {
        return getProfileAPIDelegate().getMemberType(userId, groupId, roleId);
    }

    @Override
    public void deleteProfileMember(final Long profileMemberId) throws DeletionException {
        getProfileAPIDelegate().deleteProfileMember(profileMemberId);
    }

    protected long getUserIdFromSession() {
        return SessionInfos.getUserIdFromSession();
    }

}
