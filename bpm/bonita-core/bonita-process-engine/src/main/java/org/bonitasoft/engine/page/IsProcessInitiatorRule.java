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
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * author Anthony Birembaut
 */
public class IsProcessInitiatorRule extends AuthorizationRuleWithParameters implements AuthorizationRule {

    private ProcessInstanceService processInstanceService;
    
    private SessionService sessionService;

    private SessionAccessor sessionAccessor;

    public IsProcessInitiatorRule(ProcessInstanceService processInstanceService, SessionService sessionService, SessionAccessor sessionAccessor) {
        this.processInstanceService = processInstanceService;
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
        try {
            final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            return userId == processInstance.getStartedBy();

        } catch (SProcessInstanceReadException e) {
            throw new SExecutionException(e);
        } catch (SProcessInstanceNotFoundException e) {
            // process instance may be completed already:

            try {
                final List<OrderByOption> orderByOptions = Arrays.asList(
                        new OrderByOption(SAProcessInstance.class, ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, OrderByType.DESC),
                        new OrderByOption(SAProcessInstance.class, ArchivedProcessInstancesSearchDescriptor.END_DATE, OrderByType.DESC));
                final List<FilterOption> filterOptions = Arrays.asList(new FilterOption(SAProcessInstance.class,
                        ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId));
                final QueryOptions queryOptions = new QueryOptions(0, 1, orderByOptions, filterOptions, null);

                final List<SAProcessInstance> saProcessInstances = processInstanceService.searchArchivedProcessInstances(queryOptions);
                if (saProcessInstances.isEmpty()) {
                    throw new SProcessInstanceNotFoundException(processInstanceId);
                }
                return userId == saProcessInstances.get(0).getStartedBy();
            } catch (SBonitaReadException | SProcessInstanceNotFoundException e1) {
                throw new SExecutionException(e1);
            }
        }
    }

    @Override
    public String getId() {
        return AuthorizationRuleConstants.IS_PROCESS_INITIATOR;
    }
}
