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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ClasspathArtifactManager implements BusinessArchiveArtifactManager {

    private final DependencyService dependencyService;

    public ClasspathArtifactManager(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @Override
    public boolean deploy(final BusinessArchive businessArchive, final SProcessDefinition processDefinition)
            throws ConnectorException, SBonitaException {
        final Map<String, byte[]> resources = businessArchive.getResources("^classpath/.*$");

        // remove the classpath/ on path of dependencies
        final Map<String, byte[]> resourcesWithRealName = new HashMap<>(resources.size());
        for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
            final String name = resource.getKey().substring(10);
            final byte[] jarContent = resource.getValue();
            resourcesWithRealName.put(name, jarContent);
        }
        addDependencies(resourcesWithRealName, dependencyService, processDefinition.getId());
        return true;
    }

    @Override
    public List<Problem> checkResolution(final SProcessDefinition processDefinition) {
        return Collections.emptyList();
    }

    @Override
    public void delete(SProcessDefinition processDefinition) throws SObjectModificationException {
        try {
            dependencyService.deleteDependencies(processDefinition.getId(), ScopeType.PROCESS);
        } catch (SDependencyException e) {
            throw new SObjectModificationException("Unable to delete dependencies of process " + processDefinition.getId(), e);
        }

    }

    @Override
    public void exportToBusinessArchive(long processDefinitionId, BusinessArchiveBuilder businessArchiveBuilder) throws SBonitaException {
        final ArrayList<FilterOption> filters = new ArrayList<>();
        filters.add(new FilterOption(SDependencyMapping.class, "artifactId", processDefinitionId));
        filters.add(new FilterOption(SDependencyMapping.class, "artifactType", ScopeType.PROCESS.name()));
        final List<SDependencyMapping> dependencyMappings = dependencyService
                .getDependencyMappings(new QueryOptions(0, Integer.MAX_VALUE, null, filters, null));
        for (SDependencyMapping dependencyMapping : dependencyMappings) {
            final SDependency dependency = dependencyService.getDependency(dependencyMapping.getDependencyId());
            businessArchiveBuilder.addClasspathResource(new BarResource(dependency.getFileName(), dependency.getValue()));
        }
    }

    private void addDependencies(final Map<String, byte[]> resources, final DependencyService dependencyService,
            final long processDefinitionId) throws SBonitaException {
        final List<Long> dependencyIds = getDependencyMappingsOfProcess(dependencyService, processDefinitionId);
        final List<String> dependencies = getDependenciesOfProcess(dependencyService, dependencyIds);

        for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
            if (!dependencies.contains(getDependencyName(processDefinitionId, entry.getKey()))) {
                final String name = entry.getKey();
                dependencyService.createMappedDependency(name, entry.getValue(), name /* it is the real filename */, processDefinitionId, ScopeType.PROCESS);
            }
        }
    }

    private String getDependencyName(final long processDefinitionId, final String name) {
        return processDefinitionId + "_" + name;
    }

    private List<String> getDependenciesOfProcess(final DependencyService dependencyService, final List<Long> dependencyIds) throws SBonitaException {
        if (dependencyIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<SDependency> dependencies = dependencyService.getDependencies(dependencyIds);
        final ArrayList<String> dependencyNames = new ArrayList<>(dependencies.size());
        for (final SDependency sDependency : dependencies) {
            dependencyNames.add(sDependency.getName());
        }
        return dependencyNames;
    }

    private List<Long> getDependencyMappingsOfProcess(final DependencyService dependencyService, final long processDefinitionId) throws SDependencyException {
        final List<Long> dependencyIds = new ArrayList<>();
        int fromIndex = 0;
        final int pageSize = 100;
        List<Long> currentPage;
        do {
            currentPage = dependencyService.getDependencyIds(processDefinitionId, ScopeType.PROCESS, fromIndex, pageSize);
            dependencyIds.addAll(currentPage);
            fromIndex += pageSize;
        } while (currentPage.size() == pageSize);
        return dependencyIds;
    }
}
