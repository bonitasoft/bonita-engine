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
package org.bonitasoft.console.common.server.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLocationReaderTest {

    @Mock
    private HttpServletRequest req;

    @Spy
    private final ResourceLocationReader resourceLocationReader = new ResourceLocationReader();

    @Test
    public void should_use_infopath_if_location_param_is_not_defined() {

        when(req.getParameter("location")).thenReturn(null);
        when(req.getPathInfo()).thenReturn("/bonita.css");

        final String resourceLocation = resourceLocationReader.getResourceLocationFromRequest(req);

        verify(req, times(1)).getPathInfo();
        assertThat(resourceLocation).isEqualTo("bonita.css");
    }

    @Test
    public void should_not_use_infopath_if_location_param_is_defined() throws Exception {

        when(req.getParameter("location")).thenReturn("bonita.css");

        final String resourceLocation = resourceLocationReader.getResourceLocationFromRequest(req);

        verify(req, never()).getPathInfo();
        assertThat(resourceLocation).isEqualTo("bonita.css");
    }
}
