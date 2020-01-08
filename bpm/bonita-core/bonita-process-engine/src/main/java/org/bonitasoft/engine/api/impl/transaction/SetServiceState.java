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
package org.bonitasoft.engine.api.impl.transaction;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SetServiceState implements Callable<Void>, Serializable {

    private static Logger logger = LoggerFactory.getLogger(SetServiceState.class);

    public enum ServiceAction {
        START, STOP, PAUSE, RESUME
    }

    private static final long serialVersionUID = 7880459346729952396L;

    private final long tenantId;
    private ServiceAction action;

    public SetServiceState(long tenantId, ServiceAction action) {
        this.tenantId = tenantId;
        this.action = action;
    }

    @Override
    public Void call() throws Exception {
        final PlatformServiceAccessor platformServiceAccessor = getPlatformAccessor();
        final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);
        final ClassLoaderService classLoaderService = tenantServiceAccessor.getClassLoaderService();
        final TenantConfiguration tenantConfiguration = tenantServiceAccessor.getTenantConfiguration();
        return changeServiceState(classLoaderService, tenantConfiguration);
    }

    public Void changeServiceState(ClassLoaderService classLoaderService, TenantConfiguration tenantConfiguration)
            throws SClassLoaderException, UpdateException {
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        try {

            // Set the right classloader only on start and resume because we destroy it on stop and pause anyway
            if (action == ServiceAction.START || action == ServiceAction.RESUME) {
                final ClassLoader serverClassLoader = classLoaderService.getLocalClassLoader(ScopeType.TENANT.name(),
                        tenantId);
                Thread.currentThread().setContextClassLoader(serverClassLoader);
            }

            for (final TenantLifecycleService tenantService : tenantConfiguration.getLifecycleServices()) {
                logger.info("{} tenant-level service {} on tenant with ID {}", action,
                        tenantService.getClass().getName(), tenantId);

                try {
                    switch (action) {
                        case START:
                            tenantService.start();
                            break;
                        case STOP:
                            tenantService.stop();
                            break;
                        case PAUSE:
                            tenantService.pause();
                            break;
                        case RESUME:
                            tenantService.resume();
                            break;
                    }
                } catch (final SBonitaException sbe) {
                    throw new UpdateException("Unable to " + action + " service: " + tenantService.getClass().getName(),
                            sbe);
                }
            }
            return null;
        } finally {
            // reset previous class loader:
            Thread.currentThread().setContextClassLoader(baseClassLoader);
        }
    }

    public PlatformServiceAccessor getPlatformAccessor()
            throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

}
