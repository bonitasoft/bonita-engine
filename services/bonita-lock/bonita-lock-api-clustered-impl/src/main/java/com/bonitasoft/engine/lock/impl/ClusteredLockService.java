/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.lock.impl;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.lock.impl.AbstractLockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

/**
 * create and release locks using hazelcast
 * 
 * @author Baptiste Mesta
 */
public class ClusteredLockService extends AbstractLockService {

	private final HazelcastInstance hazelcastInstance;
	private final MultiMap<String, RejectedLockHandler> rejectedLockHandlers;

	public ClusteredLockService(final HazelcastInstance hazelcastInstance, final TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor,
			int lockTimeout) {
		super(logger, sessionAccessor, lockTimeout);
		this.hazelcastInstance = hazelcastInstance;
		if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
			throw new IllegalStateException("The clustering is not an active feature.");
		}
		this.rejectedLockHandlers = this.hazelcastInstance.getMultiMap("rejectedLockHandlers");
	}

	@Override
	protected Lock getLock(String key) {
		return hazelcastInstance.getLock(key);
	}

	@Override
	protected void removeLockFromMapIfnotUsed(String key) {
	}

	@Override
	protected RejectedLockHandler getOneRejectedHandler(final String key) {
		if (rejectedLockHandlers.containsKey(key)) {
			final Collection<RejectedLockHandler> handlers = this.rejectedLockHandlers.get(key);
			final RejectedLockHandler handler = handlers.iterator().next();
			rejectedLockHandlers.remove(key, handler);
			if (handlers.size() == 0) {
				rejectedLockHandlers.remove(key);
			}
			return handler;
		}
		return null;
	}

	@Override
	protected void storeRejectedLock(final String key, final RejectedLockHandler handler) {
		rejectedLockHandlers.put(key, handler);
	}
}
