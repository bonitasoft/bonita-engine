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
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.identity.impl.IconImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class OrganizationIconServletTest {

    private static final long ICON_ID = 1238970432L;
    private static final long USER_ID = 8211558366L;
    @Spy
    private OrganizationIconServlet organizationIconServlet;
    private MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    private MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
    @Mock
    private IdentityAPI identityAPI;

    @Before
    public void before() throws Exception {
        doReturn(identityAPI).when(organizationIconServlet).getIdentityApi(any());
    }

    private void havingIcon(long iconId, byte[] content) throws NotFoundException {
        doReturn(new IconImpl(iconId, "mime-type", content)).when(identityAPI).getIcon(iconId);
    }

    @Test
    public void should_return_icon_content_when_valid_icon_id_is_given() throws Exception {
        havingIcon(ICON_ID, "content".getBytes());
        httpServletRequest.setPathInfo("/" + ICON_ID);

        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getContentAsByteArray()).isEqualTo("content".getBytes());
    }

    @Test
    public void should_set_special_headers_when_user_agent_is_firefox() throws Exception {
        havingIcon(ICON_ID, "content".getBytes());
        httpServletRequest.addHeader("User-Agent", "Firefox-1238941");
        httpServletRequest.setPathInfo("/" + ICON_ID);

        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename*=UTF-8''" + ICON_ID);
    }

    @Test
    public void should_set_normal_headers_when_user_agent_is_not_firefox() throws Exception {
        havingIcon(ICON_ID, "content".getBytes());
        httpServletRequest.addHeader("User-Agent", "Chrome-1238941");
        httpServletRequest.setPathInfo("/" + ICON_ID);

        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getHeader("Content-Disposition"))
                .isEqualTo("inline; filename=\"" + ICON_ID + "\"; filename*=UTF-8''"
                        + ICON_ID);
    }

    @Test
    public void should_status_be_BAD_REQUEST_when_id_is_missing_in_url() throws Exception {
        httpServletRequest.setPathInfo("");

        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void should_status_be_BAD_REQUEST_when_id_is_not_a_long_in_url() throws Exception {
        httpServletRequest.setPathInfo("notALong");

        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void should_status_be_BAD_REQUEST_when_null_is_set_as_id_in_url() throws Exception {
        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void should_status_be_NOT_FOUND_when_icon_does_not_exists_in_engine() throws Exception {
        doThrow(NotFoundException.class).when(identityAPI).getIcon(ICON_ID);
        httpServletRequest.setPathInfo("/" + ICON_ID);

        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void should_content_type_be_set_to_what_is_return_from_the_engine() throws Exception {
        doReturn(new IconImpl(ICON_ID, "theMimeTypeOfTheIcon", "content".getBytes())).when(identityAPI)
                .getIcon(ICON_ID);
        httpServletRequest.setPathInfo("/" + ICON_ID);

        organizationIconServlet.doGet(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getContentType()).contains("theMimeTypeOfTheIcon");
    }

    @Test
    public void should_return_no_content_success_when_deleting_icon() throws Exception {
        httpServletRequest.setPathInfo("/" + USER_ID);
        httpServletRequest.setParameter("type", "user");
        httpServletRequest.setMethod("DELETE");
        UserUpdater updater = new UserUpdater();
        updater.setIcon(null, null);
        ArgumentCaptor<UserUpdater> captor = ArgumentCaptor.forClass(UserUpdater.class);

        organizationIconServlet.doDelete(httpServletRequest, httpServletResponse);

        verify(identityAPI, times(1)).updateUser(eq(USER_ID), captor.capture());
        assertEquals(updater.getFields(), captor.getValue().getFields());
        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void should_return_API_Malformed_Exception_when_type_is_not_defined_in_url() throws Exception {
        httpServletRequest.setPathInfo("/" + USER_ID);
        httpServletRequest.setMethod("DELETE");

        organizationIconServlet.doDelete(httpServletRequest, httpServletResponse);

        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void should_return_API_Malformed_Exception_when_type_is_incorrect_in_url() throws Exception {
        httpServletRequest.setPathInfo("/" + USER_ID);
        httpServletRequest.setMethod("DELETE");
        httpServletRequest.setParameter("type", "group");

        organizationIconServlet.doDelete(httpServletRequest, httpServletResponse);
        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void should_return_not_found_when_deleting_icon_for_user_that_does_not_exist() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(identityAPI).updateUser(eq(USER_ID), any());
        httpServletRequest.setPathInfo("/" + USER_ID);
        httpServletRequest.setParameter("type", "user");
        httpServletRequest.setMethod("DELETE");

        organizationIconServlet.doDelete(httpServletRequest, httpServletResponse);
        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void should_return_bad_request_when_deleting_icon_for_user_with_generic_error() throws Exception {
        doThrow(new UpdateException("Server crash")).when(identityAPI).updateUser(eq(USER_ID), any());
        httpServletRequest.setPathInfo("/" + USER_ID);
        httpServletRequest.setParameter("type", "user");
        httpServletRequest.setMethod("DELETE");

        organizationIconServlet.doDelete(httpServletRequest, httpServletResponse);
        assertThat(httpServletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
