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
package org.bonitasoft.engine.cache;

import java.io.Serializable;
import java.util.List;

/**
 * @author Baptiste Mesta
 */
public interface CommonCacheService {

    /**
     * Store an object in the cache. If the cache don't exists it will be created.
     *
     * @param cacheName
     *            The name of the cache in which the object must be stored
     * @param key
     *            The key that will allow to retrieve the object
     * @param value
     *            The object to store
     * @throws SCacheException
     *             Error thrown if has exceptions during the cache store.
     */
    void store(String cacheName, Serializable key, Object value) throws SCacheException;

    /**
     * Remove the element according to the cache name and the key
     *
     * @param cacheName
     * @param key
     *            The name of the cache where the object must be stored
     *            The key that will allow to retrieve the object
     * @return
     *         true if an element was removed
     * @throws SCacheException
     *             Error thrown if has exceptions during the cache remove.
     */
    boolean remove(String cacheName, Object key) throws SCacheException;

    /**
     * Get a cached object.
     *
     * @param cacheName
     *            The name of the cache on which to get the object
     * @param key
     *            The key that is used to store the object
     * @return the cached object, or null if it doesn't exists
     * @throws SCacheException
     *             Error thrown if has exceptions during the cache object get.
     */
    Object get(String cacheName, Object key) throws SCacheException;

    /**
     * Get list of keys on a cache.
     *
     * @param cacheName
     *            The name of the cache on which to get the key list
     * @return the list of keys on the cache, or null if no keys exist
     * @throws SCacheException
     */
    List<Object> getKeys(String cacheName) throws SCacheException;

    /**
     * Clear the cache named by cacheName
     *
     * @param cacheName
     *            The name of the cache to clear
     * @return
     * @throws SCacheException
     *             Error thrown if has exceptions during the cache clear.
     */
    boolean clear(String cacheName) throws SCacheException;

    /**
     * Clear all cache of the service
     *
     * @throws SCacheException
     *             Error thrown if has exceptions during the cache clear.
     */
    void clearAll() throws SCacheException;

    /**
     * Return the size of the cache with cacheName.
     *
     * @param cacheName
     *            The name of cache
     * @return the size of the named cache
     * @throws SCacheException
     *             if no cache is found with that name.
     */
    int getCacheSize(String cacheName) throws SCacheException;

    /**
     * Get the names of all the caches
     *
     * @return a list of caches names
     */
    List<String> getCachesNames();

    boolean isStopped();
}
