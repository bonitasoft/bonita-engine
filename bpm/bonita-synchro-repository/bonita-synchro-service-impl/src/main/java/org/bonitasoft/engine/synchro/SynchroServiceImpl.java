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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.cache.CommonCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 * @author Charles Souillard
 */
public class SynchroServiceImpl extends AbstractSynchroService {

    private final Map<Map<String, Serializable>, String> waiters;

    private final Map<String, Serializable> eventKeyAndIdMap;

    private final Map<String, Semaphore> eventSemaphores;

    private final Lock lock = new ReentrantLock();

    /**
     * @param initialCapacity
     *            the initial capacity of the map of fired events / waiters (default 50)
     * @param logger
     *            the technical logger service
     */
    private SynchroServiceImpl(final int initialCapacity, final TechnicalLoggerService logger, final CommonCacheService cacheService) {
        super(logger, cacheService);
        waiters = new HashMap<Map<String, Serializable>, String>(initialCapacity);
        eventKeyAndIdMap = new HashMap<String, Serializable>(initialCapacity);
        eventSemaphores = new HashMap<String, Semaphore>();
    }

    @Override
    protected Map<Map<String, Serializable>, String> getWaitersMap() {
        return waiters;
    }

    @Override
    protected Map<String, Serializable> getEventKeyAndIdMap() {
        return eventKeyAndIdMap;
    }

    @Override
    protected Lock getServiceLock() {
        return lock;
    }

    @Override
    protected void releaseWaiter(final String semaphoreKey) {
        Semaphore semaphore = eventSemaphores.get(semaphoreKey);
        if (semaphore != null) {
            semaphore.release();
        }
    }

    @Override
    public Serializable waitForEvent(final Map<String, Serializable> event, final long timeout) throws InterruptedException, TimeoutException {
        Serializable id = null;
        String semaphoreKey = null;
        Semaphore semaphore = null;
        getServiceLock().lock();
        try {
            id = getFiredAndRemoveIt(event);
            if (id == null) {
                semaphoreKey = String.valueOf(event.hashCode());
                semaphore = new Semaphore(1);
                eventSemaphores.put(semaphoreKey, semaphore);
                semaphore.acquire(1);
                getWaitersMap().put(event, semaphoreKey);
            }
        } finally {
            getServiceLock().unlock();
        }
        if (semaphore != null) {
            try {
                if (!semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                    throwTimeout(event, timeout);
                }
            } catch (final InterruptedException e) {
                throwTimeout(event, timeout);
            } finally {
                getWaitersMap().remove(event);
            }
            return getEventKeyAndIdMap().get(semaphoreKey);
        } else {
            return id;
        }
    }

}
