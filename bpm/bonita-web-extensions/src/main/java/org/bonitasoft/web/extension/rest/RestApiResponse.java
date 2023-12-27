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
package org.bonitasoft.web.extension.rest;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

/**
 * A Wrapper of an HTTP Response describe by its:
 * <ul>
 * <li>Status</li>
 * <li>Headers</li>
 * <li>Body</li>
 * <li>Charset</li>
 * <li>Media type</li>
 * <li>Cookies</li>
 * </ul>
 */
public class RestApiResponse {

    /**
     * default http status code
     */
    public static final int DEFAULT_STATUS = 200;

    /**
     * default character set
     */
    public static final String DEFAULT_CHARACTER_SET = Charset.forName("UTF-8").name();

    /**
     * default media type
     */
    public static final String DEFAULT_MEDIA_TYPE = "application/json";

    private final Serializable response;
    private final int httpStatus;
    private final Map<String, String> additionalHeaders;
    private final List<Cookie> additionalCookies;
    private final String mediaType;
    private final String characterSet;

    public RestApiResponse(Serializable response, int httpStatus, Map<String, String> additionalHeaders,
            List<Cookie> additionalCookies, String mediaType,
            String characterSet) {
        this.response = response;
        this.httpStatus = httpStatus;
        this.additionalHeaders = additionalHeaders;
        this.additionalCookies = additionalCookies;
        this.mediaType = mediaType;
        this.characterSet = characterSet;
    }

    public Serializable getResponse() {
        return response;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }

    public List<Cookie> getAdditionalCookies() {
        return additionalCookies;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public String getMediaType() {
        return mediaType;
    }

}
