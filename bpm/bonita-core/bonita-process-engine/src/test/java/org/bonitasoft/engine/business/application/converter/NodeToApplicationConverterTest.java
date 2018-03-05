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
package org.bonitasoft.engine.business.application.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
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
    public void toSApplication_should_return_ImportResult_with_no_errors_and_application_with_all_fields_except_home_page() throws Exception {
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
        final ImportResult importResult = converter.toSApplication(node, createdBy);

        //then
        assertThat(importResult).isNotNull();

        final SApplication application = importResult.getApplication();
        assertThat(application.getDisplayName()).isEqualTo("My app");
        assertThat(application.getDescription()).isEqualTo("This is my app");
        assertThat(application.getHomePageId()).isNull();
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getToken()).isEqualTo("app");
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getProfileId()).isEqualTo(profileId);
        assertThat(application.getState()).isEqualTo("ENABLED");
        assertThat(application.getCreatedBy()).isEqualTo(createdBy);

        final ImportStatus importStatus = importResult.getImportStatus();
        assertThat(importStatus.getName()).isEqualTo("app");
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();

    }

    @Test
    public void toSApplication_should_always_use_default_layout() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");
        node.setLayout("dummyLayout"); // will not be used, the layout will be always the default one

        given(pageService.getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME)).willReturn(defaultLayout);

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
    public void toSApplication_should_always_use_default_theme() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setToken("app");
        node.setTheme("dummyTheme"); // will not be used, the theme will be always the default one

        given(pageService.getPageByName(ApplicationService.DEFAULT_THEME_NAME)).willReturn(defaultTheme);

        //when
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
    public void toSApplication_should_return_application_with_null_profile_id_when_node_has_no_profile() throws Exception {
        //given
        final ApplicationNode node = new ApplicationNode();
        node.setProfile(null);

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
