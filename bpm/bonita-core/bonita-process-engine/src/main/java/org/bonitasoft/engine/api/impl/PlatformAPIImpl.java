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
package org.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.scheduler.PlatformJobListenerManager;
import org.bonitasoft.engine.api.impl.scheduler.TenantJobListenerManager;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.GetTenantsCallable;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.api.impl.transaction.StartServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.StopServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.platform.ActivateTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.CheckPlatformVersion;
import org.bonitasoft.engine.api.impl.transaction.platform.CleanPlatformTableContent;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteAllTenants;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenantObjects;
import org.bonitasoft.engine.api.impl.transaction.platform.GetPlatformContent;
import org.bonitasoft.engine.api.impl.transaction.platform.IsPlatformCreated;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
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
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
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

    private static boolean isNodeStarted = false;

    public PlatformAPIImpl() {
        super();
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
            platformService.createTables();

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
        final TechnicalLoggerService technicalLoggerService = platformAccessor.getTechnicalLoggerService();

        if (platformConfiguration.shouldResumeElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            new StarterThread(platformAccessor, platformConfiguration, tenants, sessionAccessor, technicalLoggerService)
                    .start();

        }
    }

    void beforeServicesStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformAccessor, final SessionAccessor sessionAccessor,
            final List<STenant> tenants) throws Exception {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();

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
                    final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();
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

    private void startScheduler(final PlatformServiceAccessor platformAccessor, final List<STenant> tenants) throws SBonitaException,
            BonitaHomeNotSetException, BonitaHomeConfigurationException, IOException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        if (platformConfiguration.shouldStartScheduler() && !schedulerService.isStarted()) {
            schedulerService.initializeScheduler();
            addPlatformJobListeners(platformAccessor, schedulerService);
            addTenantJobListeners(tenants, schedulerService);
            schedulerService.start();
        }
    }

    private void addTenantJobListeners(final List<STenant> tenants, final SchedulerService schedulerService) throws SBonitaException,
            BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        for (STenant tenant : tenants) {
            addTenantJobListener(schedulerService, tenant.getId());
        }
    }

    private void addTenantJobListener(final SchedulerService schedulerService, final long tenantId) throws SBonitaException, BonitaHomeNotSetException,
            IOException, BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final List<AbstractBonitaTenantJobListener> jobListeners = getTenantServiceAccessor(tenantId).getTenantConfiguration().getJobListeners();
        TenantJobListenerManager tenantJobListenerManager = new TenantJobListenerManager(schedulerService);
        tenantJobListenerManager.registerListeners(jobListeners, tenantId);
    }

    private void addPlatformJobListeners(final PlatformServiceAccessor platformAccessor, final SchedulerService schedulerService) throws SSchedulerException {
        PlatformJobListenerManager platformJobListenerManager = new PlatformJobListenerManager(schedulerService);
        platformJobListenerManager.registerListener(platformAccessor.getPlatformConfiguration().getJobListeners());
    }

    void startServicesOfTenants(final PlatformServiceAccessor platformAccessor,
            final SessionAccessor sessionAccessor, final List<STenant> tenants) throws Exception {

        for (final STenant tenant : tenants) {
            final long tenantId = tenant.getId();
            if (!tenant.isPaused() && tenant.isActivated()) {
                final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();
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
            final List<PlatformLifecycleService> otherServicesToStop = nodeConfiguration.getLifecycleServices();
            final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();
            if (nodeConfiguration.shouldStartScheduler()) {
                // we shutdown the scheduler only if we are also responsible of starting it
                shutdownScheduler(platformAccessor);
            }
            final List<STenant> tenantIds = getTenants(platformAccessor);
            for (final STenant tenant : tenantIds) {
                if (nodeConfiguration.shouldClearSessions()) {
                    final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenant.getId());
                    tenantServiceAccessor.getSessionService().deleteSessions();
                }
                // stop the tenant services:
                platformAccessor.getTransactionService().executeInTransaction(new SetServiceState(tenant.getId(), new StopServiceStrategy()));
            }
            for (final PlatformLifecycleService serviceWithLifecycle : otherServicesToStop) {
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
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new DeletionException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        try {
            if (isPlatformCreated()) {
                // to ensure Hibernate cache is up-to-date before we drop tables with its content:
                platformAccessor.getTransactionService().executeInTransaction(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        final List<STenant> tenants = platformService.getTenants(new QueryOptions(0, Integer.MAX_VALUE));
                        for (final STenant sTenant : tenants) {
                            if (sTenant.isActivated()) {
                                throw new DeletionException("Cannot delete platform with some active tenants.");
                            }
                        }
                        platformService.deletePlatform();
                        return null;
                    }
                });
            }
        } catch (final DeletionException e) {
            throw e;
        } catch (final Exception e) {
            // ignore not existing platform
        }
        try {
            platformService.deleteTables();
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    @Deprecated
    public void cleanAndDeletePlaftorm() throws DeletionException {
        cleanAndDeletePlatform();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void cleanAndDeletePlatform() throws DeletionException {
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
        Long tenantId = -1L;
        try {
            // add tenant to database
            final String createdBy = "defaultUser";
            final STenant tenant = BuilderFactory.get(STenantBuilderFactory.class)
                    .createNewInstance(tenantName, createdBy, System.currentTimeMillis(), STATUS_DEACTIVATED, true).setDescription(description).done();
            tenantId = platformService.createTenant(tenant);

            transactionService.complete();
            transactionService.begin();
            try {
                createTenantFolderInBonitaHome(tenantId);
            } catch (STenantCreationException e) {
                transactionService.complete();
                throw e;
            }

            try {
                // Get user name
                userName = getUserName(tenantId);
            } catch (Exception e) {
                transactionService.complete();
                throw new STenantCreationException(e);
            }

            // Create session
            final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();
            sessionAccessor = createSessionAccessor();
            final SSession session = sessionService.createSession(tenantId, -1L, userName, true);
            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            sessionAccessor.setSessionInfo(session.getId(), tenantId);// necessary to create default data source

            // Create default profiles: done by profile updater restart handler
            // Create custom page examples: done by page service start
            // Create default themes: done by theme service start

            sessionService.deleteSession(session.getId());
        } catch (final STenantCreationException e) {
            if (tenantId != -1L) {
                try {
                    deleteTenant(tenantId);
                } catch (STenantDeletionException e1) {
                    throw new STenantCreationException("Unable to delete default tenant (after a STenantCreationException) that was being created", e1);
                }
            }
            throw e;
        } catch (final Exception e) {
            throw new STenantCreationException("Unable to create default tenant", e);
        } finally {
            cleanSessionAccessor(sessionAccessor, platformSessionId);
        }
    }

    private void createTenantFolderInBonitaHome(final long tenantId) throws STenantCreationException {
        final BonitaHomeServer home = BonitaHomeServer.getInstance();
        try {
            home.createTenant(tenantId);
        } catch (Exception e) {
            throw new STenantCreationException("Exception while creating tenant folder");
        }
    }

    protected void cleanSessionAccessor(final SessionAccessor sessionAccessor, final long platformSessionId) {
        if (sessionAccessor != null) {
            sessionAccessor.deleteSessionId();
            if (platformSessionId != -1) {
                sessionAccessor.setSessionInfo(platformSessionId, -1);
            }
        }
    }

    private String getUserName(final long tenantId) throws IOException, BonitaHomeNotSetException {
        final Properties properties = BonitaHomeServer.getInstance().getTenantProperties(tenantId);
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
            final BonitaHomeServer home = BonitaHomeServer.getInstance();
            home.deleteTenant(tenantId);
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
            STenant defaultTenant = getDefaultTenant();
            final long tenantId = defaultTenant.getId();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final SchedulerService schedulerService = platformAccessor.getSchedulerService();
            final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();
            final NodeConfiguration plaformConfiguration = platformAccessor.getPlatformConfiguration();

            // here the scheduler is started only to be able to store global jobs. Once theses jobs are stored the scheduler is stopped and it will started
            // definitively in startNode method
            startScheduler(platformAccessor, Arrays.asList(defaultTenant));
            // FIXME: commented out for the tests to not restart the scheduler all the time. Will need to be refactored. (It should be the responsibility of
            // startNode() method to start the scheduler, not ActivateTenant)
            // schedulerStarted = true;

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            final long sessionId = createSessionAndMakeItActive(platformAccessor, sessionAccessor, tenantId);

            final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor(tenantId);
            final ActivateTenant activateTenant = new ActivateTenant(tenantId, platformService, schedulerService, platformAccessor.getTechnicalLoggerService(),
                    tenantServiceAccessor.getWorkService(), tenantServiceAccessor.getConnectorExecutor(), plaformConfiguration,
                    tenantServiceAccessor.getTenantConfiguration());
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
        final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();

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
