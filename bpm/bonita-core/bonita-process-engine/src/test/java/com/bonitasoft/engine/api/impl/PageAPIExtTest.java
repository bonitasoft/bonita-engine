/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.bonitasoft.engine.page.SInvalidPageTokenException;
import com.bonitasoft.engine.page.SInvalidPageZipInconsistentException;
import com.bonitasoft.engine.page.SInvalidPageZipMissingAPropertyException;
import com.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import com.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipInconsistentException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingIndexException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.transaction.page.SearchPages;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;
import com.bonitasoft.engine.page.PageNotFoundException;
import com.bonitasoft.engine.page.PageService;
import com.bonitasoft.engine.page.PageUpdater;
import com.bonitasoft.engine.page.PageUpdater.PageUpdateField;
import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageUpdateBuilder;
import com.bonitasoft.engine.page.SPageUpdateContentBuilder;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class PageAPIExtTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

    @Mock
    private PageUpdater pageUpdater;

    private final long userId = 1;

    @Mock
    private SPageUpdateBuilder sPageUpdateBuilder;

    @Mock
    private SPageUpdateContentBuilder sPageUpdateContentBuilder;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void before() {
        pageAPIExt = spy(new PageAPIExt());

        doReturn(tenantServiceAccessor).when(pageAPIExt).getTenantAccessor();
        doReturn(pageService).when(tenantServiceAccessor).getPageService();
        doReturn(123l).when(pageAPIExt).getUserIdFromSessionInfos();

        doReturn(1l).when(sPage).getId();
        doReturn("name").when(sPage).getName();
        doReturn("display name").when(sPage).getDisplayName();
        doReturn("description").when(sPage).getDescription();
        doReturn(2l).when(sPage).getInstalledBy();
        doReturn(3l).when(sPage).getInstallationDate();
        doReturn(4l).when(sPage).getLastModificationDate();
        doReturn(userId).when(pageAPIExt).getUserIdFromSessionInfos();
    }

    @Test
    public void testGetPage() throws Exception {
        // given
        final long pageId = 123;
        doReturn(sPage).when(pageService).getPage(pageId);

        // when
        pageAPIExt.getPage(pageId);

        // then
        verify(pageService, times(1)).getPage(pageId);
    }

    @Test
    public void testGetPageByName() throws Exception {
        // given
        final String pageName = "name";
        doReturn(sPage).when(pageService).getPageByName(pageName);

        // when
        pageAPIExt.getPageByName(pageName);

        // then
        verify(pageService, times(1)).getPageByName(pageName);
    }

    @Test(expected = PageNotFoundException.class)
    public void testGetPageByNameNotFound() throws Exception {
        // given
        // when
        pageAPIExt.getPageByName("unknown");

        // then: exception
    }

    @Test
    public void testGetPageContent() throws Exception {
        // given
        final long pageId = 123;

        // when
        pageAPIExt.getPageContent(pageId);

        // then
        verify(pageService, times(1)).getPageContent(pageId);
    }

    @Test
    public void testSearchPages() throws Exception {
        doReturn(searchPages).when(pageAPIExt).getSearchPages(any(SearchOptions.class), any(SearchEntitiesDescriptor.class), any(PageService.class));

        // when
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 5).searchTerm("search").done();
        pageAPIExt.searchPages(searchOptions);

        // then
        verify(searchPages, times(1)).execute();
    }

    @Test
    public void testCreatePage() throws Exception {
        // given
        final PageCreator pageCreator = new PageCreator("name", "content.zip");
        final byte[] content = "content".getBytes();

        doReturn(sPage).when(pageAPIExt).constructPage(pageCreator, userId);
        doReturn(page).when(pageAPIExt).convertToPage(any(SPage.class));

        // when
        pageAPIExt.createPage(pageCreator, content);

        // then
        verify(pageService, times(1)).addPage(sPage, content);
    }

    @Test
    public void testUpdatePage() throws Exception {
        final Map<PageUpdateField, String> map = new HashMap<PageUpdater.PageUpdateField, String>();
        doReturn(sPage).when(pageAPIExt).constructPage(any(PageUpdater.class), anyLong());
        doReturn(page).when(pageAPIExt).convertToPage(any(SPage.class));
        doReturn(sPageUpdateBuilder).when(pageAPIExt).getPageUpdateBuilder();
        doReturn(map).when(pageUpdater).getFields();

        // given
        map.put(PageUpdateField.DISPLAY_NAME, "displayname");
        map.put(PageUpdateField.NAME, "name");
        map.put(PageUpdateField.DESCRIPTION, "description");
        map.put(PageUpdateField.CONTENT_NAME, "content.zip");

        // when
        pageAPIExt.updatePage(1, pageUpdater);

        // then
        verify(pageService, times(1)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
    }

    @Test(expected = UpdateException.class)
    public void testUpdatePageWithEmplyUpdateFileShouldThrowExceptions() throws Exception {
        doReturn(sPage).when(pageAPIExt).constructPage(any(PageUpdater.class), anyLong());
        doReturn(page).when(pageAPIExt).convertToPage(any(SPage.class));

        // given
        final Map<PageUpdateField, String> map = new HashMap<PageUpdater.PageUpdateField, String>();
        doReturn(map).when(pageUpdater).getFields();

        // when
        pageAPIExt.updatePage(1, pageUpdater);

        // then
        verify(pageService, times(1)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void testUpdatePageContent() throws Exception {

        doReturn(sPageUpdateBuilder).when(pageAPIExt).getPageUpdateBuilder();
        doReturn(sPageUpdateContentBuilder).when(pageAPIExt).getPageUpdateContentBuilder();
        doReturn(mock(SPage.class)).when(pageService).getPage(1);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair("Index.groovy", "content of the groovy".getBytes()),
                pair("page.properties", "name=mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));
        final long pageId = 1;

        // when
        pageAPIExt.updatePageContent(pageId, content);

        // then
        verify(pageService, times(1)).updatePageContent(anyLong(), eq(content), anyString());
    }

    @Test
    public void testDeletePage() throws Exception {
        // given
        final long pageId = 123;

        // when
        pageAPIExt.deletePage(pageId);

        // then
        verify(pageService, times(1)).deletePage(pageId);
    }

    @Test
    public void testDeletePages() throws Exception {
        // given
        final List<Long> pageIds = new ArrayList<Long>();
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
        final PageCreator pageCreator = new PageCreator("name", "content.zip");
        doReturn(sPage).when(pageAPIExt).constructPage(pageCreator, userId);
        doReturn(page).when(pageAPIExt).convertToPage(any(SPage.class));
        final byte[] content = IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
        doThrow(SObjectAlreadyExistsException.class).when(pageService).addPage(sPage, content);

        // when
        pageAPIExt.createPage(pageCreator, content);

        // then
        // AlreadyExistsException

    }

    @Test
    public void should_getPageProperties_check_already_exists_if_check_to_true() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        doReturn(null).when(pageService).getPageByName("MyPage");
        // when
        Properties pageProperties = pageAPIExt.getPageProperties(content, true);

        // then
        assertThat(pageProperties).isEqualTo(properties);
    }

    @Test
    public void should_getPageProperties_throw_already_exists_if_check_to_true() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        doReturn(mock(SPage.class)).when(pageService).getPageByName("MyPage");
        // when
        expectedException.expect(AlreadyExistsException.class);
        pageAPIExt.getPageProperties(content,true);

        // then
        // AlreadyExistsException
    }
    @Test
    public void should_getPageProperties_throw_retrieve_exception() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        doThrow(new SBonitaReadException(new IOException("IO issue"))).when(pageService).getPageByName("MyPage");
        // when
        expectedException.expect(RetrieveException.class);
        pageAPIExt.getPageProperties(content,true);

        // then
        // AlreadyExistsException
    }

    @Test
    public void should_getPageProperties_not_throw_already_exists_if_check_to_false() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        doReturn(mock(SPage.class)).when(pageService).getPageByName("MyPage");
        // when
        Properties pageProperties = pageAPIExt.getPageProperties(content, false);

        // then
        assertThat(pageProperties).isEqualTo(properties);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_missing_index() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        doThrow(SInvalidPageZipMissingIndexException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipMissingIndexException.class);
        pageAPIExt.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_missing_properties() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        doThrow(SInvalidPageZipMissingPropertiesException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipMissingPropertiesException.class);
        pageAPIExt.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_missing_a_property() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        doThrow(SInvalidPageZipMissingAPropertyException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipMissingAPropertyException.class);
        pageAPIExt.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_inconsistent() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        doThrow(SInvalidPageZipInconsistentException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipInconsistentException.class);
        pageAPIExt.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_token() throws Exception {
        // given
        byte[] content = new byte[]{1, 2, 3};
        doThrow(SInvalidPageTokenException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageTokenException.class);
        pageAPIExt.getPageProperties(content,false);
    }
}
