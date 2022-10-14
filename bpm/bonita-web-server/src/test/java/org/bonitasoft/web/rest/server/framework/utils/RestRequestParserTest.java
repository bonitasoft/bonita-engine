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
package org.bonitasoft.web.rest.server.framework.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIMalformedUrlException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestRequestParserTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private RestRequestParser restRequestParser;

    @Test
    public void should_parsePath_request_info_with_id() {
        doReturn("API/bpm/case/15").when(httpServletRequest).getPathInfo();

        restRequestParser.invoke();

        assertThat(restRequestParser.getResourceQualifiers().getPart(0)).isEqualTo("15");
        assertThat(restRequestParser.getResourceName()).isEqualTo("case");
        assertThat(restRequestParser.getApiName()).isEqualTo("bpm");

    }

    @Test
    public void should_parsePath_request_info() {
        doReturn("API/bpm/case").when(httpServletRequest).getPathInfo();

        restRequestParser.invoke();

        assertThat(restRequestParser.getResourceQualifiers()).isNull();
        assertThat(restRequestParser.getResourceName()).isEqualTo("case");
        assertThat(restRequestParser.getApiName()).isEqualTo("bpm");

    }

    @Test(expected = APIMalformedUrlException.class)
    public void should_parsePath_with_bad_request() {
        doReturn("API/bpm").when(httpServletRequest).getPathInfo();
        doReturn("/API").when(httpServletRequest).getServletPath();

        restRequestParser.invoke();
    }
}
