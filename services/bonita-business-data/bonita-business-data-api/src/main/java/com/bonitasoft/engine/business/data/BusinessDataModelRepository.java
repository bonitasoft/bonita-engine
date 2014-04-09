package com.bonitasoft.engine.business.data;

/**
 * @author Colin PUY
 */
public interface BusinessDataModelRepository {

    /**
     * Deploys a Business Data Model / repository on the specified tenant.
     * 
     * @param bdmArchive
     *            the Business Data Model, as a jar containing the Business Object classes to deploy.
     * @param tenantId
     *            the ID of the tenant to deploy the Business Data Model to.
     * @return the version of the BDM just deployed.
     * @throws SBusinessDataRepositoryDeploymentException
     *             if a deployment exception occurs.
     */
    String install(byte[] bdmArchive, long tenantId) throws SBusinessDataRepositoryDeploymentException;

    /**
     * Undeploy Business Data Model from specified tenant
     * 
     * @param tenantId
     *            the ID of the tenant to undeploy the Business Data Model from
     * @throws SBusinessDataRepositoryException
     *             if error occurs during undeployement
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
}
