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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.assertj.core.util.xml.XmlStringPrettyFormatter;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.ImportStatus.Status;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.identity.Group;
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
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.page.Page;

public class ProfileImportAndExportSPITest extends AbstractProfileTest {

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Export" }, story = "Export all profiles.", jira = "")
    @Test
    public void exportAllProfiles() throws Exception {
        // given
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(IOUtils.toByteArray(AbstractProfileTest.class
                .getResourceAsStream("AllProfiles.xml"))));

        // when
        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(getProfileAPI().exportAllProfiles()));

        // then
        assertThat(xmlPrettyFormatExported.length()).as("should have samse size").isEqualTo(xmlPrettyFormatExpected.length());
    }

    @Test
    public void exportImportProfile_with_groups() throws Exception {
        final String profileName = "Test";

        // given
        createGroup("acme");
        createGroup("finance", "/acme");
        final Group groupAcme = getIdentityAPI().getGroupByPath("/acme");
        final Group groupFinance = getIdentityAPI().getGroupByPath("/acme/finance");
        assertThat(groupAcme).as("group acme").isNotNull();
        assertThat(groupFinance).as("group finance").isNotNull();

        final byte[] customProfileByteArray = IOUtils.toByteArray(AbstractProfileTest.class
                .getResourceAsStream("Profile2groups.xml"));
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(customProfileByteArray));

        // when import
        final List<ImportStatus> importProfiles = getProfileAPI().importProfiles(customProfileByteArray, ImportPolicy.REPLACE_DUPLICATES);
        assertThat(importProfiles).hasSize(1);
        for (final ImportStatus importStatus : importProfiles) {
            assertThat(importStatus.getErrors()).as("error found is status: %s ", importProfiles).isEmpty();
        }

        // then
        final SearchOptions options = new SearchOptionsBuilder(0, 1).filter(ProfileSearchDescriptor.NAME, profileName).done();
        final SearchResult<Profile> searchProfiles = getProfileAPI().searchProfiles(options);

        assertThat(searchProfiles.getResult()).hasSize(1);
        final Profile importedProfile = searchProfiles.getResult().get(0);
        assertThat(importedProfile.getName()).as("check imported name").isEqualTo(profileName);

        // when export
        final long[] ids = new long[1];
        ids[0] = importedProfile.getId();
        final byte[] exportProfilesWithIdsSpecified = getProfileAPI().exportProfilesWithIdsSpecified(ids);

        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportProfilesWithIdsSpecified));

        // then
        assertThat(xmlPrettyFormatExported).as("xml exported profile should be similar to original xml file").isEqualTo(xmlPrettyFormatExpected);

        deleteGroups(getIdentityAPI().getGroupByPath("/acme"));
    }

    @Test
    public void importProfile_with_teamwork_level() throws Exception {
        // given
        final byte[] profileByteArray = IOUtils.toByteArray(AbstractProfileTest.class
                .getResourceAsStream("Profiles_teamwork.xml"));
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(profileByteArray));

        // when import
        final List<ImportStatus> importProfiles = getProfileAPI().importProfiles(profileByteArray, ImportPolicy.REPLACE_DUPLICATES);

        // then
        assertThat(importProfiles).as("should have 1 imported profiles").hasSize(1);
        for (final ImportStatus importStatus : importProfiles) {
            assertThat(importStatus.getErrors()).as("error found in status: %s ", importStatus).isEmpty();
            assertThat(importStatus.getStatus()).isEqualTo(Status.REPLACED);
        }

        // when export

        final long[] profileIds = new long[1];
        profileIds[0] = adminProfileId;
        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(getProfileAPI().exportProfilesWithIdsSpecified(profileIds)));

        // then
        assertThatXmlHaveNoDifferences(xmlPrettyFormatExpected, xmlPrettyFormatExported);

    }

    private void assertThatXmlHaveNoDifferences(final String xmlPrettyFormatExpected, final String xmlPrettyFormatExported) throws SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        final DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(xmlPrettyFormatExported, xmlPrettyFormatExpected));
        final List<?> allDifferences = diff.getAllDifferences();
        assertThat(allDifferences).as("should have no differences").isEmpty();
    }

    @Test
    public void exportImportProfile_with_custom_page() throws Exception {
        final String profileName = "custom profile";

        // given
        final byte[] customProfileByteArray = IOUtils.toByteArray(AbstractProfileTest.class
                .getResourceAsStream("CustomPageProfile.xml"));
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(customProfileByteArray));

        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 1000).done());
        assertThat(searchPages.getResult()).as("custom pages").hasSize(2);
        final Page groovyExamplePage = getPageAPI().getPageByName("custompage_groovyexample");
        final Page htmlExamplePage = getPageAPI().getPageByName("custompage_htmlexample");
        assertThat(groovyExamplePage).as("custompage_groovyexample should exists").isNotNull();
        assertThat(htmlExamplePage).as("custompage_htmlexample should exists").isNotNull();

        // when import
        final List<ImportStatus> importProfiles = getProfileAPI().importProfiles(customProfileByteArray, ImportPolicy.REPLACE_DUPLICATES);
        for (final ImportStatus importStatus : importProfiles) {
            assertThat(importStatus.getErrors()).as("error found is status: %s ", importProfiles).isEmpty();
        }

        // then

        final SearchOptions options = new SearchOptionsBuilder(0, 1).filter(ProfileSearchDescriptor.NAME, profileName).done();
        final SearchResult<Profile> searchProfiles = getProfileAPI().searchProfiles(options);

        assertThat(searchProfiles.getResult()).hasSize(1);
        final Profile importedCustomProfile = searchProfiles.getResult().get(0);
        assertThat(importedCustomProfile.getName()).as("check imported name").isEqualTo(profileName);

        final long profileId = importedCustomProfile.getId();

        final SearchResult<ProfileEntry> profileEntries = getProfileAPI().searchProfileEntries(
                new SearchOptionsBuilder(0, 3).filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId).done());

        final List<ProfileEntry> result = profileEntries.getResult();
        assertThat(result).as("should have 3 entries: %s", result).hasSize(3);
        int customPageCounter = 0;
        for (final ProfileEntry profileEntry : result) {
            if (profileEntry.getType().equals("link"))
            {
                assertThat(profileEntry.isCustom()).as("entry %s should be a custom page", profileEntry).isTrue();
                customPageCounter++;
            }
        }
        assertThat(customPageCounter).as("should have found 2 custom page").isEqualTo(2);

        // when export
        final long[] ids = new long[1];
        ids[0] = profileId;
        final byte[] exportProfilesWithIdsSpecified = getProfileAPI().exportProfilesWithIdsSpecified(ids);

        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportProfilesWithIdsSpecified));

        // then
        assertThat(xmlPrettyFormatExported).as("xml exported profile should be similar to original xml file").isEqualTo(xmlPrettyFormatExpected);
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Export" }, story = "Export specified profiles.", jira = "")
    @Test
    public void exportProfilesSpecified() throws Exception {
        // given
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(IOUtils.toByteArray(AbstractProfileTest.class
                .getResourceAsStream("AdministratorProfile.xml"))));

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ProfileSearchDescriptor.NAME, "Administrator");
        final List<Profile> profiles = getProfileAPI().searchProfiles(builder.done()).getResult();
        assertEquals(1, profiles.size());
        final Profile profile1 = profiles.get(0);
        final long[] profilesIds = new long[1];
        profilesIds[0] = profile1.getId();

        // when
        final String xmlPrettyFormatExported = XmlStringPrettyFormatter
                .xmlPrettyFormat(new String(getProfileAPI().exportProfilesWithIdsSpecified(profilesIds)));

        // then
        final Diff diff = new Diff(xmlPrettyFormatExported, xmlPrettyFormatExpected);
        if (!diff.similar())
        {
            // nb: similar is regardless of order
            assertThat(xmlPrettyFormatExported).as("xml exported profile should be similar to original xml file").isEqualTo(xmlPrettyFormatExported);
        }

    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Import", "Export" }, story = "Import and export profiles.", jira = "")
    @Test
    public void importAndExport() throws BonitaException, IOException, SAXException {
        final InputStream xmlStream1 = ProfileImportAndExportSPITest.class.getResourceAsStream("AllProfiles.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream1);
        final List<ImportStatus> importStatusList = getProfileAPI().importProfiles(xmlContent, ImportPolicy.DELETE_EXISTING);
        for (final ImportStatus importStatus : importStatusList) {
            assertThat(importStatus.getErrors()).as("error on import").isEmpty();
        }

        // profilesHaveBeenImported(4);

        final byte[] profilebytes = getProfileAPI().exportAllProfiles();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.compareXML(new String(xmlContent), new String(profilebytes));
    }

    @Test
    public void importProfileReplaceDuplicate_should_skip_new_default_profile() throws BonitaException, IOException, SAXException {
        // given
        final InputStream xmlStream1 = ProfileImportAndExportSPITest.class.getResourceAsStream("SkipNewDefaultProfile.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream1);

        // when
        final List<ImportStatus> importStatusResults = getProfileAPI().importProfiles(xmlContent, ImportPolicy.REPLACE_DUPLICATES);

        // then
        assertThat(importStatusResults).hasSize(2);
        final ImportStatus expectedImportStatus = new ImportStatus("Plop");
        expectedImportStatus.setStatus(Status.SKIPPED);

        assertThat(importStatusResults).contains(expectedImportStatus);

    }

    @Test
    public void importProfileReplaceDuplicate_should_skip_default_profile_modification() throws BonitaException, IOException, SAXException {
        // given
        final InputStream xmlStream1 = ProfileImportAndExportSPITest.class.getResourceAsStream("Profile_Data_bug.xml");
        final byte[] xmlContent = IOUtils.toByteArray(xmlStream1);

        // when
        final List<ImportStatus> importStatusResults = getProfileAPI().importProfiles(xmlContent, ImportPolicy.REPLACE_DUPLICATES);

        // then
        final SearchOptions options = new SearchOptionsBuilder(0, 1).filter(ProfileSearchDescriptor.NAME, "Administrator").done();
        final SearchResult<Profile> searchProfiles = getProfileAPI().searchProfiles(options);

        assertThat(searchProfiles.getResult()).hasSize(1);
        assertThat(searchProfiles.getResult().get(0).isDefault()).as("Administrator profile should be a default profile").isTrue();

        final ImportStatus administratorImportStatus = importStatusResults.get(0);
        assertThat(administratorImportStatus.getName()).as("Administrator should have imported").isEqualTo("Administrator");
        assertThat(administratorImportStatus.getStatus()).as("Administrator should have imported").isEqualTo(Status.REPLACED);

    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Import", "Export" }, story = "Import and export profiles.", jira = "")
    @Test
    public void importAndExportWithMissingCustomPage() throws BonitaException, IOException, SAXException {
        final ImportError groupImportError = new ImportError("unknown", Type.GROUP);
        final ImportError pageImportError = new ImportError("unknown", Type.PAGE);
        final ImportError roleImportError = new ImportError("unknown", Type.ROLE);

        // given
        final String xmlAsText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<profiles:profiles xmlns:profiles=\"http://www.bonitasoft.org/ns/profile/6.1\">"
                + "<profile isDefault=\"false\" name=\"ImportExportProfile\"> "
                + "<description>ImportExportProfileDescription</description>"
                + "<profileEntries>"
                + "<parentProfileEntry isCustom=\"true\" name=\"menu3\">"
                + "<parentName>NULL</parentName>"
                + "<index>0</index>"
                + "<type>link</type>"
                + "<page>unknown</page>"
                + "</parentProfileEntry>"
                + "</profileEntries>"
                + "<profileMapping>"
                + "<roles>"
                + "<role>unknown</role>"
                + "</roles>"
                + "<groups>"
                + "<group>unknown</group>"
                + "</groups>"
                + "</profileMapping>"
                + "</profile>"
                + "</profiles:profiles>";

        final byte[] xmlContent = xmlAsText
                .getBytes("UTF-8");

        // when
        final List<ImportStatus> status = getProfileAPI().importProfiles(xmlContent, ImportPolicy.DELETE_EXISTING);

        // then
        assertThat(status.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        final List<ImportError> errors = status.get(0).getErrors();
        assertThat(errors.size()).isEqualTo(3);
        assertThat(errors).as("should have 3 import errors").contains(groupImportError, pageImportError, roleImportError);;

    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Import" }, story = "Import profile on other duplicate.", jira = "")
    @Test
    public void importOnOtherDuplicate() throws BonitaException, IOException {
        // profile entries
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, adminProfileId);
        final List<ProfileEntry> searchedProfileEntries = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntries);
        assertEquals(10, searchedProfileEntries.size());

        /**
         * FailAndIgnoreOnDuplicate
         */
        final InputStream xmlStreamig = ProfileImportAndExportSPITest.class.getResourceAsStream("failAndIgnoreOnDuplicateProfile.xml");
        final List<ImportStatus> warningMsgsig = getProfileAPI().importProfiles(IOUtils.toByteArray(xmlStreamig), ImportPolicy.IGNORE_DUPLICATES);
        final ImportStatus actual = warningMsgsig.get(0);
        assertThat(actual.getName()).isEqualTo("Plop");
        assertThat(actual.getErrors()).hasSize(1);
        assertThat(actual.getErrors().get(0).getName()).isEqualTo("role60");

        // check profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<Profile> searchedProfilesResig = getProfileAPI().searchProfiles(builder.done());
        final List<Profile> result = searchedProfilesResig.getResult();
        Profile profile0 = result.get(0);
        final long olderId = profile0.getId();
        Profile profile1 = result.get(1);
        final long newId = profile1.getId();
        assertEquals(5L, searchedProfilesResig.getCount());
        assertEquals(adminProfileId, olderId);
        assertEquals("Administrator", profile0.getName());
        assertEquals("Administrator profile", profile0.getDescription());
        assertEquals("Team Manager", profile1.getName());
        assertEquals("Team Manager profile", profile1.getDescription());
        assertTrue(olderId < newId);

        // check new profile entry
        builder = new SearchOptionsBuilder(0, 15);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId);
        final List<ProfileEntry> searchedProfileEntriesRes2ig = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes2ig);
        final ProfileEntry profileEntry0 = searchedProfileEntriesRes2ig.get(0);

        assertEquals(11, searchedProfileEntriesRes2ig.size());
        assertEquals("Activity", profileEntry0.getName());
        assertEquals("Activity", profileEntry0.getDescription());
        assertEquals("folder", profileEntry0.getType());

        // check older profile entry unmodified
        builder = new SearchOptionsBuilder(0, 25);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, olderId);
        final List<ProfileEntry> searchedProfileEntriesRes3 = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes3);
        assertEquals(24, searchedProfileEntriesRes3.size());
        assertEquals(searchedProfileEntries.get(0).getName(), searchedProfileEntriesRes3.get(0).getName());
        assertEquals(searchedProfileEntries.get(0).getDescription(), searchedProfileEntriesRes3.get(0).getDescription());
        assertEquals(searchedProfileEntries.get(0).getType(), searchedProfileEntriesRes3.get(0).getType());

        // check new profile mapping
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, Integer.MAX_VALUE);
        searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, newId);
        final SearchResult<ProfileMember> searchpmRes1 = getProfileAPI().searchProfileMembers("user", searchOptionsBuilder.done());
        assertEquals(1, searchpmRes1.getCount());
        assertEquals(user4.getId(), searchpmRes1.getResult().get(0).getUserId());
        assertEquals(newId, searchpmRes1.getResult().get(0).getProfileId());

        searchOptionsBuilder = new SearchOptionsBuilder(0, Integer.MAX_VALUE);
        searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, olderId);
        final SearchResult<ProfileMember> searchpmRes2 = getProfileAPI().searchProfileMembers("role", searchOptionsBuilder.done());
        assertEquals(2, searchpmRes2.getCount());

        /**
         * ReplaceOnDuplicate
         */
        // profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<Profile> searchedProfilesrp = getProfileAPI().searchProfiles(builder.done());
        assertNotNull(searchedProfilesrp);
        final List<Profile> newResult = searchedProfilesrp.getResult();
        profile0 = newResult.get(0);
        profile1 = newResult.get(1);
        assertEquals(5l, searchedProfilesrp.getCount());
        assertEquals(olderId, profile0.getId());
        assertEquals("Administrator", profile0.getName());
        assertEquals("Administrator profile", profile0.getDescription());
        assertEquals(newId, profile1.getId());
        assertEquals("Team Manager", profile1.getName());
        assertEquals("Team Manager profile", profile1.getDescription());

        // profile entries
        builder = new SearchOptionsBuilder(0, 25);
        builder.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, olderId);
        final List<ProfileEntry> searchedProfileEntriesrl = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesrl);
        assertEquals(24, searchedProfileEntriesrl.size());

        final InputStream xmlStreamrp = ProfileImportAndExportSPITest.class.getResourceAsStream("replaceOnDuplicateProfile.xml");
        final List<ImportStatus> warningMsgsrl = getProfileAPI()
                .importProfiles(IOUtils.toByteArray(xmlStreamrp), ImportPolicy.REPLACE_DUPLICATES);

        final ImportStatus actual2 = warningMsgsrl.get(0);
        assertThat(actual2.getName()).isEqualTo("Process owner");
        assertThat(actual2.getErrors()).hasSize(2);
        assertThat(actual2.getErrors().get(0).getName()).isEqualTo("/groupPath1");

        // assertEquals("Unable to find the group /groupPath1 on Process owner", warningMsgsrl.get(0));

        // check profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.NAME, Order.ASC);
        final SearchResult<Profile> searchedProfilesResrl = getProfileAPI().searchProfiles(builder.done());
        final long older1 = newResult.get(0).getId();
        final long newId1 = searchedProfilesResrl.getResult().get(1).getId();
        assertEquals(5l, searchedProfilesResrl.getCount());
        assertEquals(older1, searchedProfilesResrl.getResult().get(0).getId());
        assertEquals("Administrator", searchedProfilesResrl.getResult().get(0).getName());
        assertEquals("Administrator profile", searchedProfilesResrl.getResult().get(0).getDescription());
        assertEquals("Plop", searchedProfilesResrl.getResult().get(1).getName());
        assertEquals("Plop profile", searchedProfilesResrl.getResult().get(1).getDescription());

        // check user profile entries
        final SearchOptionsBuilder builderNewId1 = new SearchOptionsBuilder(0, 25);
        builderNewId1.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        final long userProfileId = getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 1).filter(ProfileEntrySearchDescriptor.NAME, "User").done())
                .getResult().get(0).getId();
        builderNewId1.filter(ProfileEntrySearchDescriptor.PROFILE_ID,
                userProfileId);
        final List<ProfileEntry> searchedProfileEntriesRes2rl = getProfileAPI().searchProfileEntries(builderNewId1.done()).getResult();
        assertNotNull(searchedProfileEntriesRes2rl);
        assertEquals(17, searchedProfileEntriesRes2rl.size());
        assertEquals("All", searchedProfileEntriesRes2rl.get(0).getName());
        assertEquals("Processes current user can run", searchedProfileEntriesRes2rl.get(0).getDescription());
        assertEquals("link", searchedProfileEntriesRes2rl.get(0).getType());

        // check older profile entry replaced with new id
        final SearchOptionsBuilder builderNewId2 = new SearchOptionsBuilder(0, 25);
        builderNewId2.sort(ProfileEntrySearchDescriptor.NAME, Order.ASC);
        builderNewId2.filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId1);
        final List<ProfileEntry> searchedProfileEntriesRes3rl = getProfileAPI().searchProfileEntries(builderNewId2.done()).getResult();
        assertNotNull(searchedProfileEntriesRes3rl);
        assertEquals(1, searchedProfileEntriesRes3rl.size());
        assertEquals("PlopEntry", searchedProfileEntriesRes3rl.get(0).getName());
        assertEquals("BPM DES", searchedProfileEntriesRes3rl.get(0).getDescription());
        assertEquals("folder", searchedProfileEntriesRes3rl.get(0).getType());

        // check new profile mapping
        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 25);
        builder1.filter(ProfileEntrySearchDescriptor.PROFILE_ID, userProfileId);
        final SearchResult<ProfileMember> searchpmRes1rl = getProfileAPI().searchProfileMembers("user", builder1.done());
        assertEquals(0, searchpmRes1rl.getCount());

        // for group
        final SearchResult<ProfileMember> searchpmRes1Group = getProfileAPI().searchProfileMembers("group", builder1.done());
        assertEquals(1, searchpmRes1Group.getCount());
        assertEquals(group1.getId(), searchpmRes1Group.getResult().get(0).getGroupId());
        assertEquals(userProfileId, searchpmRes1Group.getResult().get(0).getProfileId());

        // for memebership
        final SearchResult<ProfileMember> searchpmRes1mem = getProfileAPI().searchProfileMembers("roleAndGroup", builder1.done());
        assertEquals(0, searchpmRes1mem.getCount());

        // for user
        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, 25);
        builder2.filter(ProfileEntrySearchDescriptor.PROFILE_ID, newId1);
        final SearchResult<ProfileMember> searchpmRes = getProfileAPI().searchProfileMembers("user", builder2.done());
        assertEquals(0, searchpmRes.getCount());

        // for role
        final SearchResult<ProfileMember> searchpmResRole = getProfileAPI().searchProfileMembers("role", builder2.done());
        assertEquals(0, searchpmResRole.getCount());

        /**
         * ExportAndImport
         */
        final byte[] xmlBytes = getProfileAPI().exportAllProfiles();
        getProfileAPI().importProfiles(xmlBytes, ImportPolicy.DELETE_EXISTING);

        final byte[] profilebytes = xmlBytes;
        assertEquals(new String(xmlBytes), new String(profilebytes));
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Import" }, story = "Import profiles and delete existing.", jira = "")
    @Test
    public void importProfilesDeleteExisting() throws BonitaException, IOException {
        final InputStream xmlStream1 = ProfileImportAndExportSPITest.class.getResourceAsStream("AllProfiles.xml");
        final List<ImportStatus> importStatusList = getProfileAPI().importProfiles(IOUtils.toByteArray(xmlStream1), ImportPolicy.DELETE_EXISTING);
        for (final ImportStatus importStatus : importStatusList) {
            assertThat(importStatus.getErrors()).as("error on import").isEmpty();
        }

        // check current status: profiles and its attributes
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<Profile> searchedProfiles = getProfileAPI().searchProfiles(builder.done());
        final long olderid1 = searchedProfiles.getResult().get(0).getId();
        final long olderid2 = searchedProfiles.getResult().get(1).getId();
        final long olderid3 = searchedProfiles.getResult().get(2).getId();
        final long olderid4 = searchedProfiles.getResult().get(3).getId();
        assertEquals(4, searchedProfiles.getResult().size());
        assertEquals(4l, searchedProfiles.getCount());
        assertEquals("Administrator", searchedProfiles.getResult().get(0).getName());
        assertEquals("Administrator profile", searchedProfiles.getResult().get(0).getDescription());
        assertTrue(searchedProfiles.getResult().get(0).isDefault());
        assertEquals("Team Manager", searchedProfiles.getResult().get(1).getName());
        assertEquals("Team Manager profile", searchedProfiles.getResult().get(1).getDescription());
        assertTrue(searchedProfiles.getResult().get(1).isDefault());
        assertEquals("Process owner", searchedProfiles.getResult().get(2).getName());
        assertEquals("Process owner profile", searchedProfiles.getResult().get(2).getDescription());
        assertFalse(searchedProfiles.getResult().get(2).isDefault());
        assertEquals("User", searchedProfiles.getResult().get(3).getName());
        assertEquals("User profile", searchedProfiles.getResult().get(3).getDescription());
        assertFalse(searchedProfiles.getResult().get(3).isDefault());

        // check profile entries and their attributes
        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            builder = new SearchOptionsBuilder(0, 10);
            builder.sort(ProfileEntrySearchDescriptor.PROFILE_ID, Order.ASC);
            builder.filter(ProfileEntrySearchDescriptor.PROFILE_ID, i);
            assertNotNull(getProfileAPI().searchProfileEntries(builder.done()).getResult());
        }

        final InputStream xmlStream = ProfileImportAndExportSPITest.class.getResourceAsStream("deleteExistingProfile.xml");
        final List<ImportStatus> importProfileStatus = getProfileAPI().importProfiles(IOUtils.toByteArray(xmlStream), ImportPolicy.DELETE_EXISTING);
        for (final ImportStatus importStatus : importProfileStatus) {
            assertThat(importStatus.getErrors()).as("error on import").isEmpty();
        }

        // check profiles
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProfileSearchDescriptor.ID, Order.ASC);
        final SearchResult<Profile> searchedProfilesRes = getProfileAPI().searchProfiles(builder.done());
        final long newId1 = searchedProfilesRes.getResult().get(0).getId();
        assertTrue(newId1 > olderid4);
        assertEquals(1l, searchedProfilesRes.getCount());
        assertEquals("Team Manager", searchedProfilesRes.getResult().get(0).getName());
        assertEquals("TM profile", searchedProfilesRes.getResult().get(0).getDescription());

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
        final List<ProfileEntry> searchedProfileEntriesRes2 = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertNotNull(searchedProfileEntriesRes2);
        assertEquals(1, searchedProfileEntriesRes2.size());
        assertEquals("Home", searchedProfileEntriesRes2.get(0).getName());
        assertEquals("My team activitys dashboard", searchedProfileEntriesRes2.get(0).getDescription());
        assertEquals("CurrentUserTeamTasksDashboard", searchedProfileEntriesRes2.get(0).getType());

        // check profile mapping
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, Integer.MAX_VALUE);
        searchOptionsBuilder.filter(ProfileMemberSearchDescriptor.PROFILE_ID, newId1);
        final SearchResult<ProfileMember> searchpms = getProfileAPI().searchProfileMembers("user", searchOptionsBuilder.done());
        assertEquals(2, searchpms.getCount());
        assertEquals(user1.getId(), searchpms.getResult().get(0).getUserId());
        assertEquals(user2.getId(), searchpms.getResult().get(1).getUserId());
        assertEquals(newId1, searchpms.getResult().get(0).getProfileId());
        assertEquals(newId1, searchpms.getResult().get(1).getProfileId());

        for (final long i : Arrays.asList(olderid1, olderid2, olderid3, olderid4)) {
            final SearchOptionsBuilder searchOptionsBuilderI = new SearchOptionsBuilder(0, Integer.MAX_VALUE);
            searchOptionsBuilderI.filter(ProfileMemberSearchDescriptor.PROFILE_ID, i);
            final SearchResult<ProfileMember> searchpms1 = getProfileAPI().searchProfileMembers("user", searchOptionsBuilderI.done());
            assertEquals(0, searchpms1.getCount());
        }
    }

    @Cover(classes = ProfileAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Profile", "Export", "Wrong parameter" }, story = "Execute profile export  with wrong parameter", jira = "ENGINE-586")
    @Test(expected = ExecutionException.class)
    public void exportProfilesWithIdsSpecifiedWithWrongParameter() throws Exception {
        final long[] profileIds = { 541646L };
        getProfileAPI().exportProfilesWithIdsSpecified(profileIds);
    }

}
