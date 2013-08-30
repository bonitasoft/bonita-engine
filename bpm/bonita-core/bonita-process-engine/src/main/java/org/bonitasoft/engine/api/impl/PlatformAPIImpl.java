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
package org.bonitasoft.engine.api.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.platform.ActivateTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.CleanPlatformTableContent;
import org.bonitasoft.engine.api.impl.transaction.platform.DeactivateTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteAllTenants;
import org.bonitasoft.engine.api.impl.transaction.platform.DeletePlatformContent;
import org.bonitasoft.engine.api.impl.transaction.platform.DeletePlatformTableContent;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenantObjects;
import org.bonitasoft.engine.api.impl.transaction.platform.GetDefaultTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.platform.GetPlatformContent;
import org.bonitasoft.engine.api.impl.transaction.platform.IsPlatformCreated;
import org.bonitasoft.engine.api.impl.transaction.platform.RefreshPlatformClassLoader;
import org.bonitasoft.engine.api.impl.transaction.platform.RefreshTenantClassLoaders;
import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.DefaultCommandProvider;
import org.bonitasoft.engine.command.SCommandAlreadyExistsException;
import org.bonitasoft.engine.command.SCommandCreationException;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandBuilder;
import org.bonitasoft.engine.commons.IOUtil;
import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.data.DataService;
import org.bonitasoft.engine.data.SDataException;
import org.bonitasoft.engine.data.SDataSourceAlreadyExistException;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.data.model.SDataSourceState;
import org.bonitasoft.engine.data.model.builder.SDataSourceModelBuilder;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.events.model.FireEventException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.STenantActivationException;
import org.bonitasoft.engine.platform.STenantCreationException;
import org.bonitasoft.engine.platform.STenantDeactivationException;
import org.bonitasoft.engine.platform.STenantDeletionException;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.restart.TenantRestartHandler;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Lu Kai
 * @author Zhang Bole
 * @author Yanyan Liu
 * @author Emmanuel Duchastenier
 */
public class PlatformAPIImpl implements PlatformAPI {

    private static final String STATUS_DEACTIVATED = "DEACTIVATED";

