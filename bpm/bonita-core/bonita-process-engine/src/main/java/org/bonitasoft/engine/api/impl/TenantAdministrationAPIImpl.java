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

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.business.data.*;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.resources.STenantResourceLight;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.TenantResource;
import org.bonitasoft.engine.tenant.TenantResourceType;
import org.bonitasoft.engine.tenant.TenantStateManager;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */

@Slf4j
public class TenantAdministrationAPIImpl implements TenantAdministrationAPI {

    protected ServiceAccessor getServiceAccessorNoException() {
        try {
            return ServiceAccessorFactory.getInstance().createServiceAccessor();
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
    @AvailableInMaintenanceMode
    public boolean isPaused() {
        final long tenantId = getTenantId();
        try {
            return getServiceAccessorNoException().getPlatformService().getDefaultTenant().isPaused();
        } catch (final SBonitaException e) {
            throw new RetrieveException("Unable to retrieve the tenant with id " + tenantId, e);
        }
    }

    @Override
    @AvailableInMaintenanceMode
    @CustomTransactions
    public void pause() throws UpdateException {
        ServiceAccessor serviceAccessor = getServiceAccessorNoException();
        try {
            serviceAccessor.getTenantStateManager().pause();
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    @Override
    @AvailableInMaintenanceMode
    @CustomTransactions
    public void resume() throws UpdateException {
        ServiceAccessor serviceAccessor = getServiceAccessorNoException();
        try {
            serviceAccessor.getTenantStateManager().resume();
            serviceAccessor.getUserTransactionService().executeInTransaction(() -> {
                resolveDependenciesForAllProcesses();
                return null;
            });
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    private void resolveDependenciesForAllProcesses() {
        getServiceAccessor().getBusinessArchiveArtifactsManager()
                .resolveDependenciesForAllProcesses(getServiceAccessor());
    }

    @Override
    @AvailableInMaintenanceMode
    public TenantResource getBusinessDataModelResource() {
        return getTenantResource(TenantResourceType.BDM);
    }

    protected TenantResource getTenantResource(TenantResourceType type) {
        TenantResourcesService tenantResourcesService = getServiceAccessor().getTenantResourcesService();
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
    @AvailableInMaintenanceMode
    public String getBusinessDataModelVersion() throws BusinessDataRepositoryException {
        try {
            final BusinessDataModelRepository modelRepository = getServiceAccessor().getBusinessDataModelRepository();
            return modelRepository.getInstalledBDMVersion();
        } catch (final SBusinessDataRepositoryException e) {
            throw new BusinessDataRepositoryException(e);
        }
    }

    private String installBusinessDataModel(final byte[] zip)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        log.info("Starting the installation of the BDM.");
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final long userId;
        try {
            userId = getUserId();
        } catch (IllegalStateException e) {
            throw new BusinessDataRepositoryDeploymentException("Unable to determine user ID");
        }
        try {
            final BusinessDataModelRepository bdmRepository = serviceAccessor.getBusinessDataModelRepository();
            TenantStateManager tenantStateManager = serviceAccessor.getTenantStateManager();
            String bdm_version = tenantStateManager.executeTenantManagementOperation("BDM Installation",
                    () -> bdmRepository.install(zip, userId));
            log.info("Installation of the BDM completed.");
            return bdm_version;
        } catch (final SBusinessDataRepositoryDeploymentException e) {
            throw new BusinessDataRepositoryDeploymentException(e);
        } catch (final InvalidBusinessDataModelException e) {
            throw e;
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    @AvailableInMaintenanceMode(onlyAvailableInMaintenanceMode = true)
    @WithLock(key = UPDATE_BDM)
    public void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        log.info("Uninstalling the currently deployed BDM");
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = serviceAccessor.getBusinessDataModelRepository();
            TenantStateManager tenantStateManager = serviceAccessor.getTenantStateManager();
            tenantStateManager.executeTenantManagementOperation("BDM Uninstallation", () -> {
                bdmRepository.uninstall(serviceAccessor.getTenantId());
                return null;
            });
            log.info("BDM successfully uninstalled");
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    @AvailableInMaintenanceMode(onlyAvailableInMaintenanceMode = true)
    @WithLock(key = UPDATE_BDM)
    @Deprecated(since = "9.0.0")
    public String updateBusinessDataModel(final byte[] zip)
            throws BusinessDataRepositoryDeploymentException, InvalidBusinessDataModelException {
        String bdmVersion;
        try {
            uninstallBusinessDataModel();
            bdmVersion = installBusinessDataModel(zip);
        } catch (Exception e) {
            log.warn(
                    "Caught an error when installing/updating the BDM, the transaction will be reverted and the previous BDM restored.");
            throw e;
        }
        log.info("Update operation completed, the BDM was successfully updated");
        return bdmVersion;
    }

    @Override
    @AvailableInMaintenanceMode(onlyAvailableInMaintenanceMode = true)
    @WithLock(key = UPDATE_BDM)
    public void cleanAndUninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = serviceAccessor.getBusinessDataModelRepository();
            TenantStateManager tenantStateManager = serviceAccessor.getTenantStateManager();
            tenantStateManager.executeTenantManagementOperation("BDM Cleanup and uninstallation", () -> {
                bdmRepository.dropAndUninstall(serviceAccessor.getTenantId());
                return null;
            });
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    @AvailableInMaintenanceMode
    public byte[] getClientBDMZip() throws BusinessDataRepositoryException {
        final BusinessDataModelRepository bdmRepository = getServiceAccessor().getBusinessDataModelRepository();
        try {
            return bdmRepository.getClientBDMZip();
        } catch (final SBusinessDataRepositoryException e) {
            throw new BusinessDataRepositoryException(e);
        }
    }

    protected ServiceAccessor getServiceAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private SessionAccessor getSessionAccessor() throws IOException, BonitaHomeConfigurationException,
            BonitaHomeNotSetException, ReflectiveOperationException {
        return ServiceAccessorFactory.getInstance().createSessionAccessor();
    }

    protected long getUserId() throws IllegalStateException {
        try {
            return getServiceAccessor().getSessionService().getSession(getSessionAccessor().getSessionId()).getUserId();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e.getMessage());
        }
    }
}
