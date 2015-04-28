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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * author Anthony Birembaut
 */
public class IsProcessInitiatorRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private ProcessInstanceService processInstanceService;
    private TechnicalLoggerService technicalLoggerService;

    public IsProcessInitiatorRule(ProcessInstanceService processInstanceService, TechnicalLoggerService technicalLoggerService) {
        this.processInstanceService = processInstanceService;
        this.technicalLoggerService = technicalLoggerService;
    }

    @Override
    public boolean isAllowed(final String key, final Map<String, Serializable> context) throws SExecutionException {
        try {
            Long userId = getLongParameter(context, URLAdapterConstants.USER_QUERY_PARAM);
            Long processInstanceId = getLongParameter(context, URLAdapterConstants.ID_QUERY_PARAM);
            if (userId == null || processInstanceId == null) {
                throw new IllegalArgumentException(
                        "Parameters 'userId' and 'processInstanceId' are mandatory to execute Page Authorization rule 'IsProcessInitiatorRule'");
            }

            final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            return userId == processInstance.getStartedBy();

        } catch (SBonitaException e) {
            throw new SExecutionException(e);
        }
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_PROCESS_INITIATOR;
    }
}
