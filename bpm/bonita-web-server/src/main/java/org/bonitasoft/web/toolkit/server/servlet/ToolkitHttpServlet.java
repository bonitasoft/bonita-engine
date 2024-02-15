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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.web.rest.server.framework.json.JSonSimpleDeserializer;
import org.bonitasoft.web.toolkit.client.common.CommonDateFormater;
import org.bonitasoft.web.toolkit.client.common.exception.api.*;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;
import org.bonitasoft.web.toolkit.client.common.json.JSonItemReader;
import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.server.ServiceException;
import org.bonitasoft.web.toolkit.server.ServiceNotFoundException;
import org.bonitasoft.web.toolkit.server.ServletCall;
import org.bonitasoft.web.toolkit.server.utils.ServerDateFormater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Séverin Moussel
 */
public abstract class ToolkitHttpServlet extends HttpServlet {

    private static final long serialVersionUID = -8470006030459575773L;

    /**
     * Console logger
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(ToolkitHttpServlet.class.getName());

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CATCH ALL EXCEPTIONS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ToolkitHttpServlet() {
        super();
        initializeToolkit();
    }

    /**
     * Initialize
     *
     * @see HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    protected final void service(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (final Exception e) {
            catchAllExceptions(retrieveLowestAPIException(e), req, resp);
        }
    }

    private Throwable retrieveLowestAPIException(final Throwable e) {
        Throwable lowest = e;

        while (lowest.getCause() != null && lowest.getCause() instanceof APIException) {
            lowest = lowest.getCause();
        }

        return lowest;
    }

    /**
     * Output an exception in JSon.
     *
     * @param e
     *        The exception to output
     * @param resp
     *        The response to fill
     * @param httpStatusCode
     *        The status code to return
     */
    protected final void outputException(final Throwable e, final HttpServletRequest req,
            final HttpServletResponse resp, final int httpStatusCode) {

        if (httpStatusCode >= 0) {
            resp.setStatus(httpStatusCode);
        }
        resp.setContentType("application/json;charset=UTF-8");

        try {
            final PrintWriter output = resp.getWriter();
            if (e instanceof APIException) {
                setLocalization((APIException) e, LocaleUtils.getUserLocaleAsString(req));
            }

            output.print(e == null ? "" : JSonSerializer.serialize(e));
            output.flush();
        } catch (final Exception e2) {
            throw new APIException(e2);
        }
    }

    /**
     * Output an exception in JSon. Expect the status code to be already set
     *
     * @param e
     *        The exception to output
     * @param resp
     *        The response to fill
     */
    protected final void outputException(final Throwable e, final HttpServletRequest req,
            final HttpServletResponse resp) {

        outputException(e, req, resp, -1);
    }

    private void setLocalization(APIException localizable, String locale) {
        if (locale != null && !locale.isEmpty()) {
            localizable.setLocale(LOCALE.valueOf(locale));
        }
    }

    /**
     * Initialize the toolkit
     */
    protected void initializeToolkit() {
        Item.setApplyInputModifiersByDefault(false);
        Item.setApplyValidatorsByDefault(false);
        Item.setApplyOutputModifiersByDefault(false);
        Item.setApplyValidatorMandatoryByDefault(false);

        CommonDateFormater.setDateFormater(new ServerDateFormater());
        JSonItemReader.setUnserializer(new JSonSimpleDeserializer());
    }

    /**
     * @param req
     *        The request called
     * @param resp
     *        The response to send
     */
    protected void catchAllExceptions(final Throwable exception, final HttpServletRequest req,
            final HttpServletResponse resp) {
        if (exception instanceof APIMethodNotAllowedException) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(exception.getMessage(), exception);
            }
            outputException(exception, req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else if (exception instanceof APINotFoundException) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(exception.getMessage(), exception);
            }
            outputException(exception, req, resp, HttpServletResponse.SC_NOT_FOUND);
        } else if (exception instanceof ServiceNotFoundException) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(exception.getMessage(), exception);
            }
            outputException(exception, req, resp, HttpServletResponse.SC_NOT_FOUND);
        } else if (exception instanceof APIItemNotFoundException) {
            LOGGER.debug(exception.getMessage(), exception);
            outputException(null, req, resp, HttpServletResponse.SC_NOT_FOUND);
        } else if (exception instanceof APIForbiddenException) {
            outputException(exception, req, resp, HttpServletResponse.SC_FORBIDDEN);
        } else if (exception instanceof ServiceException) {
            if (resp.getStatus() < HttpServletResponse.SC_BAD_REQUEST) {
                // Response status is not yet set with an error code
                outputException(exception, req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                // Response status is already set with an error code
                outputException(exception, req, resp);
            }
        } else if (exception instanceof APIIncorrectIdException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(exception.getMessage(), exception);
            }
            outputException(exception, req, resp, HttpServletResponse.SC_BAD_REQUEST);
        } else if (exception instanceof APIItemIdMalformedException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(exception.getMessage(), exception);
            }
            outputException(exception, req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(exception.getMessage(), exception);
            }
            outputException(exception, req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEFINE SERVLET
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract ServletCall defineServletCall(final HttpServletRequest req, final HttpServletResponse resp);

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INITIATE CALL
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected final void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        defineServletCall(req, resp).doGet();
    }

    @Override
    protected final void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        defineServletCall(req, resp).doPost();
    }

    @Override
    protected final void doPut(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        defineServletCall(req, resp).doPut();
    }

    @Override
    protected final void doDelete(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        defineServletCall(req, resp).doDelete();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LOCKING OVERRIDES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected final long getLastModified(final HttpServletRequest req) {
        return super.getLastModified(req);
    }

    @Override
    protected final void doHead(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        super.doHead(req, resp);
    }

    @Override
    protected final void doOptions(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    protected final void doTrace(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        super.doTrace(req, resp);
    }

    @Override
    public final void service(final ServletRequest req, final ServletResponse res)
            throws ServletException, IOException {
        super.service(req, res);
    }
}
