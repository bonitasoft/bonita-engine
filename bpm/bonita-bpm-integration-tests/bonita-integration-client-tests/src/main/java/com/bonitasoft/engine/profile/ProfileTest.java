/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.api.ProfileAPI;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
@SuppressWarnings("javadoc")
public class ProfileTest extends AbstractProfileTest {

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

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Create", "Delete", "Profile", "Custom" }, story = "Create and delete custom profile.", jira = "ENGINE-1532")
    @Test
    public void createAndDeleteCustomProfile() throws BonitaException {
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", "iconPath");

        final Profile getProfileResult = getProfileAPI().getProfile(createdProfile.getId());
        assertEquals(createdProfile.getId(), getProfileResult.getId());
        assertFalse(createdProfile.isDefault());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);
        Long profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(5), profileCount);

        // Delete custom profile using id
        getProfileAPI().deleteProfile(getProfileResult.getId());

        profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(4), profileCount);
    }

    @Test(expected = AlreadyExistsException.class)
    public void cantCreate2CustomProfilesWithSameName() throws Exception {
        final ProfileCreator creator = new ProfileCreator("aProfile");
        final Profile profile = getProfileAPI().createProfile(creator);
        try {
            getProfileAPI().createProfile(creator);
        } finally {
            // Clean up
            getProfileAPI().deleteProfile(profile.getId());
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Create", "Profile", "Custom" }, story = "Create custom profile from an other profile.", jira = "ENGINE-1532")
    @Test
    public void createCustomProfileFromOtherProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileSearchDescriptor.NAME, "Administrator");
        final List<Profile> profiles = getProfileAPI().searchProfiles(builder.done()).getResult();
        assertEquals(1, profiles.size());
        final Profile profile1 = profiles.get(0);

        logout();
        loginWith("userName1", "User1Pwd");
        final ProfileCreator profileCreator = new ProfileCreator(profile1);
        profileCreator.setName("name");
        final Profile profile2 = getProfileAPI().createProfile(profileCreator);

        // Profile1 is default
        assertNotEquals(profile1.getId(), profile2.getId());
        assertNotEquals(profile1.isDefault(), profile2.isDefault());
        assertNotEquals(profile1.getCreatedBy(), profile2.getCreatedBy());
        assertNotEquals(profile1.getCreationDate(), profile2.getCreationDate());
        assertNotEquals(profile1.getLastUpdateDate(), profile2.getLastUpdateDate());
        assertNotEquals(profile1.getLastUpdatedBy(), profile2.getLastUpdatedBy());
        assertNotEquals(profile1.getName(), profile2.getName());
        assertEquals(profile1.getDescription(), profile2.getDescription());
        assertEquals(profile1.getIconPath(), profile2.getIconPath());
        assertEquals("name", profile2.getName());

        // Clean up
        getProfileAPI().deleteProfile(profile2.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Can't", "Delete", "Default", "Profile" }, story = "Can't delete default profile.", jira = "ENGINE-1532")
    @Test(expected = DeletionException.class)
    public void cantDeleteDefaultProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileSearchDescriptor.NAME, "Administrator");
        final List<Profile> profiles = getProfileAPI().searchProfiles(builder.done()).getResult();
        assertEquals(1, profiles.size());

        // Delete default profile
        getProfileAPI().deleteProfile(profiles.get(0).getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Wrong parameter" }, jira = "ENGINE-548")
    @Test(expected = CreationException.class)
    public void createProfileWithWrongParameter() throws Exception {
        getProfileAPI().createProfile(null, null, null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Update", "Profile", "Custom" }, story = "Update custom profile.", jira = "ENGINE-1532")
    @Test
    public void updateCustomProfile() throws BonitaException {
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", "IconPath profile1");

        // Update custom profile
        final ProfileUpdater updateDescriptor = new ProfileUpdater();
        updateDescriptor.description("Updated description");
        updateDescriptor.name("Updated Name");
        updateDescriptor.iconPath("Updated iconPath");
        getProfileAPI().updateProfile(createdProfile.getId(), updateDescriptor);
        final Profile upDateProfileResult = getProfileAPI().getProfile(createdProfile.getId());
        assertEquals("Updated Name", upDateProfileResult.getName());
        assertEquals("Updated description", upDateProfileResult.getDescription());
        assertEquals(createdProfile.isDefault(), upDateProfileResult.isDefault());
        assertNotEquals(createdProfile.getLastUpdateDate(), upDateProfileResult.getLastUpdateDate());

        // Delete profile using id
        getProfileAPI().deleteProfile(createdProfile.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.DESC);
        final Long profileCount = getProfileAPI().searchProfiles(builder.done()).getCount();
        assertEquals(Long.valueOf(4), profileCount);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Update", "Profile", "Custom", "Same", "Name" }, story = "Update name of custom profile with same value.", jira = "ENGINE-2011")
    @Test
    public void updateSameCustomProfileWithSameName() throws BonitaException {
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", "IconPath profile1");

        // Update custom profile
        final ProfileUpdater updateDescriptor = new ProfileUpdater();
        updateDescriptor.name("Profile1");
        getProfileAPI().updateProfile(createdProfile.getId(), updateDescriptor);
        final Profile upDateProfileResult = getProfileAPI().getProfile(createdProfile.getId());
        assertEquals("Profile1", upDateProfileResult.getName());

        // Delete profile using id
        getProfileAPI().deleteProfile(createdProfile.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Update", "Profile", "Custom" }, story = "Update custom profile fails.", jira = "ENGINE-2011")
    @Test(expected = AlreadyExistsException.class)
    public void cantUpdateProfileWithExistingName() throws BonitaException {
        final Profile profile1 = getProfileAPI().createProfile("Profile1", "Description profile1", "IconPath profile1");
        final Profile profile2 = getProfileAPI().createProfile("Profile2", "Description profile2", "IconPath profile2");

        // Update custom profile
        final ProfileUpdater updateDescriptor = new ProfileUpdater();
        updateDescriptor.name("Profile2");
        try {
            getProfileAPI().updateProfile(profile1.getId(), updateDescriptor);
        } finally {
            // Delete profile using id
            getProfileAPI().deleteProfile(profile1.getId());
            getProfileAPI().deleteProfile(profile2.getId());
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Can't", "Update", "Default", "Profile" }, story = "Can't update default profile.", jira = "ENGINE-1532")
    @Test(expected = UpdateException.class)
    public void cantUpdateDefaultProfile() throws BonitaException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileSearchDescriptor.NAME, "Administrator");
        final List<Profile> profiles = getProfileAPI().searchProfiles(builder.done()).getResult();
        assertEquals(1, profiles.size());

        // Update default profile
        final ProfileUpdater updateDescriptor = new ProfileUpdater();
        updateDescriptor.description("Updated description");
        updateDescriptor.name("Updated Name");
        updateDescriptor.iconPath("Updated iconPath");
        getProfileAPI().updateProfile(profiles.get(0).getId(), updateDescriptor);
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
