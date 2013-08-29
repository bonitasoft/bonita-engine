package org.bonitasoft.engine.lock.impl;

import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

public abstract class AbstractLockService implements LockService {

    private static final String SEPARATOR = "_";

    protected final TechnicalLoggerService logger;

    protected final int lockTimeout;

    private final ReadSessionAccessor sessionAccessor;

    protected abstract void storeRejectedLock(final String key, final RejectedLockHandler handler);

    protected abstract RejectedLockHandler getOneRejectedHandler(final String key);

    private final Object mutex = new Object();

    protected final boolean debugEnable;

    public AbstractLockService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final int lockTimeout) {
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
        this.lockTimeout = lockTimeout;
        debugEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG);
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
        synchronized (mutex) {
            final String key = buildKey(lock.getObjectToLockId(), lock.getObjectType());
            try {
                if (debugEnable) {
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, "unlock " + lock.getLock().hashCode() + " id=" + lock.getObjectToLockId()
                            + " by thread " + Thread.currentThread().getId()
                            + " "
                            + Thread.currentThread().getName());
                }
                lock.getLock().unlock();
                final RejectedLockHandler handler = getOneRejectedHandler(key);
                if (handler != null) {
                    handler.executeOnLockFree();
                }
            } finally {
                removeLockFromMapIfnotUsed(key);
            }
        }
    }

    @Override
    public BonitaLock tryLock(final long objectToLockId, final String objectType, final RejectedLockHandler rejectedLockHandler) throws SLockException {
        synchronized (mutex) {
            final String key = buildKey(objectToLockId, objectType);
            final Lock lock = getLock(key);
            if (debugEnable) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "tryLock " + lock.hashCode() + " id=" + objectToLockId + " by thread "
                        + Thread.currentThread().getId() + " "
                        + Thread.currentThread().getName());
            }
            if (lock.tryLock()) {
                if (debugEnable) {
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, "locked " + lock.hashCode() + " id=" + objectToLockId + " by thread "
                            + Thread.currentThread().getId() + " "
                            + Thread.currentThread().getName());
                }
                return new BonitaLock(lock, objectType, objectToLockId);
            } else {
                if (debugEnable) {
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, "not locked " + lock.hashCode() + " id=" + objectToLockId + " by thread "
                            + Thread.currentThread().getId() + " "
                            + Thread.currentThread().getName());
                }
            }
            storeRejectedLock(key, rejectedLockHandler);
            return null;
        }
    }

    @Override
    public BonitaLock lock(final long objectToLockId, final String objectType) throws SLockException {
        final String key;
        final Lock lock;
        synchronized (mutex) {
            key = buildKey(objectToLockId, objectType);
            lock = getLock(key);
        }
        final long before = System.currentTimeMillis();
        if (debugEnable) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "lock " + lock.hashCode() + " id=" + objectToLockId + " by thread "
                    + Thread.currentThread().getId() + " "
                    + Thread.currentThread().getName());
        }
        lock.lock();

        if (debugEnable) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "locked " + lock.hashCode() + " id=" + objectToLockId + " by thread "
                    + Thread.currentThread().getId() + " "
                    + Thread.currentThread().getName());
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
