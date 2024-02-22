/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Emmanuel Duchastenier, Anthony Birembaut
 */
public class IsAdminRule implements AuthorizationRule {

    SessionAccessor sessionAccessor;

    SessionService sessionService;

    protected static final String PROCESS_DEPLOY_PERMISSION = "process_deploy";

    public IsAdminRule(SessionAccessor sessionAccessor, SessionService sessionService) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public boolean isAllowed(final String key, final Map<String, Serializable> context) throws SExecutionException {
        try {
            return getSession().getUserPermissions().contains(PROCESS_DEPLOY_PERMISSION);
        } catch (SSessionNotFoundException | SessionIdNotSetException e) {
            throw new SExecutionException(e);
        }
    }

    protected SSession getSession() throws SSessionNotFoundException, SessionIdNotSetException {
        return sessionService.getSession(sessionAccessor.getSessionId());
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_ADMIN;
    }
}
