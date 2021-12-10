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
package org.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.platform.GetPlatformContent;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.login.TechnicalUser;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformManager;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.platform.exception.SDeletingActivatedTenantException;
import org.bonitasoft.engine.platform.exception.STenantCreationException;
import org.bonitasoft.engine.platform.exception.STenantException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.profile.DefaultProfilesUpdater;
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

    protected PlatformServiceAccessor getPlatformAccessor()
            throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void startNode() throws StartNodeException {
        final PlatformServiceAccessor platformAccessor;
        PlatformManager platformManager;
        try {
            platformAccessor = getPlatformAccessor();
            platformManager = platformAccessor.getPlatformManager();
        } catch (final Exception e) {
            throw new StartNodeException(e);
        }
        boolean isStarted;
        try {
            isStarted = platformManager.start();
        } catch (final Exception e) {
            throw new StartNodeException("Platform starting failed.", e);
        }
        if (!isStarted) {
            throw new StartNodeException(
                    "Platform is in state " + platformManager.getState() + " and cannot be started");
        }
    }

    SessionAccessor createSessionAccessor()
            throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createSessionAccessor();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void stopNode() throws StopNodeException {
        try {
            final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            platformAccessor.getPlatformManager().stop();
        } catch (final StopNodeException e) {
            throw e;
        } catch (final Exception e) {
            throw new StopNodeException(e);
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
                    platformService.deactivateTenant(sTenant.getId());
                }
                return tenants;
            });
            for (STenant sTenant : sTenants) {
                deleteTenant(sTenant.getId());
            }
        } catch (DeletionException e) {
            throw e;
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
        return ModelConvertor.toPlatform(sPlatform, platformAccessor.getPlatformService().getSPlatformProperties());
    }

    private void createDefaultTenant(final PlatformServiceAccessor platformAccessor,
            final PlatformService platformService,
            final TransactionService transactionService) throws STenantCreationException {
        final String tenantName = "default";
        final String description = "Default tenant";
        SessionAccessor sessionAccessor = null;
        long platformSessionId = -1;
        Long tenantId = -1L;
        try {
            // add tenant to database
            final String createdBy = "defaultUser";
            final STenant tenant = STenant.builder().name(tenantName).createdBy(createdBy)
                    .created(System.currentTimeMillis()).status(STenant.ACTIVATED).defaultTenant(true)
                    .description(description).build();
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
            new DefaultProfilesUpdater(tenantServiceAccessor).execute();
            // Create custom page examples: done by page service start
            // Import default applications done in ApplicationImporter.init()
            sessionService.deleteSession(session.getId());
        } catch (final STenantCreationException e) {
            if (tenantId != -1L) {
                try {
                    deleteTenant(tenantId);
                } catch (DeletionException ex) {
                    throw new STenantCreationException(
                            "Unable to delete default tenant (after a STenantCreationException) that was being created",
                            ex);
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
        getBonitaHomeServer().createTenant(tenantId);
    }

    protected void cleanSessionAccessor(final SessionAccessor sessionAccessor, final long platformSessionId) {
        if (sessionAccessor != null) {
            sessionAccessor.deleteSessionId();
            if (platformSessionId != -1) {
                sessionAccessor.setSessionInfo(platformSessionId, -1);
            }
        }
    }

    @CustomTransactions
    protected void deleteTenant(final long tenantId) throws DeletionException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = getPlatformAccessor();
            final PlatformService platformService = platformAccessor.getPlatformService();
            final TransactionService transactionService = platformAccessor.getTransactionService();
            final TechnicalLoggerService logger = platformAccessor.getTechnicalLoggerService();

            final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);
            tenantServiceAccessor.getTenantStateManager().stop();
            tenantServiceAccessor.destroy();

            // delete tenant objects in database
            transactionService.executeInTransaction(() -> {
                platformService.deleteTenantObjects(tenantId);
                return null;
            });

            // delete tenant in database
            transactionService.executeInTransaction(() -> {
                platformService.deleteTenant(tenantId);
                return null;
            });

            logger.log(getClass(), TechnicalLogSeverity.INFO, "Destroy tenant context of tenant " + tenantId);

            // delete tenant folder
            getBonitaHomeServer().deleteTenant(tenantId);
        } catch (final SDeletingActivatedTenantException e) {
            throw new DeletionException("Unable to delete an activated tenant " + tenantId);
        } catch (final DeletionException e) {
            throw e;
        } catch (final Exception e) {
            throw new DeletionException(e);
        }
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public boolean isPlatformInitialized() throws PlatformNotFoundException {
        try {
            PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            PlatformService platformService = platformAccessor.getPlatformService();
            return platformAccessor.getTransactionService()
                    .executeInTransaction(platformService::isDefaultTenantCreated);
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
        try {
            return getPlatformAccessor().getPlatformManager().getState();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return true if the current node is started, false otherwise
     */
    @Override
    @AvailableOnStoppedNode
    public boolean isNodeStarted() {
        return getPlatformState() == PlatformState.STARTED;
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
        return getBonitaHomeServer().getClientPlatformConfigurations();
    }

    @Override
    public Map<Long, Map<String, byte[]>> getClientTenantConfigurations() {
        try {
            PlatformService platformService = getPlatformAccessor().getPlatformService();
            List<STenant> tenants = platformService.getTenants(QueryOptions.countQueryOptions());
            HashMap<Long, Map<String, byte[]>> conf = new HashMap<>();
            for (STenant tenant : tenants) {
                conf.put(tenant.getId(), getBonitaHomeServer().getTenantPortalConfigurations(tenant.getId()));
            }
            return conf;
        } catch (BonitaException | IOException | IllegalAccessException | ClassNotFoundException
                | InstantiationException | STenantException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public byte[] getClientTenantConfiguration(long tenantId, String file) {
        return getBonitaHomeServer().getTenantPortalConfiguration(tenantId, file);
    }

    protected BonitaHomeServer getBonitaHomeServer() {
        return BonitaHomeServer.getInstance();
    }

    @Override
    public void updateClientTenantConfigurationFile(long tenantId, String file, byte[] content) throws UpdateException {
        getBonitaHomeServer().updateTenantPortalConfigurationFile(tenantId, file, content);
    }
}
