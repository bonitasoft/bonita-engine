/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.data;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceParameter;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceLogBuilder;
import org.bonitasoft.engine.data.model.builder.SDataSourceModelBuilder;
import org.bonitasoft.engine.data.model.builder.SDataSourceParameterLogBuilder;
import org.bonitasoft.engine.data.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class DataServiceImpl implements DataService {

    private final SDataSourceModelBuilder logModelBuilder;

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final ClassLoaderService classLoaderService;

    private final EventService eventService;

    private final List<DataSourceConfiguration> dataSourceConfigurations;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    protected static final String DATA_SOURCE_TYPE = "___datasource___";

    public DataServiceImpl(final SDataSourceModelBuilder modelBuilder, final Recorder recorder, final ReadPersistenceService persistenceService,
            final ClassLoaderService classLoaderService, final EventService eventService, final List<DataSourceConfiguration> dataSourceConfigurations,
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        logModelBuilder = modelBuilder;
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.classLoaderService = classLoaderService;
        this.eventService = eventService;
        this.dataSourceConfigurations = dataSourceConfigurations;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    private SDataSourceLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SDataSourceLogBuilder logBuilder = logModelBuilder.getDataSourceLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SDataSourceParameterLogBuilder getQueriableLog(final ActionType actionType, final String message, final SDataSourceParameter dataSourceParameter) {
        final SDataSourceParameterLogBuilder logBuilder = logModelBuilder.getDataSourceParameterLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.dataSourceId(dataSourceParameter.getDataSourceId());
        return logBuilder;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    @Override
    public <T extends DataSourceImplementation> T getDataSourceImplementation(final Class<T> dataSourceType, final long dataSourceId)
            throws SDataSourceNotFoundException, SDataSourceInitializationException, SDataSourceInactiveException, SDataException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSourceImplementation"));
        }
        final SDataSource dataSource = getDataSource(dataSourceId);

        if (!dataSource.getState().equals(SDataSourceState.ACTIVE)) {
            throw new SDataSourceInactiveException("Unable to retrieve datasource implementation for datasource: " + dataSource + " because it is not active: "
                    + dataSource.getState(), dataSource.getState());
        }
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final ClassLoader dataSourceClassloader = classLoaderService.getLocalClassLoader(DATA_SOURCE_TYPE, dataSourceId);
            Thread.currentThread().setContextClassLoader(dataSourceClassloader);

            final Class<T> clazz = (Class<T>) Class.forName(dataSource.getImplementationClassName(), true, dataSourceClassloader);
            final DataSourceImplementation dataSourceImplementation = clazz.newInstance();
            configureDataSourceImplementation(dataSourceImplementation);
            final Map<String, String> parameters = new HashMap<String, String>();
            final QueryOptions queryOptions = QueryOptions.allResultsQueryOptions();
            final Collection<SDataSourceParameter> dataSourceParametersFromDb = getDataSourceParameters(dataSourceId, queryOptions);
            final List<SDataSourceParameter> dataSourceParameters = new ArrayList<SDataSourceParameter>(dataSourceParametersFromDb.size());
            for (final SDataSourceParameter sDataSourceParameter : dataSourceParametersFromDb) {
                dataSourceParameters.add(sDataSourceParameter);
            }
            for (final SDataSourceParameter dataSourceParameter : dataSourceParameters) {
                parameters.put(dataSourceParameter.getName(), dataSourceParameter.getValue_());
            }
            dataSourceImplementation.setParameters(parameters);

            final DataSourceImplementationProxy dataSourceImplementationProxy = new DataSourceImplementationProxy(dataSourceImplementation);
            return (T) Proxy.newProxyInstance(dataSourceClassloader, new Class[] { dataSourceType }, dataSourceImplementationProxy);

        } catch (final ClassLoaderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceImplementation", e));
            }
            throw new SDataSourceNotFoundException("Unable to find the data source classloader", e);
        } catch (final ClassNotFoundException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceImplementation", e));
            }
            throw new SDataSourceInitializationException("Unable to find data source implementation class: " + dataSource.getImplementationClassName(), e);
        } catch (final InstantiationException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceImplementation", e));
            }
            throw new SDataSourceInitializationException("Unable to create data source implementation instance of class: "
                    + dataSource.getImplementationClassName(), e);
        } catch (final IllegalAccessException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceImplementation", e));
            }
            throw new SDataSourceInitializationException("Unable to create data source implementation instance of class: "
                    + dataSource.getImplementationClassName(), e);
        } finally {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataSourceImplementation"));
            }
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private void configureDataSourceImplementation(final DataSourceImplementation dataSourceImplementation) throws SDataSourceInitializationException {
        if (dataSourceConfigurations != null) {
            for (final DataSourceConfiguration datasourceConfiguration : dataSourceConfigurations) {
                if (dataSourceImplementation.configurationMatches(datasourceConfiguration)) {
                    dataSourceImplementation.configure(datasourceConfiguration);
                }
            }
        }
    }

    @Override
    public void createDataSource(final SDataSource dataSource) throws SDataSourceAlreadyExistException, SDataException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "createDataSource"));
        }
        final SDataSourceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new datasource");
        try {
            final InsertRecord insertRecord = new InsertRecord(dataSource);
            final SInsertEvent insertEvent = (SInsertEvent) eventService.getEventBuilder().createInsertEvent(DATASOURCE).setObject(dataSource).done();
            recorder.recordInsert(insertRecord, insertEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createDataSource"));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_OK, logBuilder, "createDataSource");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createDataSource", e));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createDataSource");
            try {
                getDataSource(dataSource.getName(), dataSource.getVersion());
                throw new SDataSourceAlreadyExistException(dataSource.getName(), dataSource.getVersion());
            } catch (final SDataSourceNotFoundException e1) {
                // not because it exists
            }
            throw new SDataException("can't add datasource " + dataSource, e);
        }
    }

    @Override
    public void createDataSourceParameter(final SDataSourceParameter dataSourceParameter) throws SDataSourceParameterAlreadyExistException, SDataException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "createDataSourceParameter"));
        }

        final SDataSourceParameterLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new datasourceparameter", dataSourceParameter);
        try {
            final InsertRecord insertRecord = new InsertRecord(dataSourceParameter);
            final SInsertEvent insertEvent = (SInsertEvent) eventService.getEventBuilder().createInsertEvent(DATASOURCEPARAMETER)
                    .setObject(dataSourceParameter).done();
            recorder.recordInsert(insertRecord, insertEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createDataSourceParameter"));
            }
            initiateLogBuilder(dataSourceParameter.getId(), SQueriableLog.STATUS_OK, logBuilder, "createDataSourceParameter");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "createDataSourceParameter", e));
            }
            initiateLogBuilder(dataSourceParameter.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createDataSourceParameter");
            try {
                getDataSourceParameter(dataSourceParameter.getName(), dataSourceParameter.getDataSourceId());
                throw new SDataSourceParameterAlreadyExistException(dataSourceParameter.getName(), dataSourceParameter.getDataSourceId());
            } catch (final SDataSourceParameterNotFoundException e1) {
                // not because it already exists
            }
            throw new SDataException("can't add datasourceparameter " + dataSourceParameter, e);
        }
    }

    @Override
    public void createDataSourceParameters(final Collection<SDataSourceParameter> parameters) throws SDataSourceParameterAlreadyExistException, SDataException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "createDataSourceParameters"));
        }
        for (final SDataSourceParameter parameter : parameters) {
            createDataSourceParameter(parameter);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createDataSourceParameters"));
        }
    }

    @Override
    public SDataSource getDataSource(final long dataSourceId) throws SDataSourceNotFoundException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSource"));
            }
            final SDataSource dataSource = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SDataSource.class, "DataSource", dataSourceId));
            if (dataSource == null) {
                throw new SDataSourceNotFoundException("can't get the datasource with id " + dataSourceId, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataSource"));
            }
            return dataSource;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSource", e));
            }
            throw new SDataSourceNotFoundException("can't get the datasource with id " + dataSourceId, e);
        }
    }

    @Override
    public SDataSource getDataSource(final String name, final String version) throws SDataSourceNotFoundException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSource"));
            }
            final SDataSource dataSource = persistenceService.selectOne(SelectDescriptorBuilder.getDataSource(name, version));
            if (dataSource == null) {
                throw new SDataSourceNotFoundException("can't get the datasource with name: " + name + " and version: " + version, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataSource"));
            }
            return dataSource;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSource", e));
            }
            throw new SDataSourceNotFoundException("can't get the datasource with name: " + name + " and version: " + version, e);
        }
    }

    @Override
    public SDataSourceParameter getDataSourceParameter(final long dataSourceParameterId) throws SDataSourceParameterNotFoundException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSourceParameter"));
            }
            final SDataSourceParameter dataSourceParameter = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SDataSourceParameter.class,
                    "DataSourceParameter", dataSourceParameterId));
            if (dataSourceParameter == null) {
                throw new SDataSourceParameterNotFoundException("can't get the datasourceparameter with id " + dataSourceParameterId, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataSourceParameter"));
            }
            return dataSourceParameter;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceParameter", e));
            }
            throw new SDataSourceParameterNotFoundException("can't get the datasourceparameter with id " + dataSourceParameterId, e);
        }
    }

    @Override
    public SDataSourceParameter getDataSourceParameter(final String name, final long dataSourceId) throws SDataSourceParameterNotFoundException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSourceParameter"));
            }
            final SDataSourceParameter dataSourceParameter = persistenceService.selectOne(SelectDescriptorBuilder.getDataSourceParameter(name, dataSourceId));
            if (dataSourceParameter == null) {
                throw new SDataSourceParameterNotFoundException("can't get the datasourceparameter with name: " + name + " and dataSourceId: " + dataSourceId,
                        null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataSourceParameter"));
            }
            return dataSourceParameter;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceParameter", e));
            }
            throw new SDataSourceParameterNotFoundException("can't get the datasourceparameter with name: " + name + " and dataSourceId: " + dataSourceId, e);
        }
    }

    @Override
    public Collection<SDataSourceParameter> getDataSourceParameters(final long dataSourceId, final QueryOptions queryOptions) throws SDataException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSourceParameters"));
            }
            final Collection<SDataSourceParameter> collectionSDataSourceParameter = persistenceService.selectList(SelectDescriptorBuilder.getDataSourceParameters(
                    dataSourceId, queryOptions));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataSourceParameters"));
            }
            return collectionSDataSourceParameter;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceParameters", e));
            }
            throw new SDataException("can't get the datasourceparameters with dataSourceId: " + dataSourceId, e);
        }
    }

    @Override
    public Collection<SDataSource> getDataSources(final QueryOptions queryOptions) throws SDataException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSources"));
            }
            final Collection<SDataSource> collectionSDataSource = persistenceService.selectList(SelectDescriptorBuilder.getDataSources(queryOptions));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getDataSources"));
            }
            return collectionSDataSource;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "getDataSourceParameters", e));
            }
            throw new SDataException("can't get the datasources", e);
        }
    }

    @Override
    public void removeDataSource(final SDataSource dataSource) throws SDataSourceNotFoundException {
        final SDataSourceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a DataSource");
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeDataSource"));
            }
            final DeleteRecord deleteRecord = new DeleteRecord(dataSource);
            final SDeleteEvent deleteEvent = (SDeleteEvent) eventService.getEventBuilder().createDeleteEvent(DATASOURCE).setObject(dataSource).done();
            recorder.recordDelete(deleteRecord, deleteEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeDataSource"));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_OK, logBuilder, "removeDataSource");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "removeDataSource", e));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "removeDataSource");
            throw new SDataSourceNotFoundException("can't delete datasource " + dataSource, e);
        }
    }

    @Override
    public void removeDataSource(final long dataSourceId) throws SDataSourceNotFoundException {
        removeDataSource(getDataSource(dataSourceId));
    }

    @Override
    public void removeDataSourceParameter(final long dataSourceParameterId) throws SDataSourceParameterNotFoundException {
        removeDataSourceParameter(getDataSourceParameter(dataSourceParameterId));
    }

    @Override
    public void removeDataSourceParameter(final SDataSourceParameter dataSourceParameter) throws SDataSourceParameterNotFoundException {
        final SDataSourceParameterLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a DataSourceParameter", dataSourceParameter);
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeDataSourceParameter"));
            }
            final DeleteRecord deleteRecord = new DeleteRecord(dataSourceParameter);
            final SDeleteEvent deleteEvent = (SDeleteEvent) eventService.getEventBuilder().createDeleteEvent(DATASOURCEPARAMETER)
                    .setObject(dataSourceParameter).done();
            recorder.recordDelete(deleteRecord, deleteEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeDataSourceParameter"));
            }
            initiateLogBuilder(dataSourceParameter.getId(), SQueriableLog.STATUS_OK, logBuilder, "removeDataSourceParameter");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "removeDataSourceParameter", e));
            }
            initiateLogBuilder(dataSourceParameter.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "removeDataSourceParameter");
            throw new SDataSourceParameterNotFoundException("can't delete datasourceparameter " + dataSourceParameter, e);
        }
    }

    @Override
    public void removeDataSourceParameters(final long dataSourceId) throws SDataException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeDataSourceParameters"));
        }
        final QueryOptions queryOptions = QueryOptions.defaultQueryOptions();
        Collection<SDataSourceParameter> list;
        do {
            list = getDataSourceParameters(dataSourceId, queryOptions);
            for (final SDataSourceParameter dataSourceParameter : list) {
                try {
                    removeDataSourceParameter(dataSourceParameter);
                } catch (final SDataSourceParameterNotFoundException e) {
                    throw new SDataException("DataSourceParameterNotFoundException: " + dataSourceParameter, e);
                }
            }
        } while (list.size() == queryOptions.getNumberOfResults());
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeDataSourceParameters"));
        }
    }

    @Override
    public void removeDataSourceParameters(final Collection<Long> dataSourceParameterIds) throws SDataSourceParameterNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "removeDataSourceParameters"));
        }
        for (final Long dataSourceParameterId : dataSourceParameterIds) {
            removeDataSourceParameter(dataSourceParameterId);
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "removeDataSourceParameters"));
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

}
