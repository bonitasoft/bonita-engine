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
package org.bonitasoft.web.rest.server.api.resource;

import static org.bonitasoft.web.toolkit.client.common.json.JSonUtil.escape;

import org.restlet.ext.jackson.JacksonRepresentation;

/**
 * Representation for error entity
 *
 * @author Colin Puy
 */
public class ErrorMessage {

    private String exception; // might be 'type' with simple name of exception
    private String message;

    // DO NOT PUT stacktrace, this is not coherent with old API toolkit but as a client of REST API, I do not need stacktrace.

    public ErrorMessage() {
        // empty constructor for json serialization
    }

    public ErrorMessage(final Throwable t) {
        if (t != null) {
            exception = t.getClass().toString();
            setMessage(t.getMessage());
        }
    }

    public String getException() {
        return exception;
    }

    public void setException(final String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = escape(message);
    }

    public JacksonRepresentation<ErrorMessage> toEntity() {
        return new JacksonRepresentation<>(this);
    }
}
