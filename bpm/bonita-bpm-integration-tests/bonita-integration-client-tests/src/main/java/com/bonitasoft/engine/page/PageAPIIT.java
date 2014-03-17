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

import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

        System.out.println("clean " + searchPages.getCount() + " pages");
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
    @Ignore
    public void should_update_return_the_modified_page() throws BonitaException {
        // given
        Page page = getPageAPI().createPage(new PageCreator("mypage").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());

        // when
        PageUpdater pageUpdater = new PageUpdater(page.getName());
        String newDescription = "new description";
        pageUpdater.setDescription(newDescription);

        Page returnedPage = getPageAPI().updatePage(page.getId(), pageUpdater);

        // then
        assertThat(returnedPage.getDescription()).isEqualTo(newDescription);
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
        // given
        Page page1 = getPageAPI().createPage(new PageCreator("pagesearch1").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page2 = getPageAPI().createPage(new PageCreator("pagesearch2").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page3 = getPageAPI().createPage(new PageCreator("pagesearch3").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page4 = getPageAPI().createPage(new PageCreator("pagesearch4").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page5 = getPageAPI().createPage(new PageCreator("pagesearch5").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page6 = getPageAPI().createPage(new PageCreator("pagesearch6").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page7 = getPageAPI().createPage(new PageCreator("aName").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page8 = getPageAPI().createPage(new PageCreator("anOtherName").setDescription("an awesome page!!!!!!!").setDisplayName("Cool page!"),
                "my page content idsqdsqsqddsqqsgsdgqn a zip file".getBytes());

        // when
        SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).searchTerm("Cool").done());
        // then

        System.out.println("should_search_with_search_term " + searchPages.getCount() + " pages");
        List<Page> results = searchPages.getResult();
        assertThat(results.size()).isEqualTo(1);
        assertThat(results.get(0)).isEqualTo(page8);

    }

    @Test
    public void should_search_give_5_first_results() throws BonitaException {
        // given
        Page page1 = getPageAPI().createPage(new PageCreator("pagesearch1").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page2 = getPageAPI().createPage(new PageCreator("pagesearch2").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page3 = getPageAPI().createPage(new PageCreator("pagesearch3").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page4 = getPageAPI().createPage(new PageCreator("pagesearch4").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page5 = getPageAPI().createPage(new PageCreator("pagesearch5").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page6 = getPageAPI().createPage(new PageCreator("pagesearch6").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page7 = getPageAPI().createPage(new PageCreator("aName").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page8 = getPageAPI().createPage(new PageCreator("anOtherName").setDescription("an awesome page!!!!!!!").setDisplayName("Cool page!"),
                "my page content idsqdsqsqddsqqsgsdgqn a zip file".getBytes());

        // when
        SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 5).done());

        // then
        System.out.println("should_search_give_5_first_results " + searchPages.getCount() + " pages");
        List<Page> results = searchPages.getResult();
        assertThat(results.size()).isEqualTo(5);

    }

    @Test
    public void should_search_by_display_name() throws BonitaException {
        // given
        Page page1 = getPageAPI().createPage(new PageCreator("pagesearch1").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page2 = getPageAPI().createPage(new PageCreator("pagesearch2").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page3 = getPageAPI().createPage(new PageCreator("pagesearch3").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page4 = getPageAPI().createPage(new PageCreator("pagesearch4").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page5 = getPageAPI().createPage(new PageCreator("pagesearch5").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page6 = getPageAPI().createPage(new PageCreator("pagesearch6").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page7 = getPageAPI().createPage(new PageCreator("aName").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page8 = getPageAPI().createPage(new PageCreator("anOtherName").setDescription("an awesome page!!!!!!!").setDisplayName("Cool page!"),
                "my page content idsqdsqsqddsqqsgsdgqn a zip file".getBytes());

        // when
        SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 10).filter(PageSearchDescriptor.DISPLAY_NAME, "My Päge").done());
        // then
        System.out.println("should_search_by_display_name " + searchPages.getCount() + " pages");
        List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have 7 results").isEqualTo(7);

    }

    @Test
    public void should_search_work_on_desc_order() throws BonitaException {
        // given
        Page page1 = getPageAPI().createPage(new PageCreator("pagesearch1").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page2 = getPageAPI().createPage(new PageCreator("pagesearch2").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page3 = getPageAPI().createPage(new PageCreator("pagesearch3").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page4 = getPageAPI().createPage(new PageCreator("pagesearch4").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page5 = getPageAPI().createPage(new PageCreator("pagesearch5").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page6 = getPageAPI().createPage(new PageCreator("pagesearch6").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page7 = getPageAPI().createPage(new PageCreator("aName").setDescription("page description").setDisplayName("My Päge"),
                "my page content in a zip file".getBytes());
        Page page8 = getPageAPI().createPage(new PageCreator("ZanOtherName").setDescription("an awesome page!!!!!!!").setDisplayName("Cool page!"),
                "my page content idsqdsqsqddsqqsgsdgqn a zip file".getBytes());

        // when
        SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, 10).sort(PageSearchDescriptor.NAME, Order.DESC).done());

        // then

        List<Page> results = searchPages.getResult();
        assertThat(results.size()).as("should have 8 results").isEqualTo(8);

        assertThat(results.get(0).getName()).isEqualTo("ZanOtherName");

        // assertThat(results).isEqualTo(Arrays.asList(page8, page7, page3, page6, page5, page4, page3, page2, page1));

    }

}
