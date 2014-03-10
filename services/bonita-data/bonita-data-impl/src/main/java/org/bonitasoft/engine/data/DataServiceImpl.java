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
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceLogBuilder;
import org.bonitasoft.engine.data.model.builder.SDataSourceLogBuilderFactory;
import org.bonitasoft.engine.data.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
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
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class DataServiceImpl implements DataService {

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final ClassLoaderService classLoaderService;

    private final List<DataSourceConfiguration> dataSourceConfigurations;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    protected static final String DATA_SOURCE_TYPE = "___datasource___";

    public DataServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final ClassLoaderService classLoaderService,
            final List<DataSourceConfiguration> dataSourceConfigurations, final TechnicalLoggerService logger,
            final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.classLoaderService = classLoaderService;
        this.dataSourceConfigurations = dataSourceConfigurations;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    private SDataSourceLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SDataSourceLogBuilder logBuilder = BuilderFactory.get(SDataSourceLogBuilderFactory.class).createNewInstance();
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

    @Override
    public <T extends DataSourceImplementation> T getDataSourceImplementation(final Class<T> dataSourceType, final long dataSourceId)
            throws SDataSourceNotFoundException, SDataSourceInitializationException, SDataSourceInactiveException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getDataSourceImplementation"));
        }
        final SDataSource dataSource = getDataSource(dataSourceId);

        if (!dataSource.getState().equals(SDataSourceState.ACTIVE)) {
            throw new SDataSourceInactiveException("Unable to retrieve datasource implementation for datasource <" + dataSource
                    + ">, because it is not active <" + dataSource.getState() + ">", dataSource.getState());
        }
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final ClassLoader dataSourceClassloader = classLoaderService.getLocalClassLoader(DATA_SOURCE_TYPE, dataSourceId);
            Thread.currentThread().setContextClassLoader(dataSourceClassloader);

            final Class<T> clazz = (Class<T>) Class.forName(dataSource.getImplementationClassName(), true, dataSourceClassloader);
            final DataSourceImplementation dataSourceImplementation = clazz.newInstance();
            configureDataSourceImplementation(dataSourceImplementation);

            final DataSourceImplementationProxy dataSourceImplementationProxy = new DataSourceImplementationProxy(dataSourceImplementation);
            return (T) Proxy.newProxyInstance(dataSourceClassloader, new Class[] { dataSourceType }, dataSourceImplementationProxy);

        } catch (final SClassLoaderException e) {
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
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "CreateDataSource"));
        }
        final SDataSourceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new datasource.");
        try {
            final InsertRecord insertRecord = new InsertRecord(dataSource);
            final SInsertEvent insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DATASOURCE).setObject(dataSource)
                    .done();
            recorder.recordInsert(insertRecord, insertEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "CreateDataSource"));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_OK, logBuilder, "CreateDataSource");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "CreateDataSource", e));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "CreateDataSource");
            try {
                getDataSource(dataSource.getName(), dataSource.getVersion());
                throw new SDataSourceAlreadyExistException(dataSource.getName(), dataSource.getVersion());
            } catch (final SDataSourceNotFoundException e1) {
                // not because it exists
            }
            throw new SDataException("Can't add datasource " + dataSource + ".", e);
        }
    }

    @Override
    public SDataSource getDataSource(final long dataSourceId) throws SDataSourceNotFoundException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "GetDataSource"));
            }
            final SDataSource dataSource = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SDataSource.class, "DataSource", dataSourceId));
            if (dataSource == null) {
                throw new SDataSourceNotFoundException("can't get the datasource with id " + dataSourceId, null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "GetDataSource"));
            }
            return dataSource;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "GetDataSource", e));
            }
            throw new SDataSourceNotFoundException("can't get the datasource with id " + dataSourceId, e);
        }
    }

    @Override
    public SDataSource getDataSource(final String name, final String version) throws SDataSourceNotFoundException {
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "GetDataSource"));
            }
            final SDataSource dataSource = persistenceService.selectOne(SelectDescriptorBuilder.getDataSource(name, version));
            if (dataSource == null) {
                throw new SDataSourceNotFoundException("Can't get the datasource with name = <" + name + "> and version = <" + version + ">", null);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "GetDataSource"));
            }
            return dataSource;
        } catch (final SBonitaReadException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "GetDataSource", e));
            }
            throw new SDataSourceNotFoundException("Can't get the datasource with name = <" + name + "> and version = <" + version + ">", e);
        }
    }

    @Override
    public void removeDataSource(final SDataSource dataSource) throws SDataSourceNotFoundException {
        final SDataSourceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a DataSource");
        try {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "RemoveDataSource"));
            }
            final DeleteRecord deleteRecord = new DeleteRecord(dataSource);
            final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DATASOURCE).setObject(dataSource)
                    .done();
            recorder.recordDelete(deleteRecord, deleteEvent);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "RemoveDataSource"));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_OK, logBuilder, "RemoveDataSource");
        } catch (final SRecorderException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "RemoveDataSource", e));
            }
            initiateLogBuilder(dataSource.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "RemoveDataSource");
            throw new SDataSourceNotFoundException("can't delete datasource " + dataSource, e);
        }
    }

    @Override
    public void removeDataSource(final long dataSourceId) throws SDataSourceNotFoundException {
        removeDataSource(getDataSource(dataSourceId));
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
