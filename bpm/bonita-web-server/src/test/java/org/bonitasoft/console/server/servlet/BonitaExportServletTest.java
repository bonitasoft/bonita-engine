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
package org.bonitasoft.console.server.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class BonitaExportServletTest {

    @Mock
    HttpServletRequest hsRequest;

    @Mock
    HttpServletResponse hsResponse;

    @Mock
    HttpSession httpSession;

    @Mock
    private ApplicationAPI applicationAPI;

    @Spy
    private ApplicationsExportServlet spiedApplicationsExportServlet;

    @BeforeClass
    public static void initEnvironnement() {
        I18n.getInstance();
    }

    @Before
    public void init()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(applicationAPI).when(spiedApplicationsExportServlet).getApplicationAPI(any(APISession.class));
    }

    @Test
    public void should_getResourcesAsList_convert_ids_param_into_a_list() throws Exception {

        given(hsRequest.getParameter("id")).willReturn("1,2,3,4");

        final long[] returnedList = spiedApplicationsExportServlet.getResourcesAsList(hsRequest);

        assertThat(returnedList).isEqualTo(new long[] { 1, 2, 3, 4 });
    }

    @Test
    public void should_getResourcesAsList_convert_one_id_param_into_a_list() throws Exception {

        given(hsRequest.getParameter("id")).willReturn("1");

        final long[] returnedList = spiedApplicationsExportServlet.getResourcesAsList(hsRequest);

        assertThat(returnedList).isEqualTo(new long[] { 1 });
    }

    @Test
    public void should_getResourcesAsList_convert_null_param_into_empty_list() throws Exception {

        given(hsRequest.getParameter("id")).willReturn(null);
        try {
            spiedApplicationsExportServlet.getResourcesAsList(hsRequest);
        } catch (final Exception e) {
            assertThat(e.getMessage()).isEqualTo("Request parameter \"id\" must be set.");
        }

    }

    @Test(expected = ServletException.class)
    public void should_do_get_without_session_return_servlet_exception() throws Exception {

        given(hsRequest.getSession()).willReturn(httpSession);
        given(httpSession.getAttribute("apiSession")).willReturn(null);

        spiedApplicationsExportServlet.doGet(hsRequest, hsResponse);
    }

}
