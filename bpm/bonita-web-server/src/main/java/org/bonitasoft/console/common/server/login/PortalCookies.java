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
package org.bonitasoft.console.common.server.login;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.login.filter.TokenGenerator;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;

public class PortalCookies {

    /**
     * set the CSRF security token to the HTTP response as cookie.
     */
    public void addCSRFTokenCookieToResponse(HttpServletRequest request, HttpServletResponse res,
            Object apiTokenFromClient) {
        // set the cookie path to / if the app is deployed at root context
        String defaultCookiePath = StringUtils.isEmpty(request.getContextPath()) ? "/" : request.getContextPath();
        String path = System.getProperty("bonita.csrf.cookie.path", defaultCookiePath);
        invalidatePreviousCSRFTokenCookie(request, res, path);

        Cookie csrfCookie = new Cookie(TokenGenerator.X_BONITA_API_TOKEN, apiTokenFromClient.toString());
        // cookie path can be set via system property.
        // Can be set to '/' when another app is deployed in same server than bonita and want to share csrf cookie
        csrfCookie.setPath(path);
        csrfCookie.setSecure(isCSRFTokenCookieSecure());
        res.addCookie(csrfCookie);
    }

    // when a cookie already exists on a different path than the one expected, we need to invalidate it.
    // since there is no way of knowing the path as it is not sent server-side (getPath return null) we invalidate any cookie found
    // see https://bonitasoft.atlassian.net/browse/BS-15883 and BS-16241
    private void invalidatePreviousCSRFTokenCookie(HttpServletRequest request, HttpServletResponse res, String path) {
        Cookie cookie = getCookie(request, TokenGenerator.X_BONITA_API_TOKEN);
        if (cookie != null) {
            cookie.setMaxAge(0);
            cookie.setValue("");
            res.addCookie(cookie);
            invalidateRootCookie(res, cookie, path);
        }
    }

    //Studio sets the cookie to the path '/' so this is required when using a bunble after a studio in the same browser
    public void invalidateRootCookie(HttpServletResponse res, Cookie cookie, String path) {
        if (!"/".equals(path)) {
            Cookie rootCookie = (Cookie) cookie.clone();
            rootCookie.setPath("/");
            res.addCookie(rootCookie);
        }
    }

    // protected for testing
    public boolean isCSRFTokenCookieSecure() {
        return PropertiesFactory.getSecurityProperties().isCSRFTokenCookieSecure();
    }

    public Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }
}
