/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilderFactory;
import org.bonitasoft.engine.profile.impl.ExportedMembership;
import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;
import org.bonitasoft.engine.profile.impl.ExportedProfiles;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Baptiste Mesta
 */
public class ProfilesExporter {

    private final static String USER_SUFFIX = "ForUser";
    private final static String ROLE_SUFFIX = "ForRole";
    private final static String GROUP_SUFFIX = "ForGroup";
    private final static String ROLE_AND_GROUP_SUFFIX = "ForRoleAndGroup";
    private static final int NUMBER_OF_RESULTS = 100;
    private IdentityService identityService;
    private ProfileService profileService;
    private ProfilesParser profilesParser;

    public ProfilesExporter(IdentityService identityService, ProfileService profileService, ProfilesParser profilesParser) {
        this.identityService = identityService;
        this.profileService = profileService;
        this.profilesParser = profilesParser;
    }

    public String exportAllProfiles() throws ExecutionException {
        try {
            ArrayList<SProfile> profiles = getAllProfiles();
            ExportedProfiles exportedProfiles = toExportedProfiles(profiles);
            return profilesParser.convert(exportedProfiles);
        } catch (SBonitaException | JAXBException e) {
            throw new ExecutionException(e);
        }
    }

    private ArrayList<SProfile> getAllProfiles() throws SBonitaReadException {
        ArrayList<SProfile> profiles = new ArrayList<>();
        int from = 0;
        List<SProfile> sProfiles;
        do {
            final QueryOptions queryOptions = new QueryOptions(from, 100, Collections.singletonList(new OrderByOption(
                    SProfile.class, SProfileBuilderFactory.NAME, OrderByType.ASC)), Collections.<FilterOption> emptyList(), null);
            sProfiles = profileService.searchProfiles(queryOptions);
            from += 100;
            profiles.addAll(sProfiles);
        } while (sProfiles.size() == 100);
        return profiles;
    }

    public String exportProfiles(List<Long> longs) throws ExecutionException {
        try {
            List<SProfile> profiles = profileService.getProfiles(longs);
            ExportedProfiles exportedProfiles = toExportedProfiles(profiles);
            return profilesParser.convert(exportedProfiles);
        } catch (SBonitaException | JAXBException e) {
            throw new ExecutionException(e);
        }
    }

    ExportedProfiles toExportedProfiles(List<SProfile> profiles)
            throws SBonitaReadException, SUserNotFoundException, SGroupNotFoundException, SRoleNotFoundException {
        ArrayList<ExportedProfile> exportedProfiles = new ArrayList<>();
        for (SProfile profile : profiles) {
            exportedProfiles.add(toExportedProfile(profile));
        }
        return new ExportedProfiles(exportedProfiles);
    }

    private ExportedProfile toExportedProfile(SProfile profile)
            throws SBonitaReadException, SUserNotFoundException, SGroupNotFoundException, SRoleNotFoundException {
        ExportedProfile exportedProfile = new ExportedProfile(profile.getName(), profile.isDefault());
        exportedProfile.setDescription(profile.getDescription());
        exportedProfile.setParentProfileEntries(getExportedProfilesEntries(profile));
        exportedProfile.setProfileMapping(getExportedProfileMapping(profile));
        return exportedProfile;
    }

    private List<ExportedParentProfileEntry> getExportedProfilesEntries(SProfile profile) throws SBonitaReadException {
        ArrayList<ExportedParentProfileEntry> exportedParentProfileEntries = new ArrayList<>();
        List<SProfileEntry> sProfileEntries = searchProfileEntries(profile.getId(), 0);
        for (SProfileEntry sProfileEntry : sProfileEntries) {
            exportedParentProfileEntries.add(toExportedParentProfileEntry(sProfileEntry));
        }
        return exportedParentProfileEntries;
    }

    private ExportedParentProfileEntry toExportedParentProfileEntry(SProfileEntry sProfileEntry) throws SBonitaReadException {
        ExportedParentProfileEntry exportedParentProfileEntry = new ExportedParentProfileEntry(sProfileEntry.getName());
        exportedParentProfileEntry.setCustom(sProfileEntry.isCustom());
        exportedParentProfileEntry.setIndex(sProfileEntry.getIndex());
        exportedParentProfileEntry.setDescription(sProfileEntry.getDescription());
        exportedParentProfileEntry.setType(sProfileEntry.getType());
        exportedParentProfileEntry.setPage(sProfileEntry.getPage());
        exportedParentProfileEntry.setChildProfileEntries(getExportedChildrenProfilesEntries(sProfileEntry));
        return exportedParentProfileEntry;
    }

    private List<ExportedProfileEntry> getExportedChildrenProfilesEntries(SProfileEntry sProfileEntry) throws SBonitaReadException {
        ArrayList<ExportedProfileEntry> exportedProfileEntries = new ArrayList<>();
        List<SProfileEntry> sProfileEntries = searchProfileEntries(sProfileEntry.getProfileId(), sProfileEntry.getId());
        for (SProfileEntry profileEntry : sProfileEntries) {
            exportedProfileEntries.add(toExportedProfileEntry(profileEntry, sProfileEntry.getName()));
        }
        return exportedProfileEntries;
    }

