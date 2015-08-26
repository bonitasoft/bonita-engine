/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.theme;

import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Elias Ricken de Medeiros
 */
public interface ThemeRetriever {

    /**
     * Retrieves the default or current theme for the specific type. If no theme is found for the given information the result will be null
     *
     * @param type
     *        The type of the theme
     * @param isDefault
     *        true if is a default theme; false otherwise
     * @return The current theme for the given type or null if no theme is found;
     * @throws SBonitaReadException
     * @since 7.0.3
     */
    STheme getTheme(SThemeType type, boolean isDefault) throws SBonitaReadException;

}
