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
package org.bonitasoft.web.rest.server.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIIncorrectIdException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class APIServletCallTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletMapping httpServletMapping;
    @Mock
    private API api;

    @Spy
    private final APIServletCall apiServletCall = new APIServletCall();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        doReturn(httpServletMapping).when(request).getHttpServletMapping();
        apiServletCall.api = api;
    }

    @Test
    public void should_parsePath_request_info_with_id() {
        doReturn("/bpm/case/15").when(request).getPathInfo();

        apiServletCall.parsePath(request);

        assertThat(apiServletCall.getId().getPart(0)).isEqualTo("15");
        assertThat(apiServletCall.getResourceName()).isEqualTo("case");
        assertThat(apiServletCall.getApiName()).isEqualTo("bpm");

    }

    @Test
    public void should_parsePath_request_info_with_negative_id() {
        doReturn("/identity/user/-1").when(request).getPathInfo();
        thrown.expect(APIIncorrectIdException.class);
        thrown.expectMessage("Id must be non-zero positive for identity on resource user");

        apiServletCall.parsePath(request);
    }

    @Test
    public void doGet_On_Search_Should_Set_Content_Range_Headers_Correctly() throws Exception {
        String parameterSearchValue = "";
        String parameterPageValue = "10";
        String parameterLimitValue = "0";
        String parameterOrderValue = "id ASC";
        List<String> parameterDeployValue = new ArrayList<>();
        List<String> parameterCounterValue = new ArrayList<>();

        doReturn(parameterDeployValue).when(apiServletCall).getParameterAsList("d");
        doReturn(parameterCounterValue).when(apiServletCall).getParameterAsList("n");
        doReturn(parameterPageValue).when(apiServletCall).getParameter("p", "0");
        doReturn(parameterLimitValue).when(apiServletCall).getParameter("c", "10");
        doReturn(parameterSearchValue).when(apiServletCall).getParameter("s");
        doReturn(parameterOrderValue).when(apiServletCall).getParameter("o");
        doReturn(new ArrayList<>()).when(apiServletCall).getParameterAsList("f");

        doNothing().when(apiServletCall).head(anyString(), anyString());
        doNothing().when(apiServletCall).output(any(List.class));
        doReturn(2).when(apiServletCall).countParameters();

        final ItemSearchResult itemSearchResult = mock(ItemSearchResult.class);
        when(itemSearchResult.getPage()).thenReturn(4);
        when(itemSearchResult.getLength()).thenReturn(8);
        when(itemSearchResult.getTotal()).thenReturn(789L);

        when(api.runSearch(Integer.parseInt(parameterPageValue), Integer.parseInt(parameterLimitValue),
                parameterSearchValue, parameterOrderValue, new HashMap<>(), parameterDeployValue,
                parameterCounterValue)).thenReturn(itemSearchResult);

        apiServletCall.doGet();
        verify(api, times(1)).runSearch(Integer.parseInt(parameterPageValue), Integer.parseInt(parameterLimitValue),
                parameterSearchValue, parameterOrderValue, new HashMap<>(), parameterDeployValue,
                parameterCounterValue);
        verify(apiServletCall).head("Content-Range", 4 + "-" + 8 + "/" + 789L);
    }

}
