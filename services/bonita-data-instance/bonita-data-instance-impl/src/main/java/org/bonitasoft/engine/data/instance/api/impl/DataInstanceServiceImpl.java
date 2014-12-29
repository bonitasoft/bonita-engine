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

import java.util.*;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.CollectionUtil;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
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
import org.bonitasoft.engine.data.instance.model.archive.builder.SADataInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
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
    public SDataInstance getDataInstance(final String dataName, final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);

        final String queryName = "getDataInstanceWithNameOfContainers";
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("dataName", dataName);
        final List<SDataInstance> dataInstances = getSDatainstanceOfContainers(containerId, containerType, parentContainerResolver, queryName, inputParameters);
        if (dataInstances.size() == 0) {
            final StringBuilder stb = new StringBuilder("DataInstance with name not found: [name: ");
            stb.append(dataName).append(", container type: ").append(containerType);
            stb.append(", container id: ").append(containerId).append(']');
            throw new SDataInstanceNotFoundException(stb.toString());
        } else if (dataInstances.size() > 1) {
            //should never happen but in case...
            final StringBuilder stb = new StringBuilder("Several data have been retrieved for: [name: ");
            stb.append(dataName).append(", container type: ").append(containerType);
            stb.append(", container id: ").append(containerId).append(']');
            throw new SDataInstanceReadException(stb.toString());
        } else {
            return dataInstances.get(0);
        }
    }

    @Override
    public List<SDataInstance> getDataInstances(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver, final int fromIndex, final int numberOfResults)
            throws SDataInstanceException {
        NullCheckingUtil.checkArgsNotNull(containerType);
        final String queryName = "getDataInstancesOfContainers";
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        final List<SDataInstance> dataInstances = getSDatainstanceOfContainers(containerId, containerType, parentContainerResolver, queryName, inputParameters);

        //apply pagination
        final int startIndex = Math.max(0, fromIndex);
        final int toIndex = Math.min(dataInstances.size(), fromIndex + numberOfResults);

        return dataInstances.subList(startIndex, toIndex);
    }

    private List<SDataInstance> getSDatainstanceOfContainers(long containerId, String containerType, ParentContainerResolver parentContainerResolver, String queryName, Map<String, Object> inputParameters) throws SDataInstanceNotFoundException, SDataInstanceReadException {
        //getallContainers from me to root
        final List<Pair<Long, String>> containerHierarchy;
        try {
            containerHierarchy = parentContainerResolver.getContainerHierarchy(new Pair<Long, String>(containerId, containerType));
        } catch (SObjectNotFoundException e) {
            throw new SDataInstanceNotFoundException(e);
        } catch (SObjectReadException e) {
            throw new SDataInstanceNotFoundException(e);
        }

        final List<Long> activityInstanceContainerIds = new ArrayList<Long>();
        final List<Long> processInstanceContainerIds = new ArrayList<Long>();
        final List<Long> messageInstanceContainerIds = new ArrayList<Long>();

        for (Pair<Long, String> container : containerHierarchy) {
            if (container.getRight().equals(DataInstanceContainer.ACTIVITY_INSTANCE.name())) {
                activityInstanceContainerIds.add(container.getLeft());
            } else if (container.getRight().equals(DataInstanceContainer.PROCESS_INSTANCE.name())) {
                processInstanceContainerIds.add(container.getLeft());
            } else if (container.getRight().equals(DataInstanceContainer.MESSAGE_INSTANCE.name())) {
                messageInstanceContainerIds.add(container.getLeft());
            }
        }

        //select all data instance about those containers
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        inputParameters.put("activityInstanceContainerIds", activityInstanceContainerIds);
        inputParameters.put("processInstanceContainerIds", processInstanceContainerIds);
        inputParameters.put("messageInstanceContainerIds", messageInstanceContainerIds);

        //gte all data of any of th possible containers
        List<SDataInstance> dataInstances = null;
        try {
            dataInstances = persistenceService.selectList(new SelectListDescriptor<SDataInstance>(queryName, inputParameters, SDataInstance.class, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS)));
        } catch (final SBonitaReadException e) {
            throw new SDataInstanceReadException("Unable to check if a data instance already exists: " + e.getMessage(), e);
        }

        //order the retrieved list by container level
        Collections.sort(dataInstances, new Comparator<SDataInstance>() {
            @Override
            public int compare(SDataInstance o1, SDataInstance o2) {
                final Pair<Long, String> o1Container = new Pair<Long, String>(o1.getContainerId(), o1.getContainerType());
                final Pair<Long, String> o2Container = new Pair<Long, String>(o2.getContainerId(), o2.getContainerType());
                return containerHierarchy.indexOf(o1Container) - containerHierarchy.indexOf(o2Container);
            }
        });

        //remove duplicates
        SDataInstance previous = null;
        final Iterator<SDataInstance> it = dataInstances.iterator();
        while (it.hasNext()) {
            SDataInstance current = it.next();
            if (previous != null && previous.getName().equals(current.getName())) {
                it.remove();
            }
        }
        return dataInstances;
    }

    @Override
    public SDataInstance getLocalDataInstance(final String dataName, final long containerId, final String containerType) throws SDataInstanceReadException {
        final SDataInstance dataInstance = internalGetLocalDataInstance(dataName, containerId, containerType);
        if (dataInstance == null) {
            throw new SDataInstanceReadException("No data instance found");
        }
        return dataInstance;
    }

    private SDataInstance internalGetLocalDataInstance(final String dataName, final long containerId, final String containerType)
            throws SDataInstanceReadException {
        NullCheckingUtil.checkArgsNotNull(dataName, containerType);
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        final Map<String, Object> paraMap = CollectionUtil.buildSimpleMap(fact.getNameKey(), dataName);
        paraMap.put(fact.getContainerIdKey(), containerId);
        paraMap.put(fact.getContainerTypeKey(), containerType);

        try {
            final SDataInstance dataInstance = persistenceService.selectOne(new SelectOneDescriptor<SDataInstance>("getDataInstancesByNameAndContainer",
                    paraMap,
                    SDataInstance.class, SDataInstance.class)); // conditions :and not or
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
    public SADataInstance getSADataInstance(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver, final String dataName, final long time)
            throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getSADataInstance");
        // try {
        // final long dataInstanceId = getSADataInstanceDataVisibilityMapping(dataName, containerId, containerType);
        // final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        // final Map<String, Object> parameters = new HashMap<String, Object>(2);
        // parameters.put("dataInstanceId", dataInstanceId);
        // parameters.put("time", time);
        // final SADataInstance saDataInstance = readPersistenceService.selectOne(new SelectOneDescriptor<SADataInstance>(
        // "getSADataInstanceByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class));
        // logAfterMethod(TechnicalLogSeverity.TRACE, "getSADataInstance");
        // return saDataInstance;
        // } catch (final SBonitaReadException e) {
        // logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getSADataInstance", e);
        // throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        // }
        return null;
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
    public SADataInstance getLastSADataInstance(final String dataName, final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceException {
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
    public long getNumberOfDataInstances(final long containerId, final DataInstanceContainer containerType,
            final ParentContainerResolver parentContainerResolver) throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getNumberOfDataInstances");
        final List<SDataInstance> dataInstances;
        try {
            dataInstances = getDataInstances(containerId, containerType.name(), parentContainerResolver, 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
        } catch (SDataInstanceException e) {
            throw new SDataInstanceReadException(e);
        }
        logAfterMethod(TechnicalLogSeverity.TRACE, "getNumberOfDataInstances");
        return dataInstances.size();
    }

    @Override
    public List<SDataInstance> getDataInstances(final List<String> dataNames, final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver)
            throws SDataInstanceException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getDataInstances");
        NullCheckingUtil.checkArgsNotNull(dataNames, containerType);
        if (dataNames.isEmpty()) {
            return Collections.emptyList();
        }
        // try {
        // final List<Long> dataInstanceIds = getDataInstanceDataVisibilityMapping(dataNames, containerId, containerType);
        // final ArrayList<SDataInstance> finalResult = new ArrayList<SDataInstance>(dataNames.size());
        // finalResult.addAll(getDataInstances(dataInstanceIds));
        // logAfterMethod(TechnicalLogSeverity.TRACE, "getDataInstances");
        // return finalResult;
        // } catch (final SBonitaReadException e) {
        // logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getDataInstances", e);
        // throw new SDataInstanceReadException("Unable to find the data in the data mapping with name = " + dataNames + ", containerId = " + containerId
        // + ", containerType = " + containerType, e);
        // }
        final ArrayList<SDataInstance> finalResult = new ArrayList<SDataInstance>(dataNames.size());
        for (String dataName : dataNames) {
            finalResult.add(getDataInstance(dataName, containerId, containerType, parentContainerResolver));
        }
        return finalResult;
    }

    @Override
    public List<SADataInstance> getSADataInstances(final long containerId, final String containerType,
            final ParentContainerResolver parentContainerResolver, final List<String> dataNames, final long time)
            throws SDataInstanceReadException {
        logBeforeMethod(TechnicalLogSeverity.TRACE, "getSADataInstances");
        if (dataNames.isEmpty()) {
            return Collections.emptyList();
        }
        // try {
        // final List<Long> dataInstanceIds = getSADataInstanceDataVisibilityMapping(dataNames, containerId, containerType);
        // final ReadPersistenceService readPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        // final Map<String, Object> parameters = new HashMap<String, Object>(2);
        // parameters.put("dataInstanceIds", dataInstanceIds);
        // parameters.put("time", time);
        // final List<SADataInstance> listSADataInstance = readPersistenceService.selectList(new SelectListDescriptor<SADataInstance>(
        // "getSADataInstancesByDataInstanceIdAndArchiveDate", parameters, SADataInstance.class, new QueryOptions(0, dataInstanceIds.size())));
        // logAfterMethod(TechnicalLogSeverity.TRACE, "getSADataInstances");
        // return listSADataInstance;
        // } catch (final SBonitaReadException e) {
        // logOnExceptionMethod(TechnicalLogSeverity.TRACE, "getSADataInstances", e);
        // throw new SDataInstanceReadException("Unable to read SADataInstance", e);
        // }
        return Collections.emptyList();
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
