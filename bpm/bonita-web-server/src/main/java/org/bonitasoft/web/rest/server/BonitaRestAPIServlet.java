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
package org.bonitasoft.web.rest.server;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeConverter;
import org.bonitasoft.web.rest.server.framework.RestAPIFactory;
import org.bonitasoft.web.rest.server.framework.servlet.APIServlet;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;

/**
 * @author Séverin Moussel
 */
public class BonitaRestAPIServlet extends APIServlet {

    private static final long serialVersionUID = 525945083859596909L;

    public BonitaRestAPIServlet() {
        super();

        FlowNodeConverter.setFlowNodeConverter(new FlowNodeConverter());
    }

    @Override
    protected ItemDefinitionFactory defineApplicatioFactoryCommon() {
        return new ModelFactory();
    }

    @Override
    protected RestAPIFactory defineApplicatioFactoryServer() {
        return new BonitaRestAPIFactory();
    }

    @Override
    protected void catchAllExceptions(final Throwable exception, final HttpServletRequest req,
            final HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        try {
            req.setCharacterEncoding("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            super.catchAllExceptions(e, req, resp);
        }
        if (exception instanceof InvalidSessionException
                || exception instanceof APIException && exception.getCause() != null
                        && exception.getCause() instanceof InvalidSessionException) {
            final HttpServletRequestAccessor requestAccessor = new HttpServletRequestAccessor(req);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(exception.getMessage(), exception);
            }
            outputException(exception, req, resp, HttpServletResponse.SC_UNAUTHORIZED);
            SessionUtil.sessionLogout(requestAccessor.getHttpSession());
        } else if (exception.getCause() instanceof NotFoundException) {
            outputException(null, req, resp, HttpServletResponse.SC_NOT_FOUND);
        } else {
            super.catchAllExceptions(exception, req, resp);
        }
    }
}
