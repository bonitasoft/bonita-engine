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
package org.bonitasoft.web.toolkit.client.common.exception.http;

import org.bonitasoft.web.toolkit.client.common.exception.KnownException;
import org.bonitasoft.web.toolkit.client.common.json.JsonSerializable;

/**
 * @author Séverin Moussel
 */
public class HttpException extends KnownException implements JsonSerializable {

    private static final long serialVersionUID = 4351214615157802308L;

    protected int statusCode = 503;

    public HttpException() {
        super();
    }

    public HttpException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public HttpException(final String message) {
        super(message);
    }

    public HttpException(final Throwable cause) {
        super(cause);
    }

    /**
     * @return the statusCode
     */
    public final int getStatusCode() {
        return this.statusCode;
    }

    /**
     * @param statusCode
     *        the statusCode to set
     */
    public final HttpException setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public final String toJson() {
        return buildJson().end();
    }

    protected JsonExceptionSerializer buildJson() {
        return new JsonExceptionSerializer(this);
    }

    @Override
    protected String defaultMessage() {
        return "The connection has encountered an unknown error";
    }

}
