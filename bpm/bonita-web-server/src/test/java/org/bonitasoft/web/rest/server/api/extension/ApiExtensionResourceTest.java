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
package org.bonitasoft.web.rest.server.api.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.console.common.server.page.RestApiRenderer;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.extension.rest.RestApiResponse;
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiExtensionResourceTest {

    public static final String RETURN_VALUE = "{'return':'value'}";
    public static final String GET = "GET";

    @Mock
    private RestApiRenderer restApiRenderer;

    @Mock
    private Request request;

    @Mock
    private Response response;

    @InjectMocks
    @Spy
    ApiExtensionResource apiExtensionResource;

    private Method method;

    @Before
    public void before() throws Exception {
        doReturn(request).when(apiExtensionResource).getRequest();
        doReturn(response).when(apiExtensionResource).getResponse();
        doReturn(new Series<>(Header.class)).when(response).getHeaders();
    }

    @Test
    public void should_handle_return_result() throws Exception {
        //given
        method = new Method(GET);
        doReturn(method).when(request).getMethod();
        final RestApiResponse restApiResponse = new RestApiResponseBuilder().withResponse(RETURN_VALUE).build();
        doReturn(restApiResponse).when(restApiRenderer).handleRestApiCall(any(HttpServletRequest.class),
                any(ResourceExtensionResolver.class));

        //when
        final Representation representation = apiExtensionResource.doHandle();

        //then
        assertThat(representation.getText()).as("should return response").isEqualTo(RETURN_VALUE);
    }

    @Test
    public void should_handle_return_empty_response() throws Exception {
        //given
        method = new Method(GET);
        doReturn(method).when(request).getMethod();
        doReturn(new RestApiResponseBuilder().withResponse("").build()).when(restApiRenderer).handleRestApiCall(
                any(HttpServletRequest.class),
                any(ResourceExtensionResolver.class));

        //when
        final Representation representation = apiExtensionResource.doHandle();

        //then
        assertThat(representation.getText()).as("should return response").isEqualTo("");
    }

    @Test
    public void should_handle_return_no_response() throws Exception {
        //given
        method = new Method(GET);
        doReturn(method).when(request).getMethod();
        doReturn(new RestApiResponseBuilder().build()).when(restApiRenderer).handleRestApiCall(
                any(HttpServletRequest.class),
                any(ResourceExtensionResolver.class));

        //when
        final Representation representation = apiExtensionResource.doHandle();

        //then
        assertThat(representation.getText()).as("should return response").isEqualTo(null);
    }

    @Test
    public void should_handle_return_null() throws Exception {
        //given
        method = new Method(GET);
        doReturn(method).when(request).getMethod();

        doReturn(null).when(restApiRenderer).handleRestApiCall(any(HttpServletRequest.class),
                any(ResourceExtensionResolver.class));

        //when
        final Representation representation = apiExtensionResource.doHandle();

        //then
        assertThat(representation).isNull();
        verify(apiExtensionResource.getResponse()).setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void should_handle_return_500_error_when_exception_is_thrown() throws Exception {
        //given
        method = new Method(GET);
        doReturn(method).when(request).getMethod();
        final BonitaException bonitaException = new BonitaException("error message");
        doThrow(bonitaException).when(restApiRenderer).handleRestApiCall(any(HttpServletRequest.class),
                any(ResourceExtensionResolver.class));

        //when
        final Representation representation = apiExtensionResource.doHandle();

        //then
        assertThat(representation).isNull();
        verify(apiExtensionResource.getResponse()).setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void should_handle_content_range_and_location_with_restlet_representation() throws Exception {
        //given
        String location = "https://documentation.bonitasoft.com/bonita/7.7/";
        method = new Method(GET);
        doReturn(method).when(request).getMethod();
        final RestApiResponse restApiResponse = new RestApiResponseBuilder()
                .withAdditionalHeader(HeaderConstants.HEADER_CONTENT_RANGE, "1-10/100")
                .withAdditionalHeader(HeaderConstants.HEADER_LOCATION, location).build();
        doReturn(restApiResponse).when(restApiRenderer).handleRestApiCall(any(HttpServletRequest.class),
                any(ResourceExtensionResolver.class));
        when(response.getEntity()).thenReturn(new StringRepresentation(""));

        //when
        final Representation representation = apiExtensionResource.doHandle();

        //then
        final Range range = representation.getRange();
        assertThat(range.getIndex()).isEqualTo(1);
        assertThat(range.getSize()).isEqualTo(10);
        assertThat(range.getUnitName()).isNullOrEmpty();
        assertThat(range.getInstanceSize()).isEqualTo(100);

        assertThat(restApiResponse.getAdditionalHeaders().get(HeaderConstants.HEADER_LOCATION)).isEqualTo(location);
    }

    @Test
    public void should_handle_content_disposition_with_restlet_range() throws Exception {
        //given
        method = new Method(GET);
        doReturn(method).when(request).getMethod();
        final RestApiResponse restApiResponse = new RestApiResponseBuilder()
                .withAdditionalHeader(HeaderConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"Mon Fichier.png\"")
                .build();
        doReturn(restApiResponse).when(restApiRenderer).handleRestApiCall(any(HttpServletRequest.class),
                any(ResourceExtensionResolver.class));
        when(response.getEntity()).thenReturn(new StringRepresentation(""));

        //when
        final Representation representation = apiExtensionResource.doHandle();

        //then
        final Disposition disposition = representation.getDisposition();
        assertThat(disposition.getType()).isEqualTo("attachment");
        assertThat(disposition.getFilename()).isEqualTo("Mon Fichier.png");
    }

}
