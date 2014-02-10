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

import com.bonitasoft.engine.businessdata.BusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.businessdata.InvalidBusinessDataModelException;
import com.bonitasoft.engine.platform.TenantNotFoundException;

/**
 * This API gives access to tenant management.
 * 
 * @author Matthieu Chaffotte
 */
public interface TenantManagementAPI {

    /**
     * Deploys a new Business Data Repository on the current tenant.
     * 
     * @param jar
     *            the binary content of the jar file to deploy.
     * @throws InvalidBusinessDataModelException
     *             if the jar content passed as parameter is in an invalid format.
     * @throws BusinessDataRepositoryDeploymentException
     *             if the deployment cannot be fulfilled completely.
     */
    void deployBusinessDataRepository(final byte[] jar) throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException;

    /**
     * Allows to set the tenand mode.
     * 
     * @param tenantId
     *            the ID of the tenant to set the maintenance mode for.
     * @param mode
     *            the mode to set: "in maintenance", "running"
     * @throws UpdateException
     *             if the update could not be performed.
     * @see TenantMode
     */
    void setTenantMaintenanceMode(long tenantId, TenantMode mode) throws UpdateException;

    /**
     * @param tenantId
     *            the ID of the tenant
     * @return true if the tenant is in "Maintenance" mode, false otherwise.
     * @throws TenantNotFoundException
     *             if the tenant cannot be found with this ID.
     */
    boolean isTenantInMaintenance(long tenantId) throws TenantNotFoundException;

}
