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
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.resources.STenantResourceLight;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
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
    @CustomTransactions
    public void pause() throws UpdateException {
        TenantServiceAccessor tenantServiceAccessor = getPlatformAccessorNoException()
                .getTenantServiceAccessor();
        try {
            tenantServiceAccessor.getTenantStateManager().pause();
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused
    @CustomTransactions
    public void resume() throws UpdateException {
        TenantServiceAccessor tenantServiceAccessor = getPlatformAccessorNoException()
                .getTenantServiceAccessor();
        try {
            tenantServiceAccessor.getTenantStateManager().resume();
            tenantServiceAccessor.getUserTransactionService().executeInTransaction(() -> {
                resolveDependenciesForAllProcesses();
                return null;
            });
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    private void resolveDependenciesForAllProcesses() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        tenantAccessor.getBusinessArchiveArtifactsManager().resolveDependenciesForAllProcesses(tenantAccessor);
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
    @Deprecated
    @AvailableWhenTenantIsPaused(onlyAvailableWhenPaused = true)
    public String installBusinessDataModel(final byte[] zip)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        log.info("Starting the installation of the BDM.");
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final long userId;
        try {
            userId = getUserId();
        } catch (IllegalStateException e) {
            throw new BusinessDataRepositoryDeploymentException("Unable to determine user ID");
        }
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            TenantStateManager tenantStateManager = tenantAccessor.getTenantStateManager();
            String bdm_version = tenantStateManager.executeTenantManagementOperation("BDM Installation",
                    () -> bdmRepository.install(zip, tenantAccessor.getTenantId(), userId));
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
    @AvailableWhenTenantIsPaused(onlyAvailableWhenPaused = true)
    public void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        log.info("Uninstalling the currently deployed BDM");
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            TenantStateManager tenantStateManager = tenantAccessor.getTenantStateManager();
            tenantStateManager.executeTenantManagementOperation("BDM Uninstallation", () -> {
                bdmRepository.uninstall(tenantAccessor.getTenantId());
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
    @AvailableWhenTenantIsPaused(onlyAvailableWhenPaused = true)
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
    @AvailableWhenTenantIsPaused(onlyAvailableWhenPaused = true)
    public void cleanAndUninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            TenantStateManager tenantStateManager = tenantAccessor.getTenantStateManager();
            tenantStateManager.executeTenantManagementOperation("BDM Cleanup and uninstallation", () -> {
                bdmRepository.dropAndUninstall(tenantAccessor.getTenantId());
                return null;
            });
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
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
            return TenantServiceSingleton.getInstance();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private SessionAccessor getSessionAccessor() throws IllegalAccessException, InstantiationException, IOException,
            ClassNotFoundException, BonitaHomeConfigurationException, BonitaHomeNotSetException {
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
