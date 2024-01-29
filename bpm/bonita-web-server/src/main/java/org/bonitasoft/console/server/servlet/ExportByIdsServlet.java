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
package org.bonitasoft.console.server.servlet;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;

/**
 * Export Resources with Ids as XML file
 *
 * @author Anthony Birembaut
 */
public abstract class ExportByIdsServlet extends BonitaExportServlet {

    private static final long serialVersionUID = -671004787066141408L;

    private static final String RESOURCES_PARAM_KEY = "id";

    @Override
    protected byte[] exportResources(final HttpServletRequest request) throws BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException, ExportException, FileNotFoundException, ExecutionException {
        final APISession apiSession = (APISession) request.getSession().getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        long[] resourcesIds = getResourcesAsList(request);
        return exportResources(resourcesIds, apiSession);
    }

    protected final long[] getResourcesAsList(final HttpServletRequest request) {
        final String resourceIDParamValue = request.getParameter(RESOURCES_PARAM_KEY);
        final String[] resourcesIDAsStrings = parseIdsParamValue(resourceIDParamValue);
        final long[] resourceIDs = new long[resourcesIDAsStrings.length];
        if (resourcesIDAsStrings != null) {
            for (int i = 0; i < resourcesIDAsStrings.length; i++) {
                resourceIDs[i] = Long.valueOf(resourcesIDAsStrings[i]);
            }
        }
        return resourceIDs;
    }

    private String[] parseIdsParamValue(final String resourceIDParamValue) {
        if (resourceIDParamValue != null) {
            return resourceIDParamValue.split(",");
        } else {
            throw new RuntimeException(AbstractI18n.t_("Request parameter \"id\" must be set."));
        }
    }

    protected abstract byte[] exportResources(final long[] ids, final APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, ExportException,
            FileNotFoundException, ExecutionException;

}
