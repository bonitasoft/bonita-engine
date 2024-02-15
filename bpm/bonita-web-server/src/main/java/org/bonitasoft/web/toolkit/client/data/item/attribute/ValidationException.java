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
package org.bonitasoft.web.toolkit.client.data.item.attribute;

import java.util.List;

import org.bonitasoft.web.toolkit.client.common.exception.KnownException;

/**
 * @author Séverin Moussel
 */
@SuppressWarnings("serial")
public class ValidationException extends KnownException {

    private final List<ValidationError> errors;

    public ValidationException(final List<ValidationError> errors) {
        super(errorsToMessage(errors));
        this.errors = errors;
    }

    private static String errorsToMessage(final List<ValidationError> errors) {
        final StringBuilder sb = new StringBuilder();
        for (final ValidationError error : errors) {
            sb.append(error.getMessage());
            sb.append("\r\n");
        }

        return sb.toString();
    }

    /**
     * @return the errors
     */
    public List<ValidationError> getErrors() {
        return this.errors;
    }

}
