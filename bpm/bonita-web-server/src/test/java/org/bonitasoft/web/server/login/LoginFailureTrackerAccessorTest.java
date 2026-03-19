/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.server.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;

import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;

public class LoginFailureTrackerAccessorTest {

    @Test
    public void should_return_null_when_application_context_is_not_available() {
        ServletContext servletContext = mock(ServletContext.class);
        // getAttribute returns null by default → WebApplicationContextUtils returns null

        LoginFailureTracker result = LoginFailureTrackerAccessor.getLoginFailureTracker(servletContext);

        assertThat(result).isNull();
    }

    @Test
    public void should_return_null_when_bean_is_not_found() {
        ServletContext servletContext = mock(ServletContext.class);
        WebApplicationContext appContext = mock(WebApplicationContext.class);
        when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))
                .thenReturn(appContext);
        when(appContext.getBean(LoginFailureTracker.class))
                .thenThrow(new NoSuchBeanDefinitionException(LoginFailureTracker.class));

        LoginFailureTracker result = LoginFailureTrackerAccessor.getLoginFailureTracker(servletContext);

        assertThat(result).isNull();
    }

    @Test
    public void should_return_tracker_when_bean_is_found() {
        ServletContext servletContext = mock(ServletContext.class);
        WebApplicationContext appContext = mock(WebApplicationContext.class);
        LoginFailureTracker tracker = mock(LoginFailureTracker.class);
        when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))
                .thenReturn(appContext);
        when(appContext.getBean(LoginFailureTracker.class)).thenReturn(tracker);

        LoginFailureTracker result = LoginFailureTrackerAccessor.getLoginFailureTracker(servletContext);

        assertThat(result).isSameAs(tracker);
    }
}
