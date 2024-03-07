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
package org.bonitasoft.console.common.server.page.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.web.extension.page.PageResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide access to the resources contained in the custom page zip
 *
 * @author Anthony Birembaut
 */
public class PageResourceProviderImpl implements PageResourceProvider {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PageResourceProviderImpl.class.getName());

    private static final String VERSION_FILENAME = "VERSION";

    protected final static String THEME_RESOURCE_SERVLET_NAME = "themeResource";

    protected final static String PORTAL_THEME_NAME = "portal";

    protected final static String BONITA_THEME_CSS_FILENAME = "bonita.css";

    protected static String productVersion;

    static {
        final InputStream versionStream = PageResourceProviderImpl.class.getClassLoader()
                .getResourceAsStream(VERSION_FILENAME);
        if (versionStream != null) {
            try {
                productVersion = IOUtils.toString(versionStream);
            } catch (final Exception e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Unable to read the file " + VERSION_FILENAME, e);
                }
                productVersion = "";
            } finally {
                try {
                    versionStream.close();
                } catch (final IOException e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Unable to close the input stream for file " + VERSION_FILENAME, e);
                    }
                }
            }
        } else {
            productVersion = "";
        }
    }

    /**
     * product version param
     */
    protected final static String VERSION_PARAM = "v";

    /**
     * file name
     */
    protected final static String LOCATION_PARAM = "location";

    /**
     * theme name : the theme folder's name
     */
    protected final static String THEME_PARAM = "theme";

    private final String fullPageName;

    protected String pageName;

    protected File pageDirectory;

    private ClassLoader resourceClassLoader;

    private File pageTempFile = null;

    private final Long pageId;

    public PageResourceProviderImpl(final String pageName) {
        this(pageName, null, null, true);
    }

    public PageResourceProviderImpl(final Page page) {
        this(page.getName(), page.getId(), page.getProcessDefinitionId(), true);
    }

    private PageResourceProviderImpl(final String pageName, final Long pageId, final Long processDefinitionId,
            final boolean buildPageTempFile) {
        this.pageName = pageName;
        this.pageId = pageId;
        fullPageName = buildFullPageName(pageName, processDefinitionId);
        pageDirectory = buildPageDirectory(fullPageName);
        if (buildPageTempFile) {
            buildPageTempDirectory(fullPageName);
            pageTempFile = buildPageTempFile(fullPageName);
        }
    }

    private String buildFullPageName(final String pageName, final Long processDefinitionId) {
        final StringBuilder builder = new StringBuilder();
        if (processDefinitionId != null) {
            builder.append("p").append(processDefinitionId).append("_");
        }
        builder.append(pageName);
        return builder.toString();
    }

    protected void buildPageTempDirectory(final String fullPageName) {
        new File(WebBonitaConstantsUtils.getTenantInstance().getTempFolder(), fullPageName);
    }

    protected File buildPageTempFile(final String fullPageName) {
        return new File(WebBonitaConstantsUtils.getTenantInstance().getTempFolder(), fullPageName + ".zip");
    }

    protected File buildPageDirectory(final String fullPageName) {
        return new File(WebBonitaConstantsUtils.getTenantInstance().getPagesFolder(), fullPageName);
    }

    @Override
    public InputStream getResourceAsStream(final String resourceName) throws FileNotFoundException {
        return new FileInputStream(getResourceAsFile(resourceName));
    }

    @Override
    public File getResourceAsFile(final String resourceName) {
        return new File(pageDirectory, resourceName);
    }

    @Override
    public String getResourceURL(final String resourceName) {
        return resourceName + "?" + VERSION_PARAM + "=" + productVersion;
    }

    @Override
    public String getBonitaThemeCSSURL() {
        return THEME_RESOURCE_SERVLET_NAME + "?" + THEME_PARAM +
                "=" + PORTAL_THEME_NAME + "&" + LOCATION_PARAM + "=" + BONITA_THEME_CSS_FILENAME + "&" +
                VERSION_PARAM + "=" + productVersion;
    }

    @Override
    public File getPageDirectory() {
        return pageDirectory;
    }

    public File getTempPageFile() {
        return pageTempFile;
    }

    public void setResourceClassLoader(final ClassLoader resourceClassLoader) {
        this.resourceClassLoader = resourceClassLoader;
    }

    @Override
    public ResourceBundle getResourceBundle(final String name, final Locale locale) {
        return ResourceBundle.getBundle(name, locale, resourceClassLoader);
    }

    @Override
    public String getPageName() {
        return pageName;
    }

    @Override
    public Page getPage(final PageAPI pageAPI) throws PageNotFoundException {
        if (pageId != null) {
            return pageAPI.getPage(pageId);
        }
        return pageAPI.getPageByName(getPageName());
    }

    @Override
    public String getFullPageName() {
        return fullPageName;
    }
}
