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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.exception.SThemeReadException;
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
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @author Philippe Ozil
 */
@RunWith(MockitoJUnitRunner.class)
public class ThemeServiceStartupHelperTest {

    public static final byte[] PORTAL_CONTENT = new byte[] { 6 };
    public static final byte[] PORTAL_CSS_CONTENT = new byte[] { 1, 2, 3, 4, 5 };
    private static final byte[] MOBILE_CONTENT = new byte[] { 5 };
    @Captor
    public ArgumentCaptor<EntityUpdateDescriptor> captor;
    @Mock
    private ThemeService themeService;
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

    private void hasNoTheme(SThemeType type) throws SThemeNotFoundException, SThemeReadException {
        doThrow(SThemeNotFoundException.class).when(themeService).getTheme(type, true);
    }

    private SThemeImpl hasThemeWithContentContent(SThemeType type, byte[] content) throws SThemeNotFoundException, SThemeReadException {
        final SThemeImpl toBeReturned = new SThemeImpl();
        doReturn(toBeReturned).when(themeService).getTheme(type, true);
        toBeReturned.setContent(content);
        return toBeReturned;
    }

    @Test
    public final void createDefaultThemes_should_call_create_on_mobile_and_portal() throws Exception {
        hasNoTheme(SThemeType.MOBILE);
        hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);
        doNothing().when(themeServiceStartupHelper).createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);
        doNothing().when(themeServiceStartupHelper).createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);

        themeServiceStartupHelper.createOrUpdateDefaultThemes();

        verify(themeServiceStartupHelper, times(1)).createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);
        verify(themeServiceStartupHelper, times(1)).createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_call_createDefaultMobileTheme_when_default_mobile_theme_does_not_exist() throws Exception {
        hasNoTheme(SThemeType.MOBILE);
        hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);
        final SThemeType mobile = SThemeType.MOBILE;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(MOBILE_CONTENT, mobile);
        final SThemeType portal = SThemeType.PORTAL;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(PORTAL_CONTENT, portal);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).createDefaultTheme(MOBILE_CONTENT, SThemeType.MOBILE);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_call_updateDefaultMobileTheme_when_default_mobile_theme_is_not_good() throws Exception {
        final SThemeImpl currentMobileTheme = hasThemeWithContentContent(SThemeType.MOBILE, new byte[] { 4, 5 });
        hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).updateDefaultTheme(currentMobileTheme, MOBILE_CONTENT, SThemeType.MOBILE);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_call_updateDefaultPortalTheme_when_default_mobile_theme_is_not_good() throws Exception {
        hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        final SThemeImpl sTheme = hasThemeWithContentContent(SThemeType.PORTAL, new byte[] { 4, 5 });

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).updateDefaultTheme(sTheme, PORTAL_CONTENT, SThemeType.PORTAL);
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_not_call_updateDefaultPortalTheme_when_default_mobile_theme_good() throws Exception {
        hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);

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
    public final void createOrUpdateDefaultMobileTheme_should_not_call_createDefaultMobileTheme_when_default_mobile_theme_exists() throws Exception {
        hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);
        final SThemeType mobile1 = SThemeType.MOBILE;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(MOBILE_CONTENT, mobile1);
        final SThemeType portal = SThemeType.PORTAL;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(PORTAL_CONTENT, portal);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.MOBILE, ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT);

        final SThemeType mobile = SThemeType.MOBILE;
        verify(themeServiceStartupHelper, times(0)).createDefaultTheme(MOBILE_CONTENT, mobile);
    }

    @Test
    public final void createDefaultPortalTheme_should_call_createDefaultPortalTheme_when_default_portal_theme_does_not_exist() throws Exception {
        hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        hasNoTheme(SThemeType.PORTAL);
        final SThemeType mobile = SThemeType.MOBILE;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(MOBILE_CONTENT, mobile);
        final SThemeType portal = SThemeType.PORTAL;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(PORTAL_CONTENT, portal);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        verify(themeServiceStartupHelper, times(1)).createDefaultTheme(PORTAL_CONTENT, SThemeType.PORTAL);
    }

    @Test
    public final void createDefaultPortalTheme_should_not_call_createDefaultPortalTheme_when_default_portal_theme_exists() throws Exception {
        hasThemeWithContentContent(SThemeType.MOBILE, MOBILE_CONTENT);
        hasThemeWithContentContent(SThemeType.PORTAL, PORTAL_CONTENT);
        final SThemeType mobile1 = SThemeType.MOBILE;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(MOBILE_CONTENT, mobile1);
        final SThemeType portal = SThemeType.PORTAL;
        doNothing().when(themeServiceStartupHelper).createDefaultTheme(PORTAL_CONTENT, portal);

        themeServiceStartupHelper.createOrUpdateDefaultTheme(SThemeType.PORTAL, ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT);

        final SThemeType mobile = SThemeType.MOBILE;
        verify(themeServiceStartupHelper, times(0)).createDefaultTheme(MOBILE_CONTENT, mobile);
    }

    @Test
    public final void createDefaultMobileTheme_should_create_theme_when_zip_in_classpath() throws Exception {
        doReturn(new byte[] { 1 }).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT + ".zip");
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(themeServiceStartupHelper).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        final SThemeType mobile = SThemeType.MOBILE;
        themeServiceStartupHelper.createDefaultTheme(MOBILE_CONTENT, mobile);

        verify(themeService, times(1)).createTheme(sTheme);
    }

    @Test
    public final void createDefaultMobileTheme_should_not_create_theme_when_zip_not_in_classpath() throws Exception {
        final SThemeType mobile = SThemeType.MOBILE;
        themeServiceStartupHelper.createDefaultTheme(null, mobile);

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void createDefaultMobileTheme_should_not_create_theme_when_empty_zip_in_classpath() throws Exception {
        final SThemeType mobile = SThemeType.MOBILE;
        themeServiceStartupHelper.createDefaultTheme(new byte[] {}, mobile);

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void createDefaultPortalTheme_should_create_theme_when_zip_in_classpath() throws Exception {
        doReturn(new byte[] { 1 }).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT + ".zip");
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(themeServiceStartupHelper).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        final SThemeType portal = SThemeType.PORTAL;
        themeServiceStartupHelper.createDefaultTheme(PORTAL_CONTENT, portal);

        verify(themeService, times(1)).createTheme(sTheme);
    }

    @Test
    public final void createDefaultPortalTheme_should_not_create_theme_when_zip_not_in_classpath() throws Exception {
        final SThemeType portal = SThemeType.PORTAL;
        themeServiceStartupHelper.createDefaultTheme(null, portal);

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void createDefaultPortalTheme_should_not_create_theme_when_empty_zip_in_classpath() throws Exception {
        final SThemeType portal = SThemeType.PORTAL;
        themeServiceStartupHelper.createDefaultTheme(new byte[] {}, portal);

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public void getFileContent_should_return_null_when_file_not_in_classpath() throws IOException {
        final ThemeServiceStartupHelper startupHelper = new ThemeServiceStartupHelper(themeService);

        assertNull(startupHelper.getFileContent("filename_not_in_classpath.txt"));
    }

    @Test
    public void getFileContent_should_return_content_when_file_in_classpath() throws IOException {
        final ThemeServiceStartupHelper startupHelper = new ThemeServiceStartupHelper(themeService);

        final byte[] fileContent = startupHelper.getFileContent("filename_in_classpath.txt");
        assertNotNull(fileContent);
        assertNotEquals(0, fileContent.length);
    }

    @Test
    public void should_getCssContent_return_null_when_mobile() throws Exception {
        final ThemeServiceStartupHelper themeServiceStartupHelper = new ThemeServiceStartupHelper(themeService);

        final byte[] cssContent = themeServiceStartupHelper.getCssContent(SThemeType.MOBILE);

        assertThat(cssContent).isNull();
    }

    @Test
    public void should_getCssContent_return_bonitaCss_when_portal() throws Exception {
        final ThemeServiceStartupHelper themeServiceStartupHelper = spy(new ThemeServiceStartupHelper(themeService));
        doReturn(IOUtil.zip(Collections.singletonMap("bonita.css", new byte[] { 1, 2, 4 }))).when(themeServiceStartupHelper).getFileContent(
                ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT + "-css.zip");

        final byte[] cssContent = themeServiceStartupHelper.getCssContent(SThemeType.PORTAL);

        assertThat(cssContent).isEqualTo(new byte[] { 1, 2, 4 });
    }
}
