/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodeToApplicationConverterExtTest {

    @Mock
    private PageService pageService;

    @Mock
    private ApplicationImportValidator validator;

    @InjectMocks
    private NodeToApplicationConverterExt converter;

    @Mock
    private SPage defaultTheme;

    @Mock
    private SPage defaultLayout;

    private static final long DEFAULT_LAYOUT_ID = 100L;
    private static final long DEFAULT_THEME_ID = 200L;

    @Before
    public void setUp() throws Exception {
        given(defaultLayout.getName()).willReturn(ApplicationService.DEFAULT_LAYOUT_NAME);
        given(defaultLayout.getId()).willReturn(DEFAULT_LAYOUT_ID);

        given(defaultTheme.getName()).willReturn(ApplicationService.DEFAULT_THEME_NAME);
        given(defaultTheme.getId()).willReturn(DEFAULT_THEME_ID);

        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(defaultTheme);
        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(defaultLayout);
    }

    @Test
    public void toSApplication_should_use_layout_defined_in_ApplicationNode() throws Exception {
        //given
        String layoutName = "custompage_mainLayout";
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");
        node.setLayout(layoutName);

        long layoutId = 15L;
        SPage layout = buildMockPage(layoutId);
        given(pageService.getPageByName(layoutName)).willReturn(layout);

        //when
        long createdBy = 1L;
        final ImportResult importResult = converter.toSApplication(node, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplication application = importResult.getApplication();
        assertThat(application.getLayoutId()).isEqualTo(layoutId);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    private SPage buildMockPage(final long layoutId) {
        SPage layout = mock(SPage.class);
        given(layout.getId()).willReturn(layoutId);
        return layout;
    }

    @Test
    public void toSApplication_should_use_default_layout_when_layout_is_not_defined_in_ApplicationNode() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");

        //when
        long createdBy = 1L;
        final ImportResult importResult = converter.toSApplication(node, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplication application = importResult.getApplication();
        assertThat(application.getLayoutId()).isEqualTo(DEFAULT_LAYOUT_ID);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    @Test
    public void toSApplication_should_return_Import_result_with_errors_when_layout_is_not_found() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setLayout("notAvailableLayout");
        node.setToken("app");

        given(pageService.getPageByName("notAvailableLayout")).willReturn(null);

        //when
        final ImportResult importResult = converter.toSApplication(node, 1L);

        //then
        assertThat(importResult.getApplication().getLayoutId()).isNull();

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).containsExactly(new ImportError("notAvailableLayout", ImportError.Type.PAGE));
    }

    @Test
    public void toSApplication_should_use_theme_defined_in_ApplicationNode() throws Exception {
        //given
        String themeName = "custompage_mainTheme";
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");
        node.setTheme(themeName);

        long themeId = 15L;
        SPage theme = buildMockPage(themeId);
        given(pageService.getPageByName(themeName)).willReturn(theme);

        //when
        long createdBy = 1L;
        final ImportResult importResult = converter.toSApplication(node, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplication application = importResult.getApplication();
        assertThat(application.getThemeId()).isEqualTo(themeId);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    @Test
    public void toSApplication_should_use_default_theme_when_layout_is_not_defined_in_ApplicationNode() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");

        long createdBy = 1L;
        final ImportResult importResult = converter.toSApplication(node, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplication application = importResult.getApplication();
        assertThat(application.getThemeId()).isEqualTo(DEFAULT_THEME_ID);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    @Test
    public void toSApplication_should_return_Import_result_with_errors_when_theme_is_not_found() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setTheme("notAvailable");
        node.setToken("app");

        given(pageService.getPageByName("notAvailable")).willReturn(null);

        //when
        final ImportResult importResult = converter.toSApplication(node, 1L);

        //then
        assertThat(importResult.getApplication().getThemeId()).isNull();

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).containsExactly(new ImportError("notAvailable", ImportError.Type.PAGE));
    }

}