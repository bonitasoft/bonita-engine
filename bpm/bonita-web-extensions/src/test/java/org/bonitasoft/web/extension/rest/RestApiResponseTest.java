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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

public class RestApiResponseTest {

    RestApiResponseBuilder restApiResponseBuilder;

    @Before
    public void setUp() {
        restApiResponseBuilder = new RestApiResponseBuilder();
    }

    @Test
    public void testGetResponse_with_default_values() {
        //when
        final RestApiResponse response = restApiResponseBuilder.withResponse("response").build();

        //then
        RestApiResponseAssert.assertThat(response)
                .hasResponse("response")
                .hasMediaType("application/json")
                .hasHttpStatus(200)
                .hasCharacterSet("UTF-8")
                .hasNoAdditionalCookies();

        assertThat(response.getAdditionalHeaders()).isEmpty();
    }

    @Test
    public void testGetHttpStatus() {
        //when
        final RestApiResponse response = restApiResponseBuilder.withResponseStatus(404).build();

        //then
        RestApiResponseAssert.assertThat(response).hasHttpStatus(404);

    }

    @Test
    public void testGetAdditionalHeaders() {
        //when
        final RestApiResponse response = restApiResponseBuilder.withAdditionalHeader("x-header1", "value1")
                .withAdditionalHeader("x-header2", "value2")
                .build();

        //then
        assertThat(response.getAdditionalHeaders()).hasSize(2).contains(entry("x-header1", "value1"),
                entry("x-header1", "value1"));
    }

    @Test
    public void testGetAdditionalCookies() {
        //when
        final Cookie cookie1 = new Cookie("name1", "value1");
        final Cookie cookie2 = new Cookie("name2", "value2");

        final RestApiResponse response = restApiResponseBuilder.withAdditionalCookie(cookie1)
                .withAdditionalCookie(cookie2)
                .build();

        //then
        RestApiResponseAssert.assertThat(response).hasAdditionalCookies(cookie1, cookie2);
    }

    @Test
    public void testGetCharacterSet() {
        //given
        final String characterSet = "utf-16";

        //when
        final RestApiResponse response = restApiResponseBuilder.withCharacterSet(characterSet)
                .build();

        //then
        RestApiResponseAssert.assertThat(response).hasCharacterSet(characterSet);
    }

    @Test
    public void testGetMediaType() {
        //when
        final String mediaType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        //when
        final RestApiResponse response = restApiResponseBuilder.withMediaType(mediaType)
                .build();

        //then
        RestApiResponseAssert.assertThat(response).hasMediaType(mediaType);
    }

    @Test
    public void should_not_set_range_when_pageSize_not_set() {
        //when
        final RestApiResponse response = restApiResponseBuilder.withContentRange(0, -1)
                .build();

        //then
        assertThat(response.getAdditionalHeaders()).doesNotContainKey("Content-Range");
    }

    @Test
    public void should_not_set_range_when_pageIndex_not_set() {
        //when
        final RestApiResponse response = restApiResponseBuilder.withContentRange(-1, 10)
                .build();

        //then
        assertThat(response.getAdditionalHeaders()).doesNotContainKey("Content-Range");
    }

    @Test
    public void should_set_range_when_pageIndex_and_pageSize_is_set() {
        //when
        final RestApiResponse response = restApiResponseBuilder
                .withContentRange(0, 10)
                .build();

        //then
        assertThat(response.getAdditionalHeaders()).contains(entry("Content-Range", "0-10/*"));
    }

    @Test
    public void should_set_range_when_pageIndex_and_pageSize_and_totalSize_is_set() {
        //when
        final RestApiResponse response = restApiResponseBuilder
                .withContentRange(0, 10, 100)
                .build();

        //then
        assertThat(response.getAdditionalHeaders()).contains(entry("Content-Range", "0-10/100"));
    }

}
