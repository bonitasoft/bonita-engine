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
package org.bonitasoft.engine.business.application.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.business.application.InternalProfiles.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ImportResult;
import org.bonitasoft.engine.business.application.importer.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodeToApplicationConverterTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private PageService pageService;

    @Mock
    private SPage defaultLayout;

    @Mock
    private SPage defaultTheme;

    public static final long DEFAULT_LAYOUT_ID = 101;

    public static final long DEFAULT_THEME_ID = 102;

    private static String ICON_MIME_TYPE = "iconMimeType";

    private static final byte[] ICON_CONTENT = "iconContent".getBytes(StandardCharsets.UTF_8);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    ApplicationImportValidator validator;

    @InjectMocks
    private NodeToApplicationConverter converter;

    @Before
    public void setUp() throws Exception {
        given(defaultLayout.getId()).willReturn(DEFAULT_LAYOUT_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(defaultLayout);

        given(defaultTheme.getId()).willReturn(DEFAULT_THEME_ID);
        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(defaultTheme);
    }

    @Test
    public void toSApplication_should_set_internalField_when_applicable() throws ImportException, SBonitaReadException {
        //given
        final ApplicationNode node1 = new ApplicationNode();
        final ApplicationNode node2 = new ApplicationNode();
        node1.setProfile(INTERNAL_PROFILE_ALL.getProfileName());
        node2.setProfile(INTERNAL_PROFILE_SUPER_ADMIN.getProfileName());
        long createdBy = 1L;

        //when
        final ImportResult importResult1 = converter.toSApplication(node1, ICON_CONTENT, ICON_MIME_TYPE, createdBy);
        final ImportResult importResult2 = converter.toSApplication(node2, ICON_CONTENT, ICON_MIME_TYPE, createdBy);

        final SApplicationWithIcon application1 = importResult1.getApplication();
        final SApplicationWithIcon application2 = importResult2.getApplication();

        assertThat(application1.getInternalProfile()).isEqualTo(INTERNAL_PROFILE_ALL.getProfileName());
        assertThat(application2.getInternalProfile()).isEqualTo(INTERNAL_PROFILE_SUPER_ADMIN.getProfileName());
        assertThat(application1.getProfileId()).isNull();
        assertThat(application2.getProfileId()).isNull();
    }

    @Test
    public void toSApplication_should_return_ImportResult_with_no_errors_and_application_with_all_fields_except_home_page()
            throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setDisplayName("My app");
        node.setDescription("This is my app");
        node.setHomePage("home");
        node.setVersion("1.0");
        node.setToken("app");
        node.setIconPath("/icon.jpg");
        node.setProfile("admin");
        node.setState("ENABLED");

        long profileId = 8L;
        final SProfile profile = mock(SProfile.class);
        given(profile.getId()).willReturn(profileId);
        given(profileService.getProfileByName("admin")).willReturn(profile);

        //when
        long createdBy = 1L;
        final ImportResult importResult = converter.toSApplication(node, ICON_CONTENT, ICON_MIME_TYPE, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplicationWithIcon application = importResult.getApplication();
        assertThat(application.getDisplayName()).isEqualTo("My app");
        assertThat(application.getDescription()).isEqualTo("This is my app");
        assertThat(application.getHomePageId()).isNull();
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getToken()).isEqualTo("app");
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getProfileId()).isEqualTo(profileId);
        assertThat(application.getState()).isEqualTo("ENABLED");
        assertThat(application.getCreatedBy()).isEqualTo(createdBy);
        assertThat(application.getIconContent()).isEqualTo(ICON_CONTENT);
        assertThat(application.getIconMimeType()).isEqualTo(ICON_MIME_TYPE);
        assertThat(application.getInternalProfile()).isNull();
        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();

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

        final SApplicationWithIcon application = importResult.getApplication();
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
    public void toSApplication_should_use_default_layout_when_layout_is_not_defined_in_ApplicationNode()
            throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");

        //when
        long createdBy = 1L;
        final ImportResult importResult = converter.toSApplication(node, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplicationWithIcon application = importResult.getApplication();
        assertThat(application.getLayoutId()).isEqualTo(DEFAULT_LAYOUT_ID);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    @Test
    public void toSApplication_should_throw_importException_when_neither_specified_layout_neither_default_layout_is_found()
            throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        String notAvailableLayout = "notAvailableLayout";
        node.setLayout(notAvailableLayout);
        String token = "app";
        node.setToken(token);

        given(pageService.getPageByName(notAvailableLayout)).willReturn(null);

        //then
        expectedException.expect(ImportException.class);
        expectedException.expectMessage(
                String.format("Unable to import application with token '%s' because the layout '%s' was not found.",
                        token, notAvailableLayout));

        //when
        converter.toSApplication(node, 1L);

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

        final SApplicationWithIcon application = importResult.getApplication();
        assertThat(application.getThemeId()).isEqualTo(themeId);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    @Test
    public void toSApplication_should_use_default_theme_when_layout_is_not_defined_in_ApplicationNode()
            throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");

        long createdBy = 1L;
        final ImportResult importResult = converter.toSApplication(node, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplicationWithIcon application = importResult.getApplication();
        assertThat(application.getThemeId()).isEqualTo(DEFAULT_THEME_ID);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    @Test
    public void toSApplication_should_throw_ImportException_when_neither_specified_theme_neither_default_theme_is_found()
            throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setTheme("notAvailable");
        node.setToken("app");

        given(pageService.getPageByName("notAvailable")).willReturn(null);

        //then
        expectedException.expect(ImportException.class);
        expectedException.expectMessage(
                String.format("Unable to import application with token '%s' because the theme '%s' was not found.",
                        "app", "notAvailable", ApplicationService.DEFAULT_THEME_NAME));

        //when
        converter.toSApplication(node, 1L);

    }

    @Test
    public void toSApplication_should_return_application_with_null_profile_id_when_node_has_no_profile()
            throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setProfile(null);
        node.setToken("TokenName"); // token can never be null in the XML

        //when
        final ImportResult importResult = converter.toSApplication(node, 1L);

        //then
        assertThat(importResult).isNotNull();
        assertThat(importResult.getApplication().getProfileId()).isNull();
    }

    @Test
    public void toSApplication_should_return_Import_result_with_errors_when_profile_is_not_found() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setProfile("admin");
        node.setVersion("1.0");
        node.setToken("app");
        node.setState("ENABLED");

        given(profileService.getProfileByName("admin")).willThrow(new SProfileNotFoundException(""));

        //when
        final ImportResult importResult = converter.toSApplication(node, 1L);

        //then
        assertThat(importResult.getApplication().getProfileId()).isNull();

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).containsExactly(new ImportError("admin", ImportError.Type.PROFILE));
    }

    @Test(expected = ImportException.class)
    public void toSApplication_should_throw_ImportException_when_layout_is_not_found() throws Exception {
        //given
        final ApplicationNode node = buildApplicationNode("app", "1.0");

        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(null);

        //when
        converter.toSApplication(node, 1L);

        //then exception
    }

    @Test(expected = ImportException.class)
    public void toSApplication_should_throw_ImportException_when_theme_is_not_found() throws Exception {
        //given
        final ApplicationNode node = buildApplicationNode("app", "1.0");

        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(null);

        //when
        converter.toSApplication(node, 1L);

        //then exception
    }

    private ApplicationNode buildApplicationNode(final String token, final String version) {
        final ApplicationNode node = new ApplicationNode();
        node.setVersion(version);
        node.setToken(token);
        node.setState("ENABLED");
        return node;
    }

    @Test
    public void toSApplication_should_throw_ImportException_when_application_token_is_invalid() throws Exception {
        //given
        doThrow(new ImportException("invalid token")).when(validator).validate("invalid");
        ApplicationNode applicationNode = buildApplicationNode("invalid", "1.0");

        //then
        expectedException.expect(ImportException.class);
        expectedException.expectMessage("invalid token");

        //when
        converter.toSApplication(applicationNode, 1L);

    }

}
