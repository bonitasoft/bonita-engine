package org.bonitasoft.engine.lock.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public abstract class AbstractLockService implements LockService {

    private static final String SEPARATOR = "_";

    protected final TechnicalLoggerService logger;

    protected final int lockTimeout;

    public AbstractLockService(TechnicalLoggerService logger, int lockTimeout) {
        this.logger = logger;
        this.lockTimeout = lockTimeout;
    }

    private String buildKey(final long objectToLockId, final String objectType) {
        return objectType + SEPARATOR + objectToLockId;
    }

    @Override
    public void unlock(final BonitaLock lock) throws SLockException {
        final String key = buildKey(lock.getObjectToLockId(), lock.getObjectType());
        try {
            lock.getLock().unlock();
        } finally {
            removeLockFromMapIfnotUsed(key);
        }
    }

    @Override
    public BonitaLock tryLock(final long objectToLockId, final String objectType) throws SLockException {
        final String key = buildKey(objectToLockId, objectType);
        try {
            Lock lock = getLock(key);
            if (lock.tryLock(lockTimeout, TimeUnit.MILLISECONDS)) {
                return new BonitaLock(lock, objectType, objectToLockId);
            }
            return null;
        } catch (InterruptedException e) {
            throw new SLockException(e);
        }
    }

    @Override
    public BonitaLock lock(final long objectToLockId, final String objectType) throws SLockException {
        final String key = buildKey(objectToLockId, objectType);
        Lock lock = getLock(key);
        final long before = System.currentTimeMillis();
        lock.lock();
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
