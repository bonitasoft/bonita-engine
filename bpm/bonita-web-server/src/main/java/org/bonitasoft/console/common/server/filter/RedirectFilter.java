/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.auth.AuthenticationManager;

/**
 * Filter that redirects to a URL specified as a parameter in the request.
 * The URL must be relative to the current domain.
 *
 * @author Haroun EL ALAMI
 */
@Slf4j
public class RedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String redirectUrl = httpRequest.getParameter(AuthenticationManager.REDIRECT_URL);
        if (StringUtils.isNoneBlank(redirectUrl)) {
            // avoid redirecting to a different domain
            if (!redirectUrl.contains("//")) {
                httpResponse.sendRedirect(redirectUrl);
                return;
            }
            // If the redirectUrl is not valid
            log.warn("Invalid redirect URL: {}", redirectUrl);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
