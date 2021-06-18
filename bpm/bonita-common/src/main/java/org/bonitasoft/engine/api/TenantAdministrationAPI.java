/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api;

import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.tenant.TenantResource;

/**
 * This API gives access to tenant administration tasks.
 *
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public interface TenantAdministrationAPI {

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
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         if the tenant cannot be paused.
     */
    void pause() throws UpdateException;

    /**
     * Resume the tenant to a normal state after a pause.
     *
     * @throws org.bonitasoft.engine.exception.UpdateException
     *         if the tenant cannot be resumed.
     */
    void resume() throws UpdateException;

    /**
     * Installs a new business data model.
     *
     * @param zip
     *        the binary content of the business object model.
     * @return the version of the Business Data Model just deployed.
     * @deprecated since 7.13, as updateBusinessDataModel does the same operation
     *             and should be used instead
     * @throws InvalidBusinessDataModelException
     *         if the Business Data Model content passed as parameter is invalid.
     * @throws BusinessDataRepositoryDeploymentException
     *         if the deployment cannot be fulfilled completely.
     */
    @Deprecated
    String installBusinessDataModel(final byte[] zip)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException;

    /**
     * Uninstalls the business data model.
     *
     * @throws BusinessDataRepositoryDeploymentException
     *         if the deployment cannot be fulfilled completely.
     */
    void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException;

    /**
     * Update the business data model.
     *
     * @param zip
     *        the binary content of the business object model.
     * @return the version of the Business Data Model just deployed.
     * @throws InvalidBusinessDataModelException
     *         if the Business Data Model content passed as parameter is invalid.
     * @throws BusinessDataRepositoryDeploymentException
     *         if the deployment cannot be fulfilled completely.
     */
    String updateBusinessDataModel(final byte[] zip)
            throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException;

    /**
     * Deletes all business data and uninstalls the business data model.
     *
     * @throws BusinessDataRepositoryDeploymentException
     *         if the deployment cannot be fulfilled completely.
     */
    void cleanAndUninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException;

    /**
     * Returns the generated BDM zip.
     * This zip contains the jars with BDM Pojos and DAOs.
     * usage:
     * byte[] clientBDMZip = getTenantAdministrationAPI().getClientBDMZip();
     * clientBDMZip will typically contain : "README.md", "example-pom.xml", "bdm-dao.jar", "bdm-model.jar", "bom.zip".
     * See online <a
     * href="https://documentation.bonitasoft.com/bonita/latest/how-a-bdm-is-deployed#_bdm_classes_generation">a Java
     * code example</a>
     * on how to extract those artefacts from the Zip file.
     *
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

    /**
     * Retrieves the BDM resource, as a tenant-level resource. Or NONE if no BDM is installed.
     *
     * @return a <code>TenantResource</code> representing the current BDM on the current tenant,
     *         or TenantResource.NONE if no BDM is installed.
     * @since 7.7
     */
    TenantResource getBusinessDataModelResource();

}
