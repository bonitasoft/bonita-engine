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
package org.bonitasoft.engine.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ThemeIT extends TestWithTechnicalUser {

    @Test
    public void getMobileTheme() {
        final ThemeType type = ThemeType.MOBILE;
        final Theme defaultTheme = getThemeAPI().getDefaultTheme(type);
        assertEquals(type, defaultTheme.getType());
        assertNotNull(defaultTheme.getLastUpdatedDate());

        final Theme currentTheme = getThemeAPI().getCurrentTheme(type);
        assertEquals(type, currentTheme.getType());
        assertEquals(defaultTheme, currentTheme);

        final Date lastUpdateDate = getThemeAPI().getLastUpdateDate(type);
        assertEquals(defaultTheme.getLastUpdatedDate(), lastUpdateDate);
    }

    @Test
    public void getPortalTheme() {
        final ThemeType type = ThemeType.PORTAL;
        final Theme defaultTheme = getThemeAPI().getDefaultTheme(type);
        assertEquals(type, defaultTheme.getType());
        assertNotNull(defaultTheme.getLastUpdatedDate());
        final Theme currentTheme = getThemeAPI().getCurrentTheme(type);
        assertEquals(type, currentTheme.getType());
        assertEquals(defaultTheme, currentTheme);
        final Date lastUpdateDate = getThemeAPI().getLastUpdateDate(type);
        assertEquals(defaultTheme.getLastUpdatedDate(), lastUpdateDate);
    }

    @Test(expected = RuntimeException.class)
    public void cantGetDefaultThemeWithoutType() {
        getThemeAPI().getDefaultTheme(null);
    }

    @Test(expected = RuntimeException.class)
    public void cantGetCurrentThemeWithoutType() {
        getThemeAPI().getCurrentTheme(null);
    }

    @Test(expected = RuntimeException.class)
    public void cantGetLastUpdateDateWithoutType() {
        getThemeAPI().getLastUpdateDate(null);
    }

}
