package com.bonitasoft.engine.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ThemeAPI;
import com.bonitasoft.engine.theme.exception.RestoreThemeException;
import com.bonitasoft.engine.theme.exception.SetThemeException;

/**
 * @author Celine Souchet
 */
public class ThemeTest extends CommonAPISPTest {

    @After
    public void afterTest() throws BonitaException {
        getThemeAPI().restoreDefaultTheme(ThemeType.MOBILE);
        getThemeAPI().restoreDefaultTheme(ThemeType.PORTAL);
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Set custom" }, jira = "BS-2396, BS-2397")
    @Test
    public void setCustomTheme() throws Exception {
        final ThemeType type = ThemeType.MOBILE;
        final Theme defaultTheme = getThemeAPI().getDefaultTheme(type);

        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        final Theme createdTheme = getThemeAPI().setCustomTheme(content, cssContent, type);

        final Theme themeResult = getThemeAPI().getCurrentTheme(type);
        assertEquals(createdTheme, themeResult);
        assertEquals(content, createdTheme.getContent());
        assertEquals(cssContent, createdTheme.getCssContent());
        assertEquals(type, createdTheme.getType());
        assertFalse(createdTheme.isDefault());

        final Date lastUpdateDate = getThemeAPI().getLastUpdateDate(type);
        assertEquals(createdTheme.getLastUpdatedDate(), lastUpdateDate);
        assertTrue(defaultTheme.getLastUpdatedDate().before(lastUpdateDate));
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Set custom", "Update custom" }, jira = "BS-2396, BS-2397")
    @Test
    public void overrideCustomTheme() throws Exception {
        final Theme createdTheme = getThemeAPI().setCustomTheme("plop".getBytes(), "cssContent".getBytes(), ThemeType.MOBILE);

        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        final ThemeType type = ThemeType.MOBILE;
        final Theme updatedTheme = getThemeAPI().setCustomTheme(content, cssContent, type);
        assertEquals(content, updatedTheme.getContent());
        assertEquals(cssContent, updatedTheme.getCssContent());
        assertEquals(type, updatedTheme.getType());
        assertEquals(createdTheme.getId(), updatedTheme.getId());
        assertTrue(createdTheme.getLastUpdatedDate().before(updatedTheme.getLastUpdatedDate()));

        assertEquals(updatedTheme, getThemeAPI().getCurrentTheme(type));
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Set custom", "Wrong parameter" }, jira = "BS-2396, BS-2397")
    @Test(expected = SetThemeException.class)
    public void cantSetCustomThemeWithoutContent() throws Exception {
        final ThemeType type = ThemeType.MOBILE;
        final Theme defaultTheme = getThemeAPI().getDefaultTheme(type);

        final byte[] cssContent = "cssContent".getBytes();
        try {
            getThemeAPI().setCustomTheme(null, cssContent, type);
        } finally {
            assertEquals(defaultTheme, getThemeAPI().getDefaultTheme(type));
        }
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Set custom", "Wrong parameter" }, jira = "BS-2396, BS-2397")
    @Test(expected = SetThemeException.class)
    public void cantSetCustomThemeWithoutCssContent() throws Exception {
        final ThemeType type = ThemeType.MOBILE;
        final Theme defaultTheme = getThemeAPI().getDefaultTheme(type);

        final byte[] content = "plop".getBytes();
        try {
            getThemeAPI().setCustomTheme(content, null, type);
        } finally {
            assertEquals(defaultTheme, getThemeAPI().getDefaultTheme(type));
        }
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Set custom", "Wrong parameter" }, jira = "BS-2396, BS-2397")
    @Test(expected = SetThemeException.class)
    public void cantSetCustomThemeWithoutType() throws Exception {
        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        getThemeAPI().setCustomTheme(content, cssContent, null);
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Restore default", "Custom theme" }, jira = "BS-2396, BS-2397")
    @Test
    public void restoreDefaultTheme() throws Exception {
        final ThemeType type = ThemeType.MOBILE;
        final Theme defaultTheme = getThemeAPI().getDefaultTheme(type);

        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        getThemeAPI().setCustomTheme(content, cssContent, type);

        final Theme restoreDefaultTheme = getThemeAPI().restoreDefaultTheme(type);
        assertEquals(defaultTheme, restoreDefaultTheme);
        assertEquals(defaultTheme, getThemeAPI().getDefaultTheme(type));
        assertEquals(defaultTheme, getThemeAPI().getCurrentTheme(type));
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Restore default", "No custom theme" }, jira = "BS-2396, BS-2397")
    @Test
    public void restoreDefaultThemeIfNoExistingCustomTheme() throws Exception {
        final ThemeType type = ThemeType.MOBILE;
        final Theme defaultTheme = getThemeAPI().getDefaultTheme(type);

        final Theme restoreDefaultTheme = getThemeAPI().restoreDefaultTheme(type);
        assertEquals(defaultTheme, restoreDefaultTheme);
        assertEquals(defaultTheme, getThemeAPI().getDefaultTheme(type));
        assertEquals(defaultTheme, getThemeAPI().getCurrentTheme(type));
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Restore default", "Wrong parameter" }, jira = "BS-2396, BS-2397")
    @Test(expected = RestoreThemeException.class)
    public void cantRestoreDefaultThemeWithoutType() throws Exception {
        getThemeAPI().restoreDefaultTheme(null);
    }
}
