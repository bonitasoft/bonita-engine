/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.page.impl;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.authorization.PermissionService;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.page.AbstractSPage;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.PageServiceListener;
import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SInvalidPageTokenException;
import org.bonitasoft.engine.page.SInvalidPageZipException;
import org.bonitasoft.engine.page.SInvalidPageZipInconsistentException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingAPropertyException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import org.bonitasoft.engine.page.SInvalidPageZipMissingPropertiesException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageLogBuilder;
import org.bonitasoft.engine.page.SPageWithContent;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadOnlySelectByIdDescriptor;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.util.DigestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PageServiceImplTest {

    private static final String PAGE_PROPERTIES = "page.properties";

    private static final String INDEX_HTML = "index.html";

    private static final String INDEX_GROOVY = "Index.groovy";

    private static final String THEME_CSS = "resources/theme.css";

    private static final String CONTENT_NAME = "content.zip";

    private static final boolean PROVIDED_TRUE = true;

    private static final int INSTALLED_BY_ID = 45;

    private static final int INSTALLATION_DATE_AS_LONG = 123456;

    private static final String PAGE_NAME = "custompage_pageName";

    public static final long PROCESS_DEFINITION_ID = 846L;

    public static final long USER_ID = 98989L;

    public static final String DEFAULT_THEME_NAME = "custompage_themeBonita";
    public static final String DEFAULT_LAYOUT_NAME = "custompage_defaultlayout";

    @Mock
    private Recorder recorder;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private QueryOptions queryOptions;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private SPageLogBuilder pageLogBuilder;
    @Mock
    private ReadSessionAccessor sessionAccessor;
    @Mock
    private SessionService sessionService;
    @Mock
    private PermissionService permissionService;

    @Mock
    private EntityUpdateDescriptor entityUpdateDescriptor;
    @Captor
    private ArgumentCaptor<EntityUpdateDescriptor> entityUpdateDescriptorCaptor;

    private PageServiceImpl pageServiceImpl;

    @Mock
    PageServiceListener apiExtensionPageServiceListener;
    @Captor
    ArgumentCaptor<SPage> pageArgumentCaptor;

    @Before
    public void before() {
        pageServiceImpl = spy(
                new PageServiceImpl(readPersistenceService, recorder, queriableLoggerService, sessionAccessor,
                        sessionService, permissionService));
        doReturn(pageLogBuilder).when(pageServiceImpl).getPageLog(any(ActionType.class), anyString());
        doNothing().when(pageServiceImpl).initiateLogBuilder(anyLong(), anyInt(), any(SPersistenceLogBuilder.class),
                anyString());

        final List<PageServiceListener> listeners = singletonList(apiExtensionPageServiceListener);
        pageServiceImpl.setPageServiceListeners(listeners);
    }

    @Test
    public void getNumberOfPages() throws SBonitaException {
        // given
        final long expected = 50;
        when(readPersistenceService.getNumberOfEntities(SPage.class, queryOptions, null)).thenReturn(expected);

        // when
        final long numberOfPages = pageServiceImpl.getNumberOfPages(queryOptions);

        // then
        Assert.assertEquals(expected, numberOfPages);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfPagesThrowsException() throws SBonitaException {
        // given
        // when
        when(readPersistenceService.getNumberOfEntities(SPage.class, queryOptions, null))
                .thenThrow(new SBonitaReadException("ouch!"));
        pageServiceImpl.getNumberOfPages(queryOptions);

        // then
        // exception;
    }

    @Test(expected = SInvalidPageTokenException.class)
    public void createPage_should_throw_exception_when_name_is_empty() throws SBonitaException, IOException {

        final long pageId = 15;
        final SPage pageWithEmptyName = new SPage("", 123456, 45, true, CONTENT_NAME);
        pageWithEmptyName.setDisplayName("plop");
        pageWithEmptyName.setId(pageId);
        pageServiceImpl.addPage(pageWithEmptyName, validPageContent("plop"));

    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void addPage_should_throw_exception_when_already_exist() throws Exception {

        // given
        final SPage newPage = new SPage(PAGE_NAME, INSTALLATION_DATE_AS_LONG, INSTALLED_BY_ID, PROVIDED_TRUE,
                CONTENT_NAME);
        newPage.setDisplayName("plop");
        // when
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(newPage);
        pageServiceImpl.addPage(newPage, validPageContent(PAGE_NAME));

        // then exception

    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void should_create_page_throw_exception_when_name_exists() throws Exception {

        // given
        final SPage newPage = new SPage(PAGE_NAME, 123456, 45, true, CONTENT_NAME);
        newPage.setDisplayName("display Name");

        // when
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(newPage);
        final byte[] validContent = validPageContent(PAGE_NAME);
        pageServiceImpl.addPage(newPage, validContent);

        // then exception

    }

    @Test
    public void should_create_page_with_process_scope_when_name_exists() throws Exception {

        // given
        final SPage newPage = new SPage(PAGE_NAME, 123456, 45, false, CONTENT_NAME);
        newPage.setDisplayName("display Name");
        final byte[] validContent = validPageContent(PAGE_NAME);
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(null);
        pageServiceImpl.addPage(newPage, validContent);
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(newPage);

        // when
        final SPage newProcessPage = new SPage(PAGE_NAME, 123456, 45, false, CONTENT_NAME);
        newProcessPage.setContentType(SContentType.FORM);
        newProcessPage.setProcessDefinitionId(PROCESS_DEFINITION_ID);
        newProcessPage.setDisplayName("display Name");
        final SPage insertedPage = pageServiceImpl.addPage(newProcessPage, validContent);

        //then
        assertThat(insertedPage).isNotNull().isEqualToComparingFieldByField(newProcessPage);

    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void should_create_page_with_processDefinitionId_throw_exception_when_name_exists() throws Exception {

        // given
        final SPage newPage = new SPage(PAGE_NAME, 123456, 45, true, CONTENT_NAME);
        newPage.setContentType(SContentType.FORM);
        newPage.setProcessDefinitionId(PROCESS_DEFINITION_ID);
        newPage.setDisplayName("display Name");

        // when
        when(pageServiceImpl.getPageByNameAndProcessDefinitionId(PAGE_NAME, PROCESS_DEFINITION_ID)).thenReturn(newPage);
        final byte[] validContent = validPageContent(PAGE_NAME);
        pageServiceImpl.addPage(newPage, validContent);

        // then exception

    }

    @SuppressWarnings("unchecked")
    private byte[] validPageContent(final String pageName) throws IOException {
        return IOUtil.zip(pair("Index.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, ("name=custompage_" + pageName
                        + "\ndisplayName=mypage display name\ndescription=mypage description\n").getBytes()));
    }

    @Test
    public void getPage() throws SBonitaException {
        final long pageId = 15;
        final SPage expected = new SPage("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId))).thenReturn(expected);
        // when
        final SPage page = pageServiceImpl.getPage(pageId);
        // then
        Assert.assertEquals(expected, page);
    }

    @Test(expected = SObjectNotFoundException.class)
    public void getPageThrowsPageNotFoundException() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPage("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId))).thenReturn(null);

        pageServiceImpl.getPage(pageId);
    }

    @Test
    public void getPageByNameReturnsNullWhenNotFound() throws SBonitaException {
        // given: page does not exists
        // when
        final SPage pageByName = pageServiceImpl.getPageByName("unknown");
        // then
        assertNull(pageByName);
    }

    @Test(expected = SBonitaReadException.class)
    public void getPageThrowsException() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPage("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId))).thenThrow(
                new SBonitaReadException("ouch!"));

        pageServiceImpl.getPage(pageId);
    }

    @Test
    public void init_should_import_all_provided_page_if_is_the_first_run() throws SBonitaException {
        // given
        // resource in the classpath bonita-groovy-example-page.zip
        final Map<String, String> map = new HashMap<>();
        map.put("custompage_htmlexample_editonly", "page");
        map.put("custompage_htmlexample_final", "page");
        map.put(DEFAULT_LAYOUT_NAME, "layout");
        map.put("custompage_htmlexample", "page");
        map.put("custompage_groovyexample", "page");
        map.put("custompage_home", "page");
        map.put("custompage_tenantStatusBonita", "page");
        map.put(DEFAULT_THEME_NAME, "theme");
        doAnswer(invocation -> invocation.getArguments()[0]).when(pageServiceImpl)
                .insertPage(pageArgumentCaptor.capture(), any());

        // when
        pageServiceImpl.init();

        // then
        List<SPage> insertedPages = pageArgumentCaptor.getAllValues();
        assertThat(insertedPages)
                .hasSize(8)
                .allSatisfy(insertedPage -> {
                    String name = insertedPage.getName();
                    assertThat(name).isIn(map.keySet());
                    assertThat(insertedPage.getContentType()).isEqualTo(map.get(name));
                    assertThat(insertedPage.getPageHash()).isEqualTo(getHashOfContent(name));
                    if (insertedPage.getName().equals("custompage_htmlexample_editonly")) {
                        assertThat(insertedPage.isEditable()).isTrue();
                        assertThat(insertedPage.isProvided()).isTrue();
                        assertThat(insertedPage.isRemovable()).isFalse();
                    } else if (insertedPage.getName().equals("custompage_htmlexample_final")) {
                        assertThat(insertedPage.isEditable()).isFalse();
                        assertThat(insertedPage.isProvided()).isTrue();
                        assertThat(insertedPage.isRemovable()).isFalse();
                    } else {
                        assertThat(insertedPage.isProvided()).isTrue();
                        assertThat(insertedPage.isEditable()).isTrue();
                        assertThat(insertedPage.isRemovable()).isTrue();
                        assertThat(insertedPage.getContentType()).isEqualTo(map.get(name));
                        assertThat(insertedPage.getPageHash()).isEqualTo(getHashOfContent(name));
                    }
                });

        verify(pageServiceImpl, never()).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), anyString());
    }

    @Test
    public void import_provided_page_should_return_import_status_added_if_page_is_missing_equal_true()
            throws Exception {
        // given: a zip without properties
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        doReturn(null).when(pageServiceImpl).getPageByName("custompage_mypage");

        ImportStatus importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, true, true, true);

        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
    }

    @Test
    public void should_return_skipped_when_page_does_not_exist_and_addIfMissing_equals_false() throws Exception {
        // given: a zip without properties
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        doReturn(null).when(pageServiceImpl).getPageByName("custompage_mypage");

        ImportStatus importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, true, true, false);

        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);
        verify(pageServiceImpl, never()).insertPage(any(), any(byte[].class));
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
    }

    @Test
    public void should_replaced_an_existing_provided_page_when_content_is_different_and_still_provided()
            throws Exception {

        final SPage pageInDb = new SPage();
        pageInDb.setName("custompage_mypage");
        pageInDb.setDescription("mypage description");
        pageInDb.setDisplayName("mypage display name");
        pageInDb.setId(12);
        pageInDb.setProvided(true);
        pageInDb.setPageHash(DigestUtils.md5DigestAsHex("some hash".getBytes(StandardCharsets.UTF_8)));

        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(byte[].class), anyString());

        doReturn(pageInDb).when(pageServiceImpl).getPageByName("custompage_mypage");

        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        // final page
        ImportStatus importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, false, false,
                true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, false, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, true, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.REPLACED);

        verify(pageServiceImpl, times(3)).updatePageContent(eq(12L), eq(content), any());

    }

    @Test
    public void should_not_replaced_an_existing_page_when_content_is_different_and_no_more_provided()
            throws Exception {

        final SPage page = new SPage();
        page.setName("custompage_mypage");
        page.setDescription("mypage description");
        page.setDisplayName("mypage display name");
        page.setId(12);
        page.setProvided(false);

        page.setPageHash(DigestUtils.md5DigestAsHex("some hash".getBytes(StandardCharsets.UTF_8)));

        doReturn(page).when(pageServiceImpl).getPageByName("custompage_mypage");

        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        // final page
        ImportStatus importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, false, false,
                true);

        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);

        // edit only page
        importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, false, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);

        // removable page
        importStatus = pageServiceImpl.importProvidedPage("custompage_mypage", content, true, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);

        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
        verify(pageServiceImpl, never()).insertPage(any(), any(byte[].class));
    }

    @Test
    public void import_provided_page_should_do_nothing_and_throw_exception_when_exception_happened_on_get_page_by_name()
            throws Exception {

        final SPage page = new SPage();
        page.setName("custompage_mypage");
        page.setDescription("mypage description");
        page.setDisplayName("mypage display name");
        page.setId(12);
        page.setEditable(true);
        page.setRemovable(true);
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());
        page.setPageHash(DigestUtils.md5DigestAsHex(content));

        doThrow(SBonitaReadException.class).when(pageServiceImpl).getPageByName("custompage_mypage");

        Throwable expected = catchThrowable(
                () -> pageServiceImpl.importProvidedPage("custompage_mypage", content, true, true, true));

        assertThat(expected).isInstanceOf(SBonitaException.class);
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
        verify(pageServiceImpl, never()).insertPage(any(), any(byte[].class));
    }

    @Test
    public void init_should_not_insert_not_removable_provided_page_if_is_not_first_run() throws Exception {
        ImportStatus importStatus = new ImportStatus("custompage_htmlexample_final");
        importStatus.setStatus(ImportStatus.Status.SKIPPED);
        doReturn(importStatus).when(pageServiceImpl).importProvidedPage(eq("bonita-html-page-example-final.zip"),
                any(byte[].class), eq(false), eq(false), eq(true));
        doReturn(importStatus).when(pageServiceImpl).importProvidedPage(eq("bonita-html-page-example-editonly.zip"),
                any(byte[].class), eq(false), eq(true), eq(true));

        final Map<String, String> removablePage = new HashMap<>();
        removablePage.put(DEFAULT_LAYOUT_NAME, "layout");
        removablePage.put("custompage_htmlexample", "page");
        removablePage.put("custompage_groovyexample", "page");
        removablePage.put("custompage_home", "page");
        removablePage.put("custompage_tenantStatusBonita", "page");
        removablePage.put(DEFAULT_THEME_NAME, "theme");

        // when
        pageServiceImpl.init();
        // then
        verify(pageServiceImpl, times(6)).importProvidedPage(any(), any(byte[].class), eq(true), eq(true), eq(false));
        verify(pageServiceImpl, never()).insertPage(argThat(page -> !removablePage.containsKey(page.getName())),
                any(byte[].class));
    }

    @Test
    public void init_should_not_insert_anything_if_exception_happened_on_import_final_page() throws Exception {
        final Map<String, String> notImportedPages = new HashMap<>();
        notImportedPages.put("bonita-default-layout.zip", "layout");
        notImportedPages.put("bonita-html-page-example.zip", "page");
        notImportedPages.put("bonita-groovy-page-example.zip", "page");
        notImportedPages.put("bonita-home-page.zip", "page");
        notImportedPages.put("page-tenant-status.zip", "page");
        notImportedPages.put("bonita-html-page-example-editonly.zip", "theme");
        notImportedPages.put("bonita-theme.zip", "theme");

        doThrow(new SBonitaReadException("ouch")).when(pageServiceImpl)
                .importProvidedPage(eq("bonita-html-page-example-final.zip"), any(byte[].class), eq(false), eq(false),
                        eq(true));

        pageServiceImpl.init();

        verify(pageServiceImpl, never()).importProvidedPage(
                argThat(pageZipName -> notImportedPages.keySet().contains(pageZipName)),
                any(byte[].class), eq(true), eq(true), anyBoolean());
        verify(pageServiceImpl, never()).importProvidedPage(
                argThat(pageZipName -> notImportedPages.keySet().contains(pageZipName)),
                any(byte[].class), eq(false), eq(true), anyBoolean());
    }

    private String getHashOfContent(String name) {
        final Map<String, String> map = new HashMap<>();
        map.put(DEFAULT_LAYOUT_NAME, "/org/bonitasoft/web/page/bonita-default-layout.zip");
        map.put("custompage_htmlexample", "/org/bonitasoft/web/page/bonita-html-page-example.zip");
        map.put("custompage_htmlexample_editonly",
                "/org/bonitasoft/web/page/editonly/bonita-html-page-example-editonly.zip");
        map.put("custompage_htmlexample_final", "/org/bonitasoft/web/page/final/bonita-html-page-example-final.zip");
        map.put("custompage_groovyexample", "/org/bonitasoft/web/page/bonita-groovy-page-example.zip");
        map.put("custompage_home", "/org/bonitasoft/web/page/bonita-home-page.zip");
        map.put("custompage_tenantStatusBonita", "/org/bonitasoft/web/page/page-tenant-status.zip");
        map.put(DEFAULT_THEME_NAME, "/org/bonitasoft/web/page/bonita-theme.zip");
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(map.get(name));
            if (resourceAsStream == null) {
                throw new AssertionError("No content found for page " + name + " in classpath: " + map.get(name));
            }
            return DigestUtils.md5DigestAsHex(resourceAsStream.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void init_should_update_final_page_if_is_different() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPage currentGroovyPage = new SPage("custompage_htmlexample_final", "example", "example",
                System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentGroovyPage.setId(12L);
        currentGroovyPage.setRemovable(false);
        currentGroovyPage.setEditable(false);

        doReturn(currentGroovyPage).when(pageServiceImpl).getPageByName("custompage_htmlexample_final");
        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(byte[].class), anyString());

        // when
        pageServiceImpl.init();

        // then
        verify(pageServiceImpl).updatePageContent(eq(12L), any(byte[].class),
                eq("bonita-html-page-example-final.zip"));
    }

    @Test
    public void init_should_update_edit_only_page_if_is_different() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPage currentGroovyPage = new SPage("custompage_htmlexample_editonly", "example", "example",
                System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentGroovyPage.setId(12L);
        currentGroovyPage.setRemovable(false);
        currentGroovyPage.setEditable(true);

        doReturn(currentGroovyPage).when(pageServiceImpl).getPageByName("custompage_htmlexample_editonly");
        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(byte[].class), anyString());

        // when
        pageServiceImpl.init();

        // then
        verify(pageServiceImpl).updatePageContent(eq(12L), any(byte[].class),
                eq("bonita-html-page-example-editonly.zip"));
    }

    @Test
    public void getPageContent_should_add_properties_in_the_zip() throws SBonitaException, IOException {
        // given: a zip without properties
        final SPageWithContent page = new SPageWithContent();
        page.setName("mypage");
        page.setDescription("mypage description");
        page.setDisplayName("mypage display name");
        page.setId(12);
        final byte[] content = IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
        page.setContent(content);
        doReturn(page).when(readPersistenceService)
                .selectById(new ReadOnlySelectByIdDescriptor<>(SPageWithContent.class, 12));

        // when
        final byte[] result = pageServiceImpl.getPageContent(12);

        // then
        final Map<String, byte[]> unzip = IOUtil.unzip(result);
        assertThat(unzip.size()).isEqualTo(2);
        final Properties pageProperties = new Properties();
        pageProperties.load(new ByteArrayInputStream(unzip.get(PAGE_PROPERTIES)));

        assertThat(pageProperties.get("name")).isEqualTo("mypage");
        assertThat(pageProperties.get("displayName")).isEqualTo("mypage display name");
        assertThat(pageProperties.get("description")).isEqualTo("mypage description");
    }

    @Test
    public void getPageContent_should_add_properties_in_the_zip_with_non_mandatory_metadata()
            throws SBonitaException, IOException {
        final SPageWithContent page = new SPageWithContent();
        page.setName("mypage");
        page.setDisplayName("mypage display name");
        //no description
        page.setId(12);
        page.setContent(IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes())));
        doReturn(page).when(readPersistenceService)
                .selectById(new ReadOnlySelectByIdDescriptor<>(SPageWithContent.class, 12));

        // when
        final byte[] result = pageServiceImpl.getPageContent(12);

        // then
        final Properties pageProperties = new Properties();
        pageProperties.load(new ByteArrayInputStream(IOUtil.unzip(result).get(PAGE_PROPERTIES)));

        assertThat(pageProperties.get("name")).isEqualTo("mypage");
        assertThat(pageProperties.get("displayName")).isEqualTo("mypage display name");
        assertThat(pageProperties.get("description")).isNull();
    }

    @Test
    public void getPageContent_should_update_properties_in_the_zip_if_exists_and_keep_others()
            throws SBonitaException, IOException {
        // given: a zip with outdated properties
        final SPageWithContent page = new SPageWithContent();
        page.setName("mypageUpdated");
        page.setDescription("mypageUpdated description");
        page.setDisplayName("mypageUpdated display name");
        page.setId(12);

        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(
                pair("Index.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\naCustomProperty=plop\n"
                                .getBytes()));
        page.setContent(content);
        doReturn(page).when(readPersistenceService)
                .selectById(new ReadOnlySelectByIdDescriptor<>(SPageWithContent.class, 12));

        // when
        final byte[] result = pageServiceImpl.getPageContent(12);

        // then
        final Map<String, byte[]> unzip = IOUtil.unzip(result);
        assertThat(unzip.size()).isEqualTo(2);
        final Properties pageProperties = new Properties();
        pageProperties.load(new ByteArrayInputStream(unzip.get(PAGE_PROPERTIES)));

        assertThat(pageProperties.get("name")).isEqualTo("mypageUpdated");
        assertThat(pageProperties.get("displayName")).isEqualTo("mypageUpdated display name");
        assertThat(pageProperties.get("description")).isEqualTo("mypageUpdated description");
        assertThat(pageProperties.get("aCustomProperty")).isEqualTo("plop");
    }

    @Test(expected = SObjectNotFoundException.class)
    public void should_getPageContent_throw_not_found() throws SBonitaException {
        pageServiceImpl.getPageContent(12);
    }

    @Test
    public void init_should_do_nothing_if_non_editable_non_removable_already_here_and_the_same()
            throws SBonitaException {
        final SPage currentHomePage = new SPage("custompage_htmlexample_editonly", "example", "example",
                System.currentTimeMillis(), -1,
                true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentHomePage.setId(14);
        currentHomePage.setEditable(false);
        currentHomePage.setRemovable(false);
        currentHomePage.setPageHash(getHashOfContent("custompage_htmlexample_editonly"));
        doReturn(currentHomePage).when(pageServiceImpl).getPageByName("custompage_htmlexample_editonly");

        // when
        pageServiceImpl.init();
        // then
        verify(pageServiceImpl, never()).insertPage(
                argThat(sPage -> sPage.getName().equals("custompage_htmlexample_editonly")),
                any(byte[].class));
        verify(pageServiceImpl, never()).updatePage(eq(14), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, never()).updatePageContent(eq(14), any(byte[].class), anyString());
    }

    @Test
    public void deletePage() throws SBonitaException {
        final long pageId = 15;
        final SPage expected = new SPage("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);

        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));
        doReturn(expected).when(pageServiceImpl).getPage(pageId);

        pageServiceImpl.deletePage(pageId);

        verify(recorder, times(1)).recordDelete(any(DeleteRecord.class), nullable(String.class));

    }

    @Test
    public void deletePageThrowsPageNotFoundException() throws SBonitaException {
        final long pageId = 15L;
        final SPage expected = new SPage("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);

        doThrow(new SRecorderException("ouch !")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId))).thenReturn(expected);

        assertThrows(SObjectModificationException.class, () -> pageServiceImpl.deletePage(pageId));
    }

    @Test
    public void updatePageContent_should_check_zip_content() {
        assertThrows(SInvalidPageZipException.class,
                () -> pageServiceImpl.updatePageContent(15L, "aaa".getBytes(), CONTENT_NAME));
    }

    @Test
    public void addPage_should_check_zip_content() {
        // given
        final SPage sPage = new SPage("page1", 123456, 45, false, CONTENT_NAME);
        final byte[] content = "invalid content".getBytes();

        assertThrows(SInvalidPageZipException.class, () -> pageServiceImpl.addPage(sPage, content));
    }

    @Test
    public void zipTest_not_a_zip() {
        assertThrows(SInvalidPageZipException.class, () -> pageServiceImpl.readPageZip("badContent".getBytes(), false));
    }

    @Test
    public void zipTest_Bad_Content() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("aFile.txt", "hello".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=page"
                                .getBytes()));

        // when
        assertThrows("Missing Index.groovy or index.html", SInvalidPageZipMissingIndexException.class,
                () -> pageServiceImpl.readPageZip(content, false));

        // then
        // exception
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_for_apiExtension_without_apis()
            throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension"
                                .getBytes()));

        // when
        assertThrows("Missing fields in the page.properties: apiExtensions",
                SInvalidPageZipMissingAPropertyException.class, () -> pageServiceImpl.readPageZip(content, false));
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_for_api_extension_without_classFilename()
            throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi"
                                .getBytes()));

        // when
        assertThrows("Missing fields in the page.properties: myApi.classFileName",
                SInvalidPageZipMissingAPropertyException.class, () -> pageServiceImpl.readPageZip(content, false));
    }

    @Test
    public void should_throw_an_SInvalidPageZipInconsistentException_for_api_extension_with_classFilename_not_in_archive()
            throws Exception {
        // given
        final byte[] content = IOUtil.zip(Collections.singletonMap(PAGE_PROPERTIES,
                "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi\nmyApi.classFileName=MyController.groovy"
                        .getBytes()));

        // when
        assertThrows("RestAPIController MyController.groovy has not been found in archive.",
                SInvalidPageZipInconsistentException.class, () -> pageServiceImpl.readPageZip(content, false));
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_for_api_extension_with_empty_classFilename()
            throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi\nmyApi.classFileName="
                                .getBytes()));

        // when
        assertThrows("Missing fields in the page.properties: myApi.classFileName",
                SInvalidPageZipMissingAPropertyException.class, () -> pageServiceImpl.readPageZip(content, false));
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_if_apiExtensions_is_empty() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions="
                                .getBytes()));

        // when
        assertThrows("Missing fields in the page.properties: apiExtensions",
                SInvalidPageZipMissingAPropertyException.class, () -> pageServiceImpl.readPageZip(content, false));
    }

    @Test
    public void should_read_page_with_an_api_extension() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi\nmyApi.classFileName=MyController.groovy"
                                .getBytes()));

        // when
        pageServiceImpl.readPageZip(content, false);
    }

    @Test
    public void should_read_page_with_many_api_extensions() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair("org/MyOtherController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi, myOtherApi\nmyApi.classFileName=MyController.groovy\nmyOtherApi.classFileName=org/MyOtherController.groovy"
                                .getBytes()));

        // when
        pageServiceImpl.readPageZip(content, false);
    }

    @Test
    public void zipTest_Throws_exception() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("aFile.txt", "hello".getBytes()), pair(PAGE_PROPERTIES,
                "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=page"
                        .getBytes()));
        doThrow(SInvalidPageZipMissingIndexException.class).when(pageServiceImpl)
                .checkZipContainsRequiredEntries(anyMap());

        //when
        Throwable throwable = catchThrowable(() -> pageServiceImpl.readPageZip(content, false));

        //then
        assertThat(throwable)
                .isExactlyInstanceOf(SInvalidPageZipMissingIndexException.class);
    }

    @Test
    public void zipTest_Content_7_0_With_index_html_in_resources_folder() throws Exception {
        // given
        final Map<String, byte[]> zipContent = Collections.singletonMap("resources/index.html", "hello".getBytes());

        // when
        pageServiceImpl.checkZipContainsRequiredEntries(zipContent);

        // then
    }

    @Test
    public void zipTest_valid_Groovy() throws Exception {

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        // when then
        pageServiceImpl.readPageZip(content, false);

        // expected no exception

    }

    @Test
    public void zipTest_page_properties_invalid_name() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES,
                        "name=mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        assertThrows(SInvalidPageTokenException.class, () -> pageServiceImpl.readPageZip(content, false));

    }

    @Test
    public void zipTest_page_properties_no_name() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES, "displayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        assertThrows(SInvalidPageTokenException.class, () -> pageServiceImpl.readPageZip(content, false));

    }

    @Test
    public void zipTest_page_properties_invalid_display_name() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=\ndescription=mypage description\n".getBytes()));

        // when then
        assertThrows(SInvalidPageZipException.class, () -> pageServiceImpl.readPageZip(content, false));
    }

    @Test
    public void zipTest_page_properties_no_display_name() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndescription=mypage description\n".getBytes()));

        // when
        assertThrows("Missing fields in the page.properties: " + PageService.PROPERTIES_DISPLAY_NAME,
                SInvalidPageZipMissingAPropertyException.class, () -> pageServiceImpl.readPageZip(content, false));

        // then exception

    }

    @Test
    public void zipTestGroovyWithWrongName() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair("index.groovy", "content of the groovy".getBytes()),
                getPagePropertiesContentPair());

        // when then
        assertThrows(SInvalidPageZipMissingIndexException.class, () -> pageServiceImpl.readPageZip(content, false));
    }

    @Test
    public void zipTest_valid_Html() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_HTML, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=mypage final display name\ndescription=final mypage description\n"
                                .getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void zipTest_valid_Theme() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(THEME_CSS, "h1 { font-size:14pt; }".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=mypage final display name\ndescription=final mypage description\n"
                                .getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void zipTest_no_page_properties() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_HTML, "content of the groovy".getBytes()));

        // when then
        assertThrows("Missing page.properties", SInvalidPageZipMissingPropertiesException.class,
                () -> pageServiceImpl.readPageZip(content, false));

    }

    @Test
    public void checkPageContentIsValid_null() {
        assertThrows(SInvalidPageZipException.class, () -> pageServiceImpl.readPageZip(null, false));
    }

    @Test
    public void checkPageContentIsValid_badZip() {
        assertThrows(SInvalidPageZipException.class, () -> pageServiceImpl.readPageZip("not a zip".getBytes(), false));
    }

    @Test
    public void checkPageContentIsValid_validZip() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());
        // when
        pageServiceImpl.readPageZip(content, false);

        // then no exception

    }

    @Test
    public void should_redPageZip_call_the_internal_with_provided_false()
            throws SInvalidPageTokenException, SInvalidPageZipInconsistentException,
            SInvalidPageZipMissingAPropertyException, SInvalidPageZipMissingPropertiesException,
            SInvalidPageZipMissingIndexException {
        final byte[] content = { 0, 1, 2 };
        doReturn(null).when(pageServiceImpl).readPageZip(content, false);

        //when
        pageServiceImpl.readPageZip(content);

        //then
        verify(pageServiceImpl).readPageZip(content, false);
    }

    @Test
    public void should_add_page_throw_exception_when_invalid_zip() throws Exception {
        //given
        final SPage sPage = new SPage("page", 123456, 45, true, CONTENT_NAME);
        final byte[] badContent = "not_a_zip".getBytes();
        doThrow(IOException.class).when(pageServiceImpl).unzip(badContent);

        //when
        Throwable throwable = catchThrowable(() -> pageServiceImpl.addPage(sPage, badContent));

        //then
        assertThat(throwable)
                .isExactlyInstanceOf(SInvalidPageZipInconsistentException.class)
                .hasMessage("Error while reading zip file")
                .hasCauseExactlyInstanceOf(IOException.class);
    }

    @Test
    public void should_add_page_insertPage() throws Exception {
        //given
        final SPage sPage = new SPage("page", 123456, 45, true, CONTENT_NAME);
        sPage.setDisplayName("displayName1");
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        //when
        pageServiceImpl.addPage(sPage, content);

        //then
        verify(pageServiceImpl).insertPage(sPage, content);
    }

    @Test
    public void should_add_page_with_correct_fields() throws Exception {
        //given
        byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        doAnswer(invocation -> invocation.getArguments()[0]).when(pageServiceImpl)
                .insertPage(pageArgumentCaptor.capture(), any());

        Instant now = Instant.now();
        //when
        SPage insertedPage = pageServiceImpl.addPage(content, CONTENT_NAME, 45);

        //then
        long pageDate = insertedPage.getLastModificationDate();
        SPage expected = new SPage("custompage_mypage", "mypage description", "mypage display name",
                pageDate, 45, false, pageDate, 45, CONTENT_NAME);
        expected.setRemovable(true);
        expected.setEditable(true);

        assertThat(insertedPage).isEqualTo(expected);
        assertThat(insertedPage.getLastModificationDate()).isGreaterThanOrEqualTo(now.toEpochMilli());
        List<SPage> insertedPages = pageArgumentCaptor.getAllValues();
        assertThat(insertedPages).hasSize(1).containsExactly(expected);
    }

    @Test
    public void should_add_page_return_default_content_type() throws Exception {
        //given
        final byte[] content1 = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair());

        //when
        final SPage insertedPage1 = pageServiceImpl.addPage(content1, CONTENT_NAME, USER_ID);

        //then
        SPageAssert.assertThat(insertedPage1).hasContentType(SContentType.PAGE);

    }

    @Test
    public void should_rest_api_extension_record_page_mapping() throws Exception {
        //given
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair("contentType=" + SContentType.API_EXTENSION, "apiExtensions=myApi",
                        "myApi.classFileName=Index.groovy"));

        //when
        final SPage insertedPage = pageServiceImpl.addPage(content, CONTENT_NAME, USER_ID);

        //then
        SPageAssert.assertThat(insertedPage).hasContentType(SContentType.API_EXTENSION);
    }

    @Test
    public void should_read_rest_api_extension_in_compile_mode() throws Exception {
        //given
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair("contentType=" + SContentType.API_EXTENSION, "apiExtensions=myApi",
                        "myApi.className=com.company.Index"));

        //when
        final SPage insertedPage = pageServiceImpl.addPage(content, CONTENT_NAME, USER_ID);

        //then
        SPageAssert.assertThat(insertedPage).hasContentType(SContentType.API_EXTENSION);
    }

    protected Pair<String, byte[]> getPagePropertiesContentPair(final String... otherProperties)
            throws UnsupportedEncodingException {
        final StringBuilder stringBuilder = new StringBuilder()
                .append("name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\n");
        for (final String property : otherProperties) {
            stringBuilder.append(property).append("\n");
        }

        return pair(PAGE_PROPERTIES, stringBuilder.toString().getBytes("UTF-8"));
    }

    protected Pair<String, byte[]> getIndexGroovyContentPair() {
        return pair(INDEX_GROOVY, "content of the groovy".getBytes());
    }

    @Test
    public void addPage_should_execute_listener() throws Exception {
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair("contentType=" + SContentType.PAGE));

        final SPage insertedPage = pageServiceImpl.addPage(content, CONTENT_NAME, USER_ID);

        verify(apiExtensionPageServiceListener).pageInserted(insertedPage, content);
    }

    @Test
    public void updatePage_should_not_execute_listener() throws Exception {
        final SPage page = new SPage("name", 10201983L, 2005L, false, "contentName");
        when(readPersistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(page);

        pageServiceImpl.updatePage(page.getId(), entityUpdateDescriptor);

        verifyNoInteractions(apiExtensionPageServiceListener);
    }

    @Test
    public void updatePage_should_execute_listener() throws Exception {
        final SPage page = new SPage("name", 10201983L, 2005L, false, "contentName");
        page.setId(45L);
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair("contentType=" + SContentType.PAGE));
        final SPageWithContent pageContent = new SPageWithContent(page, content);

        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPageWithContent.class, page.getId())))
                .thenReturn(pageContent);

        pageServiceImpl.updatePageContent(page.getId(), content, "contentName");

        verify(apiExtensionPageServiceListener).pageUpdated(pageContent, content);
    }

    @Test
    public void updatePageContent_should_update_page_content_type() throws Exception {
        verifyPageUpdateContent(getPagePropertiesContentPair("contentType=" + SContentType.FORM), SContentType.FORM);
    }

    @Test
    public void updatePageContent_should_update_to_default_content_type() throws Exception {
        verifyPageUpdateContent(getPagePropertiesContentPair(), SContentType.PAGE);
    }

    protected void verifyPageUpdateContent(Pair<String, byte[]> pagePropertiesContentPair,
            final String expectedContentType) throws Exception {
        //given
        final SPage sPage = new SPage("name", 10201983L, 2005L, false, "contentName");
        sPage.setId(45L);
        final SPageWithContent pageContent = new SPageWithContent();
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPageWithContent.class, sPage.getId())))
                .thenReturn(pageContent);
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), pagePropertiesContentPair);

        //then
        doAnswer((Answer<Object>) invocation -> {

            final EntityUpdateDescriptor entityUpdateDescriptor = (EntityUpdateDescriptor) invocation
                    .getArguments()[0];
            assertThat(entityUpdateDescriptor.getFields()).containsOnly(
                    entry("description", "mypage description"),
                    entry("contentName", "contentName"),
                    entry("displayName", "mypage display name"),
                    entry("contentType", expectedContentType));
            return null;
        }).when(pageServiceImpl).updatePage(any(EntityUpdateDescriptor.class), any(AbstractSPage.class));

        //when
        pageServiceImpl.updatePageContent(sPage.getId(), content, "contentName");
    }

    @Test
    public void should_update_page_content_hash_of_provided_page() throws Exception {
        SPage page = new SPage("userPage", 10201983L, 2005L, false, "contentName");
        page.setId(55L);
        page.setProvided(true);

        byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair("contentType=" + SContentType.PAGE));
        SPageWithContent pageWithContent = new SPageWithContent(page, content);
        pageWithContent.setProvided(true);

        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPageWithContent.class, page.getId())))
                .thenReturn(pageWithContent);
        doReturn(pageWithContent).when(pageServiceImpl).updatePage(entityUpdateDescriptorCaptor.capture(),
                eq(pageWithContent));

        pageServiceImpl.updatePageContent(page.getId(), content, "contentName");

        assertThat(entityUpdateDescriptorCaptor.getValue().getFields())
                .contains(entry("pageHash", DigestUtils.md5DigestAsHex(content)));
    }

    @Test
    public void should_not_update_page_content_hash_of_not_provided_pages() throws Exception {
        SPage page = new SPage("userPage", 10201983L, 2005L, false, "contentName");
        page.setId(55L);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPageWithContent.class, page.getId())))
                .thenReturn(new SPageWithContent());
        doReturn(page).when(pageServiceImpl).updatePage(entityUpdateDescriptorCaptor.capture(), any());

        pageServiceImpl.updatePageContent(page.getId(), IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair("contentType=" + SContentType.PAGE)), "contentName");

        assertThat(entityUpdateDescriptorCaptor.getValue().getFields()).doesNotContainKey("pageHash");
    }

    @Test
    public void deletePage_should_execute_listener() throws Exception {
        final SPage page = new SPage("name", 10201983L, 2005L, false, "contentName");
        when(readPersistenceService.selectById(any())).thenReturn(page);

        pageServiceImpl.deletePage(1983L);

        verify(apiExtensionPageServiceListener).pageDeleted(page);
    }

    @Test
    public void deletePage_on_non_removable_page_should_raise_exception() throws Exception {
        //given
        final SPage page = new SPage("a page name", 10201983L, 2005L, false, "contentName");
        page.setRemovable(false);
        when(readPersistenceService.selectById(any())).thenReturn(page);

        //when
        String exceptionMessage = assertThrows("Not the right exception", SObjectModificationException.class,
                () -> pageServiceImpl.deletePage(1983L)).getMessage();

        //then
        assertThat(exceptionMessage)
                .contains("The page 'a page name' cannot be deleted because it is non-removable");
    }

    @Test
    public void update_non_editable_should_raise_exception_when_session_is_not_system() throws Exception {
        //given
        final SPage page = new SPage("a page name", 10201983L, 2005L, /* boolean provided: */false, "contentName");
        page.setEditable(false);
        when(readPersistenceService.selectById(any())).thenReturn(page);
        doReturn(false).when(pageServiceImpl).isSystemSession();

        //when
        String exceptionMessage = assertThrows("Not the right exception", SObjectModificationException.class,
                () -> pageServiceImpl.updatePage(page.getId(), entityUpdateDescriptor)).getMessage();

        //then
        assertThat(exceptionMessage)
                .contains("The page 'a page name' cannot be modified because it is not modifiable");
    }

    @Test
    public void update_non_editable_page_should_update_content_when_session_is_system() throws Exception {
        //given
        final SPage page = new SPage("the page", 999888777L, 2021L, /* boolean provided: */true, "contentName");
        page.setEditable(false);
        when(readPersistenceService.selectById(any())).thenReturn(page);
        doReturn(true).when(pageServiceImpl).isSystemSession();

        //when
        pageServiceImpl.updatePage(page.getId(), entityUpdateDescriptor);

        //then
        verify(recorder).recordUpdate(any(), any());
    }

    @Test
    public void add_page_should_call_permission_service() throws Exception {
        // given:
        SPage page = new SPage();
        page.setName("custompage_test");
        page.setDisplayName("My Custom Page");
        byte[] zip = zip(
                file("page.properties", "name=custompage_test\ncontentType=page"),
                file("resources/index.html", "someContent"));
        Properties properties = new Properties();
        properties.put("name", "custompage_test");
        properties.put("contentType", "page");

        // when:
        pageServiceImpl.addPage(page, zip);

        // then:
        verify(permissionService).addPermissions(page.getName(), properties);
    }

    @Test
    public void add_page_from_content_should_call_permission_service() throws Exception {
        // given:
        SPage page = new SPage();
        page.setName("custompage_test");
        page.setDisplayName("My Custom Page");
        byte[] zip = zip(
                file("page.properties", "name=custompage_test\ncontentType=page\ndisplayName=My Custom page"),
                file("resources/index.html", "someContent"));
        Properties properties = new Properties();
        properties.put("name", "custompage_test");
        properties.put("contentType", "page");
        properties.put("displayName", "My Custom page");

        // when:
        pageServiceImpl.addPage(page, zip);

        // then:
        verify(permissionService).addPermissions(page.getName(), properties);
    }

    @Test
    public void update_page_should_call_permission_service() throws Exception {
        // given:
        SPage page = new SPage();
        page.setId(174L);
        page.setName("custompage_test");
        page.setDisplayName("My Custom Page");
        SPageWithContent sPageContent = new SPageWithContent();
        byte[] zip = zip(
                file("page.properties", "name=custompage_test\ncontentType=page\ndisplayName=My Custom page"),
                file("resources/index.html", "someContent"));
        Properties properties = new Properties();
        properties.put("name", "custompage_test");
        properties.put("contentType", "page");
        properties.put("displayName", "My Custom page");

        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPageWithContent.class, 174L)))
                .thenReturn(sPageContent);

        // when:
        pageServiceImpl.updatePageContent(174L, zip, "myNewContent.zip");

        // then:
        verify(permissionService).addPermissions(page.getName(), properties);
    }
}
