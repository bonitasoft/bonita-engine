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
package org.bonitasoft.console.common.server.form;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.page.PageServlet;
import org.bonitasoft.console.common.server.page.ResourceRenderer;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.console.common.server.utils.UrlBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet allowing to display a form for a process or a task
 * The servlet only redirect to the generic Page servlet with the right URL
 *
 * @author Anthony Birembaut
 */
public class ProcessFormServlet extends HttpServlet {

    /**
     * UUID
     */
    private static final long serialVersionUID = -6397856355139281873L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFormServlet.class.getName());

    private static final String PAGE_SERVLET_MAPPING = "/portal/resource/";

    private static final String PROCESS_PATH_SEGMENT = "process";

    private static final String PROCESS_INSTANCE_PATH_SEGMENT = "processInstance";

    private static final String TASK_INSTANCE_PATH_SEGMENT = "taskInstance";

    private static final String TASK_PATH_SEGMENT = "task";

    private static final String USER_ID_PARAM = "user";

    private static final String ID_PARAM = "id";

    protected ProcessFormService processFormService = new ProcessFormService();

    private final ResourceRenderer resourceRenderer = new ResourceRenderer();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        long processDefinitionId = -1L;
        long processInstanceId = -1L;
        long taskInstanceId = -1L;
        String taskName = null;
        final List<String> pathSegments = resourceRenderer.getPathSegments(request.getPathInfo());
        final String user = request.getParameter(USER_ID_PARAM);
        final long userId = convertToLong(USER_ID_PARAM, user);
        final HttpSession session = request.getSession();
        final APISession apiSession = (APISession) session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        try {
            if (pathSegments.size() > 1) {
                taskInstanceId = getTaskInstanceId(apiSession, pathSegments, userId);
                processInstanceId = getProcessInstanceId(pathSegments);
                processDefinitionId = getProcessDefinitionId(apiSession, pathSegments);
            }
            if (processDefinitionId == -1L && processInstanceId == -1L && taskInstanceId == -1L) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Either process name and version are required or process instance Id (with or without task name) or task instance Id.");
                return;
            }
            processDefinitionId = processFormService.ensureProcessDefinitionId(apiSession, processDefinitionId,
                    processInstanceId, taskInstanceId);
            taskName = processFormService.getTaskName(apiSession, taskInstanceId);
            redirectToPageServlet(request, response, apiSession, processDefinitionId, processInstanceId, taskInstanceId,
                    taskName);
        } catch (final Exception e) {
            handleException(response, processDefinitionId, taskName, processInstanceId != -1L, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void redirectToPageServlet(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession,
            final long processDefinitionId, final long processInstanceId, final long taskInstanceId,
            final String taskName) throws BonitaException, IOException {
        final String pageServletURL = buildPageServletURL(request, apiSession, processDefinitionId, processInstanceId,
                taskInstanceId, taskName);
        final UrlBuilder urlBuilder = new UrlBuilder(pageServletURL);
        urlBuilder.appendParameters(request.getParameterMap());
        response.sendRedirect(response.encodeRedirectURL(urlBuilder.build()));
    }

    protected String buildPageServletURL(final HttpServletRequest request, final APISession apiSession,
            final long processDefinitionId,
            final long processInstanceId, final long taskInstanceId, final String taskName)
            throws BonitaException, IOException {
        final StringBuilder pageServletURL = new StringBuilder(request.getContextPath());
        pageServletURL.append(PAGE_SERVLET_MAPPING);
        if (taskInstanceId != -1L) {
            pageServletURL.append(TASK_INSTANCE_PATH_SEGMENT)
                    .append("/")
                    .append(processFormService.getProcessPath(apiSession, processDefinitionId))
                    .append("/")
                    .append(processFormService.encodePathSegment(taskName))
                    .append(PageServlet.RESOURCE_PATH_SEPARATOR)
                    .append("/?")
                    .append(ID_PARAM)
                    .append("=")
                    .append(taskInstanceId);
        } else if (processInstanceId != -1L) {
            pageServletURL.append(PROCESS_INSTANCE_PATH_SEGMENT)
                    .append("/")
                    .append(processFormService.getProcessPath(apiSession, processDefinitionId))
                    .append(PageServlet.RESOURCE_PATH_SEPARATOR)
                    .append("/?")
                    .append(ID_PARAM)
                    .append("=")
                    .append(processInstanceId);
        } else {
            pageServletURL.append(PROCESS_PATH_SEGMENT)
                    .append("/")
                    .append(processFormService.getProcessPath(apiSession, processDefinitionId))
                    .append(PageServlet.RESOURCE_PATH_SEPARATOR)
                    .append("/?")
                    .append(ID_PARAM)
                    .append("=")
                    .append(processDefinitionId);
        }
        return pageServletURL.toString();
    }

    protected long getProcessInstanceId(final List<String> pathSegments) {
        long processInstanceId = -1L;
        if (PROCESS_INSTANCE_PATH_SEGMENT.equals(pathSegments.get(0))) {
            final String processInstance = pathSegments.get(1);
            processInstanceId = convertToLong(PROCESS_INSTANCE_PATH_SEGMENT, processInstance);
        }
        return processInstanceId;
    }

    protected long getTaskInstanceId(final APISession apiSession, final List<String> pathSegments, final long userId)
            throws BonitaException {
        if (TASK_INSTANCE_PATH_SEGMENT.equals(pathSegments.get(0))) {
            final String taskInstance = pathSegments.get(1);
            return convertToLong(TASK_INSTANCE_PATH_SEGMENT, taskInstance);
        } else if (PROCESS_INSTANCE_PATH_SEGMENT.equals(pathSegments.get(0))) {
            final String processInstance = pathSegments.get(1);
            final long processInstanceId = convertToLong(PROCESS_INSTANCE_PATH_SEGMENT, processInstance);
            if (pathSegments.size() > 2 && TASK_PATH_SEGMENT.equals(pathSegments.get(2))) {
                final String taskName = URLDecoder.decode(pathSegments.get(3), StandardCharsets.UTF_8);
                return processFormService.getTaskInstanceId(apiSession, processInstanceId, taskName, userId);
            }
        }
        return -1L;

    }

    protected long getProcessDefinitionId(final APISession apiSession, final List<String> pathSegments)
            throws BonitaException {
        long processDefinitionId = -1L;
        if (PROCESS_PATH_SEGMENT.equals(pathSegments.get(0))) {
            if (pathSegments.size() > 2) {
                final String processName = pathSegments.get(1);
                final String processVersion = pathSegments.get(2);
                processDefinitionId = processFormService.getProcessDefinitionId(apiSession, processName,
                        processVersion);
            }
        }
        return processDefinitionId;
    }

    protected long convertToLong(final String parameterName, final String idAsString) {
        if (idAsString != null) {
            try {
                return Long.parseLong(idAsString);
            } catch (final NumberFormatException e) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Wrong value for " + parameterName + " expecting a number (long value)");
                }
            }
        }
        return -1;
    }

    protected void handleException(final HttpServletResponse response, final long processDefinitionId,
            final String taskName,
            final boolean hasProcessInstanceId, final Exception e)
            throws ServletException {
        try {
            if (e instanceof ProcessDefinitionNotFoundException) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find the process");
            } else if (e instanceof ArchivedProcessInstanceNotFoundException) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find the process instance");
            } else if (e instanceof ActivityInstanceNotFoundException) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find the task instance");
            } else {
                if (LOGGER.isWarnEnabled()) {
                    String message = "Error while trying to display a form";
                    if (processDefinitionId != -1) {
                        message = message + " for process " + processDefinitionId;
                    }
                    if (taskName != null) {
                        message = message + " for task " + taskName;
                    } else if (hasProcessInstanceId) {
                        message = message + " ( instance overview)";
                    }
                    LOGGER.warn(message, e);
                }
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } catch (final IOException ioe) {
            throw new ServletException(ioe);
        }
    }

}
