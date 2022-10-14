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
package org.bonitasoft.console.common.server.login.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.LoginManager;

/**
 * Created by Vincent Elcrin
 * Date: 30/08/13
 * Time: 10:28
 */
public abstract class AuthenticationRule {

    private LoginManager loginManager = null;

    /*
     * @return whether the process needs to be aborted or not
     */
    public abstract boolean doAuthorize(HttpServletRequestAccessor request, HttpServletResponse response)
            throws ServletException;

    protected LoginManager getLoginManager() {
        if (loginManager == null) {
            loginManager = new LoginManager();
        }
        return loginManager;
    }

    public void proceedWithRequest(FilterChain chain, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        chain.doFilter(request, response);
    }

}
