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
package org.bonitasoft.console.common.server.utils;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.user.User;

/**
 * @author Ruiheng.Fan
 * @author Baptiste Mesta
 */
public class SessionUtil {

    /**
     * the session param for the engine API session
     */
    public static final String API_SESSION_PARAM_KEY = "apiSession";

    /**
     * the session param for the user
     */
    public static final String USER_SESSION_PARAM_KEY = "user";

    /**
     * the session param for the username
     */
    public static final String USERNAME_SESSION_PARAM = "username";

    public static void sessionLogin(final User user, final APISession apiSession, final HttpSession session) {
        session.setAttribute(USERNAME_SESSION_PARAM, user.getUsername());
        session.setAttribute(USER_SESSION_PARAM_KEY, user);
        session.setAttribute(API_SESSION_PARAM_KEY, apiSession);
    }

    public static void sessionLogout(final HttpSession session) {
        sessionLogout(session, true);
    }

    public static void sessionLogout(final HttpSession session, final boolean invalidateHTTPSession) {
        session.removeAttribute(API_SESSION_PARAM_KEY);
        session.removeAttribute(USERNAME_SESSION_PARAM);
        session.removeAttribute(USER_SESSION_PARAM_KEY);
        if (invalidateHTTPSession) {
            session.invalidate();
        }
    }
}
