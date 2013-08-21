/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.lock.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * This service must be configured as a singleton.
 * 
 * @author Elias Ricken de Medeiros
 */
public class MemoryLockService implements LockService {

    private static final String SEPARATOR = "_";

    private static final Object lock = new Object();

    private final TechnicalLoggerService logger;

    public MemoryLockService(final TechnicalLoggerService logger) {
        this.logger = logger;
    }

    private final Map<String, ReentrantLock> locks = new HashMap<String, ReentrantLock>();

    private final Map<String, Long> lockCount = new HashMap<String, Long>();

    private void increaseLockCount(final String key) {
        synchronized (lock) {
            long updatedLockCount;
            if (!locks.containsKey(key)) {
                // use fair mode?
                locks.put(key, new ReentrantLock());
                updatedLockCount = 0L;
            } else {
                updatedLockCount = lockCount.get(key);
            }
            lockCount.put(key, updatedLockCount + 1);
        }
    }

    private void decreaseLockCount(final String key) {
        synchronized (lock) {
            if (lockCount.containsKey(key)) {
                final Long count = lockCount.get(key) - 1;
                lockCount.put(key, count);
                if (count <= 0) {
                    locks.remove(key);
                    lockCount.remove(key);
                }
            }
        }
    }

    Long getLockCount(final long objectToLockId, final String objectType) {
        synchronized (lock) {
            return lockCount.get(buildKey(objectToLockId, objectType));
        }
    }

    private String buildKey(final long objectToLockId, final String objectType) {
        return objectType + SEPARATOR + objectToLockId;
    }

    @Override
    public void unlock(final long objectToLockId, final String objectType) throws SLockException {
        final String key = buildKey(objectToLockId, objectType);
        locks.get(key).unlock();
        decreaseLockCount(key);
    }

    @Override
    public boolean tryLock(final long objectToLockId, final String objectType) throws SLockException {
        final String key = buildKey(objectToLockId, objectType);
        increaseLockCount(key);
        return locks.get(key).tryLock();
    }

    @Override
    public void lock(final long objectToLockId, final String objectType) throws SLockException {
        final String key = buildKey(objectToLockId, objectType);
        increaseLockCount(key);

        final long before = System.currentTimeMillis();
        locks.get(key).lock();
        final long time = System.currentTimeMillis() - before;

        final TechnicalLogSeverity severity = selectSeverity(time);
        if (severity != null && logger.isLoggable(getClass(), severity)) {
            logger.log(getClass(), severity, "The bocking call to lock for the key " + key + " took " + time + "ms.");
            if (TechnicalLogSeverity.DEBUG.equals(severity)) {
                logger.log(getClass(), severity, new Exception("Stack trace : lock for the key " + key));
            }
        }
    }

    TechnicalLogSeverity selectSeverity(final long time) {
        if (time > 150) {
            return TechnicalLogSeverity.INFO;
        } else if (time > 50) {
            return TechnicalLogSeverity.DEBUG;
        } else {
            // No need to log anything
            return null;
        }
    }
}
