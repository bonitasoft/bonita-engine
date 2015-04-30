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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * author Emmanuel Duchastenier
 */
public class IsInvolvedInProcessInstanceRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private ActivityInstanceService activityInstanceService;
    
    private SessionService sessionService;

    private SessionAccessor sessionAccessor;

    public IsInvolvedInProcessInstanceRule(ActivityInstanceService activityInstanceService, SessionService sessionService, SessionAccessor sessionAccessor) {
        this.activityInstanceService = activityInstanceService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public boolean isAllowed(final String key, final Map<String, Serializable> context) throws SExecutionException {
        long userId = getLoggedUserId(sessionAccessor, sessionService);
        Long processInstanceId = getLongParameter(context, URLAdapterConstants.ID_QUERY_PARAM);
        if (processInstanceId == null) {
            throw new IllegalArgumentException(
                    "Parameter 'id' is mandatory to execute Page Authorization rule 'IsProcessInitiatorRule'");
        }

        // is user assigned or has pending tasks on this process instance:
        final QueryOptions queryOptions = new QueryOptions(0, 1, Collections.EMPTY_LIST, Arrays.asList(new FilterOption(SHumanTaskInstance.class,
                "logicalGroup2", processInstanceId)), null);
        try {
            return activityInstanceService.getNumberOfPendingOrAssignedTasks(userId, queryOptions) > 0;
        } catch (SBonitaReadException e) {
            throw new SExecutionException(e);
        }
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_INVOLVED_IN_PROCESS_INSTANCE;
    }
}
