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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Emmanuel Duchastenier
 * @author Charles Souillard
 */
public abstract class AbstractSynchroService implements SynchroService {

    protected abstract Map<Map<String, Serializable>, SynchroObject> getWaitersMap();

    protected abstract Map<Map<String, Serializable>, Serializable> getFiredMap();

    protected abstract Object getMutex();

    private final TechnicalLoggerService logger;

    public AbstractSynchroService(final TechnicalLoggerService logger) {
        this.logger = logger;
    }

    @Override
    public void fireEvent(final Map<String, Serializable> event, final Serializable id) {
        synchronized (getMutex()) {
            final SynchroObject waiter = getWaiterAndRemoveIt(event);
            if (waiter == null) {
                getFiredMap().put(event, id);
            } else {
                waiter.setId(id);
                waiter.release();
            }
        }
    }

    private SynchroObject getWaiterAndRemoveIt(final Map<String, Serializable> event) {
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

    private Entry<Map<String, Serializable>, Serializable> getFiredAndRemoveIt(final Map<String, Serializable> event) {
        for (final Iterator<Entry<Map<String, Serializable>, Serializable>> iterator = getFiredMap().entrySet().iterator(); iterator.hasNext();) {
            final Entry<Map<String, Serializable>, Serializable> fired = iterator.next();
            if (containsAllEntries(event, fired.getKey())) {
                iterator.remove();
                return fired;
            }
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
        getFiredMap().clear();
        getWaitersMap().clear();
    }

    @Override
    public boolean hasWaiters() {
        return !getWaitersMap().isEmpty();
    }
}
