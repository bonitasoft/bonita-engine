/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application.importer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.authorization.PermissionService;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationConverter;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.PageServiceListener;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.impl.PageServiceImpl;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.DigestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DefaultLivingApplicationImporterTest {

    private static final String DEFAULT_THEME_NAME = "custompage_themeBonita";
    private static final String DEFAULT_LAYOUT_NAME = "custompage_defaultlayout";
    private static final String HTML_EXAMPLE_PAGE_NAME = "custompage_htmlexample";
    private static final String GROOVY_EXAMPLE_PAGE_NAME = "custompage_groovyexample";
    private static final String HOME_PAGE_NAME = "custompage_home";
    private static final String TENANT_STATUS_BONITA_PAGE_NAME = "custompage_tenantStatusBonita";
    private static final String MY_PAGE_NAME = "custompage_mypage";
    private static final String INDEX_GROOVY = "Index.groovy";
    private static final String PAGE_PROPERTIES = "page.properties";
    private static final String DEFAULT_APP_3_TOKEN = "default_app_3";

    private static final Map<String, String> TEST_PAGES = Map.of(
            DEFAULT_LAYOUT_NAME, "/org/bonitasoft/web/page/bonita-default-layout.zip",
            HTML_EXAMPLE_PAGE_NAME, "/org/bonitasoft/web/page/bonita-html-page-example.zip",
            GROOVY_EXAMPLE_PAGE_NAME, "/org/bonitasoft/web/page/bonita-groovy-page-example.zip",
            HOME_PAGE_NAME, "/org/bonitasoft/web/page/bonita-home-page.zip",
            TENANT_STATUS_BONITA_PAGE_NAME, "/org/bonitasoft/web/page/page-tenant-status.zip",
            DEFAULT_THEME_NAME, "/org/bonitasoft/web/page/bonita-theme.zip");

    @Mock
    private ReadPersistenceService readPersistenceService;
    @Mock
    private Recorder recorder;
    @Mock
    private QueriableLoggerService queriableLoggerService;
    @Mock
    private ReadSessionAccessor sessionAccessor;
    @Mock
    private SessionService sessionService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private PageServiceListener apiExtensionPageServiceListener;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private NodeToApplicationConverter nodeToApplicationConverter;
    @Mock
    private ApplicationPageImporter applicationPageImporter;
    @Mock
    private ApplicationMenuImporter applicationMenuImporter;

    @Captor
    private ArgumentCaptor<SPage> pageArgumentCaptor;

    private PageServiceImpl pageServiceImpl;
    private ApplicationImporter applicationImporter;
    private DefaultLivingApplicationImporter defaultLivingApplicationImporter;

    @Before
    public void before() {
        pageServiceImpl = spy(
                new PageServiceImpl(readPersistenceService, recorder, queriableLoggerService, sessionAccessor,
                        sessionService, permissionService));
        pageServiceImpl.setPageServiceListeners(Collections.singletonList(apiExtensionPageServiceListener));

        applicationImporter = spy(
                new ApplicationImporter(applicationService, nodeToApplicationConverter, applicationPageImporter,
                        applicationMenuImporter));

        defaultLivingApplicationImporter = spy(
                new DefaultLivingApplicationImporter(pageServiceImpl, applicationImporter));
    }

    @Test
    public void init_should_import_all_editable_provided_pages_if_is_the_first_run() throws SBonitaException {
        // given
        // resource in the classpath bonita-groovy-example-page.zip
        final Map<String, String> map = new HashMap<>();
        map.put(DEFAULT_LAYOUT_NAME, ContentType.LAYOUT);
        map.put(HTML_EXAMPLE_PAGE_NAME, ContentType.PAGE);
        map.put(GROOVY_EXAMPLE_PAGE_NAME, ContentType.PAGE);
        map.put(HOME_PAGE_NAME, ContentType.PAGE);
        map.put(TENANT_STATUS_BONITA_PAGE_NAME, ContentType.PAGE);
        map.put(DEFAULT_THEME_NAME, ContentType.THEME);
        doAnswer(invocation -> invocation.getArguments()[0]).when(pageServiceImpl)
                .insertPage(pageArgumentCaptor.capture(), any());

        // to simulate a first run:
        defaultLivingApplicationImporter.setAddRemovablePagesIfMissing(true);

        // when
        defaultLivingApplicationImporter.execute();

        // then
        List<SPage> insertedPages = pageArgumentCaptor.getAllValues();
        assertThat(insertedPages)
                .hasSize(6)
                .allSatisfy(insertedPage -> {
                    String name = insertedPage.getName();
                    assertThat(name).isIn(map.keySet());
                    assertThat(insertedPage.getContentType()).isEqualTo(map.get(name));
                    assertThat(insertedPage.getPageHash()).isEqualTo(getHashOfContent(name));
                    assertThat(insertedPage.isProvided()).isTrue();
                    assertThat(insertedPage.isEditable()).isTrue();
                    assertThat(insertedPage.isRemovable()).isTrue();
                    assertThat(insertedPage.getContentType()).isEqualTo(map.get(name));
                    assertThat(insertedPage.getPageHash()).isEqualTo(getHashOfContent(name));
                });

        verify(pageServiceImpl, never()).updatePage(anyLong(), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), anyString());
    }

    @Test
    public void init_should_not_insert_not_removable_provided_page_if_is_not_first_run() throws Exception {
        // given
        final Map<String, String> removablePage = new HashMap<>();
        removablePage.put(DEFAULT_LAYOUT_NAME, ContentType.LAYOUT);
        removablePage.put(HTML_EXAMPLE_PAGE_NAME, ContentType.PAGE);
        removablePage.put(GROOVY_EXAMPLE_PAGE_NAME, ContentType.PAGE);
        removablePage.put(HOME_PAGE_NAME, ContentType.PAGE);
        removablePage.put(TENANT_STATUS_BONITA_PAGE_NAME, ContentType.PAGE);
        removablePage.put(DEFAULT_THEME_NAME, ContentType.THEME);

        // to simulate a subsequent run:
        defaultLivingApplicationImporter.setAddRemovablePagesIfMissing(false);

        // when
        defaultLivingApplicationImporter.execute();

        // then
        verify(defaultLivingApplicationImporter, times(6)).importProvidedPage(any(), any(byte[].class), eq(true),
                eq(true), eq(false));
        verify(pageServiceImpl, never()).insertPage(argThat(page -> !removablePage.containsKey(page.getName())),
                any(byte[].class));
    }

    @Test
    public void import_provided_page_should_return_import_status_added_if_page_is_missing_equal_true()
            throws Exception {
        // given: a zip without properties
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair());

        doReturn(null).when(pageServiceImpl).getPageByName(MY_PAGE_NAME);

        ImportStatus importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, true,
                true, true);

        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
    }

    @Test
    public void should_return_skipped_when_page_does_not_exist_and_addIfMissing_equals_false() throws Exception {
        // given: a zip without properties
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair());

        doReturn(null).when(pageServiceImpl).getPageByName(MY_PAGE_NAME);

        ImportStatus importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, true,
                true, false);

        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);
        verify(pageServiceImpl, never()).insertPage(any(), any(byte[].class));
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
    }

    @Test
    public void should_replaced_an_existing_provided_page_when_content_is_different_and_still_provided()
            throws Exception {

        final SPage pageInDb = new SPage();
        pageInDb.setName(MY_PAGE_NAME);
        pageInDb.setDescription("mypage description");
        pageInDb.setDisplayName("mypage display name");
        pageInDb.setId(12);
        pageInDb.setProvided(true);
        pageInDb.setPageHash(DigestUtils.md5DigestAsHex("some hash".getBytes(UTF_8)));

        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(byte[].class), anyString());

        doReturn(pageInDb).when(pageServiceImpl).getPageByName(MY_PAGE_NAME);

        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair());

        // final page
        ImportStatus importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, false,
                false, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, false, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, true, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.REPLACED);

        verify(pageServiceImpl, times(3)).updatePageContent(eq(12L), eq(content), any());
    }

    @Test
    public void should_not_replaced_an_existing_page_when_content_is_different_and_no_more_provided()
            throws Exception {

        final SPage page = new SPage();
        page.setName(MY_PAGE_NAME);
        page.setDescription("mypage description");
        page.setDisplayName("mypage display name");
        page.setId(12);
        page.setProvided(false);

        page.setPageHash(DigestUtils.md5DigestAsHex("some hash".getBytes(UTF_8)));

        doReturn(page).when(pageServiceImpl).getPageByName(MY_PAGE_NAME);

        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair());

        // final page
        ImportStatus importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, false,
                false, true);

        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);

        // edit only page
        importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, false, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);

        // removable page
        importStatus = defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, true, true, true);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);

        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
        verify(pageServiceImpl, never()).insertPage(any(), any(byte[].class));
    }

    @Test
    public void import_provided_page_should_do_nothing_and_throw_exception_when_exception_happened_on_get_page_by_name()
            throws Exception {

        final SPage page = new SPage();
        page.setName(MY_PAGE_NAME);
        page.setDescription("mypage description");
        page.setDisplayName("mypage display name");
        page.setId(12);
        page.setEditable(true);
        page.setRemovable(true);
        @SuppressWarnings("unchecked")
        final byte[] content = IOUtil.zip(getIndexGroovyContentPair(), getPagePropertiesContentPair());
        page.setPageHash(DigestUtils.md5DigestAsHex(content));

        doThrow(SBonitaReadException.class).when(pageServiceImpl).getPageByName(MY_PAGE_NAME);

        Throwable expected = catchThrowable(
                () -> defaultLivingApplicationImporter.importProvidedPage(MY_PAGE_NAME, content, true, true, true));

        assertThat(expected).isInstanceOf(SBonitaException.class);
        verify(pageServiceImpl, never()).updatePageContent(anyLong(), any(byte[].class), any());
        verify(pageServiceImpl, never()).insertPage(any(), any(byte[].class));
    }

    private String getHashOfContent(String name) {
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream(TEST_PAGES.get(name))) {
            if (resourceAsStream == null) {
                throw new AssertionError(
                        "No content found for page " + name + " in classpath: " + TEST_PAGES.get(name));
            }
            return DigestUtils.md5DigestAsHex(resourceAsStream.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, byte[]> getIndexGroovyContentPair() {
        return Pair.pair(INDEX_GROOVY, "content of the groovy".getBytes());
    }

    private Pair<String, byte[]> getPagePropertiesContentPair(final String... otherProperties) {
        final StringBuilder stringBuilder = new StringBuilder()
                .append("name=custompage_mypage\ndisplayName=mypage display name\ndescription=mypage description\n");
        for (final String property : otherProperties) {
            stringBuilder.append(property).append("\n");
        }

        return Pair.pair(PAGE_PROPERTIES, stringBuilder.toString().getBytes(UTF_8));
    }

    @Test
    public void should_import_editable_default_applications_on_first_run() throws Exception {
        //given
        SApplicationWithIcon editableApp = new SApplicationWithIcon();
        editableApp.setId(3);
        editableApp.setToken(DEFAULT_APP_3_TOKEN);

        // to simulate a first run:
        defaultLivingApplicationImporter.setAddEditableApplicationsIfMissing(true);

        //when
        defaultLivingApplicationImporter.execute();

        //then
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(editableApp.getToken())),
                eq(true), anyLong(), any(byte[].class), any(), eq(true), any());
    }

    @Test
    public void should_not_import_editable_default_applications_if_not_first_run() throws Exception {
        //given
        SApplicationWithIcon editableApp = new SApplicationWithIcon();
        editableApp.setId(3);
        editableApp.setToken(DEFAULT_APP_3_TOKEN);

        // to simulate a subsequent run:
        defaultLivingApplicationImporter.setAddEditableApplicationsIfMissing(false);

        //when
        defaultLivingApplicationImporter.execute();

        //then
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(editableApp.getToken())),
                eq(true), anyLong(), any(byte[].class), any(), eq(false), any());
    }
}
