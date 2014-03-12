/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.bonitasoft.engine.bdm.Entity;

/**
 * The BusinessDataRepository service allows to manage Business Data operations. It includes deploy / undeploy of a Business Data Model, search / find of
 * Business Data entity objects. Start / stop operations as well. Do we keep that?
 * 
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface BusinessDataRepository {

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

    void start() throws SBusinessDataRepositoryDeploymentException;

    void stop();

    /**
     * Finds an Entity that is defined in a deployed Business Data Model.
     * 
     * @param entityClass
     *            the class of the entity to search for.
     * @param primaryKey
     *            the primary key to search by.
     * @return the found entity, if any.
     * @throws BusinessDataNotFoundException
     *             if the Business Data could not be found with the provided primary key.
     */
    <T> T find(Class<T> entityClass, Serializable primaryKey) throws BusinessDataNotFoundException;

    /**
     * Finds an Entity that is defined in a deployed Business Data Model, through JPQL query.
     * 
     * @param entityClass
     *            the class of the entity to search for.
     * @param qlString
     *            the JPQL query string to search the entity.
     * @param parameters
     *            the parameters needed to execute the query.
     * @return the found entity, if any.
     * @throws BusinessDataNotFoundException
     *             if the Business Data could not be found with the provided primary key.
     * @throws NonUniqueResultException
     *             if more than one result was found.
     */
    <T> T find(Class<T> entityClass, String qlString, Map<String, Object> parameters) throws BusinessDataNotFoundException, NonUniqueResultException;

    <T> T merge(T entity);

    void remove(final Entity entity);

    Set<String> getEntityClassNames();

}
