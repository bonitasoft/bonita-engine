/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

import org.bonitasoft.engine.api.ProcessRuntimeAPI;
import org.bonitasoft.engine.api.impl.ProcessInvolvementDelegate;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * This Rule authorizes a user, if this user is the manager of another user involved in the given process instance.
 * It has the same behavior as {@link ProcessRuntimeAPI#isManagerOfUserInvolvedInProcessInstance(long, long)}
 * 
 * @author Emmanuel Duchastenier
 */
public class IsManagerOfUserInvolvedInProcessInstanceRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private SessionAccessor sessionAccessor;

    private SessionService sessionService;

    private final ProcessInvolvementDelegate processInvolvementDelegate;

    public IsManagerOfUserInvolvedInProcessInstanceRule(SessionService sessionService, SessionAccessor sessionAccessor,
            ProcessInvolvementDelegate processInvolvementDelegate) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.processInvolvementDelegate = processInvolvementDelegate;
    }

    @Override
    public boolean isAllowed(final String key, final Map<String, Serializable> context) throws SExecutionException {
        Long processInstanceId = getLongParameter(context, URLAdapterConstants.ID_QUERY_PARAM);
        if (processInstanceId == null) {
            throw new IllegalArgumentException("Parameter 'id' is mandatory to execute Page Authorization rule 'isManagerOfUserInvolvedInProcessInstance'");
        }
        try {
            long userId = getLoggedUserId(sessionAccessor, sessionService);
            return processInvolvementDelegate.isManagerOfUserInvolvedInProcessInstance(userId, processInstanceId);
        } catch (BonitaException e) {
            throw new SExecutionException(e);
        }

    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_MANAGER_OF_USER_INVOLVED_IN_PROCESS_INSTANCE;
    }
}
