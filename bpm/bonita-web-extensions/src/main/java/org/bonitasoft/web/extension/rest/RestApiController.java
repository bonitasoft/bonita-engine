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
package org.bonitasoft.web.extension.rest;

import javax.servlet.http.HttpServletRequest;

/**
 * The interface to implement for Rest API extension in Bonita.
 */
public interface RestApiController {

    /**
     * Let the Rest API Extension parse request for specific attribute handling.
     *
     * @param request the HTTP servlet request intended to be used as in a servlet
     * @param responseBuilder a builder for HTTP response
     * @param context to access the current execution context data like current session,locale...
     * @return a RestApiResponse with a body, an HTTP status and other optional http content
     * @since 7.2
     */
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder,
            RestAPIContext context);

}
