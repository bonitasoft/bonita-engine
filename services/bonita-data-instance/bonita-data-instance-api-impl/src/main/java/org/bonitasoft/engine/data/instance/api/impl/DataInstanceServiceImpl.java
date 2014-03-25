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
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.instance.DataInstanceDataSource;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.exception.SDeleteDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceVisibilityMappingBuilderFactory;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceVisibilityMappingBuilderFactory;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;

/**
 * General mechanism for lookup is to look in specific flow node to search a data instance. When refering to "local" data instance, it means the lookup is
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

    protected final Recorder recorder;

    protected final ReadPersistenceService persistenceService;

    protected final ArchiveService archiveService;

    protected final TechnicalLoggerService logger;

    public DataInstanceServiceImpl(final DataService dataSourceService, final Recorder recorder, final ReadPersistenceService persistenceService,
            final ArchiveService archiveService, final TechnicalLoggerService logger) {
        this.dataSourceService = dataSourceService;
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.archiveService = archiveService;
        this.logger = logger;
    }

    // FIXME this should be done BEFORE insertChildContainer... should we add a check mappings and add it here too
    @Override
    public void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "createDataInstance");
        final DataInstanceDataSource dataInstanceDataSource = getDataInstanceDataSource(dataInstance.isTransientData());
        dataInstanceDataSource.createDataInstance(dataInstance);
        archiveDataInstance(dataInstance);
        logAfterMethod(TechnicalLogSeverity.TRACE, "createDataInstance");
    }

    private DataInstanceDataSource getDataInstanceDataSource(final String dataSourceName, final String dataSourceVersion) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getDataInstanceDataSource");
        try {
            final SDataSource dataSource = dataSourceService.getDataSource(dataSourceName, dataSourceVersion);
            final DataInstanceDataSource dataInstanceDataSource = dataSourceService.getDataSourceImplementation(DataInstanceDataSource.class,
                    dataSource.getId());
            logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstanceDataSource");
            return dataInstanceDataSource;
        } catch (final SBonitaException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getDataInstanceDataSource", e);
            throw new SDataInstanceException("Unable to get data instance data source", e);
        }
    }

    private DataInstanceDataSource getDataInstanceDataSource(final boolean isTransient) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getDataInstanceDataSource");
        final DataInstanceDataSource dataInstanceDataSource;
        if (isTransient) {
            dataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
        } else {
            dataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
        }
        logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstanceDataSource");
        return dataInstanceDataSource;
    }

    @Override
    public void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "updateDataInstance");
        NullCheckingUtil.checkArgsNotNull(dataInstance, descriptor);
        if (dataInstance.isTransientData()) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "Updating a transient data instance is not a good practice.");
            }
        }
        final DataInstanceDataSource dataInstanceDataSource = getDataInstanceDataSource(dataInstance.isTransientData());
        dataInstanceDataSource.updateDataInstance(dataInstance, descriptor);
        logAfterMethod(TechnicalLogSeverity.TRACE, "updateDataInstance");
        archiveDataInstance(dataInstance);
    }

    private void archiveDataInstance(final SDataInstance sDataInstance) throws SDataInstanceException {
        archiveDataInstance(sDataInstance, System.currentTimeMillis());
    }

    private void archiveDataInstance(final SDataInstance sDataInstance, final long archiveDate) throws SDataInstanceException {
        if (!sDataInstance.isTransientData()) {
            try {
                final SADataInstance saDataInstance = BuilderFactory.get(SADataInstanceBuilderFactory.class).createNewInstance(sDataInstance).done();
                final ArchiveInsertRecord archiveInsertRecord = new ArchiveInsertRecord(saDataInstance);
                archiveService.recordInsert(archiveDate, archiveInsertRecord);
            } catch (final SDefinitiveArchiveNotFound e) {
                logOnExceptionMethod(TechnicalLogSeverity.TRACE, "updateDataInstance", e);
                throw new SDataInstanceException("Unable to create SADataInstance", e);
            } catch (final SRecorderException e) {
                logOnExceptionMethod(TechnicalLogSeverity.TRACE, "updateDataInstance", e);
                throw new SDataInstanceException("Unable to create SADataInstance", e);
            }
        }
    }

    @Override
    public void archiveLocalDataInstancesFromProcessInstance(final long processInstanceId, final long archiveDate) throws SDataInstanceException {
        final int archiveBatchSize = 50;
        int currentIndex = 0;
        List<SDataInstance> sDataInstances = getLocalDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString(), currentIndex,
                archiveBatchSize);

        while (sDataInstances != null && sDataInstances.size() > 0) {
            for (final SDataInstance sDataInstance : sDataInstances) {
                archiveDataInstance(sDataInstance, archiveDate);
            }
            currentIndex += archiveBatchSize;
            sDataInstances = getLocalDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString(), currentIndex, archiveBatchSize);
        }
    }

    @Override
    public void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "deleteDataInstance");
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final DataInstanceDataSource dataInstanceDataSource = getDataInstanceDataSource(dataInstance.isTransientData());
        dataInstanceDataSource.deleteDataInstance(dataInstance);
        logAfterMethod(TechnicalLogSeverity.TRACE, "deleteDataInstance");
    }

    @Override
    public SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException {
        // FIXME: update the service interface to take data source information as parameters instead of look for data in both datasources
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getDataInstance");
        try {
            return getDataInstanceById(dataInstanceId);
        } finally {
            logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstance");
        }
    }

    @Override
    public SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceException {
        // FIXME: update the service interface to take data source information as parameters instead of look for data in both datasources
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getDataInstance");

        NullCheckingUtil.checkArgsNotNull(dataName, containerType);
        try {
            final long dataInstanceId = getDataInstanceDataVisibilityMapping(dataName, containerId, containerType);
            return getDataInstanceById(dataInstanceId);
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getDataInstance", e);
            throw new SDataInstanceReadException("No data found with name " + dataName + "  neither on container " + containerId + " with type "
                    + containerType
                    + " nor in its parents", e);
        } finally {
            logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstance");
        }
    }

    private SDataInstance getDataInstanceById(final long dataInstanceId) throws SDataInstanceException {
        final DataInstanceDataSource transientDataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
        try {
            return transientDataInstanceDataSource.getDataInstance(dataInstanceId);
        } catch (final SDataInstanceException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getDataInstance", e);
            final DataInstanceDataSource defaultDataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
            return defaultDataInstanceDataSource.getDataInstance(dataInstanceId);
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
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getDataInstances");
        NullCheckingUtil.checkArgsNotNull(containerType);
        try {
            final List<SDataInstanceVisibilityMapping> mappings = getDataInstanceVisibilityMappings(containerId, containerType, fromIndex, numberOfResults);
            final ArrayList<SDataInstance> dataInstances = new ArrayList<SDataInstance>(mappings.size());
            for (final SDataInstanceVisibilityMapping mapping : mappings) {
                dataInstances.add(getDataInstance(mapping.getDataInstanceId()));
            }
            logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstances");
            return dataInstances;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getDataInstances", e);
            throw new SDataInstanceReadException("Unable to read data mappings of the container with type " + containerType + " and id " + containerId, e);
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
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getLocalDataInstance");
        NullCheckingUtil.checkArgsNotNull(dataName);
        NullCheckingUtil.checkArgsNotNull(containerType);

        // FIXME: update the service interface to take data source information as parameters instead of look for data in both datasources
        final DataInstanceDataSource transientDataInstanceDataSource = getDataInstanceDataSource(TRANSIENT_DATA_SOURCE, TRANSIENT_DATA_SOURCE_VERSION);
        try {
            return transientDataInstanceDataSource.getDataInstance(dataName, containerId, containerType);
        } catch (final SDataInstanceException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getLocalDataInstance", e);
            final DataInstanceDataSource defaultDataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
            return defaultDataInstanceDataSource.getDataInstance(dataName, containerId, containerType);
        } finally {
            logAfterMethod(TechnicalLogSeverity.TRACE, "getLocalDataInstance");
        }
    }

    @Override
    public List<SDataInstance> getLocalDataInstances(final long containerId, final String containerType, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getLocalDataInstances");
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
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getLocalDataInstances", e);
            throw e;
        } finally {
            logAfterMethod(TechnicalLogSeverity.TRACE, "getLocalDataInstances");
        }
    }

    @Override
    public void addChildContainer(final long parentContainerId, final String parentContainerType, final long containerId, final String containerType)
            throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "addChildContainer");
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
            logAfterMethod(TechnicalLogSeverity.TRACE, "addChildContainer");
        } catch (final SRecorderException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "addChildContainer", e);
            throw new SDataInstanceException(e);
        } catch (final SDefinitiveArchiveNotFound e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "addChildContainer", e);
            throw new SDataInstanceException(e);
        }
    }

    @Override
    public void removeContainer(final long containerId, final String containerType) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "removeContainer");
        try {
            List<SDataInstanceVisibilityMapping> visibilityMappings;
            do {
                visibilityMappings = getDataInstanceVisibilityMappings(containerId, containerType, 0, QueryOptions.DEFAULT_NUMBER_OF_RESULTS);
                for (final SDataInstanceVisibilityMapping sDataInstanceVisibilityMapping : visibilityMappings) {
                    deleteDataInstanceVisibilityMapping(sDataInstanceVisibilityMapping);
                }
            } while (visibilityMappings.size() > 0);
            logAfterMethod(TechnicalLogSeverity.TRACE, "removeContainer");
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "removeContainer", e);
            throw new SDataInstanceReadException(e);
        }

    }

    /**
     * @param sDataInstanceVisibilityMapping
     * @throws SDataInstanceReadException
     */
    private void deleteDataInstanceVisibilityMapping(final SDataInstanceVisibilityMapping sDataInstanceVisibilityMapping) throws SDataInstanceException {
        final DeleteRecord record = new DeleteRecord(sDataInstanceVisibilityMapping);
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DATA_VISIBILITY_MAPPING).done();
        try {
            recorder.recordDelete(record, deleteEvent);
        } catch (final SRecorderException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "deleteDataInstanceVisibilityMapping", e);
            throw new SDataInstanceException(e);
        }
    }

    @Override
    public List<SDataInstanceVisibilityMapping> createDataContainer(final long containerId, final String containerType) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "createDataContainer");
        try {
            final List<SDataInstanceVisibilityMapping> listSDataInstanceVisibilityMapping = insertMappingForLocalElement(containerId, containerType);
            logAfterMethod(TechnicalLogSeverity.TRACE, "createDataContainer");
            return listSDataInstanceVisibilityMapping;
        } catch (final SRecorderException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "createDataContainer", e);
            throw new SDataInstanceException(e);
        } catch (final SDefinitiveArchiveNotFound e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "createDataContainer", e);
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
        final SDataInstanceVisibilityMapping mapping = createDataInstanceVisibilityMapping(containerId, containerType, dataName, dataInstanceId);
        final InsertRecord record = new InsertRecord(mapping);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DATA_VISIBILITY_MAPPING).done();
        recorder.recordInsert(record, insertEvent);
        // add archived mapping also because when the data change the archive mapping will be used to retrieve old value
        final SADataInstanceVisibilityMapping archivedMapping = BuilderFactory.get(SADataInstanceVisibilityMappingBuilderFactory.class)
                .createNewInstance(containerId, containerType, dataName, dataInstanceId, mapping.getId()).done();
        archiveService.recordInsert(archiveDate, new ArchiveInsertRecord(archivedMapping));
        return mapping;
    }

    protected SDataInstanceVisibilityMapping createDataInstanceVisibilityMapping(final long containerId, final String containerType, final String dataName,
            final long dataInstanceId) {
        return BuilderFactory.get(SDataInstanceVisibilityMappingBuilderFactory.class).createNewInstance(containerId, containerType, dataName, dataInstanceId)
                .done();
    }

    @Override
    public SADataInstance getSADataInstance(final long containerId, final String containerType, final String dataName, final long time)
            throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getSADataInstance");
        try {
            final long dataInstanceId = getSADataInstanceDataVisibilityMapping(dataName, containerId, containerType);
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("dataInstanceId", dataInstanceId);
            parameters.put("time", time);
            final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>(
                    "getSADataInstanceByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class));
            logAfterMethod(TechnicalLogSeverity.TRACE, "getSADataInstance");
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getSADataInstance", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public SADataInstance getSADataInstance(final long sourceObjectId, final long time) throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getSADataInstance");
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("dataInstanceId", sourceObjectId);
            parameters.put("time", time);
            final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>(
                    "getSADataInstanceByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class));
            logAfterMethod(TechnicalLogSeverity.TRACE, "getSADataInstance");
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getSADataInstance", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public List<SADataInstance> getSADataInstances(final long dataInstanceId) throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getSADataInstances");
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("dataInstanceId", dataInstanceId);
            final List<SADataInstance> listSADataInstance = readPersistenceService.selectList(new SelectListDescriptor<SADataInstance>(
                    "getSADataInstanceByDataInstanceId", parameters, SADataInstance.class, new QueryOptions(Collections.singletonList(new OrderByOption(
                            SADataInstance.class, BuilderFactory.get(SDataInstanceBuilderFactory.class).getArchiveDateKey(), OrderByType.DESC)))));
            logAfterMethod(TechnicalLogSeverity.TRACE, "getSADataInstances");
            return listSADataInstance;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getSADataInstances", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public SADataInstance getLastSADataInstance(final long dataInstanceId) throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getLastSADataInstance");
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put("dataInstanceId", dataInstanceId);
            final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>(
                    "getLastSADataInstanceByDataInstanceId", parameters, SADataInstance.class));
            logAfterMethod(TechnicalLogSeverity.TRACE, "getLastSADataInstance");
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getLastSADataInstance", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public SADataInstance getLastSADataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getLastSADataInstance");
        final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("dataName", dataName);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        try {
            final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>("getLastSADataInstanceByContainer",
                    parameters, SADataInstance.class));
            if (saDataInstance == null) {
                final SDataInstanceNotFoundException exception = new SDataInstanceNotFoundException("No archived data instance found for data:" + dataName
                        + " in container: " + containerType + " " + containerId);
                logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getLastSADataInstance", exception);
                throw exception;
            }
            logAfterMethod(TechnicalLogSeverity.TRACE, "getLastSADataInstance");
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getLastSADataInstance", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public List<SADataInstance> getLastLocalSADataInstances(final long containerId, final String containerType, final int startIndex, final int maxResults)
            throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getLastLocalSADataInstances");
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("containerId", containerId);
            parameters.put("containerType", containerType);
            final List<SADataInstance> saDataInstances = readPersistenceService.selectList(new SelectListDescriptor<SADataInstance>(
                    "getLastLocalSADataInstances", parameters, SADataInstance.class, new QueryOptions(startIndex, maxResults)));
            logAfterMethod(TechnicalLogSeverity.TRACE, "getLastLocalSADataInstances");
            return saDataInstances;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getLastLocalSADataInstances", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public long getNumberOfDataInstances(final long containerId, final DataInstanceContainer containerType) throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getNumberOfDataInstances");
        final HashMap<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType.toString());
        final SelectOneDescriptor<Long> selectOneDescriptor = new SelectOneDescriptor<Long>("getNumberOfDataInstancesForContainer", parameters,
                SDataInstanceVisibilityMapping.class);
        Long dataInstanceId;
        try {
            dataInstanceId = persistenceService.selectOne(selectOneDescriptor);
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getNumberOfDataInstances", e);
            throw new SDataInstanceReadException(e);
        }
        logAfterMethod(TechnicalLogSeverity.TRACE, "getNumberOfDataInstances");
        return dataInstanceId;
    }

    @Override
    public List<SDataInstance> getDataInstances(final List<String> dataNames, final long containerId, final String containerType)
            throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getDataInstances");
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
                logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getDataInstances", e);
            }
            if (result == null || result.size() < dataNames.size()) {
                final DataInstanceDataSource defaultDataInstanceDataSource = getDataInstanceDataSource(DEFAULT_DATA_SOURCE, DATA_SOURCE_VERSION);
                final ArrayList<SDataInstance> finalResult = new ArrayList<SDataInstance>(dataNames.size());
                if (result != null) {
                    finalResult.addAll(result);
                }
                finalResult.addAll(defaultDataInstanceDataSource.getDataInstances(dataInstanceIds));
                result = finalResult;
            }
            logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstances");
            return result;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getDataInstances", e);
            throw new SDataInstanceReadException("Unable to find the data in the data mapping with name = " + dataNames + ", containerId = " + containerId
                    + ", containerType = " + containerType, e);
        }
    }

    @Override
    public List<SADataInstance> getSADataInstances(final long containerId, final String containerType, final List<String> dataNames, final long time)
            throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getSADataInstances");
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
            logAfterMethod(TechnicalLogSeverity.TRACE, "getSADataInstances");
            return listSADataInstance;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getSADataInstances", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public List<SADataInstance> getLocalSADataInstances(final long containerId, final String containerType, final int fromIndex, final int numberOfResults)
            throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getLocalSADataInstances");
        try {
            final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("containerId", containerId);
            parameters.put("containerType", containerType);
            final List<SADataInstance> saDataInstances = readPersistenceService.selectList(new SelectListDescriptor<SADataInstance>("getLocalSADataInstances",
                    parameters, SADataInstance.class, new QueryOptions(fromIndex, numberOfResults)));
            logAfterMethod(TechnicalLogSeverity.TRACE, "getLocalSADataInstances");
            return saDataInstances;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getLocalSADataInstances", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public void deleteSADataInstance(final SADataInstance dataInstance) throws SDeleteDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final DeleteRecord deleteRecord = new DeleteRecord(dataInstance);
        final SEvent event = BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DataInstanceDataSource.DATA_INSTANCE).setObject(dataInstance)
                .done();
        final SDeleteEvent deleteEvent = (SDeleteEvent) event;
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SDeleteDataInstanceException("Impossible to delete data instance", e);
        }
    }

    @Override
    public void deleteLocalArchivedDataInstances(final long containerId, final String dataInstanceContainerType) throws SDataInstanceException {
        List<SADataInstance> sDataInstances;
        do {
            sDataInstances = getLocalSADataInstances(containerId, dataInstanceContainerType, 0, 100);
            for (final SADataInstance sDataInstance : sDataInstances) {
                deleteSADataInstance(sDataInstance);
            }
        } while (!sDataInstances.isEmpty());
    }

    @Override
    public void deleteLocalDataInstances(final long containerId, final String dataInstanceContainerType, final boolean dataPresent)
            throws SDataInstanceException {
        if (dataPresent) {
            final int deleteBatchSize = 80;
            List<SDataInstance> sDataInstances = getLocalDataInstances(containerId, dataInstanceContainerType, 0, deleteBatchSize);
            while (sDataInstances.size() > 0) {
                for (final SDataInstance sDataInstance : sDataInstances) {
                    deleteDataInstance(sDataInstance);
                }
                sDataInstances = getLocalDataInstances(containerId, dataInstanceContainerType, 0, deleteBatchSize);
            }
        }
        removeContainer(containerId, dataInstanceContainerType);
    }

    private void logBeforeMethod(final TechnicalLogSeverity technicalLogSeverity, final String methodName) {
        if (logger.isLoggable(this.getClass(), technicalLogSeverity)) {
            logger.log(this.getClass(), technicalLogSeverity, LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }

    private void logAfterMethod(final TechnicalLogSeverity technicalLogSeverity, final String methodName) {
        if (logger.isLoggable(this.getClass(), technicalLogSeverity)) {
            logger.log(this.getClass(), technicalLogSeverity, LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }

    private void logOnExceptionMethod(final TechnicalLogSeverity technicalLogSeverity, final String methodName, final Exception e) {
        if (logger.isLoggable(this.getClass(), technicalLogSeverity)) {
            logger.log(this.getClass(), technicalLogSeverity, LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, e));
        }
    }

}
