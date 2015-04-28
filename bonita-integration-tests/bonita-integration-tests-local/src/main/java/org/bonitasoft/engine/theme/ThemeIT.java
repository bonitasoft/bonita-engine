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

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Celine Souchet
 */
@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class ThemeIT extends TestWithTechnicalUser {

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Get default", "Mobile" }, jira = "BS-2396, BS-2397")
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

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Get default", "Portal" }, jira = "BS-2396, BS-2397")
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

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Get default", "Wrong parameter" }, jira = "BS-2396, BS-2397")
    @Test(expected = RuntimeException.class)
    public void cantGetDefaultThemeWithoutType() {
        getThemeAPI().getDefaultTheme(null);
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Get current", "Wrong parameter" }, jira = "BS-2396, BS-2397")
    @Test(expected = RuntimeException.class)
    public void cantGetCurrentThemeWithoutType() {
        getThemeAPI().getCurrentTheme(null);
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Get last update date", "Wrong parameter" }, jira =
            "BS-2396, BS-2397")
    @Test(expected = RuntimeException.class)
    public void cantGetLastUpdateDateWithoutType() {
        getThemeAPI().getLastUpdateDate(null);
    }

}
