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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Slf4j
public class LoginFailureTrackerAccessor {

    private LoginFailureTrackerAccessor() {
    }

    public static LoginFailureTracker getLoginFailureTracker(ServletContext servletContext) {
        try {
            ApplicationContext context = WebApplicationContextUtils
                    .getWebApplicationContext(servletContext);
            if (context == null) {
                log.warn(
                        "Spring ApplicationContext not available — brute-force login protection is disabled");
                return null;
            }
            return context.getBean(LoginFailureTracker.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn(
                    "LoginFailureTracker bean not found — brute-force login protection is disabled", e);
            return null;
        }
    }
}
