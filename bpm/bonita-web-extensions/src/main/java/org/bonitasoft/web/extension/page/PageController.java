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
package org.bonitasoft.web.extension.page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The interface to implement for a Custom Page in Bonita.
 */
public interface PageController {

    /**
     * Let the custom page parse request for specific attribute handling.
     *
     * @param request
     *        the HTTP servlet request intended to be used as in a servlet
     * @param response
     *        the HTTP servlet response intended to be used as in a servlet
     * @param pageResourceProvider
     *        provide access to the resources contained in the custom page zip
     * @param pageContext
     *        provide access to the data relative to the context in which the custom page is displayed
     */
    void doGet(HttpServletRequest request, HttpServletResponse response, PageResourceProvider pageResourceProvider,
            PageContext pageContext);

}
