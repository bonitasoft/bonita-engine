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
package org.bonitasoft.web.toolkit.client.common.exception;

import org.bonitasoft.web.toolkit.client.common.util.StringUtil;

/**
 * @author Séverin Moussel
 */
public abstract class KnownException extends RuntimeException {

    private static final long serialVersionUID = 4288951779031159740L;

    public KnownException() {
        super();
    }

    public KnownException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public KnownException(final String message) {
        super(message);
    }

    public KnownException(final Throwable cause) {
        super(cause);
    }

    /**
     * Override this method to define a default message.<br />
     * Default message will be the getMessage() result if no other message is defined.
     *
     * @return This method returns the default message of the exception.
     */
    protected String defaultMessage() {
        return "";
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (StringUtil.isBlank(message)) {
            message = defaultMessage();
        }
        return message;
    }
}
