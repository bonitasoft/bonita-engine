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
package org.bonitasoft.engine.core.document.api.impl;

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
        buffer.append("?fileName=").append(documentName);
        buffer.append("&contentStorageId=").append(contentStorageId);
        return buffer.toString();
    }

}
