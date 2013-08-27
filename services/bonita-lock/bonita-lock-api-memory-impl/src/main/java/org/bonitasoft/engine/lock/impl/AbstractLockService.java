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

	public AbstractLockService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final int lockTimeout) {
		this.logger = logger;
		this.sessionAccessor = sessionAccessor;
		this.lockTimeout = lockTimeout;
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
		try {
			lock.getLock().unlock();
			//System.err.println("Thread" + Thread.currentThread().getId() + ": unlocking key: " + key);
			final RejectedLockHandler handler = getOneRejectedHandler(key);
			if (handler != null) {
				//System.err.println("Thread" + Thread.currentThread().getId() + ": executing handler for key: " + key);
				handler.executeOnLockFree();
			}
		} finally {
			removeLockFromMapIfnotUsed(key);
		}
	}

	@Override
	public BonitaLock tryLock(final long objectToLockId, final String objectType, final RejectedLockHandler rejectedLockHandler) throws SLockException {
		final String key = buildKey(objectToLockId, objectType);
		//System.err.println("Thread" + Thread.currentThread().getId() + ": trying to get lock for key: " + key);
		final Lock lock = getLock(key);
		if (lock.tryLock()) {
			//System.err.println("Thread" + Thread.currentThread().getId() + ": got the lock for key: " + key);
			return new BonitaLock(lock, objectType, objectToLockId);
		}
		//System.err.println("Thread" + Thread.currentThread().getId() + ": did not get the lock for key: " + key);
		storeRejectedLock(key, rejectedLockHandler);
		return null;
	}

	@Override
	public BonitaLock lock(final long objectToLockId, final String objectType) throws SLockException {
		final String key = buildKey(objectToLockId, objectType);
		final Lock lock = getLock(key);
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
