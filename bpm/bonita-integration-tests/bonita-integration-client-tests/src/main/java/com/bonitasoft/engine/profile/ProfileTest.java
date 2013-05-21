/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import java.io.IOException;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.search.descriptor.ProfileSearchDescriptor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.api.ProfileAPI;

import static org.junit.Assert.assertEquals;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileTest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Creation" }, story = "Create default profile.")
    @Test
    public void defaultProfileCreation() throws BonitaException, IOException {
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

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Create", "Delete" }, story = "Create and delete profile.")
    @Test
    public void createAndDeleteProfile() throws BonitaException, IOException {
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", "iconPath");

        final Profile getProfileResult = getProfileAPI().getProfile(createdProfile.getId());
        assertEquals(createdProfile.getId(), getProfileResult.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);
        Long profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(5), profileCount);

        // Delete profile1 using id
        getProfileAPI().deleteProfile(getProfileResult.getId());

        profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(4), profileCount);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = CreationException.class)
    public void createProfileWithWrongParameter() throws Exception {
        getProfileAPI().createProfile(null, null, null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Update" }, story = "Update profile.")
    @Test
    public void updateProfile() throws BonitaException, IOException {
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", "IconPath profile1");

        final ProfileUpdater updateDescriptor = new ProfileUpdater();
        updateDescriptor.description("Updated description");
        updateDescriptor.name("Updated Name");
        updateDescriptor.iconPath("Updated iconPath");
        final Profile upDateProfileResult = getProfileAPI().updateProfile(createdProfile.getId(), updateDescriptor);
        assertEquals("Updated Name", upDateProfileResult.getName());
        assertEquals("Updated description", upDateProfileResult.getDescription());
        assertEquals("Updated iconPath", upDateProfileResult.getIconPath());

        // Delete profile1 using id
        getProfileAPI().deleteProfile(createdProfile.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);
        final Long profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(4), profileCount);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = UpdateException.class)
    public void updateProfileWithWrongParameter() throws Exception {
        getProfileAPI().updateProfile(2, null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = DeletionException.class)
    public void deleteProfileWithWrongParameter() throws Exception {
        getProfileAPI().deleteProfile(5464566L);
    }

}
