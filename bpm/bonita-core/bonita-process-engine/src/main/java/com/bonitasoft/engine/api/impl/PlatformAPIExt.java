/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.impl.AvailableOnStoppedNode;
import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
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
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.STenantCreationException;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.impl.transaction.GetNumberOfTenants;
import com.bonitasoft.engine.api.impl.transaction.GetTenantsWithOrder;
import com.bonitasoft.engine.api.impl.transaction.UpdateTenant;
import com.bonitasoft.engine.api.impl.transaction.reporting.AddReport;
import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.core.reporting.SReportBuilder;
import com.bonitasoft.engine.core.reporting.SReportBuilderFactory;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantActivationException;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantCriterion;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import com.bonitasoft.engine.platform.TenantNotFoundException;
import com.bonitasoft.engine.platform.TenantUpdater;
import com.bonitasoft.engine.platform.TenantUpdater.TenantField;
import com.bonitasoft.engine.search.SearchTenants;
import com.bonitasoft.engine.search.descriptor.SearchPlatformEntitiesDescriptor;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.manager.Features;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class PlatformAPIExt extends PlatformAPIImpl implements PlatformAPI {

    private static final String STATUS_DEACTIVATED = "DEACTIVATED";

    private static final String PROFILES_FILE_SP = "profiles-sp.xml";

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
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CREATE_TENANT);
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
    public void initializePlatform() throws CreationException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
        } catch (final Exception e) {
            throw new CreationException(e);
        }
        // 1 tx to create content and default tenant
        super.initializePlatform();
        final TransactionService transactionService = platformAccessor.getTransactionService();
        final long tenantId;
        try {
            tenantId = transactionService.executeInTransaction(new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    return getDefaultTenant().getId();
                }
            });

        } catch (final TenantNotFoundException e) {
            throw new CreationException(e);
        } catch (final Exception e) {
            throw new CreationException(e);
        }
        final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);

        long platformSessionId = -1;
        SessionAccessor sessionAccessor = null;
        try {
            final ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            sessionAccessor = serviceAccessorFactory.createSessionAccessor();
            final SessionService sessionService = platformAccessor.getSessionService();
            final SSession session = sessionService.createSession(tenantId, -1L, "dummy", true);

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();

            sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());

            // This part is specific to SP: reporting.
            transactionService.executeInTransaction(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    deployTenantReports(tenantId, tenantServiceAccessor);
                    return null;
                }

            });
        } catch (final Exception e) {
            throw new CreationException(e);
        } finally {
            cleanSessionAccessor(sessionAccessor, platformSessionId);
        }
    }

    private long create(final TenantCreator creator) throws CreationException {
        final Map<com.bonitasoft.engine.platform.TenantCreator.TenantField, Serializable> tenantFields = creator.getFields();
        try {
            final ServiceAccessorFactory serviceAccessorFactory = ServiceAccessorFactory.getInstance();
            final PlatformServiceAccessor platformAccessor = serviceAccessorFactory.createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionService transactionService = platformAccessor.getTransactionService();

            // add tenant to database
            final STenant tenant = SPModelConvertor.constructTenant(creator);

            final Long tenantId = transactionService.executeInTransaction(new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    return platformService.createTenant(tenant);
                }
            });

            // add tenant folder
            String targetDir = null;
            String sourceDir = null;
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
            // modify user name and password
            final String userName = (String) tenantFields.get(com.bonitasoft.engine.platform.TenantCreator.TenantField.USERNAME);
            try {
                final String password = (String) tenantFields.get(com.bonitasoft.engine.platform.TenantCreator.TenantField.PASSWORD);
                modifyTechnicalUser(tenant.getId(), userName, password);
            } catch (final Exception e) {
                IOUtil.deleteDir(new File(targetDir));
                deleteTenant(tenant.getId());
                throw new STenantCreationException("Modify File Exception!");
            }

            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            final SessionService sessionService = platformAccessor.getSessionService();
            final Callable<Long> initializeTenant = new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    SessionAccessor sessionAccessor = null;
                    long platformSessionId = -1;
                    try {
                        // Create session
                        sessionAccessor = serviceAccessorFactory.createSessionAccessor();
                        final SSession session = sessionService.createSession(tenantId, -1L, userName, true);
                        platformSessionId = sessionAccessor.getSessionId();
                        sessionAccessor.deleteSessionId();
                        sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());

                        // Create default data source
                        createDefaultDataSource(tenantServiceAccessor);

                        // Create default commands
                        createDefaultCommands(tenantServiceAccessor);

                        // Create default profiles
                        createDefaultProfiles(tenantServiceAccessor);

                        // Create default theme
                        createDefaultTheme(tenantServiceAccessor);

                        sessionService.deleteSession(session.getId());
                        return tenantId;
                    } finally {
                        cleanSessionAccessor(sessionAccessor, platformSessionId);
                    }
                }
            };
            return transactionService.executeInTransaction(initializeTenant);
        } catch (final Exception e) {
            throw new CreationException("Unable to create tenant " + tenantFields.get(com.bonitasoft.engine.platform.TenantCreator.TenantField.NAME), e);
        }
    }

    private void deployTenantReports(final long tenantId, final TenantServiceAccessor tenantAccessor) throws IOException, BonitaHomeNotSetException,
            SBonitaException {
        final String reportFolder = BonitaHomeServer.getInstance().getTenantReportFolder(tenantId);
        final String reportListFilename = reportFolder + File.separator + "reports.lst";
        final File reportListFile = new File(reportListFilename);
        if (!reportListFile.exists()) {
            return;
        }
        final Properties properties = PropertiesManager.getProperties(reportListFile);
        for (final Entry<Object, Object> reports : properties.entrySet()) {
            final String reportName = (String) reports.getKey();
            final String reportDescription = (String) reports.getValue();
            final byte[] content = getReportContent(reportFolder, reportName);
            final byte[] screenshot = getReportScreenshot(reportFolder, reportName);

            final ReportingService reportingService = tenantAccessor.getReportingService();
            final SReportBuilder reportBuilder = BuilderFactory.get(SReportBuilderFactory.class).createNewInstance(reportName, /* system user */-1, true,
                    reportDescription, screenshot);
            final AddReport addReport = new AddReport(reportingService, reportBuilder.done(), content);
            // Here we are already in a transaction, so we can call execute() directly:
            addReport.execute();
        }
    }

    /**
     * Get the binary content of a report, from its name.
     * 
     * @param reportFolder
     *            the folder where to look.
     * @param reportName
     *            the name of the report. The content must match <report_name>-content* to be recognized as the report content.
     * @return the binary content, if found, null otherwise. If several report contents match this pattern, the first one
     * @throws IOException
     *             if an I/O error occurs while reading the report content.
     */
    protected byte[] getReportContent(final String reportFolder, final String reportName) throws IOException {
        final File[] fileContents = new File(reportFolder).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return name.startsWith(reportName + "-content");
            }
        });
        if (fileContents.length > 0) {
            return IOUtil.getAllContentFrom(fileContents[0].getAbsoluteFile());
        } else {
            return null;
        }
    }

    /**
     * Get the binary screenshot of a report, from its name.
     * 
     * @param reportFolder
     *            the folder where to look.
     * @param reportName
     *            the name of the report. The screenshot must match <report_name>-screenshot* to be recognized as the report screenshot.
     * @return the binary screenshot, if found, null otherwise. If several report screenshots match this pattern, the first one
     * @throws IOException
     *             if an I/O error occurs while reading the report screenshot.
     */
    protected byte[] getReportScreenshot(final String reportFolder, final String reportName) throws IOException {
        final File[] filescreenshots = new File(reportFolder).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return name.startsWith(reportName + "-screenshot");
            }
        });
        if (filescreenshots.length > 0) {
            return IOUtil.getAllContentFrom(filescreenshots[0].getAbsoluteFile());
        } else {
            return null;
        }
    }

    // modify user name and password
    private void modifyTechnicalUser(final long tenantId, final String userName, final String password) throws IOException, BonitaHomeNotSetException {
        final String tenantPath = BonitaHomeServer.getInstance().getTenantConfFolder(tenantId) + File.separator + "bonita-server.properties";
        final File file = new File(tenantPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        final Properties properties = PropertiesManager.getProperties(file);
        if (userName != null) {
            properties.setProperty("userName", userName);
        }
        if (password != null) {
            properties.setProperty("userPassword", password);
        }
        PropertiesManager.saveProperties(properties, file);
    }

    @Override
    @CustomTransactions
    public void deleteTenant(final long tenantId) throws DeletionException {
        PlatformServiceAccessor platformAccessor = null;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionExecutor transactionExecutor = platformAccessor.getTransactionExecutor();

            // delete tenant objects in database
            final TransactionContent transactionContentForTenantObjects = new DeleteTenantObjects(tenantId, platformService);
            transactionExecutor.execute(transactionContentForTenantObjects);

            // delete tenant in database
            final TransactionContent transactionContent = new DeleteTenant(tenantId, platformService);
            transactionExecutor.execute(transactionContent);

            // delete tenant folder
            final String targetDir = BonitaHomeServer.getInstance().getTenantsFolder() + File.separator + tenantId;
            IOUtil.deleteDir(new File(targetDir));
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
            final WorkService workService = platformAccessor.getWorkService();
            final NodeConfiguration plaformConfiguration = platformAccessor.getPlaformConfiguration();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = createSession(tenantId, sessionService);

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();

            sessionAccessor.setSessionInfo(sessionId, tenantId);
            final TransactionContent transactionContent = new ActivateTenant(tenantId, platformService, schedulerService, plaformConfiguration,
                    platformAccessor.getTechnicalLoggerService(), workService);
            transactionContent.execute();
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
            final WorkService workService = platformAccessor.getWorkService();
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = createSession(tenantId, sessionService);

            platformSessionId = sessionAccessor.getSessionId();
            sessionAccessor.deleteSessionId();

            sessionAccessor.setSessionInfo(sessionId, tenantId);
            final TransactionContent transactionContent = new DeactivateTenant(tenantId, platformService, schedulerService, workService, sessionService);
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
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        // check existence for tenant
        Tenant tenant;
        try {
            tenant = getTenantById(tenantId);
            // update user name and password in file system
            final Map<TenantField, Serializable> updatedFields = udpater.getFields();
            final String username = (String) updatedFields.get(TenantField.USERNAME);
            final String password = (String) updatedFields.get(TenantField.PASSWOWRD);
            if (username != null || password != null) {
                modifyTechnicalUser(tenantId, username, password);
            }
            // update tenant in database
            final EntityUpdateDescriptor changeDescriptor = getTenantUpdateDescriptor(udpater);
            final UpdateTenant updateTenant = new UpdateTenant(tenant.getId(), changeDescriptor, platformService);
            updateTenant.execute();
            return getTenantById(tenantId);
        } catch (final TenantNotFoundException e) {
            throw new UpdateException(e);
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

    private EntityUpdateDescriptor getTenantUpdateDescriptor(final TenantUpdater udpateDescriptor) {
        final STenantBuilderFactory tenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);

        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final Map<TenantField, Serializable> fields = udpateDescriptor.getFields();
        for (final Entry<TenantField, Serializable> field : fields.entrySet()) {
            switch (field.getKey()) {
                case NAME:
                    descriptor.addField(tenantBuilderFact.getNameKey(), field.getValue());
                    break;
                case DESCRIPTION:
                    descriptor.addField(tenantBuilderFact.getDescriptionKey(), field.getValue());
                    break;
                case ICON_NAME:
                    descriptor.addField(tenantBuilderFact.getIconNameKey(), field.getValue());
                    break;
                case ICON_PATH:
                    descriptor.addField(tenantBuilderFact.getIconPathKey(), field.getValue());
                    break;
                case STATUS:
                    descriptor.addField(tenantBuilderFact.getStatusKey(), field.getValue());
                    break;
                default:
                    break;
            }
        }
        return descriptor;
    }

    @Override
    public SearchResult<Tenant> searchTenants(final SearchOptions searchOptions) throws SearchException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        final PlatformService platformService = platformAccessor.getPlatformService();
        final SearchPlatformEntitiesDescriptor searchPlatformEntitiesDescriptor = platformAccessor.getSearchPlatformEntitiesDescriptor();
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
        LicenseChecker.getInstance().checkLicence();
        super.startNode();
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

}
