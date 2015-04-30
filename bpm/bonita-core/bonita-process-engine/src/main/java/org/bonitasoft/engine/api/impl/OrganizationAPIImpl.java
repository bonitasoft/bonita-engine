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
package org.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.api.impl.resolver.ActorProcessDependencyDeployer;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.external.identity.mapping.ExternalIdentityMappingService;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;

/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class OrganizationAPIImpl {

    private final TenantServiceAccessor tenantAccessor;

    private final int pageSize;

    public OrganizationAPIImpl(TenantServiceAccessor tenantAccessor, int pageSize) {
        this.tenantAccessor = tenantAccessor;
        this.pageSize = pageSize;
    }

    public void deleteOrganization() throws DeletionException {
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        try {
            final QueryOptions queryOptions = new QueryOptions(0, 1);
            boolean canDeleteOrganization = processInstanceService.getNumberOfProcessInstances(queryOptions) == 0
                    && activityInstanceService.getNumberOfHumanTasks(queryOptions) == 0
                    && commentService.getNumberOfComments(queryOptions) == 0;
            if (canDeleteOrganization) {
                deleteOrganizationElements(activityInstanceService);
                updateActorProcessDependenciesForAllActors(tenantAccessor);
            } else {
                throw new DeletionException("Can't delete a organization when a process, a human tasks, or a comment is active !!.");
            }
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    private void deleteOrganizationElements(final ActivityInstanceService activityInstanceService) throws SBonitaException {
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final ProfileService profileService = tenantAccessor.getProfileService();
        final SupervisorMappingService supervisorService = tenantAccessor.getSupervisorService();
        final ExternalIdentityMappingService externalIdentityMappingService = tenantAccessor.getExternalIdentityMappingService();

        deleteCustomUserInfo(identityService);
        actorMappingService.deleteAllActorMembers();
        profileService.deleteAllProfileMembers();
        activityInstanceService.deleteAllPendingMappings();
        supervisorService.deleteAllProcessSupervisors();
        externalIdentityMappingService.deleteAllExternalIdentityMappings();
        identityService.deleteAllUserMemberships();
        identityService.deleteAllGroups();
        identityService.deleteAllRoles();
        identityService.deleteAllUsers();
    }

    private void deleteCustomUserInfo(IdentityService identityService) throws SIdentityException {
        // only definitions will be deleted because values are deleted on cascade from DB
        List<SCustomUserInfoDefinition> customUserInfoDefinitions = null;
        do {
            // the start index is always zero because the curent page will be deleted
            customUserInfoDefinitions = identityService.getCustomUserInfoDefinitions(0, pageSize);
            deleteCustomUserInfo(customUserInfoDefinitions, identityService);
        } while (customUserInfoDefinitions.size() == pageSize);
    }

    private void deleteCustomUserInfo(List<SCustomUserInfoDefinition> customUserInfoDefinitions, IdentityService identityService) throws SIdentityException {
        for (SCustomUserInfoDefinition definition : customUserInfoDefinitions) {
            identityService.deleteCustomUserInfoDefinition(definition.getId());
        }

    }

    /**
     * Check / update process resolution information, for all processes in a list of actor IDs.
     */
    private void updateActorProcessDependenciesForAllActors(final TenantServiceAccessor tenantAccessor) throws SBonitaException {
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        List<Long> processDefinitionIds;
        final ActorProcessDependencyDeployer dependencyResolver = new ActorProcessDependencyDeployer();
        do {
            processDefinitionIds = processDefinitionService.getProcessDefinitionIds(0, 100);
            for (final Long processDefinitionId : processDefinitionIds) {
                tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor, dependencyResolver);
            }
        } while (processDefinitionIds.size() == 100);
    }

}
