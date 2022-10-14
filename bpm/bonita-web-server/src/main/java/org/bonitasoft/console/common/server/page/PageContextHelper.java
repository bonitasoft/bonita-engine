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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.engine.session.APISession;

public class PageContextHelper {

    public static final String PROFILE_PARAM = "profile";

    public static final String ATTRIBUTE_API_SESSION = "apiSession";

    private final HttpServletRequest request;

    public PageContextHelper(HttpServletRequest request) {
        this.request = request;
    }

    public String getCurrentProfile() {
        return request.getParameter(PROFILE_PARAM);
    }

    public Locale getCurrentLocale() {
        return LocaleUtils.getUserLocale(request);
    }

    public APISession getApiSession() {
        final HttpSession httpSession = request.getSession();
        return (APISession) httpSession.getAttribute(ATTRIBUTE_API_SESSION);

    }
}
