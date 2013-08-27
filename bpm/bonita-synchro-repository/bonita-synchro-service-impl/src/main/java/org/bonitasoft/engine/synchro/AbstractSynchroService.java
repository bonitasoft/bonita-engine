/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.cache.CacheException;
import org.bonitasoft.engine.cache.CommonCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Emmanuel Duchastenier
 * @author Charles Souillard
 */
public abstract class AbstractSynchroService implements SynchroService {

    /**
     * @author Emmanuel Duchastenier
     */
    private final class EventAndIdEntry<K, V> implements Map.Entry<K, V> {

        private final K k;

        private V v;

        public EventAndIdEntry(final K k, final V v) {
            this.k = k;
            this.v = v;
        }

        @Override
        public K getKey() {
            return k;
        }

        @Override
        public V getValue() {
            return v;
        }

        @Override
        public V setValue(final V value) {
            v = value;
            return v;
        }
    }

    private static final String SYNCHRO_SERVICE_CACHE = "SYNCHRO_SERVICE_CACHE";

    /**
     * Long value is an identifier of the sempaphore for the current event.
     * 
     * @return
     */
    protected abstract Map<Map<String, Serializable>, SynchroObject> getWaitersMap();

    protected abstract Object getMutex();

    private final TechnicalLoggerService logger;

    private final CommonCacheService cacheService;

    public AbstractSynchroService(final TechnicalLoggerService logger, final CommonCacheService cacheService) {
        this.logger = logger;
        this.cacheService = cacheService;
    }

    @Override
    public void fireEvent(final Map<String, Serializable> event, final Serializable id) {
        synchronized (getMutex()) {
            final SynchroObject waiter = getWaiterAndRemoveIt(event);
            if (waiter == null) {
                try {
                    cacheService.store(SYNCHRO_SERVICE_CACHE, (Serializable) event, id);
                } catch (CacheException e) {
                    throw new RuntimeException(e);
                }
            } else {
                waiter.setId(id);
                waiter.release();
            }
        }
    }

    protected SynchroObject getWaiterAndRemoveIt(final Map<String, Serializable> event) {
        for (final Iterator<Entry<Map<String, Serializable>, SynchroObject>> iterator = getWaitersMap().entrySet().iterator(); iterator.hasNext();) {
            final Entry<Map<String, Serializable>, SynchroObject> waiter = iterator.next();
            if (containsAllEntries(waiter.getKey(), event)) {
                iterator.remove();
                return waiter.getValue();
            }
        }
        return null;
    }

    private boolean containsAllEntries(final Map<String, Serializable> subSet, final Map<String, Serializable> container) {
        for (final Entry<String, Serializable> entry : subSet.entrySet()) {
            final Serializable value = entry.getValue();
            if (!value.equals(container.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Entry<Map<String, Serializable>, Serializable> getFiredAndRemoveIt(final Map<String, Serializable> event) {
        try {
            List<?> keys = cacheService.getKeys(SYNCHRO_SERVICE_CACHE);
            for (Map<String, Serializable> key : ((List<Map<String, Serializable>>) keys)) {
                if (containsAllEntries(event, key)) {
                    Serializable id;
                    id = (Serializable) cacheService.get(SYNCHRO_SERVICE_CACHE, key);
                    cacheService.remove(SYNCHRO_SERVICE_CACHE, key);
                    return new EventAndIdEntry<Map<String, Serializable>, Serializable>(key, id);
                }
            }
        } catch (CacheException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Serializable waitForEvent(final Map<String, Serializable> event, final long timeout) throws InterruptedException, TimeoutException {
        SynchroObject waiter = null;
        Serializable id = null;
        synchronized (getMutex()) {
            final Entry<Map<String, Serializable>, Serializable> entry = getFiredAndRemoveIt(event);
            if (entry != null) {
                id = entry.getValue();
            } else {
                waiter = new SynchroObject();
                waiter.acquire(1);
                getWaitersMap().put(event, waiter);
            }
        }
        if (waiter != null) {
            try {
                if (!waiter.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                    throwTimeout(event, timeout);
                }
            } catch (final InterruptedException e) {
                throwTimeout(event, timeout);
            } finally {
                getWaitersMap().remove(event);
            }
            return waiter.getObjectId();
        } else {
            return id;
        }
    }

    private void throwTimeout(final Map<String, Serializable> event, final long timeout) throws TimeoutException {
        throw new TimeoutException("Event '" + event + "' has not been received on time after waiting '" + timeout + " ms'");
    }

    @Override
    public void clearAllEvents() {
        try {
            cacheService.clear(SYNCHRO_SERVICE_CACHE);
        } catch (CacheException e) {
            throw new RuntimeException(e);
        }
        getWaitersMap().clear();
    }

    @Override
    public boolean hasWaiters() {
        return !getWaitersMap().isEmpty();
    }
}
