/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class to generate standardized error responses for Spring REST APIs.
 * It encapsulates the logic to create a ResponseEntity with an error message and status.
 */
public class SpringResponseEntityUtils {

    public static ResponseEntity<Object> generateErrorResponse(String exceptionClassName, HttpStatus status,
            String message) {
        return ResponseEntity.status(status).body(new ResponseError("class " + exceptionClassName, message));
    }

    public static ResponseEntity<Object> generateErrorResponse(Exception exception, HttpStatus status) {
        return generateErrorResponse(exception.getClass().getName(), status, exception.getMessage());
    }

    public record ResponseError(String exception, String message) {}
}