    private ExportedProfileEntry toExportedProfileEntry(SProfileEntry profileEntry, String parentName) {
        ExportedProfileEntry exportedProfileEntry = new ExportedProfileEntry(profileEntry.getName());
        exportedProfileEntry.setCustom(profileEntry.isCustom());
        exportedProfileEntry.setIndex(profileEntry.getIndex());
        exportedProfileEntry.setDescription(profileEntry.getDescription());
        exportedProfileEntry.setType(profileEntry.getType());
        exportedProfileEntry.setPage(profileEntry.getPage());
        return exportedProfileEntry;
    }

    protected List<SProfileEntry> searchProfileEntries(final long profileId, final long parentId) throws SBonitaReadException {
        final List<OrderByOption> orderByOptions = Collections
                .singletonList(new OrderByOption(SProfileEntry.class, SProfileEntryBuilderFactory.INDEX, OrderByType.ASC));
        final List<FilterOption> filters = new ArrayList<>();
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PROFILE_ID, profileId));
        filters.add(new FilterOption(SProfileEntry.class, SProfileEntryBuilderFactory.PARENT_ID, parentId));
        final QueryOptions queryOptions = new QueryOptions(0, Integer.MAX_VALUE, orderByOptions, filters, null);//profiles entry of a profile will not have a lot of elements
        return profileService.searchProfileEntries(queryOptions);
    }

    private ExportedProfileMapping getExportedProfileMapping(SProfile profile)
            throws SUserNotFoundException, SBonitaReadException, SGroupNotFoundException, SRoleNotFoundException {
        ExportedProfileMapping exportedProfileMapping = new ExportedProfileMapping();
        exportedProfileMapping.setUsers(getUsers(profile));
        exportedProfileMapping.setGroups(getGroups(profile));
        exportedProfileMapping.setRoles(getRoles(profile));
        exportedProfileMapping.setMemberships(getMemberships(profile));
        return exportedProfileMapping;
    }

    private List<String> getUsers(SProfile profile) throws SBonitaReadException, SUserNotFoundException {
        ArrayList<String> users = new ArrayList<>();
        int pageIndex = 0;
        List<SProfileMember> sProfileMembers;
        do {
            sProfileMembers = searchProfileMembers(pageIndex, profile.getId(), USER_SUFFIX);
            for (final SProfileMember sProfileMember : sProfileMembers) {
                users.add(identityService.getUser(sProfileMember.getUserId()).getUserName());
            }
            pageIndex++;
        } while (sProfileMembers.size() > 0);
        return users;
    }

    private List<String> getGroups(SProfile profile) throws SBonitaReadException, SGroupNotFoundException {
        ArrayList<String> groups = new ArrayList<>();
        int pageIndex = 0;
        List<SProfileMember> sProfileMembers;
        do {
            sProfileMembers = searchProfileMembers(pageIndex, profile.getId(), GROUP_SUFFIX);
            for (final SProfileMember sProfileMember : sProfileMembers) {
                groups.add(identityService.getGroup(sProfileMember.getGroupId()).getPath());
            }
            pageIndex++;
        } while (sProfileMembers.size() > 0);
        return groups;
    }

    private List<String> getRoles(SProfile profile) throws SBonitaReadException, SRoleNotFoundException {
        ArrayList<String> roles = new ArrayList<>();
        int pageIndex = 0;
        List<SProfileMember> sProfileMembers;
        do {
            sProfileMembers = searchProfileMembers(pageIndex, profile.getId(), ROLE_SUFFIX);
            for (final SProfileMember sProfileMember : sProfileMembers) {
                roles.add(identityService.getRole(sProfileMember.getRoleId()).getName());
            }
            pageIndex++;
        } while (sProfileMembers.size() > 0);
        return roles;
    }

    private List<ExportedMembership> getMemberships(SProfile profile) throws SBonitaReadException, SRoleNotFoundException, SGroupNotFoundException {
        ArrayList<ExportedMembership> memberships = new ArrayList<>();
        int pageIndex = 0;
        List<SProfileMember> sProfileMembers;
        do {
            sProfileMembers = searchProfileMembers(pageIndex, profile.getId(), ROLE_AND_GROUP_SUFFIX);
            for (final SProfileMember sProfileMember : sProfileMembers) {
                memberships.add(new ExportedMembership(identityService.getGroup(sProfileMember.getGroupId()).getPath(),
                        identityService.getRole(sProfileMember.getRoleId()).getName()));
            }
            pageIndex++;
        } while (sProfileMembers.size() > 0);
        return memberships;
    }

    private List<SProfileMember> searchProfileMembers(final int fromIndex, final long profileId, final String querySuffix) throws SBonitaReadException {
        final QueryOptions queryOptions = new QueryOptions(fromIndex * NUMBER_OF_RESULTS, NUMBER_OF_RESULTS, Collections.singletonList(new OrderByOption(
                SProfileMember.class, SProfileMemberBuilderFactory.ID, OrderByType.ASC)), Collections.singletonList(new FilterOption(SProfileMember.class,
                        SProfileEntryBuilderFactory.PROFILE_ID, profileId)),
                null);
        return profileService.searchProfileMembers(querySuffix, queryOptions);
    }
}
