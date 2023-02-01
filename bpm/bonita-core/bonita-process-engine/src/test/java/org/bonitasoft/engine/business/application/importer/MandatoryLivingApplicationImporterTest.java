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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.ImportException;
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
public class MandatoryLivingApplicationImporterTest {

    private static final String CONTENT_NAME = "content.zip";
    private static final String HTML_EXAMPLE_EDITONLY_PAGE_NAME = "custompage_htmlexample_editonly";
    private static final String HTML_EXAMPLE_EDITONLY_ZIP_NAME = "bonita-html-page-example-editonly.zip";
    private static final String HTML_EXAMPLE_FINAL_PAGE_NAME = "custompage_htmlexample_final";
    private static final String HTML_EXAMPLE_FINAL_ZIP_NAME = "bonita-html-page-example-final.zip";
    private static final String DEFAULT_APP_1_TOKEN = "default_app_1";
    private static final String DEFAULT_APP_2_TOKEN = "default_app_2";

    private static final Map<String, String> TEST_PAGES = Map.of(
            HTML_EXAMPLE_EDITONLY_PAGE_NAME, "/org/bonitasoft/web/page/editonly/" + HTML_EXAMPLE_EDITONLY_ZIP_NAME,
            HTML_EXAMPLE_FINAL_PAGE_NAME, "/org/bonitasoft/web/page/final/" + HTML_EXAMPLE_FINAL_ZIP_NAME);

    @Mock
    private Recorder recorder;
    @Mock
    private ReadPersistenceService readPersistenceService;
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
    private MandatoryLivingApplicationImporter mandatoryLivingApplicationImporter;
    @Mock
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

        mandatoryLivingApplicationImporter = spy(
                new MandatoryLivingApplicationImporter(pageServiceImpl, applicationImporter,
                        defaultLivingApplicationImporter));
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

    @Test
    public void init_should_import_all_mandatory_provided_pages() throws SBonitaException {
        // given
        final Map<String, String> map = new HashMap<>();
        map.put(HTML_EXAMPLE_EDITONLY_PAGE_NAME, ContentType.PAGE);
        map.put(HTML_EXAMPLE_FINAL_PAGE_NAME, ContentType.PAGE);
        doAnswer(invocation -> invocation.getArguments()[0]).when(pageServiceImpl)
                .insertPage(pageArgumentCaptor.capture(), any());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        List<SPage> insertedPages = pageArgumentCaptor.getAllValues();
        assertThat(insertedPages)
                .hasSize(2)
                .allSatisfy(insertedPage -> {
                    String name = insertedPage.getName();
                    assertThat(name).isIn(map.keySet());
                    assertThat(insertedPage.getContentType()).isEqualTo(map.get(name));
                    assertThat(insertedPage.getPageHash()).isEqualTo(getHashOfContent(name));
                    if (name.equals(HTML_EXAMPLE_EDITONLY_PAGE_NAME)) {
                        assertThat(insertedPage.isEditable()).isTrue();
                        assertThat(insertedPage.isProvided()).isTrue();
                        assertThat(insertedPage.isRemovable()).isFalse();
                    } else if (name.equals(HTML_EXAMPLE_FINAL_PAGE_NAME)) {
                        assertThat(insertedPage.isEditable()).isFalse();
                        assertThat(insertedPage.isProvided()).isTrue();
                        assertThat(insertedPage.isRemovable()).isFalse();
                    } else {
                        fail("Unknown page name: {}", name);
                    }
                });

        verify(defaultLivingApplicationImporter).setAddRemovablePagesIfMissing(eq(true));
    }

    @Test
    public void init_should_not_add_removable_pages_if_missing() throws SBonitaException {
        // given
        ImportStatus importStatus = new ImportStatus(HTML_EXAMPLE_FINAL_PAGE_NAME);
        importStatus.setStatus(ImportStatus.Status.SKIPPED);
        doReturn(importStatus).when(mandatoryLivingApplicationImporter).importProvidedPage(
                eq(HTML_EXAMPLE_FINAL_ZIP_NAME),
                any(byte[].class), eq(false), eq(false), eq(true));
        doReturn(importStatus).when(mandatoryLivingApplicationImporter).importProvidedPage(
                eq(HTML_EXAMPLE_EDITONLY_ZIP_NAME),
                any(byte[].class), eq(false), eq(true), eq(true));

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(defaultLivingApplicationImporter).setAddRemovablePagesIfMissing(eq(false));
    }

