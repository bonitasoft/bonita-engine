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
package org.bonitasoft.engine.dependency.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.dependency.SDependencyCreationException;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.SPlatformDependency;
import org.bonitasoft.engine.dependency.model.SPlatformDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyMappingBuilderFactory;
import org.bonitasoft.engine.home.BonitaResource;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class PlatformDependencyServiceImpl extends AbstractDependencyService {

    private final PersistenceService platformPersistenceService;

    private final ClassLoaderService classLoaderService;

    public PlatformDependencyServiceImpl(final PersistenceService platformPersistenceService, final ClassLoaderService classLoaderService,
            BroadcastService broadcastService, UserTransactionService userTransactionService) {
        super(broadcastService, userTransactionService, platformPersistenceService);
        this.platformPersistenceService = platformPersistenceService;
        this.classLoaderService = classLoaderService;
    }

    @Override
    public List<SDependency> getDependencies(final Collection<Long> ids) throws SDependencyException {
        final Map<String, Object> parameters = Collections.singletonMap("ids", (Object) ids);
        final QueryOptions queryOptions = new QueryOptions(0, ids.size(), SPlatformDependency.class, "id", OrderByType.ASC);
        try {
            return platformPersistenceService.selectList(new SelectListDescriptor<SDependency>("getPlatformDependenciesById",
                    parameters, SPlatformDependency.class, queryOptions));
        } catch (final SBonitaReadException bre) {
            throw new SDependencyException(bre);
        }
    }

    @Override
    protected void delete(SDependency dependency) throws SDependencyDeletionException {
        try {
            platformPersistenceService.delete(dependency);
        } catch (final SPersistenceException pe) {
            throw new SDependencyDeletionException(pe);
        }
    }

    @Override
    protected List<SDependency> getDependencies(QueryOptions queryOptions) throws SDependencyException {
        List<SDependency> dependencies;
        try {
            dependencies = platformPersistenceService.selectList(new SelectListDescriptor<SDependency>("getPlatformDependencies",
                    Collections.<String, Object> emptyMap(), SDependency.class, queryOptions));
        } catch (final SBonitaReadException bre) {
            throw new SDependencyException(bre);
        }
        return dependencies;
    }

    @Override
    public SDependency getDependency(final long id) throws SDependencyNotFoundException {
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
    protected SDependency getDependency(final String name) throws SDependencyNotFoundException {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) name);
        try {
            final SPlatformDependency sDependency = platformPersistenceService.selectOne(new SelectOneDescriptor<SPlatformDependency>(
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
    protected void createDependencyMapping(final SDependencyMapping dependencyMapping) throws SDependencyException {
        try {
            platformPersistenceService.insert(dependencyMapping);
        } catch (final SPersistenceException pe) {
            throw new SDependencyException(pe);
        }
    }

    @Override
    protected void deleteDependencyMapping(final SDependencyMapping dependencyMapping) throws SDependencyException {
        try {
            platformPersistenceService.delete(dependencyMapping);
        } catch (final SPersistenceException pe) {
            throw new SDependencyException(pe);
        }
    }

    @Override
    public List<SDependencyMapping> getDependencyMappings(final QueryOptions queryOptions) throws SDependencyException {
        try {
            return platformPersistenceService.selectList(new SelectListDescriptor<SDependencyMapping>("getPlatformDependencyMappings", null,
                    SPlatformDependencyMapping.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("can't get dependency mappings", e);
        }
    }

    @Override
    protected List<SDependencyMapping> getDependencyMappings(final long dependencyId, final QueryOptions queryOptions) throws SDependencyException {
        try {
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("dependencyId", dependencyId);
            final SelectListDescriptor<SDependencyMapping> desc = new SelectListDescriptor<>("getPlatformDependencyMappingsByDependency",
                    parameters, SPlatformDependencyMapping.class, queryOptions);
            return platformPersistenceService.selectList(desc);
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("can't get dependency mappings by dependencyId: " + dependencyId, e);
        }
    }

    @Override
    protected SelectListDescriptor<Long> getSelectDescriptorForDependencyIds(QueryOptions queryOptions, Map<String, Object> parameters) {
        return new SelectListDescriptor<>("getPlatformDependencyIds", parameters, SPlatformDependency.class,
                Long.class, queryOptions);
    }

    @Override
    public SDependency createMappedDependency(String name, byte[] jarContent, String fileName, long artifactId, ScopeType scopeType)
            throws SDependencyException {
        final SDependency sDependency = BuilderFactory.get(SPlatformDependencyBuilderFactory.class)
                .createNewInstance(name, fileName, jarContent)
                .done();
        NullCheckingUtil.checkArgsNotNull(sDependency);
        try {
            platformPersistenceService.insert(sDependency);
        } catch (final SPersistenceException pe) {
            throw new SDependencyCreationException(pe);
        }
        final SDependencyMapping sDependencyMapping = BuilderFactory.get(SPlatformDependencyMappingBuilderFactory.class)
                .createNewInstance(sDependency.getId(), artifactId, scopeType).done();
        createDependencyMapping(sDependencyMapping);
        return sDependency;
    }

    @Override
    public SDependency getDependencyOfArtifact(long artifactId, ScopeType artifactType, String fileName) {
        return null;
    }

    @Override
    public Optional<Long> getIdOfDependencyOfArtifact(Long artifactId, ScopeType artifactType, String fileName) throws SBonitaReadException {
        return Optional.empty();
    }


    @Override
    protected QueryOptions getDefaultQueryOptionForDependencyMapping() {
        return new QueryOptions(0, 100, SPlatformDependencyMapping.class, "id", OrderByType.ASC);
    }

    @Override
    protected AbstractRefreshClassLoaderTask getRefreshClassLoaderTask(final ScopeType type, final long id) {
        return new RefreshPlatformClassLoaderTask(type, id);
    }

    @Override
    protected Long getTenantId() throws STenantIdNotSetException {
        return null;
    }

    @Override
    public void refreshClassLoader(final ScopeType type, final long id) throws SDependencyException {
        final Stream<BonitaResource> resources = getDependenciesResources(type, id);
        try {
            classLoaderService.refreshGlobalClassLoader(resources);
        } catch (final SClassLoaderException e) {
            throw new SDependencyException("can't refresh global classLoader", e);
        }
    }

    @Override
    public SDependency updateDependencyOfArtifact(String name, byte[] jarContent, String fileName, long artifactId, ScopeType scopeType)
            throws SDependencyException {
        throw new UnsupportedOperationException("NYI");
    }
}
