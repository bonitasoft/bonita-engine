/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingIndexException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageTokenException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageZipContentException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileEntry;
import org.bonitasoft.engine.profile.ProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.profile.ProfileEntryCreator;
import com.bonitasoft.engine.profile.ProfileEntryType;

@SuppressWarnings("javadoc")
@Deprecated
public class PageAPIIT extends CommonAPISPIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String DISPLAY_NAME = "My Päge";

    private static final String DESCRIPTION_PAGE = "page";

    private static final String DESCRIPTION_CUSTOM_PAGE = "custom page";

    private static final String ENTRY_TYPE_LINK = ProfileEntryType.LINK.toString().toLowerCase();

    private static final String ENTRY_TYPE_FOLDER = ProfileEntryType.FOLDER.toString().toLowerCase();

    private static final String ENTRY_NAME = "entryName";

    private static final String CONTENT_NAME = "content.zip";

    private static final String PAGE_DESCRIPTION = "page description";

    private static final String PAGE_NAME2 = "custompage_page2";

    private static final String PAGE_NAME1 = "custompage_page1";

    private static final String INDEX_GROOVY = "Index.groovy";

    private static final String INDEX_HTML = "index.html";

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final SearchResult<Page> searchPages = getSubscriptionPageAPI().searchPages(new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        for (final Page page : searchPages.getResult()) {
            if (!page.isProvided()) {
                getSubscriptionPageAPI().deletePage(page.getId());
            }
        }
    }

    @After
    public void after() throws BonitaException {
        logoutOnTenant();
    }

    @Test
    public void should_getPage_return_the_page() throws BonitaException {
        // given
        final String name = generateUniquePageName(0);
        final byte[] pageContent = createTestPageContent(INDEX_GROOVY, name, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(name, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME), pageContent);

        // when
        final Page returnedPage = getSubscriptionPageAPI().getPage(page.getId());

        // then
        assertThat(returnedPage).isEqualTo(page);
    }

    @Test
    public void updatePage_should_return_the_modified_page() throws BonitaException {
        // given
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");

        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME), pageContent);
        assertThat(page.getInstalledBy()).isEqualTo(john.getId());
        assertThat(page.getLastUpdatedBy()).isEqualTo(john.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");
        // when
        final PageUpdater pageUpdater = new PageUpdater();
        final String newDescription = "new description";
        final String newDisplayName = "new display name";
        final String newContentName = "new_content.zip";
        pageUpdater.setDescription(newDescription);
        pageUpdater.setDisplayName(newDisplayName);
        pageUpdater.setContentName(newContentName);

        final Page returnedPage = getSubscriptionPageAPI().updatePage(page.getId(), pageUpdater);

        // then
        assertThat(returnedPage).as("page should be returned").isNotNull();
        assertThat(returnedPage.getInstalledBy()).isEqualTo(john.getId());
        assertThat(returnedPage.getLastUpdatedBy()).isEqualTo(jack.getId());
        assertThat(returnedPage.getName()).as("page name not changed").isEqualTo(pageName);
        assertThat(returnedPage.getInstallationDate()).as("installation date not changed").isEqualTo(page.getInstallationDate());
        assertThat(returnedPage.getInstalledBy()).as("installed by not changed").isEqualTo(page.getInstalledBy());
        assertThat(returnedPage.getDisplayName()).as("display name should be:" + newDisplayName).isEqualTo(newDisplayName);
        assertThat(returnedPage.getContentName()).as("content name should be:" + newContentName).isEqualTo(newContentName);
        assertThat(returnedPage.getDescription()).as("description should be:" + newDescription).isEqualTo(newDescription);
        assertThat(returnedPage.getLastModificationDate()).as("last modification time should be updated").isAfter(page.getLastModificationDate());

        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(john);
        deleteUser(jack);

    }

    @Test(expected = AlreadyExistsException.class)
    public void updatePage_with_existing_name_should_fail() throws BonitaException {
        final PageUpdater pageUpdater = new PageUpdater();

        // given
        getSubscriptionPageAPI().createPage(new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));
        final Page page2 = getSubscriptionPageAPI().createPage(
                new PageCreator(PAGE_NAME2, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, PAGE_NAME2, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        pageUpdater.setName(PAGE_NAME1);
        getSubscriptionPageAPI().updatePage(page2.getId(), pageUpdater);

        // then
        // exception

    }

    @Test(expected = UpdatingWithInvalidPageZipContentException.class)
    public void updatePageContent_with_bad_content_should_fail() throws BonitaException, IOException {
        // given
        final Page createPage = getSubscriptionPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        getSubscriptionPageAPI().updatePageContent(createPage.getId(), IOUtil.zip(Collections.singletonMap("README.md", "empty file".getBytes())));

        // then
        // exception

    }

    @Test(expected = UpdatingWithInvalidPageTokenException.class)
    public void updatePage_with_bad_token_should_fail() throws BonitaException {
        final PageUpdater pageUpdater = new PageUpdater();

        // given
        final Page createPage = getSubscriptionPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        pageUpdater.setName("invalid token");
        getSubscriptionPageAPI().updatePage(createPage.getId(), pageUpdater);

        // then
        // exception

    }

    public void updatePage_contents_should_updates_page() throws BonitaException {
        // given
        final Page pageBefore = getSubscriptionPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final String newDescription = "new description";
        final String newDisplayName = "new display name";
        final byte[] updatedPageContent = createTestPageContent(INDEX_GROOVY, PAGE_NAME2, newDisplayName, newDescription);
        getSubscriptionPageAPI().updatePageContent(pageBefore.getId(), updatedPageContent);

        // then
        final Page pageAfter = getSubscriptionPageAPI().getPage(pageBefore.getId());
        assertThat(pageAfter.getName()).as("should update page name").isEqualTo(PAGE_NAME2);
        assertThat(pageAfter.getDisplayName()).as("should update page display name").isEqualTo(newDisplayName);
        assertThat(pageAfter.getDescription()).as("should update page name").isEqualTo(newDescription);

    }

    @Test
    public void should_update_content_return_the_modified_content() throws BonitaException, InterruptedException {
        // given
        final Date createTimeMillis = new Date(System.currentTimeMillis());
        final String pageName = generateUniquePageName(0);
        final byte[] oldContent = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                oldContent);
        final long pageId = page.getId();

        // when

        // wait to see modified last update time
        Thread.sleep(1000);

        final Date updateTimeMillis = new Date(System.currentTimeMillis());
        assertThat(updateTimeMillis).as("should wait 1 second").isAfter(createTimeMillis);

        final byte[] newContent = createTestPageContent(INDEX_HTML, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getSubscriptionPageAPI().updatePageContent(pageId, newContent);
        final byte[] returnedPageContent = getSubscriptionPageAPI().getPageContent(pageId);
        final Page returnedPage = getSubscriptionPageAPI().getPage(pageId);

        // then
        checkPageContentContainsProperties(returnedPageContent, DISPLAY_NAME, PAGE_DESCRIPTION);
        assertThat(returnedPage.getLastModificationDate()).as("last modification date should be modified ").isAfter(
                page.getLastModificationDate());
    }

    @Test
    public void should_getPage_by_name_return_the_page() throws BonitaException {
        // given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        final Page returnedPage = getSubscriptionPageAPI().getPageByName(page.getName());

        // then
        assertThat(returnedPage).isEqualTo(page);
    }

    @Test(expected = AlreadyExistsException.class)
    public void should_createPage_with_same_name_throw_already_exists() throws BonitaException {
        // , "content.zip"given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getSubscriptionPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getSubscriptionPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test(expected = InvalidPageTokenException.class)
    public void should_createPage_with_invalid_name_InvalidPageTokenException() throws BonitaException {
        // , "content.zip"given
        final String pageName = "plop";
        final byte[] pageContent = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getSubscriptionPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getSubscriptionPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test(expected = InvalidPageTokenException.class)
    public void should_createPage_with_no_name_InvalidPageTokenException() throws BonitaException {
        // , "content.zip"given
        final String pageName = "";
        final byte[] pageContent = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getSubscriptionPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getSubscriptionPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test
    public void should_createPage_with_invalid_content_InvalidPageZipContentException() throws BonitaException, IOException {
        // , "content.zip"given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = IOUtil.zip(Collections.singletonMap("README.md", "empty file".getBytes()));
        expectedException.expect(InvalidPageZipMissingIndexException.class);
        getSubscriptionPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when

        // then: expected exception
    }

    @Test
    public void should_getPageProperties_throw_alreadyExists() throws BonitaException, IOException {
        // , "content.zip"given
        final byte[] pageContent = IOUtil.zip(Collections.singletonMap("README.md", "empty file".getBytes()));

        expectedException.expect(InvalidPageZipMissingIndexException.class);
        // when
        getSubscriptionPageAPI().getPageProperties(pageContent, true);

        // then: expected exception
    }

    @Test
    public void should_getPageContent_return_the_content() throws BonitaException {
        // given
        final String pageName = generateUniquePageName(0);
        final String pageDescription = "a verry long page description, maybe the longest description you will ever see, check that:"
                + " Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut"
                + " labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris"
                + " nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit "
                + "esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in "
                + "culpa qui officia deserunt mollit anim id est laborum.";
        final byte[] bytes = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, pageDescription);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(pageDescription).setDisplayName(DISPLAY_NAME),
                bytes);

        // when
        final byte[] pageContent = getSubscriptionPageAPI().getPageContent(page.getId());
        // then
        checkPageContentContainsProperties(pageContent, DISPLAY_NAME, pageDescription);
    }

    private void checkPageContentContainsProperties(final byte[] content, final String displayName, final String description) {
        Map<String, String> contentAsMap = null;
        try {
            contentAsMap = unzip(content);
            assertThat(contentAsMap.keySet()).as("should contains page.properties").contains("page.properties");
            final String string = contentAsMap.get("page.properties");
            final Properties props = new Properties();
            props.load(new StringReader(string));
            assertThat(props.getProperty("description")).as("should have same description").isEqualTo(description);
            assertThat(props.getProperty("displayName")).as("should have same displayName").isEqualTo(displayName);
        } catch (final IOException e) {
            fail("unzip error", e);
        }

    }

    private final Map<String, String> unzip(final byte[] zipFile) throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(zipFile);
        final ZipInputStream zipInputstream = new ZipInputStream(bais);
        ZipEntry zipEntry = null;
        final Map<String, String> zipMap = new HashMap<String, String>();
        try {
            while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int bytesRead;
                final byte[] buffer = new byte[4096];
                while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                zipMap.put(zipEntry.getName(), new String(byteArrayOutputStream.toByteArray()));
            }
        } finally {
            zipInputstream.close();
        }
        return zipMap;
    }

    @Test(expected = PageNotFoundException.class)
    public void deletePage_should_delete_the_page() throws BonitaException {
        // given
        final String pageName = generateUniquePageName(0);
        final byte[] bytes = createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                bytes);

        // when
        getSubscriptionPageAPI().deletePage(page.getId());

        // then
        getSubscriptionPageAPI().getPage(page.getId());
    }

    @Test
    public void deletePage_should_delete_profile_entry() throws BonitaException {
        // given
        // page
        final String pageName = generateUniquePageName(0);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        // profile entries
        final Profile createdProfile = getProfileAPI().createProfile("Profile_deletePage_should_delete_profile_entry", "Description profile1", null);
        final long profileId = createdProfile.getId();
        final ProfileEntry folderProfileEntry = getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME, profileId).setType(ENTRY_TYPE_FOLDER));

        final ProfileEntry customPageProfileEntry = getProfileAPI().createProfileEntry(
                new ProfileEntryCreator(ENTRY_NAME + "1", profileId).setType(ENTRY_TYPE_LINK)
                        .setPage(page.getName())
                        .setCustom(new Boolean(true))
                        .setDescription(DESCRIPTION_CUSTOM_PAGE)
                        .setParentId(folderProfileEntry.getId()));

        getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME + "2", profileId).setType(ENTRY_TYPE_LINK)
                .setPage("tasklistingadmin")
                .setParentId(folderProfileEntry.getId()).setCustom(true));
        getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME + "3", profileId).setType(ENTRY_TYPE_LINK)
                .setPage("caselistinguser")
                .setDescription(DESCRIPTION_PAGE)
                .setParentId(folderProfileEntry.getId()).setCustom(false));

        // when
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 20);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PAGE, page.getName());
        builder.filter(ProfileEntrySearchDescriptor.CUSTOM, new Boolean(true));
        final List<ProfileEntry> resultProfileEntriesBefore = getProfileAPI().searchProfileEntries(builder.done()).getResult();

        assertThat(resultProfileEntriesBefore).as("should contain 1 profileEntry with pageToSearch").hasSize(1).containsOnly(customPageProfileEntry);
        getSubscriptionPageAPI().deletePage(page.getId());

        // then

        // no more custom page
        final List<ProfileEntry> resultProfileEntriesAfter = getProfileAPI().searchProfileEntries(builder.done()).getResult();
        assertThat(resultProfileEntriesAfter).as("should delete profile entry").isEmpty();

        // still have normal page
        final SearchOptionsBuilder builderAfterDelete = new SearchOptionsBuilder(0, 20);
        builderAfterDelete.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builderAfterDelete.filter(ProfileEntrySearchDescriptor.PAGE, page.getName());

        final List<ProfileEntry> resultProfileEntriesAfterDelete = getProfileAPI().searchProfileEntries(builderAfterDelete.done()).getResult();
        assertThat(resultProfileEntriesAfterDelete).as("should be empty").isEmpty();

        // cleanup
        getProfileAPI().deleteProfile(profileId);

    }

    @Test(expected = ProfileEntryNotFoundException.class)
    public void deletePage_should_delete_parent_profile_entry() throws BonitaException {
        // given
        final String pageName = generateUniquePageName(0);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        final Profile createdProfile = getProfileAPI().createProfile("deletePage_should_delete_parent_profile_entry", "Description profile1", null);
        final long profileId = createdProfile.getId();
        final ProfileEntry folderProfileEntry = getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME, profileId).setType(ENTRY_TYPE_FOLDER));

        getProfileAPI().createProfileEntry(
                new ProfileEntryCreator(ENTRY_NAME + "1", profileId).setType(ENTRY_TYPE_LINK)
                        .setPage(page.getName())
                        .setCustom(new Boolean(true))
                        .setDescription(DESCRIPTION_CUSTOM_PAGE)
                        .setParentId(folderProfileEntry.getId()));

        // when
        getSubscriptionPageAPI().deletePage(page.getId());

        // then
        getProfileAPI().getProfileEntry(folderProfileEntry.getId()); // ProfileEntryNotFoundException

    }

    @Test
    public void deletePage_with_no_parent_profile_entry() throws BonitaException {
        // given
        final String pageName = "custompage_myPageToDelete" + System.currentTimeMillis();
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, pageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        final Profile createdProfile = getProfileAPI().createProfile("deletePage_should_delete_parent_profile_entry", "Description profile1", null);
        final long profileId = createdProfile.getId();

        getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME, profileId).setType(ENTRY_TYPE_LINK)
                .setPage(page.getName())
                .setCustom(new Boolean(true))
                .setDescription(DESCRIPTION_CUSTOM_PAGE));

        // when
        getSubscriptionPageAPI().deletePage(page.getId());

        // then
        final SearchOptionsBuilder builderAfterDelete = new SearchOptionsBuilder(0, 20);
        builderAfterDelete.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builderAfterDelete.filter(ProfileEntrySearchDescriptor.PROFILE_ID, profileId);

        final List<ProfileEntry> resultProfileEntriesAfterDelete = getProfileAPI().searchProfileEntries(builderAfterDelete.done()).getResult();
        assertThat(resultProfileEntriesAfterDelete).as("profile should have no empty").isEmpty();

        // cleanup
        getProfileAPI().deleteProfile(profileId);

    }

    @Test
    public void updatePage_should_update_profile_entry() throws BonitaException {
        // given
        // page
        final String name = generateUniquePageName(0);
        final Page page = getSubscriptionPageAPI().createPage(
                new PageCreator(name, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                createTestPageContent(INDEX_GROOVY, name, DISPLAY_NAME, PAGE_DESCRIPTION));

        // profile entries
        final Profile createdProfile = getProfileAPI().createProfile("Profile_updatePage_should_update_profile_entry", "Description profile1", null);
        final long profileId = createdProfile.getId();
        final ProfileEntry folderProfileEntry = getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME, profileId).setType(ENTRY_TYPE_FOLDER));

        final ProfileEntry customPageProfileEntry = getProfileAPI().createProfileEntry(
                new ProfileEntryCreator(ENTRY_NAME + "1", profileId).setType(ENTRY_TYPE_LINK)
                        .setPage(page.getName())
                        .setCustom(true)
                        .setDescription(DESCRIPTION_CUSTOM_PAGE)
                        .setParentId(folderProfileEntry.getId()));

        getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME + "2", profileId).setType(ENTRY_TYPE_LINK)
                .setPage("tasklistingadmin")
                .setParentId(folderProfileEntry.getId()).setCustom(true));
        getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME + "3", profileId).setType(ENTRY_TYPE_LINK)
                .setPage("caselistinguser")
                .setDescription(DESCRIPTION_PAGE)
                .setParentId(folderProfileEntry.getId()).setCustom(false));
        // );

        // when
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 20);
        builder.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builder.filter(ProfileEntrySearchDescriptor.PAGE, page.getName());
        builder.filter(ProfileEntrySearchDescriptor.CUSTOM, new Boolean(true));
        final List<ProfileEntry> resultProfileEntriesBefore = getProfileAPI().searchProfileEntries(builder.done()).getResult();

        assertThat(resultProfileEntriesBefore).as("should contain 1 profileEntry with pageToSearch").hasSize(1).containsOnly(customPageProfileEntry);

        final PageUpdater pageUpdater = new PageUpdater();
        pageUpdater.setName("custompage_newName" + System.currentTimeMillis());
        pageUpdater.setDescription(page.getDescription());
        pageUpdater.setContentName(page.getContentName());
        final Page updatePage = getSubscriptionPageAPI().updatePage(page.getId(), pageUpdater);
        // then

        final SearchOptionsBuilder builderAfterUpdate = new SearchOptionsBuilder(0, 20);
        builderAfterUpdate.sort(ProfileEntrySearchDescriptor.INDEX, Order.ASC);
        builderAfterUpdate.filter(ProfileEntrySearchDescriptor.PAGE, updatePage.getName());
        final List<ProfileEntry> resultProfileEntriesAfterDelete = getProfileAPI().searchProfileEntries(builderAfterUpdate.done()).getResult();
        assertThat(resultProfileEntriesAfterDelete).as("should contain 1 profileEntry").hasSize(1);
        assertThat(resultProfileEntriesAfterDelete.get(0).getPage()).as("should contain new custom page name").isEqualTo(updatePage.getName());
        assertThat(resultProfileEntriesAfterDelete.get(0).isCustom()).as("should contain 1 custom page ").isTrue();

        // cleanup
        getProfileAPI().deleteProfile(profileId);

    }

    @Test
    public void should_search_with_search_term() throws BonitaException {
        final String description = "description";
        final String noneMatchingdisplayName = DISPLAY_NAME;
        final String matchingValue = "Cool";
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(matchingValue);
        stringBuilder.append(" page!");
        final String matchingDisplayName = stringBuilder.toString();

        // given
        final int noneMatchingCount = 8;
        for (int i = 0; i < noneMatchingCount; i++) {
            final String generateUniquePageName = generateUniquePageName(i) + i;
            getSubscriptionPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(noneMatchingdisplayName),
                    createTestPageContent(INDEX_GROOVY, generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION));
        }
        final String generateUniquePageName = generateUniquePageName(9);
        final Page pageWithMatchingSearchTerm = getSubscriptionPageAPI().createPage(
                new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(matchingDisplayName),
                createTestPageContent(INDEX_GROOVY, generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final SearchResult<Page> searchPages = getSubscriptionPageAPI().searchPages(new SearchOptionsBuilder(0, 5).searchTerm(matchingValue).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have onlmy one matching page").isEqualTo(1);
        assertThat(results.get(0)).as("should get the page whith matching search term").isEqualTo(pageWithMatchingSearchTerm);
    }

    private String generateUniquePageName(final int i) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("custompage_unique" + i);
        stringBuilder.append(System.currentTimeMillis());
        return stringBuilder.toString();
    }

    @Test
    public void should_8_pages_search_5_first_results_give_5_first_results() throws BonitaException {
        // given
        final int expectedResultSize = 5;
        for (int i = 0; i < expectedResultSize + 3; i++) {
            final String generateUniquePageName = generateUniquePageName(i) + 1;
            final byte[] pageContent = createTestPageContent(INDEX_GROOVY, generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION);
            getSubscriptionPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                    pageContent);
        }

        // when
        final SearchResult<Page> searchPages = getSubscriptionPageAPI().searchPages(new SearchOptionsBuilder(0, 5).done());

        // then
        final List<Page> results = searchPages.getResult();
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("should have only ");
        stringBuilder.append(expectedResultSize);
        stringBuilder.append(" results");
        assertThat(results.size()).as(stringBuilder.toString()).isEqualTo(expectedResultSize);

    }

    @Test
    public void should_search_by_display_name() throws BonitaException {
        // given
        final String description = PAGE_DESCRIPTION;
        final String matchingDisplayName = DISPLAY_NAME;
        final String noneMatchingDisplayName = "aaa";

        // given
        final int expectedMatchingResults = 3;
        for (int i = 0; i < expectedMatchingResults; i++) {
            final String generateUniquePageName = generateUniquePageName(i);
            final byte[] pageContent = createTestPageContent(INDEX_GROOVY, generateUniquePageName, matchingDisplayName, description);
            getSubscriptionPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(matchingDisplayName),
                    pageContent);
        }
        final String anOtherName = generateUniquePageName(4);
        getSubscriptionPageAPI().createPage(
                new PageCreator(anOtherName, CONTENT_NAME).setDescription("an awesome page!!!!!!!").setDisplayName(noneMatchingDisplayName),
                createTestPageContent(INDEX_GROOVY, anOtherName, noneMatchingDisplayName, "an awesome page!!!!!!!"));

        // when
        final SearchResult<Page> searchPages = getSubscriptionPageAPI()
                .searchPages(new SearchOptionsBuilder(0, expectedMatchingResults + 2).filter(PageSearchDescriptor.DISPLAY_NAME, matchingDisplayName).done());
        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have "
                + +expectedMatchingResults + " results").isEqualTo(expectedMatchingResults);

    }

    @Test
    public void should_search_work_on_desc_order() throws BonitaException {
        final String displayName = DISPLAY_NAME;
        final String description = PAGE_DESCRIPTION;
        final String firstPageNameInDescOrder = "custompage_zPageName";

        // given
        final int numberOfNonsMatchingPage = 5;
        for (int i = 0; i < numberOfNonsMatchingPage; i++) {
            final String generateUniquePageName = generateUniquePageName(i) + i;
            final byte[] pageContent = createTestPageContent(INDEX_GROOVY, generateUniquePageName, displayName, description);
            getSubscriptionPageAPI().createPage(new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(displayName),
                    pageContent);
        }
        final Page expectedMatchingPage = getSubscriptionPageAPI().createPage(
                new PageCreator(firstPageNameInDescOrder, CONTENT_NAME).setDescription(description).setDisplayName(displayName),
                createTestPageContent(INDEX_GROOVY, firstPageNameInDescOrder, displayName, description));

        // when
        final SearchResult<Page> searchPages = getSubscriptionPageAPI().searchPages(
                new SearchOptionsBuilder(0, 1).sort(PageSearchDescriptor.NAME, Order.DESC).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.get(0)).isEqualTo(expectedMatchingPage);

    }

    private byte[] createTestPageContent(final String fileNameToInclude, final String pageName, final String displayName, final String description)
            throws BonitaException {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry(fileNameToInclude));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("name=");
            stringBuilder.append(pageName);
            stringBuilder.append("\n");
            stringBuilder.append("displayName=");
            stringBuilder.append(displayName);
            stringBuilder.append("\n");
            stringBuilder.append("description=");
            stringBuilder.append(description);
            stringBuilder.append("\n");
            zos.write(stringBuilder.toString().getBytes());

            zos.closeEntry();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }

}