    @Test
    public void init_should_not_insert_anything_if_exception_happened_on_import_final_page() throws Exception {
        // given
        final Map<String, String> notImportedPages = new HashMap<>();
        notImportedPages.put(HTML_EXAMPLE_EDITONLY_ZIP_NAME, ContentType.THEME);

        doThrow(new SBonitaReadException("Mock exception")).when(mandatoryLivingApplicationImporter)
                .importProvidedPage(eq(HTML_EXAMPLE_FINAL_ZIP_NAME), any(byte[].class), eq(false), eq(false),
                        eq(true));

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(mandatoryLivingApplicationImporter, never()).importProvidedPage(
                argThat(notImportedPages::containsKey),
                any(byte[].class), eq(false), eq(true), anyBoolean());
        verify(defaultLivingApplicationImporter, never()).setAddRemovablePagesIfMissing(eq(true));
    }

    @Test
    public void init_should_update_final_page_if_is_different() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPage currentGroovyPage = new SPage(HTML_EXAMPLE_FINAL_PAGE_NAME, "example", "example",
                System.currentTimeMillis(), SessionService.SYSTEM_ID, true, System.currentTimeMillis(),
                SessionService.SYSTEM_ID, CONTENT_NAME);
        currentGroovyPage.setId(12L);
        currentGroovyPage.setRemovable(false);
        currentGroovyPage.setEditable(false);

