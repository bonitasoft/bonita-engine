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
package org.bonitasoft.web.rest.server.engineclient;

import org.bonitasoft.engine.api.GroupAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;

/**
 * @author Vincent Elcrin
 */
public class EngineAPIAccessor {

    private final APISession session;

    public EngineAPIAccessor(final APISession session) {
        this.session = session;
    }

    public APISession getSession() {
        return session;
    }

    public ProfileAPI getProfileAPI() {
        try {
            return TenantAPIAccessor.getProfileAPI(getSession());
        } catch (final BonitaException e) {
            throw new APIException("Error when getting engine process API", e);
        }
    }

    public ProcessAPI getProcessAPI() {
        try {
            return TenantAPIAccessor.getProcessAPI(getSession());
        } catch (final BonitaException e) {
            throw new APIException("Error when getting engine process API", e);
        }
    }

    public IdentityAPI getIdentityAPI() {
        try {
            return TenantAPIAccessor.getIdentityAPI(getSession());
        } catch (final BonitaException e) {
            throw new APIException("Error when getting engine identity API", e);
        }
    }

    public GroupAPI getGroupAPI() {
        try {
            return TenantAPIAccessor.getIdentityAPI(getSession());
        } catch (final BonitaException e) {
            throw new APIException("Error when getting engine group API", e);
        }
    }

    public PageAPI getPageAPI() {
        try {
            return TenantAPIAccessor.getCustomPageAPI(getSession());
        } catch (final BonitaException e) {
            throw new APIException("Error when getting engine page API", e);
        }
    }

    public TenantAdministrationAPI getTenantAdministrationAPI() {
        try {
            return TenantAPIAccessor.getTenantAdministrationAPI(getSession());
        } catch (final BonitaException e) {
            throw new APIException("Error when getting engine tenant management API", e);
        }
    }
}
