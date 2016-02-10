/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.business.data;

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;

/**
 * @author Colin PUY
 */
public interface BusinessDataModelRepository {

    /**
     * Deploys a Business Data Model / repository on the specified tenant.
     * 
     * @param bdmArchive
     *        the Business Data Model, as a jar containing the Business Object classes to deploy.
     * @param tenantId
     *        the ID of the tenant to deploy the Business Data Model to.
     * @return the version of the BDM just deployed.
     * @throws SBusinessDataRepositoryDeploymentException
     *         if a deployment exception occurs.
     */
    String install(byte[] bdmArchive, long tenantId) throws SBusinessDataRepositoryDeploymentException;

    /**
     * Undeploy Business Data Model from specified tenant
     * 
     * @param tenantId
     *        the ID of the tenant to undeploy the Business Data Model from
     * @throws SBusinessDataRepositoryException
     *         if error occurs during undeployement
     */
    void uninstall(long tenantId) throws SBusinessDataRepositoryException;

    boolean isDBMDeployed();

    /**
     * Retrieve the client side BDM generated zip.
     * This zip contains jars with BDM Pojos and DAOs
     * 
     * @return the zip content
     * @throws SBusinessDataRepositoryException
     */
    byte[] getClientBDMZip() throws SBusinessDataRepositoryException;

    void dropAndUninstall(long tenantId) throws SBusinessDataRepositoryException;

    /**
     * Returns the currently deployed BDM version, or null if no BDM is deployed.
     * 
     * @return the currently deployed BDM version, or null if no BDM is deployed.
     * @throws SBusinessDataRepositoryException
     *         if the BDM cannot be retrieved.
     */
    String getInstalledBDMVersion() throws SBusinessDataRepositoryException;

    /**
     * Returns the currently deployed Business Object Data Model, or null if no BDM is deployed.
     *
     * @return the currently deployed Business Object Data Model, or null if no BDM is deployed.
     * @throws SBusinessDataRepositoryException
     *         if the BDM cannot be retrieved.
     */
    BusinessObjectModel getBusinessObjectModel() throws SBusinessDataRepositoryException;
}