    @Override
    @CustomTransactions
    public void createPlatform() throws CreationException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new CreationException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionService transactionService = platformAccessor.getTransactionService();
        try {
            final SPlatform platform = constructPlatform(platformAccessor);
            platformService.createPlatformTables();
            platformService.createTenantTables();

            transactionService.begin();
            try {
                platformService.initializePlatformStructure();
            } finally {
                transactionService.complete();
            }
            transactionService.begin();
            try {
                platformService.createPlatform(platform);
                platformService.getPlatform();
            } finally {
                transactionService.complete();
            }
        } catch (final SBonitaException e) {
            throw new CreationException("Platform Creation failed.", e);
        } catch (final IOException ioe) {
            throw new CreationException("Platform Creation failed.", ioe);
        }
    }

    @Override
    @CustomTransactions
    public void initializePlatform() throws CreationException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new CreationException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionService transactionService = platformAccessor.getTransactionService();
        final TechnicalLoggerService technicalLoggerService = platformAccessor.getTechnicalLoggerService();
        // 1 tx to create content and default tenant
        try {
            transactionService.begin();
            try {
                // inside new tx because we need sequence ids
                createDefaultTenant(platformAccessor, platformService, transactionService);
                activateDefaultTenant();
            } catch (final SBonitaException e) {
                if (technicalLoggerService.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                    technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.WARNING, e);
                }
                throw new CreationException("Platform initialisation failed.", e);
            } finally {
                transactionService.complete();
            }
        } catch (final STransactionException e1) {
            throw new CreationException(e1);
        }
    }

    @Override
    @CustomTransactions
    public void createAndInitializePlatform() throws CreationException {
        createPlatform();
        initializePlatform();
    }

    protected PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    private SPlatform constructPlatform(final PlatformServiceAccessor platformAccessor) throws IOException {
        final URL resource = PlatformAPIImpl.class.getResource("platform.properties");
        final Properties properties = PropertiesManager.getProperties(resource);
        // FIXME construct platform object from a configuration file
        final String version = (String) properties.get("version");
        final String previousVersion = "";
        final String initialVersion = (String) properties.get("version");
        // FIXME createdBy when PlatformSessionAccessor will exist
        final String createdBy = "platformAdmin";
        // FIXME do that in the builder
        final long created = System.currentTimeMillis();
        final SPlatformBuilder platformBuilder = platformAccessor.getSPlatformBuilder();
        return platformBuilder.createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
    }

    private boolean isPlatformStarted(final PlatformServiceAccessor platformAccessor) {
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        try {
            return schedulerService.isStarted();
        } catch (final SSchedulerException e) {
            log(platformAccessor, e);
            return false;
        }
    }

    @Override
    @CustomTransactions
    public void startNode() throws StartNodeException {
        final PlatformServiceAccessor platformAccessor;
        SessionAccessor sessionAccessor = null;
        try {
            platformAccessor = getPlatformAccessor();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (final Exception e) {
            throw new StartNodeException(e);
        }
        final NodeConfiguration platformConfiguration = platformAccessor.getPlaformConfiguration();
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        WorkService workService = platformAccessor.getWorkService();
        try {
            try {
                final TransactionExecutor executor = platformAccessor.getTransactionExecutor();
                final RefreshPlatformClassLoader refreshPlatformClassLoader = new RefreshPlatformClassLoader(platformAccessor);
                executor.execute(refreshPlatformClassLoader);
                final List<Long> tenantIds = refreshPlatformClassLoader.getResult();

                // set tenant classloader
                final SessionService sessionService = platformAccessor.getSessionService();
                for (final Long tenantId : tenantIds) {
                    long sessionId = -1;
                    long platformSessionId = -1;
                    try {
                    	platformSessionId = sessionAccessor.getSessionId();
                        sessionAccessor.deleteSessionId();
                        sessionId = createSessionAndMakeItActive(tenantId, sessionAccessor, sessionService);
                        final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
                        final TransactionExecutor tenantExecutor = tenantServiceAccessor.getTransactionExecutor();
                        tenantExecutor.execute(new RefreshTenantClassLoaders(tenantServiceAccessor, tenantId));
                    } finally {
                    	sessionService.deleteSession(sessionId);
                    	cleanSessionAccessor(sessionAccessor);
                        sessionAccessor.setSessionInfo(platformSessionId, -1);
                    }
                }
                workService.startup();
                if (!isPlatformStarted(platformAccessor)) {
                    if (platformConfiguration.shouldStartScheduler()) {
                        schedulerService.start();
                    }
                    if (platformConfiguration.shouldResumeElements()) {
                        // Here get all elements that are not "finished"
                        // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
                        // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
                        // * transitions that are in state created: call execute on them
                        // * flow node that are completed and not deleted : call execute to make it create transitions and so on
                        // * all element that are in not stable state

                        final PlatformService platformService = platformAccessor.getPlatformService();
                        final GetDefaultTenantInstance getDefaultTenantInstance = new GetDefaultTenantInstance(platformService);
                        platformAccessor.getTransactionExecutor().execute(getDefaultTenantInstance);
                        final STenant defaultTenant = getDefaultTenantInstance.getResult();
                        final long tenantId = defaultTenant.getId();
                        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor(tenantId);
                        final long sessionId = createSessionAndMakeItActive(defaultTenant.getId(), sessionAccessor, sessionService);
                        for (final TenantRestartHandler restartHandler : platformConfiguration.getTenantRestartHandlers()) {
                            Callable<Void> callable = new Callable<Void>() {

                                @Override
                                public Void call() throws Exception {
                                    restartHandler.handleRestart(platformAccessor, tenantServiceAccessor);
                                    return null;
                                }
                            };
                            tenantServiceAccessor.getTransactionService().executeInTransaction(callable);
                        }
                        sessionService.deleteSession(sessionId);
                    }
                    for (final RestartHandler restartHandler : platformConfiguration.getRestartHandlers()) {
                        restartHandler.execute();
                    }
                }
            } catch (final ClassLoaderException e) {
                throw new StartNodeException("Platform starting failed while initializing platform classloaders.", e);
            } catch (final SDependencyException e) {
                throw new StartNodeException("Platform starting failed while initializing platform classloaders.", e);
            } catch (final SBonitaException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final BonitaHomeNotSetException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final BonitaHomeConfigurationException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final IOException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final NoSuchMethodException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final InstantiationException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final IllegalAccessException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final InvocationTargetException e) {
                throw new StartNodeException("Platform starting failed.", e);
            } catch (final Exception e) {
                throw new StartNodeException("Platform starting failed.", e);
            } finally {
                cleanSessionAccessor(sessionAccessor);
            }
        } catch (final StartNodeException e) {
            // If an exception is thrown, stop the platform that was started.
            try {
                shutdownScheduler(platformAccessor, schedulerService);
            } catch (final SBonitaException exp) {
                throw new StartNodeException("Platform stoping failed : " + exp.getMessage(), e);
            }
            throw e;
        }
    }

    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return ServiceAccessorFactory.getInstance().createTenantServiceAccessor(tenantId);
    }

    @Override
    @CustomTransactions
    public void stopNode() throws StopNodeException {
        try {
            final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            final SchedulerService schedulerService = platformAccessor.getSchedulerService();
            NodeConfiguration plaformConfiguration = platformAccessor.getPlaformConfiguration();
            if (plaformConfiguration.shouldStartScheduler()) {
                // we shutdown the scheduler only if we are also responsible of starting it
                shutdownScheduler(platformAccessor, schedulerService);
            }
            WorkService workService = platformAccessor.getWorkService();
            workService.shutdown();
            if (plaformConfiguration.shouldClearSessions()) {
                platformAccessor.getSessionService().deleteSessions();
            }
        } catch (final SBonitaException e) {
            throw new StopNodeException(e);
        } catch (final BonitaHomeNotSetException e) {
            throw new StopNodeException(e);
        } catch (final InstantiationException e) {
            throw new StopNodeException(e);
        } catch (final IllegalAccessException e) {
            throw new StopNodeException(e);
        } catch (final ClassNotFoundException e) {
            throw new StopNodeException(e);
        } catch (final IOException e) {
            throw new StopNodeException(e);
        } catch (final BonitaHomeConfigurationException e) {
            throw new StopNodeException(e.getMessage());
        } catch (final Exception e) {
            throw new StopNodeException(e);
        }
    }

    private void shutdownScheduler(final PlatformServiceAccessor platformAccessor, final SchedulerService schedulerService) throws SSchedulerException,
            FireEventException {
        if (isPlatformStarted(platformAccessor)) {
            schedulerService.shutdown();
        }
    }

    @Override
    @CustomTransactions
    public void cleanPlatform() throws DeletionException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new DeletionException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionService transactionService = platformAccessor.getTransactionService();
        final CleanPlatformTableContent clean = new CleanPlatformTableContent(platformService);
        final DeleteAllTenants deleteAll = new DeleteAllTenants(platformService);
        try {
            transactionService.executeInTransaction(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    try {
                        final STenant tenant = getDefaultTenant();
                        deactiveTenant(tenant.getId());
                    } catch (STenantNotFoundException e) {

                    }
                    clean.execute();
                    deleteAll.execute();

                    return null;
                }
            });
        } catch (Exception e) {
            throw new DeletionException(e);
        }
    }

    @Override
    @CustomTransactions
    public void deletePlatform() throws DeletionException {
        // TODO : Reduce number of transactions
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new DeletionException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionExecutor transactionExecutor = platformAccessor.getTransactionExecutor();
        final TransactionContent deletePlatformContent = new DeletePlatformContent(platformService);
        try {
            final TransactionContent deleteTenantTables = new TransactionContent() {

                @Override
                public void execute() throws SBonitaException {
                    platformService.deleteTenantTables();
                }
            };
            transactionExecutor.execute(deletePlatformContent);
            transactionExecutor.execute(deleteTenantTables);
            final TransactionContent deletePlatformTableContent = new DeletePlatformTableContent(platformService);
            transactionExecutor.execute(deletePlatformTableContent);
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    @CustomTransactions
    public void cleanAndDeletePlaftorm() throws DeletionException {
        cleanPlatform();
        deletePlatform();
    }

    @Override
    public Platform getPlatform() throws PlatformNotFoundException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new PlatformNotFoundException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        final GetPlatformContent transactionContent = new GetPlatformContent(platformService);
        try {
            transactionContent.execute();
        } catch (final SBonitaException e) {
            throw new PlatformNotFoundException(e);
        }
        final SPlatform sPlatform = transactionContent.getResult();
        return ModelConvertor.toPlatform(sPlatform);
    }

    private void createDefaultTenant(final PlatformServiceAccessor platformAccessor, final PlatformService platformService,
            final TransactionService transactionService) throws STenantCreationException {
        final String tenantName = "default";
        final String description = "Default tenant";
        String userName = "";
        SessionAccessor sessionAccessor = null;
        long platformSessionId = -1;
        try {

            // add tenant to database
            final String createdBy = "defaultUser";
            final STenantBuilder sTenantBuilder = platformAccessor.getSTenantBuilder();
            final STenant tenant = sTenantBuilder.createNewInstance(tenantName, createdBy, System.currentTimeMillis(), STATUS_DEACTIVATED, true)
                    .setDescription(description).done();
            final Long tenantId = platformService.createTenant(tenant);

            transactionService.complete();
            transactionService.begin();
            // add tenant folder
            String targetDir;
            String sourceDir;
            try {
                final BonitaHomeServer home = BonitaHomeServer.getInstance();
                targetDir = home.getTenantsFolder() + File.separator + tenant.getId();
                sourceDir = home.getTenantTemplateFolder();
            } catch (final Exception e) {
                deleteTenant(tenant.getId());
                throw new STenantCreationException("Bonita home not set!");
            }
            // copy configuration file
            try {
                FileUtils.copyDirectory(new File(sourceDir), new File(targetDir));
            } catch (final IOException e) {
                IOUtil.deleteDir(new File(targetDir));
                deleteTenant(tenant.getId());
                throw new STenantCreationException("Copy File Exception!");
            }
            // Get user name
            try {
                userName = getUserName(tenantId);
            } catch (final Exception e) {
                IOUtil.deleteDir(new File(targetDir));
                deleteTenant(tenant.getId());
                throw new STenantCreationException("Access File Exception!");
            }
            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            final SDataSourceModelBuilder sDataSourceModelBuilder = tenantServiceAccessor.getSDataSourceModelBuilder();
            final DataService dataService = tenantServiceAccessor.getDataService();
            final SessionService sessionService = platformAccessor.getSessionService();
            final CommandService commandService = tenantServiceAccessor.getCommandService();
            final SCommandBuilder commandBuilder = tenantServiceAccessor.getSCommandBuilderAccessor().getSCommandBuilder();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final SSession session = sessionService.createSession(tenantId, -1L, userName, true);
            
            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            
            sessionAccessor.setSessionInfo(session.getId(), tenantId);// necessary to create default data source
            createDefaultDataSource(sDataSourceModelBuilder, dataService);
            final DefaultCommandProvider defaultCommandProvider = tenantServiceAccessor.getDefaultCommandProvider();
            createDefaultCommands(commandService, commandBuilder, defaultCommandProvider);
            sessionService.deleteSession(session.getId());
        } catch (final Exception e) {
            throw new STenantCreationException("Unable to create tenant " + tenantName, e);
        } finally {
            cleanSessionAccessor(sessionAccessor);
            sessionAccessor.setSessionInfo(platformSessionId, -1);
        }
    }

    private void cleanSessionAccessor(final SessionAccessor sessionAccessor) {
        if (sessionAccessor != null) {
            sessionAccessor.deleteSessionId();
        }
    }

    protected void createDefaultCommands(final CommandService commandService, final SCommandBuilder commandBuilder, final DefaultCommandProvider provider)
            throws SCommandAlreadyExistsException, SCommandCreationException {
        for (final CommandDescriptor command : provider.getDefaultCommands()) {
            final SCommand sCommand = commandBuilder.createNewInstance(command.getName(), command.getDescription(), command.getImplementation())
                    .setSystem(true).done();
            commandService.create(sCommand);
        }
    }

    protected void createDefaultDataSource(final SDataSourceModelBuilder sDataSourceModelBuilder, final DataService dataService)
            throws SDataSourceAlreadyExistException, SDataException {
        final SDataSource bonitaDataSource = sDataSourceModelBuilder.getDataSourceBuilder()
                .createNewInstance("bonita_data_source", "6.0", SDataSourceState.ACTIVE, "org.bonitasoft.engine.data.instance.DataInstanceDataSourceImpl")
                .done();
        dataService.createDataSource(bonitaDataSource);

        final SDataSource transientDataSource = sDataSourceModelBuilder
                .getDataSourceBuilder()
                .createNewInstance("bonita_transient_data_source", "6.0", SDataSourceState.ACTIVE,
                        "org.bonitasoft.engine.core.data.instance.impl.TransientDataInstanceDataSource").done();
        dataService.createDataSource(transientDataSource);
    }

    private String getUserName(final long tenantId) throws IOException, BonitaHomeNotSetException {
        final String tenantPath = BonitaHomeServer.getInstance().getTenantConfFolder(tenantId) + File.separator + "bonita-server.properties";
        final File file = new File(tenantPath);
        final Properties properties = PropertiesManager.getProperties(file);
        return properties.getProperty("userName");
    }

    private void deleteTenant(final long tenantId) throws STenantDeletionException {
        // TODO : Reduce number of transactions
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = getPlatformAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionExecutor transactionExecutor = platformAccessor.getTransactionExecutor();

            // delete tenant objects in database
            final TransactionContent transactionContentForTenantObjects = new DeleteTenantObjects(tenantId, platformService);
            transactionExecutor.execute(transactionContentForTenantObjects);

            // delete tenant in database
            final TransactionContent transactionContentForTenant = new DeleteTenant(tenantId, platformService);
            transactionExecutor.execute(transactionContentForTenant);

            // delete tenant folder
            final String targetDir = BonitaHomeServer.getInstance().getTenantsFolder() + File.separator + tenantId;
            IOUtil.deleteDir(new File(targetDir));
        } catch (final STenantNotFoundException e) {
            log(platformAccessor, e);
            throw new STenantDeletionException(e);
        } catch (final SDeletingActivatedTenantException e) {
            log(platformAccessor, e);
            throw new STenantDeletionException("Unable to delete an activated tenant " + tenantId);
        } catch (final Exception e) {
            log(platformAccessor, e);
            throw new STenantDeletionException(e);
        }
    }

    private void activateDefaultTenant() throws STenantActivationException {
        // TODO : Reduce number of transactions
        PlatformServiceAccessor platformAccessor = null;
        SessionAccessor sessionAccessor = null;
        SchedulerService schedulerService = null;
        final boolean schedulerStarted = false;
        long platformSessionId = -1; 
        try {
            platformAccessor = getPlatformAccessor();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final STenant tenant = getDefaultTenant();
            final long tenantId = tenant.getId();
            final PlatformService platformService = platformAccessor.getPlatformService();
            schedulerService = platformAccessor.getSchedulerService();
            final SessionService sessionService = platformAccessor.getSessionService();
            final NodeConfiguration plaformConfiguration = platformAccessor.getPlaformConfiguration();
            final WorkService workService = platformAccessor.getWorkService();

            // here the scheduler is started only to be able to store global jobs. Once theses jobs are stored the scheduler is stopped and it will started
            // definitively in startNode method
            schedulerService.start();
            
            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            
            final long sessionId = createSessionAndMakeItActive(tenantId, sessionAccessor, sessionService);
            final ActivateTenant activateTenant = new ActivateTenant(tenantId, platformService, schedulerService, plaformConfiguration,
                    platformAccessor.getTechnicalLoggerService(), workService);
            activateTenant.execute();
            sessionService.deleteSession(sessionId);
        } catch (final STenantActivationException stae) {
            log(platformAccessor, stae);
            throw stae;
        } catch (final STenantNotFoundException stnfe) {
            log(platformAccessor, stnfe);
            throw new STenantActivationException(stnfe);
        } catch (final Exception e) {
            log(platformAccessor, e);
            throw new STenantActivationException(e);
        } finally {
            if (schedulerStarted) {
                try {
                    // stop scheduler after scheduling global jobs
                    schedulerService.shutdown();
                } catch (final SBonitaException e) {
                    log(platformAccessor, e);
                    throw new STenantActivationException(e);
                }
            }
            cleanSessionAccessor(sessionAccessor);
            sessionAccessor.setSessionInfo(platformSessionId, -1);
        }
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, "system").getId();
    }

    private void log(final PlatformServiceAccessor platformAccessor, final Exception e) {
        if (platformAccessor != null) {
            platformAccessor.getTechnicalLoggerService().log(this.getClass(), TechnicalLogSeverity.ERROR, e);
        } else {
            e.printStackTrace();
        }
    }

    private void deactiveTenant(final long tenantId) throws STenantDeactivationException {
        // TODO : Reduce number of transactions
        PlatformServiceAccessor platformAccessor = null;
        SessionAccessor sessionAccessor = null;
        long platformSessionId = -1;
        try {
            platformAccessor = getPlatformAccessor();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final SchedulerService schedulerService = platformAccessor.getSchedulerService();
            final SessionService sessionService = platformAccessor.getSessionService();
            final WorkService workService = platformAccessor.getWorkService();
            final long sessionId = createSession(tenantId, sessionService);
            
            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            
            final TransactionContent transactionContent = new DeactivateTenant(tenantId, platformService, schedulerService, workService, sessionService);
            transactionContent.execute();
            sessionService.deleteSession(sessionId);
            sessionService.deleteSessionsOfTenant(tenantId);
        } catch (final STenantDeactivationException stde) {
            log(platformAccessor, stde);
            throw stde;
        } catch (final Exception e) {
            log(platformAccessor, e);
            throw new STenantDeactivationException("Tenant deactivation failed.", e);
        } finally {
            cleanSessionAccessor(sessionAccessor);
            sessionAccessor.setSessionInfo(platformSessionId, -1);
        }
    }

    private long createSessionAndMakeItActive(final long tenantId, final SessionAccessor sessionAccessor, final SessionService sessionService)
            throws SBonitaException {
        final long sessionId = createSession(tenantId, sessionService);
        sessionAccessor.setSessionInfo(sessionId, tenantId);
        return sessionId;
    }

    @Override
    @CustomTransactions
    public boolean isPlatformCreated() throws PlatformNotFoundException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new PlatformNotFoundException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionExecutor transactionExecutor = platformAccessor.getTransactionExecutor();
        final TransactionContentWithResult<Boolean> transactionContent = new IsPlatformCreated(platformService);
        try {
            transactionExecutor.execute(transactionContent);
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            return false;
        }
    }

    @Override
    @CustomTransactions
    public PlatformState getPlatformState() throws PlatformNotFoundException {
        // TODO: find an other way to check if bonita_home is set
        getPlatform();
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        if (!isPlatformStarted(platformAccessor)) {
            return PlatformState.STOPPED;
        }
        return PlatformState.STARTED;
    }

    private STenant getDefaultTenant() throws STenantNotFoundException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = getPlatformAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            return platformService.getDefaultTenant();
        } catch (final SBonitaException e) {
            log(platformAccessor, e);
            throw new STenantNotFoundException("Unable to retrieve the defaultTenant", e);
        } catch (final Exception e) {
            log(platformAccessor, e);
            throw new STenantNotFoundException("Unable to retrieve the defaultTenant", e);
        }
    }

}
