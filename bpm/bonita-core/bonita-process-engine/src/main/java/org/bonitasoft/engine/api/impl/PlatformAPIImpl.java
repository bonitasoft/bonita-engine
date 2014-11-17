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
package org.bonitasoft.engine.api.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.GetTenantsCallable;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.api.impl.transaction.StartServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.StopServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.platform.ActivateTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.CheckPlatformVersion;
import org.bonitasoft.engine.api.impl.transaction.platform.CleanPlatformTableContent;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteAllTenants;
import org.bonitasoft.engine.api.impl.transaction.platform.DeletePlatformContent;
import org.bonitasoft.engine.api.impl.transaction.platform.DeletePlatformTableContent;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenantObjects;
import org.bonitasoft.engine.api.impl.transaction.platform.GetPlatformContent;
import org.bonitasoft.engine.api.impl.transaction.platform.IsPlatformCreated;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandService;
import org.bonitasoft.engine.command.DefaultCommandProvider;
import org.bonitasoft.engine.command.SCommandAlreadyExistsException;
import org.bonitasoft.engine.command.SCommandCreationException;
import org.bonitasoft.engine.command.model.SCommand;
import org.bonitasoft.engine.command.model.SCommandBuilderFactory;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.platform.exception.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantCreationException;
import org.bonitasoft.engine.platform.exception.STenantDeletionException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilderFactory;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.profile.ImportPolicy;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.ProfilesImporter;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.xml.Parser;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Lu Kai
 * @author Zhang Bole
 * @author Yanyan Liu
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class PlatformAPIImpl implements PlatformAPI {

    private static final String STATUS_DEACTIVATED = "DEACTIVATED";

    private static final String PROFILES_FILE = "profiles.xml";

    private static boolean isNodeStarted = false;

    private final PlatformAPIImplDelegate delegate;

    public PlatformAPIImpl() {
        super();
        delegate = new PlatformAPIImplDelegate();
    }

    public PlatformAPIImpl(final PlatformAPIImplDelegate delegate) {
        super();
        this.delegate = delegate;
    }

    protected PlatformAPIImplDelegate getDelegate() {
        return delegate;
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
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
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
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
            } catch (final Exception e) {
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
    @AvailableOnStoppedNode
    public void createAndInitializePlatform() throws CreationException {
        createPlatform();
        initializePlatform();
    }

    protected PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    private SPlatform constructPlatform(final PlatformServiceAccessor platformAccessor) {
        final PlatformService platformService = platformAccessor.getPlatformService();

        // FIXME construct platform object from a configuration file
        final String version = platformService.getSPlatformProperties().getPlatformVersion();
        final String previousVersion = "";
        final String initialVersion = version;
        // FIXME createdBy when PlatformSessionAccessor will exist
        final String createdBy = "platformAdmin";
        // FIXME do that in the builder
        final long created = System.currentTimeMillis();
        return BuilderFactory.get(SPlatformBuilderFactory.class).createNewInstance(version, previousVersion, initialVersion, createdBy, created).done();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void startNode() throws StartNodeException {
        final PlatformServiceAccessor platformAccessor;
        SessionAccessor sessionAccessor = null;
        try {
            platformAccessor = getPlatformAccessor();
            sessionAccessor = createSessionAccessor();
        } catch (final Exception e) {
            throw new StartNodeException(e);
        }

        try {
            try {
                checkPlatformVersion(platformAccessor);
                final List<STenant> tenants = getTenants(platformAccessor);
                startPlatformServices(platformAccessor);
                boolean mustRestartElements;
                if (mustRestartElements = !isNodeStarted()) {
                    // restart handlers of tenant are executed before any service start
                    beforeServicesStartOfRestartHandlersOfTenant(platformAccessor, sessionAccessor, tenants);
                }
                startServicesOfTenants(platformAccessor, sessionAccessor, tenants);
                if (mustRestartElements) {
                    startScheduler(platformAccessor, tenants);
                    restartHandlersOfPlatform(platformAccessor);
                }
                isNodeStarted = true;
                if (mustRestartElements) {
                    afterServicesStartOfRestartHandlersOfTenant(platformAccessor, sessionAccessor, tenants);
                }
            } catch (final SClassLoaderException e) {
                throw new StartNodeException("Platform starting failed while initializing platform classloaders.", e);
            } catch (final SDependencyException e) {
                throw new StartNodeException("Platform starting failed while initializing platform classloaders.", e);
            } catch (final StartNodeException sne) {
                throw sne;
            } catch (final Exception e) {
                throw new StartNodeException("Platform starting failed.", e);
            } finally {
                cleanSessionAccessor(sessionAccessor, -1);
            }
        } catch (final StartNodeException e) {
            // If an exception is thrown, stop the platform that was started.
            try {
                shutdownScheduler(platformAccessor);
            } catch (final StartNodeException sne) {
                throw sne;
            } catch (final Exception exp) {
                throw new StartNodeException("Platform stoping failed : " + exp.getMessage(), e);
            }
            throw e;
        }
    }

    SessionAccessor createSessionAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createSessionAccessor();
    }

    /**
     * @param platformAccessor
     * @param sessionAccessor
     * @param tenants
     * @throws SBonitaException
     */
    void afterServicesStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformAccessor, final SessionAccessor sessionAccessor,
            final List<STenant> tenants) {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final SessionService sessionService = platformAccessor.getSessionService();
        final TechnicalLoggerService technicalLoggerService = platformAccessor.getTechnicalLoggerService();

        if (platformConfiguration.shouldResumeElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            new StarterThread(platformAccessor, sessionService, platformConfiguration, tenants, sessionAccessor, technicalLoggerService)
                    .start();

        }
    }

    void beforeServicesStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformAccessor, final SessionAccessor sessionAccessor,
            final List<STenant> tenants) throws SessionIdNotSetException, SBonitaException, BonitaHomeNotSetException, InstantiationException,
            IllegalAccessException, ClassNotFoundException, BonitaHomeConfigurationException, IOException, Exception, SSessionNotFoundException {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final SessionService sessionService = platformAccessor.getSessionService();

        if (platformConfiguration.shouldResumeElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            for (final STenant tenant : tenants) {
                if (!tenant.isPaused()) {
                    final long tenantId = tenant.getId();
                    long sessionId = -1;
                    long platformSessionId = -1;
                    try {
                        platformSessionId = sessionAccessor.getSessionId();
                        sessionAccessor.deleteSessionId();
                        sessionId = createSessionAndMakeItActive(platformAccessor, sessionAccessor, tenantId);

                        beforeServicesStartOfRestartHandlersOfTenant(platformAccessor, tenantId);
                    } finally {
                        sessionService.deleteSession(sessionId);
                        cleanSessionAccessor(sessionAccessor, platformSessionId);
                    }
                }
            }
        }
    }

    void restartHandlersOfPlatform(final PlatformServiceAccessor platformAccessor) throws Exception {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        for (final RestartHandler restartHandler : platformConfiguration.getRestartHandlers()) {

            final Callable<Void> callable = new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    restartHandler.execute();
                    return null;
                }
            };
            platformAccessor.getTransactionService().executeInTransaction(callable);
        }
    }

    private void startScheduler(final PlatformServiceAccessor platformAccessor, final List<STenant> tenants) throws SSchedulerException, SBonitaException,
            BonitaHomeNotSetException, BonitaHomeConfigurationException, IOException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        if (platformConfiguration.shouldStartScheduler() && !schedulerService.isStarted()) {
            schedulerService.initializeScheduler();
            addPlatformJobListener(schedulerService, platformAccessor.getPlatformConfiguration());
            for (final STenant sTenant : tenants) {
                addTenantJobListener(schedulerService, sTenant.getId());
            }
            schedulerService.start();
        }
    }

    private void addPlatformJobListener(final SchedulerService schedulerService, final NodeConfiguration platformConfiguration) throws SSchedulerException {
        final List<AbstractBonitaPlatformJobListener> jobListeners = platformConfiguration.getJobListeners();
        if (!jobListeners.isEmpty()) {
            schedulerService.addJobListener(jobListeners);
        }
    }

    private void addTenantJobListener(final SchedulerService schedulerService, final long tenantId) throws SBonitaException, BonitaHomeNotSetException,
            IOException, BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final List<AbstractBonitaTenantJobListener> jobListeners = getTenantServiceAccessor(tenantId).getTenantConfiguration().getJobListeners();
        if (!jobListeners.isEmpty()) {
            schedulerService.addJobListener(jobListeners, String.valueOf(tenantId));
        }
    }

    void startServicesOfTenants(final PlatformServiceAccessor platformAccessor,
            final SessionAccessor sessionAccessor, final List<STenant> tenants) throws Exception {
        final SessionService sessionService = platformAccessor.getSessionService();

        for (final STenant tenant : tenants) {
            final long tenantId = tenant.getId();
            if (!tenant.isPaused()) {
                long sessionId = -1;
                long platformSessionId = -1;
                try {
                    platformSessionId = sessionAccessor.getSessionId();
                    sessionAccessor.deleteSessionId();
                    sessionId = createSessionAndMakeItActive(platformAccessor, sessionAccessor, tenantId);
                    final SetServiceState startService = new SetServiceState(tenantId, new StartServiceStrategy());
                    platformAccessor.getTransactionService().executeInTransaction(startService);
                } finally {
                    sessionService.deleteSession(sessionId);
                    cleanSessionAccessor(sessionAccessor, platformSessionId);
                }
            }
        }
    }

    void checkPlatformVersion(final PlatformServiceAccessor platformAccessor) throws Exception {
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionService transactionService = platformAccessor.getTransactionService();

        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService, BonitaHomeServer.getInstance());
        if (!transactionService.executeInTransaction(checkPlatformVersion)) {
            throw new StartNodeException(checkPlatformVersion.getErrorMessage());
        }
    }

    void startPlatformServices(final PlatformServiceAccessor platformAccessor) throws SBonitaException {
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final List<PlatformLifecycleService> servicesToStart = platformConfiguration.getLifecycleServices();
        for (final PlatformLifecycleService serviceWithLifecycle : servicesToStart) {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
                logger.log(getClass(), TechnicalLogSeverity.INFO, "Start service of platform : " + serviceWithLifecycle.getClass().getName());
            }
            // scheduler might be already running
            // skip service start
            if (!serviceWithLifecycle.getClass().isInstance(schedulerService) || !schedulerService.isStarted()) {
                serviceWithLifecycle.start();
            }
        }
    }

    private void beforeServicesStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformAccessor, final long tenantId) throws Exception {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);

        for (final TenantRestartHandler restartHandler : platformConfiguration.getTenantRestartHandlers()) {
            final Callable<Void> callable = new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    restartHandler.beforeServicesStart(platformAccessor, tenantServiceAccessor);
                    return null;
                }
            };
            tenantServiceAccessor.getUserTransactionService().executeInTransaction(callable);
        }
    }

    protected List<STenant> getTenants(final PlatformServiceAccessor platformAccessor) throws Exception {
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionService transactionService = platformAccessor.getTransactionService();
        return transactionService.executeInTransaction(new GetTenantsCallable(platformService));
    }

    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return ServiceAccessorFactory.getInstance().createTenantServiceAccessor(tenantId);
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void stopNode() throws StopNodeException {
        try {
            final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            final NodeConfiguration nodeConfiguration = platformAccessor.getPlatformConfiguration();
            final List<PlatformLifecycleService> otherServicesToStart = nodeConfiguration.getLifecycleServices();
            final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();
            if (nodeConfiguration.shouldStartScheduler()) {
                // we shutdown the scheduler only if we are also responsible of starting it
                shutdownScheduler(platformAccessor);
            }
            if (nodeConfiguration.shouldClearSessions()) {
                platformAccessor.getSessionService().deleteSessions();
            }
            final List<STenant> tenantIds = getTenants(platformAccessor);
            for (final STenant tenant : tenantIds) {
                // stop the tenant services:
                platformAccessor.getTransactionService().executeInTransaction(new SetServiceState(tenant.getId(), new StopServiceStrategy()));
            }
            for (final PlatformLifecycleService serviceWithLifecycle : otherServicesToStart) {
                logger.log(getClass(), TechnicalLogSeverity.INFO, "Stop service of platform: " + serviceWithLifecycle.getClass().getName());
                serviceWithLifecycle.stop();
            }
            isNodeStarted = false;
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
        } catch (final StopNodeException e) {
            throw e;
        } catch (final Exception e) {
            throw new StopNodeException(e);
        }
    }

    private void shutdownScheduler(final PlatformServiceAccessor platformAccessor) throws Exception {
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();

        if (isNodeStarted()) {
            schedulerService.stop();
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
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
                    clean.execute();
                    deleteAll.execute();
                    return null;
                }
            });
        } catch (final DeletionException e) {
            throw e;
        } catch (final Exception e) {
            throw new DeletionException(e);
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
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
    @AvailableOnStoppedNode
    public void cleanAndDeletePlaftorm() throws DeletionException {
        cleanPlatform();
        deletePlatform();
    }

    @Override
    @AvailableOnStoppedNode
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
            final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class)
                    .createNewInstance(tenantName, createdBy, System.currentTimeMillis(), STATUS_DEACTIVATED, true).setDescription(description).done();
            final Long tenantId = platformService.createTenant(tenant);

            transactionService.complete();
            transactionService.begin();
            final String targetDir = createTenantFolderInBonitaHome(tenant);
            // Get user name
            userName = getUserName(tenant, tenantId, targetDir);

            // Create session
            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            final SessionService sessionService = platformAccessor.getSessionService();
            sessionAccessor = createSessionAccessor();
            final SSession session = sessionService.createSession(tenantId, -1L, userName, true);
            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            sessionAccessor.setSessionInfo(session.getId(), tenantId);// necessary to create default data source

            // Create default commands
            createDefaultCommands(tenantServiceAccessor);

            // Create default profiles
            createDefaultProfiles(tenantServiceAccessor);

            // Create default themes : Portal and Mobile
            getDelegate().createDefaultThemes(tenantServiceAccessor);

            sessionService.deleteSession(session.getId());
        } catch (final STenantCreationException e) {
            throw e;
        } catch (final Exception e) {
            throw new STenantCreationException("Unable to create default tenant", e);
        } finally {
            cleanSessionAccessor(sessionAccessor, platformSessionId);
        }
    }

    private String getUserName(final STenant tenant, final Long tenantId, final String targetDir) throws IOException, STenantDeletionException,
            STenantCreationException {
        try {
            return getUserName(tenantId);
        } catch (final Exception e) {
            IOUtil.deleteDir(new File(targetDir));
            deleteTenant(tenant.getId());
            throw new STenantCreationException("Access File Exception !!");
        }
    }

    private String createTenantFolderInBonitaHome(final STenant tenant) throws STenantDeletionException, STenantCreationException, IOException {
        // add tenant folder
        String targetDir;
        String sourceDir;
        try {
            final BonitaHomeServer home = BonitaHomeServer.getInstance();
            targetDir = home.getTenantsFolder() + File.separator + tenant.getId();
            sourceDir = home.getTenantTemplateFolder();
        } catch (final BonitaHomeNotSetException e) {
            deleteTenant(tenant.getId());
            throw new STenantCreationException("Bonita home not set !!");
        }

        // copy configuration file
        try {
            FileUtils.copyDirectory(new File(sourceDir), new File(targetDir));
        } catch (final IOException e) {
            IOUtil.deleteDir(new File(targetDir));
            deleteTenant(tenant.getId());
            throw new STenantCreationException("Copy File Exception !!");
        }
        return targetDir;
    }

    protected void createDefaultProfiles(final TenantServiceAccessor tenantServiceAccessor) throws Exception {
        final Parser parser = tenantServiceAccessor.getProfileParser();
        final ProfileService profileService = tenantServiceAccessor.getProfileService();
        final IdentityService identityService = tenantServiceAccessor.getIdentityService();

        final String xmlContent;
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(getProfileFileName());
        if (inputStream == null) {
            // no default profiles
            return;
        }
        try {
            xmlContent = IOUtils.toString(inputStream, org.bonitasoft.engine.io.IOUtil.FILE_ENCODING);
        } finally {
            inputStream.close();
        }
        final List<ExportedProfile> profilesFromXML = ProfilesImporter.getProfilesFromXML(xmlContent, parser);
        importProfiles(profileService, identityService, profilesFromXML, tenantServiceAccessor);
    }

    protected void importProfiles(final ProfileService profileService, final IdentityService identityService, final List<ExportedProfile> profilesFromXML,
            final TenantServiceAccessor tenantServiceAccessor) throws ExecutionException {
        new ProfilesImporter(profileService, identityService, profilesFromXML, ImportPolicy.FAIL_ON_DUPLICATES).importProfiles(-1);
    }

    protected void cleanSessionAccessor(final SessionAccessor sessionAccessor, final long platformSessionId) {
        if (sessionAccessor != null) {
            sessionAccessor.deleteSessionId();
            if (platformSessionId != -1) {
                sessionAccessor.setSessionInfo(platformSessionId, -1);
            }
        }
    }

    protected void createDefaultCommands(final TenantServiceAccessor tenantServiceAccessor) throws SCommandAlreadyExistsException, SCommandCreationException {
        final CommandService commandService = tenantServiceAccessor.getCommandService();
        final DefaultCommandProvider provider = tenantServiceAccessor.getDefaultCommandProvider();
        final SCommandBuilderFactory fact = BuilderFactory.get(SCommandBuilderFactory.class);
        for (final CommandDescriptor command : provider.getDefaultCommands()) {
            final SCommand sCommand = fact.createNewInstance(command.getName(), command.getDescription(), command.getImplementation()).setSystem(true).done();
            commandService.create(sCommand);
        }
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
            final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();

            // delete tenant objects in database
            final TransactionContent transactionContentForTenantObjects = new DeleteTenantObjects(tenantId, platformService);
            transactionExecutor.execute(transactionContentForTenantObjects);

            // delete tenant in database
            final TransactionContent transactionContentForTenant = new DeleteTenant(tenantId, platformService);
            transactionExecutor.execute(transactionContentForTenant);

            // stop tenant services and clear the spring context
            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);

            // stop the tenant services:
            final SetServiceState stopService = new SetServiceState(tenantId, new StopServiceStrategy());
            platformAccessor.getTransactionService().executeInTransaction(stopService);

            logger.log(getClass(), TechnicalLogSeverity.INFO, "Destroy tenant context of tenant " + tenantId);
            tenantServiceAccessor.destroy();

            // delete tenant folder
            final String targetDir = BonitaHomeServer.getInstance().getTenantsFolder() + File.separator + tenantId;
            IOUtil.deleteDir(new File(targetDir));
        } catch (final STenantNotFoundException e) {
            log(platformAccessor, e);
            throw new STenantDeletionException(e);
        } catch (final SDeletingActivatedTenantException e) {
            log(platformAccessor, e);
            throw new STenantDeletionException("Unable to delete an activated tenant " + tenantId);
        } catch (final STenantDeletionException e) {
            log(platformAccessor, e);
            throw e;
        } catch (final Exception e) {
            log(platformAccessor, e);
            throw new STenantDeletionException(e);
        }
    }

    private void activateDefaultTenant() throws STenantActivationException {
        // TODO : Reduce number of transactions
        PlatformServiceAccessor platformAccessor = null;
        SessionAccessor sessionAccessor = null;
        long platformSessionId = -1;
        try {
            platformAccessor = getPlatformAccessor();
            sessionAccessor = createSessionAccessor();
            final long tenantId = getDefaultTenant().getId();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final SchedulerService schedulerService = platformAccessor.getSchedulerService();
            final SessionService sessionService = platformAccessor.getSessionService();
            final NodeConfiguration plaformConfiguration = platformAccessor.getPlatformConfiguration();

            // here the scheduler is started only to be able to store global jobs. Once theses jobs are stored the scheduler is stopped and it will started
            // definitively in startNode method
            schedulerService.initializeScheduler();
            schedulerService.start();
            // FIXME: commented out for the tests to not restart the scheduler all the time. Will need to be refactored. (It should be the responsibility of
            // startNode() method to start the scheduler, not ActivateTenant)
            // schedulerStarted = true;

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            final long sessionId = createSessionAndMakeItActive(platformAccessor, sessionAccessor, tenantId);

            final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor(tenantId);
            final ActivateTenant activateTenant = new ActivateTenant(tenantId, platformService, schedulerService, platformAccessor.getTechnicalLoggerService(),
                    tenantServiceAccessor.getWorkService(), plaformConfiguration, tenantServiceAccessor.getTenantConfiguration());
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
            cleanSessionAccessor(sessionAccessor, platformSessionId);
        }
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
    }

    private void log(final PlatformServiceAccessor platformAccessor, final Exception e) {
        if (platformAccessor != null) {
            final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
        } else {
            e.printStackTrace();
        }
    }

    private long createSessionAndMakeItActive(final PlatformServiceAccessor platformAccessor, final SessionAccessor sessionAccessor, final long tenantId)
            throws SBonitaException {
        final SessionService sessionService = platformAccessor.getSessionService();

        final long sessionId = createSession(tenantId, sessionService);
        sessionAccessor.setSessionInfo(sessionId, tenantId);
        return sessionId;
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
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
            final TechnicalLoggerService technicalLoggerService = platformAccessor.getTechnicalLoggerService();
            if (technicalLoggerService.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                technicalLoggerService.log(getClass(), TechnicalLogSeverity.DEBUG, e);
            }
            return false;
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public PlatformState getPlatformState() {
        if (isNodeStarted()) {
            return PlatformState.STARTED;
        }
        return PlatformState.STOPPED;
    }

    private STenant getDefaultTenant() throws STenantNotFoundException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = getPlatformAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            return platformService.getDefaultTenant();
        } catch (final STenantNotFoundException e) {
            log(platformAccessor, e);
            throw e;
        } catch (final Exception e) {
            log(platformAccessor, e);
            throw new STenantNotFoundException("Unable to retrieve the defaultTenant.", e);
        }
    }

    /**
     * @return true if the current node is started, false otherwise
     */
    @Override
    @AvailableOnStoppedNode
    public boolean isNodeStarted() {
        return isNodeStarted;
    }

    // Overrided in SP
    protected String getProfileFileName() {
        return PROFILES_FILE;
    }

    @Override
    public void rescheduleErroneousTriggers() throws UpdateException {
        try {
            final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            platformAccessor.getSchedulerService().rescheduleErroneousTriggers();
        } catch (final SSchedulerException sse) {
            throw new UpdateException(sse);
        } catch (final Exception e) {
            throw new UpdateException(e);
        }
    }

}
