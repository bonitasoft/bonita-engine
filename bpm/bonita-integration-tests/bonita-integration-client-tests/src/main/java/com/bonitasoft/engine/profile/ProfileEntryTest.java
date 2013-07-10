package com.bonitasoft.engine.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntryCreator;
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileEntryTest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "Delete" }, story = "Create and delete profile entry.")
    @Test
    public void createAndDeleteProfileEntry() throws BonitaException {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator("ProfileEntry1", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder").setParentId(1L).setPage("MyPage");
        final ProfileEntry createdProfileEntry = getProfileAPI().createProfileEntry(profileEntryCreator);

        final ProfileEntry getProfileEntryResult = getProfileAPI().getProfileEntry(createdProfileEntry.getId());
        assertEquals(createdProfileEntry.getId(), getProfileEntryResult.getId());
        assertEquals("ProfileEntry1", getProfileEntryResult.getName());
        assertEquals("Description profileEntry1", getProfileEntryResult.getDescription());
        assertEquals(adminProfileId, getProfileEntryResult.getProfileId());
        assertEquals(1L, getProfileEntryResult.getParentId());
        assertEquals(0L, getProfileEntryResult.getIndex());
        assertEquals("folder", getProfileEntryResult.getType());
        assertEquals("MyPage", getProfileEntryResult.getPage());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        SearchResult<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(ADMIN_PROFILE_ENTRY_COUNT + 1, searchedProfileEntries.getCount());

        // Delete profile1 using id
        getProfileAPI().deleteProfileEntry(getProfileEntryResult.getId());

        searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(ADMIN_PROFILE_ENTRY_COUNT, searchedProfileEntries.getCount());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "Delete" }, story = "Create profile entry in 2nd position.")
    @Test
    public void insertInIndex2() throws BonitaException {
        final ProfileEntryCreator profileEntryCreator0 = new ProfileEntryCreator("ProfileEntry0", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder").setParentId(12L).setPage("MyPage");
        getProfileAPI().createProfileEntry(profileEntryCreator0);
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(1L).setType("folder").setParentId(12L).setPage("MyPage");
        getProfileAPI().createProfileEntry(profileEntryCreator1);
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntry3", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(2L).setType("folder").setParentId(12L).setPage("MyPage");
        getProfileAPI().createProfileEntry(profileEntryCreator3);

        // insert the element between 0 and 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(2L).setType("folder").setParentId(12L).setPage("MyPage");
        final ProfileEntry createdProfileEntry = getProfileAPI().createProfileEntry(profileEntryCreator2);

        final ProfileEntry getProfileEntryResult = getProfileAPI().getProfileEntry(createdProfileEntry.getId());
        assertEquals(2L, getProfileEntryResult.getIndex());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "No index" }, story = "Create profile entry without index.")
    @Test
    public void createProfileEntryWithoutIndex() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final long profileId = createdProfile.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setIndex(2L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator2);

        // Create Profile entry 3 without Index
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntryWithoutIndex", profileId).setDescription(
                "Description profileEntryWithoutIndex").setType("folder");
        final ProfileEntry profileEntryWithoutIndex = getProfileAPI().createProfileEntry(profileEntryCreator3);

        final ProfileEntry getProfileEntryResult = getProfileAPI().getProfileEntry(profileEntryWithoutIndex.getId());
        assertEquals(profileEntryWithoutIndex.getId(), getProfileEntryResult.getId());
        assertEquals(4L, getProfileEntryResult.getIndex());

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = CreationException.class)
    public void createProfileEntryWithWrongParameter() throws Exception {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator("ProfileEntry2", adminProfileId).setDescription("Description profileEntry2")
                .setType("link");
        getProfileAPI().createProfileEntry(profileEntryCreator);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update" }, story = "Update profile entry.")
    @Test
    public void updateProfileEntry() throws BonitaException {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator("ProfileEntry2", adminProfileId).setDescription("Description profileEntry2")
                .setType("folder").setIndex(12L).setParentId(1L);
        final ProfileEntry createdProfile = getProfileAPI().createProfileEntry(profileEntryCreator);

        // Update Profile Entry
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.name("UpdatedProfileEntry3");
        updateDescriptor.description("Updated Description profileEntry3");
        updateDescriptor.parentId(1L);
        updateDescriptor.profileId(adminProfileId);
        updateDescriptor.type("link");
        updateDescriptor.page("myPage");
        updateDescriptor.index(0L);
        final ProfileEntry upDateProfileEntryResult = getProfileAPI().updateProfileEntry(createdProfile.getId(), updateDescriptor);
        assertEquals("UpdatedProfileEntry3", upDateProfileEntryResult.getName());
        assertEquals("Updated Description profileEntry3", upDateProfileEntryResult.getDescription());
        assertEquals("myPage", upDateProfileEntryResult.getPage());

        // Delete profile Entry 1 using id
        getProfileAPI().deleteProfileEntry(createdProfile.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Delete", "Index" }, story = "Update profile entry index on delete.")
    @Test
    public void updateProfileEntryIndexOnDelete() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final Long profileId = createdProfile.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setIndex(2L).setType("folder");
        final ProfileEntry profileEntryToDelete = getProfileAPI().createProfileEntry(profileEntryCreator2);

        // Create Profile Entry 3
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntry3", profileId).setDescription("Description profileEntry3")
                .setIndex(4L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator3);

        // Delete Profile Entry 2
        getProfileAPI().deleteProfileEntry(profileEntryToDelete.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        final List<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(0L, searchedProfileEntries.get(0).getIndex());
        assertEquals("ProfileEntry1", searchedProfileEntries.get(0).getName());
        assertEquals(2L, searchedProfileEntries.get(1).getIndex());
        assertEquals("ProfileEntry3", searchedProfileEntries.get(1).getName());

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Delete", "Cascade" }, story = "Delete profile entry, and its children.", jira = "ENGINE-1605")
    @Test
    public void deleteProfileEntryAndChildren() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final Long profileId = createdProfile.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder");
        final ProfileEntry profileEntryToDelete = getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setIndex(2L).setType("folder").setParentId(profileEntryToDelete.getId());
        getProfileAPI().createProfileEntry(profileEntryCreator2);

        // Create Profile Entry 3
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntry3", profileId).setDescription("Description profileEntry3")
                .setIndex(4L).setType("folder").setParentId(profileEntryToDelete.getId());
        getProfileAPI().createProfileEntry(profileEntryCreator3);

        // Delete Profile Entry 1
        getProfileAPI().deleteProfileEntry(profileEntryToDelete.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PARENT_ID, profileEntryToDelete.getId());
        final List<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertTrue(searchedProfileEntries.isEmpty());

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Insert", "Index" }, story = "Update profile entry index on insert.")
    @Test
    public void updateProfileEntryIndexOnInsert() throws BonitaException {
        // Create Profile Entry 1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final long profileId = createdProfile.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile Entry 3
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntry3", profileId).setDescription("Description profileEntry3")
                .setIndex(2L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator3);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setIndex(2L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator2);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        final List<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(0L, searchedProfileEntries.get(0).getIndex());
        assertEquals("ProfileEntry1", searchedProfileEntries.get(0).getName());
        assertEquals(2L, searchedProfileEntries.get(1).getIndex());
        assertEquals("ProfileEntry2", searchedProfileEntries.get(1).getName());
        assertEquals(4L, searchedProfileEntries.get(2).getIndex());
        assertEquals("ProfileEntry3", searchedProfileEntries.get(2).getName());

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Index" }, story = "Update profile entry index on update.")
    @Test
    public void updateProfileEntryIndexOnUpdate() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final long profileId = createdProfile.getId();

        // Create Profile Entry Menu1
        final ProfileEntryCreator profileEntryCreatorMenu1 = new ProfileEntryCreator("Menu1", profileId).setDescription("Description Menu1")
                .setIndex(0L).setType("folder");
        final ProfileEntry createdProfileMenu = getProfileAPI().createProfileEntry(profileEntryCreatorMenu1);
        final long profileMenuId = createdProfileMenu.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder").setParentId(profileMenuId);
        getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setIndex(2L).setType("folder").setParentId(profileMenuId);
        getProfileAPI().createProfileEntry(profileEntryCreator2);

        // Create Profile Entry 3
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntry3", profileId).setDescription("Description profileEntry3")
                .setIndex(4L).setType("folder").setParentId(profileMenuId);
        final ProfileEntry profileEntry = getProfileAPI().createProfileEntry(profileEntryCreator3);

        // Update Profile Entry
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.index(Long.valueOf(-1));
        getProfileAPI().updateProfileEntry(profileEntry.getId(), updateDescriptor);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        builder.filter(ProfileEntrySearchDescriptor.PARENT_ID, profileMenuId);
        final List<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(0L, searchedProfileEntries.get(0).getIndex());
        assertEquals("ProfileEntry3", searchedProfileEntries.get(0).getName());
        assertEquals(2L, searchedProfileEntries.get(1).getIndex());
        assertEquals("ProfileEntry1", searchedProfileEntries.get(1).getName());
        assertEquals(4L, searchedProfileEntries.get(2).getIndex());
        assertEquals("ProfileEntry2", searchedProfileEntries.get(2).getName());

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Not Existing" }, story = "Execute profile command with not existing", jira = "ENGINE-548")
    @Test(expected = DeletionException.class)
    public void deleteProfileEntryNotExisting() throws Exception {
        getProfileAPI().deleteProfileEntry(165486489646541L);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = UpdateException.class)
    public void updateProfileEntryWithWrongParameter() throws Exception {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator("ProfileEntry1", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder");
        final ProfileEntry createdProfileEntry = getProfileAPI().createProfileEntry(profileEntryCreator);

        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.type("link");
        updateDescriptor.page("");
        getProfileAPI().updateProfileEntry(createdProfileEntry.getId(), updateDescriptor);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Not Existing" }, story = "Execute profile command with not existing", jira = "ENGINE-548")
    @Test(expected = UpdateException.class)
    public void updateProfileEntryNotExisting() throws Exception {
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.type("link");
        updateDescriptor.page(null);
        updateDescriptor.index(0L);
        getProfileAPI().updateProfileEntry(16464654L, updateDescriptor);
    }

}
