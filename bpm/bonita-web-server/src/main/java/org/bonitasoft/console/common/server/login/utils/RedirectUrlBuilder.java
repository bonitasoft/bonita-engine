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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.utils.UrlBuilder;

/**
 * @author Vincent Elcrin
 */
public class RedirectUrlBuilder {

    private final UrlBuilder urlBuilder;

    private final List<String> blackList = List.of(
            AuthenticationManager.REDIRECT_URL);

    public RedirectUrlBuilder(final String redirectUrl) {
        urlBuilder = new UrlBuilder(redirectUrl != null ? redirectUrl : "");
    }

    public RedirectUrl build() {
        return new RedirectUrl(urlBuilder.build());

    }

    public void appendParameters(final Map<String, String[]> parameters) {
        for (final Entry<String, String[]> next : parameters.entrySet()) {
            appendParameter(next.getKey(), next.getValue());
        }
    }

    public void appendParameter(String name, String... values) {
        if (!isBlackListed(name)) {
            urlBuilder.appendParameter(name, values);
        }
    }

    public void removeParameter(String name) {
        urlBuilder.removeParameter(name);
    }

    private boolean isBlackListed(final String key) {
        return blackList.contains(key);
    }

}
