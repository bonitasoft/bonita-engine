package com.bonitasoft.engine.profile;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.bpm.model.ProfileEntryUpdateDescriptor;
import com.bonitasoft.engine.exception.profile.ProfileEntryDeletionException;
import com.bonitasoft.engine.exception.profile.ProfileEntryUpdateException;

import static org.junit.Assert.assertEquals;

/**
 * @author Julien Mege
 * @author Celine Souchet
 */
public class ProfileEntryTest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "Delete" }, story = "Create and delete profile entry.")
    @Test
    public void createAndDeleteProfileEntry() throws BonitaException, IOException {
        final Map<String, Serializable> createdProfileEntry = getProfileAPI().createProfileEntry("ProfileEntry1", "Description profileEntry1", Long.valueOf(1),
                adminProfileId, Long.valueOf(0), "folder", "MyPage");

        final Map<String, Serializable> getProfileEntryResult = getProfileAPI().getProfileEntry((Long) createdProfileEntry.get("id"));
        assertEquals(createdProfileEntry.get("id"), getProfileEntryResult.get("id"));
        assertEquals("ProfileEntry1", getProfileEntryResult.get("name"));
        assertEquals("Description profileEntry1", getProfileEntryResult.get("description"));
        assertEquals(adminProfileId, getProfileEntryResult.get("profileId"));
        assertEquals(Long.valueOf(1), getProfileEntryResult.get("parentId"));
        assertEquals(Long.valueOf(0), getProfileEntryResult.get("index"));
        assertEquals("folder", getProfileEntryResult.get("type"));
        assertEquals("MyPage", getProfileEntryResult.get("page"));

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.DESC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        SearchResult<HashMap<String, Serializable>> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(ADMIN_PROFILE_ENTRY_COUNT + 1, searchedProfileEntries.getCount());

        // Delete profile1 using id
        getProfileAPI().deleteProfileEntry((Long) getProfileEntryResult.get("id"));

        searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done());
        assertEquals(ADMIN_PROFILE_ENTRY_COUNT, searchedProfileEntries.getCount());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "Delete" }, story = "Create profile entry in 2nd position.")
    @Test
    public void insertInIndex2() throws BonitaException, IOException {
        getProfileAPI().createProfileEntry("ProfileEntry0", "Description profileEntry1", Long.valueOf(12),
                adminProfileId, Long.valueOf(0), "folder", "MyPage");
        getProfileAPI().createProfileEntry("ProfileEntry1", "Description profileEntry1", Long.valueOf(12), adminProfileId,
                Long.valueOf(1), "folder", "MyPage");
        getProfileAPI().createProfileEntry("ProfileEntry3", "Description profileEntry1", Long.valueOf(12), adminProfileId,
                Long.valueOf(2), "folder", "MyPage");

        // insert the element between 0 and 2
        final Map<String, Serializable> createdProfileEntry = getProfileAPI().createProfileEntry("ProfileEntry2", "Description profileEntry1",
                Long.valueOf(12), adminProfileId, Long.valueOf(2), "folder", "MyPage");

        final Map<String, Serializable> getProfileEntryResult = getProfileAPI().getProfileEntry((Long) createdProfileEntry.get("id"));
        assertEquals(Long.valueOf(2), getProfileEntryResult.get("index"));
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "No index" }, story = "Create profile entry without index.")
    @Test
    public void createProfileEntryWithoutIndex() throws BonitaException, IOException {
        // Create Profile1
        final Map<String, Serializable> createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final Long profileId = (Long) createdProfile.get("id");

        // Create Profile Entry 1
        getProfileAPI().createProfileEntry("ProfileEntry1", "Description profileEntry1", null, profileId, Long.valueOf(0), "folder", null);

        // Create Profile entry 2
        getProfileAPI().createProfileEntry("ProfileEntry2", "Description profileEntry2", null, profileId, Long.valueOf(2), "folder", null);

        // Create Profile entry 3 without Index
        final Map<String, Serializable> profileEntryWithoutIndex = getProfileAPI()
                .createProfileEntry("ProfileEntryWithoutIndex", "Description profileEntryWithoutIndex", null, profileId, null, "folder", null);

        final Map<String, Serializable> getProfileEntryResult = getProfileAPI().getProfileEntry((Long) profileEntryWithoutIndex.get("id"));
        assertEquals(profileEntryWithoutIndex.get("id"), getProfileEntryResult.get("id"));
        assertEquals(Long.valueOf(4), getProfileEntryResult.get("index"));

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = CommandParameterizationException.class)
    public void createProfileEntryWithWrongParameter() throws Exception {
        getProfileAPI().createProfileEntry("ProfileEntry2", "Description profileEntry2", null, adminProfileId, Long.valueOf(2), "link", null);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update" }, story = "Update profile entry.")
    @Test
    public void updateProfileEntry() throws BonitaException, IOException {
        final Map<String, Serializable> createdProfile = getProfileAPI().createProfileEntry("ProfileEntry2", "Description profileEntry2", Long.valueOf(1),
                adminProfileId, Long.valueOf(12), "folder", null);

        // Update Profile Entry
        final ProfileEntryUpdateDescriptor updateDescriptor = new ProfileEntryUpdateDescriptor();
        updateDescriptor.name("UpdatedProfileEntry3");
        updateDescriptor.description("Updated Description profileEntry3");
        updateDescriptor.parentId(Long.valueOf(1));
        updateDescriptor.profileId(adminProfileId);
        updateDescriptor.type("link");
        updateDescriptor.page("myPage");
        updateDescriptor.index(Long.valueOf(0));
        final Map<String, Serializable> upDateProfileEntryResult = getProfileAPI().updateProfileEntry((Long) createdProfile.get("id"), updateDescriptor);
        assertEquals("UpdatedProfileEntry3", upDateProfileEntryResult.get("name"));
        assertEquals("Updated Description profileEntry3", upDateProfileEntryResult.get("description"));
        assertEquals("myPage", upDateProfileEntryResult.get("page"));

        // Delete profile Entry 1 using id
        getProfileAPI().deleteProfileEntry((Long) createdProfile.get("id"));
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Delete", "Index" }, story = "Update profile entry index on delete.")
    @Test
    public void updateProfileEntryIndexOnDelete() throws BonitaException, IOException {
        // Create Profile1
        final Map<String, Serializable> createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final Long profileId = (Long) createdProfile.get("id");

        // Create Profile Entry 1
        getProfileAPI().createProfileEntry("ProfileEntry1", "Description profileEntry1", null, profileId, Long.valueOf(0), "folder", null);

        // Create Profile entry 2
        final Map<String, Serializable> profileToDelete = getProfileAPI().createProfileEntry("ProfileEntry2", "Description profileEntry2", null, profileId,
                Long.valueOf(2), "folder", null);

        // Create Profile Entry 3
        getProfileAPI().createProfileEntry("ProfileEntry3", "Description profileEntry3", null, profileId, Long.valueOf(4), "folder", null);

        // Delete Profile Entry 2
        getProfileAPI().deleteProfileEntry((Long) profileToDelete.get("id"));

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        final List<HashMap<String, Serializable>> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(Long.valueOf(0), searchedProfileEntries.get(0).get("index"));
        assertEquals("ProfileEntry1", searchedProfileEntries.get(0).get("name"));
        assertEquals(Long.valueOf(2), searchedProfileEntries.get(1).get("index"));
        assertEquals("ProfileEntry3", searchedProfileEntries.get(1).get("name"));

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Insert", "Index" }, story = "Update profile entry index on insert.")
    @Test
    public void updateProfileEntryIndexOnInsert() throws BonitaException, IOException {
        // Create Profile Entry 1
        final Map<String, Serializable> createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final Long profileId = (Long) createdProfile.get("id");

        // Create Profile Entry 1
        getProfileAPI().createProfileEntry("ProfileEntry1", "Description profileEntry1", null, profileId, Long.valueOf(0), "folder", null);

        // Create Profile Entry 3
        getProfileAPI().createProfileEntry("ProfileEntry3", "Description profileEntry3", null, profileId, Long.valueOf(2), "folder", null);

        // Create Profile entry 2
        getProfileAPI().createProfileEntry("ProfileEntry2", "Description profileEntry2", null, profileId, Long.valueOf(2), "folder", null);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        final List<HashMap<String, Serializable>> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(Long.valueOf(0), searchedProfileEntries.get(0).get("index"));
        assertEquals("ProfileEntry1", searchedProfileEntries.get(0).get("name"));
        assertEquals(Long.valueOf(2), searchedProfileEntries.get(1).get("index"));
        assertEquals("ProfileEntry2", searchedProfileEntries.get(1).get("name"));
        assertEquals(Long.valueOf(4), searchedProfileEntries.get(2).get("index"));
        assertEquals("ProfileEntry3", searchedProfileEntries.get(2).get("name"));

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Index" }, story = "Update profile entry index on update.")
    @Test
    public void updateProfileEntryIndexOnUpdate() throws BonitaException, IOException {
        // Create Profile1
        final Map<String, Serializable> createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1", null);
        final Long profileId = (Long) createdProfile.get("id");

        // Create Profile Entry Menu1
        final Map<String, Serializable> createdProfileMenu = getProfileAPI().createProfileEntry("Menu1", "Description Menu1", null, profileId,
                Long.valueOf(0), "folder", null);
        final Long profileMenuId = (Long) createdProfileMenu.get("id");

        // Create Profile Entry 1
        getProfileAPI().createProfileEntry("ProfileEntry1", "Description profileEntry1", profileMenuId, profileId, Long.valueOf(0), "folder", null);

        // Create Profile entry 2
        getProfileAPI().createProfileEntry("ProfileEntry2", "Description profileEntry2", profileMenuId, profileId, Long.valueOf(2), "folder", null);

        // Create Profile Entry 3
        final Map<String, Serializable> profileEntry = getProfileAPI().createProfileEntry("ProfileEntry3", "Description profileEntry3", profileMenuId,
                profileId, Long.valueOf(4), "folder", null);

        // Update Profile Entry
        final ProfileEntryUpdateDescriptor updateDescriptor = new ProfileEntryUpdateDescriptor();
        updateDescriptor.index(Long.valueOf(0));
        getProfileAPI().updateProfileEntry((Long) profileEntry.get("id"), updateDescriptor);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        final List<HashMap<String, Serializable>> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(Long.valueOf(0), searchedProfileEntries.get(0).get("index"));
        assertEquals("Menu1", searchedProfileEntries.get(0).get("name"));
        assertEquals(Long.valueOf(0), searchedProfileEntries.get(1).get("index"));
        assertEquals("ProfileEntry3", searchedProfileEntries.get(1).get("name"));
        assertEquals(Long.valueOf(2), searchedProfileEntries.get(2).get("index"));
        assertEquals("ProfileEntry1", searchedProfileEntries.get(2).get("name"));
        assertEquals(Long.valueOf(4), searchedProfileEntries.get(3).get("index"));
        assertEquals("ProfileEntry2", searchedProfileEntries.get(3).get("name"));

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = ProfileEntryDeletionException.class)
    public void deleteProfileEntryWithWrongParameter() throws Exception {
        getProfileAPI().deleteProfileEntry(6);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = ProfileEntryUpdateException.class)
    public void updateProfileEntryWithWrongParameter() throws Exception {
        final Map<String, Serializable> createdProfileEntry = getProfileAPI().createProfileEntry("ProfileEntry1", "Description profileEntry1", Long.valueOf(1),
                adminProfileId, Long.valueOf(0), "folder", "MyPage");

        final ProfileEntryUpdateDescriptor updateDescriptor = new ProfileEntryUpdateDescriptor();
        updateDescriptor.type("link");
        updateDescriptor.page(null);
        updateDescriptor.index(Long.valueOf(0));
        getProfileAPI().updateProfileEntry((Long) createdProfileEntry.get("id"), updateDescriptor);
    }

}