        doReturn(currentGroovyPage).when(pageServiceImpl).getPageByName(HTML_EXAMPLE_FINAL_PAGE_NAME);
        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(byte[].class), anyString());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(pageServiceImpl).updatePageContent(eq(12L), any(byte[].class),
                eq(HTML_EXAMPLE_FINAL_ZIP_NAME));
    }

    @Test
    public void init_should_update_edit_only_page_if_is_different() throws SBonitaException {
        // given
        // resource in the classpath provided-page.properties and provided-page.zip
        final SPage currentGroovyPage = new SPage(HTML_EXAMPLE_EDITONLY_PAGE_NAME, "example", "example",
                System.currentTimeMillis(), SessionService.SYSTEM_ID, true, System.currentTimeMillis(),
                SessionService.SYSTEM_ID, CONTENT_NAME);
        currentGroovyPage.setId(12L);
        currentGroovyPage.setRemovable(false);
        currentGroovyPage.setEditable(true);

        doReturn(currentGroovyPage).when(pageServiceImpl).getPageByName(HTML_EXAMPLE_EDITONLY_PAGE_NAME);
        doNothing().when(pageServiceImpl).updatePageContent(anyLong(), any(byte[].class), anyString());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(pageServiceImpl).updatePageContent(eq(12L), any(byte[].class),
                eq(HTML_EXAMPLE_EDITONLY_ZIP_NAME));
    }

    @Test
    public void init_should_do_nothing_if_non_editable_non_removable_already_here_and_the_same()
            throws SBonitaException {
        final SPage currentHomePage = new SPage(HTML_EXAMPLE_EDITONLY_PAGE_NAME, "example", "example",
                System.currentTimeMillis(), SessionService.SYSTEM_ID, true, System.currentTimeMillis(),
                SessionService.SYSTEM_ID, CONTENT_NAME);
        currentHomePage.setId(14);
        currentHomePage.setEditable(false);
        currentHomePage.setRemovable(false);
        currentHomePage.setPageHash(getHashOfContent(HTML_EXAMPLE_EDITONLY_PAGE_NAME));
        doReturn(currentHomePage).when(pageServiceImpl).getPageByName(HTML_EXAMPLE_EDITONLY_PAGE_NAME);

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(pageServiceImpl, never()).insertPage(
                argThat(sPage -> sPage.getName().equals(HTML_EXAMPLE_EDITONLY_PAGE_NAME)),
                any(byte[].class));
        verify(pageServiceImpl, never()).updatePage(eq(14), any(EntityUpdateDescriptor.class));
        verify(pageServiceImpl, never()).updatePageContent(eq(14), any(byte[].class), anyString());
    }

    @Test
    public void init_should_import_all_default_applications_on_first_run() throws Exception {
        //given
        SApplicationWithIcon finalApp1 = new SApplicationWithIcon();
        finalApp1.setId(1);
        finalApp1.setToken(DEFAULT_APP_1_TOKEN);
        SApplicationWithIcon finalApp2 = new SApplicationWithIcon();
        finalApp2.setId(2);
        finalApp2.setToken(DEFAULT_APP_2_TOKEN);

        ImportStatus app1ImportStatus = new ImportStatus(finalApp1.getToken());
        app1ImportStatus.setStatus(ImportStatus.Status.ADDED);
        ImportStatus app2ImportStatus = new ImportStatus(finalApp2.getToken());
        app2ImportStatus.setStatus(ImportStatus.Status.ADDED);

        doReturn(app1ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());
        doReturn(app2ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(defaultLivingApplicationImporter).setAddEditableApplicationsIfMissing(eq(true));
    }

    @Test
    public void init_should_not_import_editable_default_applications_if_not_first_run() throws Exception {
        // given
        SApplicationWithIcon finalApp1 = new SApplicationWithIcon();
        finalApp1.setId(1);
        finalApp1.setToken(DEFAULT_APP_1_TOKEN);
        SApplicationWithIcon finalApp2 = new SApplicationWithIcon();
        finalApp2.setId(2);
        finalApp2.setToken(DEFAULT_APP_2_TOKEN);

        ImportStatus app1ImportStatus = new ImportStatus(finalApp1.getToken());
        app1ImportStatus.setStatus(ImportStatus.Status.SKIPPED);
        ImportStatus app2ImportStatus = new ImportStatus(finalApp2.getToken());
        app2ImportStatus.setStatus(ImportStatus.Status.REPLACED);

        doReturn(app1ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());
        doReturn(app2ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(defaultLivingApplicationImporter).setAddEditableApplicationsIfMissing(eq(false));
    }

    @Test
    public void init_should_not_import_editable_default_applications_if_all_final_are_skipped() throws Exception {
        // given
        SApplicationWithIcon finalApp1 = new SApplicationWithIcon();
        finalApp1.setId(1);
        finalApp1.setToken(DEFAULT_APP_1_TOKEN);
        SApplicationWithIcon finalApp2 = new SApplicationWithIcon();
        finalApp2.setId(2);
        finalApp2.setToken(DEFAULT_APP_2_TOKEN);

        ImportStatus app1ImportStatus = new ImportStatus(finalApp1.getToken());
        app1ImportStatus.setStatus(ImportStatus.Status.SKIPPED);
        ImportStatus app2ImportStatus = new ImportStatus(finalApp2.getToken());
        app2ImportStatus.setStatus(ImportStatus.Status.SKIPPED);

        doReturn(app1ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());
        doReturn(app2ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(defaultLivingApplicationImporter).setAddEditableApplicationsIfMissing(eq(false));
    }

    @Test
    public void init_should_import_editable_default_applications_if_final_apps_are_added_or_updated() throws Exception {
        // given
        SApplicationWithIcon finalApp1 = new SApplicationWithIcon();
        finalApp1.setId(1);
        finalApp1.setToken(DEFAULT_APP_1_TOKEN);
        SApplicationWithIcon finalApp2 = new SApplicationWithIcon();
        finalApp2.setId(2);
        finalApp2.setToken(DEFAULT_APP_2_TOKEN);

        ImportStatus app1ImportStatus = new ImportStatus(finalApp1.getToken());
        app1ImportStatus.setStatus(ImportStatus.Status.ADDED);
        ImportStatus app2ImportStatus = new ImportStatus(finalApp2.getToken());
        app2ImportStatus.setStatus(ImportStatus.Status.REPLACED);

        doReturn(app1ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());
        doReturn(app2ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(defaultLivingApplicationImporter).setAddEditableApplicationsIfMissing(eq(true));
    }

    @Test
    public void init_should_not_import_default_app_when_importApplication_throw_exception() throws Exception {
        // given
        SApplicationWithIcon finalApp1 = new SApplicationWithIcon();
        finalApp1.setId(1);
        finalApp1.setToken(DEFAULT_APP_1_TOKEN);
        SApplicationWithIcon finalApp2 = new SApplicationWithIcon();
        finalApp2.setId(2);
        finalApp2.setToken(DEFAULT_APP_2_TOKEN);

        ImportStatus app1ImportStatus = new ImportStatus(finalApp1.getToken());
        app1ImportStatus.setStatus(ImportStatus.Status.ADDED);

        doReturn(app1ImportStatus).when(applicationImporter)
                .importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())), eq(false), anyLong(),
                        any(byte[].class), any(), eq(true), any());
        doThrow(new ImportException("Mock exception")).when(applicationImporter).importApplication(
                argThat(node -> node.getToken().equals(finalApp2.getToken())), eq(false), anyLong(), any(byte[].class),
                any(), eq(true), any());

        // when
        mandatoryLivingApplicationImporter.init();

        // then
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp1.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(applicationImporter).importApplication(argThat(node -> node.getToken().equals(finalApp2.getToken())),
                eq(false), anyLong(), any(byte[].class), any(), eq(true), any());
        verify(defaultLivingApplicationImporter, never()).setAddEditableApplicationsIfMissing(eq(true));
    }
}
