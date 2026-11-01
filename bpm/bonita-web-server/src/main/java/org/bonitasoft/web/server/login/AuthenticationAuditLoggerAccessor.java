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

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.console.common.server.login.AuthenticationAuditLogger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Slf4j
public class AuthenticationAuditLoggerAccessor {

    private AuthenticationAuditLoggerAccessor() {
    }

    /**
     * @return the audit logger configured in the Spring web context, or, when it is not available, an audit logger
     *         that ignores the end-user context headers
     */
    public static AuthenticationAuditLogger getAuthenticationAuditLogger(ServletContext servletContext) {
        if (servletContext != null) {
            try {
                ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
                if (context != null) {
                    return context.getBean(AuthenticationAuditLogger.class);
                }
            } catch (RuntimeException e) {
                // e.g. no bean, or a context that is closed (shutdown) or failed to start: never break an
                // authentication because of the audit log
                log.warn("AuthenticationAuditLogger bean not available, the end-user context headers are ignored: {}",
                        e.getMessage());
                log.debug("AuthenticationAuditLogger bean lookup failure", e);
            }
        }
        return new AuthenticationAuditLogger();
    }
}
