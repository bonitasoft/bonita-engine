/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Charles Souillard
 */
public class MybatisSessionCache {

    /*
     * Why a cache? We want to flush the session in the end only to prevent lock exceptions / deadlock
     * What it does not support?
     * -> insert an object and then query it by something else than id
     * -> update an object and execute a query that is using in where clause one of the modified attributes
     * -> delete an object and execute a query that will (but should'nt) retrieve this object (not fixable because of paginatedAPI)
     * How it works?
     * -> 2 possible implementations
     * 1) * cache created, updated, deleted and selected objects
     * store all statement to execute in the end
     * 2)
     * - Every time an object is inserted, we add it in the insertedObjects and we add it in the cachedObjects
     * - Every time an object is loaded, we first look for it in the cache, if not found we load it and then we store it in the cachedObjects
     * - Every time an object is deleted, we add it in the deletedObjects and we remove it from the cachedObjects
     * - Every time an object is updated, we add it in the updatedObjects and we add it in the cachedObjects
     * When the session is closed, we need to flush everything:
     * - for all removed objects:
     * - if this object was also in insertedObjects, remove it from insertedObjects
     * - if this object was in updatedObjects, remove it from updatedObjects
     * - for every updatedObject, if this object was in insertedObjects, replace the object in insertedObjects by the one in updatedObjects, remove it from
     * updatedObjects
     * - for every insertedObject, execute the insert statement
     * - for every updatedObject, execute the update statement
     * - for every deletedObject, execute the delete statement
     * WARNING ** on a un cas particulier poru la gestion des null..., si on passe un objet de updated à inserted, il faut mettre null à la place de
     * ___bonitanull___
     */
    protected Map<Class<? extends PersistentObject>, Map<Long, PersistentObject>> cachedObjects;

    protected Map<Class<? extends PersistentObject>, Map<Long, PersistentObject>> deletedObjects;

    public MybatisSessionCache() {
        this.cachedObjects = new HashMap<Class<? extends PersistentObject>, Map<Long, PersistentObject>>();
        this.deletedObjects = new HashMap<Class<? extends PersistentObject>, Map<Long, PersistentObject>>();
    }

    public PersistentObject getOrSave(final PersistentObject entity) {
        final Class<? extends PersistentObject> clazz = entity.getClass();
        final long id = entity.getId();

        if (!this.cachedObjects.containsKey(clazz)) {
            this.cachedObjects.put(clazz, new HashMap<Long, PersistentObject>());
        }
        if (this.cachedObjects.get(clazz).containsKey(id)) {
            return this.cachedObjects.get(clazz).get(id);
        }
        this.cachedObjects.get(clazz).put(id, entity);
        return entity;
    }

    public PersistentObject get(final Class<? extends PersistentObject> clazz, final long id) {
        if (!this.cachedObjects.containsKey(clazz)) {
            return null;
        }
        if (this.cachedObjects.get(clazz).containsKey(id)) {
            return this.cachedObjects.get(clazz).get(id);
        }
        return null;
    }

    public void put(final PersistentObject entity) {
        final Class<? extends PersistentObject> clazz = entity.getClass();
        final long id = entity.getId();

        if (!this.cachedObjects.containsKey(clazz)) {
            this.cachedObjects.put(clazz, new HashMap<Long, PersistentObject>());
        }
        this.cachedObjects.get(clazz).put(id, entity);
    }

}
