/**
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
 **/
package org.bonitasoft.engine.external.permission;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstancesInvolvingUser;
import org.bonitasoft.engine.search.process.SearchOpenProcessInstancesInvolvingUser;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Specific Command to ckeck if the user given can see a overview form of a ProcesInstance with the processInstanceId given.
 *
 * @author Zhao Na
 * @author Celine Souchet
 */
public class IsAllowedToSeeOverviewForm extends TenantCommand {

    private TenantServiceAccessor tenantAccessor;

    private static final String USER_ID_KEY = "USER_ID_KEY";

    private static final String PROCESSINSTANCE_ID_KEY = "PROCESSINSTANCE_ID_KEY";

    /**
     * @return a boolean representing if the user has the permission to see a overview form of the processInstance
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor tenantAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        this.tenantAccessor = tenantAccessor;
        boolean isAllowed = false;

        final Long userId = (Long) parameters.get(USER_ID_KEY);
        if (userId == null || userId == 0) {
            throw new SCommandParameterizationException("Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to Long.");
        }

        final long processInstanceId = (Long) parameters.get(PROCESSINSTANCE_ID_KEY);
        if (processInstanceId == 0) {
            throw new SCommandParameterizationException("Mandatory parameter " + PROCESSINSTANCE_ID_KEY + " is missing or not convertible to Long.");
        }

        long processDefinitionId = 0;
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        try {
            final SProcessInstance sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
            if (sProcessInstance != null) {
                processDefinitionId = sProcessInstance.getProcessDefinitionId();
            }
        } catch (final SBonitaException e) {
            final GetArchivedProcessInstanceList getArchivedProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService,
                    tenantAccessor.getProcessDefinitionService(), searchEntitiesDescriptor, processInstanceId, 0, 5);
            try {
                getArchivedProcessInstanceList.execute();
            } catch (final SCommandExecutionException e1) {
                throw e1;
            } catch (final SBonitaException e1) {
                e.setProcessInstanceIdOnContext(processInstanceId);
                throw new SCommandExecutionException("No process instance and archived process instance during executing command isAllowedToSeeOverviewForm.",
                        e);
            }
            final List<ArchivedProcessInstance> archivedPInstances = getArchivedProcessInstanceList.getResult();
            if (!archivedPInstances.isEmpty()) {
                processDefinitionId = archivedPInstances.get(0).getProcessDefinitionId();
            }
        }

        if (processDefinitionId != 0) {
            final ActorMappingService actorMappingService = this.tenantAccessor.getActorMappingService();
            try {
                isAllowed = actorMappingService.canUserStartProcessDefinition(userId, processDefinitionId);
            } catch (final SBonitaException e) {
                e.setProcessDefinitionIdOnContext(processDefinitionId);
                throw new SCommandExecutionException("No actorInitiator of user who can start the processDefinition.", e);
            }
        }

        if (!isAllowed) {
            final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
            searchOptions.addFilter("id", processInstanceId);
            final SearchOpenProcessInstancesInvolvingUser searchOpenProcessInstances = new SearchOpenProcessInstancesInvolvingUser(processInstanceService,
                    searchEntitiesDescriptor.getSearchProcessInstanceDescriptor(), userId, searchOptions, processDefinitionService);
            SearchResult<ProcessInstance> processInstanceRes = null;
            try {
                searchOpenProcessInstances.execute();
                processInstanceRes = searchOpenProcessInstances.getResult();
            } catch (final SCommandExecutionException e) {
                throw e;
            } catch (final SBonitaException sbe) {
                throw new SCommandExecutionException("No processInstance that involves user :" + userId
                        + " found durng executing method IsAllowedToSeeOverviewForm.", sbe);
            }
            if (processInstanceRes.getCount() > 0) {// ==1?
                isAllowed = true;
            } else {
                final SearchArchivedProcessInstancesInvolvingUser archivedSearcher = new SearchArchivedProcessInstancesInvolvingUser(userId,
                        processInstanceService, tenantAccessor.getProcessDefinitionService(),
                        searchEntitiesDescriptor.getSearchArchivedProcessInstanceDescriptor(), searchOptions);
                try {
                    archivedSearcher.execute();
                } catch (final SCommandExecutionException e) {
                    throw e;
                } catch (final SBonitaException e) {
                    throw new SCommandExecutionException("No archived processInstance that involves user :" + userId
                            + " found during execution of method IsAllowedToSeeOverviewForm.", e);
                }
                final SearchResult<ArchivedProcessInstance> archivedRes = archivedSearcher.getResult();
                if (archivedRes.getCount() > 0) {
                    isAllowed = true;
                }
            }
        }
        return isAllowed;
    }

}
