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
package org.bonitasoft.engine.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.ReadPersistenceService;

/**
 * @author Charles Souillard
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public interface PersistenceService extends ReadPersistenceService {

    // on save, the service MUST generate a unique ID and set it in id attribute if this attribute is equals to -1
    // else keep the already set id
    /**
     * Add a record into the table by given persistentObject.
     *
     * @param entity
     * @throws SPersistenceException
     * @throws SRetryableException
     * @since 6.0
     */
    void insert(final PersistentObject entity) throws SPersistenceException;

    /**
     * @param entities
     * @throws SPersistenceException
     * @throws SRetryableException
     */
    void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException;

    /**
     * Delete a record from the table by given persistentObject.
     *
     * @param entity
     * @throws SPersistenceException
     * @throws SRetryableException
     * @since 6.0
     */
    void delete(final PersistentObject entity) throws SPersistenceException;

    /**
     * Delete all records belong to the given entity class from the table.
     *
     * @param entityClass
     *        The class which extends persistentObject
     * @throws SPersistenceException
     * @throws SRetryableException
     * @since 6.0
     */
    void deleteAll(final Class<? extends PersistentObject> entityClass) throws SPersistenceException;

    /**
     * Executes a query update.
     *
     * @param updateQueryName
     *        the name of the declared query that represent the update.
     * @return the number of updated rows, as returned by the underlining persistence implementation.
     * @throws SPersistenceException
     *         if a persistence problem occurs when executing the update query.
     */
    int update(final String updateQueryName) throws SPersistenceException;

    /**
     * Executes a query update.
     *
     * @param updateQueryName
     * @param inputParameters
     * @return
     * @throws SPersistenceException
     */
    int update(String updateQueryName, Map<String, Object> inputParameters) throws SPersistenceException;

    /**
     * Delete all elements of a specific table for a specific tenant
     *
     * @param entityClass
     *        Entity class corresponding to the table to empty
     * @param filters
     *        Filters
     * @throws SPersistenceException
     * @since 6.1
     */
    void deleteByTenant(Class<? extends PersistentObject> entityClass, List<FilterOption> filters) throws SPersistenceException;

    /**
     * @param desc
     * @throws SPersistenceException
     * @throws SRetryableException
     */
    void update(final UpdateDescriptor desc) throws SPersistenceException;

    /**
     * @throws SPersistenceException
     * @throws SRetryableException
     */
    void flushStatements() throws SPersistenceException;

    /**
     * Delete a record from the table by id and its class type.
     *
     * @param id
     *        entity's id
     * @param entityClass
     *        The class which extends persistentObject
     * @throws SPersistenceException
     * @throws SRetryableException
     * @since 6.0
     */
    void delete(final long id, final Class<? extends PersistentObject> entityClass) throws SPersistenceException;

    /**
     * Delete records from the table.
     *
     * @param ids
     *        A list contains entity ids
     * @param entityClass
     *        The class which extends persistentObject
     * @throws SPersistenceException
     * @throws SRetryableException
     * @since 6.0
     */
    void delete(final List<Long> ids, final Class<? extends PersistentObject> entityClass) throws SPersistenceException;

}
