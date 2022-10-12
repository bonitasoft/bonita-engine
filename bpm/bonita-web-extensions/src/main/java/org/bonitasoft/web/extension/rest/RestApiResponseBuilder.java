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

import static org.bonitasoft.web.extension.rest.RestApiResponse.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Build a RestApiResponse specifying response body, status and other HTTP attributes.
 */
public class RestApiResponseBuilder {

    protected Serializable response;
    protected int httpStatus;
    protected final Map<String, String> additionalHeaders;
    protected final List<Cookie> additionalCookies;
    protected String characterSet;
    protected String mediaType;

    private int pageIndex = -1;
    private int pageSize = -1;
    private long totalSize = -1;

    public RestApiResponseBuilder() {
        this.httpStatus = DEFAULT_STATUS;
        this.additionalHeaders = new HashMap<>();
        this.additionalCookies = new ArrayList<>();
        this.characterSet = DEFAULT_CHARACTER_SET;
        this.mediaType = DEFAULT_MEDIA_TYPE;
    }

    /**
     * Set the body of the response
     *
     * @param response the response body
     */
    public RestApiResponseBuilder withResponse(Serializable response) {
        this.response = response;
        return this;
    }

    /**
     * Set the HTTP status of the response. By default, OK status (200) is set.
     *
     * @param httpStatus the HTTP status of the response
     * @see HttpServletResponse
     */
    public RestApiResponseBuilder withResponseStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    /**
     * Adds a header in the HTTP response
     *
     * @param name the name of the header to add in the response.
     * @param value the value for this header
     * @see org.apache.http.HttpHeaders
     */
    public RestApiResponseBuilder withAdditionalHeader(String name, String value) {
        additionalHeaders.put(name, value);
        return this;
    }

    /**
     * Adds a cookie to the HTTP response
     *
     * @param cookie the {@link javax.servlet.http.Cookie} to add to the response
     */
    public RestApiResponseBuilder withAdditionalCookie(Cookie cookie) {
        additionalCookies.add(cookie);
        return this;
    }

    /**
     * Set the character set of the HTTP response. By default UTF-8 is set.
     *
     * @param characterSet the name of the character set
     * @see java.nio.charset.Charset
     */
    public RestApiResponseBuilder withCharacterSet(String characterSet) {
        this.characterSet = characterSet;
        return this;
    }

    /**
     * Set the media type of the HTTP response body. By default "application/json" is set.
     *
     * @param mediaType the media type to set.
     * @see <a href="http://www.iana.org/assignments/media-types/media-types.xhtml">Registered media types</a>
     */
    public RestApiResponseBuilder withMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * When returning a paged result, sets the start index and the page size.
     * Setting content range overrides the Content-Range header of the response.
     *
     * @param pageIndex the start index of the returned page.
     * @param pageSize the size of the returned page.
     * @return the {@link RestApiResponseBuilder}
     */
    public RestApiResponseBuilder withContentRange(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * When returning a paged result, sets the start index, the page size and the total size.
     * Setting content range overrides the Content-Range header of the response.
     *
     * @param pageIndex the start index of the returned page.
     * @param pageSize the size of the returned page.
     * @param totalSize the total size of the requested entity.
     * @return the {@link RestApiResponseBuilder}
     */
    public RestApiResponseBuilder withContentRange(int pageIndex, int pageSize, long totalSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
        return this;
    }

    /**
     * @return the RestApiResponse response
     */
    public RestApiResponse build() {
        if (pageIndex >= 0 && pageSize >= 0) {
            additionalHeaders.put("Content-Range",
                    String.format("%s-%s/%s", pageIndex, pageSize, totalSize >= 0 ? totalSize : "*"));
        }
        return new RestApiResponse(response, httpStatus, additionalHeaders, additionalCookies, mediaType, characterSet);
    }

}
