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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import javax.servlet.ServletContext;

import org.bonitasoft.console.common.server.login.AuthenticationAuditLogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;

class AuthenticationAuditLoggerAccessorTest {

    private final ServletContext servletContext = mock(ServletContext.class);

    private final WebApplicationContext appContext = mock(WebApplicationContext.class);

    @Test
    void getAuthenticationAuditLogger_should_return_the_bean_of_the_web_context() {
        final AuthenticationAuditLogger auditLogger = new AuthenticationAuditLogger(true);
        doReturn(appContext).when(servletContext)
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        doReturn(auditLogger).when(appContext).getBean(AuthenticationAuditLogger.class);

        assertThat(AuthenticationAuditLoggerAccessor.getAuthenticationAuditLogger(servletContext))
                .isSameAs(auditLogger);
    }

    @Test
    void getAuthenticationAuditLogger_should_return_a_default_logger_when_the_bean_is_not_found() {
        doReturn(appContext).when(servletContext)
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        doThrow(new NoSuchBeanDefinitionException(AuthenticationAuditLogger.class)).when(appContext)
                .getBean(AuthenticationAuditLogger.class);

        assertThat(AuthenticationAuditLoggerAccessor.getAuthenticationAuditLogger(servletContext)).isNotNull();
    }

    @Test
    void getAuthenticationAuditLogger_should_return_a_default_logger_when_the_web_context_is_closed() {
        doReturn(appContext).when(servletContext)
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        doThrow(new IllegalStateException("context has been closed already")).when(appContext)
                .getBean(AuthenticationAuditLogger.class);

        assertThat(AuthenticationAuditLoggerAccessor.getAuthenticationAuditLogger(servletContext)).isNotNull();
    }

    @Test
    void getAuthenticationAuditLogger_should_return_a_default_logger_when_the_web_context_failed_to_start() {
        doReturn(new IllegalStateException("root context failed")).when(servletContext)
                .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        assertThat(AuthenticationAuditLoggerAccessor.getAuthenticationAuditLogger(servletContext)).isNotNull();
    }

    @Test
    void getAuthenticationAuditLogger_should_return_a_default_logger_without_web_context() {
        assertThat(AuthenticationAuditLoggerAccessor.getAuthenticationAuditLogger(servletContext)).isNotNull();
        assertThat(AuthenticationAuditLoggerAccessor.getAuthenticationAuditLogger(null)).isNotNull();
    }
}
