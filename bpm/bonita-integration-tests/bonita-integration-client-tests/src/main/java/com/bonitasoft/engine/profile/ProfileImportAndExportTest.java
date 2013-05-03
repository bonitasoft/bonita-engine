package com.bonitasoft.engine.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.search.descriptor.ProfileSearchDescriptor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.exception.profile.ProfileExportException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProfileImportAndExportTest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Export" }, story = "Export all profiles.")
    @Test
    public void exportAllProfiles() throws BonitaException, IOException {
        final Map<Long, Long> numberOfProfileMembers = getProfileAPI().getNumberOfProfileMembers(Arrays.asList(adminProfileId, userProfileId));
        assertNotNull(numberOfProfileMembers);
        assertEquals(2, numberOfProfileMembers.size());
        assertEquals(Long.valueOf(5), numberOfProfileMembers.get(adminProfileId));
        assertEquals(Long.valueOf(1), numberOfProfileMembers.get(userProfileId));

        final byte[] profilebytes = getProfileAPI().exportAllProfiles();

        final String xmlStr = new String(profilebytes);
        final String[] strs = xmlStr.split("profile name=\"");
        assertEquals(5, strs.length);
        assertEquals("Administrator", strs[1].substring(0, strs[1].indexOf('\"')));
        assertEquals("Process owner", strs[2].substring(0, strs[2].indexOf('\"')));
        // assertEquals("Process owner", strs[3].substring(0, strs[3].indexOf("\"")));
        // assertEquals("User", strs[4].substring(0, strs[4].indexOf("\"")));
        final File f = new File("AllProfiles.xml");
        if (!f.exists()) {
            f.createNewFile();
        }
        final FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(profilebytes);
        fileOutputStream.flush();
        fileOutputStream.close();
        f.delete();
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Export" }, story = "Export specified profiles.")
    @Test
    public void exportProfilesSpecified() throws BonitaException, IOException {
        final List<Long> profileIds = new ArrayList<Long>();
        profileIds.add(adminProfileId);
        profileIds.add(userProfileId);
        final Map<Long, Long> numberOfProfileMembers = getProfileAPI().getNumberOfProfileMembers(profileIds);
        assertNotNull(numberOfProfileMembers);
        assertEquals(2, numberOfProfileMembers.size());
        assertEquals(Long.valueOf(5), numberOfProfileMembers.get(adminProfileId));
        assertEquals(Long.valueOf(1), numberOfProfileMembers.get(userProfileId));

        final long[] profIds = { profileIds.get(1).longValue() };
        final byte[] profilebytes = getProfileAPI().exportProfilesWithIdsSpecified(profIds);

        final String xmlStr = new String(profilebytes);
        final String[] strs = xmlStr.split("profile name=\"");
        assertEquals(2, strs.length);
        assertEquals("User", strs[1].substring(0, strs[1].indexOf('\"')));

        final File f = new File("Profiles.xml");
        if (!f.exists()) {
            f.createNewFile();
        }
        final FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(profilebytes);
        fileOutputStream.flush();
        fileOutputStream.close();
        f.delete();
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Import", "Export" }, story = "Import and export profiles.")
    @Test
    public void importAndExport() throws BonitaException, IOException, SAXException {
        final InputStream xmlStream1 = ProfileImportAndExportTest.class.getResourceAsStream("AllProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream1);
        final List<String> warningMsgs1 = getProfileAPI().importProfilesUsingSpecifiedPolicy(xmlContent, ImportPolicy.DELETE_EXISTING);
        assertEquals(0, warningMsgs1.size());

        // profilesHaveBeenImported(4);

        final byte[] profilebytes = getProfileAPI().exportAllProfiles();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.compareXML(new String(xmlContent), new String(profilebytes));
    }

    @SuppressWarnings("unchecked")
    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Import" }, story = "Import profile on other duplicate.")
    @Test
    public void importOnOtherDuplicate() throws BonitaException, IOException {
        final String idKey = "id";
        final String nameKey = "name";
        final String descriptionKey = "description";

        // final InputStream xmlStream = ProfileImportAndExportTest.class.getResourceAsStream("deleteExistingProfile.xml");
        // final List<String> warningMsgs = getProfileAPI().importProfilesUsingSpecifiedPolicy(IOUtils.toByteArray(xmlStream), ImportPolicy.DELETE_EXISTING);
        // assertEquals(0, warningMsgs.size());
        //
        // // profiles
        // SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        // final SearchResult<HashMap<String, Serializable>> searchedProfilesig = getProfileAPI().searchProfiles(builder.done());
        // assertNotNull(searchedProfilesig);
        // assertList(
        // Arrays.asList(1l, "Team Manager", "TM profile"),
        // Arrays.asList(searchedProfilesig.getCount(), searchedProfilesig.getResult().get(0).get(nameKey),
        // searchedProfilesig.getResult().get(0).get(descriptionKey)));
        //
        // profile entries
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        final List<HashMap<String, Serializable>> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntries);
        assertEquals(10, searchedProfileEntries.size());

        /**
         * FailAndIgnoreOnDuplicate
         */
        final InputStream xmlStreamig = ProfileImportAndExportTest.class.getResourceAsStream("failAndIgnoreOnDuplicateProfile.xml");
        final List<String> warningMsgsig = getProfileAPI().importProfilesUsingSpecifiedPolicy(IOUtils.toByteArray(xmlStreamig), ImportPolicy.IGNORE_DUPLICATES);
        assertEquals("Role with name role60 not found.", warningMsgsig.get(0));

        // check profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<HashMap<String, Serializable>> searchedProfilesResig = getProfileAPI().searchProfiles(builder.done());
        final long olderId = (Long) searchedProfilesResig.getResult().get(0).get(idKey);
        final long newId = (Long) searchedProfilesResig.getResult().get(1).get(idKey);
        assertList(
                Arrays.asList(2L, adminProfileId, "Team Manager", "Administrator", "TM profile", "Administrator profile"),
                Arrays.asList(searchedProfilesResig.getCount(), olderId, searchedProfilesResig.getResult().get(0).get(nameKey), searchedProfilesResig
                        .getResult().get(1).get(nameKey), searchedProfilesResig.getResult().get(0).get(descriptionKey), searchedProfilesResig.getResult()
                        .get(1).get(descriptionKey)));
        assertTrue(olderId < newId);

        // check new profile entry
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId);
        final List<HashMap<String, Serializable>> searchedProfileEntriesRes2ig = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes2ig);
        assertList(
                Arrays.asList(1, "BPM", "BPM DES", "folder"),
                Arrays.asList(searchedProfileEntriesRes2ig.size(), searchedProfileEntriesRes2ig.get(0).get(nameKey),
                        searchedProfileEntriesRes2ig.get(0).get(descriptionKey), searchedProfileEntriesRes2ig.get(0).get("type")));

        // check older profile entry unmodified
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, olderId);
        final List<HashMap<String, Serializable>> searchedProfileEntriesRes3 = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes3);
        assertList(Arrays.asList(1, searchedProfileEntries.get(0).get(nameKey), searchedProfileEntries.get(0).get(descriptionKey), searchedProfileEntries
                .get(0).get("type")), Arrays.asList(searchedProfileEntriesRes3.size(), searchedProfileEntriesRes3.get(0).get(nameKey),
                searchedProfileEntriesRes3.get(0).get(descriptionKey), searchedProfileEntriesRes3.get(0).get("type")));

        // check new profile mapping
        final SearchResult<HashMap<String, Serializable>> searchpmRes1 = getProfileAPI().searchProfileMembersForProfile(newId, "user",
                new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(1, searchpmRes1.getCount());
        assertList(Arrays.asList(user3.getId(), newId),
                Arrays.asList(searchpmRes1.getResult().get(0).get("userId"), searchpmRes1.getResult().get(0).get("profileId")));

        final SearchResult<HashMap<String, Serializable>> searchpmRes2 = getProfileAPI().searchProfileMembersForProfile(olderId, "role",
                new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(0, searchpmRes2.getCount());

        /**
         * ReplaceOnDuplicate
         */
        // profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<HashMap<String, Serializable>> searchedProfilesrp = getProfileAPI().searchProfiles(builder.done());
        assertNotNull(searchedProfilesrp);
        assertList(
                Arrays.asList(2l, olderId, "Team Manager", "TM profile", newId, "Administrator", "Administrator profile"),
                Arrays.asList(searchedProfilesrp.getCount(), searchedProfilesrp.getResult().get(0).get(idKey),
                        searchedProfilesrp.getResult().get(0).get(nameKey), searchedProfilesrp.getResult().get(0).get(descriptionKey), searchedProfilesrp
                                .getResult().get(1).get(idKey), searchedProfilesrp.getResult().get(1).get(nameKey),
                        searchedProfilesrp.getResult().get(1).get(descriptionKey)));

        // profile entries
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, olderId);
        final List<HashMap<String, Serializable>> searchedProfileEntriesrl = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesrl);
        assertEquals(1, searchedProfileEntriesrl.size());

        final InputStream xmlStreamrp = ProfileImportAndExportTest.class.getResourceAsStream("replaceOnDuplicateProfile.xml");
        final List<String> warningMsgsrl = getProfileAPI()
                .importProfilesUsingSpecifiedPolicy(IOUtils.toByteArray(xmlStreamrp), ImportPolicy.REPLACE_DUPLICATES);
        assertEquals("Role with name role2 not found.", warningMsgsrl.get(0));

        // check profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<HashMap<String, Serializable>> searchedProfilesResrl = getProfileAPI().searchProfiles(builder.done());
        final long older1 = newId;
        final long newId1 = (Long) searchedProfilesResrl.getResult().get(1).get(idKey);
        final long newId2 = (Long) searchedProfilesResrl.getResult().get(2).get(idKey);
        assertList(Arrays.asList(3l, older1, "Administrator", "Administrator profile", "Process owner", "Process profile", "Team Manager", "TM profile2"),
                Arrays.asList(searchedProfilesResrl.getCount(), searchedProfilesResrl.getResult().get(0).get(idKey), searchedProfilesResrl.getResult().get(0)
                        .get(nameKey), searchedProfilesResrl.getResult().get(0).get(descriptionKey), searchedProfilesResrl.getResult().get(1).get(nameKey),
                        searchedProfilesResrl.getResult().get(1).get(descriptionKey), searchedProfilesResrl.getResult().get(2).get(nameKey),
                        searchedProfilesResrl.getResult().get(2).get(descriptionKey)));

        // check new profile entry
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId1);
        final List<HashMap<String, Serializable>> searchedProfileEntriesRes2rl = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes2rl);
        assertList(
                Arrays.asList(1, "Process", "Process DES", "folder"),
                Arrays.asList(searchedProfileEntriesRes2rl.size(), searchedProfileEntriesRes2rl.get(0).get(nameKey),
                        searchedProfileEntriesRes2rl.get(0).get(descriptionKey), searchedProfileEntriesRes2rl.get(0).get("type")));

        // check older profile entry replaced with new id
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId2);
        final List<HashMap<String, Serializable>> searchedProfileEntriesRes3rl = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes3rl);
        assertList(
                Arrays.asList(1, searchedProfileEntriesrl.get(0).get(nameKey) + "2", searchedProfileEntriesrl.get(0).get(descriptionKey) + "2", "link"),
                Arrays.asList(searchedProfileEntriesRes3rl.size(), searchedProfileEntriesRes3rl.get(0).get(nameKey),
                        searchedProfileEntriesRes3rl.get(0).get(descriptionKey), searchedProfileEntriesRes3rl.get(0).get("type")));

        // check new profile mapping
        final SearchResult<HashMap<String, Serializable>> searchpmRes1rl = getProfileAPI().searchProfileMembersForProfile(newId1, "user",
                new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(1, searchpmRes1rl.getCount());
        assertList(Arrays.asList(user4.getId(), newId1),
                Arrays.asList(searchpmRes1rl.getResult().get(0).get("userId"), searchpmRes1rl.getResult().get(0).get("profileId")));

        // for group
        final SearchResult<HashMap<String, Serializable>> searchpmRes1Group = getProfileAPI().searchProfileMembersForProfile(newId1, "group",
                new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(1, searchpmRes1Group.getCount());
        assertList(Arrays.asList(group1.getId(), newId1),
                Arrays.asList(searchpmRes1Group.getResult().get(0).get("groupId"), searchpmRes1Group.getResult().get(0).get("profileId")));

        // for memebership
        final SearchResult<HashMap<String, Serializable>> searchpmRes1mem = getProfileAPI().searchProfileMembersForProfile(newId1, "roleAndGroup",
                new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(1, searchpmRes1mem.getCount());
        assertList(
                Arrays.asList(group1.getId(), role1.getId(), newId1),
                Arrays.asList(searchpmRes1mem.getResult().get(0).get("groupId"), searchpmRes1mem.getResult().get(0).get("roleId"), searchpmRes1mem.getResult()
                        .get(0).get("profileId")));

        // for user
        final SearchResult<HashMap<String, Serializable>> searchpmRes = getProfileAPI().searchProfileMembersForProfile(newId2, "user",
                new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(2, searchpmRes.getCount());
        assertList(Arrays.asList(user2.getId(), user5.getId()),
                Arrays.asList(searchpmRes.getResult().get(0).get("userId"), searchpmRes.getResult().get(1).get("userId")));

        // for role
        final SearchResult<HashMap<String, Serializable>> searchpmResRole = getProfileAPI().searchProfileMembersForProfile(newId2, "role",
                new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(1, searchpmResRole.getCount());
        assertList(Arrays.asList(role1.getId()), Arrays.asList(searchpmResRole.getResult().get(0).get("roleId")));

        /**
         * ExportAndImport
         */
        final byte[] xmlBytes = getProfileAPI().exportAllProfiles();
        getProfileAPI().importProfilesUsingSpecifiedPolicy(xmlBytes, ImportPolicy.DELETE_EXISTING);

        final byte[] profilebytes = xmlBytes;
        assertEquals(new String(xmlBytes), new String(profilebytes));
    }

    @SuppressWarnings("unchecked")
    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Import" }, story = "Import profiles and delete existing.")
    @Test
    public void importProfilesDeleteExisting() throws BonitaException, IOException {
        final String idKey = "id";
        final String nameKey = "name";
        final String descriptionKey = "description";

        final InputStream xmlStream1 = ProfileImportAndExportTest.class.getResourceAsStream("AllProfiles.xml");
        final List<String> warningMsgs1 = getProfileAPI().importProfilesUsingSpecifiedPolicy(IOUtils.toByteArray(xmlStream1), ImportPolicy.DELETE_EXISTING);
        assertEquals(0, warningMsgs1.size());

        // check current status: profiles and its attributes
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<HashMap<String, Serializable>> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        final long olderid1 = (Long) searchedProfiles.getResult().get(0).get(idKey);
        final long olderid2 = (Long) searchedProfiles.getResult().get(1).get(idKey);
        final long olderid3 = (Long) searchedProfiles.getResult().get(2).get(idKey);
        final long olderid4 = (Long) searchedProfiles.getResult().get(3).get(idKey);
        assertEquals(4, searchedProfiles.getResult().size());
        assertList(Arrays.asList(4l, "Administrator", "Team manager", "Process owner", "User"), Arrays.asList(searchedProfiles.getCount(), searchedProfiles
                .getResult().get(0).get(nameKey), searchedProfiles.getResult().get(1).get(nameKey), searchedProfiles.getResult().get(2).get(nameKey),
                searchedProfiles.getResult().get(3).get(nameKey)));
        assertList(
                Arrays.asList(4l, "Administrator profile", "Team Manager profile", "Process owner profile", "User profile"),
                Arrays.asList(searchedProfiles.getCount(), searchedProfiles.getResult().get(0).get(descriptionKey),
                        searchedProfiles.getResult().get(1).get(descriptionKey), searchedProfiles.getResult().get(2).get(descriptionKey), searchedProfiles
                                .getResult().get(3).get(descriptionKey)));

        // check profile entries and their attributes
        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            builder = new SearchOptionsBuilder(0, 10);
            builder.sort(ProfileEntrySearchDescriptor.PROFILE_ID, Order.ASC);
            builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, i);
            assertNotNull(getProfileAPI().searchProfileEntries(builder.done()).getResult());
        }

        final InputStream xmlStream = ProfileImportAndExportTest.class.getResourceAsStream("deleteExistingProfile.xml");
        final List<String> warningMsgs = getProfileAPI().importProfilesUsingSpecifiedPolicy(IOUtils.toByteArray(xmlStream), ImportPolicy.DELETE_EXISTING);
        assertEquals(0, warningMsgs.size());

        // check profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<HashMap<String, Serializable>> searchedProfilesRes = getProfileAPI().searchProfiles(builder.done());
        final long newId1 = (Long) searchedProfilesRes.getResult().get(0).get(idKey);
        assertTrue(newId1 > olderid4);
        assertList(
                Arrays.asList(1l, "Team Manager", "TM profile"),
                Arrays.asList(searchedProfilesRes.getCount(), searchedProfilesRes.getResult().get(0).get(nameKey),
                        searchedProfilesRes.getResult().get(0).get(descriptionKey)));

        // check profileEntries
        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            builder = new SearchOptionsBuilder(0, 10);
            builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
            builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, i);
            assertTrue(getProfileAPI().searchProfileEntries(builder.done()).getCount() == 0);
        }

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId1);
        final List<HashMap<String, Serializable>> searchedProfileEntriesRes2 = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes2);
        assertList(
                Arrays.asList(1, "Home", "My team activitys dashboard", "CurrentUserTeamTasksDashboard"),
                Arrays.asList(searchedProfileEntriesRes2.size(), searchedProfileEntriesRes2.get(0).get(nameKey),
                        searchedProfileEntriesRes2.get(0).get(descriptionKey), searchedProfileEntriesRes2.get(0).get("type")));

        // check profile mapping
        final SearchResult<HashMap<String, Serializable>> searchpms = getProfileAPI()
                .searchProfileMembersForProfile(newId1, "user", new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        assertEquals(2, searchpms.getCount());
        assertList(
                Arrays.asList(user1.getId(), user2.getId(), newId1, newId1),
                Arrays.asList(searchpms.getResult().get(0).get("userId"), searchpms.getResult().get(1).get("userId"),
                        searchpms.getResult().get(0).get("profileId"), searchpms.getResult().get(1).get("profileId")));

        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            final SearchResult<HashMap<String, Serializable>> searchpms1 = getProfileAPI()
                    .searchProfileMembersForProfile(i, "user", new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
            assertEquals(0, searchpms1.getCount());
        }
    }

    private void assertList(final List<? extends Object> expectedList, final List<Serializable> resultList) {
        if (expectedList != null && resultList != null && resultList.size() == expectedList.size()) {
            for (int i = 0; i < expectedList.size(); i++) {
                assertEquals(expectedList.get(i), resultList.get(i));
            }
        } else {
            Assert.fail("Expected list and real list are not of the same size.");
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Export", "Wrong parameter" }, story = "Execute profile export  with wrong parameter", jira = "ENGINE-586")
    @Test(expected = ProfileExportException.class)
    public void exportProfilesWithIdsSpecifiedWithWrongParameter() throws Exception {
        final long[] profileIds = { 541646L };
        getProfileAPI().exportProfilesWithIdsSpecified(profileIds);
    }

}
