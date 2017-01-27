/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ProcessInvolvementDelegate;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Anthony Birembaut
 */
public class IsProcessInitiatorRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private SessionService sessionService;

    private SessionAccessor sessionAccessor;

    private final ProcessInvolvementDelegate processInvolvementDelegate;

    public IsProcessInitiatorRule(SessionService sessionService, SessionAccessor sessionAccessor, ProcessInvolvementDelegate processInvolvementDelegate) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.processInvolvementDelegate = processInvolvementDelegate;
    }

    @Override
    public boolean isAllowed(final String key, final Map<String, Serializable> context) throws SExecutionException {
        long userId = getLoggedUserId(sessionAccessor, sessionService);
        Long processInstanceId = getLongParameter(context, URLAdapterConstants.ID_QUERY_PARAM);
        if (processInstanceId == null) {
            throw new IllegalArgumentException(
                    "Parameter 'id' is mandatory to execute Page Authorization rule 'IsProcessInitiatorRule'");
        }
        try {
            return processInvolvementDelegate.isProcessOrArchivedProcessInitiator(userId, processInstanceId);
        } catch (ProcessInstanceNotFoundException e) {
            throw new SExecutionException(e);
        }
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_PROCESS_INITIATOR;
    }
}
