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
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.api.impl.transaction.platform.GetPlatformContent;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.*;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Lu Kai
 * @author Zhang Bole
 * @author Yanyan Liu
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
@Slf4j
public class PlatformAPIImpl implements PlatformAPI {

    public PlatformAPIImpl() {
        super();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void initializePlatform() {
        //nothing to do
    }

    protected ServiceAccessor getServiceAccessor()
            throws BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException,
            ReflectiveOperationException {
        return ServiceAccessorFactory.getInstance().createServiceAccessor();
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void startNode() throws StartNodeException {
        PlatformManager platformManager;
        try {
            platformManager = getServiceAccessor().getPlatformManager();
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

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void stopNode() throws StopNodeException {
        try {
            getServiceAccessor().getPlatformManager().stop();
        } catch (final StopNodeException e) {
            throw e;
        } catch (final Exception e) {
            throw new StopNodeException(e);
        }
    }

    @Override
    @AvailableOnStoppedNode
    public Platform getPlatform() throws PlatformNotFoundException {
        ServiceAccessor platformAccessor;
        try {
            platformAccessor = getServiceAccessor();
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

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public boolean isPlatformCreated() {
        try {
            final ServiceAccessor serviceAccessor = getServiceAccessor();
            final PlatformService platformService = serviceAccessor.getPlatformService();
            return serviceAccessor.getTransactionService().executeInTransaction(platformService::isPlatformCreated);
        } catch (Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public PlatformState getPlatformState() {
        try {
            return getServiceAccessor().getPlatformManager().getState();
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
            getServiceAccessor().getSchedulerService().rescheduleErroneousTriggers();
        } catch (final Exception e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public Map<String, byte[]> getClientPlatformConfigurations() {
        return getBonitaHomeServer().getClientPlatformConfigurations();
    }

    @Override
    public Map<String, byte[]> getClientTenantConfigurations() {
        try {
            PlatformService platformService = getServiceAccessor().getPlatformService();
            long tenantId = platformService.getDefaultTenantId();
            return getBonitaHomeServer().getTenantPortalConfigurations(tenantId);
        } catch (ReflectiveOperationException | BonitaException | IOException | STenantNotFoundException e) {
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
