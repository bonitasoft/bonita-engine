/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.dependency.impl;

import static org.bonitasoft.engine.home.BonitaResource.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.AbstractSDependency;
import org.bonitasoft.engine.dependency.model.SAbstractDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.home.BonitaResource;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractDependencyService implements DependencyService {

    protected static final int BATCH_SIZE = 100;
    private ReadPersistenceService persistenceService;

    public AbstractDependencyService(ReadPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    protected abstract void delete(AbstractSDependency dependency) throws SDependencyDeletionException;

    protected abstract List<AbstractSDependency> getDependencies(QueryOptions queryOptions) throws SDependencyException;

    protected abstract AbstractSDependency getDependency(String name)
            throws SDependencyNotFoundException, SDependencyDeletionException;

    @Override
    public void deleteDependency(final String name) throws SDependencyException {
        deleteDependency(getDependency(name));
    }

    @Override
    public Stream<BonitaResource> getDependenciesResources(final ScopeType type, final long id)
            throws SDependencyException {
        List<Long> dependencyIds = getDependencyIds(id, type, 0, Integer.MAX_VALUE);
        return dependencyIds.stream()
                .map(dependencyId -> {
                    try {
                        // get only the content of the dependency to avoid having connected objects
                        return getDependencyContentOnly(dependencyId);
                    } catch (SDependencyNotFoundException | SBonitaReadException e) {
                        throw new SBonitaRuntimeException(e);
                    }
                })
                .map(dependency -> resource(dependency.getFileName(), dependency.getContent()));
    }

    protected abstract void createDependencyMapping(SAbstractDependencyMapping dependencyMapping)
            throws SDependencyException;

    protected abstract void deleteDependencyMapping(SAbstractDependencyMapping dependencyMapping)
            throws SDependencyException;

    protected abstract List<SAbstractDependencyMapping> getDependencyMappings(long dependencyId,
            QueryOptions queryOptions) throws SDependencyException;

    @Override
    public List<Long> getDependencyIds(final long artifactId, final ScopeType artifactType, final int startIndex,
            final int maxResult)
            throws SDependencyException {
        NullCheckingUtil.checkArgsNotNull(artifactId, artifactType, startIndex, maxResult);
        final QueryOptions queryOptions = new QueryOptions(startIndex, maxResult);
        try {
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("artifactId", artifactId);
            parameters.put("artifactType", artifactType);
            final SelectListDescriptor<Long> desc = getSelectDescriptorForDependencyIds(queryOptions, parameters);
            return persistenceService.selectList(desc);
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("Can't get dependencies", e);
        }
    }

    protected abstract SelectListDescriptor<Long> getSelectDescriptorForDependencyIds(QueryOptions queryOptions,
            Map<String, Object> parameters);

    protected abstract QueryOptions getDefaultQueryOptionForDependencyMapping();

    @Override
    public void deleteDependency(final AbstractSDependency dependency) throws SDependencyException {
        for (SAbstractDependencyMapping dependencyMapping : getDependencyMappings(dependency.getId(),
                getDefaultQueryOptionForDependencyMapping())) {
            deleteDependencyMapping(dependencyMapping);
        }
        delete(dependency);
    }

    @Override
    public void deleteDependencies(final long id, final ScopeType type) throws SDependencyException {
        int fromIndex = 0;
        List<Long> dependencyIds = getDependencyIds(id, type, fromIndex, BATCH_SIZE);
        while (!dependencyIds.isEmpty()) {
            for (final Long dependencyId : dependencyIds) {
                final List<SAbstractDependencyMapping> dependencyMappings = getDependencyMappings(dependencyId,
                        getDefaultQueryOptionForDependencyMapping());
                if (dependencyMappings.size() == 1) {// only when the dependency is linked only to on element
                    final SAbstractDependencyMapping dependencyMapping = dependencyMappings.get(0);
                    deleteDependencyMapping(dependencyMapping);
                    deleteDependency(getDependency(dependencyId));
                } else {
                    fromIndex++;
                }
            }
            dependencyIds = getDependencyIds(id, type, fromIndex, BATCH_SIZE);
        }
    }
}
