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
package org.bonitasoft.engine.lock.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * This service must be configured as a singleton.
 * 
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public class MemoryLockService implements LockService {

    protected static final String SEPARATOR = "_";

    private final Map<String, ReentrantLock> locks = Collections.synchronizedMap(new HashMap<String, ReentrantLock>());

    protected final TechnicalLoggerService logger;

    protected final int lockTimeout;

    private final Map<Integer, Object> mutexs;

    protected final boolean debugEnable;

    private final boolean traceEnable;

    private final int lockPoolSize;

    /**
     * @param logger
     * @param sessionAccessor
     * @param lockTimeout
     *            timeout to obtain a lock in seconds
     */
    public MemoryLockService(final TechnicalLoggerService logger, final int lockTimeout, final int lockPoolSize) {
        this.logger = logger;
        this.lockTimeout = lockTimeout;
        debugEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG);
        traceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        this.lockPoolSize = lockPoolSize;

        // the goal of this map of mutexs is not to solve completely the competition between keys
        // it is only improving the default "one lock" behavior by partitioning ids among a chosen pool size
        // this a sharding approach
        final Map<Integer, Object> tmpMutexs = new HashMap<Integer, Object>();
        for (int i = 0; i < lockPoolSize; i++) {
            tmpMutexs.put(i, new MemoryLockServiceMutex());
        }
        mutexs = Collections.unmodifiableMap(tmpMutexs);
    }

    private static final class MemoryLockServiceMutex {

    }

    private static final class MemoryLockServiceReentrantLock extends ReentrantLock {

        private static final long serialVersionUID = -4360812005753126401L;

    }

    private Object getMutex(final long objectToLockId) {
        final int poolKeyForThisObjectId = Long.valueOf(objectToLockId % lockPoolSize).intValue();
        if (!mutexs.containsKey(poolKeyForThisObjectId)) {
            throw new RuntimeException("No mutex defined for objectToLockId '" + objectToLockId + "' with generated key '" + poolKeyForThisObjectId + "'");
        }
        return mutexs.get(poolKeyForThisObjectId);
    }

    protected ReentrantLock getLock(final String key) {
        if (!locks.containsKey(key)) {
            // use fair mode?
            locks.put(key, new ReentrantLock());
        }
        return getLockFromKey(key);
    }

    protected ReentrantLock removeLockFromMapIfNotUsed(final String key) {
        final ReentrantLock reentrantLock = getLockFromKey(key);
        /*
         * The reentrant lock must not have waiting thread that tries to lock it, nor a lockservice.lock that locked it
         */
        if (reentrantLock != null && !reentrantLock.hasQueuedThreads()) {
            if (debugEnable) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "removed from map " + reentrantLock.hashCode() + " id=" + key);
            }
            locks.remove(key);
        }
        return reentrantLock;
    }

    private String buildKey(final long objectToLockId, final String objectType, final long tenantId) {
        return objectType + SEPARATOR + objectToLockId + SEPARATOR + tenantId;
    }

    @Override
    public void unlock(final BonitaLock lock, final long tenantId) {
        final String key = buildKey(lock.getObjectToLockId(), lock.getObjectType(), tenantId);
        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "will unlock " + lock.getLock().hashCode() + " id=" + key);
        }
        synchronized (getMutex(lock.getObjectToLockId())) {
            ReentrantLock removedLock = removeLockFromMapIfNotUsed(key);
            // Compare the references
            if (removedLock != lock.getLock()) {
                throw new IllegalStateException("The lock held by the BonitaLock and the one associated to the key do not match.");
            }
            lock.getLock().unlock();
            if (traceEnable) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "unlock " + lock.getLock().hashCode() + " id=" + key);
            }
        }
    }

    @Override
    public BonitaLock tryLock(final long objectToLockId, final String objectType, final long timeout, final TimeUnit timeUnit, final long tenantId) {
        final String key = buildKey(objectToLockId, objectType, tenantId);
        final ReentrantLock lock;
        synchronized (getMutex(objectToLockId)) {
            lock = getLock(key);

            if (traceEnable) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "tryLock " + lock.hashCode() + " id=" + key);
            }

            if (lock.isHeldByCurrentThread()) {
                // We do not want to support reentrant access
                return null;
            }
        }
        try {
            if (lock.tryLock(timeout, timeUnit)) {
                if (traceEnable) {
                    logger.log(getClass(), TechnicalLogSeverity.TRACE, "locked " + lock.hashCode() + " id=" + key);
                }

                // Ensure the lock is still in the map : someone may have called unlock
                // between the synchronized block and the tryLock call
                synchronized (getMutex(objectToLockId)) {
                    ReentrantLock previousLock = getLockFromKey(key);

                    if (previousLock == null) {
                        // Someone unlocked the lock while we were tryLocking so it was removed from the Map.
                        // Let's add it again
                        locks.put(key, lock);
                    } else if (previousLock != lock) {
                        // Compare the 2 Locks by reference : if they are the same: fine, just continue as we locked on the correct lock.
                        // Otherwise we have to release the lock just acquired : someone was faster than us to lock on the same key.
                        lock.unlock();
                        return null;
                    } else {
                        // everything is fine : this is the same lock. Go on !
                    }
                }

                return new BonitaLock(lock, objectType, objectToLockId);
            }
        } catch (final InterruptedException e) {
            logger.log(getClass(), TechnicalLogSeverity.ERROR, "The trylock was interrupted " + lock.hashCode() + " id=" + key);
        }
        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "not locked " + lock.hashCode() + " id=" + key);
        }
        return null;
    }

    @Override
    public BonitaLock lock(final long objectToLockId, final String objectType, final long tenantId) throws SLockException {
        BonitaLock lock = tryLock(objectToLockId, objectType, lockTimeout, TimeUnit.SECONDS, tenantId);
        if (lock == null) {
            throw new SLockException("Unable (default timeout) to acquire the lock for " + objectToLockId + ":" + objectType
                    + getDetailsOnLock(objectToLockId, objectType, tenantId));
        }
        return lock;
    }

    protected StringBuilder getDetailsOnLock(final long objectToLockId, final String objectType, final long tenantId) {
        String key = buildKey(objectToLockId, objectType, tenantId);
        ReentrantLock reentrantLock = getLockFromKey(key);
        StringBuilder details = new StringBuilder(", Details: ");
        if (reentrantLock == null) {
            details.append("The lock was removed from the locks map in the memory lock service");
        } else if (reentrantLock.isLocked()) {
            details.append("The lock is locked");
            if (reentrantLock.isHeldByCurrentThread()) {
                details.append(", held by current thread.");
            } else {
                try {
                    Method getOwnerMethod = reentrantLock.getClass().getDeclaredMethod("getOwner");
                    getOwnerMethod.setAccessible(true);
                    Thread thread = (Thread) getOwnerMethod.invoke(reentrantLock);
                    details.append(", held by thread ");
                    details.append(thread.getName());
                } catch (Exception e) {
                    logger.log(getClass(), TechnicalLogSeverity.INFO, "Error while fetching exception details on lock.", e);
                }
            }
        } else {
            details.append("no additional details could be found (lock exists and is not locked, there should be no problem).");
        }
        return details;
    }

    protected ReentrantLock getLockFromKey(final String key) {
        return locks.get(key);
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
