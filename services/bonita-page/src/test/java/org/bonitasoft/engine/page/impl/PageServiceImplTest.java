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
 */
package org.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.assertj.core.data.MapEntry;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
import org.bonitasoft.engine.page.SPageContent;
import org.bonitasoft.engine.page.SPageLogBuilder;
import org.bonitasoft.engine.page.SPageWithContent;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadOnlySelectByIdDescriptor;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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

    public static final String BONITA_DEFAULT_THEME_ZIP_NAME = "bonita-bootstrap-default-theme.zip";
    public static final String BONITA_DEFAULT_LAYOUT_ZIP_NAME = "bonita-default-layout.zip";
    public static final String DEFAULT_THEME_NAME = "custompage_bootstrapdefaulttheme";
    public static final String DEFAULT_LAYOUT_NAME = "custompage_defaultlayout";


    @Mock
    private Recorder recorder;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private QueryOptions queryOptions;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private SPageContent sPageContent;

    @Mock
    private SPageLogBuilder pageLogBuilder;

    @Mock
    private EntityUpdateDescriptor entityUpdateDescriptor;

    @Mock
    ProfileService profileService;

    private PageServiceImpl pageServiceImpl;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    PageServiceListener apiExtensionPageServiceListener;

    @Before
    public void before() {
        pageServiceImpl = spy(new PageServiceImpl(readPersistenceService, recorder, technicalLoggerService, queriableLoggerService,
                profileService));
        doReturn(pageLogBuilder).when(pageServiceImpl).getPageLog(any(ActionType.class), anyString());
        doNothing().when(pageServiceImpl).initiateLogBuilder(anyLong(), anyInt(), any(SPersistenceLogBuilder.class), anyString());

        final List<PageServiceListener> listeners = Arrays.asList(apiExtensionPageServiceListener);
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
        when(readPersistenceService.getNumberOfEntities(SPage.class, queryOptions, null)).thenThrow(new SBonitaReadException("ouch!"));
        pageServiceImpl.getNumberOfPages(queryOptions);

        // then
        // exception;
    }

    @Test(expected = SInvalidPageTokenException.class)
    public void createPage_should_throw_exception_when_name_is_empty() throws SBonitaException, IOException {

        final long pageId = 15;
        final SPageImpl pageWithEmptyName = new SPageImpl("", 123456, 45, true, CONTENT_NAME);
        pageWithEmptyName.setDisplayName("plop");
        pageWithEmptyName.setId(pageId);
        pageServiceImpl.addPage(pageWithEmptyName, validPageContent("plop"));

    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void addPage_should_throw_exception_when_already_exist() throws Exception {

        // given
        final SPageImpl newPage = new SPageImpl(PAGE_NAME, INSTALLATION_DATE_AS_LONG, INSTALLED_BY_ID, PROVIDED_TRUE, CONTENT_NAME);
        newPage.setDisplayName("plop");
        // when
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(newPage);
        pageServiceImpl.addPage(newPage, validPageContent(PAGE_NAME));

        // then exception

    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void should_create_page_throw_exception_when_name_exists() throws Exception {

        // given
        final SPageImpl newPage = new SPageImpl(PAGE_NAME, 123456, 45, true, CONTENT_NAME);
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
        final SPageImpl newPage = new SPageImpl(PAGE_NAME, 123456, 45, false, CONTENT_NAME);
        newPage.setDisplayName("display Name");
        final byte[] validContent = validPageContent(PAGE_NAME);
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(null);
        pageServiceImpl.addPage(newPage, validContent);
        when(pageServiceImpl.getPageByName(PAGE_NAME)).thenReturn(newPage);

        // when
        final SPageImpl newProcessPage = new SPageImpl(PAGE_NAME, 123456, 45, false, CONTENT_NAME);
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
        final SPageImpl newPage = new SPageImpl(PAGE_NAME, 123456, 45, true, CONTENT_NAME);
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
                pair(PAGE_PROPERTIES, ("name=custompage_" + pageName + "\ndisplayName=mypage display name\ndescription=mypage description\n").getBytes()));
    }

    @Test
    public void getPage() throws SBonitaException {
        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
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
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
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
        assertTrue(pageByName == null);
    }

    @Test(expected = SBonitaReadException.class)
    public void getPageThrowsException() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId))).thenThrow(
                new SBonitaReadException("ouch!"));

        pageServiceImpl.getPage(pageId);
    }

    @Test
    public void start_should_import_provided_page() throws SBonitaException {
        // given
        // resource in the classpath bonita-groovy-example-page.zip
        pageServiceImpl.setProvidedPages(getProvidedPageList(Arrays.asList(
                "bonita-groovy-page-example.zip",
                "bonita-home-page.zip",
                "bonita-html-page-example.zip",
                BONITA_DEFAULT_LAYOUT_ZIP_NAME,
                BONITA_DEFAULT_THEME_ZIP_NAME)));

        doAnswer(invocation -> {
            final SPage sPage = (SPage) invocation.getArguments()[0];

            final Map<String, String> map = new HashMap<>();
            map.put(DEFAULT_LAYOUT_NAME, "layout");
            map.put("custompage_htmlexample", "page");
            map.put("custompage_groovyexample", "page");
            map.put("custompage_home", "page");
            map.put(DEFAULT_THEME_NAME, "theme");

            assertThat(map).contains(entry(sPage.getName(), sPage.getContentType()));

            return sPage;
        }).when(pageServiceImpl).insertPage(any(), any());

        // when
        pageServiceImpl.start();

        // then
        verify(pageServiceImpl, times(5)).insertPage(any(SPage.class), any(byte[].class));
        verify(pageServiceImpl, times(0)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, times(0)).updatePageContent(anyLong(), any(byte[].class), anyString());
    }

    @Test
    public void importing_provided_pages_should_respect_declared_visibility() throws SBonitaException {
        // given
        final ImportPageDescriptor importPageDescriptor1 = new ImportPageDescriptor(BONITA_DEFAULT_LAYOUT_ZIP_NAME,
                false);
        final ImportPageDescriptor importPageDescriptor2 = new ImportPageDescriptor(BONITA_DEFAULT_THEME_ZIP_NAME,
                true);
        pageServiceImpl.setProvidedPages(Arrays.asList(
                importPageDescriptor1,
                importPageDescriptor2));
        doNothing().when(pageServiceImpl).createPage(any(), any(), any());

        // when
        pageServiceImpl.start();

        // then
        verify(pageServiceImpl, times(1)).createPage(eq(importPageDescriptor1), any(byte[].class),
                any(Properties.class));
        verify(pageServiceImpl, times(1)).createPage(eq(importPageDescriptor2), any(byte[].class),
                any(Properties.class));
    }
    @Test
    public void start_should_update_provided_page_if_different() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPageImpl currentGroovyPage = new SPageImpl("custompage_groovyexample", "example", "example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentGroovyPage.setId(12);

        final SPageImpl currentHtmlPage = new SPageImpl("custompage_htmlexample", "example", "example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentHtmlPage.setId(13);

        final SPageImpl currentHomePage = new SPageImpl("custompage_home", "example", "example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentHomePage.setId(14);

        final SPageImpl currentLayoutPage = new SPageImpl(DEFAULT_LAYOUT_NAME, "example of layout", "Layout Example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentLayoutPage.setId(15);

        final SPageImpl currentThemePage = new SPageImpl(DEFAULT_THEME_NAME, "example of theme", "Theme Example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentThemePage.setId(16);

        doReturn(currentGroovyPage).when(pageServiceImpl).getPageByName("custompage_groovyexample");
        doReturn(currentHtmlPage).when(pageServiceImpl).getPageByName("custompage_htmlexample");
        doReturn(currentHomePage).when(pageServiceImpl).getPageByName("custompage_home");
        doReturn(currentLayoutPage).when(pageServiceImpl).getPageByName(DEFAULT_LAYOUT_NAME);
        doReturn(currentThemePage).when(pageServiceImpl).getPageByName(DEFAULT_THEME_NAME);
        doReturn(new byte[] { 1, 2, 3 }).when(pageServiceImpl).getPageContent(12);
        doReturn(new byte[] { 1, 2, 3 }).when(pageServiceImpl).getPageContent(13);
        doReturn(new byte[] { 1, 2, 3 }).when(pageServiceImpl).getPageContent(14);
        doReturn(new byte[] { 1, 2, 3 }).when(pageServiceImpl).getPageContent(15);
        doReturn(new byte[] { 1, 2, 3 }).when(pageServiceImpl).getPageContent(16);

        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(byte[].class), anyString());

        // when
        pageServiceImpl.setProvidedPages(getProvidedPageList(Arrays.asList(
                "bonita-groovy-page-example.zip",
                "bonita-home-page.zip",
                "bonita-html-page-example.zip",
                BONITA_DEFAULT_LAYOUT_ZIP_NAME,
                BONITA_DEFAULT_THEME_ZIP_NAME)));
        pageServiceImpl.start();

        // then
        verify(pageServiceImpl, times(0)).insertPage(any(SPage.class), any(byte[].class));
        verify(pageServiceImpl, times(1)).updatePageContent(eq(13L), any(byte[].class), eq("bonita-html-page-example.zip"));
        verify(pageServiceImpl, times(1)).updatePageContent(eq(12L), any(byte[].class), eq("bonita-groovy-page-example.zip"));
        verify(pageServiceImpl, times(1)).updatePageContent(eq(14L), any(byte[].class), eq("bonita-home-page.zip"));
        verify(pageServiceImpl, times(1)).updatePageContent(eq(15L), any(byte[].class), eq(BONITA_DEFAULT_LAYOUT_ZIP_NAME));
        verify(pageServiceImpl, times(1)).updatePageContent(eq(16L), any(byte[].class), eq(BONITA_DEFAULT_THEME_ZIP_NAME));
    }

    private List<ImportPageDescriptor> getProvidedPageList(List<String> zipNameList) {
        return zipNameList.stream().map(s -> new ImportPageDescriptor(s, false)).collect(Collectors.toList());
    }

    @Test
    public void getPageContent_should_add_properties_in_the_zip() throws SBonitaException, IOException {
        // given: a zip without properties
        final SPageWithContentImpl page = new SPageWithContentImpl();
        page.setName("mypage");
        page.setDescription("mypage description");
        page.setDisplayName("mypage display name");
        page.setId(12);
        final byte[] content = IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
        page.setContent(content);
        doReturn(page).when(readPersistenceService).selectById(new ReadOnlySelectByIdDescriptor<>(SPageWithContent.class, 12));

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
    public void getPageContent_should_add_properties_in_the_zip_with_non_mandatory_metadata() throws SBonitaException, IOException {
        final SPageWithContentImpl page = new SPageWithContentImpl();
        page.setName("mypage");
        page.setDisplayName("mypage display name");
        //no description
        page.setId(12);
        page.setContent(IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes())));
        doReturn(page).when(readPersistenceService).selectById(new ReadOnlySelectByIdDescriptor<>(SPageWithContent.class, 12));

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
    public void getPageContent_should_update_properties_in_the_zip_if_exists_and_keep_others() throws SBonitaException, IOException {
        // given: a zip with outdated properties
        final SPageWithContentImpl page = new SPageWithContentImpl();
        page.setName("mypageUpdated");
        page.setDescription("mypageUpdated description");
        page.setDisplayName("mypageUpdated display name");
        page.setId(12);

        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(
                pair("Index.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\naCustomProperty=plop\n".getBytes()));
        page.setContent(content);
        doReturn(page).when(readPersistenceService).selectById(new ReadOnlySelectByIdDescriptor<>(SPageWithContent.class, 12));

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
    public void should_getPageContent_throw_not_found() throws SBonitaException, IOException {
        pageServiceImpl.getPageContent(12);
    }

    @Test
    public void start_should_do_nothing_if_already_here_and_the_same() throws SBonitaException, IOException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPageImpl currentGroovyPage = new SPageImpl("custompage_groovyexample", "example", "example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentGroovyPage.setId(12);

        final SPageImpl currentHtmlPage = new SPageImpl("custompage_htmlexample", "example", "example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentHtmlPage.setId(13);

        final SPageImpl currentHomePage = new SPageImpl("custompage_home", "example", "example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentHomePage.setId(14);

        final SPageImpl currentLayoutPage = new SPageImpl(DEFAULT_LAYOUT_NAME, "example of layout", "Layout Example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentLayoutPage.setId(15);

        final SPageImpl currentThemePage = new SPageImpl(DEFAULT_THEME_NAME, "example of theme", "Theme Example", System.currentTimeMillis(), -1, true,
                System.currentTimeMillis(),
                -1,
                CONTENT_NAME);
        currentThemePage.setId(16);
        // when
        pageServiceImpl.start();
        // then
        verify(pageServiceImpl, times(0)).insertPage(any(SPage.class), any(byte[].class));
        verify(pageServiceImpl, times(0)).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, times(0)).updatePageContent(anyLong(), any(byte[].class), anyString());
    }

    @Test
    public void deletePage() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);

        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) {
                // Deletion OK
                return null;
            }

        }).when(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));
        doReturn(expected).when(pageServiceImpl).getPage(pageId);

        pageServiceImpl.deletePage(pageId);

        verify(recorder, times(1)).recordDelete(any(DeleteRecord.class), nullable(String.class));

    }

    @Test(expected = SObjectModificationException.class)
    public void deletePageThrowsPageNotFoundException() throws SBonitaException {

        final long pageId = 15;
        final SPage expected = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
        expected.setId(pageId);

        doThrow(new SRecorderException("ouch !")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, pageId))).thenReturn(expected);

        pageServiceImpl.deletePage(pageId);
    }

    @Test(expected = SBonitaException.class)
    public void updatePageContent_should_check_zip_content() throws Exception {
        // given
        final long pageId = 15;
        final SPage sPage = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);

        // when
        pageServiceImpl.updatePageContent(pageId, "aaa".getBytes(), CONTENT_NAME);

        // then
        // exception
    }

    @Test(expected = SObjectAlreadyExistsException.class)
    public void updatePageWithExistingName() throws Exception {

        final long pageId1 = 15;
        final long pageId2 = 20;

        final SPageImpl page1 = new SPageImpl("page1", 123456, 45, true, CONTENT_NAME);
        page1.setDisplayName("displayName1");
        final SPageImpl page2 = new SPageImpl("page2", 123456, 45, true, CONTENT_NAME);
        page2.setDisplayName("displayName2");

        final byte[] content = IOUtil.zip(Collections.singletonMap("Index.groovy", "content of the groovy".getBytes()));
        final Map<String, Object> fields = new HashMap<>();

        when(pageServiceImpl.getPageByName(page1.getName())).thenReturn(page1);

        // given
        pageServiceImpl.addPage(page1, content);
        page1.setId(pageId1);

        pageServiceImpl.addPage(page2, content);
        page2.setId(pageId2);

        // when

        // try to update page2 with page1 name
        fields.put(SPageFields.PAGE_NAME, page1.getName());
        fields.put(SPageFields.PAGE_ID, page1.getId());

        pageServiceImpl.updatePage(page2.getId(), entityUpdateDescriptor);

        // then
        // exception

    }

    @Test(expected = SBonitaException.class)
    public void addPage_should_check_zip_content() throws Exception {

        // given
        final long pageId = 15;
        final Map<String, Object> fields = new HashMap<>();
        final SPage sPage = new SPageImpl("page1", 123456, 45, false, CONTENT_NAME);
        sPage.setId(pageId);
        final byte[] content = "invalid content".getBytes();
        fields.put(SPageContentFields.PAGE_CONTENT, content);

        // when
        pageServiceImpl.addPage(sPage, content);

        // then
        // exception

    }

    @Test
    public void zipTest_not_a_zip() throws Exception {
        exception.expect(SInvalidPageZipException.class);
        // given
        final byte[] content = "badContent".getBytes();

        // when
        pageServiceImpl.readPageZip(content, false);

        // then exception
    }

    @Test
    public void zipTest_Bad_Content() throws Exception {
        exception.expect(SInvalidPageZipMissingIndexException.class);
        exception.expectMessage("Missing Index.groovy or index.html");

        // given
        final byte[] content = IOUtil.zip(pair("aFile.txt", "hello".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=page".getBytes()));

        // when
        pageServiceImpl.readPageZip(content, false);

        // then
        // exception
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_for_apiExtension_without_apis() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension".getBytes()));

        exception.expect(SInvalidPageZipMissingAPropertyException.class);
        exception.expectMessage("Missing fields in the page.properties: apiExtensions");

        // when
        pageServiceImpl.readPageZip(content, false);
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_for_api_extension_without_classFilename() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi"
                                .getBytes()));

        exception.expect(SInvalidPageZipMissingAPropertyException.class);
        exception.expectMessage("Missing fields in the page.properties: myApi.classFileName");

        // when
        pageServiceImpl.readPageZip(content, false);
    }

    @Test
    public void should_throw_an_SInvalidPageZipInconsistentException_for_api_extension_with_classFilename_not_in_archive() throws Exception {
        // given
        final byte[] content = IOUtil.zip(Collections.singletonMap(PAGE_PROPERTIES,
                "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi\nmyApi.classFileName=MyController.groovy"
                        .getBytes()));

        exception.expect(SInvalidPageZipInconsistentException.class);
        exception.expectMessage("RestAPIController MyController.groovy has not been found in archive.");

        // when
        pageServiceImpl.readPageZip(content, false);
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_for_api_extension_with_empty_classFilename() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions=myApi\nmyApi.classFileName="
                                .getBytes()));

        exception.expect(SInvalidPageZipMissingAPropertyException.class);
        exception.expectMessage("Missing fields in the page.properties: myApi.classFileName");

        // when
        pageServiceImpl.readPageZip(content, false);
    }

    @Test
    public void should_throw_an_SInvalidPageZipMissingAPropertyException_if_apiExtensions_is_empty() throws Exception {
        // given
        final byte[] content = IOUtil.zip(pair("MyController.groovy", "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES,
                        "name=custompage_mypage\ndisplayName=My Page\ndescription=mypage description\n\ncontentType=apiExtension\napiExtensions="
                                .getBytes()));

        exception.expect(SInvalidPageZipMissingAPropertyException.class);
        exception.expectMessage("Missing fields in the page.properties: apiExtensions");

        // when
        pageServiceImpl.readPageZip(content, false);
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
        doThrow(SInvalidPageZipMissingIndexException.class).when(pageServiceImpl).checkZipContainsRequiredEntries(anyMap());

        //when
        Throwable throwable = catchThrowable(() -> pageServiceImpl.readPageZip(content, false));

        //then
        assertThat(throwable)
                .isExactlyInstanceOf(SInvalidPageZipMissingIndexException.class)
        ;
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
        exception.expect(SInvalidPageTokenException.class);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES, "name=mypage\ndisplayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void zipTest_page_properties_no_name() throws Exception {
        exception.expect(SInvalidPageTokenException.class);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES, "displayName=mypage display name\ndescription=mypage description\n".getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void zipTest_page_properties_invalid_display_name() throws Exception {
        exception.expect(SInvalidPageZipException.class);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=\ndescription=mypage description\n".getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void zipTest_page_properties_no_display_name() throws Exception {
        exception.expect(SInvalidPageZipMissingAPropertyException.class);
        exception.expectMessage("Missing fields in the page.properties: " + PageService.PROPERTIES_DISPLAY_NAME);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndescription=mypage description\n".getBytes()));

        // when
        pageServiceImpl.readPageZip(content, false);

        // then exception

    }

    @Test
    public void zipTestGroovyWithWrongName() throws Exception {
        exception.expect(SInvalidPageZipMissingIndexException.class);

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair("index.groovy", "content of the groovy".getBytes()),
                getPagePropertiesContentPair());

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void zipTest_valid_Html() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_HTML, "content of the groovy".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage final display name\ndescription=final mypage description\n".getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }
    
    @Test
    public void zipTest_valid_Theme() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(THEME_CSS, "h1 { font-size:14pt; }".getBytes()),
                pair(PAGE_PROPERTIES, "name=custompage_mypage\ndisplayName=mypage final display name\ndescription=final mypage description\n".getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void zipTest_no_page_properties() throws Exception {
        exception.expect(SInvalidPageZipMissingPropertiesException.class);
        exception.expectMessage("Missing page.properties");

        // given
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(pair(INDEX_HTML, "content of the groovy".getBytes()));

        // when then
        pageServiceImpl.readPageZip(content, false);

    }

    @Test
    public void checkPageContentIsValid_null() throws Exception {
        exception.expect(SInvalidPageZipException.class);
        // given

        // when
        pageServiceImpl.readPageZip(null, false);

        // then

    }

    @Test
    public void checkPageContentIsValid_badZip() throws Exception {
        exception.expect(SInvalidPageZipException.class);
        // given

        // when
        pageServiceImpl.readPageZip("not a zip".getBytes(), false);

        // then

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
    public void should_redPageZip_call_the_internal_with_provided_false() throws SInvalidPageTokenException, SInvalidPageZipInconsistentException,
            SInvalidPageZipMissingAPropertyException, SInvalidPageZipMissingPropertiesException, SInvalidPageZipMissingIndexException {
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
        final SPage sPage = new SPageImpl("page", 123456, 45, true, CONTENT_NAME);
        final byte[] badContent = "not_a_zip".getBytes();
        doThrow(IOException.class).when(pageServiceImpl).unzip(badContent);

        //when
        Throwable throwable = catchThrowable(() -> pageServiceImpl.addPage(sPage, badContent));

        //then
        assertThat(throwable)
                .isExactlyInstanceOf(SInvalidPageZipInconsistentException.class)
                .hasMessage("Error while reading zip file")
                .hasCauseExactlyInstanceOf(IOException.class)
        ;
    }

    @Test
    public void should_add_page_insertPage() throws Exception {
        //given
        final SPageImpl sPage = new SPageImpl("page", 123456, 45, true, CONTENT_NAME);
        sPage.setDisplayName("displayName1");
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        //when
        pageServiceImpl.addPage(sPage, content);

        //then
        verify(pageServiceImpl).insertPage(sPage, content);
    }

    @Test
    public void add_page_with_content_should_not_add_provided_page() throws Exception {
        //given
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(),
                getPagePropertiesContentPair());

        //when
        pageServiceImpl.addPage(content, CONTENT_NAME, USER_ID);

        //then
        final SPage sPage;
        verify(pageServiceImpl).insertPage(any(SPage.class), eq(content));
    }

    @Test
    public void should_add_page_return_provided_content_type() throws Exception {
        //given
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair("contentType=" + SContentType.PAGE));

        //when
        final SPage insertedPage = pageServiceImpl.addPage(content, CONTENT_NAME, USER_ID);

        //then
        SPageAssert.assertThat(insertedPage).hasContentType(SContentType.PAGE);

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
                getPagePropertiesContentPair("contentType=" + SContentType.API_EXTENSION, "apiExtensions=myApi", "myApi.classFileName=Index.groovy"));

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
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair("contentType=" + SContentType.PAGE));

        final SPage insertedPage = pageServiceImpl.addPage(content, CONTENT_NAME, USER_ID);

        verify(apiExtensionPageServiceListener).pageInserted(insertedPage, content);
    }

    @Test
    public void updatePage_should_not_execute_listener() throws Exception {
        final SPageImpl page = new SPageImpl("name", 10201983L, 2005L, false, "contentName");
        when(readPersistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(page);
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair("contentType=" + SContentType.API_EXTENSION));

        pageServiceImpl.updatePage(page.getId(), entityUpdateDescriptor);

        verifyZeroInteractions(apiExtensionPageServiceListener);
    }

    @Test
    public void updatePage_should_execute_listener() throws Exception {
        final SPageImpl page = new SPageImpl("name", 10201983L, 2005L, false, "contentName");
        page.setId(45L);
        final SPageContent pageContent = new SPageContentImpl();
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPageContent.class, page.getId()))).thenReturn(pageContent);
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPage.class, page.getId()))).thenReturn(page);
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair("contentType=" + SContentType.PAGE));

        pageServiceImpl.updatePageContent(page.getId(), content, "contentName");

        verify(apiExtensionPageServiceListener).pageUpdated(page, content);
    }

    @Test
    public void updatePageContent_should_update_page_content_type() throws Exception {
        verifyPageUpdateContent(getPagePropertiesContentPair("contentType=" + SContentType.FORM), SContentType.FORM);
    }

    @Test
    public void updatePageContent_should_update_to_default_content_type() throws Exception {
        verifyPageUpdateContent(getPagePropertiesContentPair(), SContentType.PAGE);
    }

    protected void verifyPageUpdateContent(Pair<String, byte[]> pagePropertiesContentPair, final String expectedContentType) throws Exception {
        //given
        final SPageImpl sPage = new SPageImpl("name", 10201983L, 2005L, false, "contentName");
        sPage.setId(45L);
        final SPageContent pageContent = new SPageContentImpl();
        when(readPersistenceService.selectById(new SelectByIdDescriptor<>(SPageContent.class, sPage.getId()))).thenReturn(pageContent);
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), pagePropertiesContentPair);

        //then
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {

                final EntityUpdateDescriptor entityUpdateDescriptor = (EntityUpdateDescriptor) invocation.getArguments()[1];
                assertThat(entityUpdateDescriptor.getFields()).contains(expectedUpdateFields()).contains(entry("contentType", expectedContentType));
                return null;
            }
        }).when(pageServiceImpl).updatePage(anyLong(), any(EntityUpdateDescriptor.class));

        //when
        pageServiceImpl.updatePageContent(sPage.getId(), content, "contentName");
    }

    private MapEntry[] expectedUpdateFields() {
        return new MapEntry[] { entry("description", "mypage description"), entry("name", "custompage_mypage"),
                entry("contentName", "contentName"), entry("displayName", "mypage display name") };
    }

    @Test
    public void deletePage_should_execute_listener() throws Exception {
        final SPageImpl page = new SPageImpl("name", 10201983L, 2005L, false, "contentName");
        when(readPersistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(page);

        pageServiceImpl.deletePage(1983L);

        verify(apiExtensionPageServiceListener).pageDeleted(page);
    }

}
