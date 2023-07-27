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
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import groovy.lang.GroovyClassLoader;
import org.bonitasoft.console.common.server.page.extension.PageContextImpl;
import org.bonitasoft.console.common.server.page.extension.PageResourceProviderImpl;
import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.session.APISession;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * Class used by servlets to display a custom page
 * Since each instance of the servlet carry an instance of this class, it should have absolutely no instance attribute
 *
 * @author Anthony Birembaut
 */
public class PageRenderer {

    public static final String PROFILE_PARAM = "profile";

    protected CustomPageService customPageService = new CustomPageService();

    protected ResourceRenderer resourceRenderer;

    public PageRenderer(final ResourceRenderer resourceRenderer) {
        this.resourceRenderer = resourceRenderer;
    }

    public void displayCustomPage(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession, final String pageName)
            throws CompilationFailedException, InstantiationException, IllegalAccessException, IOException,
            BonitaException {

        final PageResourceProviderImpl pageResourceProvider = getPageResourceProvider(pageName);
        displayCustomPage(request, response, apiSession, pageResourceProvider, getCurrentLocale(request));
    }

    public void displayCustomPage(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession, final long pageId)
            throws CompilationFailedException, InstantiationException, IllegalAccessException, IOException,
            BonitaException {
        displayCustomPage(request, response, apiSession, getPageResourceProvider(pageId, apiSession),
                getCurrentLocale(request));
    }

    public void displayCustomPage(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession, final long pageId, final Locale currentLocale)
            throws CompilationFailedException, InstantiationException, IllegalAccessException, IOException,
            BonitaException {
        displayCustomPage(request, response, apiSession, getPageResourceProvider(pageId, apiSession), currentLocale);
    }

    public void ensurePageFolderIsPresent(final APISession apiSession,
            final PageResourceProviderImpl pageResourceProvider) throws BonitaException, IOException {
        customPageService.ensurePageFolderIsPresent(apiSession, pageResourceProvider);
    }

    private void displayCustomPage(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession,
            final PageResourceProviderImpl pageResourceProvider, final Locale currentLocale)
            throws BonitaException, IOException, InstantiationException,
            IllegalAccessException {
        customPageService.ensurePageFolderIsUpToDate(apiSession, pageResourceProvider);
        enforceLocaleCookieIfPresentInURLOrBrowser(request, response, currentLocale);
        if (isGroovyPage(pageResourceProvider)) {
            displayGroovyPage(request, response, apiSession, pageResourceProvider);
        } else {
            displaySimpleHtmlPage(request, response, pageResourceProvider);
        }
    }

    private boolean isGroovyPage(final PageResourceProviderImpl pageResourceProvider) {
        final File pageFolder = pageResourceProvider.getPageDirectory();
        final File indexGroovy = customPageService.getGroovyPageFile(pageFolder);
        return indexGroovy.exists();
    }

    private void displaySimpleHtmlPage(final HttpServletRequest request, final HttpServletResponse response,
            final PageResourceProviderImpl pageResourceProvider)
            throws IOException, BonitaException, IllegalAccessException {

        final File resourceFile = getIndexFile(pageResourceProvider);
        resourceRenderer.renderFile(request, response, resourceFile, true);
    }

    private File getIndexFile(final PageResourceProviderImpl pageResourceProvider) {
        final File indexHtml = new File(getResourceFolder(pageResourceProvider), CustomPageService.PAGE_INDEX_FILENAME);
        if (indexHtml.exists()) {
            return indexHtml;
        }
        //fallback try to found index.html a the root of the zip to support custompage legacy.
        return new File(getResourceFolder(pageResourceProvider).getParent(), CustomPageService.PAGE_INDEX_FILENAME);
    }

    private File getResourceFolder(final PageResourceProviderImpl pageResourceProvider) {
        return new File(pageResourceProvider.getPageDirectory(), CustomPageService.RESOURCES_PROPERTY);
    }

    private void displayGroovyPage(final HttpServletRequest request, final HttpServletResponse response,
            final APISession apiSession,
            final PageResourceProviderImpl pageResourceProvider)
            throws CompilationFailedException, InstantiationException, IllegalAccessException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        final ClassLoader originalClassloader = Thread.currentThread().getContextClassLoader();
        final GroovyClassLoader pageClassloader = customPageService.getPageClassloader(apiSession,
                pageResourceProvider);
        try {
            Thread.currentThread().setContextClassLoader(pageClassloader);
            pageResourceProvider.setResourceClassLoader(pageClassloader);
            final Class<?> pageClass = customPageService.registerPage(pageClassloader, pageResourceProvider);
            final org.bonitasoft.web.extension.page.PageController pageController = ((Class<org.bonitasoft.web.extension.page.PageController>) pageClass)
                    .newInstance();
            pageController.doGet(request, response, pageResourceProvider,
                    new PageContextImpl(apiSession, getCurrentLocale(request), getCurrentProfile(request)));
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassloader);
        }
    }

    public String getCurrentProfile(final HttpServletRequest request) {
        return request.getParameter(PROFILE_PARAM);
    }

    private void enforceLocaleCookieIfPresentInURLOrBrowser(final HttpServletRequest request,
            final HttpServletResponse response, final Locale currentLocale) {
        final String localeFromCookie = LocaleUtils.getStandardizedLocaleFromCookie(request);
        if (currentLocale != null && !currentLocale.toString().equals(localeFromCookie)) {
            //Set the cookie if the locale is in the URL and different from the existing cookie value or the cookie does not exist yet
            LocaleUtils.addOrReplaceLocaleCookieResponse(response, currentLocale.toString());
        }
    }

    public Locale getCurrentLocale(final HttpServletRequest request) {
        return LocaleUtils.getUserLocale(request);
    }

    public PageResourceProviderImpl getPageResourceProvider(final String pageName) {
        return new PageResourceProviderImpl(pageName);
    }

    public PageResourceProviderImpl getPageResourceProvider(final long pageId, final APISession apiSession)
            throws BonitaException {
        final Page page = customPageService.getPage(apiSession, pageId);
        return new PageResourceProviderImpl(page);
    }

}
