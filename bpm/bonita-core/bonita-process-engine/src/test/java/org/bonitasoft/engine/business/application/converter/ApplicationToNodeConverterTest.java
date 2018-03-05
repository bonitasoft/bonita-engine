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
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationToNodeConverterTest {

    @Mock
    private ProfileService profileService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationPageToNodeConverter pageConverter;
    @Mock
    private ApplicationMenuToNodeConverter menuConverter;
    @Mock
    private PageService pageService;
    @Mock
    private SPage defaultLayout;
    @Mock
    private SPage defaultTheme;

    @InjectMocks
    private ApplicationToNodeConverter converter;

    @Test
    public void toNode_should_return_convert_all_string_fields() throws Exception {
        //given
        long createdBy = 11L;
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), createdBy, "enabled", null, null);
        application.setDescription("this is my app");
        application.setIconPath("/icon.jpg");

        //when
        final ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getToken()).isEqualTo("app");
        assertThat(applicationNode.getDisplayName()).isEqualTo("my app");
        assertThat(applicationNode.getVersion()).isEqualTo("1.0");
        assertThat(applicationNode.getDescription()).isEqualTo("this is my app");
        assertThat(applicationNode.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(applicationNode.getState()).isEqualTo("enabled");
        assertThat(applicationNode.getProfile()).isNull();
        assertThat(applicationNode.getHomePage()).isNull();

    }

    @Test
    public void toNode_should_replace_profile_id_by_profile_name() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10, "enabled", null, null);
        application.setProfileId(7L);
        final SProfile profile = mock(SProfile.class);
        given(profile.getName()).willReturn("admin");

        given(profileService.getProfile(7L)).willReturn(profile);

        //when
        final ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getProfile()).isEqualTo("admin");
    }

    @Test(expected = ExportException.class)
    public void toNode_should_throw_ExportException_when_profileService_throws_exception() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, null);
        application.setProfileId(7L);

        given(profileService.getProfile(7L)).willThrow(new SProfileNotFoundException(""));

        //when
        converter.toNode(application);

        //then exception
    }

    @Test
    public void toNode_should_replaceHomePageId_by_application_page_token() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, null);
        application.setHomePageId(8L);
        final SApplicationPage homePage = mock(SApplicationPage.class);
        given(homePage.getToken()).willReturn("home");

        given(applicationService.getApplicationPage(8L)).willReturn(homePage);

        //when
        final ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getHomePage()).isEqualTo("home");
    }

    @Test(expected = ExportException.class)
    public void toNode_should_throw_ExportException_when_applicationService_throws_exception() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, null);
        application.setHomePageId(8L);

        given(applicationService.getApplicationPage(8L)).willThrow(new SObjectNotFoundException());

        //when
        converter.toNode(application);

        //then exception
    }

    @Test
    public void toNode_should_replaceLayoutId_by_page_name() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", 9L, null);
        final SPage layout = mock(SPage.class);
        given(layout.getName()).willReturn("mainLayout");

        given(pageService.getPage(9L)).willReturn(layout);

        //when
        final ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getLayout()).isEqualTo("mainLayout");
    }

    @Test
    public void toNode_should_replaceThemeId_by_page_name() throws Exception {
        //given
        long themeId = 20L;
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, themeId);
        final SPage layout = mock(SPage.class);
        given(layout.getName()).willReturn("mainTheme");

        given(pageService.getPage(themeId)).willReturn(layout);

        //when
        final ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode).isNotNull();
        assertThat(applicationNode.getTheme()).isEqualTo("mainTheme");
    }

    @Test(expected = ExportException.class)
    public void toNodeShouldThrowExceptionAtMenuConversion() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, null);

        doThrow(new SBonitaReadException("")).when(menuConverter).addMenusToApplicationNode(nullable(Long.class), nullable(Long.class), nullable(ApplicationNode.class),
                nullable(ApplicationMenuNode.class));

        converter.toNode(application);
    }

    @Test
    public void toNodeShouldAddConvertedMenus() throws Exception {
        //given
        final long applicationId = 1191L;
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, null);
        application.setId(applicationId);

        doNothing().when(menuConverter).addMenusToApplicationNode(eq(applicationId), isNull(Long.class), any(ApplicationNode.class),
                isNull(ApplicationMenuNode.class));

        //when
        converter.toNode(application);

        //then
        verify(menuConverter).addMenusToApplicationNode(eq(applicationId), isNull(Long.class), any(ApplicationNode.class), isNull(ApplicationMenuNode.class));
    }

    @Test(expected = ExportException.class)
    public void toNodeShouldAddThrowExceptionAtPageConversion() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, null);

        given(applicationService.searchApplicationPages(any(QueryOptions.class))).willThrow(new SBonitaReadException(""));

        converter.toNode(application);
    }

    @Test
    public void toNodeShouldAddConvertedPages() throws Exception {
        //given
        final SApplicationImpl application = new SApplicationImpl("app", "my app", "1.0", new Date().getTime(), 10L, "enabled", null, null);
        final List<SApplicationPage> pages = new ArrayList<>(1);
        final SApplicationPage page = mock(SApplicationPage.class);
        pages.add(page);

        final ApplicationPageNode pageNode = mock(ApplicationPageNode.class);
        given(pageConverter.toPage(page)).willReturn(pageNode);

        given(applicationService.searchApplicationPages(any(QueryOptions.class))).willReturn(pages).willReturn(Collections.<SApplicationPage>emptyList());

        //when
        final ApplicationNode applicationNode = converter.toNode(application);

        //then
        assertThat(applicationNode.getApplicationPages().size()).isEqualTo(1);
        assertThat(applicationNode.getApplicationPages().get(0)).isEqualTo(pageNode);
    }


}
