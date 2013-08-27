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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * This service must be configured as a singleton.
 * 
 * @author Elias Ricken de Medeiros
 */
public class MemoryLockService extends AbstractLockService {

	private static final Object lock = new Object();

	private final Map<String, ReentrantLock> locks = new HashMap<String, ReentrantLock>();
	private final Map<String, List<RejectedLockHandler>> rejectedLockHandlers = new HashMap<String, List<RejectedLockHandler>>();

	public MemoryLockService(final TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor, int lockTimeout) {
		super(logger, sessionAccessor, lockTimeout);
	}

	@Override
	protected RejectedLockHandler getOneRejectedHandler(final String key) {
		synchronized (lock) {
			if (rejectedLockHandlers.containsKey(key)) {
				final RejectedLockHandler handler = rejectedLockHandlers.get(key).remove(0);
				if (rejectedLockHandlers.get(key).size() == 0) {
					rejectedLockHandlers.remove(key);
				}
				return handler;
			}
		}
		return null;
	}
	
	@Override
	protected void storeRejectedLock(final String key, final RejectedLockHandler handler) {
		synchronized (lock) {
			if (!rejectedLockHandlers.containsKey(key)) {
				rejectedLockHandlers.put(key, new ArrayList<RejectedLockHandler>());
			}
			rejectedLockHandlers.get(key).add(handler);
		}
	    
	}

	@Override
	protected Lock getLock(final String key) {
		synchronized (lock) {
			if (!locks.containsKey(key)) {
				// use fair mode?
						locks.put(key, new ReentrantLock());
			}
			return locks.get(key);
		}
	}

	@Override
	protected void removeLockFromMapIfnotUsed(final String key) {
		synchronized (lock) {
			ReentrantLock reentrantLock = locks.get(key);
			if (reentrantLock != null && !reentrantLock.hasQueuedThreads()) {
				locks.remove(key);
			}
		}
	}
}
