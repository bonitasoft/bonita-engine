/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.document.api.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Nicolas Chabanoles
 */
public class SDocumentDownloadURLProviderImpl implements SDocumentDownloadURLProvider {

    private final String servletUrl;

    public SDocumentDownloadURLProviderImpl(final String servletUrl) {
        this.servletUrl = servletUrl;
    }

    @Override
    public String generateURL(String documentName, String contentStorageId) {
        final StringBuffer buffer = new StringBuffer(servletUrl);
        buffer.append("?fileName=").append(getUrlEncodedValue(documentName));
        buffer.append("&contentStorageId=").append(contentStorageId);
        return buffer.toString();
    }

    private String getUrlEncodedValue(String documentName) {
        String encoding = "UTF-8";
        try {
            return URLEncoder.encode(documentName, encoding);
        } catch (UnsupportedEncodingException e) {
            // This exception should never occur,
            // as UnsupportedEncodingException is thrown only if the named encoding is not supported
            throw new IllegalStateException("the named encoding " + encoding + " is not supported.", e);
        }
    }
}
