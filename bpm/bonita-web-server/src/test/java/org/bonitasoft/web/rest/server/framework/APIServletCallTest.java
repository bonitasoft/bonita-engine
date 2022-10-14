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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIIncorrectIdException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class APIServletCallTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private APISession apiSession;
    @Mock
    private HttpSession httpSession;
    @Mock
    private API api;

    @Spy
    private final APIServletCall apiServletCall = new APIServletCall();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() {
        doReturn(httpSession).when(request).getSession();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        doReturn(false).when(apiSession).isTechnicalUser();
        doReturn("john").when(apiSession).getUserName();
        apiServletCall.api = api;
    }

    @Test
    public void should_parsePath_request_info_with_id() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("API/bpm/case/15").when(request).getPathInfo();

        apiServletCall.parsePath(request);

        assertThat(apiServletCall.getId().getPart(0)).isEqualTo("15");
        assertThat(apiServletCall.getResourceName()).isEqualTo("case");
        assertThat(apiServletCall.getApiName()).isEqualTo("bpm");

    }

    @Test
    public void should_parsePath_request_info_with_negative_id() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        doReturn("API/identity/user/-1").when(request).getPathInfo();
        thrown.expect(APIIncorrectIdException.class);
        thrown.expectMessage("Id must be non-zero positive for identity on resource user");

        apiServletCall.parsePath(request);
    }

    @Test
    public void doGet_On_Search_Should_Set_Content_Range_Headers_Correctly() throws Exception {
        doReturn(new ArrayList<String>()).when(apiServletCall).getParameterAsList("d");
        doReturn(new ArrayList<String>()).when(apiServletCall).getParameterAsList("n");
        doReturn("0").when(apiServletCall).getParameter("p");
        doReturn("0").when(apiServletCall).getParameter("c");
        doReturn("id ASC").when(apiServletCall).getParameter("o");
        doReturn("").when(apiServletCall).getParameter("s");
        doReturn(null).when(apiServletCall).getParameterAsList("f");
        doReturn(new ArrayList<String>()).when(apiServletCall).getParameterAsList("d");

        doNothing().when(apiServletCall).head(anyString(), anyString());
        doNothing().when(apiServletCall).output(any(List.class));
        doReturn(2).when(apiServletCall).countParameters();

        final ItemSearchResult itemSearchResult = mock(ItemSearchResult.class);
        when(itemSearchResult.getPage()).thenReturn(4);
        when(itemSearchResult.getLength()).thenReturn(8);
        when(itemSearchResult.getTotal()).thenReturn(789L);
        when(api.runSearch(anyInt(), anyInt(), anyString(), anyString(), any(Map.class), any(List.class),
                any(List.class))).thenReturn(itemSearchResult);

        apiServletCall.doGet();
        verify(apiServletCall).head(anyString(), anyString());
    }

}
