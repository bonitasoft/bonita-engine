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

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
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

}
