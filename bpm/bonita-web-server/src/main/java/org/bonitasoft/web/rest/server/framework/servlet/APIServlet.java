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
package org.bonitasoft.web.rest.server.framework.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.rest.server.framework.RestAPIFactory;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.server.ServletCall;
import org.bonitasoft.web.toolkit.server.servlet.ToolkitHttpServlet;

/**
 * @author Séverin Moussel
 */
public abstract class APIServlet extends ToolkitHttpServlet {

    private static final long serialVersionUID = 1852124460966605504L;

    @Override
    protected void initializeToolkit() {
        super.initializeToolkit();
        ItemDefinitionFactory.setDefaultFactory(defineApplicatioFactoryCommon());
        RestAPIFactory.setDefaultFactory(defineApplicatioFactoryServer());
    }

    @Override
    protected ServletCall defineServletCall(final HttpServletRequest req, final HttpServletResponse resp) {
        return new APIServletCall(req, resp);
    }

    protected abstract ItemDefinitionFactory defineApplicatioFactoryCommon();

    protected abstract RestAPIFactory defineApplicatioFactoryServer();
}
