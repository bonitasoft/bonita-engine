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
package org.bonitasoft.console.common.server.jsp;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Séverin Moussel
 */
public class JSPUtils {

    private final HttpServletRequest request;

    private final HttpSession session;

    public JSPUtils(final HttpServletRequest request, final HttpSession session) {
        super();
        this.request = request;
        this.session = session;
    }

    public String getSessionOrCookie(final String name, final String defaultValue) {

        // Try reading in session
        String result = (String) this.session.getAttribute(name);

        // else, try reading in cookies
        if (result == null) {
            final Cookie[] cookies = this.request.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals(name)) {
                        result = cookies[i].getValue();
                        break;
                    }
                }
            }
        }

        // else, set default value
        if (result == null) {
            result = defaultValue;
        }

        return result;
    }

    public String getParameter(final String name, final String defaultValue) {
        String result = this.request.getParameter(name);

        if (result == null) {
            result = defaultValue;
        }

        return result;
    }

    public String getParameter(final String name) {
        return this.getParameter(name, null);
    }

    public HttpServletRequest getRequest() {
        return this.request;
    }

    public boolean getParameter(final String name, final boolean defaultValue) {
        final String result = this.request.getParameter(name);

        if (result != null) {
            try {

                // If boolean is passed as an integer
                final int intValue = Integer.parseInt(result);
                return intValue > 0;

            } catch (final NumberFormatException e) {

                // If boolean is passed as a recognized string
                if ("false".equalsIgnoreCase(result) || "no".equals(result)) {
                    return false;
                }
                if ("true".equalsIgnoreCase(result) || "yes".equals(result)) {
                    return false;
                }
            }
        }

        return defaultValue;
    }

    public int getParameter(final String name, final int defaultValue) {
        final String result = this.request.getParameter(name);

        if (result != null) {
            try {

                // If boolean is passed as an integer
                return Integer.parseInt(result);

            } catch (final NumberFormatException e) {
                // DO nothing
            }
        }

        return defaultValue;
    }

}
