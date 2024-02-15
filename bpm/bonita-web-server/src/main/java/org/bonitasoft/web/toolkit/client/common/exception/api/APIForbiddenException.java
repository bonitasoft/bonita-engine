/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.toolkit.client.common.exception.api;

import org.bonitasoft.web.toolkit.client.common.i18n.T_;

/**
 * @author Vincent Elcrin
 */
public class APIForbiddenException extends APIException {

    private static final long serialVersionUID = 4021139564084425851L;

    public APIForbiddenException(final String message) {
        super(message);
    }

    public APIForbiddenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public APIForbiddenException(T_ localizedMessage) {
        super(localizedMessage);
    }

    public APIForbiddenException(T_ localizedMessage, Throwable cause) {
        super(localizedMessage, cause);
    }
}
