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
package org.bonitasoft.engine.api.impl.transaction;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SetServiceState implements Callable<Void>, Serializable {

    public enum ServiceAction {START, STOP, PAUSE, RESUME}

    private static final long serialVersionUID = 7880459346729952396L;

    private final long tenantId;
    private ServiceAction action;

    public SetServiceState(long tenantId, ServiceAction action) {
        this.tenantId = tenantId;
        this.action = action;
    }

    @Override
    public Void call() throws Exception {
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final PlatformServiceAccessor platformServiceAccessor = getPlatformAccessor();
            final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);

            // Set the right classloader only on start and resume because we destroy it on stop and pause anyway
            if (action == ServiceAction.START || action == ServiceAction.RESUME) {
                final ClassLoaderService classLoaderService = tenantServiceAccessor.getClassLoaderService();
                final ClassLoader serverClassLoader = classLoaderService.getLocalClassLoader(ScopeType.TENANT.name(), tenantId);
                Thread.currentThread().setContextClassLoader(serverClassLoader);
            }

            final TenantConfiguration tenantConfiguration = tenantServiceAccessor.getTenantConfiguration();
            final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
            for (final TenantLifecycleService tenantService : tenantConfiguration.getLifecycleServices()) {
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
                    logger.log(getClass(), TechnicalLogSeverity.INFO, action + " tenant-level service "
                            + tenantService.getClass().getName() + " on tenant with ID " + tenantId);
                }
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
                    throw new UpdateException("Unable to " + action + " service: " + tenantService.getClass().getName(), sbe);
                }
            }
            return null;
        } finally {
            // reset previous class loader:
            Thread.currentThread().setContextClassLoader(baseClassLoader);
        }
    }

    public PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

}
