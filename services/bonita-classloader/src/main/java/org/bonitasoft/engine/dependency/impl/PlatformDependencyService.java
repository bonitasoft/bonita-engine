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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.dependency.SDependencyCreationException;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.AbstractSDependency;
import org.bonitasoft.engine.dependency.model.DependencyContent;
import org.bonitasoft.engine.dependency.model.SAbstractDependencyMapping;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.SPlatformDependency;
import org.bonitasoft.engine.dependency.model.SPlatformDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class PlatformDependencyService extends AbstractDependencyService {

    private final PersistenceService platformPersistenceService;

    public PlatformDependencyService(final PersistenceService platformPersistenceService) {
        super(platformPersistenceService);
        this.platformPersistenceService = platformPersistenceService;
    }

    @Override
    public List<AbstractSDependency> getDependencies(final Collection<Long> ids) throws SDependencyException {
        final Map<String, Object> parameters = Collections.singletonMap("ids", ids);
        final QueryOptions queryOptions = new QueryOptions(0, ids.size(), SPlatformDependency.class, "id",
                OrderByType.ASC);
        try {
            return platformPersistenceService.selectList(new SelectListDescriptor<>("getPlatformDependenciesById",
                    parameters, SPlatformDependency.class, queryOptions));
        } catch (final SBonitaReadException bre) {
            throw new SDependencyException(bre);
        }
    }

    @Override
    protected void delete(AbstractSDependency dependency) throws SDependencyDeletionException {
        try {
            platformPersistenceService.delete(dependency);
        } catch (final SPersistenceException pe) {
            throw new SDependencyDeletionException(pe);
        }
    }

    @Override
    protected List<AbstractSDependency> getDependencies(QueryOptions queryOptions) throws SDependencyException {
        List<AbstractSDependency> dependencies;
        try {
            dependencies = platformPersistenceService.selectList(new SelectListDescriptor<>("getPlatformDependencies",
                    Collections.emptyMap(), SPlatformDependency.class, queryOptions));
        } catch (final SBonitaReadException bre) {
            throw new SDependencyException(bre);
        }
        return dependencies;
    }

    @Override
    public AbstractSDependency getDependency(final long id) throws SDependencyNotFoundException {
        final SelectByIdDescriptor<SPlatformDependency> selectByIdDescriptor = new SelectByIdDescriptor<>(
                SPlatformDependency.class, id);
        try {
            final SPlatformDependency sDependency = platformPersistenceService.selectById(selectByIdDescriptor);
            if (sDependency == null) {
                throw new SDependencyNotFoundException("No dependency exists using id: " + id);
            }
            return sDependency;
        } catch (final SBonitaReadException bre) {
            throw new SDependencyNotFoundException(bre);
        }
    }

    @Override
    public DependencyContent getDependencyContentOnly(final long id)
            throws SDependencyNotFoundException, SBonitaReadException {
        NullCheckingUtil.checkArgsNotNull(id);
        SelectOneDescriptor<DependencyContent> desc = new SelectOneDescriptor<>("getPlatformDependencyContentOnly",
                Collections.singletonMap("id", id), SPlatformDependency.class, DependencyContent.class);
        return Optional.ofNullable(platformPersistenceService.selectOne(desc))
                .orElseThrow(() -> new SDependencyNotFoundException("Can't get content of dependency with id: " + id));
    }

    @Override
    protected AbstractSDependency getDependency(final String name) throws SDependencyNotFoundException {
        final Map<String, Object> parameters = Collections.singletonMap("name", name);
        try {
            final SPlatformDependency sDependency = platformPersistenceService.selectOne(new SelectOneDescriptor<>(
                    "getPlatformDependencyByName", parameters, SPlatformDependency.class));
            if (sDependency == null) {
                throw new SDependencyNotFoundException("No dependency exists using name: " + name);
            }
            return sDependency;
        } catch (final SBonitaReadException bre) {
            throw new SDependencyNotFoundException(bre);
        }
    }

    @Override
    protected void createDependencyMapping(final SAbstractDependencyMapping dependencyMapping)
            throws SDependencyException {
        try {
            platformPersistenceService.insert(dependencyMapping);
        } catch (final SPersistenceException pe) {
            throw new SDependencyException(pe);
        }
    }

    @Override
    protected void deleteDependencyMapping(final SAbstractDependencyMapping dependencyMapping)
            throws SDependencyException {
        try {
            platformPersistenceService.delete(dependencyMapping);
        } catch (final SPersistenceException pe) {
            throw new SDependencyException(pe);
        }
    }

    @Override
    public List<SDependencyMapping> getDependencyMappings(final QueryOptions queryOptions) throws SDependencyException {
        try {
            return platformPersistenceService
                    .selectList(new SelectListDescriptor<>("getPlatformDependencyMappings", null,
                            SPlatformDependencyMapping.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("can't get dependency mappings", e);
        }
    }

    @Override
    protected List<SAbstractDependencyMapping> getDependencyMappings(final long dependencyId,
            final QueryOptions queryOptions) throws SDependencyException {
        try {
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("dependencyId", dependencyId);
            final SelectListDescriptor<SAbstractDependencyMapping> desc = new SelectListDescriptor<>(
                    "getPlatformDependencyMappingsByDependency",
                    parameters, SPlatformDependencyMapping.class, queryOptions);
            return platformPersistenceService.selectList(desc);
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("can't get dependency mappings by dependencyId: " + dependencyId, e);
        }
    }

    @Override
    protected SelectListDescriptor<Long> getSelectDescriptorForDependencyIds(QueryOptions queryOptions,
            Map<String, Object> parameters) {
        return new SelectListDescriptor<>("getPlatformDependencyIds", parameters, SPlatformDependency.class,
                Long.class, queryOptions);
    }

    @Override
    public AbstractSDependency createMappedDependency(String name, byte[] jarContent, String fileName, long artifactId,
            ScopeType scopeType)
            throws SDependencyException {
        final SPlatformDependency sDependency = new SPlatformDependency(name, fileName, jarContent);
        NullCheckingUtil.checkArgsNotNull(sDependency);
        try {
            platformPersistenceService.insert(sDependency);
        } catch (final SPersistenceException pe) {
            throw new SDependencyCreationException(pe);
        }
        final SPlatformDependencyMapping sDependencyMapping = new SPlatformDependencyMapping(artifactId, scopeType,
                sDependency.getId());
        createDependencyMapping(sDependencyMapping);
        return sDependency;
    }

    @Override
    public SDependency getDependencyOfArtifact(long artifactId, ScopeType artifactType, String fileName) {
        return null;
    }

    @Override
    public Optional<Long> getIdOfDependencyOfArtifact(Long artifactId, ScopeType artifactType, String fileName) {
        return Optional.empty();
    }

    @Override
    protected QueryOptions getDefaultQueryOptionForDependencyMapping() {
        return new QueryOptions(0, 100, SPlatformDependencyMapping.class, "id", OrderByType.ASC);
    }

    @Override
    public SDependency updateDependencyOfArtifact(String name, byte[] jarContent, String fileName, long artifactId,
            ScopeType scopeType) {
        throw new UnsupportedOperationException("NYI");
    }
}
