/*******************************************************************************
 * Copyright (C) 2011,2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean.impl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import com.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;

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
