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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.console.common.server.servlet.ResourceServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anthony Birembaut
 *         This servlet is deprecated.
 *         You can now access your resources through their relative URL.
 *         see the custom page documentation.
 */
@Deprecated
public class PageResourceServlet extends ResourceServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = 6745970275852563050L;

    /**
     * Logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(PageResourceServlet.class.getName());

    /**
     * theme name : the theme folder's name
     */
    protected final static String PAGE_PARAM_NAME = "page";

    /**
     * resources subfolder name
     */
    protected final static String RESOURCES_SUBFOLDER_NAME = "resources";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("This servlet is deprecated. " +
                    "You can now access your resources through their relative URL." +
                    "see the custom page documentation.");
        }
        super.doGet(request, response);
    }

    @Override
    protected String getResourceParameterName() {
        return PAGE_PARAM_NAME;
    }

    @Override
    protected File getResourcesParentFolder() {
        return WebBonitaConstantsUtils.getTenantInstance().getPagesFolder();
    }

    @Override
    protected String getSubFolderName() {
        return RESOURCES_SUBFOLDER_NAME;
    }

    @Override
    protected String getDefaultResourceName() {
        return null;
    }

}
