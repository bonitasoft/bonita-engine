/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean;

import org.bonitasoft.engine.events.model.HandlerUnregistrationException;

/**
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public interface BonitaMXBean {

    /**
     * Make the MBean available through the default MBeanServer.
     * 
     * @throws MBeanStartException
     */
    void start() throws MBeanStartException;

    /**
     * Make the MBean unavailable through the default MBeanServer.
     * 
     * @throws MBeanStopException
     * @throws HandlerUnregistrationException
     */
    void stop() throws MBeanStopException, HandlerUnregistrationException;

    String getName();

}
