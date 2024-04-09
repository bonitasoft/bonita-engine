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
package org.bonitasoft.console.common.server.page;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Julien Mege
 */

@RunWith(MockitoJUnitRunner.class)
public class PageResourceServletTest {

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    HttpSession httpSession;

    @Test
    public void should_verify_authorisation_for_the_given_location_param() throws Exception {

        final PageResourceServlet pageResourceServlet = spy(new PageResourceServlet());
        when(req.getParameter(pageResourceServlet.getResourceParameterName())).thenReturn("name");
        when(req.getMethod()).thenReturn("GET");

        when(pageResourceServlet.getResourcesParentFolder()).thenReturn(new File("."));

        when(req.getParameter("location")).thenReturn("../../../file.txt");
        try {
            pageResourceServlet.service(req, res);
        } catch (final ServletException e) {
            assertTrue(e.getMessage().startsWith("For security reasons, access to this file paths"));
        }
    }

}
