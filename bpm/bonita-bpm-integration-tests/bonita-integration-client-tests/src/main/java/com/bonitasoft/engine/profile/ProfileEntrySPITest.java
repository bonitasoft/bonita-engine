/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
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
public class ProfileEntrySPITest extends AbstractProfileSPTest {

    private static final String ENTRY_DESCRIPTION = "entry description";

    private static final String ENTRY_TYPE_LINK = ProfileEntryType.LINK.toString().toLowerCase();

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "Delete" }, story = "Create and delete profile entry.", jira = "")
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

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "Without", "Name" }, story = "Create profile entry without name.", jira = "ENGINE-1607")
    @Test
    public void createProfileEntryWithoutName() throws BonitaException {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator(adminProfileId).setDescription("Description profileEntry1").setIndex(0L)
                .setType("folder").setParentId(1L).setPage("MyPage");
        final ProfileEntry createdProfileEntry = getProfileAPI().createProfileEntry(profileEntryCreator);

        final ProfileEntry getProfileEntryResult = getProfileAPI().getProfileEntry(createdProfileEntry.getId());
        assertEquals(createdProfileEntry.getId(), getProfileEntryResult.getId());
        assertNull(getProfileEntryResult.getName());

        // Clean up
        getProfileAPI().deleteProfileEntry(getProfileEntryResult.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "With", "Same", "Page", "Parent", "Profile" }, story = "Can't create profile entry with same parent, profile, page.", jira = "ENGINE-1607")
    @Test()
    public void canCreate2ProfileEntriesWithSamePageInSameParent() throws BonitaException {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator(adminProfileId).setParentId(1L).setPage("MyPage").setDescription("description");
        final ProfileEntry profileEntry = getProfileAPI().createProfileEntry(profileEntryCreator);

        try {
            final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator(adminProfileId).setParentId(1L).setPage("MyPage")
                    .setDescription("description2");
            getProfileAPI().createProfileEntry(profileEntryCreator2);
        } finally {
            // Clean up
            getProfileAPI().deleteProfileEntry(profileEntry.getId());
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "With", "Same", "Name", "Parent", "Profile" }, story = "Create profile entry with same parent, profile, name.", jira = "ENGINE-1607")
    @Test
    public void create2ProfileEntriesWithSameNameInSameParent() throws BonitaException {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator(adminProfileId).setParentId(1L).setPage("MyPage");
        final ProfileEntry profileEntry = getProfileAPI().createProfileEntry(profileEntryCreator);

        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator(adminProfileId).setParentId(1L).setPage("MyPage2")
                .setDescription("description2");
        final ProfileEntry profileEntry2 = getProfileAPI().createProfileEntry(profileEntryCreator2);

        // Clean up
        getProfileAPI().deleteProfileEntry(profileEntry.getId());
        getProfileAPI().deleteProfileEntry(profileEntry2.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "Delete" }, story = "Create profile entry in 2nd position.", jira = "")
    @Test
    public void insertInIndex2() throws BonitaException {
        final ProfileEntryCreator profileEntryCreator0 = new ProfileEntryCreator("ProfileEntry0", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder").setParentId(12L).setPage("MyPage");
        getProfileAPI().createProfileEntry(profileEntryCreator0);
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(1L).setType("folder").setParentId(12L).setPage("MyPage2");
        getProfileAPI().createProfileEntry(profileEntryCreator1);
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntry3", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(2L).setType("folder").setParentId(12L).setPage("MyPage3");
        getProfileAPI().createProfileEntry(profileEntryCreator3);

        // insert the element between 0 and 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", adminProfileId).setDescription("Description profileEntry1")
                .setIndex(1L).setType("folder").setParentId(12L).setPage("MyPage4");
        final ProfileEntry createdProfileEntry = getProfileAPI().createProfileEntry(profileEntryCreator2);

        final ProfileEntry getProfileEntryResult = getProfileAPI().getProfileEntry(createdProfileEntry.getId());
        assertEquals(2L, getProfileEntryResult.getIndex());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Create", "No index" }, story = "Create profile entry without index.", jira = "")
    @Test
    public void createProfileEntryWithoutIndex() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1");
        final long profileId = createdProfile.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder");
        final ProfileEntry profileEntry1 = getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setIndex(2L).setType("folder");
        final ProfileEntry profileEntry2 = getProfileAPI().createProfileEntry(profileEntryCreator2);

        // Create Profile entry 3 without Index
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntryWithoutIndex", profileId).setDescription(
                "Description profileEntryWithoutIndex").setType("folder");
        final ProfileEntry profileEntryWithoutIndex = getProfileAPI().createProfileEntry(profileEntryCreator3);

        final ProfileEntry getProfileEntryResult = getProfileAPI().getProfileEntry(profileEntryWithoutIndex.getId());
        assertEquals(profileEntryWithoutIndex.getId(), getProfileEntryResult.getId());
        assertEquals(4L, getProfileEntryResult.getIndex());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        final List<ProfileEntry> profileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(profileEntry1, profileEntries.get(0));
        assertEquals(profileEntry2, profileEntries.get(1));
        assertEquals(profileEntryWithoutIndex, profileEntries.get(2));

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Test
    public void createProfileEntryNico() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1");
        final long profileId = createdProfile.getId();

        // Create Folder Profile Entry
        final ProfileEntryCreator folderCreator = new ProfileEntryCreator("a", profileId).setType("folder");
        final ProfileEntry folderProfileEntry = getProfileAPI().createProfileEntry(folderCreator);

        final List<ProfileEntry> profileEntries = new ArrayList<ProfileEntry>();

        // Create Profile entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("tasklistinguser")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator1));

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("tasklistingadmin")
                .setParentId(folderProfileEntry.getId()).setCustom(true);
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator2));

        // Create Profile entry 3
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("caselistinguser")
                .setParentId(folderProfileEntry.getId()).setCustom(false);
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator3));

        // Create Profile entry 4
        final ProfileEntryCreator profileEntryCreator4 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("caselistingadmin")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator4));

        // Create Profile entry 5
        final ProfileEntryCreator profileEntryCreator5 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("processlistinguser")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator5));

        // Create Profile entry 6
        final ProfileEntryCreator profileEntryCreator6 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("processlistingadmin")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator6));

        // Create Profile entry 7
        final ProfileEntryCreator profileEntryCreator7 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("userlistingadmin")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator7));

        // Create Profile entry 8
        final ProfileEntryCreator profileEntryCreator8 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("grouplistingadmin")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator8));

        // Create Profile entry 9
        final ProfileEntryCreator profileEntryCreator9 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("rolelistingadmin")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator9));

        // Create Profile entry 10
        final ProfileEntryCreator profileEntryCreator10 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("importexportorganization")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator10));

        // Create Profile entry 11
        final ProfileEntryCreator profileEntryCreator11 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("profilelisting")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator11));

        // Create Profile entry 12
        final ProfileEntryCreator profileEntryCreator12 = new ProfileEntryCreator("", profileId).setType(ENTRY_TYPE_LINK).setPage("reportlistingadminext")
                .setParentId(folderProfileEntry.getId());
        profileEntries.add(getProfileAPI().createProfileEntry(profileEntryCreator12));

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 20);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        builder.filter(ProfileEntrySearchDescriptor.PARENT_ID, folderProfileEntry.getId());
        final List<ProfileEntry> resultProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        for (final ProfileEntry resultProfileEntry : resultProfileEntries) {
            assertEquals(profileEntries.get(resultProfileEntries.indexOf(resultProfileEntry)).getId(), resultProfileEntry.getId());
        }
        assertEquals(0L, resultProfileEntries.get(0).getIndex());
        assertFalse(resultProfileEntries.get(0).isCustom());
        assertEquals(2L, resultProfileEntries.get(1).getIndex());
        assertTrue(resultProfileEntries.get(1).isCustom());
        assertEquals(4L, resultProfileEntries.get(2).getIndex());
        assertFalse(resultProfileEntries.get(2).isCustom());
        assertEquals(6L, resultProfileEntries.get(3).getIndex());
        assertEquals(8L, resultProfileEntries.get(4).getIndex());
        assertEquals(10L, resultProfileEntries.get(5).getIndex());
        assertEquals(12L, resultProfileEntries.get(6).getIndex());
        assertEquals(14L, resultProfileEntries.get(7).getIndex());
        assertEquals(16L, resultProfileEntries.get(8).getIndex());
        assertEquals(18L, resultProfileEntries.get(9).getIndex());
        assertEquals(20L, resultProfileEntries.get(10).getIndex());
        assertEquals(22L, resultProfileEntries.get(11).getIndex());

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Test
    public void searchProfileEntryByPage() throws BonitaException {

        // given
        final String pageToSearch = "tasklistinguser";
        final String profileEntryName = "entry1";

        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1");
        final long profileId = createdProfile.getId();

        // Create Folder Profile Entry
        final ProfileEntryCreator folderCreator = new ProfileEntryCreator("folderName", profileId).setType("folder");
        final ProfileEntry folderProfileEntry = getProfileAPI().createProfileEntry(folderCreator);
        final List<ProfileEntry> profileEntries = new ArrayList<ProfileEntry>();

        // custom page
        final ProfileEntry createProfileEntry = getProfileAPI().createProfileEntry(
                new ProfileEntryCreator(profileEntryName, profileId).setType(ENTRY_TYPE_LINK).setPage(pageToSearch)
                        .setDescription(ENTRY_DESCRIPTION)
                        .setCustom(true)
                        .setParentId(folderProfileEntry.getId()));
        profileEntries.add(createProfileEntry);

        // page with same name but not a custom page
        profileEntries.add(getProfileAPI().createProfileEntry(
                new ProfileEntryCreator(profileEntryName, profileId).setType(ENTRY_TYPE_LINK).setPage(pageToSearch)
                        .setDescription(ENTRY_DESCRIPTION)
                        .setCustom(false)
                        .setParentId(folderProfileEntry.getId())));

        profileEntries.add(getProfileAPI().createProfileEntry(new ProfileEntryCreator("entry2", profileId).setType(ENTRY_TYPE_LINK).setPage("tasklistingadmin")
                .setDescription(ENTRY_DESCRIPTION)
                .setParentId(folderProfileEntry.getId()).setCustom(true)));

        profileEntries.add(getProfileAPI().createProfileEntry(new ProfileEntryCreator("entry3", profileId).setType(ENTRY_TYPE_LINK).setPage("caselistinguser")
                .setDescription(ENTRY_DESCRIPTION)
                .setParentId(folderProfileEntry.getId()).setCustom(false)));

        // when

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 20);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PAGE, pageToSearch);
        builder.filter(ProfileEntrySearchDescriptor.CUSTOM, new Boolean(true));

        final List<ProfileEntry> resultProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();

        // then
        assertThat(resultProfileEntries).as("should contain 1 item with pageToSearch").hasSize(1).containsOnly(createProfileEntry);

        // cleanup
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Wrong parameter" }, story = "Execute profile command with wrong parameter", jira = "ENGINE-548")
    @Test(expected = CreationException.class)
    public void createProfileEntryWithWrongParameter() throws Exception {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator("ProfileEntry2", adminProfileId).setDescription("Description profileEntry2")
                .setType(ENTRY_TYPE_LINK);
        getProfileAPI().createProfileEntry(profileEntryCreator);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update" }, story = "Update profile entry.", jira = "")
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
        updateDescriptor.type(ENTRY_TYPE_LINK);
        updateDescriptor.page("myPage");
        updateDescriptor.index(0L);
        final ProfileEntry upDateProfileEntryResult = getProfileAPI().updateProfileEntry(createdProfile.getId(), updateDescriptor);
        assertEquals("UpdatedProfileEntry3", upDateProfileEntryResult.getName());
        assertEquals("Updated Description profileEntry3", upDateProfileEntryResult.getDescription());
        assertEquals("myPage", upDateProfileEntryResult.getPage());

        // Delete profile Entry 1 using id
        getProfileAPI().deleteProfileEntry(createdProfile.getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Delete", "Index" }, story = "Update profile entry index on delete.", jira = "")
    @Test
    public void updateProfileEntryIndexOnDelete() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1");
        final Long profileId = createdProfile.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setIndex(2L).setType("folder").setPage("toto");
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
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1");
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

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Insert", "Index" }, story = "Update profile entry index on insert.", jira = "")
    @Test
    public void updateProfileEntryIndexOnInsert() throws BonitaException {
        // Create Profile Entry 1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1");
        final long profileId = createdProfile.getId();

        // Create Profile Entry 1
        final ProfileEntryCreator profileEntryCreator1 = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator1);

        // Create Profile Entry 3
        final ProfileEntryCreator profileEntryCreator3 = new ProfileEntryCreator("ProfileEntry3", profileId).setDescription("Description profileEntry3")
                .setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator3);

        // Create Profile entry 2
        final ProfileEntryCreator profileEntryCreator2 = new ProfileEntryCreator("ProfileEntry2", profileId).setDescription("Description profileEntry2")
                .setType("folder");
        getProfileAPI().createProfileEntry(profileEntryCreator2);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);
        final List<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertEquals(0L, searchedProfileEntries.get(0).getIndex());
        assertEquals("ProfileEntry1", searchedProfileEntries.get(0).getName());
        assertEquals(2L, searchedProfileEntries.get(1).getIndex());
        assertEquals("ProfileEntry3", searchedProfileEntries.get(1).getName());
        assertEquals(4L, searchedProfileEntries.get(2).getIndex());
        assertEquals("ProfileEntry2", searchedProfileEntries.get(2).getName());

        // Delete profile1
        getProfileAPI().deleteProfile(profileId);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Update", "Index" }, story = "Update profile entry index on update.", jira = "")
    @Test
    public void updateProfileEntryIndexOnUpdate() throws BonitaException {
        // Create Profile1
        final Profile createdProfile = getProfileAPI().createProfile("Profile1", "Description profile1");
        final long profileId = createdProfile.getId();

        // Create Profile Entry Menu1
        final ProfileEntryCreator profileEntryCreatorMenu1 = new ProfileEntryCreator("Menu1", profileId).setDescription("Description Menu1").setIndex(0L)
                .setType("folder");
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
        updateDescriptor.type(ENTRY_TYPE_LINK);
        updateDescriptor.page("");
        getProfileAPI().updateProfileEntry(createdProfileEntry.getId(), updateDescriptor);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "Not Existing" }, story = "Execute profile command with not existing", jira = "ENGINE-548")
    @Test(expected = UpdateException.class)
    public void updateProfileEntryNotExisting() throws Exception {
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.type(ENTRY_TYPE_LINK);
        updateDescriptor.page(null);
        updateDescriptor.index(0L);
        getProfileAPI().updateProfileEntry(16464654L, updateDescriptor);
    }

    private Map<String, ProfileEntry> beforeIndexTests() throws AlreadyExistsException, CreationException {

        final ProfileCreator profileCreator = new ProfileCreator("myprofile");
        final Profile profile = getProfileAPI().createProfile(profileCreator);

        final long profileId = profile.getId();
        final ProfileEntryCreator folderProfileEntryCreator = new ProfileEntryCreator("folder", profileId).setDescription("the first profile entry").setType(
                "folder");

        final ProfileEntry folderProfileEntry = getProfileAPI().createProfileEntry(folderProfileEntryCreator);

        final ProfileEntryCreator firstProfileEntryCreator = new ProfileEntryCreator("FirstProfileEntry", profileId).setDescription("the first profile entry")
                .setType("page").setIndex(0L).setPage("MyPage1").setParentId(folderProfileEntry.getId());

        final ProfileEntryCreator secondProfileEntryCreator = new ProfileEntryCreator("secondProfileEntry", profileId)
                .setDescription("the second profile entry").setIndex(2L).setType("page").setPage("MyPage2").setParentId(folderProfileEntry.getId());
        final ProfileEntryCreator thirdProfileEntryCreator = new ProfileEntryCreator("thirdProfileEntry", profileId).setDescription("the third profile entry")
                .setIndex(4L).setType("page").setPage("MyPage3").setParentId(folderProfileEntry.getId());

        final ProfileEntry firstProfileEntry = getProfileAPI().createProfileEntry(firstProfileEntryCreator);
        final ProfileEntry secondProfileEntry = getProfileAPI().createProfileEntry(secondProfileEntryCreator);
        final ProfileEntry thirdProfileEntry = getProfileAPI().createProfileEntry(thirdProfileEntryCreator);
        final HashMap<String, ProfileEntry> returnedMap = new HashMap<String, ProfileEntry>();

        returnedMap.put("1st", firstProfileEntry);
        returnedMap.put("2nd", secondProfileEntry);
        returnedMap.put("3rd", thirdProfileEntry);
        return returnedMap;

    }

    private void cleanProfilesEntriesIndexTest(final Map<String, ProfileEntry> profileEntries) throws DeletionException {
        // Clean up
        getProfileAPI().deleteProfileEntry(profileEntries.get("1st").getId());
        getProfileAPI().deleteProfileEntry(profileEntries.get("2nd").getId());
        getProfileAPI().deleteProfileEntry(profileEntries.get("3rd").getId());
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "First" }, story = "Need to support Profile entries indexation for drag and Drop", jira = "ENGINE-1644")
    @Test
    public void updateProfileEntryToFirstPosition() throws Exception {
        // create profiles
        final HashMap<String, ProfileEntry> profileEntries = (HashMap<String, ProfileEntry>) beforeIndexTests();
        ProfileEntry firstProfileEntry = profileEntries.get("1st");
        ProfileEntry secondProfileEntry = profileEntries.get("2nd");
        ProfileEntry thirdProfileEntry = profileEntries.get("3rd");

        // update the second profile entry to set his index to the first position
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.index(-1);
        getProfileAPI().updateProfileEntry(secondProfileEntry.getId(), updateDescriptor);

        secondProfileEntry = getProfileAPI().getProfileEntry(secondProfileEntry.getId());
        firstProfileEntry = getProfileAPI().getProfileEntry(firstProfileEntry.getId());
        thirdProfileEntry = getProfileAPI().getProfileEntry(thirdProfileEntry.getId());

        assertEquals(0L, secondProfileEntry.getIndex());// the second profile entry must have the first index (0) after updating and re-indexing
        assertEquals(2L, firstProfileEntry.getIndex());// the first profile entry must have the second index after re-indexing
        assertEquals(4L, thirdProfileEntry.getIndex());// the third profile entry must keep his index after re-indexing

        // clean up data
        cleanProfilesEntriesIndexTest(profileEntries);

    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry", "First" }, story = "Need to support Profile entries indexation for drag and Drop", jira = "ENGINE-1644")
    @Test
    public void updateProfileEntryToLastPosition() throws Exception {
        // create profiles
        final HashMap<String, ProfileEntry> profileEntries = (HashMap<String, ProfileEntry>) beforeIndexTests();

        ProfileEntry firstProfileEntry = profileEntries.get("1st");
        ProfileEntry secondProfileEntry = profileEntries.get("2nd");
        ProfileEntry thirdProfileEntry = profileEntries.get("3rd");

        // set to the last position assume profileEntries has 3 elements
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.index(5);

        secondProfileEntry = getProfileAPI().updateProfileEntry(secondProfileEntry.getId(), updateDescriptor);
        firstProfileEntry = getProfileAPI().getProfileEntry(firstProfileEntry.getId());
        thirdProfileEntry = getProfileAPI().getProfileEntry(thirdProfileEntry.getId());

        assertEquals(0L, firstProfileEntry.getIndex());// the first profile entry must keep index (0) after updating and re-indexing
        assertEquals(2L, thirdProfileEntry.getIndex());// the third profile entry must have the second index (2) after re-indexing
        assertEquals(4L, secondProfileEntry.getIndex());// the second profile entry must have the last index (4)

        // clean up data
        cleanProfilesEntriesIndexTest(profileEntries);

    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile entry" }, story = "Need to support Profile entries indexation for drag and Drop", jira = "ENGINE-1644")
    @Test
    public void updateProfileEntryAfterAnotherOne() throws Exception {
        // create profiles
        final HashMap<String, ProfileEntry> profileEntries = (HashMap<String, ProfileEntry>) beforeIndexTests();
        ProfileEntry firstProfileEntry = profileEntries.get("1st");
        ProfileEntry secondProfileEntry = profileEntries.get("2nd");
        ProfileEntry thirdProfileEntry = profileEntries.get("3rd");

        // set to the last position assume profileEntries has 3 elements
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();

        // move the third element in second place
        updateDescriptor.index(1);
        thirdProfileEntry = getProfileAPI().updateProfileEntry(thirdProfileEntry.getId(), updateDescriptor);
        secondProfileEntry = getProfileAPI().getProfileEntry(secondProfileEntry.getId());
        firstProfileEntry = getProfileAPI().getProfileEntry(firstProfileEntry.getId());

        assertEquals(0L, firstProfileEntry.getIndex());// the first profile entry must keep index (0) after updating and re-indexing
        assertEquals(2L, thirdProfileEntry.getIndex());// the third profile entry must have the second index (2) after re-indexing
        assertEquals(4L, secondProfileEntry.getIndex());// the second profile entry must have the last index (4)

        // clean up data
        cleanProfilesEntriesIndexTest(profileEntries);
    }

    @Test
    public void create_profileEntry_updates_profile_metaData() throws BonitaException {
        // given
        final long profileId = adminProfileId;
        final Profile profileBefore = getProfileAPI().getProfile(profileId);

        // when
        logoutOnTenant();
        loginOnDefaultTenantWith("userName1", "User1Pwd");
        createProfileEntry(profileId);

        // then
        final Profile profileAfterInsert = getProfileAPI().getProfile(profileId);
        checkMetaData(profileBefore, profileAfterInsert, user1);
    }

    @Test
    public void update_profileEntry_updates_profile_metaData() throws BonitaException {
        // given
        final ProfileEntry createdProfileEntry = createProfileEntry(adminProfileId);
        final Profile profileAfterInsert = getProfileAPI().getProfile(adminProfileId);

        // when updating profile entry
        logoutOnTenant();
        loginOnDefaultTenantWith("userName2", "User2Pwd");
        final ProfileEntryUpdater updateDescriptor = new ProfileEntryUpdater();
        updateDescriptor.name("UpdatedProfileEntry");
        getProfileAPI().updateProfileEntry(createdProfileEntry.getId(), updateDescriptor);

        // then
        final Profile profileAfterUpdate = getProfileAPI().getProfile(adminProfileId);
        checkMetaData(profileAfterInsert, profileAfterUpdate, user2);

    }

    @Test
    public void delete_profileEntry_updates_profile_metaData() throws BonitaException {
        // given
        final Profile profileBefore = getProfileAPI().getProfile(adminProfileId);
        final ProfileEntry createProfileEntry = createProfileEntry(adminProfileId);

        // when
        logoutOnTenant();
        loginOnDefaultTenantWith("userName3", "User3Pwd");
        getProfileAPI().deleteProfileEntry(createProfileEntry.getId());

        // then
        final Profile profileAfterDelete = getProfileAPI().getProfile(adminProfileId);
        checkMetaData(profileBefore, profileAfterDelete, user3);

    }

    private void checkMetaData(final Profile profileBefore, final Profile profileAfter, final User user) {
        assertThat(profileAfter.getLastUpdateDate()).as("lastUpdateDate should be modified").isAfter(profileBefore.getLastUpdateDate());
        assertThat(profileAfter.getLastUpdatedBy()).as("lastUpdatedBy should be modified").isNotEqualTo(profileBefore.getLastUpdatedBy());
        assertThat(profileAfter.getLastUpdatedBy()).as("lastUpdatedBy should be modified").isEqualTo(user.getId());

    }

    private ProfileEntry createProfileEntry(final long profileId) throws CreationException {
        final ProfileEntryCreator profileEntryCreator = new ProfileEntryCreator("ProfileEntry1", profileId).setDescription("Description profileEntry1")
                .setIndex(0L).setType("folder").setParentId(1L).setPage("MyPage");
        return getProfileAPI().createProfileEntry(profileEntryCreator);
    }

}
