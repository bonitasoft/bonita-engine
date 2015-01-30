/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeType;

import com.bonitasoft.engine.theme.exception.RestoreThemeException;
import com.bonitasoft.engine.theme.exception.SetThemeException;

/**
 * Manage mobile and portal theme
 * 
 * @author Celine Souchet
 */
public interface ThemeAPI extends org.bonitasoft.engine.api.ThemeAPI {

    /**
     * Update a custom theme, if it exists; otherwise create it.
     * 
     * @param content
     *            The zip file associated with this theme, as a binary content.
     * @param cssContent
     *            The CSS file associated with this theme, as a binary content.
     * @param type
     *            The type of the theme
     * @return The current theme
     * @throws SetThemeException
     *             If can't set theme
     * @throws InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme setCustomTheme(byte[] content, byte[] cssContent, ThemeType type) throws SetThemeException;

    /**
     * Restore the default theme. Delete the custom theme.
     * 
     * @param type
     *            The type of the theme to restore
     * @return The default theme
     * @throws RestoreThemeException
     *             If can't restore default theme
     * @since 6.2
     */
    Theme restoreDefaultTheme(ThemeType type) throws RestoreThemeException;

}
