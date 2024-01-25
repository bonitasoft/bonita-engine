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
package org.bonitasoft.console.common.server.servlet;

import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.business.application.Icon;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.http.ServerException;
import org.bonitasoft.web.toolkit.client.data.APIID;

@Slf4j
public class ApplicationIconServlet extends IconServlet {

    @Override
    protected Optional<IconContent> retrieveIcon(Long iconId, APISession apiSession) {
        ApplicationAPI applicationApi = getApplicationApi(apiSession);
        try {
            Icon icon = applicationApi.getIconOfApplication(iconId);
            if (icon != null) {
                return Optional.of(new IconContent(icon.getContent(), icon.getMimeType()));
            } else {
                return Optional.empty();
            }
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated as of 9.0.0, Application icon should be deleted/updated at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        log.warn("DELETE request on Application Icon is deprecated! " +
                "An application icon should be deleted or updated at startup instead.");
        super.doDelete(request, response);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated as of 9.0.0, Application icon should be deleted/updated at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    protected void deleteIcon(Long entityId, APISession apiSession, HttpServletRequest request) throws ServerException {
        ApplicationAPI applicationApi = getApplicationApi(apiSession);
        ApplicationUpdater updater = new ApplicationUpdater();
        updater.setIcon(null, null);
        try {
            applicationApi.updateApplication(entityId, updater);
        } catch (ApplicationNotFoundException e) {
            throw new APIItemNotFoundException(Application.class.getName(), APIID.makeAPIID(entityId));
        } catch (UpdateException | AlreadyExistsException e) {
            throw new APIException(e);
        }
    }

    ApplicationAPI getApplicationApi(APISession apiSession) {
        return new APIClient(apiSession).getApplicationAPI();
    }

}
