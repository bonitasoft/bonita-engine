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
package org.bonitasoft.engine.api.impl;

import java.util.Date;

import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeType;

/**
 * @author Celine Souchet
 * @author Laurent Leseigneur
 * @deprecated since 7.13.0, this API does nothing. There is no replacement, as it used to serve old removed feature.
 *             This API will be removed in a future version.
 */
@Deprecated(since = "7.13.0")
@AvailableWhenTenantIsPaused
public class ThemeAPIImpl implements ThemeAPI {

    @Override
    public Theme getCurrentTheme(final ThemeType type) {
        return null;
    }

    @Override
    public Theme getDefaultTheme(final ThemeType type) {
        return null;
    }

    @Override
    public Date getLastUpdateDate(final ThemeType type) {
        return null;
    }

}
