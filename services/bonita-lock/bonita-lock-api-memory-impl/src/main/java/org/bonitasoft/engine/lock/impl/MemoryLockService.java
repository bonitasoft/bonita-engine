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

    private final boolean debugEnabled;

    public MemoryLockService(final TechnicalLoggerService logger) {
        this.logger = logger;
        debugEnabled = logger.isLoggable(MemoryLockService.class, TechnicalLogSeverity.DEBUG);
    }

    private final Map<String, ReentrantLock> locks = new HashMap<String, ReentrantLock>();

    private final Map<String, Long> lockCount = new HashMap<String, Long>();

    private void ensureProcessHasLockObject(final String key) {
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

    private String getKey(final long objectToLockId, final String objectType) throws SLockException {
        final StringBuilder stb = new StringBuilder();
        stb.append(objectType);
        stb.append(SEPARATOR);
        stb.append(objectToLockId);
        return stb.toString();
    }

    @Override
    public void unlock(final long objectToLockId, final String objectType) throws SLockException {
        final String key = getKey(objectToLockId, objectType);
        locks.get(key).unlock();
        removeFromMapIfPossible(key);
    }

    private void removeFromMapIfPossible(final String key) {
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

    @Override
    public boolean tryLock(final long objectToLockId, final String objectType) throws SLockException {
        final String key = getKey(objectToLockId, objectType);
        ensureProcessHasLockObject(key);
        return locks.get(key).tryLock();
    }

    @Override
    public void lock(long objectToLockId, String objectType) throws SLockException {
        final String key = getKey(objectToLockId, objectType);
        ensureProcessHasLockObject(key);

        final long before = System.currentTimeMillis();
        locks.get(key).lock();
        final long after = System.currentTimeMillis();
        final long time = after - before;
        if (time > 50) {
            System.err.println("SHARED LOCK ON: " + key + " took " + time + "ms. Stack:");
            Thread.dumpStack();
        }
    }

}
