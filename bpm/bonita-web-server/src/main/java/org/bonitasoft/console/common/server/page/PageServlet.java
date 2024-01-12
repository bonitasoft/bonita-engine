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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UnauthorizedAccessException;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.livingapps.ApplicationModelFactory;
import org.bonitasoft.livingapps.exception.CreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet allowing to display a page or the resource of a page with an URL like
 * /portal/resource/<page-mapping-key>/content/
 * (it can be a custom page or an external page)
 * This servlet is used to display process forms/overview pages
 * Note: whenever there is an InvalidSessionException from the engine it performs an HTTP session logout and send a 401
 * error. since we are in a iframe, we cannot redirect to the login page.
 * However, the page should handle the 401 and refresh the top window.
 *
 * @author Anthony Birembaut
 */
public class PageServlet extends HttpServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = 2789496969243916444L;

    /**
     * Logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(PageServlet.class.getName());

    public static final String RESOURCE_PATH_SEPARATOR = "/content";

    public static final String API_PATH_SEPARATOR = "/API";

    public static final String THEME_PATH_SEPARATOR = "/theme";

    public static final String APPLICATION_PARAM = "app";

    protected CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();

    protected PageMappingService pageMappingService = new PageMappingService();

    protected BonitaHomeFolderAccessor bonitaHomeFolderAccessor = new BonitaHomeFolderAccessor();

    protected ResourceRenderer resourceRenderer = new ResourceRenderer();

    protected PageRenderer pageRenderer = new PageRenderer(resourceRenderer);

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String pathInfo = request.getPathInfo();

        if (!pathInfo.contains(RESOURCE_PATH_SEPARATOR + "/") && pathInfo.indexOf(API_PATH_SEPARATOR + "/") > 0) {
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

        final HttpSession session = request.getSession();
        final APISession apiSession = (APISession) session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY);

        final String pathInfo = request.getPathInfo();
        // Check if requested URL is missing final slash (necessary in order to be able to use relative URLs for resources)
        if (pathInfo.endsWith(RESOURCE_PATH_SEPARATOR)) {
            customPageRequestModifier.redirectToValidPageUrl(request, response);
        } else if (pathInfo.indexOf(RESOURCE_PATH_SEPARATOR + "/") > 0) {
            final String[] pathInfoSegments = pathInfo.split(RESOURCE_PATH_SEPARATOR + "/", 2);
            String resourcePath = getResourcePath(pathInfoSegments);
            final String mappingKey = pathInfoSegments[0].substring(1);
            try {
                resolveAndDisplayPage(request, response, apiSession, mappingKey, resourcePath);
            } catch (final Exception e) {
                handleException(request, response, mappingKey, isNotResourcePath(resourcePath), e);
            }
        } else if (pathInfo.indexOf(THEME_PATH_SEPARATOR + "/") > 0) {
            final String[] pathInfoSegments = pathInfo.split(THEME_PATH_SEPARATOR + "/", 2);
            String resourcePath = getResourcePath(pathInfoSegments);
            final String mappingKey = pathInfoSegments[0].substring(1);
            try {
                renderThemeResource(request, response, apiSession, resourcePath);
            } catch (final Exception e) {
                handleException(request, response, mappingKey, false, e);
            }
        } else {
            final String message = "/content or /theme is expected in the URL after the page mapping key";
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Bad request: " + message);
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
        }
    }

    protected void renderThemeResource(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession, String resourcePath)
            throws BonitaException, CreationException, IllegalAccessException, IOException, ServletException {

        String appToken = request.getParameter(APPLICATION_PARAM);
        if (appToken != null) {
            renderThemeResource(request, response, apiSession, resourcePath, appToken);
        } else {
            String appTokenFromReferer = getAppFromReferer(request);
            if (appTokenFromReferer != null) {
                if (resourcePath.endsWith(".css")) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                                "App parameter retrieved from the referer. Redirecting the request to get it in the URL for resource "
                                        + resourcePath);
                    }
                    String queryString = StringUtils.isEmpty(request.getQueryString()) ? ""
                            : (request.getQueryString() + "&");
                    response.sendRedirect("?" + queryString + APPLICATION_PARAM + "=" + appTokenFromReferer);
                } else {
                    renderThemeResource(request, response, apiSession, resourcePath, appTokenFromReferer);
                }
            } else {
                // Try to get requested resource from portal theme
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable tor retrieve app parameter for resource " + resourcePath
                            + ". Request referer is missing an an app parameter. Forwarding to the portal theme.");
                }
                String themePath = THEME_PATH_SEPARATOR + "/" + resourcePath;
                //security check against directory traversal attack
                customPageRequestModifier.forwardIfRequestIsAuthorized(request, response, THEME_PATH_SEPARATOR,
                        themePath);
            }
        }
    }

    protected void renderThemeResource(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession, String resourcePath,
            String appToken) throws BonitaException, CreationException, IllegalAccessException, IOException {
        // Try to get requested resource from the current Living application theme
        Long themeId = getThemeId(apiSession, appToken);
        resourceRenderer.renderFile(request, response, getResourceFile(response, apiSession, themeId, resourcePath));
    }

    protected String getResourcePath(final String[] pathInfoSegments) {
        String resourcePath = null;
        if (pathInfoSegments.length > 1 && !pathInfoSegments[1].isEmpty()) {
            resourcePath = pathInfoSegments[1];
        }
        return resourcePath;
    }

    protected String getAppFromReferer(final HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer != null) {
            List<NameValuePair> paramList = null;
            try {
                paramList = URLEncodedUtils.parse(new URI(referer), "UTF-8");
            } catch (URISyntaxException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unable tor retrieve app parameter. Bad request referer: " + e.getMessage());
                }
            }
            for (NameValuePair param : paramList) {
                if (APPLICATION_PARAM.equalsIgnoreCase(param.getName())) {
                    return param.getValue();
                }
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Unable tor retrieve app parameter. Request referer is null.");
        }
        return null;
    }

    protected Long getThemeId(APISession apiSession, final String appToken) throws BonitaException, CreationException {
        ApplicationModelFactory applicationModelFactory = new ApplicationModelFactory(
                TenantAPIAccessor.getLivingApplicationAPI(apiSession),
                TenantAPIAccessor.getCustomPageAPI(apiSession),
                TenantAPIAccessor.getProfileAPI(apiSession));
        return applicationModelFactory.createApplicationModel(appToken).getApplicationThemeId();
    }

    protected void resolveAndDisplayPage(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession,
            final String mappingKey, final String resourcePath)
            throws BonitaException, IOException, InstantiationException, IllegalAccessException {

        boolean isNotResourcePath = isNotResourcePath(resourcePath);
        try {
            Locale currentLocale = pageRenderer.getCurrentLocale(request);
            final PageReference pageReference = pageMappingService.getPage(request, apiSession, mappingKey,
                    currentLocale,
                    isNotResourcePath);
            if (pageReference.getURL() != null) {
                displayExternalPage(response, pageReference.getURL());
            } else if (pageReference.getPageId() != null) {
                displayPageOrResource(request, response, apiSession, pageReference.getPageId(), resourcePath,
                        currentLocale);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    final String message = "Both URL and pageId are not set in the page mapping for " + mappingKey;
                    LOGGER.debug(message);
                }
            }
        } catch (final UnauthorizedAccessException e) {
            final String message = "User not Authorized";
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Forbidden: " + message, e);
            }
            if (isNotResourcePath) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.flushBuffer();
            }
        } catch (final NotFoundException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not found: Cannot find the form mapping for key " + mappingKey, e);
            }
            if (isNotResourcePath) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Form mapping not found");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.flushBuffer();
            }

        }
    }

    protected void displayPageOrResource(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession,
            final Long pageId, final String resourcePath, final Locale currentLocale)
            throws InstantiationException, IllegalAccessException, IOException, BonitaException {
        boolean isNotResourcePath = isNotResourcePath(resourcePath);
        try {
            if (isNotResourcePath) {
                pageRenderer.displayCustomPage(request, response, apiSession, pageId, currentLocale);
            } else {
                resourceRenderer.renderFile(request, response,
                        getResourceFile(response, apiSession, pageId, resourcePath));
            }
        } catch (final PageNotFoundException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Cannot find the page with ID " + pageId);
            }
            if (isNotResourcePath) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.flushBuffer();
            }
        }
    }

    private boolean isNotResourcePath(final String resourcePath) {
        return resourcePath == null || CustomPageService.PAGE_INDEX_FILENAME.equals(resourcePath)
                || CustomPageService.PAGE_CONTROLLER_FILENAME.equals(resourcePath)
                || CustomPageService.PAGE_INDEX_NAME.equals(resourcePath);
    }

    protected File getResourceFile(final HttpServletResponse response, final APISession apiSession, final Long pageId,
            final String resourcePath)
            throws IOException, BonitaException {
        final PageResourceProviderImpl pageResourceProvider = pageRenderer.getPageResourceProvider(pageId, apiSession);
        final File resourceFile = pageResourceProvider
                .getResourceAsFile(CustomPageService.RESOURCES_PROPERTY + File.separator + resourcePath);
        if (!bonitaHomeFolderAccessor.isInFolder(resourceFile, pageResourceProvider.getPageDirectory())) {
            final String message = "For security reasons, access to this file path is forbidden : " + resourcePath;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Forbidden: " + message);
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.flushBuffer();
        }
        pageRenderer.ensurePageFolderIsPresent(apiSession, pageResourceProvider);
        return resourceFile;
    }

    protected void displayExternalPage(final HttpServletResponse response, final String url) throws IOException {
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    protected void handleException(final HttpServletRequest request, final HttpServletResponse response,
            final String mappingKey, final boolean isNotResourcePath, final Exception e)
            throws ServletException, IOException {
        if (e instanceof IllegalArgumentException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The parameters passed to the servlet are invalid.", e);
            }
            if (isNotResourcePath) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Request.");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.flushBuffer();
            }
        } else if (e instanceof InvalidSessionException) {
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
                final String message = "Error while trying to display a page or resource for key " + mappingKey;
                LOGGER.warn(message, e);
            }
            if (!response.isCommitted()) {
                if (isNotResourcePath) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.flushBuffer();
                }
            } else {
                throw new IOException("The response is already commited.", e);
            }
        }
    }

}
