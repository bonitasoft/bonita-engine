/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.external.permission;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActorsOfUserCanStartProcessDefinitions;
import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.bonitasoft.engine.search.process.SearchArchivedProcessInstancesInvolvingUser;
import org.bonitasoft.engine.search.process.SearchOpenProcessInstancesInvolvingUser;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Specific Command to ckeck if the user given can see a overview form of a ProcesInstance with the processInstanceId given.
 * 
 * @author Zhao Na
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
        boolean isHas = false;

        final long userId = (Long) parameters.get(USER_ID_KEY);
        if (userId == 0) {
            throw new SCommandParameterizationException("Mandatory parameter " + USER_ID_KEY + " is missing or not convertible to Long.");
        }

        final long processInstanceId = (Long) parameters.get(PROCESSINSTANCE_ID_KEY);
        if (processInstanceId == 0) {
            throw new SCommandParameterizationException("Mandatory parameter " + PROCESSINSTANCE_ID_KEY + " is missing or not convertible to Long.");
        }

        long processDefinitionId = 0;
        final ProcessInstanceService processInstanceService = this.tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final SearchProcessInstanceDescriptor searchProcessInstanceDescriptor = searchEntitiesDescriptor.getProcessInstanceDescriptor();

        final GetProcessInstance getProcessInstance = new GetProcessInstance(processInstanceService, processDefinitionService, searchProcessInstanceDescriptor,
                processInstanceId);
        try {
            getProcessInstance.execute();
        } catch (final SBonitaException e) {
            final GetArchivedProcessInstanceList getArchivedProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService,
                    searchEntitiesDescriptor, processInstanceId, 0, 5);
            try {
                getArchivedProcessInstanceList.execute();
            } catch (final SBonitaException e1) {
                throw new SCommandExecutionException("No processInstance and archived ProcessInstance with id: " + processInstanceId
                        + " during executing command isAllowedToSeeOverviewForm.", e);
            }
            final List<ArchivedProcessInstance> archivedPInstances = getArchivedProcessInstanceList.getResult();
            if (archivedPInstances.size() > 0) {
                processDefinitionId = archivedPInstances.get(0).getProcessDefinitionId();
            }
        }
        final ProcessInstance processInstance = getProcessInstance.getResult();
        if (processInstance != null) {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }

        if (processDefinitionId != 0) {
            final ActorMappingService actorMappingService = this.tenantAccessor.getActorMappingService();
            final GetActorsOfUserCanStartProcessDefinitions checker = new GetActorsOfUserCanStartProcessDefinitions(actorMappingService, processDefinitionId,
                    userId);
            try {
                checker.execute();
            } catch (final SBonitaException e) {
                throw new SCommandExecutionException("No actorInitiator of user who can start the processDefinition with id:" + processDefinitionId, e);
            }
            final List<SActor> ckRes = checker.getResult();
            if (ckRes != null && ckRes.size() == 1) {
                isHas = true;
            }
        }

        if (!isHas) {
            final SearchOptionsImpl searchOptions = new SearchOptionsImpl(0, 10);
            searchOptions.addFilter("id", processInstanceId);
            final SearchOpenProcessInstancesInvolvingUser searchOpenProcessInstances = new SearchOpenProcessInstancesInvolvingUser(processInstanceService,
                    searchEntitiesDescriptor.getProcessInstanceDescriptor(), userId, searchOptions, processDefinitionService);
            SearchResult<ProcessInstance> processInstanceRes = null;
            try {
                searchOpenProcessInstances.execute();
                processInstanceRes = searchOpenProcessInstances.getResult();
            } catch (final SBonitaException sbe) {
                throw new SCommandExecutionException("No processInstance that involves user :" + userId
                        + " found durng executing method IsAllowedToSeeOverviewForm.", sbe);
            }
            if (processInstanceRes.getCount() > 0) {// ==1?
                isHas = true;
            } else {
                final SearchArchivedProcessInstancesInvolvingUser archivedSearcher = new SearchArchivedProcessInstancesInvolvingUser(userId,
                        processInstanceService, searchEntitiesDescriptor.getArchivedProcessInstancesDescriptor(), searchOptions);
                try {
                    archivedSearcher.execute();
                } catch (final SBonitaException e) {
                    throw new SCommandExecutionException("No archived processInstance that involves user :" + userId
                            + " found during execution of method IsAllowedToSeeOverviewForm.", e);
                }
                final SearchResult<ArchivedProcessInstance> archivedRes = archivedSearcher.getResult();
                if (archivedRes.getCount() > 0) {
                    isHas = true;
                }
            }
        }
        return isHas;
    }

}
