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

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.CollectionUtil;
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
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyLogBuilder;
import org.bonitasoft.engine.dependency.model.builder.SDependencyLogBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingLogBuilder;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingLogBuilderFactory;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class TenantDependencyService extends AbstractDependencyService {

    private final ReadPersistenceService persistenceService;
    private final Recorder recorder;
    private final QueriableLoggerService queriableLoggerService;

    public TenantDependencyService(final ReadPersistenceService persistenceService, final Recorder recorder,
            final QueriableLoggerService queriableLoggerService) {
        super(persistenceService);
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.queriableLoggerService = queriableLoggerService;
    }

    private SDependencyLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SDependencyLogBuilder logBuilder = BuilderFactory.get(SDependencyLogBuilderFactory.class)
                .createNewInstance();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SDependencyMappingLogBuilder getQueriableLog(final ActionType actionType, final String message,
            final SAbstractDependencyMapping dependencyMapping) {
        final SDependencyMappingLogBuilder logBuilder = BuilderFactory.get(SDependencyMappingLogBuilderFactory.class)
                .createNewInstance();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.dependencyId(dependencyMapping.getDependencyId());
        logBuilder.objectId(dependencyMapping.getId());
        return logBuilder;
    }

    @Override
    protected List<AbstractSDependency> getDependencies(QueryOptions queryOptions) throws SDependencyException {
        List<AbstractSDependency> dependencies;
        try {
            dependencies = persistenceService.selectList(new SelectListDescriptor<>("getDependencies", null,
                    AbstractSDependency.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("Can't get dependencies", e);
        }
        return dependencies;
    }

    @Override
    protected SDependency getDependency(String name) throws SDependencyNotFoundException {
        final Map<String, Object> parameters = Collections.singletonMap("name", name);
        final SelectOneDescriptor<SDependency> desc = new SelectOneDescriptor<>("getDependencyByName", parameters,
                SDependency.class);
        final SDependency sDependency;
        try {
            sDependency = persistenceService.selectOne(desc);
        } catch (SBonitaReadException e) {
            throw new SDependencyNotFoundException("Dependency with name " + name + " does not exist.");
        }
        if (sDependency == null) {
            throw new SDependencyNotFoundException("Dependency with name " + name + " does not exist.");
        }
        return sDependency;
    }

    @Override
    protected List<SAbstractDependencyMapping> getDependencyMappings(final long dependencyId,
            final QueryOptions queryOptions) throws SDependencyException {
        NullCheckingUtil.checkArgsNotNull(dependencyId, queryOptions);
        try {
            final Map<String, Object> parameters = new HashMap<>();
            parameters.put("dependencyId", dependencyId);
            final SelectListDescriptor<SAbstractDependencyMapping> desc = new SelectListDescriptor<>(
                    "getDependencyMappingsByDependency", parameters,
                    SDependencyMapping.class, queryOptions);
            return persistenceService.selectList(desc);
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("Can't get dependency mappings by dependencyId: " + dependencyId, e);
        }
    }

    @Override
    protected QueryOptions getDefaultQueryOptionForDependencyMapping() {
        return new QueryOptions(0, 100, SDependencyMapping.class, "id", OrderByType.ASC);
    }

    @Override
    protected void delete(AbstractSDependency dependency) throws SDependencyDeletionException {
        NullCheckingUtil.checkArgsNotNull(dependency);
        final SDependencyLogBuilder logBuilder = getQueriableLog(ActionType.DELETED,
                "Deleting a dependency named " + dependency.getName());
        try {
            delete(dependency, DEPENDENCY);
            log(dependency.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteDependency");
        } catch (final SRecorderException e) {
            log(dependency.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteDependency");
            throw new SDependencyDeletionException("Can't delete dependency" + dependency, e);
        }
    }

    private void delete(PersistentObject object, String eventType) throws SRecorderException {
        recorder.recordDelete(new DeleteRecord(object), eventType);
    }

    @Override
    public void deleteDependencyMapping(final SAbstractDependencyMapping dependencyMapping)
            throws SDependencyException {
        NullCheckingUtil.checkArgsNotNull(dependencyMapping);
        final SDependencyMappingLogBuilder logBuilder = getQueriableLog(ActionType.DELETED,
                "Deleting a dependency mapping", dependencyMapping);
        try {
            delete(dependencyMapping, DEPENDENCYMAPPING);
            log(dependencyMapping.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteDependencyMapping");
        } catch (final SRecorderException e) {
            log(dependencyMapping.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteDependencyMapping");
            throw new SDependencyException("Can't delete dependency mapping" + dependencyMapping, e);
        }
    }

    @Override
    public List<AbstractSDependency> getDependencies(final Collection<Long> ids) throws SDependencyException {
        NullCheckingUtil.checkArgsNotNull(ids);
        try {
            final SelectListDescriptor<AbstractSDependency> desc = new SelectListDescriptor<>("getDependenciesByIds",
                    CollectionUtil.buildSimpleMap("ids",
                            ids),
                    SDependency.class, QueryOptions.countQueryOptions());
            return persistenceService.selectList(desc);
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("Can't get dependencies", e);
        }
    }

    @Override
    protected SelectListDescriptor<Long> getSelectDescriptorForDependencyIds(QueryOptions queryOptions,
            Map<String, Object> parameters) {
        return new SelectListDescriptor<>("getDependencyIds", parameters, SDependencyMapping.class, Long.class,
                queryOptions);
    }

    @Override
    public List<SDependencyMapping> getDependencyMappings(final QueryOptions queryOptions) throws SDependencyException {
        NullCheckingUtil.checkArgsNotNull(queryOptions);
        try {
            return persistenceService.selectList(new SelectListDescriptor<>(
                    "getDependencyMappings", null, SDependencyMapping.class, queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SDependencyException("Can't get dependency mappings", e);
        }
    }

    @Override
    public SDependency getDependency(final long id) throws SDependencyNotFoundException {
        NullCheckingUtil.checkArgsNotNull(id);
        try {
            final SelectByIdDescriptor<SDependency> desc = new SelectByIdDescriptor<>(SDependency.class, id);
            final SDependency sDependency = persistenceService.selectById(desc);
            if (sDependency == null) {
                throw new SDependencyNotFoundException("Can't get dependency with id: " + id);
            }
            return sDependency;
        } catch (final SBonitaReadException e) {
            throw new SDependencyNotFoundException("Can't get dependency with id: " + id, e);
        }
    }

    @Override
    public DependencyContent getDependencyContentOnly(final long id)
            throws SDependencyNotFoundException, SBonitaReadException {
        NullCheckingUtil.checkArgsNotNull(id);
        SelectOneDescriptor<DependencyContent> desc = new SelectOneDescriptor<>("getDependencyContentOnly",
                Collections.singletonMap("id", id), SDependency.class, DependencyContent.class);
        return Optional.ofNullable(persistenceService.selectOne(desc))
                .orElseThrow(() -> new SDependencyNotFoundException("Can't get content of dependency with id: " + id));
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder,
            final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.build();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    public SDependency createMappedDependency(String name, byte[] jarContent, String fileName, long artifactId,
            ScopeType scopeType)
            throws SDependencyException {
        final SDependency sDependency = createDependency(name, jarContent, fileName, artifactId, scopeType);
        createDependencyMapping(artifactId, scopeType, sDependency);
        return sDependency;
    }

    public SDependency updateDependencyOfArtifact(String name, byte[] jarContent, String fileName, long artifactId,
            ScopeType scopeType)
            throws SDependencyException {
        try {
            final SDependency sDependency = getDependencyOfArtifact(artifactId, scopeType, fileName);
            if (sDependency == null) {
                throw new SDependencyNotFoundException("unable to find dependency " + fileName + " on artifact: "
                        + artifactId + " with type " + scopeType);
            }
            recorder.recordUpdate(
                    UpdateRecord.buildSetFields(sDependency, Collections.singletonMap("value_", jarContent)),
                    DEPENDENCY);
            return sDependency;
        } catch (SBonitaReadException | SRecorderException e) {
            throw new SDependencyException(e);
        }
    }

    private SDependency createDependency(String name, byte[] jarContent, String fileName, long artifactId,
            ScopeType scopeType)
            throws SDependencyCreationException {
        final SDependency sDependency = new SDependencyBuilderFactory().createNewInstance(name, artifactId, scopeType,
                fileName, jarContent);
        final SDependencyLogBuilder logBuilder = getQueriableLog(ActionType.CREATED,
                "Creating a dependency with name " + sDependency.getName());
        NullCheckingUtil.checkArgsNotNull(sDependency);
        try {
            insert(sDependency, DEPENDENCY);
        } catch (final SRecorderException e) {
            log(sDependency.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createDependency");
            throw new SDependencyCreationException("Can't create dependency " + sDependency, e);
        }
        return sDependency;
    }

    private void insert(PersistentObject object, String eventType) throws SRecorderException {
        recorder.recordInsert(new InsertRecord(object), eventType);
    }

    private void createDependencyMapping(long artifactId, ScopeType scopeType, SDependency sDependency)
            throws SDependencyException {
        final SDependencyMapping sDependencyMapping = new SDependencyMapping(artifactId, scopeType,
                sDependency.getId());
        createDependencyMapping(sDependencyMapping);
    }

    @Override
    protected void createDependencyMapping(SAbstractDependencyMapping dependencyMapping) throws SDependencyException {
        final SDependencyMappingLogBuilder logBuilder1 = getQueriableLog(ActionType.CREATED,
                "Creating a dependency mapping", dependencyMapping);
        NullCheckingUtil.checkArgsNotNull(dependencyMapping);
        try {
            insert(dependencyMapping, DEPENDENCYMAPPING);
            log(dependencyMapping.getId(), SQueriableLog.STATUS_OK, logBuilder1, "createDependencyMapping");
        } catch (final SRecorderException e) {
            log(dependencyMapping.getId(), SQueriableLog.STATUS_FAIL, logBuilder1, "createDependencyMapping");
            throw new SDependencyException("Can't create dependency mapping" + dependencyMapping, e);
        }
    }

    @Override
    public SDependency getDependencyOfArtifact(long artifactId, ScopeType artifactType, String fileName)
            throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<>(3);
        inputParameters.put("artifactId", artifactId);
        inputParameters.put("artifactType", artifactType);
        inputParameters.put("fileName", fileName);
        return persistenceService
                .selectOne(new SelectOneDescriptor<>("getDependencyOfArtifact", inputParameters, SDependency.class));
    }

    @Override
    public Optional<Long> getIdOfDependencyOfArtifact(Long artifactId, ScopeType artifactType, String fileName)
            throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<>(3);
        inputParameters.put("artifactId", artifactId);
        inputParameters.put("artifactType", artifactType);
        inputParameters.put("fileName", fileName);
        Long idOfDependencyOfArtifact = persistenceService.selectOne(
                new SelectOneDescriptor<>("getIdOfDependencyOfArtifact", inputParameters, SDependency.class));
        return Optional.ofNullable(idOfDependencyOfArtifact);
    }
}
