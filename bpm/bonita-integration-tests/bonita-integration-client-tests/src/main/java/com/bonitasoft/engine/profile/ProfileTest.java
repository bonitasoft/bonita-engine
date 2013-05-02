/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.profile;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.search.descriptor.ProfileSearchDescriptor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.bpm.model.ProfileUpdateDescriptor;
import com.bonitasoft.engine.exception.profile.ProfileCreationException;
import com.bonitasoft.engine.exception.profile.ProfileDeletionException;
import com.bonitasoft.engine.exception.profile.ProfileUpdateException;

import static org.junit.Assert.assertEquals;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileTest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Creation" }, story = "Create default profile.")
    @Test
    public void defaultProfileCreation() throws BonitaException, IOException {
        Map<String, Serializable> getProfileResult = getProfileAPI().getProfile(adminProfileId);
        assertEquals("Administrator", getProfileResult.get("name"));

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        final SearchResult<HashMap<String, Serializable>> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(ADMIN_PROFILE_ENTRY_COUNT, searchedProfileEntries.getCount());

        getProfileResult = getProfileAPI().getProfile(userProfileId);
        assertEquals("User", getProfileResult.get("name"));

        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, 10);
        builder2.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        builder2.filter(ProfileEntrySearchDescriptor.PROFILE_ID, userProfileId);
        final SearchResult<HashMap<String, Serializable>> searchedProfileEntries2 = getProfileAPI().searchProfileEntries(builder2.done());
        assertEquals(USER_PROFILE_ENTRY_COUNT, searchedProfileEntries2.getCount());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Create", "Delete" }, story = "Create and delete profile.")
    @Test
    public void createAndDeleteProfile() throws BonitaException, IOException {
        final Map<String, Serializable> createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", "iconPath");

        final Map<String, Serializable> getProfileResult = getProfileAPI().getProfile((Long) createdProfile.get("id"));
        assertEquals(createdProfile.get("id"), getProfileResult.get("id"));

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);
        Long profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(5), profileCount);

        // Delete profile1 using id
        getProfileAPI().deleteProfile((Long) getProfileResult.get("id"));

        profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(4), profileCount);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = ProfileCreationException.class)
    public void createProfileWithWrongParameter() throws Exception {
        getProfileAPI().createProfile(null, null, null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Update" }, story = "Update profile.")
    @Test
    public void updateProfile() throws BonitaException, IOException {
        final Map<String, Serializable> createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", "IconPath profile1");

        final ProfileUpdateDescriptor updateDescriptor = new ProfileUpdateDescriptor();
        updateDescriptor.description("Updated description");
        updateDescriptor.name("Updated Name");
        updateDescriptor.iconPath("Updated iconPath");
        final Map<String, Serializable> upDateProfileResult = getProfileAPI().updateProfile((Long) createdProfile.get("id"), updateDescriptor);
        assertEquals("Updated Name", upDateProfileResult.get("name"));
        assertEquals("Updated description", upDateProfileResult.get("description"));
        assertEquals("Updated iconPath", upDateProfileResult.get("iconPath"));

        // Delete profile1 using id
        getProfileAPI().deleteProfile((Long) createdProfile.get("id"));

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);
        final Long profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(4), profileCount);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = ProfileUpdateException.class)
    public void updateProfileWithWrongParameter() throws Exception {
        getProfileAPI().updateProfile(2, null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = ProfileDeletionException.class)
    public void deleteProfileWithWrongParameter() throws Exception {
        getProfileAPI().deleteProfile(5464566L);
    }

}
