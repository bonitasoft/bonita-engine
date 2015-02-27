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
package org.bonitasoft.engine.persistence;

import java.util.List;
import java.util.Map;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public interface ReadPersistenceService {

    /**
     * @param selectDescriptor
     * @return
     * @throws SBonitaReadException
     * @throws SRetryableException
     */
    <T extends PersistentObject> T selectById(SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException;

    /**
     * @param selectDescriptor
     * @return
     * @throws SBonitaReadException
     * @throws SRetryableException
     */
    <T> T selectOne(SelectOneDescriptor<T> selectDescriptor) throws SBonitaReadException;

    /**
     * @param selectDescriptor
     * @return
     * @throws SBonitaReadException
     * @throws SRetryableException
     */
    <T> List<T> selectList(SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException;

    /**
     * @param entityClass
     * @param options
     * @param parameters
     * @return
     * @throws SBonitaReadException
     * @throws SRetryableException
     */
    <T extends PersistentObject> long getNumberOfEntities(Class<T> entityClass, QueryOptions options, Map<String, Object> parameters)
            throws SBonitaReadException;

    /**
     * @param entityClass
     * @param querySuffix
     * @param options
     * @param parameters
     * @return
     * @throws SBonitaReadException
     * @throws SRetryableException
     */
    <T extends PersistentObject> long getNumberOfEntities(Class<T> entityClass, String querySuffix, QueryOptions options, Map<String, Object> parameters)
            throws SBonitaReadException;

    /**
     * @param entityClass
     * @param options
     * @param parameters
     * @return
     * @throws SBonitaReadException
     * @throws SRetryableException
     */
    <T extends PersistentObject> List<T> searchEntity(Class<T> entityClass, QueryOptions options, Map<String, Object> parameters)
            throws SBonitaReadException;

    /**
     * @param entityClass
     *            class of the object we want to search on
     * @param querySuffix
     *            Used to define customized search query
     * @param options
     *            query options
     * @param parameters
     * @return
     * @throws SBonitaReadException
     * @throws SRetryableException
     */
    <T extends PersistentObject> List<T> searchEntity(Class<T> entityClass, String querySuffix, QueryOptions options, Map<String, Object> parameters)
            throws SBonitaReadException;

    /**
     * @return
     *         the name of the persistence service
     */
    String getName();

}
