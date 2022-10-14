/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.page;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.groups.Tuple;
import org.bonitasoft.console.common.server.page.CustomPageService;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.console.common.server.servlet.FileUploadServlet;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.UnauthorizedFolderException;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageCreator.PageField;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.page.PageUpdater;
import org.bonitasoft.engine.search.SearchFilterOperation;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.page.PageResourceProvider;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Fabio Lombardi
 */

@RunWith(MockitoJUnitRunner.class)
public class PageDatastoreTest extends APITestWithMock {

    public static final long TENANT_ID = 1L;
    public static final long PAGE_ID = 123L;
    public static final String PAGE_ZIP = "page.zip";
    public static final String PAGE_REST_API_ZIP = "pageApiExtension.zip";
    private PageDatastore pageDatastore;

    @Mock
    APISession engineSession;

    @Mock
    PageAPI pageAPI;

    @Mock
    PageItem pageItem;

    @Mock
    WebBonitaConstantsUtils constantsValue;

    @Mock
    Page mockedPage;

    @Mock
    Page mockedApiExtension;

    @Mock
    BonitaHomeFolderAccessor tenantFolder;

    @Mock
    CustomPageService customPageService;

    @Mock
    private File mockedZipFile;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    PageItem pageToBeAdded;
    PageItem apiExtensionToBeAdded;

    File pagesDir = new File("target/bonita-home/client/tenants/1/work/pages");

    @Mock
    private PageResourceProviderImpl pageResourceProvider;

    @Before
    public void setUp() throws Exception {
        final Date mockedDate = new Date(0L);

        final File apiExtensionZipFile = deployZipFileToTarget(PAGE_REST_API_ZIP);
        final File pageZipFile = deployZipFileToTarget(PAGE_ZIP);

        deleteDir(pagesDir);
        // Given
        when(mockedPage.getId()).thenReturn(PAGE_ID);
        when(mockedPage.getName()).thenReturn("custompage_page1");
        when(mockedPage.getDisplayName()).thenReturn("Page 1");
        when(mockedPage.getDescription()).thenReturn("This is a page description");
        when(mockedPage.getInstallationDate()).thenReturn(mockedDate);
        when(mockedPage.getLastModificationDate()).thenReturn(mockedDate);
        when(mockedPage.getInstalledBy()).thenReturn(1L);
        when(mockedPage.isProvided()).thenReturn(false);

        when(mockedApiExtension.getId()).thenReturn(PAGE_ID);
        when(mockedApiExtension.getName()).thenReturn("custompage_apiExt");
        when(mockedApiExtension.getDisplayName()).thenReturn("Page 1");
        when(mockedApiExtension.getDescription()).thenReturn("This is a page description");
        when(mockedApiExtension.getInstallationDate()).thenReturn(mockedDate);
        when(mockedApiExtension.getLastModificationDate()).thenReturn(mockedDate);
        when(mockedApiExtension.getInstalledBy()).thenReturn(1L);

        when(tenantFolder.getTempFile(PAGE_ZIP)).thenReturn(pageZipFile);
        when(tenantFolder.getTempFile(PAGE_REST_API_ZIP)).thenReturn(pageZipFile);

        when(constantsValue.getTempFolder()).thenReturn(pageZipFile.getParentFile());
        when(constantsValue.getPagesFolder()).thenReturn(pagesDir);

        pageDatastore = spy(new PageDatastore(engineSession, constantsValue, pageAPI, customPageService, tenantFolder));

        pageToBeAdded = new PageItem();
        pageToBeAdded.setUrlToken("custompage_page1");
        pageToBeAdded.setDisplayName("Page 1");
        pageToBeAdded.setDescription("This is a page description");
        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, pageZipFile.getName());

