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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.cache.CommonCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Emmanuel Duchastenier
 * @author Charles Souillard
 */
public abstract class AbstractSynchroService implements SynchroService {

    protected static final String SYNCHRO_SERVICE_CACHE = "SYNCHRO_SERVICE_CACHE";

    /**
     * String value is an identifier of the sempaphore for the current event.
     */
    protected abstract Map<Map<String, Serializable>, String> getWaitersMap();

    /**
     * Maitains a map of <EventKey, ID of the object being waited for>
     */
    protected abstract Map<String, Serializable> getEventKeyAndIdMap();

    protected abstract void releaseWaiter(String semaphoreKey);

    protected abstract Lock getServiceLock();

    protected final TechnicalLoggerService logger;

    protected final CommonCacheService cacheService;

    public AbstractSynchroService(final TechnicalLoggerService logger, final CommonCacheService cacheService) {
        this.logger = logger;
        this.cacheService = cacheService;
    }

    @Override
    public void fireEvent(final Map<String, Serializable> event, final Serializable id) {
        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Firing event " + event + " with id " + id);
        getServiceLock().lock();
        try {
            final String semaphoreKey = getWaiterAndRemoveIt(event);
            if (semaphoreKey == null) {
                // No waiter found yet:
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "No waiter found, storing event " + event);
                try {
                    cacheService.store(SYNCHRO_SERVICE_CACHE, (Serializable) event, id);
                } catch (SCacheException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Waiter already exists, let's release waiter:
                getEventKeyAndIdMap().put(semaphoreKey, id);
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "releasing waiter for event " + event + " and id " + id);
                releaseWaiter(semaphoreKey);
            }
        } finally {
            getServiceLock().unlock();
        }
    }

    protected String getWaiterAndRemoveIt(final Map<String, Serializable> event) {
        for (final Iterator<Entry<Map<String, Serializable>, String>> iterator = getWaitersMap().entrySet().iterator(); iterator.hasNext();) {
            final Entry<Map<String, Serializable>, String> waiter = iterator.next();
            if (matchedAtLeastAllExpectedEntries(waiter.getKey(), event)) {
                iterator.remove();
                return waiter.getValue();
            }
        }
        return null;
    }

    protected boolean matchedAtLeastAllExpectedEntries(final Map<String, Serializable> expectedEventEntries, final Map<String, Serializable> actualEventEntries) {
        for (final Entry<String, Serializable> expectedEventEntry : expectedEventEntries.entrySet()) {
            final Serializable expectedValue = expectedEventEntry.getValue();
            if (!expectedValue.equals(actualEventEntries.get(expectedEventEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected Serializable getFiredAndRemoveIt(final Map<String, Serializable> expectedEvent) {
        try {
            List<?> firedEvents = cacheService.getKeys(SYNCHRO_SERVICE_CACHE);
            for (Map<String, Serializable> firedEvent : (List<Map<String, Serializable>>) firedEvents) {
                if (matchedAtLeastAllExpectedEntries(expectedEvent, firedEvent)) {
                    // Serializable id = (Serializable) cacheService.get(SYNCHRO_SERVICE_CACHE, firedEvent);
                    // System.out.println("id=" + id);
                    cacheService.remove(SYNCHRO_SERVICE_CACHE, firedEvent);
                    return firedEvent.get("id");
                }
            }
        } catch (SCacheException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    protected void throwTimeout(final Map<String, Serializable> event, final long timeout) throws TimeoutException {
        throw new TimeoutException("Event '" + event + "' has not been received on time after waiting '" + timeout + " ms'");
    }

    @Override
    public void clearAllEvents() {
        try {
            cacheService.clear(SYNCHRO_SERVICE_CACHE);
        } catch (SCacheException e) {
            throw new RuntimeException(e);
        }
        getWaitersMap().clear();
    }

    @Override
    public boolean hasWaiters() {
        return !getWaitersMap().isEmpty();
    }

}
