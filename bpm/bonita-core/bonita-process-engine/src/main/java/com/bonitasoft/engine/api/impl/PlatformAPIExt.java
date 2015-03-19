/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.impl.transaction.GetNumberOfTenants;
import com.bonitasoft.engine.api.impl.transaction.GetTenantsWithOrder;
import com.bonitasoft.engine.api.impl.transaction.NotifyNodeStoppedTask;
import com.bonitasoft.engine.api.impl.transaction.RegisterTenantJobListeners;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantActivationException;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantCriterion;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import com.bonitasoft.engine.platform.TenantNotFoundException;
import com.bonitasoft.engine.platform.TenantUpdater;
import com.bonitasoft.engine.platform.TenantUpdater.TenantField;
import com.bonitasoft.engine.profile.ProfilesImporterExt;
import com.bonitasoft.engine.search.SearchTenants;
import com.bonitasoft.engine.search.descriptor.SearchPlatformEntitiesDescriptor;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.manager.Features;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.impl.AvailableOnStoppedNode;
import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.api.impl.transaction.StartServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.StopServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.platform.ActivateTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeactivateTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenant;
import org.bonitasoft.engine.api.impl.transaction.platform.DeleteTenantObjects;
import org.bonitasoft.engine.api.impl.transaction.platform.GetDefaultTenantInstance;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.MissingServiceException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.platform.exception.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.exception.STenantCreationException;
import org.bonitasoft.engine.platform.exception.STenantDeletionException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.profile.ImportPolicy;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class PlatformAPIExt extends PlatformAPIImpl implements PlatformAPI {

    private static final String STATUS_DEACTIVATED = "DEACTIVATED";

    public static final String PROFILES_FILE_SP = "profiles-sp.xml";

    private final LicenseChecker checker;

    public PlatformAPIExt() {
        this(LicenseChecker.getInstance());
    }

    public PlatformAPIExt(final LicenseChecker checker) {
        super();
        this.checker = checker;
    }

    @Override
    protected PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    @Override
    protected TenantServiceAccessor getTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return ServiceAccessorFactory.getInstance().createTenantServiceAccessor(tenantId);
    }

    @Override
    @CustomTransactions
    public final long createTenant(final TenantCreator creator) throws CreationException, AlreadyExistsException {
        checker.checkLicenseAndFeature(Features.CREATE_TENANT);
        PlatformServiceAccessor platformAccessor = null;
        // useless? done in create(Tenantcreator)
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionService transactionService = platformAccessor.getTransactionService();
            final String tenantName = (String) creator.getFields().get(TenantCreator.TenantField.NAME);
            transactionService.executeInTransaction(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    platformService.getTenantByName(tenantName);
                    return null;
                }

            });
            throw new AlreadyExistsException("Tenant named \"" + tenantName + "\" already exists!");
        } catch (final STenantNotFoundException e) {
            // ok
        } catch (final AlreadyExistsException e) {
            throw e;
        } catch (final Exception e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw new CreationException(e);
        }
        creator.setDefaultTenant(false);
        return create(creator);
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void createPlatform() throws CreationException {
        if (!checker.checkLicense()) {
            throw new CreationException("The licence is not valid: " + checker.getErrorMessage());
        }
        super.createPlatform();
    }

    private long create(final TenantCreator creator) throws CreationException {
        final Map<com.bonitasoft.engine.platform.TenantCreator.TenantField, Serializable> tenantFields = creator.getFields();
        Long tenantId = -1L;
        boolean bhTenantCreated = false;
        try {
            final ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            final PlatformServiceAccessor platformAccessor = serviceAccessorFactory.createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionService transactionService = platformAccessor.getTransactionService();

            // add tenant to database
            final STenant tenant = SPModelConvertor.constructTenant(creator);

            tenantId = transactionService.executeInTransaction(new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    return platformService.createTenant(tenant);
                }
            });
            BonitaHomeServer.getInstance().createTenant(tenantId);
            bhTenantCreated = true;
            // modify user name and password
            final String userName = (String) tenantFields.get(com.bonitasoft.engine.platform.TenantCreator.TenantField.USERNAME);
            final String password = (String) tenantFields.get(com.bonitasoft.engine.platform.TenantCreator.TenantField.PASSWORD);
            BonitaHomeServer.getInstance().modifyTechnicalUser(tenant.getId(), userName, password);

            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            final SessionService sessionService = platformAccessor.getSessionService();
            final Long finalTenantId = tenantId;
            final Callable<Long> initializeTenant = new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    SessionAccessor sessionAccessor = null;
                    long platformSessionId = -1;
                    try {
                        // Create session
                        sessionAccessor = serviceAccessorFactory.createSessionAccessor();
                        final SSession session = sessionService.createSession(finalTenantId, -1L, userName, true);
                        platformSessionId = sessionAccessor.getSessionId();
                        sessionAccessor.deleteSessionId();
                        sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());

                        // Create default commands
                        createDefaultCommands(tenantServiceAccessor);

                        // Create default profiles
                        createDefaultProfiles(tenantServiceAccessor);

                        // Create customPage examples
                        createCustomPageExamples(tenantServiceAccessor);

                        // Create default themes
                        getDelegate().createDefaultThemes(tenantServiceAccessor);

                        registerTenantJobListeners(platformAccessor, finalTenantId);

                        sessionService.deleteSession(session.getId());
                        return finalTenantId;
                    } finally {
                        cleanSessionAccessor(sessionAccessor, platformSessionId);
                    }
                }

            };
            return transactionService.executeInTransaction(initializeTenant);
        } catch (final Exception e) {
            if (bhTenantCreated) {
                try {
                    BonitaHomeServer.getInstance().deleteTenant(tenantId);
                } catch (Exception e1) {
                    throw new CreationException("Unable to delete default tenant (after a STenantCreationException) that was being created", e1);
                }
            }
            throw new CreationException("Unable to create tenant " + tenantFields.get(com.bonitasoft.engine.platform.TenantCreator.TenantField.NAME), e);
        }
    }

    private void registerTenantJobListeners(final PlatformServiceAccessor platformServiceAccessor, final Long tenantId) throws SSchedulerException {
        final BroadcastService broadcastService = platformServiceAccessor.getBroadcastService();
        final RegisterTenantJobListeners registerTenantJobListeners = new RegisterTenantJobListeners(tenantId);
        broadcastService.execute(registerTenantJobListeners, tenantId);
    }

    @Override
    protected void importProfiles(final ProfileService profileService, final IdentityService identityService, final List<ExportedProfile> profilesFromXML,
            final org.bonitasoft.engine.service.TenantServiceAccessor tenantServiceAccessor) throws ExecutionException {
        final PageService pageService = ((TenantServiceAccessor) tenantServiceAccessor).getPageService();
        new ProfilesImporterExt(profileService, identityService, pageService, profilesFromXML, ImportPolicy.FAIL_ON_DUPLICATES).importProfiles(-1);
    }

    private void createCustomPageExamples(final TenantServiceAccessor tenantServiceAccessor) throws CreationException {
        try {
            tenantServiceAccessor.getPageService().start();
        } catch (final SBonitaException e) {
            throw new CreationException(e);
        }
    }

    @Override
    @CustomTransactions
    public void deleteTenant(final long tenantId) throws DeletionException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionExecutor transactionExecutor = platformAccessor.getTransactionExecutor();
            final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();

            // delete tenant objects in database
            final TransactionContent transactionContentForTenantObjects = new DeleteTenantObjects(tenantId, platformService);
            transactionExecutor.execute(transactionContentForTenantObjects);

            // delete tenant in database
            final TransactionContent transactionContent = new DeleteTenant(tenantId, platformService);
            transactionExecutor.execute(transactionContent);

            // stop tenant services and clear the spring context:
            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            platformAccessor.getTransactionService().executeInTransaction(new SetServiceState(tenantId, new StopServiceStrategy()));

            logger.log(getClass(), TechnicalLogSeverity.INFO, "Destroy tenant context of tenant " + tenantId);
            tenantServiceAccessor.destroy();

            // delete tenant folder
            BonitaHomeServer.getInstance().deleteTenant(tenantId);
        } catch (final STenantNotFoundException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new DeletionException(e);
        } catch (final SDeletingActivatedTenantException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new DeletionException("Unable to delete an activated tenant " + tenantId);
        } catch (final Exception e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw new DeletionException(e);
        }
    }

    @Override
    public void activateTenant(final long tenantId) throws TenantNotFoundException, TenantActivationException {
        PlatformServiceAccessor platformAccessor = null;
        SessionAccessor sessionAccessor = null;
        long platformSessionId = -1;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final Tenant alreadyActivateTenant = getTenantById(tenantId);
            if ("ACTIVATED".equals(alreadyActivateTenant.getState())) {
                throw new TenantActivationException("Tenant activation: failed. Tenant already activated.");
            }
            final PlatformService platformService = platformAccessor.getPlatformService();
            final SchedulerService schedulerService = platformAccessor.getSchedulerService();
            final SessionService sessionService = platformAccessor.getSessionService();

            final NodeConfiguration nodeConfiguration = platformAccessor.getPlatformConfiguration();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = createSession(tenantId, sessionService);

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();

            sessionAccessor.setSessionInfo(sessionId, tenantId);
            final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor(tenantId);

            final WorkService workService = tenantServiceAccessor.getWorkService();
            final ConnectorExecutor connectorExecutor = tenantServiceAccessor.getConnectorExecutor();

            final ActivateTenant transactionContent = new ActivateTenant(tenantId, platformService, schedulerService,
                    platformAccessor.getTechnicalLoggerService(), workService, connectorExecutor, nodeConfiguration,
                    tenantServiceAccessor.getTenantConfiguration());
            transactionContent.execute();

            final BroadcastService broadcastService = platformAccessor.getBroadcastService();
            final SetServiceState setServiceState = new SetServiceState(tenantId, new StartServiceStrategy());
            final Map<String, TaskResult<Void>> result = broadcastService.execute(setServiceState, tenantId);
            handleResult(result);

            sessionService.deleteSession(sessionId);
        } catch (final TenantNotFoundException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new TenantNotFoundException(tenantId);
        } catch (final TenantActivationException e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw e;
        } catch (final Exception e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw new TenantActivationException("Tenant activation: failed.", e);
        } finally {
            cleanSessionAccessor(sessionAccessor, platformSessionId);
        }
    }

    private void handleResult(final Map<String, TaskResult<Void>> result) throws UpdateException {
        for (final Entry<String, TaskResult<Void>> entry : result.entrySet()) {
            if (entry.getValue().isError()) {
                throw new UpdateException("There is at least one exception on the node " + entry.getKey(), entry.getValue().getThrowable());
            }
            if (entry.getValue().isTimeout()) {
                throw new UpdateException("There is at least one timeout after " + entry.getValue().getTimeout() + " " + entry.getValue().getTimeunit()
                        + " on the node " + entry.getKey());
            }
        }
    }

    private void log(final PlatformServiceAccessor platformAccessor, final Exception e, final TechnicalLogSeverity logSeverity) {
        if (platformAccessor != null) {
            platformAccessor.getTechnicalLoggerService().log(this.getClass(), logSeverity, e);
        } else {
            e.printStackTrace();
        }
    }

    @Override
    public void deactiveTenant(final long tenantId) throws TenantNotFoundException, TenantDeactivationException {
        PlatformServiceAccessor platformAccessor = null;
        SessionAccessor sessionAccessor = null;
        long platformSessionId = -1;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final Tenant alreadyDeactivateTenant = getTenantById(tenantId);
            if (STATUS_DEACTIVATED.equals(alreadyDeactivateTenant.getState())) {
                throw new TenantDeactivationException("Tenant already deactivated.");
            }
            final PlatformService platformService = platformAccessor.getPlatformService();
            final SchedulerService schedulerService = platformAccessor.getSchedulerService();
            final SessionService sessionService = platformAccessor.getSessionService();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = createSession(tenantId, sessionService);

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();
            sessionAccessor.setSessionInfo(sessionId, tenantId);

            final BroadcastService broadcastService = platformAccessor.getBroadcastService();
            final SetServiceState setServiceState = new SetServiceState(tenantId, new StopServiceStrategy());
            final Map<String, TaskResult<Void>> result = broadcastService.execute(setServiceState, tenantId);
            handleResult(result);

            final DeactivateTenant transactionContent = new DeactivateTenant(tenantId, platformService, schedulerService);
            transactionContent.execute();
            sessionService.deleteSession(sessionId);
            sessionService.deleteSessionsOfTenant(tenantId);
        } catch (final TenantNotFoundException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new TenantNotFoundException(tenantId);
        } catch (final Exception e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw new TenantDeactivationException("Tenant deactivation failed.", e);
        } finally {
            cleanSessionAccessor(sessionAccessor, platformSessionId);
        }
    }

    @Override
    public List<Tenant> getTenants(final int startIndex, final int maxResults, final TenantCriterion pagingCriterion) {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new RetrieveException(e);
        }
        try {
            final PlatformService platformService = platformAccessor.getPlatformService();
            final STenantBuilderFactory tenantBuildeFactr = BuilderFactory.get(STenantBuilderFactory.class);
            String field = null;
            OrderByType order = null;
            switch (pagingCriterion) {
                case NAME_ASC:
                    field = tenantBuildeFactr.getNameKey();
                    order = OrderByType.ASC;
                    break;
                case DESCRIPTION_ASC:
                    field = tenantBuildeFactr.getDescriptionKey();
                    order = OrderByType.ASC;
                    break;
                case CREATION_ASC:
                    field = tenantBuildeFactr.getCreatedKey();
                    order = OrderByType.ASC;
                    break;
                case STATE_ASC:
                    field = tenantBuildeFactr.getStatusKey();
                    order = OrderByType.ASC;
                    break;
                case NAME_DESC:
                    field = tenantBuildeFactr.getNameKey();
                    order = OrderByType.DESC;
                    break;
                case DESCRIPTION_DESC:
                    field = tenantBuildeFactr.getDescriptionKey();
                    order = OrderByType.DESC;
                    break;
                case CREATION_DESC:
                    field = tenantBuildeFactr.getCreatedKey();
                    order = OrderByType.DESC;
                    break;
                case STATE_DESC:
                    field = tenantBuildeFactr.getStatusKey();
                    order = OrderByType.DESC;
                    break;
                case DEFAULT:
                    field = tenantBuildeFactr.getCreatedKey();
                    order = OrderByType.DESC;
                    break;
            }
            final String fieldContent = field;
            final OrderByType orderContent = order;
            final TransactionContentWithResult<List<STenant>> transactionContent = new GetTenantsWithOrder(platformService, startIndex, maxResults,
                    orderContent, fieldContent);
            transactionContent.execute();
            final List<STenant> tenants = transactionContent.getResult();
            return SPModelConvertor.toTenants(tenants);
        } catch (final SBonitaException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new RetrieveException(e);
        }
    }

    @Override
    public Tenant getTenantByName(final String tenantName) throws TenantNotFoundException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final GetTenantInstance transactionContent = new GetTenantInstance(tenantName, platformService);
            transactionContent.execute();
            return SPModelConvertor.toTenant(transactionContent.getResult());
        } catch (final STenantNotFoundException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new TenantNotFoundException("No tenant exists with name: " + tenantName);
        } catch (final SBonitaException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new TenantNotFoundException("Unable to retrieve the tenant with name " + tenantName);
        } catch (final Exception e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw new TenantNotFoundException("Unable to retrieve the tenant with name " + tenantName);
        }
    }

    @Override
    public Tenant getDefaultTenant() throws TenantNotFoundException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final GetDefaultTenantInstance transactionContent = new GetDefaultTenantInstance(platformService);
            transactionContent.execute();
            return SPModelConvertor.toTenant(transactionContent.getResult());
        } catch (final SBonitaException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new TenantNotFoundException("Unable to retrieve the defaultTenant");
        } catch (final Exception e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw new TenantNotFoundException("Unable to retrieve the defaultTenant");
        }
    }

    @Override
    public Tenant getTenantById(final long tenantId) throws TenantNotFoundException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final GetTenantInstance transactionContent = new GetTenantInstance(tenantId, platformService);
            transactionContent.execute();
            return SPModelConvertor.toTenant(transactionContent.getResult());
        } catch (final STenantNotFoundException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new TenantNotFoundException("No tenant exists with id: " + tenantId);
        } catch (final SBonitaException e) {
            log(platformAccessor, e, TechnicalLogSeverity.DEBUG);
            throw new TenantNotFoundException("Unable to retrieve the tenant with id " + tenantId);
        } catch (final Exception e) {
            log(platformAccessor, e, TechnicalLogSeverity.ERROR);
            throw new TenantNotFoundException("Unable to retrieve the tenant with id " + tenantId);
        }
    }

    @Override
    public int getNumberOfTenants() {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        try {
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionContentWithResult<Integer> transactionContent = new GetNumberOfTenants(platformService);
            transactionContent.execute();
            return transactionContent.getResult();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Tenant updateTenant(final long tenantId, final TenantUpdater udpater) throws UpdateException {
        if (udpater == null || udpater.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final PlatformService platformService = getPlatformService();
        // check existence for tenant
        try {
            final STenant tenant = platformService.getTenant(tenantId);
            // update user name and password in file system
            final Map<TenantField, Serializable> updatedFields = udpater.getFields();
            final String username = (String) updatedFields.get(TenantField.USERNAME);
            final String password = (String) updatedFields.get(TenantField.PASSWOWRD);
            if (username != null || password != null) {
                BonitaHomeServer.getInstance().modifyTechnicalUser(tenantId, username, password);
            }
            // update tenant in database
            final STenantUpdateBuilder tenantUpdateBuilder = getTenantUpdateDescriptor(udpater);
            platformService.updateTenant(tenant, tenantUpdateBuilder.done());
            return SPModelConvertor.toTenant(tenant);
        } catch (final BonitaHomeNotSetException e) {
            throw new UpdateException(e);
        } catch (final IOException e) {
            throw new UpdateException(e);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        } catch (final Exception e) {
            throw new UpdateException(e);
        }
    }

    private STenantUpdateBuilder getTenantUpdateDescriptor(final TenantUpdater tenantUpdater) {
        final STenantUpdateBuilderFactory updateBuilderFactory = BuilderFactory.get(STenantUpdateBuilderFactory.class);
        final STenantUpdateBuilder updateDescriptor = updateBuilderFactory.createNewInstance();

        final Map<TenantField, Serializable> fields = tenantUpdater.getFields();
        for (final Entry<TenantField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    updateDescriptor.setName((String) field.getValue());
                    break;
                case DESCRIPTION:
                    updateDescriptor.setDescription((String) field.getValue());
                    break;
                case ICON_NAME:
                    updateDescriptor.setIconName((String) field.getValue());
                    break;
                case ICON_PATH:
                    updateDescriptor.setIconPath((String) field.getValue());
                    break;
                case STATUS:
                    updateDescriptor.setStatus((String) field.getValue());
                    break;
                default:
                    break;
            }
        }
        return updateDescriptor;
    }

    @Override
    public SearchResult<Tenant> searchTenants(final SearchOptions searchOptions) {
        final PlatformService platformService = getPlatformService();
        final SearchPlatformEntitiesDescriptor searchPlatformEntitiesDescriptor = getPlatformAccessorNoException().getSearchPlatformEntitiesDescriptor();
        final SearchTenants searchTenants = new SearchTenants(platformService, searchPlatformEntitiesDescriptor.getSearchTenantDescriptor(), searchOptions);
        try {
            searchTenants.execute();
            return searchTenants.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void startNode() throws StartNodeException {
        checker.checkLicense();
        super.startNode();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void stopNode() throws StopNodeException {
        super.stopNode();
        try {
            final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            final BroadcastService broadcastService = platformAccessor.getBroadcastService();
            broadcastService.submit(new NotifyNodeStoppedTask());
        } catch (final Exception e) {
            throw new StopNodeException(e);
        }
    }

    public void stopNode(final String message) throws StopNodeException {
        super.stopNode();
        try {
            final PlatformServiceAccessor platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final TechnicalLoggerService loggerService = platformAccessor.getTechnicalLoggerService();
            if (loggerService.isLoggable(PlatformAPIExt.class, TechnicalLogSeverity.ERROR)) {
                loggerService.log(PlatformAPIExt.class, TechnicalLogSeverity.ERROR, "The engine was stopped due to '" + message + "'.");
            }
        } catch (final BonitaHomeNotSetException bhnse) {
            throw new StopNodeException(bhnse);
        } catch (final BonitaHomeConfigurationException bhce) {
            throw new StopNodeException(bhce);
        } catch (final InstantiationException ie) {
            throw new StopNodeException(ie);
        } catch (final IllegalAccessException iae) {
            throw new StopNodeException(iae);
        } catch (final ClassNotFoundException cnfe) {
            throw new StopNodeException(cnfe);
        } catch (final IOException ioe) {
            throw new StopNodeException(ioe);
        }
    }

    @Override
    protected String getProfileFileName() {
        return PROFILES_FILE_SP;
    }

    protected PlatformService getPlatformService() {
        final PlatformServiceAccessor platformAccessor = getPlatformAccessorNoException();
        final PlatformService platformService = platformAccessor.getPlatformService();
        return platformService;
    }

    protected PlatformServiceAccessor getPlatformAccessorNoException() {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        return platformAccessor;
    }

    // @Override
    protected void startServices(final TechnicalLoggerService logger, final long tenantId,
            final org.bonitasoft.engine.service.TenantServiceAccessor tenantServiceAccessor)
            throws SBonitaException {
        // super.startServices(logger, tenantId, tenantServiceAccessor);
        tenantServiceAccessor.getTransactionExecutor().execute(new TransactionContent() {

            @Override
            public void execute() throws SBonitaException {
                PageService pageService;
                try {
                    pageService = ((TenantServiceAccessor) tenantServiceAccessor).getPageService();
                } catch (final MissingServiceException e) {
                    // if not in the configuration just ignore it
                    return;
                }
                pageService.start();
            }
        });
    }

}
