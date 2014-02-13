/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.UpdateException;

/**
 * This API gives access to tenant management.
 * 
 * @author Matthieu Chaffotte
 */
public interface TenantManagementAPI {

    /**
     * Allows to set the tenand mode.
     * 
     * @param mode
     *            the mode to set: "in maintenance", "running"
     * 
     * @throws UpdateException
     *             if the update could not be performed.
     * @see {@link TenantMode}
     */
    void setMaintenanceMode(TenantMode mode) throws UpdateException;

    boolean isInMaintenance();

}
