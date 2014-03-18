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

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;

@SuppressWarnings("javadoc")
public class PageAPIIT extends CommonAPISPTest {

    @Before
    public void before() throws BonitaException {
        login();
    }

    @After
    public void after() throws BonitaException {
        SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 1000).done());
        for (Page page : searchPages.getResult()) {
            getPageAPI().deletePage(page.getId());
        }
        logout();
    }

    @Test
    public void should_getPage_return_the_page() throws BonitaException {
        // given
        Page page = getPageAPI().createPage(new PageCreator("mypage").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // when
        Page returnedPage = getPageAPI().getPage(page.getId());

        // then
        assertThat(returnedPage).isEqualTo(page);
    }

    @Test
    public void should_update_return_the_modified_page() throws BonitaException {
        // given
        long currentTimeMillis = System.currentTimeMillis();
        Page page = getPageAPI().createPage(new PageCreator("mypage").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // when
        PageUpdater pageUpdater = new PageUpdater();
        String newDescription = "new description";
        pageUpdater.setDescription(newDescription);

        Page returnedPage = getPageAPI().updatePage(page.getId(), pageUpdater);

        // then
        assertThat(returnedPage.getDescription()).isEqualTo(newDescription);
        assertThat(returnedPage.getLastModificationDate()).isAfter(new Date(currentTimeMillis));

    }

    @Test
    public void should_update_content_return_the_modified_content() throws BonitaException {
        // given
        long currentTimeMillis = System.currentTimeMillis();
        Page page = getPageAPI().createPage(new PageCreator("mypage").setDescription("page description").setDisplayName("My Päge"),
                "old content".getBytes());
        long pageId = page.getId();

        // when
        getPageAPI().updatePageContent(pageId, "new content".getBytes());
        byte[] newPageContent = getPageAPI().getPageContent(pageId);
        Page returnedPage = getPageAPI().getPage(pageId);

        // then
        assertThat(newPageContent).isEqualTo("new content".getBytes());
        assertThat(returnedPage.getLastModificationDate()).isAfter(new Date(currentTimeMillis));

    }

    @Test
    public void should_getPage_by_name_return_the_page() throws BonitaException {
        // given
        Page page = getPageAPI().createPage(new PageCreator("mypage").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // when
        Page returnedPage = getPageAPI().getPageByName(page.getName());

        // then
        assertThat(returnedPage).isEqualTo(page);
    }

    @Test(expected = AlreadyExistsException.class)
    public void should_createPage_with_same_name_throw_already_exists() throws BonitaException {
        // given
        getPageAPI().createPage(new PageCreator("mypagedup").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // when
        getPageAPI().createPage(new PageCreator("mypagedup").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // then: expected exception
    }

    @Test
    public void should_getPageContent_return_the_content() throws BonitaException {
        // given
        Page page = getPageAPI().createPage(new PageCreator("mypagewithcontent").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // when
        byte[] pageContent = getPageAPI().getPageContent(page.getId());

        // then
        assertThat(pageContent).isEqualTo("my page content in a zip file".getBytes());
    }

    @Test(expected = PageNotFoundException.class)
    public void should_deletePage_delete_the_page() throws BonitaException {
        // given
        Page page = getPageAPI().createPage(new PageCreator("mypagetodelete").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // when
        getPageAPI().deletePage(page.getId());

        // then
        getPageAPI().getPage(page.getId());
    }

    @Test
    public void should_search_with_search_term() throws BonitaException {
        String description = "description";
        String content = "content";
        String noneMatchingdisplayName = "My Päge";
        String matchingValue = "Cool";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(matchingValue);
        stringBuilder.append(" page!");
        String matchingDisplayName = stringBuilder.toString();

        // given
        int noneMatchingCount = 8;
        for (int i = 0; i < noneMatchingCount; i++) {
            getPageAPI().createPage(new PageCreator(generateUniquePageName()).setDescription(description).setDisplayName(noneMatchingdisplayName),
                    content.getBytes());
        }
        Page pageWithMatchingSearchTerm = getPageAPI().createPage(
                new PageCreator(generateUniquePageName()).setDescription(description).setDisplayName(matchingDisplayName),
                content.getBytes());

        // when
        SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).searchTerm(matchingValue).done());

        // then
        List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have onlmy one matching page").isEqualTo(1);
        assertThat(results.get(0)).as("should get the page whith matching search term").isEqualTo(pageWithMatchingSearchTerm);
    }

    private String generateUniquePageName() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(("unique_page_name_"));
        stringBuilder.append(System.currentTimeMillis());
        return stringBuilder.toString();
    }

    @Test
    public void should_8_pages_search_5_first_results_give_5_first_results() throws BonitaException {
        String displayName = "My Päge";
        String description = "page description";
        String content = "my page content in a zip file";

        // given
        int expectedResultSize = 5;
        for (int i = 0; i < expectedResultSize + 3; i++) {
            getPageAPI().createPage(new PageCreator(generateUniquePageName()).setDescription(description).setDisplayName(displayName), content.getBytes());
        }

        // when
        SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).done());

        // then
        List<Page> results = searchPages.getResult();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("should have only ");
        stringBuilder.append(expectedResultSize);
        stringBuilder.append(" results");
        assertThat(results.size()).as(stringBuilder.toString()).isEqualTo(expectedResultSize);

    }

    @Test
    public void should_search_by_display_name() throws BonitaException {
        // given
        String content = "content";
        String description = "page description";
        String matchingDisplayName = "My Päge";
        String noneMatchingDisplayName = "aaa";

        // given
        int expectedMatchingResults = 3;
        for (int i = 0; i < expectedMatchingResults; i++) {
            getPageAPI().createPage(new PageCreator(generateUniquePageName()).setDescription(description).setDisplayName(matchingDisplayName),
                    content.getBytes());
        }
        getPageAPI().createPage(new PageCreator("anOtherName").setDescription("an awesome page!!!!!!!").setDisplayName(noneMatchingDisplayName),
                "my page content idsqdsqsqddsqqsgsdgqn a zip file".getBytes());

        // when
        SearchResult<Page> searchPages = getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, expectedMatchingResults + 2).filter(PageSearchDescriptor.DISPLAY_NAME, matchingDisplayName).done());
        // then
        List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have "
                + +expectedMatchingResults + " results").isEqualTo(expectedMatchingResults);

    }

    @Test
    public void should_search_work_on_desc_order() throws BonitaException {
        String displayName = "My Päge";
        String description = "page description";
        String firstPageNameInDescOrder = "zPageName";
        String content = "my page content in a zip file";

        // given
        int numberOfNonsMatchingPage = 5;
        for (int i = 0; i < numberOfNonsMatchingPage; i++) {
            getPageAPI().createPage(new PageCreator(generateUniquePageName()).setDescription(description).setDisplayName(displayName), content.getBytes());
        }
        Page expectedMatchingPage = getPageAPI().createPage(new PageCreator(firstPageNameInDescOrder).setDescription(description).setDisplayName(displayName),
                content.getBytes());

        // when
        SearchResult<Page> searchPages = getPageAPI().searchPages(
                new SearchOptionsBuilder(0, 1).sort(PageSearchDescriptor.NAME, Order.DESC).done());

        // then
        List<Page> results = searchPages.getResult();
        assertThat(results.get(0)).isEqualTo(expectedMatchingPage);

    }

}
