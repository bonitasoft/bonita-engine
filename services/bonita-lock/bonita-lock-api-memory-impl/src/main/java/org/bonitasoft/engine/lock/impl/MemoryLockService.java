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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * This service must be configured as a singleton.
 * 
 * @author Elias Ricken de Medeiros
 */
public final class MemoryLockService implements LockService {

	private final ReentrantLock lock = new ReentrantLock();

	private static final String SEPARATOR = "_";

	protected final TechnicalLoggerService logger;

	protected final int lockTimeout;

	private final ReadSessionAccessor sessionAccessor;

	protected final boolean debugEnable;

	private final boolean traceEnable;

	private final Map<String, Pair> waiters;

	private static class Pair {
		final Condition condition;
		final AtomicInteger count = new AtomicInteger(1);
		public Pair(Condition condition) {
	        super();
	        this.condition = condition;
        }
		
	}
	/**
	 * 
	 * @param logger
	 * @param sessionAccessor
	 * @param lockTimeout
	 *            timeout to obtain a lock in seconds
	 */
	public MemoryLockService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final int lockTimeout) {
		this.logger = logger;
		this.sessionAccessor = sessionAccessor;
		this.lockTimeout = lockTimeout;
		this.debugEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG);
		this.traceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
		this.waiters = new HashMap<String, Pair>();
	}

	@Override
	public BonitaLock lock(final long objectToLockId, final String objectType) throws SLockException {
		final String key = buildKey(objectToLockId, objectType);
		final long before = System.currentTimeMillis();
		if (traceEnable) {
			logger.log(getClass(), TechnicalLogSeverity.TRACE, "lock " + lock.hashCode() + " id=" + key);
		}
		lock.lock();
		try {
			Condition condition = null;
			if (waiters.containsKey(key)) {
				waiters.get(key).count.getAndIncrement();
				condition = waiters.get(key).condition;
				boolean lockObtained = false;
				try {
					lockObtained = condition.await(lockTimeout, TimeUnit.SECONDS);
				} catch (InterruptedException e) {

				}
				if (!lockObtained) {
					throw new SLockException("Timeout trying to lock " + objectToLockId + ":" + objectType);
				}
			} else {
				condition = lock.newCondition();
				waiters.put(key, new Pair(condition));
			}
		} finally {
			lock.unlock();
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

	@Override
	public void unlock(final BonitaLock bonitaLock) throws SLockException {
		final String key = buildKey(bonitaLock.getObjectToLockId(), bonitaLock.getObjectType());
		if (traceEnable) {
			logger.log(getClass(), TechnicalLogSeverity.TRACE, "will unlock " + bonitaLock.getLock().hashCode() + " id=" + key);
		}
		lock.lock();
		try {

			final Pair waiter = waiters.get(key);
			if (waiter == null) {
				Thread.dumpStack();
				throw new SLockException("Unable to unlock an unexisting lock for key: " + key); 
			}
			//conditions.remove(key);
			waiter.count.getAndDecrement();
			if (waiter.count.get() == 0) {
				waiters.remove(key);
			}
			waiter.condition.signal();
		} finally {
			lock.unlock();
		}
		if (traceEnable) {
			logger.log(getClass(), TechnicalLogSeverity.TRACE, "unlock " + bonitaLock.getLock().hashCode() + " id=" + key);
		}
	}

	private TechnicalLogSeverity selectSeverity(final long time) {
		if (time > 150) {
			return TechnicalLogSeverity.INFO;
		} else if (time > 50) {
			return TechnicalLogSeverity.DEBUG;
		} else {
			// No need to log anything
			return null;
		}
	}

	protected String buildKey(final long objectToLockId, final String objectType) {
		try {
			return objectType + SEPARATOR + objectToLockId + SEPARATOR + sessionAccessor.getTenantId();
		} catch (TenantIdNotSetException e) {
			throw new IllegalStateException("Tenant not set");
		}
	}
}
