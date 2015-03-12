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
package org.bonitasoft.engine.command.web.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.profile.ProfileMember;
import org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class ProfileImportCommandIT extends AbstractCommandProfileIT {

    private List<User> createUsers() throws BonitaException {
        final User user1 = createUser("userName1", "User1Pwd", "User1FirstName", "User1LastName");
        final User user2 = createUser("userName2", "User2Pwd", "User2FirstName", "User2LastName");
        final User user3 = createUser("userName3", "User3Pwd", "User3FirstName", "User3LastName");
        final User user4 = createUser("userName4", "User4Pwd", "User4FirstName", "User4LastName");
        final User user5 = createUser("userName5", "User5Pwd", "User5FirstName", "User5LastName");
        return Arrays.asList(user1, user2, user3, user4, user5);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Command", "Profile", "Import" }, story = "Import profiles and delete existing.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void importProfilesCommand() throws BonitaException, IOException {
        final List<User> users = createUsers();
        final InputStream xmlStream1 = ProfileImportCommandIT.class.getResourceAsStream("AllProfiles.xml");
        final byte[] xmlContent1 = IOUtils.toByteArray(xmlStream1);
        xmlStream1.close();

        final Map<String, Serializable> importParameters1 = new HashMap<String, Serializable>();
        importParameters1.put("xmlContent", xmlContent1);
        final List<String> warningMsgs1 = (List<String>) getCommandAPI().execute(IMPORT_PROFILES_CMD, importParameters1);
        assertEquals(0, warningMsgs1.size());

        // check current status: profiles and its attributes
        SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, Integer.MAX_VALUE).sort(ProfileSearchDescriptor.ID, Order.ASC);
        final List<Profile> searchedProfiles = getProfileAPI().searchProfiles(searchBuilder.done()).getResult();

        final long olderid1 = searchedProfiles.get(0).getId();
        final long olderid2 = searchedProfiles.get(1).getId();
        final long olderid3 = searchedProfiles.get(2).getId();
        final long olderid4 = searchedProfiles.get(3).getId();

        assertEquals(4, searchedProfiles.size());
        assertEquals("Administrator", searchedProfiles.get(0).getName());
        assertEquals("Administrator profile", searchedProfiles.get(0).getDescription());
        assertEquals("Team manager", searchedProfiles.get(1).getName());
        assertEquals("Team Manager profile", searchedProfiles.get(1).getDescription());
        assertEquals("Process owner", searchedProfiles.get(2).getName());
        assertEquals("Process owner profile", searchedProfiles.get(2).getDescription());
        assertEquals("User", searchedProfiles.get(3).getName());
        assertEquals("User profile", searchedProfiles.get(3).getDescription());

        // check profile entries and their attributes
        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, Integer.MAX_VALUE).filter(ProfileEntrySearchDescriptor.PROFILE_ID, i)
                    .sort(ProfileEntrySearchDescriptor.PROFILE_ID, Order.ASC).done();
            final SearchResult<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(searchOptions);
            assertNotNull(searchedProfileEntries);
        }

        final InputStream xmlStream = ProfileImportCommandIT.class.getResourceAsStream("deleteExistingProfile.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
        xmlStream.close();

        final Map<String, Serializable> importParameters = new HashMap<String, Serializable>();
        importParameters.put("xmlContent", xmlContent);
        final List<String> warningMsgs = (List<String>) getCommandAPI().execute(IMPORT_PROFILES_CMD, importParameters);
        assertEquals(0, warningMsgs.size());

        // check profiles

        searchBuilder = new SearchOptionsBuilder(0, Integer.MAX_VALUE).sort(ProfileSearchDescriptor.ID, Order.ASC);
        final List<Profile> searchedProfilesRes = getProfileAPI().searchProfiles(searchBuilder.done()).getResult();

        final long newId1 = searchedProfilesRes.get(0).getId();
        assertTrue(newId1 > olderid4);

        assertEquals(1, searchedProfilesRes.size());
        assertEquals("Team Manager", searchedProfilesRes.get(0).getName());
        assertEquals("TM profile", searchedProfilesRes.get(0).getDescription());

        // check profileEntries
        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, Integer.MAX_VALUE).filter(ProfileEntrySearchDescriptor.PROFILE_ID, i)
                    .sort(ProfileEntrySearchDescriptor.NAME, Order.ASC).done();
            final SearchResult<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(searchOptions);

            assertTrue(searchedProfileEntries.getResult().size() == 0);
        }

        SearchOptions searchOptions = new SearchOptionsBuilder(0, Integer.MAX_VALUE).filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId1)
                .sort(ProfileEntrySearchDescriptor.NAME, Order.ASC).done();
        final SearchResult<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(searchOptions);
        final List<ProfileEntry> searchedProfileEntriesRes2 = searchedProfileEntries.getResult();
        assertNotNull(searchedProfileEntriesRes2);
        assertEquals(1, searchedProfileEntriesRes2.size());
        assertEquals("Home", searchedProfileEntriesRes2.get(0).getName());
        assertEquals("My team activitys dashboard", searchedProfileEntriesRes2.get(0).getDescription());
        assertEquals("CurrentUserTeamTasksDashboard", searchedProfileEntriesRes2.get(0).getType());

        // check profile mapping
        searchOptions = new SearchOptionsBuilder(0, Integer.MAX_VALUE).filter(ProfileMemberSearchDescriptor.PROFILE_ID, newId1)
                .sort(ProfileMemberSearchDescriptor.ID, Order.ASC).done();
        final SearchResult<ProfileMember> searchpms = getProfileAPI().searchProfileMembers("user", searchOptions);
        assertEquals(2, searchpms.getCount());
        assertEquals(users.get(0).getId(), searchpms.getResult().get(0).getUserId());
        assertEquals(users.get(1).getId(), searchpms.getResult().get(1).getUserId());
        assertEquals(newId1, searchpms.getResult().get(0).getProfileId());
        assertEquals(newId1, searchpms.getResult().get(1).getProfileId());

        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            searchOptions = new SearchOptionsBuilder(0, Integer.MAX_VALUE).filter(ProfileEntrySearchDescriptor.PROFILE_ID, i).done();
            final SearchResult<ProfileMember> searchpms1 = getProfileAPI().searchProfileMembers("user", searchOptions);
            assertEquals(0, searchpms1.getCount());
        }

        // delete user
        for (final User user : users) {
            deleteUsers(user);
        }
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Command", "Profile", "Import", "Wrong parameter" }, story = "Execute profile import command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void importProfilesCommandWithWrongParameter() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(IMPORT_PROFILES_CMD, parameters);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Creation" }, story = "Create default profile.", jira = "")
    @Test
    public void defaultProfileCreation() throws BonitaException {
        Profile getProfileResult = getProfileAPI().getProfile(adminProfileId);
        assertEquals("Administrator", getProfileResult.getName());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        final SearchResult<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(ADMIN_PROFILE_ENTRY_COUNT, searchedProfileEntries.getCount());

        getProfileResult = getProfileAPI().getProfile(userProfileId);
        assertEquals("User", getProfileResult.getName());

        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, 10);
        builder2.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        builder2.filter(ProfileEntrySearchDescriptor.PROFILE_ID, userProfileId);
        final SearchResult<ProfileEntry> searchedProfileEntries2 = getProfileAPI().searchProfileEntries(builder2.done());
        assertEquals(USER_PROFILE_ENTRY_COUNT, searchedProfileEntries2.getCount());
    }

}
