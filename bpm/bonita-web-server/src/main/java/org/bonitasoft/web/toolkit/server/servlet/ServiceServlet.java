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
package org.bonitasoft.web.toolkit.server.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.toolkit.server.ServiceFactory;
import org.bonitasoft.web.toolkit.server.ServiceServletCall;
import org.bonitasoft.web.toolkit.server.ServletCall;

/**
 * This class is the entry point of all server calls
 *
 * @author Séverin Moussel
 */
@SuppressWarnings("serial")
public abstract class ServiceServlet extends ToolkitHttpServlet {

    private final ServiceFactory serviceFactory;

    public ServiceServlet(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @Override
    protected ServletCall defineServletCall(final HttpServletRequest req, final HttpServletResponse resp) {
        return new ServiceServletCall(serviceFactory, req, resp);
    }
}
