/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean;

import com.bonitasoft.engine.monitoring.SMonitoringException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface SPlatformServiceMXBean extends BonitaMXBean {

    /**
     * Return true if the scheduler service is started, false if it is stopped or if its state cannot be determined.
     */
    boolean isSchedulerStarted();

    /**
     * Return the current number of active transactions.
     * 
     * @return the current number of active transactions.
     */
    long getNumberOfActiveTransactions();

    /**
     * return the current number of executing jobs
     *
     * @throws com.bonitasoft.engine.monitoring.SMonitoringException
     */
    long getNumberOfExecutingJobs() throws SMonitoringException;

}
