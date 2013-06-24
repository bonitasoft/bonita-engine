/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.instance.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.archive.SDefinitiveArchiveNotFound;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.instance.DataInstanceDataSource;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDeleteDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceLogBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceLogBuilder;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * General mechanism for lookup is to look in specific flownode to search a data instance. When refering to "local" data instance, it means the lookup is
 * performed only on the specific element, and not on inherited data for parent containers.
 * 
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 */
public class DataInstanceServiceImpl implements DataInstanceService {

    public static final String DEFAULT_DATA_SOURCE = "bonita_data_source";

    public static final String DATA_SOURCE_VERSION = "6.0";

    public static final String TRANSIENT_DATA_SOURCE = "bonita_transient_data_source";

    public static final String TRANSIENT_DATA_SOURCE_VERSION = "6.0";

    private final DataService dataSourceService;

    private final SDataInstanceBuilders dataInstanceBuilders;

    private final Recorder recorder;

    private final SEventBuilders eventBuilders;

    private final ReadPersistenceService persistenceService;

    private final ArchiveService archiveService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    public DataInstanceServiceImpl(final DataService dataSourceService, final SDataInstanceBuilders dataInstanceBuilders, final Recorder recorder,
            final SEventBuilders eventBuilders, final ReadPersistenceService persistenceService, final ArchiveService archiveService,
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        this.dataSourceService = dataSourceService;
        this.dataInstanceBuilders = dataInstanceBuilders;
        this.recorder = recorder;
        this.eventBuilders = eventBuilders;
        this.persistenceService = persistenceService;
        this.archiveService = archiveService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    // FIXME this should be done BEFORE insertChildContainer... should we add a check mappings and add it here too
    @Override
    public void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "createDataInstance"));
        }
        final DataInstanceDataSource dataInstanceDataSource = getDataInstanceDataSource(dataInstance.isTransientData());
        dataInstanceDataSource.createDataInstance(dataInstance);
        archiveDataInstance(dataInstance);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createDataInstance"));
        }
    }

    private DataInstanceDataSource getDataInstanceDataSource(final String dataSourceName, final String dataSourceVersion) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataInstanceDataSource"));
        }
        try {
            final SDataSource dataSource = dataSourceService.getDataSource(dataSourceName, dataSourceVersion);
            final DataInstanceDataSource dataInstanceDataSource = dataSourceService.getDataSourceImplementation(DataInstanceDataSource.class,
                    dataSource.getId());
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataInstanceDataSource"));
            }
            return dataInstanceDataSource;
        } catch (final SBonitaException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataInstanceDataSource", e));
            }
            throw new SDataInstanceException("Unable to get data instance data source", e);
        }
    }

    private DataInstanceDataSource getDataInstanceDataSource(final boolean isTransient) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataInstanceDataSource"));
        }
        final DataInstanceDataSource dataInstanceDataSource;
        if (isTransient) {
            dataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
        } else {
            dataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataInstanceDataSource"));
        }
        return dataInstanceDataSource;
    }

    @Override
    public void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "updateDataInstance"));
        }
        NullCheckingUtil.checkArgsNotNull(dataInstance, descriptor);
        final DataInstanceDataSource dataInstanceDataSource = getDataInstanceDataSource(dataInstance.isTransientData());
        dataInstanceDataSource.updateDataInstance(dataInstance, descriptor);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "updateDataInstance"));
        }
        archiveDataInstance(dataInstance);
    }

    private void archiveDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        if (!dataInstance.isTransientData()) {
            try {
                final SADataInstance saDataInstance = dataInstanceBuilders.getSADataInstanceBuilder().createNewInstance(dataInstance).done();
                final ArchiveInsertRecord archiveInsertRecord = new ArchiveInsertRecord(saDataInstance);
                archiveService.recordInsert(System.currentTimeMillis(), archiveInsertRecord, getQueriableLog(ActionType.CREATED, "archive the SADataInstance"));
            } catch (final SDefinitiveArchiveNotFound e) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateDataInstance", e));
                }
                throw new SDataInstanceException("Unable to create SADataInstance", e);
            } catch (final SRecorderException e) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "updateDataInstance", e));
                }
                throw new SDataInstanceException("Unable to create SADataInstance", e);
            }
        }
    }

    @Override
    public void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteDataInstance"));
        }
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final DataInstanceDataSource dataInstanceDataSource = getDataInstanceDataSource(dataInstance.isTransientData());
        dataInstanceDataSource.deleteDataInstance(dataInstance);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteDataInstance"));
        }
    }

    @Override
    public SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException {
        // FIXME: update the service interface to take data source information as parameters instead of look for data in both datasources
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataInstance"));
        }
        final DataInstanceDataSource transientDataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
        try {
            return transientDataInstanceDataSource.getDataInstance(dataInstanceId);
        } catch (final SDataInstanceException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataInstance", e));
            }
            final DataInstanceDataSource dataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
            return dataInstanceDataSource.getDataInstance(dataInstanceId);
        } finally {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataInstance"));
            }
        }
    }

    @Override
    public SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataInstance"));
        }
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);
        // FIXME: update the service interface to take data source information as parameters instead of look for data in both datasources
        try {
            final long dataInstanceId = getDataInstanceDataVisibilityMapping(dataName, containerId, containerType);
            final DataInstanceDataSource transientDataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
            try {
                return transientDataInstanceDataSource.getDataInstance(dataInstanceId);
            } catch (final SDataInstanceException e) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataInstance", e));
                }
                final DataInstanceDataSource defaultDataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
                return defaultDataInstanceDataSource.getDataInstance(dataInstanceId);
            }
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataInstance", e));
            }
            throw new SDataInstanceException("No data found with name " + dataName + "  neither on container " + containerId + " with type " + containerType
                    + " nor in its parents", e);
        } finally {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataInstance"));
            }
        }
    }

    private long getDataInstanceDataVisibilityMapping(final String dataName, final long containerId, final String containerType) throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("dataName", dataName);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        final SelectOneDescriptor<Long> selectOneDescriptor = new SelectOneDescriptor<Long>("getDataInstanceIdFromMapping", parameters,
                SDataInstanceVisibilityMapping.class);
        final Long dataInstanceId = persistenceService.selectOne(selectOneDescriptor);
        if (dataInstanceId == null) {
            final StringBuilder stb = new StringBuilder("DataInstance with name not found from mapping: [name: ");
            stb.append(dataName).append(", container type: ").append(containerType);
            stb.append(", container id: ").append(containerId).append(']');
            throw new SBonitaReadException(stb.toString(), null, selectOneDescriptor);
        }
        return dataInstanceId;
    }

    private List<Long> getDataInstanceDataVisibilityMapping(final List<String> dataNames, final long containerId, final String containerType)
            throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("dataNames", dataNames);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        final SelectListDescriptor<Long> selectListDescriptor = new SelectListDescriptor<Long>("getDataInstanceIdsFromMapping", parameters,
                SDataInstanceVisibilityMapping.class, new QueryOptions(0, dataNames.size()));
        return persistenceService.selectList(selectListDescriptor);
    }

    private long getSADataInstanceDataVisibilityMapping(final String dataName, final long containerId, final String containerType) throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("dataName", dataName);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        final SelectOneDescriptor<Long> selectOneDescriptor = new SelectOneDescriptor<Long>("getSADataInstanceIdFromMapping", parameters,
                SADataInstanceVisibilityMapping.class);
        final Long dataInstanceId = persistenceService.selectOne(selectOneDescriptor);
        if (dataInstanceId == null) {
            final StringBuilder stb = new StringBuilder("DataInstance with name not found from mapping: [name: ");
            stb.append(dataName).append(", container type: ").append(containerType);
            stb.append(", container id: ").append(containerId).append(']');
            throw new SBonitaReadException(stb.toString(), null, selectOneDescriptor);
        }
        return dataInstanceId;
    }

    private List<Long> getSADataInstanceDataVisibilityMapping(final List<String> dataNames, final long containerId, final String containerType)
            throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("dataNames", dataNames);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        final SelectListDescriptor<Long> selectListDescriptor = new SelectListDescriptor<Long>("getSADataInstanceIdsFromMapping", parameters,
                SADataInstanceVisibilityMapping.class, new QueryOptions(0, dataNames.size()));
        return persistenceService.selectList(selectListDescriptor);
    }

    @Override
    public List<SDataInstance> getDataInstances(final long containerId, final String containerType, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataInstances"));
        }
        NullCheckingUtil.checkArgsNotNull(containerType);
        try {
            final List<SDataInstanceVisibilityMapping> mappings = getDataInstanceVisibilityMappings(containerId, containerType, fromIndex, numberOfResults);
            final ArrayList<SDataInstance> dataInstances = new ArrayList<SDataInstance>(mappings.size());
            for (final SDataInstanceVisibilityMapping mapping : mappings) {
                dataInstances.add(getDataInstance(mapping.getDataInstanceId()));
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataInstances"));
            }
            return dataInstances;
        } catch (final SBonitaReadException e1) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataInstances", e1));
            }
            throw new SDataInstanceException("Unable to read data mappings of the container with type " + containerType + " and id " + containerId, e1);
        }
    }

    private List<SDataInstanceVisibilityMapping> getDataInstanceVisibilityMappings(final long containerId, final String containerType, final int fromIndex,
            final int numberOfResults) throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        final SelectListDescriptor<SDataInstanceVisibilityMapping> selectDescriptor = new SelectListDescriptor<SDataInstanceVisibilityMapping>(
                "getDataInstanceVisibilityMappings", parameters, SDataInstanceVisibilityMapping.class, new QueryOptions(fromIndex, numberOfResults));
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public SDataInstance getLocalDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLocalDataInstance"));
        }
        NullCheckingUtil.checkArgsNotNull(dataName);
        NullCheckingUtil.checkArgsNotNull(containerType);

        // FIXME: update the service interface to take data source information as parameters instead of look for data in both datasources
        final DataInstanceDataSource transientDataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
        try {
            return transientDataInstanceDataSource.getDataInstance(dataName, containerId, containerType);
        } catch (final SDataInstanceException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getLocalDataInstance", e));
            }
            final DataInstanceDataSource defaultDataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
            return defaultDataInstanceDataSource.getDataInstance(dataName, containerId, containerType);
        } finally {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLocalDataInstance"));
            }
        }
    }

    @Override
    public List<SDataInstance> getLocalDataInstances(final long containerId, final String containerType, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLocalDataInstances"));
        }
        NullCheckingUtil.checkArgsNotNull(containerType);
        final DataInstanceDataSource transientDataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
        final DataInstanceDataSource defaultDataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
        try {
            final List<SDataInstance> transientDataInstances = transientDataInstanceDataSource.getDataInstances(containerId, containerType, fromIndex,
                    numberOfResults);
            final List<SDataInstance> dataInstances = defaultDataInstanceDataSource.getDataInstances(containerId, containerType, fromIndex, numberOfResults);
            dataInstances.addAll(transientDataInstances);
            return dataInstances;
        } catch (final SDataInstanceException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getLocalDataInstances", e));
            }
            throw e;
        } finally {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLocalDataInstances"));
            }
        }
    }

    @Override
    public void addChildContainer(final long parentContainerId, final String parentContainerType, final long containerId, final String containerType)
            throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "addChildContainer"));
        }
        try {
            // insert mappings from parent element
            final List<SDataInstanceVisibilityMapping> mappings = insertMappingForLocalElement(containerId, containerType);
            final ArrayList<String> localData = new ArrayList<String>(mappings.size());
            for (final SDataInstanceVisibilityMapping sDataInstanceVisibilityMapping : mappings) {
                localData.add(sDataInstanceVisibilityMapping.getDataName());
            }
            final long archivedDate = System.currentTimeMillis();
            final int batchSize = 80;
            int currentIndex = 0;
            List<SDataInstance> parentVisibleDataInstances = getDataInstances(parentContainerId, parentContainerType, currentIndex, batchSize);
            while (parentVisibleDataInstances.size() > 0) {
                for (final SDataInstance parentData : parentVisibleDataInstances) {
                    if (!localData.contains(parentData.getName())) {
                        insertDataInstanceVisibilityMapping(containerId, containerType, parentData.getName(), parentData.getId(), archivedDate);
                    }
                }
                currentIndex += batchSize;
                parentVisibleDataInstances = getDataInstances(parentContainerId, parentContainerType, currentIndex, batchSize);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "addChildContainer"));
            }
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "addChildContainer", e));
            }
            throw new SDataInstanceException(e);
        } catch (final SDefinitiveArchiveNotFound e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "addChildContainer", e));
            }
            throw new SDataInstanceException(e);
        }
    }

    @Override
    public void removeContainer(final long containerId, final String containerType) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeContainer"));
        }
        try {
            List<SDataInstanceVisibilityMapping> visibilityMappings;
            do {
                visibilityMappings = getDataInstanceVisibilityMappings(containerId, containerType, 0, QueryOptions.DEFAULT_NUMBER_OF_RESULTS);
                for (final SDataInstanceVisibilityMapping sDataInstanceVisibilityMapping : visibilityMappings) {
                    deleteDataInstanceVisibilityMapping(containerId, sDataInstanceVisibilityMapping);
                }
            } while (visibilityMappings.size() > 0);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeContainer"));
            }
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "removeContainer", e));
            }
            throw new SDataInstanceException(e);
        }

    }

    /**
     * @param containerId
     * @param sDataInstanceVisibilityMapping
     * @throws SDataInstanceException
     */
    private void deleteDataInstanceVisibilityMapping(final long containerId, final SDataInstanceVisibilityMapping sDataInstanceVisibilityMapping)
            throws SDataInstanceException {
        final DeleteRecord record = new DeleteRecord(sDataInstanceVisibilityMapping);
        final SPersistenceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a data visibility mapping", sDataInstanceVisibilityMapping);
        final SDeleteEvent deleteEvent = (SDeleteEvent) eventBuilders.getEventBuilder().createDeleteEvent(DATA_VISIBILITY_MAPPING).done();
        try {
            recorder.recordDelete(record, deleteEvent);
            initiateLogBuilder(containerId, SQueriableLog.STATUS_OK, logBuilder, "removeContainer");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "removeContainer", e));
            }
            initiateLogBuilder(containerId, SQueriableLog.STATUS_FAIL, logBuilder, "removeContainer");
            throw new SDataInstanceException(e);
        }
    }

    @Override
    public List<SDataInstanceVisibilityMapping> createDataContainer(final long containerId, final String containerType) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "createDataContainer"));
        }
        try {
            final List<SDataInstanceVisibilityMapping> listSDataInstanceVisibilityMapping = insertMappingForLocalElement(containerId, containerType);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createDataContainer"));
            }
            return listSDataInstanceVisibilityMapping;
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createDataContainer", e));
            }
            throw new SDataInstanceException(e);
        } catch (final SDefinitiveArchiveNotFound e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createDataContainer", e));
            }
            throw new SDataInstanceException(e);
        }
    }

    protected List<SDataInstanceVisibilityMapping> insertMappingForLocalElement(final long containerId, final String containerType) throws SRecorderException,
            SDataInstanceException, SDefinitiveArchiveNotFound {
        final int batchSize = 50;
        int currentIndex = 0;
        final long archiveDate = System.currentTimeMillis();
        List<SDataInstance> localDataInstances = getLocalDataInstances(containerId, containerType, 0, batchSize);
        final List<SDataInstanceVisibilityMapping> mappings = new ArrayList<SDataInstanceVisibilityMapping>(localDataInstances.size());
        while (localDataInstances != null && localDataInstances.size() > 0) {
            for (final SDataInstance sDataInstance : localDataInstances) {
                mappings.add(insertDataInstanceVisibilityMapping(containerId, containerType, sDataInstance.getName(), sDataInstance.getId(), archiveDate));
            }
            currentIndex += batchSize;
            localDataInstances = getLocalDataInstances(containerId, containerType, currentIndex, batchSize);
        }
        return mappings;
    }

    /**
     * Insert mapping to be able to tell which is the data that is visible from the container:
     * i.e. with the given name on the given container the visible data have the id given by the visibility mapping
     * 
     * @param containerId
     * @param containerType
     * @param dataName
     * @param dataInstanceId
     * @param archiveDate
     * @throws SRecorderException
     * @throws SDefinitiveArchiveNotFound
     */
    protected SDataInstanceVisibilityMapping insertDataInstanceVisibilityMapping(final long containerId, final String containerType, final String dataName,
            final long dataInstanceId, final long archiveDate) throws SRecorderException, SDefinitiveArchiveNotFound {
        final SDataInstanceVisibilityMapping mapping = dataInstanceBuilders.getDataInstanceVisibilityMappingBuilder()
                .createNewInstance(containerId, containerType, dataName, dataInstanceId).done();
        SPersistenceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new data visibility mapping", mapping);
        final InsertRecord record = new InsertRecord(mapping);
        final SInsertEvent insertEvent = (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent(DATA_VISIBILITY_MAPPING).done();
        recorder.recordInsert(record, insertEvent);
        initiateLogBuilder(containerId, SQueriableLog.STATUS_OK, logBuilder, "insertDataInstaceVisibilityMapping");
        // add archived mapping also because when the data change the archive mapping will be used to retrieve old value
        final SADataInstanceVisibilityMapping archivedMapping = dataInstanceBuilders.getArchivedDataInstanceVisibilityMappingBuilder()
                .createNewInstance(containerId, containerType, dataName, dataInstanceId, mapping.getId()).done();
        logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new archived data visibility mapping", archivedMapping);
        archiveService.recordInsert(archiveDate, new ArchiveInsertRecord(archivedMapping), logBuilder.done());
        return mapping;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SDataInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final PersistentObject visibilityMapping) {
        final SDataInstanceLogBuilder logBuilder = dataInstanceBuilders.getDataInstanceLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public SADataInstance getSADataInstance(final long containerId, final String containerType, final String dataName, final long time)
            throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getSADataInstance"));
        }
        try {
            final long dataInstanceId = getSADataInstanceDataVisibilityMapping(dataName, containerId, containerType);
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("dataInstanceId", dataInstanceId);
            parameters.put("time", time);
            final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>(
                    "getSADataInstanceByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getSADataInstance"));
            }
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getSADataInstance", e));
            }
            throw new SDataInstanceException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public SADataInstance getSADataInstance(final long sourceObjectId, final long time) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getSADataInstance"));
        }
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("dataInstanceId", sourceObjectId);
            parameters.put("time", time);
            final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>(
                    "getSADataInstanceByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getSADataInstance"));
            }
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getSADataInstance", e));
            }
            throw new SDataInstanceException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public List<SADataInstance> getSADataInstances(final long dataInstanceId) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getSADataInstances"));
        }
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("dataInstanceId", dataInstanceId);
            final List<SADataInstance> listSADataInstance = readPersistenceService.selectList(new SelectListDescriptor<SADataInstance>(
                    "getSADataInstanceByDataInstanceId", parameters, SADataInstance.class, new QueryOptions(Collections.singletonList(new OrderByOption(
                            SADataInstance.class, dataInstanceBuilders.getDataInstanceBuilder().getArchiveDateKey(), OrderByType.DESC)))));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getSADataInstances"));
            }
            return listSADataInstance;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getSADataInstances", e));
            }
            throw new SDataInstanceException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public SADataInstance getLastSADataInstance(final long dataInstanceId) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLastSADataInstance"));
        }
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("dataInstanceId", dataInstanceId);
            final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>(
                    "getLastSADataInstanceByDataInstanceId", parameters, SADataInstance.class));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLastSADataInstance"));
            }
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getLastSADataInstance", e));
            }
            throw new SDataInstanceException("Unable to read SADataInstance", e);
        }
    }

    private SQueriableLog getQueriableLog(final ActionType actionType, final String message) {
        final SADataInstanceLogBuilder saDataInstanceLogBuilder = dataInstanceBuilders.getSADataInstanceLogBuilder();
        saDataInstanceLogBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
        saDataInstanceLogBuilder.setActionType(actionType);
        return saDataInstanceLogBuilder.done();
    }

    @Override
    public long getNumberOfDataInstances(final long containerId, final DataInstanceContainer containerType) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getNumberOfDataInstances"));
        }
        final HashMap<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType.toString());
        final SelectOneDescriptor<Long> selectOneDescriptor = new SelectOneDescriptor<Long>("getNumberOfDataInstancesForContainer", parameters,
                SDataInstanceVisibilityMapping.class);
        Long dataInstanceId;
        try {
            dataInstanceId = persistenceService.selectOne(selectOneDescriptor);
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getNumberOfDataInstances", e));
            }
            throw new SDataInstanceException(e);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getNumberOfDataInstances"));
        }
        return dataInstanceId;
    }

    @Override
    public List<SDataInstance> getDataInstances(final List<String> dataNames, final long containerId, final String containerType) throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataInstances"));
        }
        NullCheckingUtil.checkArgsNotNull(dataNames, containerType);
        // FIXME: update the service interface to take data source information as parameters instead of look for data in both datasources
        if (dataNames.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final List<Long> dataInstanceIds = getDataInstanceDataVisibilityMapping(dataNames, containerId, containerType);
            final DataInstanceDataSource transientDataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
            List<SDataInstance> result = null;
            try {
                result = transientDataInstanceDataSource.getDataInstances(dataInstanceIds);
            } catch (final SDataInstanceException e) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataInstances", e));
                }
            }
            if (result == null || result.size() < dataNames.size()) {
                final DataInstanceDataSource defaultDataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
                final ArrayList<SDataInstance> finalResult = new ArrayList<SDataInstance>(dataNames.size());
                if (result != null) {
                    finalResult.addAll(result);
                }
                finalResult.addAll(defaultDataInstanceDataSource.getDataInstances(dataInstanceIds));
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataInstances"));
                }
                return finalResult;
            } else {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataInstances"));
                }
                return result;
            }
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataInstances", e));
            }
            throw new SDataInstanceException("Unable to find the data in the data mapping", e);
        }
    }

    @Override
    public List<SADataInstance> getSADataInstances(final long containerId, final String containerType, final List<String> dataNames, final long time)
            throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getSADataInstances"));
        }
        if (dataNames.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final List<Long> dataInstanceIds = getSADataInstanceDataVisibilityMapping(dataNames, containerId, containerType);
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("dataInstanceIds", dataInstanceIds);
            parameters.put("time", time);
            final List<SADataInstance> listSADataInstance = readPersistenceService.selectList(new SelectListDescriptor<SADataInstance>(
                    "getSADataInstancesByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class, new QueryOptions(0, dataInstanceIds.size())));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getSADataInstances"));
            }
            return listSADataInstance;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getSADataInstances", e));
            }
            throw new SDataInstanceException("Unable to read SADataInstance", e);
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public List<SADataInstance> getLocalSADataInstances(final long containerId, final String containerType, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getLocalSADataInstances"));
        }
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("containerId", containerId);
            parameters.put("containerType", containerType);
            final List<SADataInstance> saDataInstances = readPersistenceService.selectList(new SelectListDescriptor<SADataInstance>("getLocalSADataInstances",
                    parameters, SADataInstance.class, new QueryOptions(fromIndex, numberOfResults)));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getLocalSADataInstances"));
            }
            return saDataInstances;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getLocalSADataInstances", e));
            }
            throw new SDataInstanceException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public void deleteSADataInstance(final SADataInstance dataInstance) throws SDeleteDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final SPersistenceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting an archived data instance", dataInstance);
        final DeleteRecord deleteRecord = new DeleteRecord(dataInstance);
        final SEvent event = eventBuilders.getEventBuilder().createDeleteEvent(DataInstanceDataSource.DATA_INSTANCE).setObject(dataInstance).done();
        final SDeleteEvent deleteEvent = (SDeleteEvent) event;
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(dataInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteDataInstance");

        } catch (final SRecorderException e) {
            initiateLogBuilder(dataInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteDataInstance");

            throw new SDeleteDataInstanceException("Impossible to delete data instance", e);
        }
    }
}
