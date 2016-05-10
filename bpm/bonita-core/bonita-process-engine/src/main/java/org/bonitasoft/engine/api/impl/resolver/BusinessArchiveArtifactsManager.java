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
package org.bonitasoft.engine.api.impl.resolver;

import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.ERROR;
import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.INFO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilderFactory;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Handles the resolution of Process Dependencies. A process can have a list of <code>ProcessDependencyResolver</code>s which validates different aspects of the
 * process to validate (or "resolve")
 *
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class BusinessArchiveArtifactsManager {

    private static final int BATCH_SIZE = 100;

    private final List<BusinessArchiveArtifactManager> dependencyResolvers;
    private final TechnicalLoggerService technicalLoggerService;

    public BusinessArchiveArtifactsManager(final List<BusinessArchiveArtifactManager> dependencyResolvers, TechnicalLoggerService technicalLoggerService) {
        this.dependencyResolvers = dependencyResolvers;
        this.technicalLoggerService = technicalLoggerService;
    }

    public boolean resolveDependencies(final BusinessArchive businessArchive, final SProcessDefinition sDefinition) {
        final List<BusinessArchiveArtifactManager> artifactManagers = getArtifactManagers();
        boolean resolved = true;
        for (final BusinessArchiveArtifactManager artifactManager : artifactManagers) {
            try {
                resolved &= artifactManager.deploy(businessArchive, sDefinition);
                if (!resolved) {
                    for (Problem problem : artifactManager.checkResolution(sDefinition)) {
                        technicalLoggerService.log(BusinessArchiveArtifactsManager.class, INFO, problem.getDescription());
                    }
                }
            } catch (BonitaException | SBonitaException e) {
                // not logged, we will check later why the process is not resolved
                technicalLoggerService.log(BusinessArchiveArtifactsManager.class, ERROR, "Unable to deploy process", e);
                resolved = false;
            }
        }
        return resolved;
    }

    public void resolveDependenciesForAllProcesses(TenantServiceAccessor tenantAccessor) {
        try {
            List<Long> processDefinitionIds = tenantAccessor.getProcessDefinitionService().getProcessDefinitionIds(0, Integer.MAX_VALUE);
            resolveDependencies(processDefinitionIds, tenantAccessor);
        } catch (SBonitaReadException e) {
            technicalLoggerService.log(BusinessArchiveArtifactsManager.class, ERROR,
                    "Unable to retrieve tenant process definitions, dependency resolution aborted");
        }
    }

    private void resolveDependencies(final List<Long> processDefinitionIds, final TenantServiceAccessor tenantAccessor) {
        for (Long id : processDefinitionIds) {
            resolveDependencies(id, tenantAccessor);
        }
    }

    public void deleteDependencies(final SProcessDefinition processDefinition) throws SObjectModificationException, SBonitaReadException, SRecorderException {
        final List<BusinessArchiveArtifactManager> resolvers = getArtifactManagers();
        for (BusinessArchiveArtifactManager resolver : resolvers) {
            resolver.delete(processDefinition);
        }
    }

    /*
     * Done in a separated transaction
     * We try here to check if now the process is resolved so it must not be done in the same transaction that did the modification
     * this does not throw exception, it only log because it can be retried after.
     */
    public void resolveDependencies(final long processDefinitionId, final TenantServiceAccessor tenantAccessor) {
        resolveDependencies(processDefinitionId, tenantAccessor, getArtifactManagers().toArray(new BusinessArchiveArtifactManager[getArtifactManagers().size()]));
    }

    public void resolveDependencies(final long processDefinitionId, final TenantServiceAccessor tenantAccessor,
                                    final BusinessArchiveArtifactManager... resolvers) {
        final TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        try {
            boolean resolved = true;
            for (final BusinessArchiveArtifactManager dependencyResolver : resolvers) {
                final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                resolved &= dependencyResolver.checkResolution(processDefinition).isEmpty();
            }
            changeResolutionStatus(processDefinitionId, tenantAccessor, processDefinitionService, dependencyService, resolved);
        } catch (final SBonitaException e) {
            final Class<BusinessArchiveArtifactsManager> clazz = BusinessArchiveArtifactsManager.class;
            if (loggerService.isLoggable(clazz, TechnicalLogSeverity.DEBUG)) {
                loggerService.log(clazz, TechnicalLogSeverity.DEBUG, e);
            }
            if (loggerService.isLoggable(clazz, TechnicalLogSeverity.WARNING)) {
                loggerService.log(clazz, TechnicalLogSeverity.WARNING, "Unable to resolve dependencies after they were modified because of " + e.getMessage()
                        + ". Please retry it manually");
            }
        }
    }

    private void changeResolutionStatus(final long processDefinitionId, final TenantServiceAccessor tenantAccessor,
                                        final ProcessDefinitionService processDefinitionService, final DependencyService dependencyService,
                                        final boolean resolved) throws SBonitaException {
        final SProcessDefinitionDeployInfo processDefinitionDeployInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);
        if (resolved) {
            if (ConfigurationState.UNRESOLVED.name().equals(processDefinitionDeployInfo.getConfigurationState())) {
                processDefinitionService.resolveProcess(processDefinitionId);
            }
        } else {
            if (ConfigurationState.RESOLVED.name().equals(processDefinitionDeployInfo.getConfigurationState())) {
                final EntityUpdateDescriptor updateDescriptor = BuilderFactory.get(SProcessDefinitionDeployInfoUpdateBuilderFactory.class).createNewInstance()
                        .updateConfigurationState(ConfigurationState.UNRESOLVED).done();
                processDefinitionService.updateProcessDefinitionDeployInfo(processDefinitionId, updateDescriptor);
            }
        }
    }

    private String getDependencyName(final long processDefinitionId, final String name) {
        return processDefinitionId + "_" + name;
    }

    private void addDependencies(final Map<String, byte[]> resources, final DependencyService dependencyService,
                                 final long processDefinitionId) throws SBonitaException {
        final List<Long> dependencyIds = getDependencyMappingsOfProcess(dependencyService, processDefinitionId);
        final List<String> dependencies = getDependenciesOfProcess(dependencyService, dependencyIds);

        for (Entry<String, byte[]> entry : resources.entrySet()) {
            if (!dependencies.contains(getDependencyName(processDefinitionId, entry.getKey()))) {
                addDependency(entry.getKey(), entry.getValue(), dependencyService, processDefinitionId);
            }
        }

    }

    private List<String> getDependenciesOfProcess(final DependencyService dependencyService, final List<Long> dependencyIds) throws SBonitaException {
        if (dependencyIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<SDependency> dependencies = dependencyService.getDependencies(dependencyIds);
        final ArrayList<String> dependencyNames = new ArrayList<String>(dependencies.size());
        for (final SDependency sDependency : dependencies) {
            dependencyNames.add(sDependency.getName());
        }
        return dependencyNames;
    }

    private List<Long> getDependencyMappingsOfProcess(final DependencyService dependencyService, final long processDefinitionId) throws SDependencyException {
        final List<Long> dependencyIds = new ArrayList<Long>();
        int fromIndex = 0;
        List<Long> currentPage;
        do {
            currentPage = dependencyService.getDependencyIds(processDefinitionId, ScopeType.PROCESS, fromIndex, BATCH_SIZE);
            dependencyIds.addAll(currentPage);
            fromIndex = fromIndex + BATCH_SIZE;
        } while (currentPage.size() == BATCH_SIZE);
        return dependencyIds;
    }

    private void addDependency(final String name, final byte[] jarContent, final DependencyService dependencyService,
                               final long processDefinitionId) throws SDependencyException {
        dependencyService.createMappedDependency(name, jarContent, name + ".jar", processDefinitionId, ScopeType.PROCESS);
    }

    public List<BusinessArchiveArtifactManager> getArtifactManagers() {
        return dependencyResolvers;
    }

    public BusinessArchive exportBusinessArchive(long processDefinitionId, DesignProcessDefinition designProcessDefinition)
            throws InvalidBusinessArchiveFormatException, SBonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(designProcessDefinition);
        for (BusinessArchiveArtifactManager businessArchiveArtifactManager : getArtifactManagers()) {
            businessArchiveArtifactManager.exportToBusinessArchive(processDefinitionId, businessArchiveBuilder);
        }
        return businessArchiveBuilder.done();
    }
}
