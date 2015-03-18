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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.dependency.ArtifactAccessor;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyCreationException;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyMappingNotFoundException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyLogBuilder;
import org.bonitasoft.engine.dependency.model.builder.SDependencyLogBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingLogBuilder;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingLogBuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
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
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class DependencyServiceImpl implements DependencyService {

    private static final int BATCH_SIZE = 100;

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    private final ClassLoaderService classLoaderService;

    private final Map<String, Long> lastUpdates = Collections.synchronizedMap(new HashMap<String, Long>());

    public DependencyServiceImpl(final ReadPersistenceService persistenceService, final Recorder recorder, final EventService eventService,
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService, final ClassLoaderService classLoaderService) {
        super();
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
        this.classLoaderService = classLoaderService;
    }

    private String getKey(final ScopeType artifactType, final long artifactId) {
        final StringBuilder sb = new StringBuilder(artifactType.name());
        sb.append("________");
        sb.append(artifactId);
        return sb.toString();
    }

    private SDependencyLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SDependencyLogBuilder logBuilder = BuilderFactory.get(SDependencyLogBuilderFactory.class).createNewInstance();
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

    private SDependencyMappingLogBuilder getQueriableLog(final ActionType actionType, final String message, final SDependencyMapping dependencyMapping) {
        final SDependencyMappingLogBuilder logBuilder = BuilderFactory.get(SDependencyMappingLogBuilderFactory.class).createNewInstance();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.dependencyId(dependencyMapping.getDependencyId());
        logBuilder.objectId(dependencyMapping.getId());
        return logBuilder;
    }

    @Override
    public void createDependency(final SDependency dependency) throws SDependencyCreationException {
        final SDependencyLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a dependency with name " + dependency.getName());
        NullCheckingUtil.checkArgsNotNull(dependency);
        try {
            final InsertRecord insertRecord = new InsertRecord(dependency);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(DEPENDENCY, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DEPENDENCY).setObject(dependency).done();
            }
            recorder.recordInsert(insertRecord, insertEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createDependency"));
            }
            log(dependency.getId(), SQueriableLog.STATUS_OK, logBuilder, "createDependency");
        } catch (final SRecorderException e) {
            log(dependency.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createDependency");
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createDependency", e));
            }
            throw new SDependencyCreationException("Can't create dependency " + dependency, e);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Created dependency " + dependency);
        }
    }

    @Override
    public void createDependencyMapping(final SDependencyMapping dependencyMapping) throws SDependencyException {
        final SDependencyMappingLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a dependency mapping", dependencyMapping);
        NullCheckingUtil.checkArgsNotNull(dependencyMapping);
        try {
            final InsertRecord insertRecord = new InsertRecord(dependencyMapping);

            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(DEPENDENCYMAPPING, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DEPENDENCYMAPPING).setObject(dependencyMapping)
                        .done();
            }
            recorder.recordInsert(insertRecord, insertEvent);
            log(dependencyMapping.getId(), SQueriableLog.STATUS_OK, logBuilder, "createDependencyMapping");
            lastUpdates.put(getKey(dependencyMapping.getArtifactType(), dependencyMapping.getArtifactId()), System.currentTimeMillis());
            refreshLocalClassLoader(dependencyMapping);
        } catch (final SRecorderException e) {
            log(dependencyMapping.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createDependencyMapping");
            throw new SDependencyException("Can't create dependency mapping" + dependencyMapping, e);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Created dependency mapping " + dependencyMapping);
        }
    }

    @Override
    public void deleteAllDependencies() throws SDependencyDeletionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteAllDependencies"));
        }
        final QueryOptions queryOptions = new QueryOptions(0, 100, SDependency.class, "id", OrderByType.ASC);
        List<SDependency> dependencies;
        do {
            try {
                dependencies = getDependencies(queryOptions);
            } catch (final SDependencyException e) {
                throw new SDependencyDeletionException(e);
            }
            for (final SDependency dependency : dependencies) {
                deleteDependency(dependency);
            }
        } while (dependencies.size() == queryOptions.getNumberOfResults());
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteAllDependencies"));
        }
    }

    @Override
    public void deleteAllDependencyMappings() throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteAllDependencyMappings"));
        }
        final QueryOptions queryOptions = new QueryOptions(0, 100, SDependencyMapping.class, "id", OrderByType.ASC);
        List<SDependencyMapping> dependencyMappings;
        do {
            dependencyMappings = getDependencyMappings(queryOptions);
            for (final SDependencyMapping dependencyMapping : dependencyMappings) {
                deleteDependencyMapping(dependencyMapping);
            }
        } while (dependencyMappings.size() == queryOptions.getNumberOfResults());
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteAllDependencyMappings"));
        }
    }

    @Override
    public void deleteDependency(final SDependency dependency) throws SDependencyDeletionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Deleting dependency " + dependency);
        }
        NullCheckingUtil.checkArgsNotNull(dependency);
        final SDependencyLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a dependency named " + dependency.getName());
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(DEPENDENCY, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DEPENDENCY).setObject(dependency).done();
        }
        try {
            final DeleteRecord record = new DeleteRecord(dependency);
            recorder.recordDelete(record, deleteEvent);
            log(dependency.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteDependency");
        } catch (final SRecorderException e) {
            log(dependency.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteDependency");
            throw new SDependencyDeletionException("Can't delete dependency" + dependency, e);
        }
    }

    @Override
    public void deleteDependency(final long id) throws SDependencyNotFoundException, SDependencyDeletionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteDependency"));
        }
        NullCheckingUtil.checkArgsNotNull(id);
        deleteDependency(getDependency(id));
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteDependency"));
        }
    }

    @Override
    public void deleteDependency(final String name) throws SDependencyNotFoundException, SDependencyDeletionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteDependency"));
        }
        final SDependency dependency = getDependencyByName(name);
        try {
            delete(dependency);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteDependency"));
            }
        } catch (final SDependencyException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteDependency", e));
            }
            throw new SDependencyDeletionException("Can't delete dependency with name: " + name, e);
        }
    }

    private SDependency getDependencyByName(final String name) throws SDependencyNotFoundException {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) name);
        final SelectOneDescriptor<SDependency> desc = new SelectOneDescriptor<SDependency>("getDependencyByName", parameters, SDependency.class);
        try {
            final SDependency sDependency = persistenceService.selectOne(desc);
            if (sDependency == null) {
                throw new SDependencyNotFoundException("Dependency with name " + name + " does not exist.");
            }
            return sDependency;
        } catch (final SBonitaReadException sbre) {
            throw new SDependencyNotFoundException(sbre);
        }
    }

    @Override
    public void deleteDependencyMapping(final long id) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteDependencyMapping"));
        }
        NullCheckingUtil.checkArgsNotNull(id);
        final SDependencyMapping dependencyMapping = getDependencyMapping(id);
        deleteDependencyMapping(dependencyMapping);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteDependencyMapping"));
        }
    }

    @Override
    public void deleteDependencyMapping(final SDependencyMapping dependencyMapping) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Deleting dependency mapping " + dependencyMapping);
        }
        NullCheckingUtil.checkArgsNotNull(dependencyMapping);
        final SDependencyMappingLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a dependency mapping", dependencyMapping);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(DEPENDENCYMAPPING, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DEPENDENCYMAPPING).setObject(dependencyMapping)
                    .done();
        }
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(dependencyMapping);
            recorder.recordDelete(deleteRecord, deleteEvent);
            log(dependencyMapping.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteDependencyMapping");
            lastUpdates.put(getKey(dependencyMapping.getArtifactType(), dependencyMapping.getArtifactId()), System.currentTimeMillis());
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteDependencyMapping"));
            }
            refreshLocalClassLoader(dependencyMapping);
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "deleteDependencyMapping", e));
            }
            log(dependencyMapping.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteDependencyMapping");
            throw new SDependencyException("Can't delete dependency mapping" + dependencyMapping, e);
        }
    }

    @Override
    public List<SDependency> getDependencies(final QueryOptions queryOptions) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDependencies"));
        }
        NullCheckingUtil.checkArgsNotNull(queryOptions);
        try {
            final List<SDependency> listSDependency = persistenceService.selectList(new SelectListDescriptor<SDependency>("getDependencies", null,
                    SDependency.class, queryOptions));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDependencies"));
            }
            return listSDependency;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDependencies", e));
            }
            throw new SDependencyException("Can't get dependencies", e);
        }
    }

    @Override
    public List<SDependency> getDependencies(final Collection<Long> ids) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDependencies"));
        }
        NullCheckingUtil.checkArgsNotNull(ids);
        try {
            final SelectListDescriptor<SDependency> desc = new SelectListDescriptor<SDependency>("getDependenciesByIds", CollectionUtil.buildSimpleMap("ids",
                    ids), SDependency.class, QueryOptions.countQueryOptions());
            final List<SDependency> listSDependency = persistenceService.selectList(desc);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDependencies"));
            }
            return listSDependency;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDependencies", e));
            }
            throw new SDependencyException("Can't get dependencies", e);
        }
    }

    @Override
    public SDependency getDependency(final long id) throws SDependencyNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDependency"));
        }
        NullCheckingUtil.checkArgsNotNull(id);
        try {
            final SelectByIdDescriptor<SDependency> desc = new SelectByIdDescriptor<SDependency>("getDependencyById", SDependency.class, id);
            final SDependency sDependency = persistenceService.selectById(desc);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDependency"));
            }
            if (sDependency == null) {
                throw new SDependencyNotFoundException("Can't get dependency with id: " + id);
            }
            return sDependency;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDependency", e));
            }
            throw new SDependencyNotFoundException("Can't get dependency with id: " + id, e);
        }
    }

    @Override
    public List<Long> getDependencyIds(final long artifactId, final ScopeType artifactType, final int startIndex, final int maxResult)
            throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDependencyIds"));
        }
        NullCheckingUtil.checkArgsNotNull(artifactId, artifactType, startIndex, maxResult);
        final QueryOptions queryOptions = new QueryOptions(startIndex, maxResult);
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("artifactId", artifactId);
            parameters.put("artifactType", artifactType);
            final SelectListDescriptor<Long> desc = new SelectListDescriptor<Long>("getDependencyIds", parameters, SDependencyMapping.class, Long.class,
                    queryOptions);
            final List<Long> listIds = persistenceService.selectList(desc);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDependencyIds"));
            }
            return listIds;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDependencyIds", e));
            }
            throw new SDependencyException("Can't get dependencies", e);
        }
    }

    @Override
    public SDependencyMapping getDependencyMapping(final long id) throws SDependencyMappingNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDependencyMapping"));
        }
        NullCheckingUtil.checkArgsNotNull(id);
        try {
            final SelectByIdDescriptor<SDependencyMapping> desc = new SelectByIdDescriptor<SDependencyMapping>("getDependencyMapping",
                    SDependencyMapping.class, id);
            final SDependencyMapping sDependencyMapping = persistenceService.selectById(desc);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDependencyMapping"));
            }
            if (sDependencyMapping == null) {
                throw new SDependencyMappingNotFoundException("Can't get dependency mapping with id: " + id);
            }
            return sDependencyMapping;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDependencyMapping", e));
            }
            throw new SDependencyMappingNotFoundException("Can't get dependency mapping with id: " + id, e);
        }
    }

    @Override
    public List<SDependencyMapping> getDependencyMappings(final QueryOptions queryOptions) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDependencyMappings"));
        }
        NullCheckingUtil.checkArgsNotNull(queryOptions);
        try {
            final List<SDependencyMapping> listSDependencyMapping = persistenceService.selectList(new SelectListDescriptor<SDependencyMapping>(
                    "getDependencyMappings", null, SDependencyMapping.class, queryOptions));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDependencyMappings"));
            }
            return listSDependencyMapping;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDependencyMappings", e));
            }
            throw new SDependencyException("Can't get dependency mappings", e);
        }
    }

    @Override
    public List<SDependencyMapping> getDependencyMappings(final long dependencyId, final QueryOptions queryOptions) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDependencyMappings"));
        }
        NullCheckingUtil.checkArgsNotNull(dependencyId, queryOptions);
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("dependencyId", dependencyId);
            final SelectListDescriptor<SDependencyMapping> desc = new SelectListDescriptor<SDependencyMapping>("getDependencyMappingsByDependency", parameters,
                    SDependencyMapping.class, queryOptions);
            final List<SDependencyMapping> listSDependencyMapping = persistenceService.selectList(desc);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDependencyMappings"));
            }
            return listSDependencyMapping;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDependencyMappings", e));
            }
            throw new SDependencyException("Can't get dependency mappings by dependencyId: " + dependencyId, e);
        }
    }

    @Override
    public long getLastUpdatedTimestamp(final ScopeType artifactType, final long artifactId) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLastUpdatedTimestamp"));
        }
        NullCheckingUtil.checkArgsNotNull(artifactType, artifactId);
        final String key = getKey(artifactType, artifactId);
        if (lastUpdates.containsKey(key)) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLastUpdatedTimestamp"));
            }
            return lastUpdates.get(key);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLastUpdatedTimestamp"));
        }
        return 0;
    }

    @Override
    public void updateDependency(final SDependency dependency, final EntityUpdateDescriptor descriptor) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "updateDependency"));
        }
        NullCheckingUtil.checkArgsNotNull(dependency, descriptor);
        final SDependencyLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "Updating a dependency named " + dependency.getName());
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(DEPENDENCY, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(DEPENDENCY).setObject(dependency).done();
        }
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(dependency, descriptor);
            recorder.recordUpdate(updateRecord, updateEvent);
            log(dependency.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateDependency");
            QueryOptions queryOptions = new QueryOptions(0, 100, SDependencyMapping.class, "id", OrderByType.ASC);
            List<SDependencyMapping> dependencyMappings;
            final long updateTimeStamp = System.currentTimeMillis();
            do {
                dependencyMappings = getDependencyMappings(dependency.getId(), queryOptions);
                for (final SDependencyMapping dependencyMapping : dependencyMappings) {
                    lastUpdates.put(getKey(dependencyMapping.getArtifactType(), dependencyMapping.getArtifactId()), updateTimeStamp);
                    refreshLocalClassLoader(dependencyMapping);
                }
                queryOptions = QueryOptions.getNextPage(queryOptions);
            } while (dependencyMappings.size() == queryOptions.getNumberOfResults());
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateDependency"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateDependency", e));
            }
            log(dependency.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateDependency");
            throw new SDependencyException("Can't update dependency " + dependency, e);
        }
    }

    @Override
    public void updateDependencyMapping(final SDependencyMapping dependencyMapping, final EntityUpdateDescriptor descriptor) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "updateDependencyMapping"));
        }
        NullCheckingUtil.checkArgsNotNull(dependencyMapping, descriptor);
        final SDependencyMappingLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "Updating a dependency mapping", dependencyMapping);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(DEPENDENCYMAPPING, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(DEPENDENCYMAPPING).setObject(dependencyMapping)
                    .done();
        }
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(dependencyMapping, descriptor);
            recorder.recordUpdate(updateRecord, updateEvent);
            log(dependencyMapping.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateDependencyMapping");
            lastUpdates.put(getKey(dependencyMapping.getArtifactType(), dependencyMapping.getArtifactId()), System.currentTimeMillis());
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateDependencyMapping"));
            }
            refreshLocalClassLoader(dependencyMapping);
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateDependencyMapping", e));
            }
            log(dependencyMapping.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateDependencyMapping");
            throw new SDependencyException("Can't update dependency mapping " + dependencyMapping, e);
        }
    }

    @Override
    public List<SDependencyMapping> removeDisconnectedDependencyMappings(final ArtifactAccessor artifactAccessor) throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeDisconnectedDependencyMappings"));
        }
        QueryOptions loopQueryOptions = new QueryOptions(0, 100, SDependencyMapping.class, "id", OrderByType.ASC);
        List<SDependencyMapping> dependencyMappings;
        final List<SDependencyMapping> result = new ArrayList<SDependencyMapping>();
        do {
            dependencyMappings = getDependencyMappings(loopQueryOptions);
            for (final SDependencyMapping dependencyMapping : dependencyMappings) {
                if (!artifactAccessor.artifactExists(dependencyMapping.getArtifactType(), dependencyMapping.getArtifactId())) {
                    result.add(dependencyMapping);
                    deleteDependencyMapping(dependencyMapping);
                }
            }
            loopQueryOptions = QueryOptions.getNextPage(loopQueryOptions);
        } while (dependencyMappings.size() == loopQueryOptions.getNumberOfResults());
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeDisconnectedDependencyMappings"));
        }
        return result;
    }

    @Override
    public List<SDependencyMapping> getDisconnectedDependencyMappings(final ArtifactAccessor artifactAccessor, final QueryOptions queryOptions)
            throws SDependencyException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDisconnectedDependencyMappings"));
        }

        List<SDependencyMapping> dependencyMappings;
        final List<SDependencyMapping> result = new ArrayList<SDependencyMapping>();
        int numberOfResultsFound = 0;
        final int startIndex = queryOptions.getFromIndex();
        final int numberOfResults = queryOptions.getNumberOfResults();

        QueryOptions loopQueryOptions = new QueryOptions(queryOptions);
        do {
            dependencyMappings = getDependencyMappings(loopQueryOptions);
            for (final SDependencyMapping dependencyMapping : dependencyMappings) {
                if (!artifactAccessor.artifactExists(dependencyMapping.getArtifactType(), dependencyMapping.getArtifactId())) {
                    numberOfResultsFound++;
                    if (numberOfResultsFound > startIndex) {
                        // add it in the results
                        result.add(dependencyMapping);
                    }
                    if (result.size() == numberOfResults) {
                        // stop the for iteration, we have the number of results we want
                        break;
                    }
                }
            }
            loopQueryOptions = QueryOptions.getNextPage(loopQueryOptions);
        } while (dependencyMappings.size() == numberOfResults && result.size() < numberOfResults);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDisconnectedDependencyMappings"));
        }
        return result;
    }

    private void log(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public void deleteDependencies(final long id, final ScopeType type) throws SDependencyException {
        final QueryOptions queryOptionsForMapping = new QueryOptions(0, 2, SDependencyMapping.class, "id", OrderByType.ASC);
        int fromIndex = 0;

        List<Long> dependencyIds = getDependencyIds(id, type, fromIndex, BATCH_SIZE);
        while (!dependencyIds.isEmpty()) {
            for (final Long dependencyId : dependencyIds) {
                final List<SDependencyMapping> dependencyMappings = getDependencyMappings(dependencyId, queryOptionsForMapping);
                if (dependencyMappings.size() == 1) {// only when the dependency is linked only to on element
                    final SDependencyMapping dependencyMapping = dependencyMappings.get(0);
                    deleteDependencyMapping(dependencyMapping);
                    deleteDependency(dependencyId);
                } else {
                    fromIndex++;
                }
            }
            dependencyIds = getDependencyIds(id, type, fromIndex, BATCH_SIZE);
        }
    }

    private void refreshLocalClassLoader(final SDependencyMapping dependencyMapping) throws SDependencyException {
        refreshClassLoader(dependencyMapping.getArtifactType(), dependencyMapping.getArtifactId());
    }

    private Map<String, byte[]> getDependenciesResources(final ScopeType type, final long id) throws SDependencyException {
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        int fromIndex = 0;
        List<Long> dependencyIds = null;
        do {
            dependencyIds = getDependencyIds(id, type, fromIndex, BATCH_SIZE);
            if (dependencyIds != null && dependencyIds.size() > 0) {
                final List<SDependency> dependencies = getDependencies(dependencyIds);
                for (final SDependency dependency : dependencies) {
                    resources.put(dependency.getFileName(), dependency.getValue());
                }
            }
            fromIndex = fromIndex + BATCH_SIZE;
        } while (dependencyIds != null && dependencyIds.size() == BATCH_SIZE);
        return resources;
    }

    @Override
    public void refreshClassLoader(final ScopeType type, final long id) throws SDependencyException {
        final Map<String, byte[]> resources = getDependenciesResources(type, id);
        try {
            classLoaderService.refreshLocalClassLoader(type.name(), id, resources);
        } catch (final SClassLoaderException e) {
            throw new SDependencyException("Cannot refresh classLoader with type'" + type + "' and id " + id, e);
        }
    }

    @Override
    public void updateDependenciesOfArtifact(final long id, final ScopeType type, final List<SDependency> dependencies) throws SDependencyException {
        final Map<String, SDependency> newDependenciesByName = getMapOfNames(dependencies);
        int fromIndex = 0;

        List<Long> dependencyIds = getDependencyIds(id, type, fromIndex, BATCH_SIZE);
        while (!dependencyIds.isEmpty()) {
            final List<SDependency> currentDependencies = getDependencies(dependencyIds);
            for (final SDependency currentDependency : currentDependencies) {
                if (!newDependenciesByName.containsKey(currentDependency.getName())) {
                    delete(currentDependency);
                } else {
                    final SDependency newDependency = newDependenciesByName.get(currentDependency.getName());
                    update(currentDependency, newDependency);
                }
                // remove from list
                newDependenciesByName.remove(currentDependency.getName());
            }
            fromIndex = fromIndex + BATCH_SIZE;
            dependencyIds = getDependencyIds(id, type, fromIndex, BATCH_SIZE);
        }

        // all artifact that are still here must be created
        for (final SDependency sDependency : newDependenciesByName.values()) {
            createForArtifact(id, type, sDependency);
        }
    }

    private void createForArtifact(final long id, final ScopeType type, final SDependency sDependency) throws SDependencyCreationException,
            SDependencyException {
        createDependency(sDependency);
        final SDependencyMapping sDependencyMapping = BuilderFactory.get(SDependencyMappingBuilderFactory.class)
                .createNewInstance(sDependency.getId(), id, type).done();
        createDependencyMapping(sDependencyMapping);
    }

    private void update(final SDependency currentDependency, final SDependency newDependency) throws SDependencyException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SDependencyBuilderFactory.class).getDescriptionKey(), newDependency.getDescription());
        descriptor.addField(BuilderFactory.get(SDependencyBuilderFactory.class).getFileNameKey(), newDependency.getFileName());
        descriptor.addField(BuilderFactory.get(SDependencyBuilderFactory.class).getValueKey(), newDependency.getValue());
        updateDependency(currentDependency, descriptor);
    }

    private void delete(final SDependency dependency) throws SDependencyException, SDependencyDeletionException {
        final QueryOptions queryOptions = new QueryOptions(0, 10, SDependencyMapping.class, "id", OrderByType.ASC);
        final SDependencyMapping sDependencyMapping = getDependencyMappings(dependency.getId(), queryOptions).get(0);
        deleteDependencyMapping(sDependencyMapping);
        deleteDependency(dependency);
    }

    private Map<String, SDependency> getMapOfNames(final List<SDependency> dependencies) {
        final HashMap<String, SDependency> hashMap = new HashMap<String, SDependency>(dependencies.size());
        for (final SDependency sDependency : dependencies) {
            hashMap.put(sDependency.getName(), sDependency);
        }
        return hashMap;
    }

}
