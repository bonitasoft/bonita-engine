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
package org.bonitasoft.console.common.server.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * @author Vincent Elcrin, Julien Reboul
 */
public class UrlBuilder {

    private final URIBuilder uriBuilder;

    private final List<NameValuePair> parameters = new LinkedList<>();

    public UrlBuilder(final String urlString) {
        URI uri;
        try {
            uri = new URI(urlString);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        uriBuilder = new URIBuilder(uri);
        uriBuilder.setCharset(Consts.UTF_8);
        if (uri.getRawQuery() != null) {
            //avoid NPE thrown by httpClient 4.5.2 regression
            parameters.addAll(URLEncodedUtils.parse(uri.getRawQuery(), StandardCharsets.UTF_8));
        }
    }

    public void appendParameter(final String key, final String... values) {
        if (!isParameterAlreadyDefined(parameters, key)) {
            parameters.add(new BasicNameValuePair(key, new UrlValue(values).toString()));
        }
    }

    public void removeParameter(final String key) {
        parameters.removeIf(param -> param.getName().equals(key));
    }

    private boolean isParameterAlreadyDefined(List<NameValuePair> params, String key) {
        for (NameValuePair param : params) {
            if (param.getName().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public void appendParameters(final Map<String, String[]> parameters) {
        for (Entry<String, String[]> next : parameters.entrySet()) {
            appendParameter(next.getKey(),
                    new UrlValue(next.getValue()).toString());
        }
    }

    public String build() {
        if (!parameters.isEmpty()) {
            uriBuilder.setParameters(parameters);
        } else {
            //in case the parameters list is an empty list setParameters does not remove the question mark
            //removeQuery does remove the question mark in the URL
            uriBuilder.removeQuery();
        }
        return uriBuilder.toString();
    }
}
