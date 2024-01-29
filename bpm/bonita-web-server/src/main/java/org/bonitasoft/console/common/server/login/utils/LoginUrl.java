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
package org.bonitasoft.console.common.server.login.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;

/**
 * @author Vincent Elcrin
 */
public class LoginUrl {

    private final String location;

    /**
     * @throws ServletException
     */
    public LoginUrl(final AuthenticationManager authenticationManager, final String redirectUrl,
            final HttpServletRequestAccessor request) throws ServletException {
        location = getLoginPageUrl(authenticationManager, redirectUrl, request);
    }

    public String getLocation() {
        return location;
    }

    private String getLoginPageUrl(final AuthenticationManager authenticationManager, final String redirectURL,
            final HttpServletRequestAccessor request)
            throws ServletException {
        return authenticationManager.getLoginPageURL(request, URLEncoder.encode(redirectURL, StandardCharsets.UTF_8));
    }

}
