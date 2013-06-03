/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.document.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider;

/**
 * {@link StandardAuthenticationProvider} with user added in header
 * must be replaced by the CookieAwareStandardAuthenticationProvider when the 0.4.0 version of OpenCMIS is released
 * This is used by the {@link AuthAwareCookieManager} to have 1 cookie by user
 */
public class StandardAuthenticationProviderWithUserInHeaders extends StandardAuthenticationProvider {

    /**
   * 
   */
    private static final long serialVersionUID = -5776259912869880291L;

    /**
   * 
   */
    public static final String USER_FOR_COOKIE = "user_for_cookie";

    @Override
    public Map<String, List<String>> getHTTPHeaders(final String url) {
        Map<String, List<String>> headers = super.getHTTPHeaders(url);
        if (headers == null) {
            headers = new HashMap<String, List<String>>();
        }
        headers.put(USER_FOR_COOKIE, Collections.singletonList(getUser()));
        return headers;
    }

}
