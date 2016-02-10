/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.UpdateException;

import com.bonitasoft.engine.businessdata.BusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.InvalidBusinessDataModelException;

/**
 * This API gives access to tenant management.
 * 
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.api.TenantAdministrationAPI} instead.
 */
@Deprecated
public interface TenantManagementAPI {

    /**
     * @return
     *         true if the tenant is paused.
     */
    boolean isPaused();

    /**
     * Pause the tenant so nothing is executed anymore.
     * when the tenant is paused:
     * Only technical user can login when the tenant is paused.
     * All users connected are disconnected (apart from the technical user).
     * Only IdentityAPI, ThemeAPI and ProfileAPI are accessible.
     * 
     * @throws UpdateException
     *         if the tenant cannot be paused.
     */
    void pause() throws UpdateException;

    /**
     * Resume the tenant to a normal state after a pause.
     * 
     * @throws UpdateException
     *         if the tenant cannot be resumed.
     */
    void resume() throws UpdateException;

    /**
     * Installs a new business data model.
     * 
     * @param zip
     *        the binary content of the business object model.
     * @return the version of the Business Data Model just deployed.
     * @throws InvalidBusinessDataModelException
     *         if the Business Data Model content passed as parameter is invalid.
     * @throws BusinessDataRepositoryDeploymentException
     *         if the deployment cannot be fulfilled completely.
     */
    String installBusinessDataModel(final byte[] zip) throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException;

    /**
     * Uninstalls the business data model.
     * 
     * @throws BusinessDataRepositoryDeploymentException
     *         if the deployment cannot be fulfilled completely.
     */
    void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException;

    /**
     * Deletes all business data and uninstalls the business data model.
     * 
     * @throws BusinessDataRepositoryDeploymentException
     *         if the deployment cannot be fulfilled completely.
     */
    void cleanAndUninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException;

    /**
     * @return zip content of the deployed client Business data model, null if no Business data model has been deployed
     * @throws BusinessDataRepositoryException
     *         if the Business Data Model cannot be retrieved.
     */
    byte[] getClientBDMZip() throws BusinessDataRepositoryException;

    /**
     * Returns the current Business Data Model version, if any, or null if no Business Data Model is currently deployed.
     * 
     * @return the current Business Data Model version, if any, or null if no Business Data Model is currently deployed
     * @throws BusinessDataRepositoryException
     *         if the BDM version cannot be retrieved properly.
     */
    String getBusinessDataModelVersion() throws BusinessDataRepositoryException;
}
