package com.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.bonitasoft.engine.page.PageService;


public class PageAPIExtTest {


    @Test
    public void testGetPage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testGetPageContent() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testSearchPages() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testCreatePage() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testDeletePage() throws Exception {
        // given
        PageAPIExt pageAPIExt = new PageAPIExt();

        long pageId;
        // when
        pageAPIExt.deletePage(pageId);

        PageService mock;
        // then
        verify(mock);
    }

    @Test
    public void testDeletePages() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

    @Test
    public void testCheckPageAlreadyExists() throws Exception {
        throw new RuntimeException("not yet implemented");
    }

}
