/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api;

import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Parent class providing common methods for Bonita REST Controllers
 */
@Slf4j
public abstract class AbstractRESTController {

    public static final String API_SPRING_INTERNAL = "APISpringInternal";

    public AbstractRESTController() {
        // For testing purposes, to make sure that the new implementation is deployed and that
        // we do not fallback on the Restlet implementation.
        // Will be removed once the entire Restlet refactoring is done:
        log.info("Creating REST Controller {}", this.getClass().getName());
    }

    public APISession getApiSession(HttpSession session) {
        APISession apiSession = (APISession) session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        if (apiSession == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return apiSession;
    }

    // VisibleForTesting
    public CommandAPI getCommandAPI(APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getCommandAPI(apiSession);
    }

    protected CommandAPI getCommandAPI(HttpSession session)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getCommandAPI(getApiSession(session));
    }

    // VisibleForTesting
    public ProcessAPI getProcessAPI(APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getProcessAPI(apiSession);
    }

    protected ProcessAPI getProcessAPI(HttpSession session)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getProcessAPI(getApiSession(session));
    }

    public TenantAdministrationAPI getTenantAdministrationAPI(HttpSession session)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getTenantAdministrationAPI(getApiSession(session));
    }

}
