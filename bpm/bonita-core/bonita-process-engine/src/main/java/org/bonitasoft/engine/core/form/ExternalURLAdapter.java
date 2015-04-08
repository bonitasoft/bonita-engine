/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.core.form;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.page.URLAdapter;
import org.bonitasoft.engine.page.URLAdapterConstants;

/**
 * @author Baptiste Mesta, Anthony Birembaut
 */
public class ExternalURLAdapter implements URLAdapter {

    private static final String PARAM_TAG = "?";

    private static final String HASH_TAG = "#";

    @Override
    public String adapt(String url, String key, Map<String, Serializable> context) throws SExecutionException {
        @SuppressWarnings("unchecked")
        final Map<String, String[]> queryParameters = (Map<String, String[]>) context.get(URLAdapterConstants.QUERY_PARAMETERS);
        final String[] hash = url.split(HASH_TAG);
        StringBuffer newURL = new StringBuffer(hash[0]);

        appendParametersToURL(newURL, queryParameters);

        if (url.contains(HASH_TAG)) {
            newURL.append(HASH_TAG);
            if (hash.length > 1) {
                newURL.append(hash[1]);
            }
        }

        return newURL.toString();
    }

    protected void appendParametersToURL(StringBuffer url, final Map<String, String[]> parameters) throws SExecutionException {
        if (parameters != null) {
            for (Entry<String, String[]> parameterEntry : parameters.entrySet()) {
                appendParameterToURL(url, parameterEntry.getKey(), parameterEntry.getValue());
            }
        }
    }

    protected void appendParameterToURL(final StringBuffer newURL, final String key, final String[] value) {
        appendSeparator(newURL, PARAM_TAG);
        newURL.append(key).append("=");
        for (int i = 0; i < value.length; i++) {
            if (i > 0) {
                newURL.append(",");
            }
            newURL.append(value[i]);
        }
    }

    protected void appendSeparator(final StringBuffer buffer, final String initialSeparator) {
        if (buffer.indexOf(initialSeparator) != -1) {
            buffer.append("&");
        } else {
            buffer.append(initialSeparator);
        }
    }

    @Override
    public String getId() {
        return URLAdapterConstants.EXTERNAL_URL_ADAPTER;
    }
}
