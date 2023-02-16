/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.profile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.transaction.profile.CreateProfileMember;
import org.bonitasoft.engine.api.impl.transaction.profile.ProfileMemberUtils;
import org.bonitasoft.engine.api.utils.VisibleForTesting;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.command.SGroupProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.command.SRoleProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.command.SUserMembershipProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.command.SUserProfileMemberAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.*;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.exception.profilemember.SProfileMemberNotFoundException;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.search.profile.SearchProfileMembersForProfile;
import org.bonitasoft.engine.search.profile.SearchProfiles;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.session.SessionService;

public class ProfileAPIDelegate {

    private static ProfileAPIDelegate profileAPIDelegate;

    protected ProfileService profileService;

    protected ApplicationService applicationService;

    private SearchEntitiesDescriptor searchEntitiesDescriptor;
    private IdentityService identityService;

    @VisibleForTesting
    protected ProfileAPIDelegate(ProfileService profileService, IdentityService identityService,
            SearchEntitiesDescriptor searchEntitiesDescriptor, ApplicationService applicationService) {
        this.profileService = profileService;
        this.identityService = identityService;
        this.searchEntitiesDescriptor = searchEntitiesDescriptor;
        this.applicationService = applicationService;
    }

    public static TenantServiceAccessor getTenantAccessor() {
        return TenantServiceSingleton.getInstance();
    }

    public static ProfileAPIDelegate getInstance() {
        if (profileAPIDelegate == null) {
            TenantServiceAccessor tenantAccessor = getTenantAccessor();
            profileAPIDelegate = new ProfileAPIDelegate(tenantAccessor.getProfileService(),
                    tenantAccessor.getIdentityService(),
                    tenantAccessor.getSearchEntitiesDescriptor(),
                    tenantAccessor.getApplicationService());
        }
        return profileAPIDelegate;
    }

