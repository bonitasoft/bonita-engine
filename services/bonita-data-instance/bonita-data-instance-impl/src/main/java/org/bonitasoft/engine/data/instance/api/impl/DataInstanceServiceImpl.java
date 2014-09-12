/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.archive.SDefinitiveArchiveNotFound;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SCreateDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.exception.SDeleteDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SUpdateDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SDataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstanceVisibilityMapping;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceVisibilityMappingBuilderFactory;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceVisibilityMappingBuilderFactory;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * General mechanism for lookup is to look in specific flow node to search a data instance. When refering to "local" data instance, it means the lookup is
 * performed only on the specific element, and not on inherited data for parent containers.
 * 
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta: include data instance data source directly here
 */
public class DataInstanceServiceImpl implements DataInstanceService {

    private static final String DATA_INSTANCE = "DATA_INSTANCE";

    protected final Recorder recorder;

    protected final ReadPersistenceService persistenceService;

    protected final ArchiveService archiveService;

    protected final TechnicalLoggerService logger;

    public DataInstanceServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final ArchiveService archiveService, final TechnicalLoggerService logger) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.archiveService = archiveService;
        this.logger = logger;
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
    public SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);
        try {
            final long dataInstanceId = getDataInstanceDataVisibilityMapping(dataName, containerId, containerType);
            return getDataInstance(dataInstanceId);
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("No data found with name " + dataName + "  neither on container " + containerId + " with type "
                    + containerType
                    + " nor in its parents", e);
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

    protected long getSADataInstanceDataVisibilityMapping(final String dataName, final long containerId, final String containerType)
            throws SBonitaReadException {
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

    protected List<Long> getSADataInstanceDataVisibilityMapping(final List<String> dataNames, final long containerId, final String containerType)
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
        NullCheckingUtil.checkArgsNotNull(containerType);
        try {
            final List<SDataInstanceVisibilityMapping> mappings = getDataInstanceVisibilityMappings(containerId, containerType, fromIndex, numberOfResults);
            final ArrayList<SDataInstance> dataInstances = new ArrayList<SDataInstance>(mappings.size());
            for (final SDataInstanceVisibilityMapping mapping : mappings) {
                dataInstances.add(getDataInstance(mapping.getDataInstanceId()));
            }
            return dataInstances;
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Unable to read data mappings of the container with type " + containerType + " and id " + containerId, e);
        }
    }

    protected List<SDataInstanceVisibilityMapping> getDataInstanceVisibilityMappings(final long containerId, final String containerType, final int fromIndex,
            final int numberOfResults) throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        final SelectListDescriptor<SDataInstanceVisibilityMapping> selectDescriptor = new SelectListDescriptor<SDataInstanceVisibilityMapping>(
                "getDataInstanceVisibilityMappings", parameters, SDataInstanceVisibilityMapping.class, new QueryOptions(fromIndex, numberOfResults));
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public SDataInstance getLocalDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceReadException {
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        final Map<String, Object> paraMap = CollectionUtil.buildSimpleMap(fact.getNameKey(), dataName);
        paraMap.put(fact.getContainerIdKey(), containerId);
        paraMap.put(fact.getContainerTypeKey(), containerType);

        try {
            final SDataInstance dataInstance = persistenceService.selectOne(new SelectOneDescriptor<SDataInstance>("getDataInstancesByNameAndContainer",
                    paraMap,
                    SDataInstance.class, SDataInstance.class)); // conditions :and not or
            if (dataInstance == null) {
                throw new SDataInstanceReadException("No data instance found");
            }
            return dataInstance;
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Unable to check if a data instance already exists: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SDataInstance> getLocalDataInstances(final long containerId, final String containerType, final int fromIndex, final int numberOfResults)
            throws SDataInstanceReadException {
        NullCheckingUtil.checkArgsNotNull(containerType);
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        final Map<String, Object> paraMap = CollectionUtil.buildSimpleMap(fact.getContainerIdKey(), containerId);
        final OrderByOption orderByOption = new OrderByOption(SDataInstance.class, fact.getIdKey(), OrderByType.ASC);
        paraMap.put(fact.getContainerTypeKey(), containerType);

        try {
            return persistenceService.selectList(new SelectListDescriptor<SDataInstance>("getDataInstancesByContainer", paraMap, SDataInstance.class,
                    SDataInstance.class, new QueryOptions(fromIndex, numberOfResults, Arrays.asList(orderByOption))));
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Unable to check if a data instance already exists for the data container of type " + containerType
                    + " with id "
                    + containerId + " for reason: " + e.getMessage(), e);
        }
    }

    @Override
    public void addChildContainer(final long parentContainerId, final String parentContainerType, final long containerId, final String containerType, boolean shouldArchiveMapping)
            throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "addChildContainer");
        try {
            // insert mappings from parent element
            final List<SDataInstanceVisibilityMapping> mappings = insertMappingForLocalElement(containerId, containerType, shouldArchiveMapping);
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
                        insertDataInstanceVisibilityMapping(containerId, containerType, parentData.getName(), parentData.getId(), archivedDate, shouldArchiveMapping);
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
                visibilityMappings = getDataInstanceVisibilityMappings(containerId, containerType, 0, 100);
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
    public List<SDataInstanceVisibilityMapping> createDataContainer(final long containerId, final String containerType, boolean shouldArchiveMapping) throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "createDataContainer");
        try {
            final List<SDataInstanceVisibilityMapping> listSDataInstanceVisibilityMapping = insertMappingForLocalElement(containerId, containerType, shouldArchiveMapping);
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

    protected List<SDataInstanceVisibilityMapping> insertMappingForLocalElement(final long containerId, final String containerType, boolean shouldArchiveMapping) throws SRecorderException,
            SDataInstanceException, SDefinitiveArchiveNotFound {
        final int batchSize = 50;
        int currentIndex = 0;
        final long archiveDate = System.currentTimeMillis();
        List<SDataInstance> localDataInstances = getLocalDataInstances(containerId, containerType, 0, batchSize);
        final List<SDataInstanceVisibilityMapping> mappings = new ArrayList<SDataInstanceVisibilityMapping>(localDataInstances.size());
        while (localDataInstances != null && localDataInstances.size() > 0) {
            for (final SDataInstance sDataInstance : localDataInstances) {
                mappings.add(insertDataInstanceVisibilityMapping(containerId, containerType, sDataInstance.getName(), sDataInstance.getId(), archiveDate, shouldArchiveMapping));
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
     * @param shouldArchive
     *          true if we create an archived version of the mapping
     * @throws SRecorderException
     * @throws SDefinitiveArchiveNotFound
     */
    protected SDataInstanceVisibilityMapping insertDataInstanceVisibilityMapping(final long containerId, final String containerType, final String dataName,
            final long dataInstanceId, final long archiveDate, final boolean shouldArchive) throws SRecorderException, SDefinitiveArchiveNotFound {
        final SDataInstanceVisibilityMapping mapping = createDataInstanceVisibilityMapping(containerId, containerType, dataName, dataInstanceId);
        final InsertRecord record = new InsertRecord(mapping);
        final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DATA_VISIBILITY_MAPPING).done();
        recorder.recordInsert(record, insertEvent);
        // add archived mapping also because when the data change the archive mapping will be used to retrieve old value
        final SADataInstanceVisibilityMapping archivedMapping = BuilderFactory.get(SADataInstanceVisibilityMappingBuilderFactory.class)
                .createNewInstance(containerId, containerType, dataName, dataInstanceId, mapping.getId()).done();
        if(shouldArchive){
            archiveService.recordInsert(archiveDate, new ArchiveInsertRecord(archivedMapping));
        }
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
        if (dataNames.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final List<Long> dataInstanceIds = getDataInstanceDataVisibilityMapping(dataNames, containerId, containerType);
            final ArrayList<SDataInstance> finalResult = new ArrayList<SDataInstance>(dataNames.size());
            finalResult.addAll(getDataInstances(dataInstanceIds));
            logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstances");
            return finalResult;
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

    private void deleteSADataInstance(final SADataInstance dataInstance) throws SDeleteDataInstanceException {
        final DeleteRecord deleteRecord = new DeleteRecord(dataInstance);
        final SEvent event = BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DATA_INSTANCE).setObject(dataInstance)
                .done();
        final SDeleteEvent deleteEvent = (SDeleteEvent) event;
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SDeleteDataInstanceException("Impossible to delete data instance", e);
        }
    }

    @Override
    public void deleteLocalArchivedDataInstances(final long containerId, final String containerType) throws SDataInstanceException {
        List<SADataInstance> sDataInstances;
        do {
            sDataInstances = getLocalSADataInstances(containerId, containerType, 0, 100);
            for (final SADataInstance sDataInstance : sDataInstances) {
                deleteSADataInstance(sDataInstance);
            }
        } while (!sDataInstances.isEmpty());
        deleteArchivedContainer(containerId, containerType);
    }

    private void deleteArchivedContainer(final long containerId, final String containerType) throws SDataInstanceException {
        try {
            List<SADataInstanceVisibilityMapping> visibilityMappings;
            do {
                visibilityMappings = getSADataInstanceVisibilityMappings(containerId, containerType, 0, 100);
                for (final SADataInstanceVisibilityMapping sDataInstanceVisibilityMapping : visibilityMappings) {
                    deleteSADataInstanceVisibilityMapping(sDataInstanceVisibilityMapping);
                }
            } while (visibilityMappings.size() > 0);
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException(e);
        }
    }

    private void deleteSADataInstanceVisibilityMapping(final SADataInstanceVisibilityMapping sDataInstanceVisibilityMapping) throws SDataInstanceException {
        final DeleteRecord record = new DeleteRecord(sDataInstanceVisibilityMapping);
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DATA_VISIBILITY_MAPPING).done();
        try {
            recorder.recordDelete(record, deleteEvent);
        } catch (final SRecorderException e) {
            logOnExceptionMethod(TechnicalLogSeverity.TRACE, "deleteSADataInstanceVisibilityMapping", e);
            throw new SDataInstanceException(e);
        }
    }

    protected List<SADataInstanceVisibilityMapping> getSADataInstanceVisibilityMappings(final long containerId, final String containerType,
            final int fromIndex, final int numberOfResults) throws SBonitaReadException {
        final HashMap<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("containerId", containerId);
        parameters.put("containerType", containerType);
        final SelectListDescriptor<SADataInstanceVisibilityMapping> selectDescriptor = new SelectListDescriptor<SADataInstanceVisibilityMapping>(
                "getSADataInstanceVisibilityMappings", parameters, SADataInstanceVisibilityMapping.class, new QueryOptions(fromIndex, numberOfResults));
        return persistenceService.selectList(selectDescriptor);
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

    // ==================================================
    // was the data instance data source code

    private SInsertEvent getInsertEvent(final Object obj) {
        return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DATA_INSTANCE).setObject(obj).done();
    }

    private SUpdateEvent getUpdateEvent(final Object obj) {
        return (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(DATA_INSTANCE).setObject(obj).done();
    }

    private SDeleteEvent getDeleteEvent(final Object obj) {
        return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DATA_INSTANCE).setObject(obj).done();
    }

    @Override
    public void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        try {
            final InsertRecord insertRecord = new InsertRecord(dataInstance);
            final SInsertEvent insertEvent = getInsertEvent(dataInstance);
            recorder.recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SCreateDataInstanceException("Impossible to create data instance.", e);
        }
        archiveDataInstance(dataInstance);
    }

    @Override
    public void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(dataInstance, descriptor);
        final SUpdateEvent updateEvent = getUpdateEvent(dataInstance);
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SUpdateDataInstanceException("Impossible to update data instance '" + dataInstance.getName() + "': " + e.getMessage(), e);
        }
        archiveDataInstance(dataInstance);
    }

    @Override
    public void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        final DeleteRecord deleteRecord = new DeleteRecord(dataInstance);
        final SDeleteEvent deleteEvent = getDeleteEvent(dataInstance);
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SDeleteDataInstanceException("Impossible to delete data instance", e);
        }
    }

    @Override
    public SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstanceId);
        try {
            final SelectByIdDescriptor<SDataInstance> selectDescriptor = new SelectByIdDescriptor<SDataInstance>("getDataInstanceById", SDataInstance.class,
                    dataInstanceId);
            final SDataInstance dataInstance = persistenceService.selectById(selectDescriptor);
            if (dataInstance == null) {
                throw new SDataInstanceNotFoundException("Cannot get the data instance with id " + dataInstanceId);
            }
            return dataInstance;
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Cannot get the data instance with id " + dataInstanceId, e);
        }
    }

    private List<SDataInstance> getDataInstances(final List<Long> dataInstanceIds) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstanceIds);
        if (dataInstanceIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            final Map<String, Object> parameters = CollectionUtil.buildSimpleMap("ids", dataInstanceIds);
            final SelectListDescriptor<SDataInstance> selectDescriptor = new SelectListDescriptor<SDataInstance>("getDataInstanceByIds", parameters,
                    SDataInstance.class, new QueryOptions(0, dataInstanceIds.size()));
            final List<SDataInstance> dataInstances = persistenceService.selectList(selectDescriptor);
            if (dataInstances == null) {
                throw new SDataInstanceNotFoundException("Cannot get the data instance with id " + dataInstanceIds);
            }
            return dataInstances;
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Cannot get the data instance with id " + dataInstanceIds, e);
        }
    }

}
