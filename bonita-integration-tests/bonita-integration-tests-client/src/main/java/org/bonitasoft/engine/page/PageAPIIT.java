/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.page;

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

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingIndexException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageTokenException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageZipContentException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.CommonTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("javadoc")
public class PageAPIIT extends CommonAPIIT {

    public static final long PROCESS_DEFINITION_ID = 5846L;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String DISPLAY_NAME = "My Päge";

    private static final String CONTENT_NAME = "content.zip";

    private static final String PAGE_DESCRIPTION = "page description";

    private static final String PAGE_NAME2 = "custompage_page2";

    private static final String PAGE_NAME1 = "custompage_page1";
    private static final String UTF8 = "UTF-8";

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
        for (final Page page : searchPages.getResult()) {
            if (!page.isProvided()) {
                getPageAPI().deletePage(page.getId());
            }
        }
    }

    @After
    public void after() throws Exception {
        logoutOnTenant();
    }

    @Test
    public void should_getPage_return_the_page() throws Exception {
        // given
        final String name = generateUniquePageName(0);
        final byte[] pageContent = createTestPageContent(name, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(
                new PageCreator(name, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME).setContentType(ContentType.FORM)
                        .setProcessDefinitionId(PROCESS_DEFINITION_ID),
                pageContent);

        // when
        final Page returnedPage = getPageAPI().getPage(page.getId());

        // then
        assertThat(returnedPage).isEqualToComparingFieldByField(page);
        PageAssert.assertThat(returnedPage)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .hasContentType(ContentType.FORM);
    }

    @Test
    public void updatePage_should_return_the_modified_page() throws Exception {
        // given
        final User john = createUser("john", "bpm");
        final User jack = createUser("jack", "bpm");

        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
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
        pageUpdater.setContentType(ContentType.FORM);
        pageUpdater.setProcessDefinitionId(5L);

        final Page returnedPage = getPageAPI().updatePage(page.getId(), pageUpdater);

        // then
        PageAssert.assertThat(returnedPage)
                .hasInstalledBy(john.getId())
                .hasInstalledBy(page.getInstalledBy())
                .hasLastUpdatedBy(jack.getId())
                .hasName(pageName)
                .hasInstallationDate(page.getInstallationDate())
                .hasDisplayName(newDisplayName)
                .hasContentName(newContentName)
                .hasDescription(newDescription)
                .hasContentType(ContentType.FORM)
                .hasProcessDefinitionId(5L);

        assertThat(returnedPage.getLastModificationDate()).as("last modification time should be updated").isAfter(page.getLastModificationDate());

        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(john);
        deleteUser(jack);

    }

    @Test(expected = AlreadyExistsException.class)
    public void updatePage_with_existing_name_should_fail() throws Exception {
        final PageUpdater pageUpdater = new PageUpdater();

        // given
        getPageAPI().createPage(new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));
        final Page page2 = getPageAPI().createPage(new PageCreator(PAGE_NAME2, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(PAGE_NAME2, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        pageUpdater.setName(PAGE_NAME1);
        getPageAPI().updatePage(page2.getId(), pageUpdater);

        // then
        // exception

    }

    @Test(expected = UpdatingWithInvalidPageZipContentException.class)
    public void updatePageContent_with_bad_content_should_fail() throws Exception {
        // given
        final Page createPage = getPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        getPageAPI().updatePageContent(createPage.getId(), IOUtil.zip(Collections.singletonMap("README.md", "empty file".getBytes())));

        // then
        // exception

    }

    @Test(expected = UpdatingWithInvalidPageTokenException.class)
    public void updatePage_with_bad_token_should_fail() throws Exception {
        final PageUpdater pageUpdater = new PageUpdater();

        // given
        final Page createPage = getPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        pageUpdater.setName("invalid token");
        getPageAPI().updatePage(createPage.getId(), pageUpdater);

        // then
        // exception

    }

    public void updatePage_contents_should_updates_page() throws Exception {
        // given
        final Page pageBefore = getPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final String newDescription = "new description";
        final String newDisplayName = "new display name";
        final byte[] updatedPageContent = CommonTestUtil.createTestPageContent(PAGE_NAME2, newDisplayName, newDescription);
        getPageAPI().updatePageContent(pageBefore.getId(), updatedPageContent);

        // then
        final Page pageAfter = getPageAPI().getPage(pageBefore.getId());
        assertThat(pageAfter.getName()).as("should update page name").isEqualTo(PAGE_NAME2);
        assertThat(pageAfter.getDisplayName()).as("should update page display name").isEqualTo(newDisplayName);
        assertThat(pageAfter.getDescription()).as("should update page name").isEqualTo(newDescription);

    }

    @Test
    public void should_update_content_return_the_modified_content() throws Exception {
        // given
        final Date createTimeMillis = new Date(System.currentTimeMillis());
        final String pageName = generateUniquePageName(0);
        final byte[] oldContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                oldContent);
        final long pageId = page.getId();

        // when

        // wait to see modified last update time
        Thread.sleep(1000);

        final Date updateTimeMillis = new Date(System.currentTimeMillis());
        assertThat(updateTimeMillis).as("should wait 1 second").isAfter(createTimeMillis);

        final byte[] newContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getPageAPI().updatePageContent(pageId, newContent);
        final byte[] returnedPageContent = getPageAPI().getPageContent(pageId);
        final Page returnedPage = getPageAPI().getPage(pageId);

        // then
        checkPageContentContainsProperties(returnedPageContent, DISPLAY_NAME, PAGE_DESCRIPTION);
        assertThat(returnedPage.getLastModificationDate()).as("last modification date should be modified ").isAfter(
                page.getLastModificationDate());
    }

    @Test
    public void should_getPage_by_name_return_the_page() throws Exception {
        // given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        final Page returnedPage = getPageAPI().getPageByName(page.getName());

        // then
        assertThat(returnedPage).isEqualToComparingFieldByField(page);
    }

    @Test(expected = AlreadyExistsException.class)
    public void should_createPage_with_same_name_throw_already_exists() throws Exception {
        // , "content.zip"given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test(expected = InvalidPageTokenException.class)
    public void should_createPage_with_invalid_name_InvalidPageTokenException() throws Exception {
        // , "content.zip"given
        final String pageName = "plop";
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test(expected = InvalidPageTokenException.class)
    public void should_createPage_with_no_name_InvalidPageTokenException() throws Exception {
        // , "content.zip"given
        final String pageName = "";
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test
    public void should_createPage_with_invalid_content_InvalidPageZipContentException() throws Exception {
        // , "content.zip"given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = IOUtil.zip(Collections.singletonMap("README.md", "empty file".getBytes()));
        expectedException.expect(InvalidPageZipMissingIndexException.class);
        getPageAPI().createPage(new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when

        // then: expected exception
    }

    @Test
    public void should_getPageProperties_throw_alreadyExists() throws Exception {
        // , "content.zip"given
        final byte[] pageContent = IOUtil.zip(Collections.singletonMap("README.md", "empty file".getBytes()));

        expectedException.expect(InvalidPageZipMissingIndexException.class);
        // when
        getPageAPI().getPageProperties(pageContent, true);

        // then: expected exception
    }

    @Test
    public void should_getPageContent_return_the_content() throws Exception {
        // given
        final String pageName = generateUniquePageName(0);
        final String pageDescription = "a verry long page description, maybe the longest description you will ever see, check that:"
                + " Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut"
                + " labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris"
                + " nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit "
                + "esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in "
                + "culpa qui officia deserunt mollit anim id est laborum.";
        final byte[] bytes = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, pageDescription);
        final Page page = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(pageDescription).setDisplayName(DISPLAY_NAME),
                bytes);

        // when
        final byte[] pageContent = getPageAPI().getPageContent(page.getId());
        // then
        checkPageContentContainsProperties(pageContent, DISPLAY_NAME, pageDescription);
    }

    private void checkPageContentContainsProperties(final byte[] content, final String displayName, final String description) throws Exception {
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

    private final Map<String, String> unzip(final byte[] zipFile) throws Exception {
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
                zipMap.put(zipEntry.getName(), new String(byteArrayOutputStream.toByteArray(), UTF8));
            }
        } finally {
            zipInputstream.close();
        }
        return zipMap;
    }

    @Test(expected = PageNotFoundException.class)
    public void deletePage_should_delete_the_page() throws Exception {
        // given
        final String pageName = generateUniquePageName(0);
        final byte[] bytes = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                bytes);

        // when
        getPageAPI().deletePage(page.getId());

        // then
        getPageAPI().getPage(page.getId());
    }

    @Test(expected = AlreadyExistsException.class)
    public void should_duplicates_with_same_name_and_process_definitionId_throw_exception() throws Exception {
        // given
        final String pageName = generateUniquePageName(0);
        final byte[] bytes = createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME, ContentType.FORM, PROCESS_DEFINITION_ID).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                bytes);

        // when then exception
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME, ContentType.FORM, PROCESS_DEFINITION_ID).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                bytes);

    }

    @Test
    public void should_search_with_search_term() throws Exception {
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
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(noneMatchingdisplayName),
                    CommonTestUtil.createTestPageContent(generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION));
        }
        final String generateUniquePageName = generateUniquePageName(9);
        final Page pageWithMatchingSearchTerm = getPageAPI().createPage(
                new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(matchingDisplayName),
                CommonTestUtil.createTestPageContent(generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).searchTerm(matchingValue).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have onlmy one matching page").isEqualTo(1);
        assertThat(results.get(0)).as("should get the page whith matching search term").isEqualToComparingFieldByField(pageWithMatchingSearchTerm);
    }

    private String generateUniquePageName(final int i) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("custompage_unique" + i);
        stringBuilder.append(System.currentTimeMillis());
        return stringBuilder.toString();
    }

    @Test
    public void should_8_pages_search_5_first_results_give_5_first_results() throws Exception {
        // given
        final int expectedResultSize = 5;
        for (int i = 0; i < expectedResultSize + 3; i++) {
            final String generateUniquePageName = generateUniquePageName(i) + 1;
            final byte[] pageContent = CommonTestUtil.createTestPageContent(generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION);
            getPageAPI().createPage(new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
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
    public void should_search_by_display_name() throws Exception {
        // given
        final String description = PAGE_DESCRIPTION;
        final String matchingDisplayName = DISPLAY_NAME;
        final String noneMatchingDisplayName = "aaa";

        // given
        final int expectedMatchingResults = 3;
        for (int i = 0; i < expectedMatchingResults; i++) {
            final String generateUniquePageName = generateUniquePageName(i);
            final byte[] pageContent = CommonTestUtil.createTestPageContent(generateUniquePageName, matchingDisplayName, description);
            getPageAPI().createPage(new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(matchingDisplayName),
                    pageContent);
        }
        final String anOtherName = generateUniquePageName(4);
        getPageAPI().createPage(new PageCreator(anOtherName, CONTENT_NAME).setDescription("an awesome page!!!!!!!").setDisplayName(noneMatchingDisplayName),
                CommonTestUtil.createTestPageContent(anOtherName, noneMatchingDisplayName, "an awesome page!!!!!!!"));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, expectedMatchingResults + 2).filter(PageSearchDescriptor.DISPLAY_NAME, matchingDisplayName).done());
        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have "
                + +expectedMatchingResults + " results").isEqualTo(expectedMatchingResults);

    }

    @Test
    public void should_search_by_content_type() throws Exception {
        // given
        final String description = PAGE_DESCRIPTION;
        final String matchingDisplayName = DISPLAY_NAME;
        final String noneMatchingDisplayName = "aaa";

        // given
        final int expectedMatchingResults = 3;
        for (int i = 0; i < expectedMatchingResults; i++) {
            final String generateUniquePageName = generateUniquePageName(i);
            final byte[] pageContent = createTestPageContent(generateUniquePageName, matchingDisplayName, description);
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME, ContentType.FORM, PROCESS_DEFINITION_ID + i).setDescription(
                            "should be excluded from results")
                            .setDisplayName(matchingDisplayName),
                    pageContent);
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription("should be in search results")
                            .setDisplayName(matchingDisplayName),
                    pageContent);
        }
        final String anOtherName = generateUniquePageName(4);
        getPageAPI().createPage(
                new PageCreator(anOtherName, CONTENT_NAME).setDescription("should be excluded from results").setDisplayName(noneMatchingDisplayName),
                createTestPageContent(anOtherName, noneMatchingDisplayName, "an awesome page!!!!!!!"));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, expectedMatchingResults + 2).filter(PageSearchDescriptor.DISPLAY_NAME, matchingDisplayName)
                        .filter(PageSearchDescriptor.CONTENT_TYPE, ContentType.PAGE).done());
        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have "
                + +expectedMatchingResults + " results").isEqualTo(expectedMatchingResults);

    }

    @Test
    public void should_search_work_on_desc_order() throws Exception {
        final String displayName = DISPLAY_NAME;
        final String description = PAGE_DESCRIPTION;
        final String firstPageNameInDescOrder = "custompage_zPageName";

        // given
        final int numberOfNonsMatchingPage = 5;
        for (int i = 0; i < numberOfNonsMatchingPage; i++) {
            final String generateUniquePageName = generateUniquePageName(i) + i;
            final byte[] pageContent = CommonTestUtil.createTestPageContent(generateUniquePageName, displayName, description);
            getPageAPI().createPage(new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description).setDisplayName(displayName),
                    pageContent);
        }
        final Page expectedMatchingPage = getPageAPI().createPage(
                new PageCreator(firstPageNameInDescOrder, CONTENT_NAME).setDescription(description).setDisplayName(displayName),
                CommonTestUtil.createTestPageContent(firstPageNameInDescOrder, displayName, description));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, 1).sort(PageSearchDescriptor.NAME, Order.DESC).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.get(0)).isEqualToComparingFieldByField(expectedMatchingPage);

    }
}
