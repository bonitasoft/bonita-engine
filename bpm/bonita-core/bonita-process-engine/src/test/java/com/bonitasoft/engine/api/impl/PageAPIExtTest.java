package com.bonitasoft.engine.api.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.transaction.page.SearchPages;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;
import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.PageUpdater;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class PageAPIExtTest {

    @Mock
    PageService pageService;

    @Mock
    PageAPIExt pageAPIExt;

    @Mock
    TenantServiceAccessor tenantServiceAccessor;

    @Mock
    SPage sPage;

    @Mock
    Page page;

    @Mock
    SessionAccessor sessionAccessor;

    @Mock
    QueryOptions queryOptions;

    @Mock
    SearchPages searchPages;

    private final long userId = 1;

    @Before
    public void before() throws Exception {
        pageAPIExt = spy(new PageAPIExt());

        doReturn(tenantServiceAccessor).when(pageAPIExt).getTenantAccessor();
        doReturn(pageService).when(tenantServiceAccessor).getPageService();

        doReturn(1l).when(sPage).getId();
        doReturn("name").when(sPage).getName();
        doReturn("display name").when(sPage).getDisplayName();
        doReturn("description").when(sPage).getDescription();
        doReturn(2l).when(sPage).getInstalledBy();
        doReturn(3l).when(sPage).getInstallationDate();
        doReturn(4l).when(sPage).getLastModificationDate();

    }

    @Test
    public void testGetPage() throws Exception {
        // given
        long pageId = 123;
        doReturn(sPage).when(pageService).getPage(pageId);

        // when
        pageAPIExt.getPage(pageId);

        // then
        verify(pageService, times(1)).getPage(pageId);
    }

    @Test
    public void testGetPageByName() throws Exception {
        // given
        String pageName = "name";
        doReturn(sPage).when(pageService).getPageByName(pageName);

        // when
        pageAPIExt.getPageByName(pageName);

        // then
        verify(pageService, times(1)).getPageByName(pageName);
    }

    @Test
    public void testGetPageContent() throws Exception {
        // given
        long pageId = 123;

        // when
        pageAPIExt.getPageContent(pageId);

        // then
        verify(pageService, times(1)).getPageContent(pageId);
    }

    @Test
    public void testSearchPages() throws Exception {
        doReturn(searchPages).when(pageAPIExt).getSearchPages(any(SearchOptions.class), any(SearchEntitiesDescriptor.class), any(PageService.class));

        // when
        SearchOptions searchOptions = new SearchOptionsBuilder(0, 5).searchTerm("search").done();
        pageAPIExt.searchPages(searchOptions);

        // then
        verify(searchPages, times(1)).execute();
    }

    @Test
    public void testCreatePage() throws Exception {
        // given
        final PageCreator pageCreator = new PageCreator("name");
        final byte[] content = "content".getBytes();

        doReturn(userId).when(pageAPIExt).getUserIdFromSessionInfos();
        doReturn(sPage).when(pageAPIExt).constructPage(pageCreator, userId);
        doReturn(page).when(pageAPIExt).convertToPage(any(SPage.class));

        // when
        pageAPIExt.createPage(pageCreator, content);

        // then
        verify(pageService, times(1)).addPage(sPage, content);
    }

    @Test
    @Ignore
    public void testUpdatePage() throws Exception {
        // given
        final PageUpdater pageUpdater = new PageUpdater();
        pageUpdater.setName("name");
        final long pageId = 1;

        doReturn(userId).when(pageAPIExt).getUserIdFromSessionInfos();
        doReturn(sPage).when(pageAPIExt).constructPage(pageUpdater, userId);
        doReturn(page).when(pageAPIExt).convertToPage(any(SPage.class));

        // when
        pageAPIExt.updatePage(pageId, pageUpdater);

        // then
        verify(pageService, times(1)).updatePage(pageId, sPage);
    }

    @Test
    public void testUpdatePageContent() throws Exception {
        // given
        // final PageCreator pageCreator = new PageCreator("name");
        final byte[] content = "content".getBytes();
        final long pageId = 1;

        doReturn(userId).when(pageAPIExt).getUserIdFromSessionInfos();
        doReturn(page).when(pageAPIExt).convertToPage(any(SPage.class));

        // when
        pageAPIExt.updatePageContent(pageId, content);

        // then
        verify(pageService, times(1)).updatePageContent(pageId, content);
    }

    @Test
    public void testDeletePage() throws Exception {
        // given
        long pageId = 123;

        // when
        pageAPIExt.deletePage(pageId);

        // then
        verify(pageService, times(1)).deletePage(pageId);
    }

    @Test
    public void testDeletePages() throws Exception {
        // given
        List<Long> pageIds = new ArrayList<Long>();
        for (int pageId = 0; pageId < 10; pageId++) {
            pageIds.add(new Long(pageId));
        }

        // when
        pageAPIExt.deletePages(pageIds);

        // then
        verify(pageService, times(pageIds.size())).deletePage(anyLong());
    }

    @Test(expected = AlreadyExistsException.class)
    public void testCheckPageAlreadyExists() throws Exception {
        // given
        doReturn(sPage).when(pageService).getPageByName("name");

        // when
        pageAPIExt.checkPageAlreadyExists("name", tenantServiceAccessor);

        // then
        // AlreadyExistsException

    }

}
