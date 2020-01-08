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
package org.bonitasoft.engine.data.instance.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.data.instance.api.DataContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SCreateDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceReadException;
import org.bonitasoft.engine.data.instance.exception.SDeleteDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SUpdateDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceBuilder;
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
 * General mechanism for lookup is to look in specific flow node to search a data instance. When referring to "local"
 * data instance, it means the lookup is
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
        if (!sDataInstance.isTransientData()) {
            try {
                final SADataInstance saDataInstance = new SADataInstanceBuilder().createNewInstance(sDataInstance);
                final ArchiveInsertRecord archiveInsertRecord = new ArchiveInsertRecord(saDataInstance);
                archiveService.recordInsert(System.currentTimeMillis(), archiveInsertRecord);
            } catch (final SRecorderException e) {
                logOnExceptionMethod("updateDataInstance", e);
                throw new SDataInstanceException("Unable to create SADataInstance", e);
            }
        }
    }

    @Override
    public SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);

        final String queryName = "getDataInstancesWithNames";
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("dataNames", Collections.singletonList(dataName));
        final List<SDataInstance> dataInstances = getSDatainstanceOfContainers(containerId, containerType,
                parentContainerResolver, queryName, inputParameters);
        if (dataInstances.size() == 0) {
            throw new SDataInstanceNotFoundException(
                    "DataInstance with name not found: [name: " + dataName + ", container type: " + containerType
                            + ", container id: " + containerId + ']');
        } else if (dataInstances.size() > 1) {
            //should never happen but in case...
            throw new SDataInstanceReadException(
                    "Several data have been retrieved for: [name: " + dataName + ", container type: " + containerType
                            + ", container id: " + containerId + ']');
        } else {
            return dataInstances.get(0);
        }
    }

    @Override
    public List<SDataInstance> getDataInstances(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(containerType);

        final String queryName = "getDataInstances";
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        final List<SDataInstance> dataInstances = getSDatainstanceOfContainers(containerId, containerType,
                parentContainerResolver, queryName, inputParameters);

        //apply pagination
        final int startIndex = Math.max(0, fromIndex);
        final int toIndex = Math.min(dataInstances.size(), fromIndex + numberOfResults);

        return dataInstances.subList(startIndex, toIndex);
    }

    private Map<String, List<Long>> buildContainersMap(final List<DataContainer> containerHierarchy,
            final Map<String, Object> inputParameters) {
        final Map<String, List<Long>> containers = new HashMap<String, List<Long>>();
        for (DataContainer container : containerHierarchy) {
            final String containerTypeKey = container.getType();
            if (!containers.containsKey(containerTypeKey)) {
                containers.put(containerTypeKey, new ArrayList<Long>());
                inputParameters.put("containerType" + containers.size(), containerTypeKey);
                inputParameters.put("containerType" + containers.size() + "Ids", containers.get(containerTypeKey));
            }
            containers.get(containerTypeKey).add(container.getId());
        }
        return containers;
    }

    private List<SDataInstance> getSDatainstanceOfContainers(long containerId, String containerType,
            ParentContainerResolver parentContainerResolver,
            String queryName, Map<String, Object> inputParameters)
            throws SDataInstanceNotFoundException, SDataInstanceReadException {
        //getAllContainers from me to root
        final List<DataContainer> containerHierarchy;
        try {
            containerHierarchy = parentContainerResolver
                    .getContainerHierarchy(new DataContainer(containerId, containerType));
        } catch (SObjectNotFoundException | SObjectReadException e) {
            throw new SDataInstanceNotFoundException(e);
        }

        final Map<String, List<Long>> containers = buildContainersMap(containerHierarchy, inputParameters);

        //gte all data of any of th possible containers
        List<SDataInstance> dataInstances;
        try {
            dataInstances = persistenceService.selectList(
                    new SelectListDescriptor<SDataInstance>(getDynamicContainersQueryName(queryName, containers.size()),
                            inputParameters, SDataInstance.class,
                            new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS)));
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Unable to check if a data instance already exists: " + e.getMessage(),
                    e);
        }

        //order the retrieved list by container level
        Collections.sort(dataInstances, new DataInContainersComparator(containerHierarchy));

        //remove duplicates
        Set<String> alreadyUsedNames = new HashSet<String>();
        final Iterator<SDataInstance> it = dataInstances.iterator();
        while (it.hasNext()) {
            SDataInstance current = it.next();
            if (alreadyUsedNames.contains(current.getName())) {
                it.remove();
            } else {
                alreadyUsedNames.add(current.getName());
            }
        }
        return dataInstances;
    }

    @Override
    public SDataInstance getLocalDataInstance(final String dataName, final long containerId, final String containerType)
            throws SDataInstanceReadException {
        final SDataInstance dataInstance = internalGetLocalDataInstance(dataName, containerId, containerType);
        if (dataInstance == null) {
            throw new SDataInstanceReadException("No data instance found");
        }
        return dataInstance;
    }

    private SDataInstance internalGetLocalDataInstance(final String dataName, final long containerId,
            final String containerType)
            throws SDataInstanceReadException {
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);
        final Map<String, Object> paraMap = CollectionUtil.buildSimpleMap(SDataInstance.NAME, dataName);
        paraMap.put(SDataInstance.CONTAINER_ID, containerId);
        paraMap.put(SDataInstance.CONTAINER_TYPE, containerType);

        try {
            return persistenceService
                    .selectOne(new SelectOneDescriptor<SDataInstance>("getDataInstancesByNameAndContainer",
                            paraMap,
                            SDataInstance.class, SDataInstance.class));
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Unable to check if a data instance already exists: " + e.getMessage(),
                    e);
        }
    }

    @Override
    public List<SDataInstance> getLocalDataInstances(final long containerId, final String containerType,
            final int fromIndex, final int numberOfResults)
            throws SDataInstanceReadException {
        NullCheckingUtil.checkArgsNotNull(containerType);
        final Map<String, Object> paraMap = CollectionUtil.buildSimpleMap(SDataInstance.CONTAINER_ID, containerId);
        final OrderByOption orderByOption = new OrderByOption(SDataInstance.class, SDataInstance.ID, OrderByType.ASC);
        paraMap.put(SDataInstance.CONTAINER_TYPE, containerType);

        try {
            return persistenceService.selectList(new SelectListDescriptor<SDataInstance>("getDataInstancesByContainer",
                    paraMap, SDataInstance.class,
                    SDataInstance.class, new QueryOptions(fromIndex, numberOfResults, Arrays.asList(orderByOption))));
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException(
                    "Unable to check if a data instance already exists for the data container of type " + containerType
                            + " with id "
                            + containerId + " for reason: " + e.getMessage(),
                    e);
        }
    }

    private List<SADataInstance> getSADatainstanceOfContainers(long containerId, String containerType,
            ParentContainerResolver parentContainerResolver,
            String queryName, Map<String, Object> inputParameters)
            throws SDataInstanceReadException, SObjectReadException, SObjectNotFoundException, SBonitaReadException {
        //getAllContainers from me to root
        final List<DataContainer> containerHierarchy;
        containerHierarchy = parentContainerResolver
                .getArchivedContainerHierarchy(new DataContainer(containerId, containerType));
        final Map<String, List<Long>> containers = buildContainersMap(containerHierarchy, inputParameters);
        //get all data of any of th possible containers
        List<SADataInstance> dataInstances;
        dataInstances = persistenceService.selectList(new SelectListDescriptor<SADataInstance>(
                getDynamicContainersQueryName(queryName, containers.size()),
                inputParameters, SADataInstance.class, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS)));

        //order the retrieved list by container level and by archive date
        Collections.sort(dataInstances, new ArchivedDataInContainersComparator(containerHierarchy));
        //remove duplicates
        Set<String> alreadyUsedNames = new HashSet<>();
        final Iterator<SADataInstance> it = dataInstances.iterator();
        while (it.hasNext()) {
            SADataInstance current = it.next();
            if (alreadyUsedNames.contains(current.getName())) {
                it.remove();
            } else {
                alreadyUsedNames.add(current.getName());
            }
        }
        return dataInstances;
    }

    private String getDynamicContainersQueryName(String queryName, long nbOfContainers) {
        return queryName + "Of" + nbOfContainers + "Containers";
    }

    @Override
    public SADataInstance getSADataInstance(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver, final String dataName, final long time)
            throws SDataInstanceReadException {
        logBeforeMethod("getSADataInstance");
        final String queryName = "getArchivedDataInstancesWithNames";
        final Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("dataNames", Collections.singletonList(dataName));
        inputParameters.put("time", time);
        final List<SADataInstance> dataInstances;
        try {
            dataInstances = getSADatainstanceOfContainers(containerId, containerType, parentContainerResolver,
                    queryName, inputParameters);
        } catch (SObjectReadException | SObjectNotFoundException | SBonitaReadException e) {
            throw new SDataInstanceReadException(e);
        }
        if (dataInstances.size() == 0) {
            throw new SDataInstanceReadException(
                    "DataInstance with name not found: [name: " + dataName + ", container type: " + containerType
                            + ", container id: " + containerId + ']');
        } else {
            return dataInstances.get(0);
        }
    }

    @Override
    public SADataInstance getSADataInstance(final long sourceObjectId, final long time)
            throws SDataInstanceReadException {
        logBeforeMethod("getSADataInstance");
        try {
            final ReadPersistenceService readPersistenceService = archiveService
                    .getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("dataInstanceId", sourceObjectId);
            parameters.put("time", time);
            final SADataInstance saDataInstance = readPersistenceService
                    .selectOne(new SelectOneDescriptor<SADataInstance>(
                            "getSADataInstanceByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class));
            logAfterMethod("getSADataInstance");
            return saDataInstance;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod("getSADataInstance", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public SADataInstance getLastSADataInstance(final String dataName, final long containerId,
            final String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException {
        logBeforeMethod("getLastSADataInstance");
        SADataInstance saDataInstance = getSADataInstance(containerId, containerType, parentContainerResolver, dataName,
                System.currentTimeMillis());
        if (saDataInstance == null) {
            throw new SDataInstanceNotFoundException("No archived data instance found for data:" + dataName
                    + " in container: " + containerType + " " + containerId);
        }
        return saDataInstance;
    }

    @Override
    public List<SADataInstance> getSADataInstances(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver, final List<String> dataNames, final long time)
            throws SDataInstanceReadException {
        logBeforeMethod("getSADataInstances");
        if (dataNames.isEmpty()) {
            return Collections.emptyList();
        }
        final String queryName = "getArchivedDataInstancesWithNames";
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("time", time);
        inputParameters.put("dataNames", dataNames);
        try {
            return getSADatainstanceOfContainers(containerId, containerType, parentContainerResolver, queryName,
                    inputParameters);
        } catch (SObjectReadException | SObjectNotFoundException | SBonitaReadException e) {
            throw new SDataInstanceReadException(e);
        }
    }

    @Override
    public List<SADataInstance> getLastLocalSADataInstances(final long containerId, final String containerType,
            final int startIndex, final int maxResults)
            throws SDataInstanceReadException {
        logBeforeMethod("getLastLocalSADataInstances");
        try {
            final ReadPersistenceService readPersistenceService = archiveService
                    .getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("containerId", containerId);
            parameters.put("containerType", containerType);
            final List<SADataInstance> saDataInstances = readPersistenceService
                    .selectList(new SelectListDescriptor<SADataInstance>(
                            "getLastLocalSADataInstances", parameters, SADataInstance.class,
                            new QueryOptions(startIndex, maxResults)));
            logAfterMethod("getLastLocalSADataInstances");
            return saDataInstances;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod("getLastLocalSADataInstances", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public long getNumberOfDataInstances(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceReadException {
        logBeforeMethod("getNumberOfDataInstances");
        final List<SDataInstance> dataInstances;
        try {
            dataInstances = getDataInstances(containerId, containerType, parentContainerResolver, 0,
                    QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        } catch (SDataInstanceException e) {
            throw new SDataInstanceReadException(e);
        }
        logAfterMethod("getNumberOfDataInstances");
        return dataInstances.size();
    }

    @Override
    public List<SDataInstance> getDataInstances(final List<String> dataNames, final long containerId,
            final String containerType,
            final ParentContainerResolver parentContainerResolver)
            throws SDataInstanceException {
        logBeforeMethod("getDataInstances");
        NullCheckingUtil.checkArgsNotNull(dataNames, containerType);
        if (dataNames.isEmpty()) {
            return Collections.emptyList();
        }

        final String queryName = "getDataInstancesWithNames";
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("dataNames", dataNames);
        return getSDatainstanceOfContainers(containerId, containerType, parentContainerResolver, queryName,
                inputParameters);
    }

    @Override
    public List<SADataInstance> getLocalSADataInstances(final long containerId, final String containerType,
            final int fromIndex, final int numberOfResults)
            throws SDataInstanceReadException {
        logBeforeMethod("getLocalSADataInstances");
        try {
            final ReadPersistenceService readPersistenceService = archiveService
                    .getDefinitiveArchiveReadPersistenceService();
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put("containerId", containerId);
            parameters.put("containerType", containerType);
            final List<SADataInstance> saDataInstances = readPersistenceService
                    .selectList(new SelectListDescriptor<SADataInstance>("getLocalSADataInstances",
                            parameters, SADataInstance.class, new QueryOptions(fromIndex, numberOfResults)));
            logAfterMethod("getLocalSADataInstances");
            return saDataInstances;
        } catch (final SBonitaReadException e) {
            logOnExceptionMethod("getLocalSADataInstances", e);
            throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        }
    }

    @Override
    public void deleteLocalArchivedDataInstances(final long containerId, final String containerType)
            throws SDataInstanceException {

        HashMap<String, Object> map = new HashMap<>();
        map.put("containerId", containerId);
        map.put("containerType", containerType);
        try {
            archiveService.deleteFromQuery("deleteLocalSADataInstances", map);
        } catch (SRecorderException e) {
            throw new SDataInstanceException(e);
        }
    }

    @Override
    public void deleteLocalArchivedDataInstances(List<Long> containerIds, final String containerType)
            throws SDataInstanceException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("containerIds", containerIds);
        map.put("containerType", containerType);
        try {
            archiveService.deleteFromQuery("deleteLocalSADataInstancesOfContainers", map);
        } catch (SRecorderException e) {
            throw new SDataInstanceException(e);
        }
    }

    @Override
    public void deleteLocalDataInstances(final long containerId, final String dataInstanceContainerType,
            final boolean dataPresent)
            throws SDataInstanceException {
        if (dataPresent) {
            final int deleteBatchSize = 80;
            List<SDataInstance> sDataInstances = getLocalDataInstances(containerId, dataInstanceContainerType, 0,
                    deleteBatchSize);
            while (sDataInstances.size() > 0) {
                for (final SDataInstance sDataInstance : sDataInstances) {
                    deleteDataInstance(sDataInstance);
                }
                sDataInstances = getLocalDataInstances(containerId, dataInstanceContainerType, 0, deleteBatchSize);
            }
        }
    }

    private void logBeforeMethod(final String methodName) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                    LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
    }

    private void logAfterMethod(final String methodName) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                    LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }

    private void logOnExceptionMethod(final String methodName, final Exception e) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                    LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, e));
        }
    }

    @Override
    public void createDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        try {
            recorder.recordInsert(new InsertRecord(dataInstance), DATA_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SCreateDataInstanceException("Impossible to create data instance.", e);
        }
        archiveDataInstance(dataInstance);
    }

    @Override
    public void updateDataInstance(final SDataInstance dataInstance, final EntityUpdateDescriptor descriptor)
            throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(dataInstance, descriptor), DATA_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SUpdateDataInstanceException(
                    "Impossible to update data instance '" + dataInstance.getName() + "': " + e.getMessage(), e);
        }
        archiveDataInstance(dataInstance);
    }

    @Override
    public void deleteDataInstance(final SDataInstance dataInstance) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstance);
        try {
            recorder.recordDelete(new DeleteRecord(dataInstance), DATA_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SDeleteDataInstanceException("Impossible to delete data instance", e);
        }
    }

    @Override
    public SDataInstance getDataInstance(final long dataInstanceId) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataInstanceId);
        try {
            final SelectByIdDescriptor<SDataInstance> selectDescriptor = new SelectByIdDescriptor<SDataInstance>(
                    SDataInstance.class,
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

}
