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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.lock.impl.AbstractLockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.MultiMap;

/**
 * create and release locks using hazelcast
 * 
 * @author Baptiste Mesta
 */
public class ClusteredLockService extends AbstractLockService {

	private final HazelcastInstance hazelcastInstance;

	private final MultiMap<String, RejectedLockHandler> rejectedLockHandlers;

	private final IExecutorService executorService; 

	public ClusteredLockService(final HazelcastInstance hazelcastInstance, final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor,
			final int lockTimeout) {
		super(logger, sessionAccessor, lockTimeout);
		this.hazelcastInstance = hazelcastInstance;
		if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
			throw new IllegalStateException("The clustering is not an active feature.");
		}
		this.executorService = this.hazelcastInstance.getExecutorService("ExecutorLock");
		this.rejectedLockHandlers = this.hazelcastInstance.getMultiMap("rejectedLockHandlers");
	}

	@Override
	protected Lock getLock(final String key) {
		return hazelcastInstance.getLock(key);
	}

	@Override
	protected void removeLockFromMapIfnotUsed(final String key) {
	}

	@Override
	protected RejectedLockHandler getOneRejectedHandler(final String key) {
		final Callable<RejectedLockHandler> task = new MyCallable(key);
		try {
	        return this.executorService.submitToKeyOwner(task, key).get();
        } catch (InterruptedException e) {
	        throw new SBonitaRuntimeException(e);
        } catch (ExecutionException e) {
        	throw new SBonitaRuntimeException(e);
        }
	}

	private static final class MyCallable implements Callable<RejectedLockHandler>, HazelcastInstanceAware {
		
		private transient HazelcastInstance hazelcastInstance;
		private String key;
		
		public MyCallable(final String key) {
			this.key = key;
		}
		
		@Override
		public RejectedLockHandler call() throws Exception {
			final MultiMap<String, RejectedLockHandler> rejectedLockHandlers = this.hazelcastInstance.getMultiMap("rejectedLockHandlers");
			if (rejectedLockHandlers.containsKey(key)) {
				final Collection<RejectedLockHandler> handlers = rejectedLockHandlers.get(key);
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
		public void setHazelcastInstance(final HazelcastInstance hazelcastInstance) {
			this.hazelcastInstance = hazelcastInstance;

		}
	}
	@Override
	protected void storeRejectedLock(final String key, final RejectedLockHandler handler) {
		rejectedLockHandlers.put(key, handler);
	}

	@Override
	protected boolean isOwnedByCurrentThread(final Lock lock, final String key) {
		return false;
	}
}
