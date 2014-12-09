package org.bonitasoft.engine.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ThemeTest extends CommonAPITest {

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @After
    public void after() throws Exception {
       logoutOnTenant();
    }

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
