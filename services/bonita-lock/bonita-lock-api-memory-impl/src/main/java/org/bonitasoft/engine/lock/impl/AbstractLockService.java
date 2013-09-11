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
package org.bonitasoft.engine.lock.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * 
 * 
 * @author Baptiste Mesta
 * 
 */
public abstract class AbstractLockService implements LockService {

    private static final String SEPARATOR = "_";

    protected final TechnicalLoggerService logger;

    protected final int lockTimeout;

    private final ReadSessionAccessor sessionAccessor;

    protected abstract void storeRejectedLock(final String key, final RejectedLockHandler handler);

    protected abstract RejectedLockHandler getOneRejectedHandler(final String key);

    private final Object mutex = new Object();

    protected final boolean debugEnable;

    private final boolean traceEnable;

    public AbstractLockService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final int lockTimeout) {
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.lockTimeout = lockTimeout;
        debugEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG);
        traceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
    }

    private String buildKey(final long objectToLockId, final String objectType) {
        try {
            return objectType + SEPARATOR + objectToLockId + SEPARATOR + sessionAccessor.getTenantId();
        } catch (TenantIdNotSetException e) {
            throw new IllegalStateException("Tenant not set");
        }
    }

    @Override
    public void unlock(final BonitaLock lock) throws SLockException {
        final String key = buildKey(lock.getObjectToLockId(), lock.getObjectType());
        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "will unlock " + lock.getLock().hashCode() + " id=" + key);
        }
        RejectedLockHandler handler = null;
        synchronized (mutex) {
            try {
                lock.getLock().unlock();
                handler = getOneRejectedHandler(key);
                if (traceEnable) {
                    logger.log(getClass(), TechnicalLogSeverity.TRACE, "unlock " + lock.getLock().hashCode() + " id=" + key + ", execute rejectedLockHandler:"
                            + handler);
                }
            } finally {
                removeLockFromMapIfnotUsed(key);
            }
        }
        if (handler != null) {
            handler.executeOnLockFree();
        }
    }

    @Override
    public BonitaLock tryLock(final long objectToLockId, final String objectType, final RejectedLockHandler rejectedLockHandler) throws SLockException {
        synchronized (mutex) {
            final String key = buildKey(objectToLockId, objectType);
            final Lock lock = getLock(key);
            if (traceEnable) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "tryLock " + lock.hashCode() + " id=" + key);
            }

            if (!isOwnedByCurrentThread(lock, key) && lock.tryLock()) {
                if (traceEnable) {
                    logger.log(getClass(), TechnicalLogSeverity.TRACE, "locked " + lock.hashCode() + " id=" + key);
                }
                return new BonitaLock(lock, objectType, objectToLockId);
            }
            if (traceEnable) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "not locked " + lock.hashCode() + " id=" + key + "store rejectedLockHandler:"
                        + rejectedLockHandler);
            }
            storeRejectedLock(key, rejectedLockHandler);
            return null;
        }
    }

    /**
     * 
     * Check if the lock is own by the current thread: should not happen
     * 
     * @param lock
     * @param key
     * @return
     */
    protected abstract boolean isOwnedByCurrentThread(Lock lock, String key);

    @Override
    public BonitaLock lock(final long objectToLockId, final String objectType) throws SLockException {
        final String key;
        final Lock lock;
        synchronized (mutex) {
            key = buildKey(objectToLockId, objectType);
            lock = getLock(key);
        }
        final long before = System.currentTimeMillis();
        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "lock " + lock.hashCode() + " id=" + key);
        }
        // outside mutex because it's a long lock
        try {
            boolean tryLock = lock.tryLock(lockTimeout, TimeUnit.SECONDS);
            if (!tryLock) {
                throw new SLockException("Timeout trying to lock " + objectToLockId + ":" + objectType);
            }
        } catch (InterruptedException e) {
            throw new SLockException(e);
        }

        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "locked " + lock.hashCode() + " id=" + key);
        }
        final long time = System.currentTimeMillis() - before;

        final TechnicalLogSeverity severity = selectSeverity(time);
        if (severity != null) {
            logger.log(getClass(), severity, "The bocking call to lock for the key " + key + " took " + time + "ms.");
            if (TechnicalLogSeverity.DEBUG.equals(severity)) {
                logger.log(getClass(), severity, new Exception("Stack trace : lock for the key " + key));
            }
        }
        return new BonitaLock(lock, objectType, objectToLockId);
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

    protected abstract Lock getLock(final String key);

    protected abstract void removeLockFromMapIfnotUsed(final String key);

}
