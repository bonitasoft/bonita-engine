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
package org.bonitasoft.livingapps;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.page.CustomPageAuthorizationsHelper;
import org.bonitasoft.console.common.server.page.CustomPageRequestModifier;
import org.bonitasoft.console.common.server.page.CustomPageService;
import org.bonitasoft.console.common.server.page.PageRenderer;
import org.bonitasoft.console.common.server.page.ResourceRenderer;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet which displays an application page (iframe below the layout menu) with an URL like
 * /portal/resource/app/<app-token>/<application-page-token>/content/
 * Note: whenever there is an InvalidSessionException from the engine it performs an HTTP session logout and send a 401
 * error. since we are in a iframe, we cannot redirect to the login page.
 * However, the page should handle the 401 and refresh the top window.
 */
public class LivingApplicationPageServlet extends HttpServlet {

    public static final String RESOURCE_PATH_SEPARATOR = "/content";
    public static final String API_PATH_SEPARATOR = "/API";
    public static final String THEME_PATH_SEPARATOR = "/theme";
    /**
     * uuid
     */
    private static final long serialVersionUID = -5410859017103815654L;
    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(LivingApplicationPageServlet.class.getName());

    protected ResourceRenderer resourceRenderer = new ResourceRenderer();

    protected PageRenderer pageRenderer = new PageRenderer(resourceRenderer);

    protected BonitaHomeFolderAccessor bonitaHomeFolderAccessor = new BonitaHomeFolderAccessor();

    protected CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String pathInfo = request.getPathInfo();
        final List<String> pathSegments = resourceRenderer.getPathSegments(pathInfo);
        if (isValidPathForToken(API_PATH_SEPARATOR, pathSegments)) {
            //Support relative calls to the REST API from the forms using ../API/
            final String apiPath = pathInfo.substring(pathInfo.indexOf(API_PATH_SEPARATOR + "/"));
            //security check against directory traversal attack
            customPageRequestModifier.forwardIfRequestIsAuthorized(request, response, API_PATH_SEPARATOR, apiPath);
        } else {
            super.service(request, response);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String pathInfo = request.getPathInfo();
        final HttpSession session = request.getSession();
        final APISession apiSession = (APISession) session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY);

        // Check if requested URL is missing final slash (necessary in order to be able to use relative URLs for resources)
        if (pathInfo.endsWith(RESOURCE_PATH_SEPARATOR)) {
            customPageRequestModifier.redirectToValidPageUrl(request, response);
            return;
        }

        String appToken = null;
        String pageToken = null;
        String customPageName = null;
        String resourcePath = null;

