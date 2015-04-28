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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.impl.transaction.dependency.AddSDependency;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilderFactory;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyCreationException;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
public class DependencyResolver {

    private static final int BATCH_SIZE = 100;

    private final List<ProcessDependencyDeployer> dependencyResolvers;

    public DependencyResolver(final List<ProcessDependencyDeployer> dependencyResolvers) {
        this.dependencyResolvers = dependencyResolvers;
    }

    public boolean resolveDependencies(final BusinessArchive businessArchive, final TenantServiceAccessor tenantAccessor, final SProcessDefinition sDefinition) {
        final List<ProcessDependencyDeployer> resolvers = getResolvers();
        boolean resolved = true;
        for (final ProcessDependencyDeployer resolver : resolvers) {
            try {
                resolved &= resolver.deploy(tenantAccessor, businessArchive, sDefinition);
                if (!resolved) {
                    for (Problem problem : resolver.checkResolution(tenantAccessor, sDefinition)) {
                        tenantAccessor.getTechnicalLoggerService().log(DependencyResolver.class, INFO, problem.getDescription());
                    }
                }
            } catch (final BonitaException e) {
                // not logged, we will check later why the process is not resolved
                resolved = false;
            }
        }
        return resolved;
    }

    public void resolveDependenciesForAllProcesses(TenantServiceAccessor tenantAccessor) {
        final TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        try {
            List<Long> processDefinitionIds = tenantAccessor.getProcessDefinitionService().getProcessDefinitionIds(0, Integer.MAX_VALUE);
            resolveDependencies(processDefinitionIds, tenantAccessor);
        } catch (SProcessDefinitionReadException e) {
            loggerService.log(DependencyResolver.class, ERROR, "Unable to retrieve tenant process definitions, dependency resolution aborted");
        }
    }

    private void resolveDependencies(final List<Long> processDefinitionIds, final TenantServiceAccessor tenantAccessor) {
        for (Long id : processDefinitionIds) {
            resolveDependencies(id, tenantAccessor);
        }
    }

    /*
     * Done in a separated transaction
     * We try here to check if now the process is resolved so it must not be done in the same transaction that did the modification
     * this does not throw exception, it only log because it can be retried after.
     */
    public void resolveDependencies(final long processDefinitionId, final TenantServiceAccessor tenantAccessor) {
        final List<ProcessDependencyDeployer> resolvers = getResolvers();
        resolveDependencies(processDefinitionId, tenantAccessor, resolvers.toArray(new ProcessDependencyDeployer[resolvers.size()]));
    }

    public void resolveDependencies(final long processDefinitionId, final TenantServiceAccessor tenantAccessor, final ProcessDependencyDeployer... resolvers) {
        final TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        try {
            boolean resolved = true;
            for (final ProcessDependencyDeployer dependencyResolver : resolvers) {
                final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                resolved &= dependencyResolver.checkResolution(tenantAccessor, processDefinition).isEmpty();
            }
            changeResolutionStatus(processDefinitionId, tenantAccessor, processDefinitionService, dependencyService, resolved);
        } catch (final SBonitaException e) {
            final Class<DependencyResolver> clazz = DependencyResolver.class;
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
                resolveAndCreateDependencies(tenantAccessor.getTenantId(), processDefinitionService, dependencyService, processDefinitionId);
            }
        } else {
            if (ConfigurationState.RESOLVED.name().equals(processDefinitionDeployInfo.getConfigurationState())) {
                final EntityUpdateDescriptor updateDescriptor = BuilderFactory.get(SProcessDefinitionDeployInfoUpdateBuilderFactory.class).createNewInstance()
                        .updateConfigurationState(ConfigurationState.UNRESOLVED).done();
                processDefinitionService.updateProcessDefinitionDeployInfo(processDefinitionId, updateDescriptor);
            }
        }
    }

    /**
     * create dependencies based on the bonita home
     * 
     * @param processDefinitionService
     * @param dependencyService
     * @param processDefinitionId
     * @throws SBonitaException
     */
    public void resolveAndCreateDependencies(final long tenantId, final ProcessDefinitionService processDefinitionService,
            final DependencyService dependencyService, final long processDefinitionId) throws SBonitaException {
        Map<String, byte[]> resources = null;
                try {
        resources = BonitaHomeServer.getInstance().getProcessClasspath(tenantId, processDefinitionId);
                } catch (final IOException e) {
                    throw new SDependencyCreationException(e);
        } catch (BonitaHomeNotSetException e) {
            throw new SDependencyCreationException(e);
        }
        addDependencies(resources, dependencyService, processDefinitionId);
        processDefinitionService.resolveProcess(processDefinitionId);
    }

    private String getDependencyName(final long processDefinitionId, final String name) {
        return processDefinitionId + "_" + name;
    }

    private void addDependencies(final Map<String, byte[]> resources, final DependencyService dependencyService,
            final long processDefinitionId) throws SBonitaException {
        final List<Long> dependencyIds = getDependencyMappingsOfProcess(dependencyService, processDefinitionId);
        final List<String> dependencies = getDependenciesOfProcess(dependencyService, dependencyIds);

        final Iterator<Entry<String, byte[]>> iterator = resources.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<java.lang.String, byte[]> entry = iterator.next();
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

    /**
     * create dependencies based on the business archive
     * 
     * @param businessArchive
     * @param processDefinitionService
     * @param dependencyService
     * @param sDefinition
     * @throws SBonitaException
     */
    public void resolveAndCreateDependencies(final BusinessArchive businessArchive, final ProcessDefinitionService processDefinitionService,
            final DependencyService dependencyService, final SProcessDefinition sDefinition) throws SBonitaException {
        final Long processDefinitionId = sDefinition.getId();
        if (businessArchive != null) {
            final Map<String, byte[]> resources = businessArchive.getResources("^classpath/.*$");

            // remove the classpath/ on path of dependencies
            final Map<String, byte[]> resourcesWithRealName = new HashMap<String, byte[]>(resources.size());
            for (final Entry<String, byte[]> resource : resources.entrySet()) {
                final String name = resource.getKey().substring(10);
                final byte[] jarContent = resource.getValue();
                resourcesWithRealName.put(name, jarContent);
            }
            addDependencies(resourcesWithRealName, dependencyService, sDefinition.getId());
        }
        processDefinitionService.resolveProcess(processDefinitionId);
    }

    private void addDependency(final String name, final byte[] jarContent, final DependencyService dependencyService,
            final long processdefinitionId) throws SDependencyException {
        final AddSDependency addSDependency = new AddSDependency(dependencyService, name, jarContent, processdefinitionId, ScopeType.PROCESS);
        addSDependency.execute();
    }

    public List<ProcessDependencyDeployer> getResolvers() {
        return dependencyResolvers;
    }
}
