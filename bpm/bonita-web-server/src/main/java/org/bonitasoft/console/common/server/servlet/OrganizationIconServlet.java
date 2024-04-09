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

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.Icon;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMalformedUrlException;
import org.bonitasoft.web.toolkit.client.common.exception.http.ServerException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Anthony Birembaut
 * @author Baptiste Mesta
 */
public class OrganizationIconServlet extends IconServlet {

    @Override
    protected Optional<IconContent> retrieveIcon(Long iconId, APISession apiSession) {
        IdentityAPI identityAPI = getIdentityApi(apiSession);
        Icon icon;
        try {
            icon = identityAPI.getIcon(iconId);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
        return Optional.of(new IconContent(icon.getContent(), icon.getMimeType()));
    }

    @Override
    protected void deleteIcon(Long entityId, APISession apiSession, HttpServletRequest request) throws ServerException {
        String entityType = request.getParameter("type");
        if (entityType == null || !entityType.equals("user")) {
            throw new APIMalformedUrlException(request.getRequestURL().toString(),
                    "Cannot delete a non-user icon. Provide type=user as a query parameter.");
        }
        IdentityAPI identityAPI = getIdentityApi(apiSession);
        UserUpdater updater = new UserUpdater();
        updater.setIcon(null, null);
        try {
            identityAPI.updateUser(entityId, updater);
        } catch (UserNotFoundException e) {
            throw new APIItemNotFoundException(User.class.getName(), APIID.makeAPIID(entityId));
        } catch (UpdateException e) {
            throw new APIException(e);
        }
    }

    IdentityAPI getIdentityApi(APISession apiSession) {
        return new APIClient(apiSession).getIdentityAPI();
    }

}
