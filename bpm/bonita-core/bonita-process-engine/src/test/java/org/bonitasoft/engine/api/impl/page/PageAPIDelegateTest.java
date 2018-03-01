/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.api.impl.transaction.page.SearchPages;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipInconsistentException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingIndexException;
import org.bonitasoft.engine.exception.InvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageMappingService;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.page.PageUpdater.PageUpdateField;
import org.bonitasoft.engine.page.SInvalidPageTokenException;
import org.bonitasoft.engine.page.SInvalidPageZipInconsistentException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.SPageUpdateBuilder;
import org.bonitasoft.engine.page.SPageUpdateContentBuilder;
import org.bonitasoft.engine.page.impl.SPageMappingImpl;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageAPIDelegateTest {

    private static final long PAGE_ID = 4578;
    private static final Long PROCESS_ID_1 = 566446515l;
    private static final Long PROCESS_ID_2 = 566746515l;
    private final long userId = 123L;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    SearchPages searchPages;
    @Mock
    TenantServiceAccessor serviceAccessor;
    @Mock
    SPage sPage;
    @Mock
    Page page;
    @Mock
    private PageUpdater pageUpdater;
    @Mock
    private SPageUpdateBuilder sPageUpdateBuilder;
    @Mock
    private SPageUpdateContentBuilder sPageUpdateContentBuilder;
    private PageAPIDelegate pageAPIDelegate;
    @Mock
    private PageService pageService;
    @Mock
    private PageMappingService pageMappingService;
    @Mock
    private FormMappingService formMappingService;
    @Mock
    private BusinessArchiveArtifactsManager businessArchiveArtifactsManager;

    @Before
    public void before() {
        doReturn(pageService).when(serviceAccessor).getPageService();
        doReturn(pageMappingService).when(serviceAccessor).getPageMappingService();
        doReturn(formMappingService).when(serviceAccessor).getFormMappingService();
        doReturn(businessArchiveArtifactsManager).when(serviceAccessor).getBusinessArchiveArtifactsManager();
        doReturn(mock(SearchEntitiesDescriptor.class)).when(serviceAccessor).getSearchEntitiesDescriptor();
        pageAPIDelegate = spy(new PageAPIDelegate(serviceAccessor, userId));
    }

    @Test
    public void testSearchPages() throws Exception {
        doReturn(searchPages).when(pageAPIDelegate).getSearchPages(any(SearchOptions.class));

        // when
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 5).searchTerm("search").done();
        pageAPIDelegate.searchPages(searchOptions);

        // then
        verify(searchPages, times(1)).execute();
    }

    @Test
    public void createPageShouldCallAddPageOnPageService() throws Exception {
        // given
        final PageCreator pageCreator = new PageCreator("name", "content.zip");
        final byte[] content = "content".getBytes();

        doReturn(sPage).when(pageAPIDelegate).constructPage(pageCreator, userId);
        doReturn(page).when(pageAPIDelegate).convertToPage(nullable(SPage.class));

        // when
        pageAPIDelegate.createPage(pageCreator, content);

        // then
        verify(pageService, times(1)).addPage(sPage, content);
    }

    @Test
    public void createPageWithContentShouldCallAddPageOnPageService() throws Exception {
        // given
        final byte[] content = "content".getBytes();
        final String contentName = "contentName";

        doReturn(page).when(pageAPIDelegate).convertToPage(nullable(SPage.class));

        // when
        pageAPIDelegate.createPage(contentName, content);

        // then
        verify(pageService, times(1)).addPage(content, contentName, userId);
    }

    @Test
    public void testDeletePage() throws Exception {
        final long pageId = 123;
        final SFormMappingImpl formMapping = new SFormMappingImpl();
        formMapping.setPageMapping(new SPageMappingImpl());
        doReturn(Collections.<SFormMapping> singletonList(formMapping)).when(formMappingService).searchFormMappings(any(QueryOptions.class));

        pageAPIDelegate.deletePage(pageId);

        verify(pageService).deletePage(pageId);
        verify(pageMappingService).update(any(SPageMapping.class), isNull(Long.class)); // As the page has just been deleted
        verify(formMappingService, times(1)).searchFormMappings(any(QueryOptions.class));
    }

    @Test
    public void updatePageMapping_return_affected_processes() throws Exception {
        doReturn(Arrays.asList(formMapping(PROCESS_ID_1), formMapping(PROCESS_ID_2), formMapping(PROCESS_ID_1))).when(formMappingService).searchFormMappings(
                any(QueryOptions.class));

        final Set<Long> processDefinitionIds = pageAPIDelegate.updatePageMappings(PAGE_ID);

        assertThat(processDefinitionIds).containsOnly(PROCESS_ID_1, PROCESS_ID_2);
    }

    @Test
    public void deletePage_update_resolution_of_related_processes() throws Exception {
        doReturn(Arrays.asList(formMapping(PROCESS_ID_1), formMapping(PROCESS_ID_2), formMapping(PROCESS_ID_1))).when(formMappingService).searchFormMappings(
                any(QueryOptions.class));

        pageAPIDelegate.deletePage(PAGE_ID);

        verify(businessArchiveArtifactsManager).resolveDependencies(PROCESS_ID_1, serviceAccessor);
        verify(businessArchiveArtifactsManager).resolveDependencies(PROCESS_ID_2, serviceAccessor);
    }

    private SFormMapping formMapping(Long processId) {
        final SFormMappingImpl sFormMapping = new SFormMappingImpl();
        sFormMapping.setProcessDefinitionId(processId);
        return sFormMapping;
    }

    @Test
    public void testDeletePages() throws Exception {
        // given
        final List<Long> pageIds = new ArrayList<Long>();
        for (long pageId = 0; pageId < 10; pageId++) {
            pageIds.add(pageId);
        }
        doNothing().when(pageAPIDelegate).deletePage(anyLong());

        // when
        pageAPIDelegate.deletePages(pageIds);

        // then
        verify(pageAPIDelegate, times(pageIds.size())).deletePage(anyLong());
    }

    @Test
    public void testGetPage() throws Exception {
        // given
        final long pageId = 123;
        doReturn(sPage).when(pageService).getPage(pageId);

        // when
        pageAPIDelegate.getPage(pageId);

        // then
        verify(pageService, times(1)).getPage(pageId);
    }

    @Test
    public void testGetPageByName() throws Exception {
        // given
        final String pageName = "name";
        doReturn(sPage).when(pageService).getPageByName(pageName);

        // when
        pageAPIDelegate.getPageByName(pageName);

        // then
        verify(pageService, times(1)).getPageByName(pageName);
    }

    @Test(expected = PageNotFoundException.class)
    public void testGetPageByNameNotFound() throws Exception {
        // when
        pageAPIDelegate.getPageByName("unknown");
        // then: exception
    }

    @Test(expected = UpdateException.class)
    public void testUpdatePageWithEmplyUpdateFileShouldThrowExceptions() throws Exception {
        // given
        final Map<PageUpdateField, String> map = new HashMap<PageUpdater.PageUpdateField, String>();
        doReturn(map).when(pageUpdater).getFields();

        // when
        pageAPIDelegate.updatePage(1, pageUpdater);

        // then
        verify(pageService, times(1)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
    }

    @Test
    public void testUpdatePageContent() throws Exception {
        doReturn(sPageUpdateBuilder).when(pageAPIDelegate).getPageUpdateBuilder();
        doReturn(mock(SPage.class)).when(pageService).getPage(1);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair("Index.groovy", "content of the groovy".getBytes()),
                pair("page.properties", "name=mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));
        final long pageId = 1;

        // when
        pageAPIDelegate.updatePageContent(pageId, content);

        // then
        verify(pageService, times(1)).updatePageContent(anyLong(), eq(content), nullable(String.class));
    }

    @Test
    public void testUpdatePage() throws Exception {
        final Map<PageUpdateField, String> map = new HashMap<PageUpdater.PageUpdateField, String>();
        doReturn(sPage).when(pageAPIDelegate).constructPage(any(PageUpdater.class), anyLong());
        doReturn(page).when(pageAPIDelegate).convertToPage(nullable(SPage.class));
        doReturn(sPageUpdateBuilder).when(pageAPIDelegate).getPageUpdateBuilder();
        doReturn(map).when(pageUpdater).getFields();

        // given
        map.put(PageUpdateField.DISPLAY_NAME, "displayname");
        map.put(PageUpdateField.NAME, "name");
        map.put(PageUpdateField.DESCRIPTION, "description");
        map.put(PageUpdateField.CONTENT_NAME, "content.zip");

        // when
        pageAPIDelegate.updatePage(1, pageUpdater);

        // then
        verify(pageService, times(1)).updatePage(anyLong(), nullable(EntityUpdateDescriptor.class));
    }

    @Test
    public void testGetPageContent() throws Exception {
        // given
        final long pageId = 123;

        // when
        pageAPIDelegate.getPageContent(pageId);

        // then
        verify(pageService, times(1)).getPageContent(pageId);
    }

    @Test(expected = AlreadyExistsException.class)
    public void testCheckPageAlreadyExists() throws Exception {
        // given
        final PageCreator pageCreator = new PageCreator("name", "content.zip");
        doReturn(sPage).when(pageAPIDelegate).constructPage(pageCreator, userId);
        final byte[] content = IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
        doThrow(SObjectAlreadyExistsException.class).when(pageService).addPage(sPage, content);

        // when
        pageAPIDelegate.createPage(pageCreator, content);

        // then
        // AlreadyExistsException

    }

    @Test
    public void should_getPageProperties_check_already_exists_if_check_to_true() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        final Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        doReturn(null).when(pageService).getPageByName("MyPage");
        // when
        final Properties pageProperties = pageAPIDelegate.getPageProperties(content, true);

        // then
        assertThat(pageProperties).isEqualTo(properties);
    }

    @Test
    public void should_getPageProperties_throw_already_exists_if_check_to_true() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        final Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        doReturn(mock(SPage.class)).when(pageService).getPageByName("MyPage");
        // when
        expectedException.expect(AlreadyExistsException.class);
        pageAPIDelegate.getPageProperties(content, true);

        // then
        // AlreadyExistsException
    }

    @Test
    public void should_getPageProperties_throw_retrieve_exception() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        final Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        doThrow(new SBonitaReadException(new IOException("IO issue"))).when(pageService).getPageByName("MyPage");
        // when
        expectedException.expect(RetrieveException.class);
        pageAPIDelegate.getPageProperties(content, true);

        // then
        // AlreadyExistsException
    }

    @Test
    public void should_getPageProperties_not_throw_already_exists_if_check_to_false() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        final Properties properties = new Properties();
        properties.setProperty(PageService.PROPERTIES_NAME, "MyPage");
        doReturn(properties).when(pageService).readPageZip(content);
        // when
        final Properties pageProperties = pageAPIDelegate.getPageProperties(content, false);

        // then
        assertThat(pageProperties).isEqualTo(properties);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_missing_index() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        doThrow(SInvalidPageZipMissingIndexException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipMissingIndexException.class);
        pageAPIDelegate.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_missing_properties() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        doThrow(SInvalidPageZipMissingPropertiesException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipMissingPropertiesException.class);
        pageAPIDelegate.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_missing_a_property() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        doThrow(SInvalidPageZipMissingAPropertyException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipMissingAPropertyException.class);
        pageAPIDelegate.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_inconsistent() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        doThrow(SInvalidPageZipInconsistentException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageZipInconsistentException.class);
        pageAPIDelegate.getPageProperties(content, false);
    }

    @Test
    public void should_getPageProperties_throw_Invalid_token() throws Exception {
        // given
        final byte[] content = new byte[] { 1, 2, 3 };
        doThrow(SInvalidPageTokenException.class).when(pageService).readPageZip(content);
        // when
        expectedException.expect(InvalidPageTokenException.class);
        pageAPIDelegate.getPageProperties(content, false);
    }

}
