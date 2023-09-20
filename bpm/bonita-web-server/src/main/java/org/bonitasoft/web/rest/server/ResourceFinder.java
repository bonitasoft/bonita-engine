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
package org.bonitasoft.web.rest.server;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.Request;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.resource.Finder;

public abstract class ResourceFinder extends Finder {

    private FinderFactory finderFactory;

    public boolean handlesResource(Serializable object) {
        return false;
    }

    protected CommandAPI getCommandAPI(final Request request) {
        final APISession apiSession = getAPISession(request);
        try {
            return TenantAPIAccessor.getCommandAPI(apiSession);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected ProcessAPI getProcessAPI(final Request request) {
        final APISession apiSession = getAPISession(request);
        try {
            return TenantAPIAccessor.getProcessAPI(apiSession);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected BusinessDataAPI getBdmAPI(final Request request) {
        final APISession apiSession = getAPISession(request);
        try {
            return TenantAPIAccessor.getBusinessDataAPI(apiSession);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected TenantAdministrationAPI getTenantAdministrationAPI(final Request request) {
        final APISession apiSession = getAPISession(request);
        try {
            return TenantAPIAccessor.getTenantAdministrationAPI(apiSession);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected APISession getAPISession(final Request request) {
        final HttpSession httpSession = ServletUtils.getRequest(request).getSession();
        return (APISession) httpSession.getAttribute("apiSession");
    }

    public void setFinderFactory(FinderFactory finderFactory) {
        this.finderFactory = finderFactory;
    }

    public FinderFactory getFinderFactory() {
        return finderFactory;
    }

    public Serializable toClientObject(Serializable object) {
        return object;
    }
}
