/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface TenantMonitoringService extends MonitoringService {

    String ENTITY_MBEAN_PREFIX = "Bonitasoft:name=Entity,type=EntityMBean,tenant=";

    String SERVICE_MBEAN_PREFIX = "Bonitasoft:name=Service,type=ServiceMBean,tenant=";

    long getNumberOfUsers() throws SMonitoringException;

    /**
     * Get the current number of active transaction.
     * 
     * @return the current number of active transaction.
     * @since 6.0
     */
    long getNumberOfActiveTransactions();

    /**
     * Get the current number of executing jobs.
     * 
     * @return the current number of executing jobs.
     * @since 6.0
     */
    long getNumberOfExecutingJobs();

}
