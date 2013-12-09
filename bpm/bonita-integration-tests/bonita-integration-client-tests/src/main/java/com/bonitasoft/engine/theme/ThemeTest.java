package com.bonitasoft.engine.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

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

    @Test
    public void setCustomTheme() throws Exception {
        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        final ThemeType type = ThemeType.MOBILE;
        final Theme createdTheme = getThemeAPI().setCustomTheme(content, cssContent, type);

        final Theme themeResult = getThemeAPI().getCurrentTheme(type);
        assertEquals(createdTheme, themeResult);
        assertFalse(createdTheme.isDefault());
    }

    @Test
    public void overrideCustomTheme() throws Exception {
        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        final ThemeType type = ThemeType.MOBILE;
        final Theme createdTheme = getThemeAPI().setCustomTheme(content, cssContent, type);

        final Theme createdTheme2 = getThemeAPI().setCustomTheme(content, cssContent, ThemeType.PORTAL);
        assertEquals(createdTheme.getContent(), createdTheme2.getContent());
        assertEquals(createdTheme.getCssContent(), createdTheme2.getCssContent());
        assertEquals(createdTheme.getId(), createdTheme2.getId());
        assertNotEquals(createdTheme.getType(), createdTheme2.getType());
        assertNotEquals(createdTheme.getLastUpdatedDate(), createdTheme2.getLastUpdatedDate());
    }

    @Test(expected = SetThemeException.class)
    public void cantSetCustomThemeWithoutContent() throws Exception {
        final byte[] cssContent = "cssContent".getBytes();
        final ThemeType type = ThemeType.MOBILE;
        getThemeAPI().setCustomTheme(null, cssContent, type);
    }

    @Test(expected = SetThemeException.class)
    public void cantSetCustomThemeWithoutCssContent() throws Exception {
        final byte[] content = "plop".getBytes();
        final ThemeType type = ThemeType.MOBILE;
        getThemeAPI().setCustomTheme(content, null, type);
    }

    @Test(expected = SetThemeException.class)
    public void cantSetCustomThemeWithoutType() throws Exception {
        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        getThemeAPI().setCustomTheme(content, cssContent, null);
    }

    @Test
    public void restoreDefaultTheme() throws Exception {
        final byte[] content = "plop".getBytes();
        final byte[] cssContent = "cssContent".getBytes();
        final ThemeType type = ThemeType.MOBILE;
        getThemeAPI().setCustomTheme(content, cssContent, type);

        getThemeAPI().restoreDefaultTheme(ThemeType.MOBILE);
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme" }, jira = "BS-2396")
    @Test(expected = RestoreThemeException.class)
    public void restoreDefaultThemeIfNoExistingCustomTheme() throws Exception {
        getThemeAPI().restoreDefaultTheme(ThemeType.MOBILE);
    }

    @Cover(classes = ThemeAPI.class, concept = BPMNConcept.NONE, keywords = { "Theme", "Wrong parameter" }, jira = "BS-2396")
    @Test(expected = RestoreThemeException.class)
    public void cantRestoreDefaultThemeWithoutType() throws Exception {
        getThemeAPI().restoreDefaultTheme(null);
    }
}