        //Validate mapping key contain "AppToken/PageToken/" and at least one more segment
        final List<String> pathSegments = resourceRenderer.getPathSegments(pathInfo);
        if (pathSegments.size() >= 3) {
            appToken = pathSegments.get(0);
            pageToken = pathSegments.get(1);

            if (isValidPathForToken(RESOURCE_PATH_SEPARATOR, pathSegments)) {
                final String pageMapping = "/" + appToken + "/" + pageToken + RESOURCE_PATH_SEPARATOR + "/";
                if (pathInfo.length() > pageMapping.length()) {
                    resourcePath = pathInfo.substring(pageMapping.length());
                }
                boolean isNotResourcePath = isNotResourcePath(resourcePath);
                customPageName = getCustomPageName(appToken, pageToken, apiSession, request, response,
                        isNotResourcePath);
                if (StringUtils.isBlank(customPageName)) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Error while trying to retrieve the application page");
                    }
                    //the response status is set in the error handling of getCustomPageName
                    return;
                }
                try {
                    if (isAuthorized(apiSession, appToken, customPageName)) {
                        if (isNotResourcePath) {
                            pageRenderer.displayCustomPage(request, response, apiSession, customPageName);
                        } else {
                            final File resourceFile = getResourceFile(resourcePath, customPageName);
                            pageRenderer.ensurePageFolderIsPresent(apiSession,
                                    pageRenderer.getPageResourceProvider(customPageName));
                            resourceRenderer.renderFile(request, response, resourceFile);
                        }
                    } else {
                        if (isNotResourcePath) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not Authorized");
                        } else {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.flushBuffer();
                        }
                    }
                } catch (final Exception e) {
                    handleException(customPageName, e, request, response, isNotResourcePath);
                }
            } else if (isValidPathForToken(THEME_PATH_SEPARATOR, pathSegments)) {
                //Support relative calls to the THEME from the application page using ../theme/
                final String themeResourcePath = pathInfo.substring(pathInfo.indexOf(THEME_PATH_SEPARATOR + "/"));
                String appThemeResourcePrefix = "/apps/" + appToken;
                customPageRequestModifier.forwardIfRequestIsAuthorized(request, response,
                        appThemeResourcePrefix + THEME_PATH_SEPARATOR + "/",
                        appThemeResourcePrefix + themeResourcePath);
            } else {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(
                            "One of the separator '/content', '/theme' or '/API' is expected in the URL after the application token and the page token.");
                }
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.flushBuffer();
            }

        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "The info path is suppose to contain the application token, the page token and one of the separator '/content', '/theme' or '/API'.");
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.flushBuffer();
        }

    }

    private String getCustomPageName(final String appToken, final String pageToken, final APISession apiSession,
            final HttpServletRequest request, final HttpServletResponse response, final boolean isNotResourcePath)
            throws ServletException, IOException {
        try {
            final Long customPageId = getApplicationApi(apiSession).getApplicationPage(appToken, pageToken).getPageId();
            return getPageApi(apiSession).getPage(customPageId).getName();
        } catch (final NotFoundException e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("The application page " + appToken + "/" + pageToken + " was not found.");
            }
            if (isNotResourcePath) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Cannot find the page" + pageToken + "for the application" + appToken + ".");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.flushBuffer();
            }
        } catch (final Exception e) {
            handleException(appToken + "/" + pageToken, e, request, response, isNotResourcePath);
        }
        return "";
    }

    private boolean isValidPathForToken(final String TokenSeparator, final List<String> pathSegments) {
        return pathSegments.size() > 2 && pathSegments.get(2).equals(TokenSeparator.substring(1));
    }

    private boolean isNotResourcePath(final String resourcePath) {
        return resourcePath == null || CustomPageService.PAGE_INDEX_FILENAME.equals(resourcePath)
                || CustomPageService.PAGE_CONTROLLER_FILENAME.equals(resourcePath)
                || CustomPageService.PAGE_INDEX_NAME.equals(resourcePath);
    }

    private File getResourceFile(final String resourcePath, final String pageName) throws IOException, BonitaException {
        final PageResourceProviderImpl pageResourceProvider = pageRenderer.getPageResourceProvider(pageName);
        final File resourceFile = new File(pageResourceProvider.getPageDirectory(),
                CustomPageService.RESOURCES_PROPERTY + File.separator
                        + resourcePath);

        if (!bonitaHomeFolderAccessor.isInFolder(resourceFile, pageResourceProvider.getPageDirectory())) {
            throw new BonitaException("Unauthorized access to the file " + resourcePath);
        }
        return resourceFile;
    }

    private boolean isAuthorized(final APISession apiSession, final String appToken, final String pageName)
            throws BonitaException {
        return getCustomPageAuthorizationsHelper(apiSession).isPageAuthorized(appToken, pageName);
    }

    private void handleException(final String pageName, final Exception e, final HttpServletRequest request,
            final HttpServletResponse response, final boolean isNotResourcePath) throws ServletException, IOException {
        if (e instanceof InvalidSessionException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Invalid Bonita engine session.", e);
            }
            SessionUtil.sessionLogout(request.getSession());
            if (isNotResourcePath) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Bonita engine session.");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.flushBuffer();
            }
        } else {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Error while trying to render the application page " + pageName, e);
            }
            throw new ServletException(e.getMessage());
        }
    }

    protected ApplicationAPI getApplicationApi(final APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLivingApplicationAPI(apiSession);
    }

    protected PageAPI getPageApi(final APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getCustomPageAPI(apiSession);
    }

    protected CustomPageAuthorizationsHelper getCustomPageAuthorizationsHelper(final APISession apiSession)
            throws BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException {
        return new CustomPageAuthorizationsHelper(apiSession,
                TenantAPIAccessor.getLivingApplicationAPI(apiSession), TenantAPIAccessor.getCustomPageAPI(apiSession),
                new ApplicationModelFactory(
                        TenantAPIAccessor.getLivingApplicationAPI(apiSession),
                        TenantAPIAccessor.getCustomPageAPI(apiSession),
                        TenantAPIAccessor.getProfileAPI(apiSession)));
    }
}
