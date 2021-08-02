/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api;

import java.util.Date;

import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeType;

/**
 * Manage mobile and portal theme. A Theme is a look &amp; feel in Bonita Portal.
 *
 * @author Celine Souchet
 * @deprecated since 7.13.0, this API does nothing. There is no replacement, as it used to serve old removed feature.
 *             This API will be removed in a future version.
 */
@Deprecated(since = "7.13.0")
public interface ThemeAPI {

    /**
     * Get the current theme for the specific type.
     *
     * @param type
     *        The type of the theme
     * @return The theme
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs while retrieving the theme
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.2
     * @deprecated now does nothing and returns null
     */
    Theme getCurrentTheme(ThemeType type);

    /**
     * Get the default theme for the specific type.
     *
     * @param type
     *        The type of the theme
     * @return The theme
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs while retrieving the theme
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.2
     * @deprecated now does nothing and returns null
     */
    Theme getDefaultTheme(ThemeType type);

    /**
     * Get the last updated date of the current theme for the specific type.
     *
     * @param type
     *        The type of theme
     * @return The last updated date of the theme
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.2
     * @deprecated now does nothing and returns null
     */
    Date getLastUpdateDate(ThemeType type);

}