    public SearchResult<Profile> searchProfiles(final SearchOptions options) throws SearchException {
        final SearchProfiles searchProfileTransaction = new SearchProfiles(profileService,
                searchEntitiesDescriptor.getSearchProfileDescriptor(), options);
        try {
            searchProfileTransaction.execute();
            return searchProfileTransaction.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    public Map<Long, Long> getNumberOfProfileMembers(final List<Long> profileIds) {
        try {
            final List<SProfileMember> listOfProfileMembers = profileService.getProfileMembers(profileIds);
            final Map<Long, SProfileMember> profileMembers = new HashMap<>();
            final Map<Long, Long> result = new HashMap<>();
            for (final SProfileMember p : listOfProfileMembers) {
                profileMembers.put(p.getProfileId(), p);
            }

            for (final Long obj : profileMembers.keySet()) {
                long number = 0;
                for (final SProfileMember listOfProfileMember : listOfProfileMembers) {
                    final long tempProfileId = listOfProfileMember.getProfileId();
                    if (obj == tempProfileId) {
                        number++;
                    }
                }
                result.put(obj, number);
            }
            return result;
        } catch (final SBonitaException sbe) {
            throw new RetrieveException(sbe);
        }
    }

    public Profile getProfile(final long id) throws ProfileNotFoundException {
        try {
            return ModelConvertor.toProfile(profileService.getProfile(id));
        } catch (final SProfileNotFoundException e) {
            throw new ProfileNotFoundException(e);
        }
    }

    @Deprecated
    public List<Profile> getProfilesForUser(final long userId) {
        try {
            return ModelConvertor
                    .toProfiles(profileService.searchProfilesOfUser(userId, 0, 1000, "name", OrderByType.ASC));
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    public List<Profile> getProfilesForUser(final long userId, final int startIndex, final int maxResults,
            final ProfileCriterion criterion) {
        if (SessionService.SYSTEM_ID == userId) { // It's the tenant admin user
            return Collections.emptyList();
        }
        try {
            return ModelConvertor.toProfiles(
                    profileService.searchProfilesOfUser(userId, startIndex, maxResults, criterion.getField(),
                            OrderByType.valueOf(criterion.getOrder().name())));
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    public SearchResult<ProfileMember> searchProfileMembers(final String memberType, final SearchOptions options)
            throws SearchException {
        try {
            final SearchProfileMembersForProfile searchProfileMembersForProfile = new SearchProfileMembersForProfile(
                    getProfileMemberQuerySuffix(memberType),
                    profileService, getProfileMemberDescriptor(searchEntitiesDescriptor, memberType), options);
            searchProfileMembersForProfile.execute();
            return searchProfileMembersForProfile.getResult();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
    }

    public ProfileMember createProfileMember(final Long profileId, final Long userId, final Long groupId,
            final Long roleId) throws CreationException, AlreadyExistsException {

        try {
            final MemberType memberType = getMemberType(userId, groupId, roleId);
            final CreateProfileMember createProfileMember = new CreateProfileMember(profileService, identityService,
                    profileId, userId, groupId, roleId,
                    memberType);
            try {
                checkIfProfileMemberExists(profileId, userId, groupId, roleId,
                        memberType);
            } catch (final SBonitaException e1) {
                throw new AlreadyExistsException(e1);
            }
            createProfileMember.execute();
            return convertToProfileMember(createProfileMember);
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }

    }

    public void deleteProfileMember(final Long profileMemberId) throws DeletionException {
        try {
            // add a lock because the update profile call getProfile then update profile -> deadlock...
            final SProfileMember profileMember = profileService.getProfileMemberWithoutDisplayName(profileMemberId);
            profileService.updateProfileMetaData(profileMember.getProfileId());
            profileService.deleteProfileMember(profileMember.getId());
        } catch (final SProfileMemberNotFoundException spmnfe) {
            throw new DeletionException(new ProfileMemberNotFoundException(spmnfe));
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    protected ProfileMember convertToProfileMember(final CreateProfileMember createProfileMember) {
        return ModelConvertor.toProfileMember(createProfileMember.getResult());
    }

    protected void checkIfProfileMemberExists(final Long profileId,
            final Long userId, final Long groupId, final Long roleId, final MemberType memberType)
            throws SBonitaException {
        final SearchEntityDescriptor searchDescriptor = searchEntitiesDescriptor.getSearchProfileMemberUserDescriptor();
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1);
        searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, profileId);

        SearchProfileMembersForProfile searchProfileMembersForProfile;

        switch (memberType) {
            case USER:
                searchProfileMembersForProfile = new SearchProfileMembersForProfile("ForUser", profileService,
                        searchDescriptor, searchOptionsBuilder.done());
                searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.USER_ID, userId);
                searchProfileMembersForProfile.execute();
                if (searchProfileMembersForProfile.getResult().getCount() != 0) {
                    throw new SUserProfileMemberAlreadyExistsException(
                            "A profileMember with userId \"" + userId + "\" already exists");
                }
                break;
            case GROUP:
                searchProfileMembersForProfile = new SearchProfileMembersForProfile("ForGroup", profileService,
                        searchDescriptor, searchOptionsBuilder.done());
                searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.GROUP_ID, groupId);
                searchProfileMembersForProfile.execute();

                if (searchProfileMembersForProfile.getResult().getCount() != 0) {
                    throw new SGroupProfileMemberAlreadyExistsException(
                            "A profileMember with groupId \"" + groupId + "\" already exists");
                }
                break;
            case ROLE:
                searchProfileMembersForProfile = new SearchProfileMembersForProfile("ForRole", profileService,
                        searchDescriptor, searchOptionsBuilder.done());
                searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.ROLE_ID, roleId);
                searchProfileMembersForProfile.execute();

                if (searchProfileMembersForProfile.getResult().getCount() != 0) {
                    throw new SRoleProfileMemberAlreadyExistsException(
                            "A profileMember with roleId \"" + roleId + "\" already exists");
                }
                break;
            default:
                searchProfileMembersForProfile = new SearchProfileMembersForProfile("ForRoleAndGroup", profileService,
                        searchDescriptor,
                        searchOptionsBuilder.done());
                searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.ROLE_ID, roleId);
                searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.GROUP_ID, groupId);
                searchProfileMembersForProfile.execute();

                if (searchProfileMembersForProfile.getResult().getCount() != 0) {
                    throw new SUserMembershipProfileMemberAlreadyExistsException(
                            "A profileMember with groupId \"" + groupId + "\" and roleId \"" + roleId
                                    + "\" already exists");
                }
                break;
        }
    }

    private boolean isPositiveLong(final Long value) {
        return value != null && value > 0;
    }

    public MemberType getMemberType(final Long userId, final Long groupId, final Long roleId) throws CreationException {
        MemberType memberType;
        if (isPositiveLong(userId)) {
            memberType = MemberType.USER;
        } else if (isPositiveLong(groupId) && !isPositiveLong(roleId)) {
            memberType = MemberType.GROUP;
        } else if (isPositiveLong(roleId) && !isPositiveLong(groupId)) {
            memberType = MemberType.ROLE;
        } else if (isPositiveLong(roleId) && isPositiveLong(groupId)) {
            memberType = MemberType.MEMBERSHIP;
        } else {
            throw new CreationException(String.format("Parameters map must contain at least one of entries: %s, %s, %s",
                    ProfileMemberUtils.USER_ID, ProfileMemberUtils.GROUP_ID, ProfileMemberUtils.ROLE_ID));
        }
        return memberType;
    }

    private SearchEntityDescriptor getProfileMemberDescriptor(final SearchEntitiesDescriptor searchEntitiesDescriptor,
            final String memberType)
            throws SBonitaReadException {
        if (ProfileMemberUtils.USER_TYPE.equals(memberType)) {
            return searchEntitiesDescriptor.getSearchProfileMemberUserDescriptor();
        } else if (ProfileMemberUtils.GROUP_TYPE.equals(memberType)) {
            return searchEntitiesDescriptor.getSearchProfileMemberGroupDescriptor();
        } else if (ProfileMemberUtils.ROLE_TYPE.equals(memberType)) {
            return searchEntitiesDescriptor.getSearchProfileMemberRoleDescriptor();
        } else if (ProfileMemberUtils.ROLE_AND_GROUP_TYPE.equals(memberType)) {
            return searchEntitiesDescriptor.getSearchProfileMemberRoleAndGroupDescriptor();
        } else {
            throw new SBonitaReadException("Member type must be equalse to user,group,role or roleAndGroup.");
        }
    }

    private String getProfileMemberQuerySuffix(final String memberType) throws SBonitaReadException {
        if (ProfileMemberUtils.USER_TYPE.equals(memberType)) {
            return ProfileMemberUtils.USER_SUFFIX;
        } else if (ProfileMemberUtils.GROUP_TYPE.equals(memberType)) {
            return ProfileMemberUtils.GROUP_SUFFIX;
        } else if (ProfileMemberUtils.ROLE_TYPE.equals(memberType)) {
            return ProfileMemberUtils.ROLE_SUFFIX;
        } else if (ProfileMemberUtils.ROLE_AND_GROUP_TYPE.equals(memberType)) {
            return ProfileMemberUtils.ROLE_AND_GROUP_SUFFIX;
        } else {
            throw new SBonitaReadException("Member type must be equalse to user,group,role or roleAndGroup.");
        }
    }

}
