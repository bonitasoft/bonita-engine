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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
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

    private final Map<String, ReentrantLock> usedLocks = Collections.synchronizedMap(new HashMap<String, ReentrantLock>());

    protected final TechnicalLoggerService logger;

    protected final int lockTimeout;

    private final Map<Integer, Object> mutexs;

    protected final boolean debugEnabled;

    private final boolean traceEnabled;

    private final int lockPoolSize;

    /**
     * @param lockTimeout timeout to obtain a lock (in seconds)
     * @param lockPoolSize the size of the lock pool
     */
    public MemoryLockService(final TechnicalLoggerService logger, final int lockTimeout, final int lockPoolSize) {
        this.logger = logger;
        this.lockTimeout = lockTimeout;
        debugEnabled = logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG);
        traceEnabled = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        this.lockPoolSize = lockPoolSize;

        // The goal of this map of mutexs is not to solve completely the competition between keys,
        // it is only improving the default "one lock" behaviour by partitioning ids among a chosen pool size.
        // This a sharding approach.
        final Map<Integer, Object> tmpMutexs = new HashMap<>();
        for (int i = 0; i < lockPoolSize; i++) {
            tmpMutexs.put(i, new MemoryLockServiceMutex());
        }
        mutexs = Collections.unmodifiableMap(tmpMutexs);
    }

    private static final class MemoryLockServiceMutex {

    }

    private Object getMutex(final long objectId) {
        final int poolKeyForThisObjectId = Long.valueOf(objectId % lockPoolSize).intValue();
        if (!mutexs.containsKey(poolKeyForThisObjectId)) {
            throw new RuntimeException("No mutex defined for objectToLockId '" + objectId + "' with generated key '" + poolKeyForThisObjectId + "'");
        }
        return mutexs.get(poolKeyForThisObjectId);
    }

    protected ReentrantLock getLockAndPutItInMap(final String key) {
        if (!usedLocks.containsKey(key)) {
            // use fair mode?
            usedLocks.put(key, new ReentrantLock());
        }
        return getLockFromKey(key);
    }

    protected ReentrantLock removeLockFromMapIfNotUsed(final String key) {
        final ReentrantLock reentrantLock = getLockFromKey(key);
        // The reentrant lock must not have any waiting thread that tries to lock it, nor a lockservice.lock() that locked it:
        if (reentrantLock != null && !reentrantLock.hasQueuedThreads()) {
            if (traceEnabled) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "removed from map " + reentrantLock.hashCode() + " id=" + key);
            }
            usedLocks.remove(key);
        }
        return reentrantLock;
    }

    private String buildKey(final long objectToLockId, final String objectType, final long tenantId) {
        return objectType + SEPARATOR + objectToLockId + SEPARATOR + tenantId;
    }

    @Override
    public void unlock(final BonitaLock bonitaLock, final long tenantId) {
        final String key = buildKey(bonitaLock.getObjectToLockId(), bonitaLock.getObjectType(), tenantId);
        final Lock lock = bonitaLock.getLock();
        if (traceEnabled) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "Will unlock " + lock.hashCode() + " id=" + key);
        }
        synchronized (getMutex(bonitaLock.getObjectToLockId())) {
            ReentrantLock removedLock = removeLockFromMapIfNotUsed(key);
            // Compare the references
            if (removedLock != lock) {
                throw new IllegalStateException("The lock held by the BonitaLock and the one associated to the key do not match.");
            }
            lock.unlock();
            if (traceEnabled) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "Unlocked " + lock.hashCode() + " id=" + key);
            }
        }
    }

    @Override
    public BonitaLock tryLock(long objectToLockId, String objectType, long timeout, TimeUnit timeUnit, long tenantId) {
        // Let's try to tryLock() 10 times maximum:
        for (int i = 0; i < 10; i++) {
            try {
                return internalTryLock(objectToLockId, objectType, lockTimeout, TimeUnit.SECONDS, tenantId);
            } catch (SBonitaRuntimeException ignored) {
                if (debugEnabled) {
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Bad luck concurrency trying to lock. Let's retry to lock...");
                }
            } finally {
                if (i > 0 && debugEnabled) {
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG,
                            MessageFormat.format("********* YES! retrying solved the problem after {0} retries *********. You can remove those logs, now", i));
                }
            }
        }
        // Could not retrieve the lock for 10 times: log specific exception and return null:
        logger.log(getClass(), TechnicalLogSeverity.WARNING, MessageFormat.format("Tried to acquire the lock for 10 times without success for {0}:{1}{3}",
                objectType, objectToLockId, getDetailsOnLock(objectToLockId, objectType, tenantId)));
        return null;
    }

    protected BonitaLock internalTryLock(final long objectToLockId, final String objectType, final long timeout, final TimeUnit timeUnit, final long tenantId) {
        final String key = buildKey(objectToLockId, objectType, tenantId);
        final ReentrantLock lock;
        synchronized (getMutex(objectToLockId)) {
            lock = getLockAndPutItInMap(key);

            if (traceEnabled) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, MessageFormat.format("TryLock {0} with id={1}", String.valueOf(lock.hashCode()), key));
            }

            if (lock.isHeldByCurrentThread()) {
                // We do not want to support reentrant access
                final String message = "Trying to acquire the lock another time by the same Thread, this should not happen !";
                logger.log(getClass(), TechnicalLogSeverity.WARNING, message);
                throw new IllegalStateException(message);
            }
        }
        try {
            if (lock.tryLock(timeout, timeUnit)) {
                if (traceEnabled) {
                    logger.log(getClass(), TechnicalLogSeverity.TRACE, "Locked " + lock.hashCode() + " id=" + key);
                }

                // Ensure the lock is still in the map : someone may have called unlock
                // between the synchronized block and the tryLock call
                synchronized (getMutex(objectToLockId)) {
                    ReentrantLock previousLock = getLockFromKey(key);

                    if (previousLock == null) {
                        // Someone unlocked the lock while we were tryLocking so it was removed from the Map.
                        // Let's add it again
                        if (traceEnabled) {
                            logger.log(getClass(), TechnicalLogSeverity.TRACE, "Yes, someone just released the lock, let's take it !");
                        }
                        usedLocks.put(key, lock);
                    } else if (previousLock != lock) {
                        if (traceEnabled) {
                            try {
                                Method getOwnerMethod = previousLock.getClass().getDeclaredMethod("getOwner");
                                getOwnerMethod.setAccessible(true);
                                Thread ownerThread = (Thread) getOwnerMethod.invoke(previousLock);
                                String previousThread = ownerThread.getName();
                                logger.log(getClass(), TechnicalLogSeverity.TRACE,
                                        MessageFormat.format("Bad luck, someone (thread {0}) was faster that us to take our lock on {1} {2}", previousThread,
                                                objectType, objectToLockId));
                            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
                            }
                        }
                        // Compare the 2 Locks by reference : if they are the same: fine, just continue as we locked on the correct lock.
                        // Otherwise we have to release the lock just acquired : someone was faster than us to lock on the same key.
                        lock.unlock();
                        throw new SBonitaRuntimeException("Lock acquire concurrency exception. Should trigger a retry lock.");
                    }
                    // else everything is fine : this is the same lock. Go on !
                }

                return new BonitaLock(lock, objectType, objectToLockId);
            }
        } catch (final InterruptedException e) {
            logger.log(getClass(), TechnicalLogSeverity.ERROR, "The trylock was interrupted " + lock.hashCode() + " id=" + key);
        }
        if (traceEnabled) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE,
                    MessageFormat.format("Could not lock after {0} {1} the lock {2} with id={3}", lockTimeout, TimeUnit.SECONDS,
                            String.valueOf(lock.hashCode()), key));
        }
        return null;
    }

    @Override
    public BonitaLock lock(final long objectToLockId, final String objectType, final long tenantId) throws SLockException {
        BonitaLock lock = tryLock(objectToLockId, objectType, lockTimeout, TimeUnit.SECONDS, tenantId);
        if (lock == null) {
            throw new SLockException(MessageFormat.format("Unable to acquire the lock after {0} {1} for {2}:{3}{4}", lockTimeout, TimeUnit.SECONDS,
                    objectType, objectToLockId, getDetailsOnLock(objectToLockId, objectType, tenantId)));
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
            details.append("The lock on ").append(key).append(" is locked");
            if (reentrantLock.isHeldByCurrentThread()) {
                details.append(", held by current thread.");
            } else {
                try {
                    Method getOwnerMethod = reentrantLock.getClass().getDeclaredMethod("getOwner");
                    getOwnerMethod.setAccessible(true);
                    Thread ownerThread = (Thread) getOwnerMethod.invoke(reentrantLock);
                    details.append(", held by thread ");
                    details.append(ownerThread.getName());
                    details.append(" with id ");
                    details.append(ownerThread.getId());
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
        return usedLocks.get(key);
    }

}
