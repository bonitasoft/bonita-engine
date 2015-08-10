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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.exception.SThemeReadException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    @Mock
    private ThemeService themeService;
    @InjectMocks
    @Spy
    private ThemeServiceStartupHelper themeServiceStartupHelper;

    @Test
    public final void createDefaultThemes_should_call_create_on_mobile_and_portal() throws Exception {
        hasNotDefaultTheme(SThemeType.MOBILE);
        hasDefaultTheme(SThemeType.PORTAL);

        doNothing().when(themeServiceStartupHelper).createOrUpdateDefaultMobileTheme();
        doNothing().when(themeServiceStartupHelper).createOrUpdateDefaultMobileTheme();

        themeServiceStartupHelper.createDefaultThemes();

        verify(themeServiceStartupHelper, times(1)).createOrUpdateDefaultMobileTheme();
        verify(themeServiceStartupHelper, times(1)).createOrUpdateDefaultMobileTheme();
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_call_createDefaultMobileTheme_when_default_mobile_theme_does_not_exist() throws Exception {
        hasNotDefaultTheme(SThemeType.MOBILE);
        hasDefaultTheme(SThemeType.PORTAL);

        doNothing().when(themeServiceStartupHelper).createDefaultMobileTheme();
        doNothing().when(themeServiceStartupHelper).createDefaultPortalTheme();

        themeServiceStartupHelper.createOrUpdateDefaultMobileTheme();

        verify(themeServiceStartupHelper, times(1)).createDefaultMobileTheme();
    }

    @Test
    public final void createOrUpdateDefaultMobileTheme_should_not_call_createDefaultMobileTheme_when_default_mobile_theme_exists() throws Exception {
        hasDefaultTheme(SThemeType.MOBILE);
        hasDefaultTheme(SThemeType.PORTAL);

        doNothing().when(themeServiceStartupHelper).createDefaultMobileTheme();
        doNothing().when(themeServiceStartupHelper).createDefaultPortalTheme();

        themeServiceStartupHelper.createOrUpdateDefaultMobileTheme();

        verify(themeServiceStartupHelper, times(0)).createDefaultMobileTheme();
    }

    @Test
    public final void createDefaultPortalTheme_should_call_createDefaultPortalTheme_when_default_portal_theme_does_not_exist() throws Exception {
        hasDefaultTheme(SThemeType.MOBILE);
        hasNotDefaultTheme(SThemeType.PORTAL);

        doNothing().when(themeServiceStartupHelper).createDefaultMobileTheme();
        doNothing().when(themeServiceStartupHelper).createDefaultPortalTheme();

        themeServiceStartupHelper.createOrUpdateDefaultPortalTheme();

        verify(themeServiceStartupHelper, times(1)).createDefaultPortalTheme();
    }

    @Test
    public final void createDefaultPortalTheme_should_not_call_createDefaultPortalTheme_when_default_portal_theme_exists() throws Exception {
        hasDefaultTheme(SThemeType.MOBILE);
        hasDefaultTheme(SThemeType.PORTAL);

        doNothing().when(themeServiceStartupHelper).createDefaultMobileTheme();
        doNothing().when(themeServiceStartupHelper).createDefaultPortalTheme();

        themeServiceStartupHelper.createOrUpdateDefaultPortalTheme();

        verify(themeServiceStartupHelper, times(0)).createDefaultMobileTheme();
    }

    @Test
    public final void createDefaultMobileTheme_should_create_theme_when_zip_in_classpath() throws Exception {
        doReturn(new byte[] {1}).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT + ".zip");
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(themeServiceStartupHelper).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        themeServiceStartupHelper.createDefaultMobileTheme();

        verify(themeService, times(1)).createTheme(sTheme);
    }

    @Test
    public final void createDefaultMobileTheme_should_not_create_theme_when_zip_not_in_classpath() throws Exception {
        doReturn(null).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT + ".zip");

        themeServiceStartupHelper.createDefaultMobileTheme();

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void createDefaultMobileTheme_should_not_create_theme_when_empty_zip_in_classpath() throws Exception {
        doReturn(new byte[] {}).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_MOBILE_THEME_DEFAULT + ".zip");

        themeServiceStartupHelper.createDefaultMobileTheme();

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void createDefaultPortalTheme_should_create_theme_when_zip_in_classpath() throws Exception {
        doReturn(new byte[] {1}).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT + ".zip");
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(themeServiceStartupHelper).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        themeServiceStartupHelper.createDefaultPortalTheme();

        verify(themeService, times(1)).createTheme(sTheme);
    }

    @Test
    public final void createDefaultPortalTheme_should_not_create_theme_when_zip_not_in_classpath() throws Exception {
        doReturn(null).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT + ".zip");


        themeServiceStartupHelper.createDefaultPortalTheme();

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void createDefaultPortalTheme_should_not_create_theme_when_empty_zip_in_classpath() throws Exception {
        doReturn(new byte[] {}).when(themeServiceStartupHelper).getFileContent(ThemeServiceStartupHelper.BONITA_PORTAL_THEME_DEFAULT + ".zip");


        themeServiceStartupHelper.createDefaultPortalTheme();

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

    private void hasNotDefaultTheme(SThemeType portal) throws SThemeNotFoundException, SThemeReadException {
        doThrow(SThemeNotFoundException.class).when(themeService).getTheme(portal, true);
    }

    private void hasDefaultTheme(SThemeType mobile) throws SThemeNotFoundException, SThemeReadException {
        doReturn(null).when(themeService).getTheme(mobile, true);
    }
}
