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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.page.PageAssert.assertThat;
import static org.junit.Assert.assertThrows;

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

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.application.ApplicationIT;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingIndexException;
import org.bonitasoft.engine.exception.UpdatingWithInvalidPageZipContentException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.CommonTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PageAPIIT extends CommonAPIIT {

    public static final long PROCESS_DEFINITION_ID = 5846L;

    private static final String DISPLAY_NAME = "My P\u00e4ge";

    private static final String CONTENT_NAME = "content.zip";

    private static final String PAGE_DESCRIPTION = "page description";

    private static final String PAGE_NAME2 = "custompage_page2";

    private static final String PAGE_NAME1 = "custompage_page1";

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        final SearchResult<Page> searchPages = getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, Integer.MAX_VALUE).done());
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
                new PageCreator(name, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME)
                        .setContentType(ContentType.FORM)
                        .setProcessDefinitionId(PROCESS_DEFINITION_ID),
                pageContent);

        // when
        final Page returnedPage = getPageAPI().getPage(page.getId());

        // then
        Assertions.assertThat(returnedPage).usingRecursiveComparison().isEqualTo(page);
        PageAssert.assertThat(returnedPage)
                .hasProcessDefinitionId(PROCESS_DEFINITION_ID)
                .hasContentType(ContentType.FORM);
    }

    @Test
    public void updatePage_should_set_provided_field_to_false_if_provided_pages_are_modified() throws Exception {
        //given:
        try (var is = PageAPIIT.class.getResourceAsStream("/provided_page_ready_to_update.zip")) {
            getPageAPI().createPage("provided_page_ready_to_update.zip", is.readAllBytes());
        }

        // when
        final PageUpdater pageUpdater = new PageUpdater();
        final String newDisplayName = "new display name";
        pageUpdater.setDisplayName(newDisplayName);

        final Page providedPageToUpdate = getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, 1)
                        .filter(PageSearchDescriptor.NAME, "custompage_providedpagetoupdate").done())
                .getResult().get(0);
        final Page returnedPage = getPageAPI().updatePage(providedPageToUpdate.getId(), pageUpdater);

        //then
        assertThat(returnedPage.isProvided()).isFalse();
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
        final Page pageBeforeUpdate = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);
        Thread.sleep(10);
        assertThat(pageBeforeUpdate.getInstalledBy()).isEqualTo(john.getId());
        assertThat(pageBeforeUpdate.getLastUpdatedBy()).isEqualTo(john.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");
        // when
        final PageUpdater pageUpdater = new PageUpdater();
        final String newDescription = "new description";
        final String newDisplayName = "new display name";
        final String newContentName = "new_content.zip";
        pageUpdater.setDescription(newDescription);
        pageUpdater.setDisplayName(newDisplayName);
        pageUpdater.setName("newName");
        pageUpdater.setContentName(newContentName);
        pageUpdater.setContentType(ContentType.FORM);
        pageUpdater.setProcessDefinitionId(5L);

        final Page returnedPage = getPageAPI().updatePage(pageBeforeUpdate.getId(), pageUpdater);

        // then
        assertThat(returnedPage)
                .hasInstalledBy(john.getId())
                .hasInstalledBy(pageBeforeUpdate.getInstalledBy())
                .hasLastUpdatedBy(jack.getId())
                .hasName(pageBeforeUpdate.getName())
                .hasInstallationDate(pageBeforeUpdate.getInstallationDate())
                .hasDisplayName(newDisplayName)
                .hasContentName(newContentName)
                .hasDescription(newDescription)
                .hasContentType(ContentType.FORM)
                .hasProcessDefinitionId(5L);

        assertThat(returnedPage.getLastModificationDate()).as("last modification time should be updated")
                .isAfter(pageBeforeUpdate.getLastModificationDate());

        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(john);
        deleteUser(jack);

    }

    @Test(expected = AlreadyExistsException.class)
    public void updateForm_with_existing_process_definitionId_should_fail() throws Exception {
        final PageUpdater pageUpdater = new PageUpdater();

        // given
        getPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME)
                        .setProcessDefinitionId(PROCESS_DEFINITION_ID),
                CommonTestUtil.createTestPageContent(PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));
        final Page page2 = getPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        pageUpdater.setProcessDefinitionId(PROCESS_DEFINITION_ID);
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
        getPageAPI().updatePageContent(createPage.getId(),
                IOUtil.zip(Collections.singletonMap("README.md", "empty file".getBytes())));

        // then
        // exception

    }

    @Test
    public void updatePageContent_should_update_page_from_properties_except_name() throws Exception {
        // given
        final Page pageBefore = getPageAPI().createPage(
                new PageCreator(PAGE_NAME1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(PAGE_NAME1, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final String newDescription = "new description";
        final String newDisplayName = "new display name";
        final byte[] updatedPageContent = CommonTestUtil.createTestPageContent(PAGE_NAME2, newDisplayName,
                newDescription);
        getPageAPI().updatePageContent(pageBefore.getId(), updatedPageContent);

        // then
        final Page pageAfter = getPageAPI().getPage(pageBefore.getId());
        assertThat(pageAfter.getName()).as("should not update page name").isEqualTo(PAGE_NAME1);
        assertThat(pageAfter.getDisplayName()).as("should update page display name").isEqualTo(newDisplayName);
        assertThat(pageAfter.getDescription()).as("should update page name").isEqualTo(newDescription);

    }

    @Test
    public void should_update_content_return_the_modified_content() throws Exception {
        // given
        final Date createTimeMillis = new Date(System.currentTimeMillis());
        final String pageName = generateUniquePageName(0);
        final byte[] oldContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
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
    public void should_create_page_use_content_type_in_properties() throws Exception {
        // given
        final String pageName1 = generateUniquePageName(0);
        final byte[] pageContent1 = CommonTestUtil.createTestPageContent(pageName1, DISPLAY_NAME,
                "with content " + PAGE_DESCRIPTION,
                "contentType=WillBeIgnored", "apiExtensions=myGetResource", "myGetResource.method=POST",
                "myGetResource.pathTemplate=helloWorld",
                "myGetResource.classFileName=Index.groovy", "myGetResource.permissions=application_visualization");

        final String pageName2 = generateUniquePageName(1);
        final byte[] pageContent2 = CommonTestUtil.createTestPageContent(pageName2, DISPLAY_NAME,
                "with page creator " + PAGE_DESCRIPTION, "contentType="
                        + ContentType.API_EXTENSION,
                "apiExtensions=myGetResource", "myGetResource.method=GET", "myGetResource.pathTemplate=helloWorld",
                "myGetResource.classFileName=Index.groovy", "myGetResource.permissions=application_visualization");

        // when
        final Page pageWithCreator = getPageAPI().createPage(
                new PageCreator(pageName1, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME)
                        .setContentType(ContentType.API_EXTENSION),
                pageContent1);
        final Page pageWithContent = getPageAPI().createPage(pageName2, pageContent2);

        // then
        assertThat(pageWithContent).hasContentType(ContentType.API_EXTENSION);
        assertThat(pageWithCreator).hasContentType(ContentType.API_EXTENSION);
    }

    @Test
    public void should_create_a_page_and_delete_it_and_recreate_it_with_same_values() throws Exception {
        // given
        final String pageName = generateUniquePageName(1);
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME,
                "with page creator " + PAGE_DESCRIPTION, "contentType="
                        + ContentType.API_EXTENSION,
                "apiExtensions=myGetResource, myPostResource", "myGetResource.method=GET",
                "myGetResource.pathTemplate=helloWorld", "myGetResource.classFileName=Index.groovy",
                "myGetResource.permissions=newPermission",
                "myPostResource.method=POST",
                "myPostResource.pathTemplate=helloWorld", "myPostResource.classFileName=Index.groovy",
                "myPostResource.permissions=newPermission");

        // when
        Page pageWithContent = getPageAPI().createPage(pageName, pageContent);
        getPageAPI().deletePage(pageWithContent.getId());

        pageWithContent = getPageAPI().createPage(pageName, pageContent);

        // then
        assertThat(pageWithContent).hasContentType(ContentType.API_EXTENSION);
    }

    @Test
    public void should_getPage_by_name_return_the_page() throws Exception {
        // given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        final Page page = getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        final Page returnedPage = getPageAPI().getPageByName(page.getName());

        // then
        Assertions.assertThat(returnedPage).usingRecursiveComparison().isEqualTo(page);
    }

    @Test(expected = AlreadyExistsException.class)
    public void should_createPage_with_same_name_throw_already_exists() throws Exception {
        // , "content.zip"given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test(expected = InvalidPageTokenException.class)
    public void should_createPage_with_invalid_name_InvalidPageTokenException() throws Exception {
        // , "content.zip"given
        final String pageName = "plop";
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test(expected = InvalidPageTokenException.class)
    public void should_createPage_with_no_name_InvalidPageTokenException() throws Exception {
        // , "content.zip"given
        final String pageName = "";
        final byte[] pageContent = CommonTestUtil.createTestPageContent(pageName, DISPLAY_NAME, PAGE_DESCRIPTION);
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // when
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent);

        // then: expected exception
    }

    @Test
    public void should_createPage_with_invalid_content_InvalidPageZipContentException() throws Exception {
        // given
        final String pageName = generateUniquePageName(0);
        final byte[] pageContent = zip(file("README.md", "empty file".getBytes()),
                file("page.properties", String.format("name=%s\ncontentType=page", pageName).getBytes()));

        // when
        assertThrows(InvalidPageZipMissingIndexException.class, () -> getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                pageContent));

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

    @Test
    public void should_throw_an_exception_if_the_page_content_does_not_exist() {
        // when
        final PageNotFoundException exception = assertThrows(PageNotFoundException.class,
                () -> getPageAPI().getPageContent(995464654654L));

        // then
        assertThat(exception.getMessage()).contains("Page with id 995464654654 not found");
    }

    private void checkPageContentContainsProperties(final byte[] content, final String displayName,
            final String description) throws Exception {
        try {
            Map<String, String> contentAsMap = unzip(content);
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

    private Map<String, String> unzip(final byte[] zipFile) throws Exception {
        final ByteArrayInputStream bais = new ByteArrayInputStream(zipFile);
        ZipEntry zipEntry;
        final Map<String, String> zipMap = new HashMap<>();
        try (ZipInputStream zipInputstream = new ZipInputStream(bais)) {
            while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int bytesRead;
                final byte[] buffer = new byte[4096];
                while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                zipMap.put(zipEntry.getName(), byteArrayOutputStream.toString(UTF_8));
            }
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
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME, ContentType.FORM, PROCESS_DEFINITION_ID)
                        .setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                bytes);

        // when then exception
        getPageAPI().createPage(
                new PageCreator(pageName, CONTENT_NAME, ContentType.FORM, PROCESS_DEFINITION_ID)
                        .setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                bytes);

    }

    @Test
    public void should_search_with_search_term() throws Exception {
        final String description = "description";
        final String matchingValue = "Cool";
        final String matchingDisplayName = matchingValue + " page!";

        // given
        final int noneMatchingCount = 8;
        for (int i = 0; i < noneMatchingCount; i++) {
            final String generateUniquePageName = generateUniquePageName(i) + i;
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description)
                            .setDisplayName(DISPLAY_NAME),
                    CommonTestUtil.createTestPageContent(generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION));
        }
        final String generateUniquePageName = generateUniquePageName(9);
        final Page pageWithMatchingSearchTerm = getPageAPI().createPage(
                new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description)
                        .setDisplayName(matchingDisplayName),
                CommonTestUtil.createTestPageContent(generateUniquePageName, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final SearchResult<Page> searchPages = getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, 5).searchTerm(matchingValue).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have only one matching page").isEqualTo(1);
        Assertions.assertThat(results.get(0)).as("should get the page with matching search term")
                .usingRecursiveComparison().isEqualTo(pageWithMatchingSearchTerm);
    }

    private String generateUniquePageName(final int i) {
        return ("custompage_unique" + i) + System.currentTimeMillis();
    }

    @Test
    public void should_8_pages_search_5_first_results_give_5_first_results() throws Exception {
        // given
        final int expectedResultSize = 5;
        for (int i = 0; i < expectedResultSize + 3; i++) {
            final String generateUniquePageName = generateUniquePageName(i) + 1;
            final byte[] pageContent = CommonTestUtil.createTestPageContent(generateUniquePageName, DISPLAY_NAME,
                    PAGE_DESCRIPTION);
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(PAGE_DESCRIPTION)
                            .setDisplayName(DISPLAY_NAME),
                    pageContent);
        }

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).done());

        // then
        final List<Page> results = searchPages.getResult();
        String stringBuilder = "should have only " + expectedResultSize + " results";
        assertThat(results.size()).as(stringBuilder).isEqualTo(expectedResultSize);

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
            final byte[] pageContent = CommonTestUtil.createTestPageContent(generateUniquePageName, matchingDisplayName,
                    description);
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description)
                            .setDisplayName(matchingDisplayName),
                    pageContent);
        }
        final String anOtherName = generateUniquePageName(4);
        getPageAPI().createPage(
                new PageCreator(anOtherName, CONTENT_NAME).setDescription("an awesome page!!!!!!!")
                        .setDisplayName(noneMatchingDisplayName),
                CommonTestUtil.createTestPageContent(anOtherName, noneMatchingDisplayName, "an awesome page!!!!!!!"));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, expectedMatchingResults + 2)
                        .filter(PageSearchDescriptor.DISPLAY_NAME, matchingDisplayName).done());
        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have "
                + expectedMatchingResults + " results").isEqualTo(expectedMatchingResults);

    }

    @Test
    public void should_search_by_content_type() throws Exception {
        // given
        final String matchingDisplayName = DISPLAY_NAME;
        final String noneMatchingDisplayName = "aaa";

        // given
        final int expectedMatchingResults = 3;
        for (int i = 0; i < expectedMatchingResults; i++) {
            final String generateUniquePageName = generateUniquePageName(i);
            final byte[] pageContent = createTestPageContent(generateUniquePageName, matchingDisplayName,
                    PAGE_DESCRIPTION);
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME, ContentType.FORM, PROCESS_DEFINITION_ID + i)
                            .setDescription(
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
                new PageCreator(anOtherName, CONTENT_NAME).setDescription("should be excluded from results")
                        .setDisplayName(noneMatchingDisplayName),
                createTestPageContent(anOtherName, noneMatchingDisplayName, "an awesome page!!!!!!!"));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, expectedMatchingResults + 2)
                        .filter(PageSearchDescriptor.DISPLAY_NAME, matchingDisplayName)
                        .filter(PageSearchDescriptor.CONTENT_TYPE, ContentType.PAGE).done());
        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have "
                + expectedMatchingResults + " results").isEqualTo(expectedMatchingResults);

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
            final byte[] pageContent = CommonTestUtil.createTestPageContent(generateUniquePageName, displayName,
                    description);
            getPageAPI().createPage(
                    new PageCreator(generateUniquePageName, CONTENT_NAME).setDescription(description)
                            .setDisplayName(displayName),
                    pageContent);
        }
        final Page expectedMatchingPage = getPageAPI().createPage(
                new PageCreator(firstPageNameInDescOrder, CONTENT_NAME).setDescription(description)
                        .setDisplayName(displayName),
                CommonTestUtil.createTestPageContent(firstPageNameInDescOrder, displayName, description));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, 1).sort(PageSearchDescriptor.NAME, Order.DESC).done());

        // then
        final List<Page> results = searchPages.getResult();
        Assertions.assertThat(results.get(0)).usingRecursiveComparison().isEqualTo(expectedMatchingPage);
    }

    @Test
    public void updatePageContent_and_deletePage_should_update_permissions() throws Exception {
        // setup
        final User jack = createUser("jack", "bpm");
        Profile profile = getProfileAPI()
                .searchProfiles(new SearchOptionsBuilder(0, 1).filter(ProfileSearchDescriptor.NAME, "User").done())
                .getResult().get(0);
        getProfileAPI().createProfileMember(profile.getId(), jack.getId(), null, null);

        getLivingApplicationAPI().importApplications(
                IOUtils.toByteArray(ApplicationIT.class.getResourceAsStream("testApp.xml")),
                ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        // given
        final String apiExtensionName = generateUniquePageName(0);
        final byte[] apiExtensionContent1 = CommonTestUtil.createTestPageContent(apiExtensionName, DISPLAY_NAME,
                "with content " + PAGE_DESCRIPTION, "contentType=" + ContentType.API_EXTENSION,
                "apiExtensions=myGetResource, myPostResource", "myGetResource.method=GET",
                "myGetResource.pathTemplate=helloWorld",
                "myGetResource.classFileName=Index.groovy", "myGetResource.permissions=application_visualization",
                "myPostResource.method=POST",
                "myPostResource.pathTemplate=helloWorld", "myPostResource.classFileName=Index.groovy",
                "myPostResource.permissions = application_visualization");

        // when
        final Page apiExtension = getPageAPI().createPage(apiExtensionName, apiExtensionContent1);

        // need to log back in to actualize permissions:
        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");

        // Check that we are not authorized before update of the page properties content:
        APICallContext apiCallContext = new APICallContext("GET", "extension", "helloWorld_v2", null);
        assertThat(getPermissionAPI().isAuthorized(apiCallContext)).isFalse();

        final byte[] apiExtensionContent2 = CommonTestUtil.createTestPageContent(apiExtensionName, DISPLAY_NAME,
                "with content " + PAGE_DESCRIPTION, "contentType=" + ContentType.API_EXTENSION,
                "apiExtensions=myGetResource, myPutResource", "myGetResource.method=GET",
                "myGetResource.pathTemplate=helloWorld_v2",
                "myGetResource.classFileName=Index.groovy", "myGetResource.permissions=application_visualization",
                "myPutResource.method=PUT",
                "myPutResource.pathTemplate=helloWorld", "myPutResource.classFileName=Index.groovy",
                "myPutResource.permissions = application_visualization");
        getPageAPI().updatePageContent(apiExtension.getId(), apiExtensionContent2);

        // Application testApp.xml references layout custompage_layoutBonita that declares resources bound to
        // the 'application_visualization' permission. So this new apiExtension, accessible through 'GET|extension/helloWorld_v2', is accessible
        // to anyone having 'application_visualization' permission.

        // Check that permission has been update to file through user permission check:
        assertThat(getPermissionAPI().isAuthorized(apiCallContext)).isTrue();
        // Check that previous version has indeed been removed:
        assertThat(getPermissionAPI().isAuthorized(new APICallContext("GET", "extension", "helloWorld", null)))
                .isFalse();

        getPageAPI().deletePage(apiExtension.getId());

        // Check that permissions have been removed with the page deletion:
        assertThat(getPermissionAPI().isAuthorized(apiCallContext)).isFalse();

        //cleanup
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        deleteUser(jack);
    }

    @Test
    public void should_search_work_with_processDefinitionId_set_to_null() throws Exception {
        // given
        String name = generateUniquePageName(345);
        getPageAPI().createPage(
                new PageCreator(name, CONTENT_NAME).setDescription(PAGE_DESCRIPTION).setDisplayName(DISPLAY_NAME),
                CommonTestUtil.createTestPageContent(name, DISPLAY_NAME, PAGE_DESCRIPTION));

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, 1).filter(PageSearchDescriptor.NAME, name)
                        .filter(PageSearchDescriptor.PROCESS_DEFINITION_ID, null).done());

        // then
        final List<Page> results = searchPages.getResult();
        assertThat(results.get(0).getName()).isEqualTo(name);

    }

}
