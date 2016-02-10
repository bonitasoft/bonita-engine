/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorDeletionException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilder;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilderFactory;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.api.impl.transaction.actor.ExportActorMapping;
import org.bonitasoft.engine.api.impl.transaction.actor.ImportActorMapping;
import org.bonitasoft.engine.bpm.actor.ActorMappingImportException;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ActorBusinessArchiveArtifactManager implements BusinessArchiveArtifactManager {

    private final ActorMappingService actorMappingService;
    private final IdentityService identityService;
    private final TechnicalLoggerService technicalLoggerService;

    public ActorBusinessArchiveArtifactManager(ActorMappingService actorMappingService, IdentityService identityService,
                                               TechnicalLoggerService technicalLoggerService) {
        this.actorMappingService = actorMappingService;
        this.identityService = identityService;
        this.technicalLoggerService = technicalLoggerService;
    }

    @Override
    public boolean deploy(final BusinessArchive businessArchive, final SProcessDefinition processDefinition)
            throws ActorMappingImportException {
        BuilderFactory.getInstance();
        final SActorBuilderFactory sActorBuilderFactory = BuilderFactory.get(SActorBuilderFactory.class);
        final Set<SActorDefinition> actors = processDefinition.getActors();
        final Set<SActor> sActors = new HashSet<>(actors.size() + 1);
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
            ActorMapping actorMapping = businessArchive.getActorMapping();
            if (actorMapping != null) {
                final ImportActorMapping importActorMapping = new ImportActorMapping(actorMappingService,identityService);
                    importActorMapping.execute(actorMapping,processDefinition.getId());
            }
            // ignored
        } catch (SBonitaException e) {
            technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.WARNING, "Error in importing the actor-mapping", e);
        }
        return checkResolution(actorMappingService, processDefinition.getId()).isEmpty();
    }

    @Override
    public List<Problem> checkResolution(final SProcessDefinition processDefinition) {
        final long processDefinitionId = processDefinition.getId();
        return checkResolution(actorMappingService, processDefinitionId);
    }

    @Override
    public void delete(SProcessDefinition processDefinition) throws SObjectModificationException {
        try {
            actorMappingService.deleteActors(processDefinition.getId());
        } catch (SActorDeletionException e) {
            throw new SObjectModificationException("Unable to delete actors of the process definition <" + processDefinition.getName() + ">", e);
        }
    }

    @Override
    public void exportToBusinessArchive(long processDefinitionId, BusinessArchiveBuilder businessArchiveBuilder) throws SBonitaException {
        final ExportActorMapping exportActorMapping = new ExportActorMapping(actorMappingService, identityService, processDefinitionId);
        businessArchiveBuilder.setActorMapping(exportActorMapping.getActorMapping());
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