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
     * @throws SBusinessDataRepositoryDeploymentException
     *             if a deployment exception occurs.
     */
    void deploy(byte[] bdmArchive, long tenantId) throws SBusinessDataRepositoryDeploymentException;

    /**
     * Undeploy Business Data Model from specified tenant
     * 
     * @param tenantId
     *            the ID of the tenant to undeploy the Business Data Model from
     * @throws SBusinessDataRepositoryException
     *             if error occurs during undeployement
     */
    void undeploy(long tenantId) throws SBusinessDataRepositoryException;

    byte[] getDeployedBDMDependency() throws SBusinessDataRepositoryException;
    
    boolean isDBMDeployed();
}
