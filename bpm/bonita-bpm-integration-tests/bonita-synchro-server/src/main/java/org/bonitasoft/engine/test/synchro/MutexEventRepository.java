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
package org.bonitasoft.engine.test.synchro;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Event repository based on a mutex
 * 
 * @author Baptiste Mesta
 */
public class MutexEventRepository implements EventRepository {

    private final Map<Map<String, Serializable>, SynchroObject> waiters;

    private final Map<Map<String, Serializable>, Long> fired;

    private final Object mutex = new Object();

    private static final MutexEventRepository INSTANCE = new MutexEventRepository(50);
    
    /**
     * @param i
     */
    private MutexEventRepository(final int initialCapacity) {
        fired = new HashMap<Map<String, Serializable>, Long>(initialCapacity);
        waiters = new HashMap<Map<String, Serializable>, SynchroObject>(initialCapacity);
    }

    public static MutexEventRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public void fireEvent(final Map<String, Serializable> event, final Long id) {
        synchronized (mutex) {
            final SynchroObject waiter = getWaiterAndRemoveIt(event);
            if (waiter == null) {
                fired.put(event, id);
            } else {
                waiter.setId(id);
                waiter.release();
            }
        }
    }

    private SynchroObject getWaiterAndRemoveIt(final Map<String, Serializable> event) {
        for (final Iterator<Entry<Map<String, Serializable>, SynchroObject>> iterator = waiters.entrySet().iterator(); iterator.hasNext();) {
            final Entry<Map<String, Serializable>, SynchroObject> waiter = iterator.next();
            if (containsAllEntries(waiter.getKey(), event)) {
                iterator.remove();
                return waiter.getValue();
            }
        }
        return null;
    }

    /**
     * @param subSet
     * @param event
     * @return
     */
    private boolean containsAllEntries(final Map<String, Serializable> subSet, final Map<String, Serializable> container) {
        for (final Entry<String, Serializable> entry : subSet.entrySet()) {
            final Serializable value = entry.getValue();
            if (!value.equals(container.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private Entry<Map<String, Serializable>, Long> getFiredAndRemoveIt(final Map<String, Serializable> event) {
        for (final Iterator<Entry<Map<String, Serializable>, Long>> iterator = fired.entrySet().iterator(); iterator.hasNext();) {
            final Entry<Map<String, Serializable>, Long> fired = iterator.next();
            if (containsAllEntries(event, fired.getKey())) {
                iterator.remove();
                return fired;
            }
        }
        return null;
    }

    @Override
    public Long waitForEvent(final Map<String, Serializable> event, final long timeout) throws InterruptedException, TimeoutException {
        SynchroObject waiter = null;
        Long id = null;
        synchronized (mutex) {
            final Entry<Map<String, Serializable>, Long> entry = getFiredAndRemoveIt(event);
            if (entry != null) {
                id = entry.getValue();
            } else {
                waiter = new SynchroObject();
                waiter.acquire(1);
                waiters.put(event, waiter);
            }
        }
        if (waiter != null) {
            try {
                if (!waiter.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                    throwTimeout(event, timeout);
                }
            } catch (final InterruptedException e) {
                throwTimeout(event, timeout);
            }
            return waiter.getObjectId();
        } else {
            return id;
        }
    }

    private void throwTimeout(final Map<String, Serializable> event, final long timeout) throws TimeoutException {
        throw new TimeoutException("Event '" + event + "' has not been received on time after waiting '" + timeout + "ms'");
    }

    @Override
    public void clearAllEvents() {
        fired.clear();
        waiters.clear();
    }

    @Override
    public boolean hasWaiters() {
        return !waiters.isEmpty();
    }
}
