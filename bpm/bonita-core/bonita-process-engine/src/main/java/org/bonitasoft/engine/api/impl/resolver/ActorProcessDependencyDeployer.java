
/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.api.impl.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilder;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilderFactory;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.api.impl.transaction.actor.ImportActorMapping;
import org.bonitasoft.engine.bpm.actor.ActorMappingImportException;
import org.bonitasoft.engine.bpm.bar.ActorMappingContribution;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ActorProcessDependencyDeployer implements ProcessDependencyDeployer {

    @Override
    public boolean deploy(final TenantServiceAccessor tenantAccessor, final BusinessArchive businessArchive, final SProcessDefinition processDefinition)
            throws ActorMappingImportException {
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        BuilderFactory.getInstance();
        final SActorBuilderFactory sActorBuilderFactory = BuilderFactory.get(SActorBuilderFactory.class);
        final IdentityService identityService = tenantAccessor.getIdentityService();
        final Set<SActorDefinition> actors = processDefinition.getActors();
        final Set<SActor> sActors = new HashSet<SActor>(actors.size() + 1);
        final SActorDefinition actorInitiator = processDefinition.getActorInitiator();
        String initiatorName = null;
        if (actorInitiator != null) {
            initiatorName = actorInitiator.getName();
            final SActorBuilder sActorBuilder = sActorBuilderFactory.create(initiatorName, processDefinition.getId(), true);
            sActorBuilder.addDescription(actorInitiator.getDescription());
            sActors.add(sActorBuilder.getActor());
        }
        for (final SActorDefinition actor : actors) {
            if (initiatorName == null || !initiatorName.equals(actor.getName())) {
                final SActorBuilder sActorBuilder = sActorBuilderFactory.create(actor.getName(), processDefinition.getId(), false);
                sActorBuilder.addDescription(actor.getDescription());
                sActors.add(sActorBuilder.getActor());
            }
        }
        try {
            actorMappingService.addActors(sActors);
            final byte[] actorMappingXML = businessArchive.getResource(ActorMappingContribution.ACTOR_MAPPING_FILE);
            if (actorMappingXML != null) {
                final String actorMapping = new String(actorMappingXML);
                final ImportActorMapping importActorMapping = new ImportActorMapping(actorMappingService, identityService, processDefinition.getId(),
                        actorMapping);
                try {
                    importActorMapping.execute();
                } catch (final SBonitaException e) {
                    // ignore
                }
            }
        } catch (final SBonitaException e) {
            // ignored
        }
        return checkResolution(actorMappingService, processDefinition.getId()).isEmpty();
    }

    @Override
    public List<Problem> checkResolution(final TenantServiceAccessor tenantAccessor, final SProcessDefinition processDefinition) {
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final long processDefinitionId = processDefinition.getId();
        return checkResolution(actorMappingService, processDefinitionId);
    }

    public List<Problem> checkResolution(final ActorMappingService actorMappingService, final long processDefinitionId) {
        try {
            final List<Problem> problems = new ArrayList<Problem>();
            QueryOptions queryOptions = new QueryOptions(0, 100, SActor.class, "id", OrderByType.ASC);
            List<SActor> actors = actorMappingService.getActors(processDefinitionId, queryOptions);
            while (!actors.isEmpty()) {
                for (final SActor sActor : actors) {
                    checkIfAActorMemberExists(actorMappingService, problems, sActor);
                }
                queryOptions = QueryOptions.getNextPage(queryOptions);
                actors = actorMappingService.getActors(processDefinitionId, queryOptions);
            }
            return problems;
        } catch (final SBonitaReadException e) {
            return Collections.singletonList((Problem) new ProblemImpl(Level.ERROR, processDefinitionId, "process", "Unable to read actors"));
        }
    }

    private void checkIfAActorMemberExists(final ActorMappingService actorMappingService, final List<Problem> problems, final SActor sActor)
            throws SBonitaReadException {
        final List<SActorMember> actorMembers = actorMappingService.getActorMembers(sActor.getId(), 0, 1);
        if (actorMembers.isEmpty()) {
            final Problem problem = new ProblemImpl(Level.ERROR, sActor.getId(), "actor", "Actor '" + sActor.getName()
                    + "' does not contain any members");
            problems.add(problem);
        }
    }
}
