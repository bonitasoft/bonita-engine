/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.exception.SThemeCreationException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class PlatformAPIImplDelegateTest {

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.PlatformAPIImplDelegate#createDefaultThemes(org.bonitasoft.engine.service.TenantServiceAccessor)}.
     * 
     * @throws IOException
     * @throws SThemeCreationException
     */
    @Test
    public final void should_call_create_theme_from_theme_service_when_creating_default_themes_from_filenames_in_classpath() throws SThemeCreationException,
            IOException {
        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final ThemeService themeService = mock(ThemeService.class);
        doReturn(themeService).when(tenantServiceAccessor).getThemeService();

        final PlatformAPIImplDelegate delegate = spy(new PlatformAPIImplDelegate());
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(delegate).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        delegate.createDefaultThemes(tenantServiceAccessor);

        // Call 2 times : one for portal, and one for mobile
        verify(themeService, times(2)).createTheme(any(STheme.class));
    }

    @Test
    public final void should_not_call_create_theme_for_empty_portal_zip() throws SThemeCreationException, IOException {
        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final ThemeService themeService = mock(ThemeService.class);
        doReturn(themeService).when(tenantServiceAccessor).getThemeService();

        final PlatformAPIImplDelegate delegate = spy(new PlatformAPIImplDelegate("empty-theme", "not_used_in_this_test"));

        delegate.createDefaultPortalTheme(tenantServiceAccessor);

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void should_not_call_create_theme_for_empty_mobile_zip() throws SThemeCreationException, IOException {
        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final ThemeService themeService = mock(ThemeService.class);
        doReturn(themeService).when(tenantServiceAccessor).getThemeService();

        final PlatformAPIImplDelegate delegate = spy(new PlatformAPIImplDelegate("not_used_in_this_test", "empty-theme"));

        delegate.createDefaultMobileTheme(tenantServiceAccessor);

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void should_not_call_create_theme_from_theme_service_when_creating_default_themes_from_portal_zip_not_in_classpath()
            throws SThemeCreationException, IOException {
        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final ThemeService themeService = mock(ThemeService.class);
        doReturn(themeService).when(tenantServiceAccessor).getThemeService();

        final PlatformAPIImplDelegate delegate = spy(new PlatformAPIImplDelegate("portal_not_in_classpath", "bonita-mobile-theme"));
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(delegate).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        delegate.createDefaultThemes(tenantServiceAccessor);

        // Call 1 times : Mobile
        verify(themeService, times(1)).createTheme(any(STheme.class));
    }

    @Test
    public final void should_not_call_create_theme_from_theme_service_when_creating_default_themes_from_mobile_zip_not_in_classpath()
            throws SThemeCreationException, IOException {
        final TenantServiceAccessor tenantServiceAccessor = mock(TenantServiceAccessor.class);
        final ThemeService themeService = mock(ThemeService.class);
        doReturn(themeService).when(tenantServiceAccessor).getThemeService();

        final PlatformAPIImplDelegate delegate = spy(new PlatformAPIImplDelegate("bonita-portal-theme", "mobile_not_in_classpath"));
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(delegate).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        delegate.createDefaultThemes(tenantServiceAccessor);

        // Call 1 times : Portal
        verify(themeService, times(1)).createTheme(any(STheme.class));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.api.impl.PlatformAPIImplDelegate#getFileContent(java.lang.String)}.
     */
    @Test
    public void should_return_null_when_getting_content_from_filename_not_in_classpath() throws IOException {
        final PlatformAPIImplDelegate delegate = new PlatformAPIImplDelegate();

        assertNull(delegate.getFileContent("filename_not_in_classpath.txt"));
    }

    @Test
    public void should_return_content_when_getting_content_from_filename_in_classpath() throws IOException {
        final PlatformAPIImplDelegate delegate = new PlatformAPIImplDelegate();

        final byte[] fileContent = delegate.getFileContent("filename_in_classpath.txt");
        assertNotNull(fileContent);
        assertNotEquals(0, fileContent.length);
    }
}
