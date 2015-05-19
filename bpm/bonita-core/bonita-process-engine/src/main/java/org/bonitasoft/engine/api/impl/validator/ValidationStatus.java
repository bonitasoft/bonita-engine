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

package org.bonitasoft.engine.api.impl.validator;

/**
 * @author Elias Ricken de Medeiros
 */
public class ValidationStatus {

    boolean valid;

    private String message;

    private ErrorType errorType;

    public enum ErrorType {
        INVALID_CHARACTER,

        RESERVED_KEY_WORD,

        NONE
    }

    public ValidationStatus(final boolean valid) {
        this.valid = valid;
        errorType = valid? ErrorType.NONE : ErrorType.INVALID_CHARACTER;
    }

    public ValidationStatus(boolean valid, final String message, final ErrorType errorType) {
        this.valid = valid;
        this.message = message;
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        return valid;
    }
}
