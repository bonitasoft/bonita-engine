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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.exception.SThemeCreationException;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.exception.SThemeReadException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;
import org.junit.Test;

/**
 * @author Celine Souchet
 * @author Philippe Ozil
 */
public class ThemeServiceStartupHelperTest {

	@Test
	public final void createDefaultThemes_should_call_createDefaultMobileTheme_when_default_mobile_theme_does_not_exist() throws SThemeNotFoundException, 
			SThemeReadException, SThemeCreationException, IOException {
    	final ThemeService themeService = getMockThemeService(false, true);
    	
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService));
        doNothing().when(startupHelper).createDefaultMobileTheme();
        doNothing().when(startupHelper).createDefaultPortalTheme();
        
        startupHelper.createDefaultThemes();
        
        verify(startupHelper, times(1)).createDefaultMobileTheme();
	}
	
	@Test
	public final void createDefaultThemes_should_not_call_createDefaultMobileTheme_when_default_mobile_theme_exists() throws SThemeNotFoundException, 
			SThemeReadException, SThemeCreationException, IOException {
    	final ThemeService themeService = getMockThemeService(true, true);
    	
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService));
        doNothing().when(startupHelper).createDefaultMobileTheme();
        doNothing().when(startupHelper).createDefaultPortalTheme();
        
        startupHelper.createDefaultThemes();
        
        verify(startupHelper, times(0)).createDefaultMobileTheme();
	}
	
	@Test
	public final void createDefaultThemes_should_call_createDefaultPortalTheme_when_default_portal_theme_does_not_exist() throws SThemeNotFoundException, 
			SThemeReadException, SThemeCreationException, IOException {
        final ThemeService themeService = getMockThemeService(true, false);
    	
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService));
        doNothing().when(startupHelper).createDefaultMobileTheme();
        doNothing().when(startupHelper).createDefaultPortalTheme();
        
        startupHelper.createDefaultThemes();
        
        verify(startupHelper, times(1)).createDefaultPortalTheme();
	}
	
	@Test
	public final void createDefaultThemes_should_not_call_createDefaultPortalTheme_when_default_portal_theme_exists() throws SThemeNotFoundException, 
			SThemeReadException, SThemeCreationException, IOException {
        final ThemeService themeService = getMockThemeService(true, true);
    	
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService));
        doNothing().when(startupHelper).createDefaultMobileTheme();
        doNothing().when(startupHelper).createDefaultPortalTheme();
        
        startupHelper.createDefaultThemes();
        
        verify(startupHelper, times(0)).createDefaultMobileTheme();
	}
	
    @Test
    public final void createDefaultMobileTheme_should_create_theme_when_zip_in_classpath() throws SThemeCreationException,
            IOException, SThemeReadException, SThemeNotFoundException {
    	final ThemeService themeService = mock(ThemeService.class);
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService));
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(startupHelper).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        startupHelper.createDefaultMobileTheme();

        verify(themeService, times(1)).createTheme(sTheme);
    }
    
    @Test
    public final void createDefaultMobileTheme_should_not_create_theme_when_zip_not_in_classpath() throws SThemeCreationException, IOException {
    	final ThemeService themeService = mock(ThemeService.class);
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService, "not_used_in_this_test", "not_in_classpath"));

        startupHelper.createDefaultMobileTheme();

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }
    
    @Test
    public final void createDefaultMobileTheme_should_not_create_theme_when_empty_zip_in_classpath() throws SThemeCreationException, IOException {
    	final ThemeService themeService = mock(ThemeService.class);
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService, "not_used_in_this_test", "empty-theme"));

        startupHelper.createDefaultMobileTheme();

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }
    
    @Test
    public final void createDefaultPortalTheme_should_create_theme_when_zip_in_classpath() throws SThemeCreationException,
            IOException, SThemeReadException, SThemeNotFoundException {
    	final ThemeService themeService = mock(ThemeService.class);
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService));
        final STheme sTheme = mock(STheme.class);
        doReturn(sTheme).when(startupHelper).buildSTheme(any(byte[].class), any(byte[].class), any(SThemeType.class));

        startupHelper.createDefaultPortalTheme();

        verify(themeService, times(1)).createTheme(sTheme);
    }
    
    @Test
    public final void createDefaultPortalTheme_should_not_create_theme_when_zip_not_in_classpath() throws SThemeCreationException, IOException {
    	final ThemeService themeService = mock(ThemeService.class);
        final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService, "not_in_classpath", "not_used_in_this_test"));

        startupHelper.createDefaultPortalTheme();

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public final void createDefaultPortalTheme_should_not_create_theme_when_empty_zip_in_classpath() throws SThemeCreationException, IOException {
    	final ThemeService themeService = mock(ThemeService.class);
    	final ThemeServiceStartupHelper startupHelper = spy(new ThemeServiceStartupHelper(themeService, "empty-theme", "not_used_in_this_test"));
    	
    	startupHelper.createDefaultPortalTheme();

        verify(themeService, times(0)).createTheme(any(STheme.class));
    }

    @Test
    public void getFileContent_should_return_null_when_file_not_in_classpath() throws IOException {
    	final ThemeService themeService = mock(ThemeService.class);
        final ThemeServiceStartupHelper startupHelper = new ThemeServiceStartupHelper(themeService);

        assertNull(startupHelper.getFileContent("filename_not_in_classpath.txt"));
    }

    @Test
    public void getFileContent_should_return_content_when_file_in_classpath() throws IOException {
    	final ThemeService themeService = mock(ThemeService.class);
        final ThemeServiceStartupHelper startupHelper = new ThemeServiceStartupHelper(themeService);

        final byte[] fileContent = startupHelper.getFileContent("filename_in_classpath.txt");
        assertNotNull(fileContent);
        assertNotEquals(0, fileContent.length);
    }
    
    /**
     * Returns a mock of the theme service
     * @param hasDefaultMobileTheme whether attempts to retrieve the default mobile theme will return something (null) or throw SThemeNotFoundException
     * @param hasDefaultPortalTheme whether attempts to retrieve the default portal theme will return something (null) or throw SThemeNotFoundException
     * @return theme service mock
     * @throws SThemeReadException 
     * @throws SThemeNotFoundException 
     */
    private ThemeService getMockThemeService(final boolean hasDefaultMobileTheme, final boolean hasDefaultPortalTheme) throws SThemeNotFoundException, SThemeReadException {
    	final ThemeService themeService = mock(ThemeService.class);
    	
    	if (hasDefaultMobileTheme)
    		doReturn(null).when(themeService).getTheme(SThemeType.MOBILE, true);
    	else
    		doThrow(SThemeNotFoundException.class).when(themeService).getTheme(SThemeType.MOBILE, true);
    	
    	if (hasDefaultPortalTheme)
    		doReturn(null).when(themeService).getTheme(SThemeType.PORTAL, true);
    	else
    		doThrow(SThemeNotFoundException.class).when(themeService).getTheme(SThemeType.PORTAL, true);
    	
    	return themeService;
    }
}
