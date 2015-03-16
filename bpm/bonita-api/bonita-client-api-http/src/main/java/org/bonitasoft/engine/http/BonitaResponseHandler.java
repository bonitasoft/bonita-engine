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
package org.bonitasoft.engine.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * @author Julien Mege
 * @author Matthieu Chaffotte
 */
public class BonitaResponseHandler extends BasicResponseHandler {

    public static final String REST_SERVER_EXCEPTION = "REST_SERVER_EXCEPTION";

    /**
     * Returns the response body as a String if the response was successful (a
     * 2xx status code). If no response body exists, this returns null. If the
     * response was unsuccessful (&gt;= 300 status code), throws an {@link org.apache.http.client.HttpResponseException}.
     */
    @Override
    public String handleResponse(final HttpResponse response) throws HttpResponseException, IOException {
        final HttpEntity entity = response.getEntity();
        final StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }

        return entity == null ? null : EntityUtils.toString(entity);
    }

}