        apiExtensionToBeAdded = new PageItem();
        apiExtensionToBeAdded.setUrlToken("custompage_apiExt");
        apiExtensionToBeAdded.setDisplayName("Page 1");
        apiExtensionToBeAdded.setDescription("This is a page description");
        apiExtensionToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, apiExtensionZipFile.getName());

    }

    private File deployZipFileToTarget(final String zipFileName) throws IOException, URISyntaxException {
        final File file = new File(getClass().getResource(zipFileName).toURI());
        assertThat(file).as("file should exists " + file.getAbsolutePath()).exists();
        FileUtils.copyFileToDirectory(file, new File("target"));
        return new File("target/" + zipFileName);
    }

    @Test
    public void should_add_a_page_return_valid_page() throws Exception {

        // Given
        deleteDir(pagesDir);
        when(pageAPI.createPage(any(String.class), any(byte[].class))).thenReturn(mockedPage);

        // When
        final PageItem addedPage = pageDatastore.add(pageToBeAdded);

        // Validate
        assertNotNull(addedPage);
    }

    @Test
    public void should_add_a_api_extension_add_related_permission() throws Exception {
        // given
        when(pageAPI.createPage(any(String.class), any(byte[].class))).thenReturn(mockedApiExtension);
        doReturn(pageResourceProvider).when(customPageService).getPageResourceProvider(eq(mockedApiExtension));

        // when
        pageDatastore.add(apiExtensionToBeAdded);

        //then
        verify(customPageService).writePageToPageDirectory(any(Page.class), eq(pageResourceProvider), any(File.class),
                eq(engineSession));
    }

    @Test(expected = APIException.class)
    public void should_add_a_not_valid_page_rise_exception() {
        // Given
        deleteDir(pagesDir);
        final URL zipFileUrl = getClass().getResource("/InvalidPage.zip");
        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, zipFileUrl.getPath());

        // When
        final PageItem addedPage = pageDatastore.add(pageToBeAdded);

        // Validate
        assertNotNull(addedPage);
    }

    @Test(expected = APIException.class)
    public void should_get_a_not_existing_page_rise_exception() throws Exception {
        // Given
        deleteDir(pagesDir);
        when(pageAPI.getPage(1L)).thenThrow(new PageNotFoundException("newPage"));

        // When
        pageDatastore.get(makeAPIID(1L));
    }

    @Test
    public void should_thrown_ApiException_and_not_remove_contentFile_when_delete_a_not_existing() throws Exception {
        // Given
        deleteDir(pagesDir);
        when(pageAPI.getPage(1L)).thenThrow(new PageNotFoundException("newPage"));

        // When
        final Throwable throwable = catchThrowable(() -> pageDatastore.delete(singletonList(makeAPIID(1L))));

        assertThat(throwable).isInstanceOf(APIException.class);
        verify(pageAPI, never()).deletePage(1L);
        verify(customPageService, never()).removePageLocally(any(PageResourceProvider.class));
    }

    @Test
    public void should_call_customPageService_for_custom_page_permissions_when_adding_a_page() throws Exception {
        // Given
        deleteDir(pagesDir);
        when(pageAPI.createPage(any(String.class), any(byte[].class))).thenReturn(mockedPage);

        // When
        pageDatastore.add(pageToBeAdded);

        // Then
        verify(customPageService).writePageToPageDirectory(any(Page.class), eq(null), any(File.class),
                eq(engineSession));
    }

    @Test
    public void should_thrown_APIException_when_deletePage_thrown_DeletionException() throws Exception {
        // Given
        when(pageAPI.getPage(PAGE_ID)).thenReturn(mockedPage);
        doThrow(new DeletionException("")).when(pageAPI).deletePage(PAGE_ID);

        // When
        final Throwable throwable = catchThrowable(() -> pageDatastore.delete(singletonList(APIID.makeAPIID(PAGE_ID))));

        assertThat(throwable).isInstanceOf(APIException.class);
        verify(customPageService).ensurePageFolderIsUpToDate(any(), any());
        verify(customPageService, never()).removePageLocally(any(PageResourceProvider.class));
    }

    @Test
    public void should_deletePage_and_remove_all_dependencies_when_deleting_page() throws Exception {
        // Given
        when(pageAPI.getPage(PAGE_ID)).thenReturn(mockedApiExtension);
        doReturn(pageResourceProvider).when(customPageService).getPageResourceProvider(eq(mockedApiExtension));

        // When
        pageDatastore.delete(singletonList(makeAPIID(PAGE_ID)));

        // then
        verify(pageAPI).deletePage(mockedApiExtension.getId());
        verify(customPageService).removePageLocally(pageResourceProvider);
        verify(customPageService).ensurePageFolderIsUpToDate(any(), any());
    }

    @Test
    public void should_update_resource_permission_when_updating_api_extension() throws Exception {
        // Given
        when(pageAPI.getPage(mockedApiExtension.getId())).thenReturn(mockedApiExtension);
        when(pageAPI.updatePage(eq(mockedApiExtension.getId()), any(PageUpdater.class))).thenReturn(mockedApiExtension);

        doReturn(pageResourceProvider).when(customPageService).getPageResourceProvider(any(Page.class));

        final File apiExtensionZipFile = deployZipFileToTarget(PAGE_REST_API_ZIP);
        doReturn(apiExtensionZipFile).when(tenantFolder).getTempFile(eq(apiExtensionZipFile.getAbsolutePath()));

        // When
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE,
                apiExtensionZipFile.getAbsolutePath() + FileUploadServlet.RESPONSE_SEPARATOR
                        + apiExtensionZipFile.getName());
        pageDatastore.update(makeAPIID(mockedApiExtension.getId()), attributes);

        // then
        verify(pageAPI).updatePage(eq(PAGE_ID), any());
        verify(customPageService).ensurePageFolderIsUpToDate(any(), any());
        verify(customPageService).removePageLocally(any(Page.class));
        verify(customPageService).writePageToPageDirectory(any(Page.class), eq(pageResourceProvider), any(File.class),
                eq(engineSession));
    }

    @Test
    public void should_not_update_resource_and_permission_pageContent_when_pageApi_updatePageContent_thown_exception()
            throws Exception {
        // Given
        when(pageAPI.getPage(mockedApiExtension.getId())).thenReturn(mockedApiExtension);
        doReturn(pageResourceProvider).when(customPageService).getPageResourceProvider(any(Page.class));
        doThrow(new UpdateException("")).when(pageAPI).updatePageContent(eq(PAGE_ID), any());

        final File apiExtensionZipFile = deployZipFileToTarget(PAGE_REST_API_ZIP);
        doReturn(apiExtensionZipFile).when(tenantFolder).getTempFile(eq(apiExtensionZipFile.getAbsolutePath()));

        // When
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE,
                apiExtensionZipFile.getAbsolutePath() + FileUploadServlet.RESPONSE_SEPARATOR
                        + apiExtensionZipFile.getName());
        Throwable throwable = catchThrowable(
                () -> pageDatastore.update(makeAPIID(mockedApiExtension.getId()), attributes));

        // then
        assertThat(throwable).isInstanceOf(APIException.class);
        verify(customPageService).ensurePageFolderIsUpToDate(any(), any());
        verify(customPageService, never()).removePageLocally(any(Page.class));
        verify(customPageService).writePageToPageDirectory(any(Page.class), eq(pageResourceProvider), any(File.class),
                eq(engineSession));
    }

    @Test(expected = APIForbiddenException.class)
    public void it_throws_an_exception_adding_page_with_unauthorized_path() throws IOException {
        // Given
        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "unauthorized_page.zip");

        doThrow(new UnauthorizedFolderException("error")).when(tenantFolder).getTempFile("unauthorized_page.zip");

        // When
        pageDatastore.add(pageToBeAdded);

    }

    @Test
    public void it_throws_an_exception_when_cannot_write_file_on_add() throws IOException {
        // Given
        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "error_page.zip");
        doThrow(new IOException("error")).when(tenantFolder).getTempFile("error_page.zip");

        // When
        try {
            pageDatastore.add(pageToBeAdded);
        } catch (final APIException e) {
            assertEquals(e.getMessage(), "java.io.IOException: error");
        }

    }

    @Test(expected = APIForbiddenException.class)
    public void it_throws_an_exception_updatting_page_with_unauthorized_path()
            throws IOException, PageNotFoundException {
        // Given
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "unauthorized_page.zip");

        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "unauthorized_page.zip");
        doReturn(mockedPage).when(pageAPI).getPage(TENANT_ID);
        doThrow(new UnauthorizedFolderException("error")).when(tenantFolder).getTempFile("unauthorized_page.zip");

        // When
        pageDatastore.update(APIID.makeAPIID(TENANT_ID), attributes);

    }

    @Test
    public void it_throws_an_exception_when_cannot_write_file_on_update() throws IOException, PageNotFoundException {
        // Given
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "error_page.zip");

        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "error_page.zip");
        doReturn(mockedPage).when(pageAPI).getPage(TENANT_ID);
        doThrow(new IOException("error")).when(tenantFolder).getTempFile("error_page.zip");

        // When

        final Throwable throwable = catchThrowable(() -> pageDatastore.update(APIID.makeAPIID(TENANT_ID), attributes));

        assertThat(throwable).isInstanceOf(APIException.class).hasMessage("java.io.IOException: error");

    }

    private void deleteDir(final File pagesDir) {
        if (pagesDir.exists()) {
            try {
                IOUtil.deleteDir(pagesDir);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void it_should_set_the_process_definition_id_on_creation() throws Exception {
        final byte[] zipContent = new byte[1];
        final APIID processId = APIID.makeAPIID(2555L);
        doReturn("contentName").when(pageItem).getContentName();
        doReturn(null).when(pageItem).getContentType();
        doReturn(processId).when(pageItem).getProcessId();
        doReturn(zipContent).when(pageDatastore).readZipFile(mockedZipFile);
        doReturn(mockedPage).when(pageAPI).createPage(anyString(), any(byte[].class));
        pageDatastore.createEnginePage(pageItem, mockedZipFile);
        final PageUpdater pageUpdater = new PageUpdater();
        pageUpdater.setProcessDefinitionId(processId.toLong());
        final ArgumentCaptor<PageUpdater> argumentCaptor = ArgumentCaptor.forClass(PageUpdater.class);

        verify(pageAPI, times(1)).updatePage(eq(mockedPage.getId()), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(pageUpdater);
    }

    @Test
    public void it_should_not_set_the_process_definition_id_on_creation() throws Exception {
        final byte[] zipContent = new byte[1];
        doReturn("contentName").when(pageItem).getContentName();
        doReturn(null).when(pageItem).getProcessId();
        doReturn(zipContent).when(pageDatastore).readZipFile(mockedZipFile);
        doReturn(mockedPage).when(pageAPI).createPage(anyString(), any(byte[].class));
        pageDatastore.createEnginePage(pageItem, mockedZipFile);

        verify(pageAPI, times(0)).updatePage(anyLong(), any(PageUpdater.class));
    }

    @Test
    public void it_should_set_the_content_type_on_creation_if_it_is_given() throws Exception {
        final byte[] zipContent = new byte[1];
        final APIID processId = APIID.makeAPIID(2555L);
        doReturn("contentName").when(pageItem).getContentName();
        doReturn(ContentType.PAGE).when(pageItem).getContentType();
        doReturn(processId).when(pageItem).getProcessId();
        doReturn(zipContent).when(pageDatastore).readZipFile(mockedZipFile);
        doReturn(mockedPage).when(pageAPI).createPage(anyString(), any(byte[].class));
        pageDatastore.createEnginePage(pageItem, mockedZipFile);
        final PageUpdater pageUpdater = new PageUpdater();
        pageUpdater.setContentType(ContentType.PAGE);
        pageUpdater.setProcessDefinitionId(processId.toLong());
        final ArgumentCaptor<PageUpdater> argumentCaptor = ArgumentCaptor.forClass(PageUpdater.class);

        verify(pageAPI, times(1)).updatePage(eq(mockedPage.getId()), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(pageUpdater);
    }

    @Test
    public void it_should_set_the_new_zip_name_value_to_original_file_name_field_on_creation() throws Exception {

        // Given
        deleteDir(pagesDir);
        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "page.zip::newPage.zip");
        when(pageAPI.createPage(any(String.class), any(byte[].class))).thenReturn(mockedPage);

        // When
        pageDatastore.add(pageToBeAdded);

        final ArgumentCaptor<PageItem> argumentCaptor = ArgumentCaptor.forClass(PageItem.class);

        //then
        verify(pageDatastore, times(1)).createEnginePage(argumentCaptor.capture(), any(File.class));

        assertThat(argumentCaptor.getValue().getContentName()).isEqualTo("newPage.zip");
    }

    @Test
    public void it_should_set_the_new_zip_name_value_to_original_file_name_field_on_creation_with_contentName_attribute()
            throws Exception {

        // Given
        deleteDir(pagesDir);
        pageToBeAdded.setAttribute(PageDatastore.UNMAPPED_ATTRIBUTE_ZIP_FILE, "page.zip");
        pageToBeAdded.setAttribute(PageItem.ATTRIBUTE_CONTENT_NAME, "newPage.zip");
        when(pageAPI.createPage(any(String.class), any(byte[].class))).thenReturn(mockedPage);

        // When
        pageDatastore.add(pageToBeAdded);

        final ArgumentCaptor<PageItem> argumentCaptor = ArgumentCaptor.forClass(PageItem.class);

        //then
        verify(pageDatastore, times(1)).createEnginePage(argumentCaptor.capture(), any(File.class));

        assertThat(argumentCaptor.getValue().getContentName()).isEqualTo("newPage.zip");
    }

    @Test
    public void testBuildPageCreatorFrom() {
        final String testUrlTokenString = "testUrlTokenString";
        final String testContentNameString = "testContentName";
        final String testDescriptionString = "testDescriptionString";
        final String testDisplayNameString = "testDescriptionString";

        doReturn(testUrlTokenString).when(pageItem).getUrlToken();
        doReturn(testContentNameString).when(pageItem).getContentName();
        doReturn(testDescriptionString).when(pageItem).getDescription();
        doReturn(testDisplayNameString).when(pageItem).getDisplayName();

        final PageCreator pageCreator = pageDatastore.buildPageCreatorFrom(pageItem);

        final Map<PageField, Serializable> expectedFields = new HashMap<>();
        expectedFields.put(PageField.NAME, testUrlTokenString);
        expectedFields.put(PageField.CONTENT_NAME, testContentNameString);
        expectedFields.put(PageField.CONTENT_TYPE, ContentType.PAGE);
        expectedFields.put(PageField.DESCRIPTION, testDescriptionString);
        expectedFields.put(PageField.DISPLAY_NAME, testDisplayNameString);

        assertThat(pageCreator.getFields()).isEqualTo(expectedFields);
    }

    @Test
    public void should_IsPageTokenValid_returns_true_when_url_token_starts_with_custom_page_prefix() {
        assertThat(pageDatastore.isPageTokenValid(PageDatastore.PAGE_TOKEN_PREFIX + "anySuffix123456")).isTrue();
    }

    @Test
    public void should_IsPageTokenValid_returns_false_when_url_token_contains_non_alphanumeric_values() {
        assertThat(pageDatastore.isPageTokenValid(PageDatastore.PAGE_TOKEN_PREFIX + "suffixxx_dsqds")).isFalse();
    }

    @Test
    public void should_IsPageTokenValid_returns_false_when_url_token_does_not_starts_with_custom_page_prefix() {
        assertThat(pageDatastore.isPageTokenValid("WrongStartString" + PageDatastore.PAGE_TOKEN_PREFIX + "suffixx"))
                .isFalse();
    }

    @Test
    public void should_ConvertEngineToConsoleItem_returns_null_if_item_is_null() {
        assertThat(pageDatastore.convertEngineToConsoleItem(null)).isNull();
    }

    @Test
    public void testAPIIdsToLong() {
        final APIID id1 = makeAPIID("1");
        final APIID id2 = makeAPIID("2");
        final List<APIID> listId = Arrays.asList(id1, id2);

        assertThat(pageDatastore.APIIdsToLong(listId)).isEqualTo(Arrays.asList(1L, 2L));
    }

    @Test
    public void testRunSearch() throws Exception {
        final SearchOptionsCreator creator = new SearchOptionsCreator(0, 1, null, new Sorts(null), new Filters(null));
        pageDatastore.runSearch(creator);
        verify(pageAPI, times(1)).searchPages(creator.create());
    }

    @Test
    public void should_Get_returns_a_page_item_for_a_given_APIID() throws Exception {
        final APIID apiid = makeAPIID(2L);
        pageDatastore.get(apiid);
        verify(pageAPI, times(1)).getPage(eq(apiid.toLong()));
        verify(pageDatastore, times(1)).convertEngineToConsoleItem(any(Page.class));
    }

    @Test
    public void should_ConvertEngineToConsoleItem_return_null_when_null_page_is_given() {
        //given
        final Page nullPage = null;
        //when
        final PageItem testPageItem = pageDatastore.convertEngineToConsoleItem(nullPage);
        //then
        assertThat(testPageItem).isNull();
    }

    @Test
    public void should_ConvertEngineToConsoleItem_return_a_page_item_when_page_is_given() {
        //when
        final PageItem testPageItem = pageDatastore.convertEngineToConsoleItem(mockedPage);
        //then
        assertThat(testPageItem).isNotNull();
    }

    @Test
    public void should_DeleteTempDirectory_throw_API_Exception_when_an_exception_is_raised() throws Exception {
        //given
        doThrow(new IOException("to be test")).when(pageDatastore).IOUtilDeleteDir(any(File.class));

        //when
        try {
            pageDatastore.deleteTempDirectory(mockedZipFile);
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(APIException.class);
        }
    }

    @Test
    public void makeSearchOptionCreator_converts_isProvided_field_to_boolean() {
        final List<SearchFilter> filters = pageDatastore.makeSearchOptionCreator(0, 10, "", "displayName ASC",
                Collections.singletonMap(PageItem.ATTRIBUTE_IS_PROVIDED, "false")).create().getFilters();
        assertThat(filters.get(0).getValue()).isEqualTo(false);
    }

    @Test
    public void makeSearchOptionCreator_with_empty_filter_map_should_return_empty_filter_list() {
        final SearchOptionsCreator searchOptionsCreator = pageDatastore.makeSearchOptionCreator(0, 10, "",
                "displayName ASC", new HashMap<>());
        final List<SearchFilter> filters = searchOptionsCreator.create().getFilters();
        assertThat(filters).isEmpty();
    }

    @Test
    public void makeSearchOptionCreator_with_ATTRIBUTE_PROCESS_ID_filter_map_should_return_ATTRIBUTE_PROCESS_ID_in_filter_list() {
        final Map<String, String> filters = new HashMap<>();
        final String processID = "2124654";
        filters.put(PageItem.ATTRIBUTE_PROCESS_ID, processID);
        final SearchOptionsCreator searchOptionsCreator = pageDatastore.makeSearchOptionCreator(0, 10, "",
                "displayName ASC", filters);
        final List<SearchFilter> filtersResult = searchOptionsCreator.create().getFilters();
        assertThat(filtersResult).extracting("field", "operation", "value")
                .contains(new Tuple("processDefinitionId", SearchFilterOperation.EQUALS, processID));
    }

    @Test
    public void makeSearchOptionCreator_with_FILTER_CONTENT_TYPE_form_filter_map_should_return_FILTER_CONTENT_TYPE_in_filter_list() {
        final Map<String, String> filters = new HashMap<>();
        final String form = "form";
        filters.put(PageItem.FILTER_CONTENT_TYPE, form);
        final SearchOptionsCreator searchOptionsCreator = pageDatastore.makeSearchOptionCreator(0, 10, "",
                "displayName ASC", filters);
        final List<SearchFilter> filtersResult = searchOptionsCreator.create().getFilters();
        assertThat(filtersResult).extracting("field", "operation", "value")
                .contains(new Tuple("contentType", SearchFilterOperation.EQUALS, form));
    }

    @Test
    public void makeSearchOptionCreator_with_FILTER_CONTENT_TYPE_processPage_filter_map_should_return_FILTER_CONTENT_TYPE_form_or_page_in_filter_list() {
        final Map<String, String> filters = new HashMap<>();
        final String form = "processPage";
        filters.put(PageItem.FILTER_CONTENT_TYPE, form);
        final SearchOptionsCreator searchOptionsCreator = pageDatastore.makeSearchOptionCreator(0, 10, "",
                "displayName ASC", filters);
        final List<SearchFilter> filtersResult = searchOptionsCreator.create().getFilters();
        assertThat(filtersResult).extracting("field", "operation", "value").contains(
                new Tuple(null, SearchFilterOperation.L_PARENTHESIS, null),
                new Tuple("contentType", SearchFilterOperation.EQUALS, "form"),
                new Tuple(null, SearchFilterOperation.OR, null),
                new Tuple("contentType", SearchFilterOperation.EQUALS, "page"),
                new Tuple(null, SearchFilterOperation.R_PARENTHESIS, null));
    }

}
