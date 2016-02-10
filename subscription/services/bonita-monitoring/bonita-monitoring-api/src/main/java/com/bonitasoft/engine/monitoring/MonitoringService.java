/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring;

import com.bonitasoft.engine.monitoring.mbean.BonitaMXBean;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.engine.monitoring.mbean.MBeanStopException;

/**
 * Start Monitoring MBeans and needed handlers
 * 
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public interface MonitoringService {

    /**
     * Starts the monitoring service : makes available the following MBeans :
     * <ul>
     * <li>Bonitasoft:name=Entity,type=EntityMBean</li>
     * <li>Bonitasoft:name=Service,type=ServiceMBean</li>
     * <li>Bonitasoft:name=JVM,type=JVMMBean</li>
     * </ul>
     * 
     * @throws MBeanStartException
     * @since 6.0
     */
    void registerMBeans() throws MBeanStartException;

    /**
     * Stops the monitoring service i.e makes the following MBeans unavailable.
     * <ul>
     * <li>Bonitasoft:name=Entity,type=EntityMBean</li>
     * <li>Bonitasoft:name=Service,type=ServiceMBean</li>
     * <li>Bonitasoft:name=JVM,type=JVMMBean</li>
     * </ul>
     * 
     * @since 6.0
     */
    void unregisterMbeans() throws MBeanStopException;

    /**
     * Add a bonitaMBean to the default collection of bonitaMXBeans
     * 
     * @param bonitaMBean
     *            BonitaMXBean
     * @since 6.0
     */
    void addMBean(BonitaMXBean bonitaMBean);

}
