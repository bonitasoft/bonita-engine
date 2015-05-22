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

import org.bonitasoft.engine.api.impl.ProcessInvolvementAPIImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * author Emmanuel Duchastenier
 */
public class IsTaskPerformerRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private final ActivityInstanceService activityInstanceService;

    private final SessionService sessionService;

    private final SessionAccessor sessionAccessor;

    public IsTaskPerformerRule(ActivityInstanceService activityInstanceService, SessionService sessionService, SessionAccessor sessionAccessor) {
        this.activityInstanceService = activityInstanceService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public boolean isAllowed(String key, Map<String, Serializable> context) throws SExecutionException {
        try {
            long userId = getLoggedUserId(sessionAccessor, sessionService);
            Long processInstanceId = getLongParameter(context, URLAdapterConstants.ID_QUERY_PARAM);
            if (processInstanceId == null) {
                throw new IllegalArgumentException(
                        "Parameter 'id' is mandatory to execute Page Authorization rule 'IsProcessInitiatorRule'");
            }
            return ProcessInvolvementAPIImpl.isAssignedToArchivedTaskOfProcess(userId, processInstanceId, activityInstanceService);
        } catch (final SBonitaException e) {
            throw new SExecutionException(e);
        }
    }


    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_TASK_PERFORMER;
    }

}
