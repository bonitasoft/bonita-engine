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
package org.bonitasoft.console.common.server.login.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Paul AMAR
 */
public class URLProtector {

    protected final List<String> tokens = Arrays.asList("https", "http", "www", "HTTPS", "HTTP", "WWW", "//");

    public String protectRedirectUrl(String redirectUrl) {
        if (redirectUrl != null && !redirectUrl.startsWith("portal")) {
            return removeTokenFromUrl(redirectUrl, new ArrayList<>(tokens));
        }
        return redirectUrl;
    }

    private String removeTokenFromUrl(String redirectUrl, List<String> tokens) {
        if (tokens.size() > 0) {
            return removeTokenFromUrl(redirectUrl.replaceAll(tokens.remove(0), ""), tokens);
        }
        return redirectUrl;
    }

}
