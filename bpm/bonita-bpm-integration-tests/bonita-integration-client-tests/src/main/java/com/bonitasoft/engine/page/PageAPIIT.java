/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
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
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.profile.ProfileEntryCreator;
import com.bonitasoft.engine.profile.ProfileEntryType;

@SuppressWarnings("javadoc")
public class PageAPIIT extends CommonAPISPTest {

    private static final String DESCRIPTION_PAGE = "page";

    private static final String DESCRIPTION_CUSTOM_PAGE = "custom page";

    private static final String ENTRY_TYPE_LINK = ProfileEntryType.LINK.toString().toLowerCase();

    private static final String ENTRY_TYPE_FOLDER = ProfileEntryType.FOLDER.toString().toLowerCase();

    private static final String ENTRY_NAME = "entryName";

    private static final String CONTENT_NAME = "content.zip";

    private static final String PAGE_DESCRIPTION = "page description";

    private static final String PAGE_NAME2 = "page2";

    private static final String PAGE_NAME1 = "page1";

    private static final String INDEX_GROOVY = "Index.groovy";

    private static final String INDEX_HTML = "index.html";

    @Before
    public void before() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
    }

    @After
    public void after() throws BonitaException {
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        for (final Page page : searchPages.getResult()) {
            if (!page.isProvided()) {
                getPageAPI().deletePage(page.getId());
            }
        }
       logoutOnTenant();
    }

    @Test
    public void should_getPage_return_the_page() throws BonitaException {
        // given
        final byte[] pageContent = getPageContent(INDEX_GROOVY);
        final Page page = getPageAPI().createPage(new PageCreator("mypage", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                pageContent);

        // when
        final Page returnedPage = getPageAPI().getPage(page.getId());

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
        final byte[] pageContent = getPageContent(INDEX_GROOVY);
        final String pageName = "mypage";
        final Page page = getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                pageContent);
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

        final Page returnedPage = getPageAPI().updatePage(page.getId(), pageUpdater);

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
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        deleteUser(john);
        deleteUser(jack);

    }

    @Test(expected = AlreadyExistsException.class)
    public void updatePage_with_existing_name_should_fail() throws BonitaException {
        final byte[] pageContent = getPageContent(INDEX_GROOVY);
        final PageUpdater pageUpdater = new PageUpdater();

        // given
        getPageAPI().createPage(new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                pageContent);
        final Page page2 = getPageAPI().createPage(new PageCreator(PAGE_NAME2, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                pageContent);

        // when
        pageUpdater.setName(PAGE_NAME1);
        getPageAPI().updatePage(page2.getId(), pageUpdater);

        // then
        // exception

    }

    @Test
    public void should_update_content_return_the_modified_content() throws BonitaException {
        // given
        final long currentTimeMillis = System.currentTimeMillis();
        final byte[] oldContent = getPageContent(INDEX_GROOVY);
        final Page page = getPageAPI().createPage(new PageCreator("mypage", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                oldContent);
        final long pageId = page.getId();

        // when
        final byte[] newContent = getPageContent(INDEX_HTML);
        getPageAPI().updatePageContent(pageId, newContent);
        final byte[] returnedPageContent = getPageAPI().getPageContent(pageId);
        final Page returnedPage = getPageAPI().getPage(pageId);

        // then
        assertThat(returnedPageContent).isEqualTo(newContent);
        assertThat(returnedPage.getLastModificationDate()).isAfter(new Date(currentTimeMillis));

    }

    @Test
    public void should_getPage_by_name_return_the_page() throws BonitaException {
        // given
        final byte[] pageContent = getPageContent(INDEX_GROOVY);
        final Page page = getPageAPI().createPage(new PageCreator("mypage", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                pageContent);

        // when
        final Page returnedPage = getPageAPI().getPageByName(page.getName());

        // then
        assertThat(returnedPage).isEqualTo(page);
    }

    @Test(expected = AlreadyExistsException.class)
    public void should_createPage_with_same_name_throw_already_exists() throws BonitaException {
        // , "content.zip"given
        final byte[] pageContent = getPageContent(INDEX_GROOVY);
        getPageAPI().createPage(new PageCreator("mypagedup", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                pageContent);

        // when
        getPageAPI().createPage(new PageCreator("mypagedup", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                pageContent);

        // then: expected exception
    }

    @Test
    public void should_getPageContent_return_the_content() throws BonitaException {
        // given
        final byte[] bytes = getPageContent(INDEX_GROOVY);
        final Page page = getPageAPI().createPage(
                new PageCreator("mypagewithcontent", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                bytes);

        // when
        final byte[] pageContent = getPageAPI().getPageContent(page.getId());

        // then
        assertThat(pageContent).isEqualTo(bytes);
    }

    @Test(expected = PageNotFoundException.class)
    public void deletePage_should_delete_the_page() throws BonitaException {

        // given
        final byte[] bytes = getPageContent(INDEX_GROOVY);
        final Page page = getPageAPI().createPage(
                new PageCreator("mypagetodelete", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                bytes);

        // when
        getPageAPI().deletePage(page.getId());

        // then
        getPageAPI().getPage(page.getId());
    }

    @Test
    public void deletePage_should_delete_profile_entry() throws BonitaException {
        // given

        // page
        final Page page = getPageAPI().createPage(
                new PageCreator("myPageToDelete", CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                getPageContent(INDEX_GROOVY));

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
        getPageAPI().deletePage(page.getId());

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
        final Page page = getPageAPI().createPage(
                new PageCreator("myPageToDelete_" + System.currentTimeMillis(), CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                getPageContent(INDEX_GROOVY));

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
        getPageAPI().deletePage(page.getId());

        // then
        getProfileAPI().getProfileEntry(folderProfileEntry.getId()); // ProfileEntryNotFoundException

    }

    @Test
    public void deletePage_with_no_parent_profile_entry() throws BonitaException {
        // given
        final Page page = getPageAPI().createPage(
                new PageCreator("myPageToDelete_" + System.currentTimeMillis(), CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                getPageContent(INDEX_GROOVY));

        final Profile createdProfile = getProfileAPI().createProfile("deletePage_should_delete_parent_profile_entry", "Description profile1", null);
        final long profileId = createdProfile.getId();

        getProfileAPI().createProfileEntry(new ProfileEntryCreator(ENTRY_NAME, profileId).setType(ENTRY_TYPE_LINK)
                .setPage(page.getName())
                .setCustom(new Boolean(true))
                .setDescription(DESCRIPTION_CUSTOM_PAGE));

        // when
        getPageAPI().deletePage(page.getId());

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
        final Page page = getPageAPI().createPage(
                new PageCreator("name_" + System.currentTimeMillis(), CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName("My Päge"),
                getPageContent(INDEX_GROOVY));

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
        pageUpdater.setName("newName_" + System.currentTimeMillis());
        pageUpdater.setDescription(page.getDescription());
        pageUpdater.setContentName(page.getContentName());
        final Page updatePage = getPageAPI().updatePage(page.getId(), pageUpdater);
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
        final String noneMatchingdisplayName = "My Päge";
        final String matchingValue = "Cool";
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(matchingValue);
        stringBuilder.append(" page!");
        final String matchingDisplayName = stringBuilder.toString();
        final byte[] pageContent = getPageContent(INDEX_GROOVY);

        // given
        final int noneMatchingCount = 8;
        for (int i = 0; i < noneMatchingCount; i++) {
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName(), CONTENT_NAME).setDescription(description).setDisplayName(noneMatchingdisplayName),
                    pageContent);
        }
        final Page pageWithMatchingSearchTerm = getPageAPI().createPage(
                new PageCreator(generateUniquePageName(), CONTENT_NAME).setDescription(description).setDisplayName(matchingDisplayName),
                pageContent);

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).searchTerm(matchingValue).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have onlmy one matching page").isEqualTo(1);
        assertThat(results.get(0)).as("should get the page whith matching search term").isEqualTo(pageWithMatchingSearchTerm);
    }

    private String generateUniquePageName() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("unique_page_name_");
        stringBuilder.append(System.currentTimeMillis());
        return stringBuilder.toString();
    }

    @Test
    public void should_8_pages_search_5_first_results_give_5_first_results() throws BonitaException {
        final String displayName = "My Päge";
        final String description = PAGE_DESCRIPTION;
        final byte[] pageContent = getPageContent(INDEX_GROOVY);

        // given
        final int expectedResultSize = 5;
        for (int i = 0; i < expectedResultSize + 3; i++) {
            getPageAPI().createPage(new PageCreator(generateUniquePageName(), CONTENT_NAME).setDescription(description).setDisplayName(displayName),
                    pageContent);
        }

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).done());

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
        final byte[] pageContent = getPageContent(INDEX_GROOVY);
        final String description = PAGE_DESCRIPTION;
        final String matchingDisplayName = "My Päge";
        final String noneMatchingDisplayName = "aaa";

        // given
        final int expectedMatchingResults = 3;
        for (int i = 0; i < expectedMatchingResults; i++) {
            getPageAPI().createPage(new PageCreator(generateUniquePageName(), CONTENT_NAME).setDescription(description).setDisplayName(matchingDisplayName),
                    pageContent);
        }
        getPageAPI().createPage(new PageCreator("anOtherName", CONTENT_NAME).setDescription("an awesome page!!!!!!!").setDisplayName(noneMatchingDisplayName),
                pageContent);

        // when
        final SearchResult<Page> searchPages = getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, expectedMatchingResults + 2).filter(PageSearchDescriptor.DISPLAY_NAME, matchingDisplayName).done());
        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have "
                + +expectedMatchingResults + " results").isEqualTo(expectedMatchingResults);

    }

    @Test
    public void should_search_work_on_desc_order() throws BonitaException {
        final String displayName = "My Päge";
        final String description = PAGE_DESCRIPTION;
        final String firstPageNameInDescOrder = "zPageName";
        final byte[] pageContent = getPageContent(INDEX_GROOVY);

        // given
        final int numberOfNonsMatchingPage = 5;
        for (int i = 0; i < numberOfNonsMatchingPage; i++) {
            getPageAPI().createPage(new PageCreator(generateUniquePageName(), CONTENT_NAME).setDescription(description).setDisplayName(displayName),
                    pageContent);
        }
        final Page expectedMatchingPage = getPageAPI().createPage(
                new PageCreator(firstPageNameInDescOrder, CONTENT_NAME).setDescription(description).setDisplayName(displayName),
                pageContent);

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, 1).sort(PageSearchDescriptor.NAME, Order.DESC).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.get(0)).isEqualTo(expectedMatchingPage);

    }

    private byte[] getPageContent(final String fileNameToInclude) throws BonitaException {
        final byte[] buffer;
        try {
            buffer = IOUtil.zip(Collections.singletonMap(fileNameToInclude, "return \"\";".getBytes()));
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
        return buffer;
    }

}
