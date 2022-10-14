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
package org.bonitasoft.web.rest.server.api.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.bonitasoft.web.rest.server.utils.RestletAppBuilder.aTestApp;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.rest.server.utils.FakeResource;
import org.bonitasoft.web.rest.server.utils.FakeResource.FakeService;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.restlet.Application;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;

@RunWith(MockitoJUnitRunner.class)
public class CommonResourceTest extends RestletTest {

    @Mock
    private FakeService fakeService;

    @Mock
    HttpSession httpSession;

    @Override
    protected Application configureApplication() {
        FakeResource resource = spy(new FakeResource(fakeService));
        doReturn(httpSession).when(resource).getHttpSession();
        return aTestApp().attach("/test", resource);
    }

    @Test
    public void getParameterShouldNotVerifyNotNullIfNotMandatory() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        final String parameterName = "any string parameter name";
        final String parameterValue = "some value";
        doReturn(parameterValue).when(spy).getRequestParameter(anyString());

        // when:
        spy.getParameter(parameterName, false);

        // then:
        verify(spy, times(0)).verifyNotNullParameter(parameterValue, parameterName);
    }

    @Test
    public void getParameterShouldVerifyNotNullIfMandatory() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        doReturn(null).when(spy).getRequestParameter(anyString());
        doNothing().when(spy).verifyNotNullParameter(anyObject(), anyString());
        final String parameterName = "any string parameter name";

        // when:
        spy.getParameter(parameterName, true);

        // then:
        verify(spy).verifyNotNullParameter(null, parameterName);
    }

    @Test
    public void getSearchOrderMustRetrieveProperParameter() throws Exception {
        final CommonResource spy = spy(new CommonResource());
        doReturn("dummy sort value").when(spy).getParameter(anyString(), anyBoolean());
        spy.getSearchOrder();

        verify(spy).getParameter(APIServletCall.PARAMETER_ORDER, false);
    }

    @Test
    public void getSearchPageNumberMustRetrieveProperParameter() throws Exception {
        final CommonResource spy = spy(new CommonResource());
        doReturn(new Integer(88)).when(spy).getIntegerParameter(anyString(), anyBoolean());
        spy.getSearchPageNumber();

        verify(spy).getIntegerParameter(APIServletCall.PARAMETER_PAGE, true);
    }

    @Test
    public void getSearchPageSizeMustRetrieveProperParameter() throws Exception {
        final CommonResource spy = spy(new CommonResource());
        doReturn(new Integer(77)).when(spy).getIntegerParameter(anyString(), anyBoolean());
        spy.getSearchPageSize();

        verify(spy).getIntegerParameter(APIServletCall.PARAMETER_LIMIT, true);
    }

    @Test
    public void getSearchTermMustRetrieveProperParameter() throws Exception {
        final CommonResource spy = spy(new CommonResource());
        doReturn("lookFor").when(spy).getParameter(anyString(), anyBoolean());
        spy.getSearchTerm();

        verify(spy).getParameter(APIServletCall.PARAMETER_SEARCH, false);
    }

    @Test
    public void getMandatoryParameterShouldCheckNonNull() throws Exception {
        final CommonResource spy = spy(new CommonResource());
        final String parameterName = "name of the parameter";
        doNothing().when(spy).verifyNotNullParameter(any(Class.class), anyString());
        final String objectInParameterMap = "dummyString";
        doReturn(objectInParameterMap).when(spy).getRequestParameter(anyString());

        spy.getParameter(parameterName, true);

        verify(spy).verifyNotNullParameter(objectInParameterMap, parameterName);
    }

    @Test(expected = APIException.class)
    public void nullMandatoryParameterIsForbidden() throws Exception {
        new CommonResource().verifyNotNullParameter(null, "unused");
    }

    @Test
    public void notNullMandatoryParameterIsForbidden() throws Exception {
        new CommonResource().verifyNotNullParameter(new Object(), "unused");
        // no Exception
    }

    @Test
    public void parseFilterShoulReturnNullIfListIsNull() throws Exception {
        assertThat(new CommonResource().parseFilters(null)).isNull();
    }

    @Test
    public void parseFilterShouldBuildExpectedMap() throws Exception {
        // given:
        final List<String> filters = Arrays.asList("toto=17", "titi='EN_ECHEC'", "task=task=with=equal=in=name");

        // when:
        final Map<String, String> parseFilters = new CommonResource().parseFilters(filters);

        // then:
        assertThat(parseFilters.size()).isEqualTo(3);
        assertThat(parseFilters.get("toto")).isEqualTo("17");
        assertThat(parseFilters.get("titi")).isEqualTo("'EN_ECHEC'");
        assertThat(parseFilters.get("task")).isEqualTo("task=with=equal=in=name");
    }

    @Test
    public void parseFilterWithSpecialCharactersShouldBuildExpectedMap() throws Exception {
        // given:
        final List<String> filters = Arrays.asList("a=b", "c=/d/d,e");

        // when:
        final Map<String, String> parseFilters = new CommonResource().parseFilters(filters);

        // then:
        assertThat(parseFilters.size()).isEqualTo(2);
        assertThat(parseFilters.get("a")).isEqualTo("b");
        assertThat(parseFilters.get("c")).isEqualTo("/d/d,e");
    }

    @Test
    public void parseFilterShouldBuildMapEvenIfNoValueForParam() throws Exception {
        // given:
        final List<String> filters = new ArrayList<>(2);
        filters.add("nomatchingvalue=");

        // when:
        final Map<String, String> parseFilters = new CommonResource().parseFilters(filters);

        // then:
        assertThat(parseFilters.size()).isEqualTo(1);
        assertThat(parseFilters.get("nomatchingvalue")).isNull();
    }

    @Test
    public void getIntegerParameterShouldReturnNullIfgetParameterReturnsNull() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        doReturn(null).when(spy).getParameter(anyString(), anyBoolean());

        // then:
        assertThat(spy.getIntegerParameter("", false)).isNull();
        assertThat(spy.getIntegerParameter("", true)).isNull();
    }

    @Test
    public void getLongParameterShouldReturnNullIfgetParameterReturnsNull() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        doReturn(null).when(spy).getParameter(anyString(), anyBoolean());

        // then:
        assertThat(spy.getLongParameter("", false)).isNull();
        assertThat(spy.getLongParameter("", true)).isNull();
    }

    @Test
    public void buildSearchOptionsShouldCallAllGetxxxSearchParameterMethods() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        doReturn(null).when(spy).getSearchFilters();
        doReturn(null).when(spy).getSearchOrder();
        doReturn(0).when(spy).getSearchPageNumber();
        doReturn(10).when(spy).getSearchPageSize();
        doReturn(null).when(spy).getSearchTerm();

        // when:
        spy.buildSearchOptions();

        // then:
        verify(spy).getSearchFilters();
        verify(spy).getSearchOrder();
        verify(spy).getSearchPageNumber();
        verify(spy).getSearchPageSize();
        verify(spy).getSearchTerm();
    }

    @Test
    public void getQueryParameter_return_value() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        final String parameterName = APIServletCall.PARAMETER_QUERY;
        final String parameterValue = "some value";
        doReturn(parameterValue).when(spy).getRequestParameter(parameterName);

        // when:
        final String mandatoryParameter = spy.getQueryParameter(true);
        final String optionalParameter = spy.getQueryParameter(false);

        // then:
        assertThat(mandatoryParameter).as("should return mandatory parameter").isEqualTo(parameterValue);
        assertThat(mandatoryParameter).as("should return optional parameter").isEqualTo(parameterValue);
        assertThat(mandatoryParameter).as("should be equals").isEqualTo(optionalParameter);
        verify(spy, times(1)).verifyNotNullParameter(parameterValue, parameterName);
    }

    @Test
    public void getQueryParameter_return_null() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        doReturn(null).when(spy).getRequestParameter(anyString());

        // when:
        final String parameter = spy.getQueryParameter(false);

        // then:
        assertThat(parameter).as("should return null").isNull();
        verify(spy, times(0)).verifyNotNullParameter(anyString(), anyString());
    }

    @Test(expected = APIException.class)
    public void getQueryParameter_throws_Exception() throws Exception {
        // given:
        final CommonResource spy = spy(new CommonResource());
        doReturn(null).when(spy).getRequestParameter(anyString());

        // when then exception
        spy.getQueryParameter(true);
    }

    @Test
    public void should_respond_400_bad_request_if_IllegalArgumentException_occurs() throws Exception {
        when(fakeService.saySomething()).thenThrow(new IllegalArgumentException("an error message"));

        final Response response = request("/test").get();

        assertThat(response).hasStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        assertThat(response).hasJsonEntityEqualTo(
                "{\"exception\":\"class java.lang.IllegalArgumentException\",\"message\":\"an error message\"}'");
    }

    @Test
    public void should_respond_401_unauthorized_if_InvalidSessionException_occurs() throws Exception {
        when(fakeService.saySomething()).thenThrow(new InvalidSessionException("an error message"));

        final Response response = request("/test").get();

        assertThat(response).hasStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        assertThat(response).hasJsonEntityEqualTo(
                "{\"exception\":\"class org.bonitasoft.engine.session.InvalidSessionException\",\"message\":\"an error message\"}'");
    }

    @Test
    public void getParametersAsList_should_support_value_with_comma() throws Exception {
        //given
        final CommonResource resource = spy(new CommonResource());
        final Form form = new Form("f=a=b&f=c=d,e");
        given(resource.getQuery()).willReturn(form);

        //when
        final List<String> parametersValues = resource.getParameterAsList("f");

        //then
        assertThat(parametersValues).containsExactly("a=b", "c=d,e");
    }

    @Test
    public void getParametersAsList_should_support_values_with_slash() throws Exception {
        //given
        final CommonResource resource = spy(new CommonResource());
        final Form form = new Form("f=a%3Db&f=c%3D%2Fd%2Fd%2Ce");
        given(resource.getQuery()).willReturn(form);

        //when
        final List<String> parametersValues = resource.getParameterAsList("f");

        //then
        assertThat(parametersValues).containsExactly("a=b", "c=/d/d,e");
    }

    @Test
    public void getParametersAsList_should_return_emptyList_when_parameter_does_not_exist() throws Exception {
        //given
        final CommonResource resource = spy(new CommonResource());
        final Form form = new Form("f=a=b&f=c=d,e");
        given(resource.getQuery()).willReturn(form);

        //when
        final List<String> parametersValues = resource.getParameterAsList("anyAbsent");

        //then
        assertThat(parametersValues).isEmpty();
    }

}
