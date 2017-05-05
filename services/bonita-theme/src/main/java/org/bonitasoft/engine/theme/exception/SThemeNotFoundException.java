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
package org.bonitasoft.engine.theme.exception;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Celine Souchet
 */
public class SThemeNotFoundException extends SBonitaException {

    private static final long serialVersionUID = -6787110496892352279L;

    public SThemeNotFoundException(final String message) {
        super(message);
    }

    public SThemeNotFoundException(final Throwable cause) {
        super(cause);
    }

    public SThemeNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SThemeNotFoundException(final long themeId) {
        super("No theme exists with id = <" + themeId + ">");
    }

    public SThemeNotFoundException(final SThemeType type) {
        super("No theme exists with type = " + type);
    }

    public SThemeNotFoundException(final SThemeType type, final boolean isDefault) {
        super("No theme exists with type = " + type + ", and isDefault = " + isDefault);
    }

}
