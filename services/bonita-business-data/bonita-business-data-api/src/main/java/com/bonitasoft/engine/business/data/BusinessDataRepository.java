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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.commons.TenantLifecycleService;

import com.bonitasoft.engine.bdm.Entity;

/**
 * The BusinessDataRepository service allows to manage Business Data operations. It includes deploy / undeploy of a Business Data Model, search / find / create
 * / update of Business Data entity objects.
 *
 * @see Entity
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface BusinessDataRepository extends TenantLifecycleService {

    /**
     * Finds an Entity that is defined in a deployed Business Data Model.
     *
     * @param entityClass
     *            the class of the entity to search for.
     * @param primaryKey
     *            the primary key to search by.
     * @return the found entity, if any.
     * @throws SBusinessDataNotFoundException
     *             if the Business Data could not be found with the provided primary key.
     */
    <T extends Entity> T findById(Class<T> entityClass, Long primaryKey) throws SBusinessDataNotFoundException;

    /**
     * Finds entities that is defined in a deployed Business Data Model. If a primary key does not match an existing entity no exception is thrown and nothing
     * is added in the list.
     *
     * @param entityClass
     *        the class of the entity to search for.
     * @param primaryKeys
     *        the primary keys.
     * @return the list of found entities
     */
    <T extends Entity> List<T> findByIds(Class<T> entityClass, List<Long> primaryKeys);

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
     * @throws SBusinessDataNotFoundException
     *             if the Business Data could not be found with the provided primary key.
     * @throws NonUniqueResultException
     *             if more than one result was found.
     */
    <T extends Serializable> T find(Class<T> resultClass, String jpqlQuery, Map<String, Serializable> parameters) throws NonUniqueResultException;

    <T extends Serializable> List<T> findList(Class<T> resultClass, String jpqlQuery, Map<String, Serializable> parameters, int startIndex, int maxResults);

    <T extends Serializable> T findByNamedQuery(String queryName, Class<T> resultClass, Map<String, Serializable> parameters) throws NonUniqueResultException;

    <T extends Serializable> List<T> findListByNamedQuery(String queryName, Class<T> resultClass, Map<String, Serializable> parameters, int startIndex,
            int maxResults);

    /**
     * Saves or updates an entity in the Business Data Repository.
     *
     * @param entity
     *            the entity to save / update.
     * @return the freshly persisted entity.
     */
    void persist(Entity entity);

    /**
     * Removes an entity from the Business Data Repository.
     *
     * @param entity
     *            the entity to remove.
     */
    void remove(Entity entity);

    /**
     * Reconnect the given entity with the persistence unit
     * @param entity
     *        the entity to reconnect.
     * @return the connected entity.
     */
    Entity merge(Entity entity);

    /**
     * Retrieves the <code>Set</code> of known Entity class names in this Business Data Repository.
     *
     * @return the <code>Set</code> of known Entity class names, as qualified class names.
     */
    Set<String> getEntityClassNames();


}
