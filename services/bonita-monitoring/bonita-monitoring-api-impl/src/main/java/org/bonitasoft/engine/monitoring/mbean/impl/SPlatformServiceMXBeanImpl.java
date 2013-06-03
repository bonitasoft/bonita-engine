/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.monitoring.mbean.impl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.bonitasoft.engine.monitoring.PlatformMonitoringService;
import org.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import org.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import org.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;

/**
 * @author Elias Ricken de Medeiros
 */
public class SPlatformServiceMXBeanImpl implements SPlatformServiceMXBean {

    private final MBeanServer mbserver;

    private final ObjectName name;

    private final String strName;

    private final PlatformMonitoringService monitoringService;

    public SPlatformServiceMXBeanImpl(final PlatformMonitoringService monitoringService) throws MalformedObjectNameException {
        this.mbserver = MBeanUtil.getMBeanServer();
        this.strName = PlatformMonitoringService.SERVICE_MBEAN_NAME;
        this.name = new ObjectName(this.strName);
        this.monitoringService = monitoringService;
    }

    @Override
    public void start() throws MBeanStartException {
        try {
            // register the MXBean
            if (!mbserver.isRegistered(name)) {
                mbserver.registerMBean(this, name);
            }
        } catch (final Exception e) {
            throw new MBeanStartException(e);
        }
    }

    @Override
    public void stop() throws MBeanStopException {
        try {
            // Unregister the MXBean
            if (mbserver.isRegistered(name)) {
                mbserver.unregisterMBean(name);
            }
        } catch (final Exception e) {
            throw new MBeanStopException(e);
        }
    }

    @Override
    public boolean isSchedulerStarted() {
        return monitoringService.isSchedulerStarted();
    }

    @Override
    public String getName() {
        return this.strName;
    }

    @Override
    public long getNumberOfActiveTransactions() {
        return monitoringService.getNumberOfActiveTransactions();
    }

}
