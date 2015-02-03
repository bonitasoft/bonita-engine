/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api;

import java.util.Date;

import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeType;

/**
 * Manage mobile and portal theme. A Theme is a look &amp; feel in Bonita BPM Portal.
 * 
 * @author Celine Souchet
 */
public interface ThemeAPI {

    /**
     * Get the current theme for the specific type.
     * 
     * @param type
     *            The type of the theme
     * @return The theme
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs while retrieving the theme
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme getCurrentTheme(ThemeType type);

    /**
     * Get the default theme for the specific type.
     * 
     * @param type
     *            The type of the theme
     * @return The theme
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs while retrieving the theme
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Theme getDefaultTheme(ThemeType type);

    /**
     * Get the last updated date of the current theme for the specific type.
     * 
     * @param type
     *            The type of theme
     * @return The last updated date of the theme
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.2
     */
    Date getLastUpdateDate(ThemeType type);

}
