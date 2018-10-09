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
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenantObjects;
import org.bonitasoft.engine.api.impl.transaction.platform.GetPlatformContent;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.login.TechnicalUser;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.execution.work.TenantRestarter;
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
import org.bonitasoft.engine.platform.exception.STenantException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.profile.DefaultProfilesUpdater;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.JobRegister;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
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
    static boolean isNodeStarted = false;

    public PlatformAPIImpl() {
        super();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void createPlatform() throws CreationException {
        //nothing to do
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

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void startNode() throws StartNodeException {
        if (isNodeStarted) {
            throw new StartNodeException("Node already started");
        }
        final PlatformServiceAccessor platformAccessor;
        SessionAccessor sessionAccessor;
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
                final boolean mustRestartElements = !isNodeStarted();
                Map<STenant, List<TenantRestartHandler>> restartHandlersOfTenant = null;
                if (mustRestartElements) {
                    // restart handlers of tenant are executed before any service start
                    restartHandlersOfTenant = beforeServicesStartOfRestartHandlersOfTenant(platformAccessor, sessionAccessor, tenants);
                }
                startServicesOfTenants(platformAccessor, sessionAccessor, tenants);
                if (mustRestartElements) {
                    startScheduler(platformAccessor, tenants);
                    restartHandlersOfPlatform(platformAccessor);
                }
                isNodeStarted = true;
                if (mustRestartElements) {
                    afterServicesStartOfRestartHandlersOfTenant(platformAccessor, restartHandlersOfTenant);
                }
                registerMissingTenantsDefaultJobs(platformAccessor, sessionAccessor, tenants);

            } catch (final SClassLoaderException | SDependencyException e) {
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
                throw new StartNodeException("Platform stopping failed : " + exp.getMessage(), e);
            }
            throw e;
        }
    }

    /**
     * Registers missing default jobs (if any) for the provided tenants
     */
    void registerMissingTenantsDefaultJobs(final PlatformServiceAccessor platformAccessor, final SessionAccessor sessionAccessor,
            final List<STenant> tenants) throws BonitaHomeNotSetException, BonitaHomeConfigurationException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, SBonitaException, IOException, ClassNotFoundException {
        final TransactionService transactionService = platformAccessor.getTransactionService();
        for (final STenant tenant : tenants) {
            long platformSessionId = -1;
            try {
                final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenant.getId());

                final long sessionId = createSession(tenant.getId(), tenantServiceAccessor.getSessionService());
                platformSessionId = sessionAccessor.getSessionId();
                sessionAccessor.deleteSessionId();
                sessionAccessor.setSessionInfo(sessionId, tenant.getId());

                final SchedulerService schedulerService = tenantServiceAccessor.getSchedulerService();
                final TenantConfiguration tenantConfiguration = tenantServiceAccessor.getTenantConfiguration();
                final List<JobRegister> defaultJobs = tenantConfiguration.getJobsToRegister();

                // Only register missing default jobs if they are missing
                transactionService.begin();
                final List<String> scheduledJobNames = schedulerService.getJobs();
                try {
                    for (final JobRegister defaultJob : defaultJobs) {
                        if (!scheduledJobNames.contains(defaultJob.getJobName())) {
                            registerJob(schedulerService, defaultJob);
                        }
                    }
                } finally {
                    transactionService.complete();
                }
            } finally {
                cleanSessionAccessor(sessionAccessor, platformSessionId);
            }
        }
    }

    void registerJob(final SchedulerService schedulerService, final JobRegister jobRegister) throws SSchedulerException {
        final SJobDescriptor jobDescriptor = BuilderFactory.get(SJobDescriptorBuilderFactory.class)
                .createNewInstance(jobRegister.getJobClass().getName(), jobRegister.getJobName(), true).done();
        final List<SJobParameter> jobParameters = new ArrayList<>();
        for (final Entry<String, Serializable> entry : jobRegister.getJobParameters().entrySet()) {
            jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance(entry.getKey(), entry.getValue()).done());
        }
        final Trigger trigger = jobRegister.getTrigger();
        schedulerService.schedule(jobDescriptor, jobParameters, trigger);
    }

    SessionAccessor createSessionAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createSessionAccessor();
    }

    void afterServicesStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformAccessor,
            final Map<STenant, List<TenantRestartHandler>> tenantRestartHandlersOfTenants) {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        if (platformConfiguration.shouldResumeElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            for (Entry<STenant, List<TenantRestartHandler>> tenantRestartHandlers : tenantRestartHandlersOfTenants.entrySet()) {
                TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantRestartHandlers.getKey().getId());
                new TenantRestarter(platformAccessor, tenantServiceAccessor).executeAfterServicesStart(tenantRestartHandlers.getValue());
            }

        }
    }

    Map<STenant, List<TenantRestartHandler>> beforeServicesStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformAccessor,
            final SessionAccessor sessionAccessor,
            final List<STenant> tenants) throws Exception {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        Map<STenant, List<TenantRestartHandler>> restartHandlers = new HashMap<>();
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

                        restartHandlers.put(tenant, beforeServicesStartOfRestartHandlersOfTenant(platformAccessor, tenantId));
                    } finally {
                        sessionService.deleteSession(sessionId);
                        cleanSessionAccessor(sessionAccessor, platformSessionId);
                    }
                }
            }
        }
        return restartHandlers;
    }

    void restartHandlersOfPlatform(final PlatformServiceAccessor platformAccessor) throws Exception {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        for (final RestartHandler restartHandler : platformConfiguration.getRestartHandlers()) {

            platformAccessor.getTransactionService().executeInTransaction(() -> {
                restartHandler.execute();
                return null;
            });
        }
    }

    void startScheduler(final PlatformServiceAccessor platformAccessor, final List<STenant> tenants) throws SBonitaException,
            BonitaHomeNotSetException, BonitaHomeConfigurationException, IOException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException, ClassNotFoundException {
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        if (platformConfiguration.shouldStartScheduler() && !schedulerService.isStarted()) {
            schedulerService.initializeScheduler();
            addPlatformJobListeners(platformAccessor, schedulerService);
            addTenantJobListeners(platformAccessor, tenants, schedulerService);
            schedulerService.start();
        }
    }

    private void addTenantJobListeners(PlatformServiceAccessor platformAccessor, final List<STenant> tenants, final SchedulerService schedulerService)
            throws SBonitaException,
            BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException, ClassNotFoundException {
        for (STenant tenant : tenants) {
            addTenantJobListener(platformAccessor, schedulerService, tenant.getId());
        }
    }

    private void addTenantJobListener(PlatformServiceAccessor platformAccessor, final SchedulerService schedulerService, final long tenantId)
            throws SBonitaException, BonitaHomeNotSetException,
            IOException, BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        final List<AbstractBonitaTenantJobListener> jobListeners = platformAccessor.getTenantServiceAccessor(tenantId).getTenantConfiguration()
                .getJobListeners();
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

        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        if (!transactionService.executeInTransaction(checkPlatformVersion)) {
            throw new StartNodeException(checkPlatformVersion.getErrorMessage());
        }
    }

    void startPlatformServices(final PlatformServiceAccessor platformAccessor) throws SBonitaException, StartNodeException {
        final SchedulerService schedulerService = platformAccessor.getSchedulerService();
        final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();
        final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();
        final List<PlatformLifecycleService> servicesToStart = getPlatformServicesToStart(platformConfiguration);
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

    protected List<PlatformLifecycleService> getPlatformServicesToStart(NodeConfiguration platformConfiguration) throws StartNodeException {
        return platformConfiguration.getLifecycleServices();
    }

    private List<TenantRestartHandler> beforeServicesStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformAccessor, final long tenantId)
            throws Exception {
        final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);

        final Callable<List<TenantRestartHandler>> callable = new Callable<List<TenantRestartHandler>>() {

            @Override
            public List<TenantRestartHandler> call() throws Exception {
                return new TenantRestarter(platformAccessor, tenantServiceAccessor).executeBeforeServicesStart();
            }
        };
        return tenantServiceAccessor.getUserTransactionService().executeInTransaction(callable);

    }

    List<STenant> getTenants(final PlatformServiceAccessor platformAccessor) throws Exception {
        final PlatformService platformService = platformAccessor.getPlatformService();
        final TransactionService transactionService = platformAccessor.getTransactionService();
        return transactionService.executeInTransaction(new GetTenantsCallable(platformService));
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void stopNode() throws StopNodeException {
        try {
            final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            final NodeConfiguration nodeConfiguration = platformAccessor.getPlatformConfiguration();
            final List<PlatformLifecycleService> otherServicesToStop = getPlatformServicesToStart(nodeConfiguration);
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
        } catch (final SBonitaException | BonitaHomeNotSetException | InstantiationException | IllegalAccessException | ClassNotFoundException
                | IOException e) {
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
        final PlatformServiceAccessor platformAccessor;

        try {
            platformAccessor = getPlatformAccessor();
            List<STenant> sTenants = platformAccessor.getTransactionService().executeInTransaction(() -> {
                final PlatformService platformService = platformAccessor.getPlatformService();
                final List<STenant> tenants = platformService.getTenants(new QueryOptions(0, Integer.MAX_VALUE));
                for (final STenant sTenant : tenants) {
                    platformService.deactiveTenant(sTenant.getId());
                }
                return tenants;
            });
            for (STenant sTenant : sTenants) {
                deleteTenant(sTenant.getId());
            }
        } catch (Exception e) {
            throw new DeletionException(e);
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void deletePlatform() throws DeletionException {

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
            createTenantFolderInBonitaHome(tenantId);

            // Create session
            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            final SessionService sessionService = tenantServiceAccessor.getSessionService();
            TechnicalUser technicalUser = tenantServiceAccessor.getTechnicalUser();
            sessionAccessor = createSessionAccessor();
            final SSession session = sessionService.createSession(tenantId, -1L, technicalUser.getUserName(), true);
            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            sessionAccessor.setSessionInfo(session.getId(), tenantId);// necessary to create default data source

            // Create default profiles: they will be updated by the tenant profile update handler in a separate thread but we create them here synchronously
            new DefaultProfilesUpdater(platformAccessor, tenantServiceAccessor).execute();
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

    private void createTenantFolderInBonitaHome(final long tenantId) {
        getBonitaHomeServerInstance().createTenant(tenantId);
    }

    protected void cleanSessionAccessor(final SessionAccessor sessionAccessor, final long platformSessionId) {
        if (sessionAccessor != null) {
            sessionAccessor.deleteSessionId();
            if (platformSessionId != -1) {
                sessionAccessor.setSessionInfo(platformSessionId, -1);
            }
        }
    }

    void deleteTenant(final long tenantId) throws STenantDeletionException {
        // TODO : Reduce number of transactions
        PlatformServiceAccessor platformAccessor;
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
            getBonitaHomeServerInstance().deleteTenant(tenantId);
        } catch (final STenantNotFoundException e) {
            throw new STenantDeletionException(e);
        } catch (final SDeletingActivatedTenantException e) {
            throw new STenantDeletionException("Unable to delete an activated tenant " + tenantId);
        } catch (final STenantDeletionException e) {
            throw e;
        } catch (final Exception e) {
            throw new STenantDeletionException(e);
        }
    }

    private void activateDefaultTenant() throws STenantActivationException {
        // TODO : Reduce number of transactions
        SessionAccessor sessionAccessor = null;
        long platformSessionId = -1;
        try {
            PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            sessionAccessor = createSessionAccessor();
            STenant defaultTenant = getDefaultTenant();
            final long tenantId = defaultTenant.getId();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final SchedulerService schedulerService = platformAccessor.getSchedulerService();
            final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();
            final NodeConfiguration platformConfiguration = platformAccessor.getPlatformConfiguration();

            // here the scheduler is started only to be able to store global jobs. Once theses jobs are stored the scheduler is stopped and it will started
            // definitively in startNode method
            startScheduler(platformAccessor, Collections.singletonList(defaultTenant));
            // FIXME: commented out for the tests to not restart the scheduler all the time. Will need to be refactored. (It should be the responsibility of
            // startNode() method to start the scheduler, not ActivateTenant)
            // schedulerStarted = true;

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            final long sessionId = createSessionAndMakeItActive(platformAccessor, sessionAccessor, tenantId);

            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            final ActivateTenant activateTenant = new ActivateTenant(tenantId, platformService, schedulerService, platformAccessor.getTechnicalLoggerService(),
                    tenantServiceAccessor.getWorkService(), tenantServiceAccessor.getConnectorExecutor(), platformConfiguration,
                    tenantServiceAccessor.getTenantConfiguration());
            activateTenant.execute();
            sessionService.deleteSession(sessionId);
        } catch (final STenantActivationException e) {
            throw e;
        } catch (final Exception e) {
            throw new STenantActivationException(e);
        } finally {
            cleanSessionAccessor(sessionAccessor, platformSessionId);
        }
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
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
    public boolean isPlatformInitialized() throws PlatformNotFoundException {
        try {
            PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            PlatformService platformService = platformAccessor.getPlatformService();
            return platformAccessor.getTransactionService().executeInTransaction(platformService::isDefaultTenantCreated);
        } catch (final Exception e) {
            throw new PlatformNotFoundException("Cannot determine if the default tenant is created", e);
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public boolean isPlatformCreated() throws PlatformNotFoundException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            return platformAccessor.getTransactionService().executeInTransaction(platformService::isPlatformCreated);
        } catch (Exception e) {
            throw new BonitaRuntimeException(e);
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
        try {
            PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            return platformService.getDefaultTenant();
        } catch (final STenantNotFoundException e) {
            throw e;
        } catch (final Exception e) {
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
        } catch (final Exception e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public Map<String, byte[]> getClientPlatformConfigurations() {
        return getBonitaHomeServerInstance().getClientPlatformConfigurations();
    }

    @Override
    public Map<Long, Map<String, byte[]>> getClientTenantConfigurations() {
        try {
            PlatformService platformService = getPlatformAccessor().getPlatformService();
            List<STenant> tenants = platformService.getTenants(QueryOptions.countQueryOptions());
            HashMap<Long, Map<String, byte[]>> conf = new HashMap<>();
            for (STenant tenant : tenants) {
                conf.put(tenant.getId(),
                        getBonitaHomeServerInstance().getClientTenantConfigurations(tenant.getId()));
            }
            return conf;
        } catch (BonitaException | IOException | IllegalAccessException | ClassNotFoundException | InstantiationException | STenantException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public byte[] getClientTenantConfiguration(long tenantId, String file) {
        return getBonitaHomeServerInstance().getTenantPortalConfiguration(tenantId, file);
    }

    BonitaHomeServer getBonitaHomeServerInstance() {
        return BonitaHomeServer.getInstance();
    }

    @Override
    public void updateClientTenantConfigurationFile(long tenantId, String file, byte[] content) throws UpdateException {
        getBonitaHomeServerInstance().updateTenantPortalConfigurationFile(tenantId, file, content);
    }
}
