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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import groovy.lang.GroovyClassLoader;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.page.extension.RestAPIContextImpl;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.extension.rest.RestApiResponse;
import org.bonitasoft.web.rest.server.api.extension.ControllerClassName;
import org.bonitasoft.web.rest.server.api.extension.ResourceExtensionResolver;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used by servlets to display a custom rest api
 * Since each instance of the servlet carry an instance of this class, it should have absolutely no instance attribute
 *
 * @author Laurent Leseigneur
 */
public class RestApiRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiRenderer.class.getName());

    private final CustomPageService customPageService = new CustomPageService();

    public org.bonitasoft.web.extension.rest.RestApiResponse handleRestApiCall(final HttpServletRequest request,
            ResourceExtensionResolver resourceExtensionResolver)
            throws CompilationFailedException, InstantiationException, IllegalAccessException, IOException,
            BonitaException {
        final PageContextHelper pageContextHelper = new PageContextHelper(request);
        final APISession apiSession = pageContextHelper.getApiSession();
        final Long pageId = resourceExtensionResolver.resolvePageId(apiSession);
        final Page page = customPageService.getPage(apiSession, pageId);
        final PageResourceProviderImpl pageResourceProvider = new PageResourceProviderImpl(page);
        customPageService.ensurePageFolderIsUpToDate(apiSession, pageResourceProvider);
        final ControllerClassName restApiControllerClassName = resourceExtensionResolver
                .resolveRestApiControllerClassName(pageResourceProvider);
        final String mappingKey = resourceExtensionResolver.generateMappingKey();
        return renderResponse(request, apiSession, pageContextHelper, pageResourceProvider, restApiControllerClassName,
                mappingKey);

    }

    private org.bonitasoft.web.extension.rest.RestApiResponse renderResponse(final HttpServletRequest request,
            final APISession apiSession,
            final PageContextHelper pageContextHelper,
            final PageResourceProviderImpl pageResourceProvider, ControllerClassName restApiControllerClassName,
            String mappingKey)
            throws CompilationFailedException, InstantiationException, IllegalAccessException, IOException,
            BonitaException {
        final ClassLoader originalClassloader = Thread.currentThread().getContextClassLoader();
        final GroovyClassLoader pageClassloader = customPageService.getPageClassloader(apiSession,
                pageResourceProvider);
        try {
            Thread.currentThread().setContextClassLoader(pageClassloader);
            final Class<?> restApiControllerClass = customPageService.registerRestApiPage(pageClassloader,
                    pageResourceProvider, restApiControllerClassName, mappingKey);
            pageResourceProvider.setResourceClassLoader(pageClassloader);
            try {
                return doHandle(request, apiSession, pageContextHelper, pageResourceProvider, restApiControllerClass);
            } catch (final Throwable e) {
                LOGGER.error("Error when executing rest api extension call to " + mappingKey, e);
                throw e;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassloader);
        }
    }

    protected RestApiResponse doHandle(final HttpServletRequest request,
            final APISession apiSession,
            final PageContextHelper pageContextHelper,
            final PageResourceProviderImpl pageResourceProvider,
            final Class<?> restApiControllerClass) throws InstantiationException, IllegalAccessException {
        final org.bonitasoft.web.extension.rest.RestApiController restApiController = instantiate(
                restApiControllerClass);
        return restApiController.doHandle(request,
                new org.bonitasoft.web.extension.rest.RestApiResponseBuilder(),
                new RestAPIContextImpl(apiSession, new APIClient(apiSession), pageContextHelper.getCurrentLocale(),
                        pageResourceProvider));
    }

    protected <T extends Object> T instantiate(Class<?> baseClass)
            throws InstantiationException, IllegalAccessException {
        return (T) baseClass.newInstance();
    }

}
