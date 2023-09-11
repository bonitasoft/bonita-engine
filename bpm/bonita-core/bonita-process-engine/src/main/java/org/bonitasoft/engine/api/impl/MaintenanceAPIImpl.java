/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import org.bonitasoft.engine.api.MaintenanceAPI;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.maintenance.MaintenanceInfo;
import org.bonitasoft.engine.maintenance.MaintenanceInfoNotFoundException;
import org.bonitasoft.engine.maintenance.impl.MaintenanceInfoImpl;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.builder.SPlatformUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.impl.SPlatformUpdateBuilderImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.tenant.TenantStateManager;

/**
 * This API gives access to maintenance administration tasks.
 */
public class MaintenanceAPIImpl implements MaintenanceAPI {

    public MaintenanceAPIImpl() {
    }

    protected ServiceAccessor getServiceAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public MaintenanceInfo getMaintenanceInfo() throws MaintenanceInfoNotFoundException, PlatformNotFoundException {
        try {
            PlatformService platformService = getServiceAccessor().getPlatformService();
            MaintenanceInfo.State state = platformService.getDefaultTenant().isPaused() ? MaintenanceInfo.State.ENABLED
                    : MaintenanceInfo.State.DISABLED;
            SPlatform platform = platformService.getPlatform();
            return MaintenanceInfoImpl.builder()
                    .maintenanceMessage(platform.getMaintenanceMessage())
                    .maintenanceMessageActive(platform.isMaintenanceMessageActive())
                    .maintenanceState(state)
                    .build();
        } catch (STenantNotFoundException e) {
            throw new MaintenanceInfoNotFoundException("Maintenance info not found", e);
        } catch (SPlatformNotFoundException e) {
            throw new PlatformNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public void enableMaintenanceMode() throws UpdateException {
        try {
            TenantStateManager tenantStateManager = getServiceAccessor().getTenantStateManager();
            tenantStateManager.pause();
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void disableMaintenanceMode() throws UpdateException {
        try {
            TenantStateManager tenantStateManager = getServiceAccessor().getTenantStateManager();
            tenantStateManager.resume();
            disableMaintenanceMessage();
        } catch (Exception e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void updateMaintenanceMessage(String message) throws UpdateException {
        try {
            PlatformService platformService = getServiceAccessor().getPlatformService();
            platformService.updatePlatform(getPlatformUpdateBuilder()
                    .setMaintenanceMessage(message).done());
        } catch (SPlatformUpdateException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void enableMaintenanceMessage() throws UpdateException {
        try {
            PlatformService platformService = getServiceAccessor().getPlatformService();
            platformService.updatePlatform(getPlatformUpdateBuilder()
                    .setMaintenanceMessageActive(true).done());
        } catch (SPlatformUpdateException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void disableMaintenanceMessage() throws UpdateException {
        try {
            PlatformService platformService = getServiceAccessor().getPlatformService();
            platformService.updatePlatform(getPlatformUpdateBuilder()
                    .setMaintenanceMessageActive(false).done());
        } catch (SPlatformUpdateException e) {
            throw new UpdateException(e);
        }
    }

    protected SPlatformUpdateBuilder getPlatformUpdateBuilder() {
        return SPlatformUpdateBuilderImpl.builder()
                .descriptor(new EntityUpdateDescriptor())
                .build();
    }
}
