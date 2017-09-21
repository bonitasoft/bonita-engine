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
package org.bonitasoft.engine.theme.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.theme.ThemeRetriever;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.bonitasoft.engine.theme.model.impl.SThemeImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @author Philippe Ozil
 */
@RunWith(MockitoJUnitRunner.class)
public class ThemeServiceStartupHelperTest {

    private static final byte[] PORTAL_CONTENT = new byte[] { 6 };
    private static final byte[] PORTAL_CSS_CONTENT = new byte[] { 1, 2, 3, 4, 5 };
    private static final byte[] MOBILE_CONTENT = new byte[] { 5 };

    @Captor
    private ArgumentCaptor<EntityUpdateDescriptor> captor;

    @Mock
    private ThemeService themeService;

    @Mock
    private ThemeRetriever themeRetriever;

    @Mock
    private ThemeActionCalculator themeActionCalculator;

    @InjectMocks
    @Spy
    private ThemeServiceStartupHelper themeServiceStartupHelper;

    @Before
    public void before() throws Exception {
        doReturn(MOBILE_CONTENT).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT + ".zip");
        doReturn(PORTAL_CONTENT).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT + ".zip");
        doReturn(null).when(themeServiceStartupHelper).getCssContent(SThemeType.MOBILE);
        doReturn(PORTAL_CSS_CONTENT).when(themeServiceStartupHelper).getCssContent(SThemeType.PORTAL);
    }

    private void hasNoTheme(SThemeType type) throws SBonitaReadException {
        doReturn(null).when(themeRetriever).getTheme(type, true);
    }

    private SThemeImpl hasThemeWithContentContent(SThemeType type, byte[] content) throws SBonitaReadException {
        final SThemeImpl toBeReturned = new SThemeImpl();
        doReturn(toBeReturned).when(themeRetriever).getTheme(type, true);
        toBeReturned.setContent(content);
        return toBeReturned;
    }

    @Test
    public final void createDefaultThemes_should_call_create_on_mobile_and_portal() throws Exception {
        hasNoTheme(SThemeType.MOBILE);
        hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);
        doNothing().when(themeServiceStartupHelper).createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);
        doNothing().when(themeServiceStartupHelper).createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        themeServiceStartupHelper.createOrUpdateDefaultThemes();

        verify(themeServiceStartupHelper, times(1)).createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);
        verify(themeServiceStartupHelper, times(1)).createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_call_createDefaultMobileTheme_when_action_is_create() throws Exception {
        hasNoTheme(SThemeType.MOBILE);
        SThemeImpl portalTheme = hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);
        final SThemeType mobile = SThemeType.MOBILE;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(MOBILE_CONTENT, mobile);
        final SThemeType portal = SThemeType.PORTAL;

        given(themeActionCalculator.calculateAction(null, MOBILE_CONTENT)).willReturn(ThemeActionCalculator.ThemeAction.CREATE);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).createDefaultTheme(MOBILE_CONTENT, SThemeType.MOBILE);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_call_updateDefaultMobileTheme_when_action_is_update() throws Exception {
        final SThemeImpl mobileTheme = hasThemeWithContentContent(SThemeType.MOBILE, new byte[] { 4, 5 });
        SThemeImpl portalTheme = hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);

        given(themeActionCalculator.calculateAction(mobileTheme, MOBILE_CONTENT)).willReturn(ThemeActionCalculator.ThemeAction.UPDATE);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).updateDefaultTheme(mobileTheme, MOBILE_CONTENT, SThemeType.MOBILE);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_call_updateDefaultPortalTheme_when_action_is_update() throws Exception {
        SThemeImpl mobileTheme = hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        final SThemeImpl portalTheme = hasThemeWithContentContent(SThemeType.PORTAL, new byte[] { 4, 5 });

        given(themeActionCalculator.calculateAction(portalTheme, PORTAL_CONTENT)).willReturn(ThemeActionCalculator.ThemeAction.UPDATE);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).updateDefaultTheme(portalTheme, PORTAL_CONTENT, SThemeType.PORTAL);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_not_call_updateDefaultPortalTheme_when_action_is_none() throws Exception {
        SThemeImpl mobileTheme = hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        SThemeImpl portalTheme = hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);

        given(themeActionCalculator.calculateAction(portalTheme, PORTAL_CONTENT)).willReturn(ThemeActionCalculator.ThemeAction.NONE);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        verify(themeServiceStartupHelper, never()).updateDefaultTheme(any(STheme.class), any(byte[].class), any(SThemeType.class));
    }

    @Test
    public void should_updateDefaultMobileTheme_update_the_theme() throws Exception {
        final SThemeImpl theme = new SThemeImpl();

        themeServiceStartupHelper.updateDefaultTheme(theme, MOBILE_CONTENT, SThemeType.MOBILE);

        verify(themeService).updateTheme(eq(theme), captor.capture());
        final EntityUpdateDescriptor entityUpdateDescriptor = captor.getValue();

        assertThat(entityUpdateDescriptor.getFields().keySet()).containsOnly("content", "cssContent", "lastUpdateDate");
        assertThat(entityUpdateDescriptor.getFields().get("content")).isEqualTo(MOBILE_CONTENT);
        assertThat(entityUpdateDescriptor.getFields().get("cssContent")).isNull();
    }

    @Test
    public void should_updateDefaultPortalTheme_update_the_theme() throws Exception {
        final SThemeImpl theme = new SThemeImpl();

        themeServiceStartupHelper.updateDefaultTheme(theme, PORTAL_CONTENT, SThemeType.PORTAL);

        verify(themeService).updateTheme(eq(theme), captor.capture());
        final EntityUpdateDescriptor entityUpdateDescriptor = captor.getValue();

        assertThat(entityUpdateDescriptor.getFields().keySet()).containsOnly("content", "cssContent", "lastUpdateDate");
        assertThat(entityUpdateDescriptor.getFields().get("content")).isEqualTo(PORTAL_CONTENT);
        assertThat(entityUpdateDescriptor.getFields().get("cssContent")).isEqualTo(PORTAL_CSS_CONTENT);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_not_call_createDefaultMobileTheme_when_action_is_none() throws Exception {
        SThemeImpl mobileTheme = hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        SThemeImpl portalTheme = hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);

        given(themeActionCalculator.calculateAction(mobileTheme, MOBILE_CONTENT)).willReturn(ThemeActionCalculator.ThemeAction.NONE);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(0)).createDefaultTheme(MOBILE_CONTENT, SThemeType.MOBILE);
    }

    @Test
    public final void createDefaultPortalTheme_should_call_createDefaultPortalTheme_when_action_is_create() throws Exception {
        SThemeImpl mobileTheme = hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        hasNoTheme(SThemeType.PORTAL);
        final SThemeType mobile = SThemeType.MOBILE;
        final SThemeType portal = SThemeType.PORTAL;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(PORTAL_CONTENT, portal);

        given(themeActionCalculator.calculateAction(null, PORTAL_CONTENT)).willReturn(ThemeActionCalculator.ThemeAction.CREATE);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).createDefaultTheme(PORTAL_CONTENT, SThemeType.PORTAL);
    }

    @Test
    public final void createDefaultPortalTheme_should_not_call_createDefaultPortalTheme_when_action_is_none() throws Exception {
        SThemeImpl mobileTheme = hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        SThemeImpl portalTheme = hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);
        final SThemeType mobile = SThemeType.MOBILE;
        final SThemeType portal = SThemeType.PORTAL;

        given(themeActionCalculator.calculateAction(portalTheme, PORTAL_CONTENT)).willReturn(ThemeActionCalculator.ThemeAction.NONE);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(0)).createDefaultTheme(PORTAL_CONTENT, mobile);
    }

    @Test
    public final void createDefaultMobileTheme_should_create_theme_when_zip_in_classpath() throws Exception {
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(themeServiceStartupHelper).buildSTheme(nullable(byte[].class), nullable(byte[].class), nullable(SThemeType.class));

        final SThemeType mobile = SThemeType.MOBILE;
        themeServiceStartupHelper.createDefaultTheme(MOBILE_CONTENT, mobile);

        verify(themeService, times(1)).createTheme(sTheme);
    }

    @Test
    public final void createDefaultPortalTheme_should_create_theme_when_zip_in_classpath() throws Exception {
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(themeServiceStartupHelper).buildSTheme(nullable(byte[].class), nullable(byte[].class), nullable(SThemeType.class));

        final SThemeType portal = SThemeType.PORTAL;
        themeServiceStartupHelper.createDefaultTheme(PORTAL_CONTENT, portal);

        verify(themeService, times(1)).createTheme(sTheme);
    }

    @Test
    public void getFileContent_should_return_null_when_file_not_in_classpath() throws IOException {
        final ThemeServiceStartupHelper startupHelper = new ThemeServiceStartupHelper(themeService, themeRetriever, themeActionCalculator);

        assertNull(startupHelper.getFileContent("filename_not_in_classpath.txt"));
    }

    @Test
    public void getFileContent_should_return_content_when_file_in_classpath() throws IOException {
        final ThemeServiceStartupHelper startupHelper = new ThemeServiceStartupHelper(themeService, themeRetriever, themeActionCalculator);

        final byte[] fileContent = startupHelper.getFileContent("filename_in_classpath.txt");
        assertNotNull(fileContent);
        assertNotEquals(0, fileContent.length);
    }

    @Test
    public void should_getCssContent_return_null_when_mobile() throws Exception {
        final ThemeServiceStartupHelper themeServiceStartupHelper = new ThemeServiceStartupHelper(themeService, themeRetriever, themeActionCalculator);

        final byte[] cssContent = themeServiceStartupHelper.getCssContent(SThemeType.MOBILE);

        assertThat(cssContent).isNull();
    }

    @Test
    public void should_getCssContent_return_bonitaCss_when_portal() throws Exception {
        final ThemeServiceStartupHelper themeServiceStartupHelper = spy(new ThemeServiceStartupHelper(themeService, themeRetriever, themeActionCalculator));
        doReturn(IOUtil.zip(Collections.singletonMap("bonita.css", new byte[] { 1, 2, 4 }))).when(themeServiceStartupHelper).getFileContent(
                ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT + "-css.zip");

        final byte[] cssContent = themeServiceStartupHelper.getCssContent(SThemeType.PORTAL);

        assertThat(cssContent).isEqualTo(new byte[] { 1, 2, 4 });
    }
}
