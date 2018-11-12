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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.impl.transaction.PauseServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.ResumeServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.ServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.execution.work.TenantRestarter;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.resources.STenantResourceLight;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.TenantResource;
import org.bonitasoft.engine.tenant.TenantResourceType;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */

public class TenantAdministrationAPIImpl implements TenantAdministrationAPI {

    protected PlatformServiceAccessor getPlatformAccessorNoException() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected long getTenantId() {
        try {
            return getSessionAccessor().getTenantId();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused
    public boolean isPaused() {
        final long tenantId = getTenantId();
        try {
            return getPlatformAccessorNoException().getPlatformService().getTenant(tenantId).isPaused();
        } catch (final SBonitaException e) {
            throw new RetrieveException("Unable to retrieve the tenant with id " + tenantId, e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused
    public void pause() throws UpdateException {
        setTenantPaused(true);
    }

    @Override
    @AvailableWhenTenantIsPaused
    public void resume() throws UpdateException {
        setTenantPaused(false);
        resolveDependenciesForAllProcesses();
    }

    private void resolveDependenciesForAllProcesses() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        tenantAccessor.getBusinessArchiveArtifactsManager().resolveDependenciesForAllProcesses(tenantAccessor);
    }

    private void setTenantPaused(final boolean shouldBePaused) throws UpdateException {
        final PlatformServiceAccessor platformServiceAccessor = getPlatformAccessorNoException();
        final PlatformService platformService = platformServiceAccessor.getPlatformService();

        final long tenantId = getTenantId();
        try {
            final STenant tenant = platformService.getTenant(tenantId);

            if (shouldBePaused && !STenant.ACTIVATED.equals(tenant.getStatus()) || !shouldBePaused && !STenant.PAUSED.equals(tenant.getStatus())) {
                throw new UpdateException("Can't " + (shouldBePaused ? "pause" : "resume") + " a tenant in state " + tenant.getStatus());
            }

            final STenantUpdateBuilderFactory updateBuilderFactory = BuilderFactory.get(STenantUpdateBuilderFactory.class);
            final STenantUpdateBuilder updateDescriptor = updateBuilderFactory.createNewInstance();
            if (shouldBePaused) {
                updateDescriptor.setStatus(STenant.PAUSED);
                pauseServicesForTenant(platformServiceAccessor, tenantId);
            } else {
                updateDescriptor.setStatus(STenant.ACTIVATED);
                resumeServicesForTenant(platformServiceAccessor, tenantId);
            }
            updateTenant(platformService, updateDescriptor, tenant);
        } catch (final STenantNotFoundException e) {
            throw new UpdateException("Tenant does not exist", e);
        }
    }

    @AvailableWhenTenantIsPaused
    protected void pauseServicesForTenant(final PlatformServiceAccessor platformServiceAccessor, final long tenantId)
            throws UpdateException {
        // clustered services
        try {
            pauseScheduler(platformServiceAccessor, tenantId);
            deleteSessionsOfTenantExceptTechnicalUser(platformServiceAccessor, tenantId);

            // on all nodes
            setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(platformServiceAccessor, tenantId, new PauseServiceStrategy());
        } catch (final SSchedulerException e) {
            throw new UpdateException("Unable to pause the scheduler.", e);
        }
    }

    private void deleteSessionsOfTenantExceptTechnicalUser(final PlatformServiceAccessor platformServiceAccessor, final long tenantId) {
        final SessionService sessionService = platformServiceAccessor.getTenantServiceAccessor(tenantId).getSessionService();
        sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
    }

    private void resumeServicesForTenant(final PlatformServiceAccessor platformServiceAccessor, final long tenantId)
            throws UpdateException {
        // clustered services
        try {
            final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);
            TenantRestarter tenantRestarter = new TenantRestarter(platformServiceAccessor, tenantServiceAccessor);
            List<TenantRestartHandler> tenantRestartHandlers = tenantRestarter.executeBeforeServicesStart();
            resumeScheduler(platformServiceAccessor, tenantId);

            // on all nodes
            setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(platformServiceAccessor, tenantId, new ResumeServiceStrategy());

            tenantRestarter
                    .executeAfterServicesStartAfterCurrentTransaction(tenantRestartHandlers);
        } catch (final RestartException e) {
            throw new UpdateException("Unable to resume all elements of the work service.", e);
        } catch (final SSchedulerException e) {
            throw new UpdateException("Unable to resume the scheduler.", e);
        } catch (STransactionNotFoundException e) {
            throw new UpdateException("Unable to resume the tenant restart handlers.", e);
        }
    }

    // In package private for unit tests
    void setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(final PlatformServiceAccessor platformServiceAccessor, final long tenantId,
            final ServiceStrategy serviceStrategy) throws UpdateException {
        final BroadcastService broadcastService = platformServiceAccessor.getBroadcastService();
        final SetServiceState setServiceState = new SetServiceState(tenantId, serviceStrategy);
        final Map<String, TaskResult<Void>> result;
        try {
            execute(setServiceState);
            result = broadcastService.executeOnOthersAndWait(setServiceState, tenantId);
        } catch (Exception e) {
            throw new UpdateException(e);
        }
        handleResult(result);
    }

    void execute(SetServiceState setServiceState) throws Exception {
        setServiceState.call();
    }

    private void pauseScheduler(final PlatformServiceAccessor platformServiceAccessor, final long tenantId) throws SSchedulerException {
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        schedulerService.pauseJobs(tenantId);
    }

    private void resumeScheduler(final PlatformServiceAccessor platformServiceAccessor, final long tenantId) throws SSchedulerException {
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        schedulerService.resumeJobs(tenantId);
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

    protected void updateTenant(final PlatformService platformService, final STenantUpdateBuilder descriptor, final STenant tenant) throws UpdateException {
        try {
            platformService.updateTenant(tenant, descriptor.done());
        } catch (final SBonitaException e) {
            throw new UpdateException("Could not update the tenant pause mode", e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused
    public TenantResource getBusinessDataModelResource() {
        return getTenantResource(TenantResourceType.BDM);
    }

    protected TenantResource getTenantResource(TenantResourceType type) {
        TenantResourcesService tenantResourcesService = getTenantAccessor().getTenantResourcesService();
        org.bonitasoft.engine.resources.TenantResourceType resourceType = org.bonitasoft.engine.resources.TenantResourceType
                .valueOf(type.name());
        try {
            STenantResourceLight tenantResource = tenantResourcesService.getSingleLightResource(resourceType);
            return ModelConvertor.toTenantResource(tenantResource);
        } catch (SBonitaReadException e) {
            return TenantResource.NONE;
        }
    }

    //visible for testing
    @Override
    @AvailableWhenTenantIsPaused
    public String getBusinessDataModelVersion() throws BusinessDataRepositoryException {
        try {
            final BusinessDataModelRepository modelRepository = getTenantAccessor().getBusinessDataModelRepository();
            return modelRepository.getInstalledBDMVersion();
        } catch (final SBusinessDataRepositoryException e) {
            throw new BusinessDataRepositoryException(e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused(only = true)
    public String installBusinessDataModel(final byte[] zip) throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final long userId;
        try {
            userId = getUserId();
        } catch (IllegalStateException e) {
            throw new BusinessDataRepositoryDeploymentException("Unable to determine user ID");
        }
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            return bdmRepository.install(zip, tenantAccessor.getTenantId(), userId);
        } catch (final IllegalStateException e) {
            throw new InvalidBusinessDataModelException(e);
        } catch (final SBusinessDataRepositoryDeploymentException e) {
            throw new BusinessDataRepositoryDeploymentException(e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused(only = true)
    public void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            bdmRepository.uninstall(tenantAccessor.getTenantId());
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused(only = true)
    public void cleanAndUninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            bdmRepository.dropAndUninstall(tenantAccessor.getTenantId());
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused
    public byte[] getClientBDMZip() throws BusinessDataRepositoryException {
        final BusinessDataModelRepository bdmRepository = getTenantAccessor().getBusinessDataModelRepository();
        try {
            return bdmRepository.getClientBDMZip();
        } catch (final SBusinessDataRepositoryException e) {
            throw new BusinessDataRepositoryException(e);
        }
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            return TenantServiceSingleton.getInstance(getTenantId());
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private SessionAccessor getSessionAccessor() throws IllegalAccessException, InstantiationException, IOException, ClassNotFoundException, BonitaHomeConfigurationException, BonitaHomeNotSetException {
        return ServiceAccessorFactory.getInstance().createSessionAccessor();
    }

    protected long getUserId() throws IllegalStateException {
        try {
            return getTenantAccessor().getSessionService().getSession(getSessionAccessor().getSessionId()).getUserId();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e.getMessage());
        }
    }
}
