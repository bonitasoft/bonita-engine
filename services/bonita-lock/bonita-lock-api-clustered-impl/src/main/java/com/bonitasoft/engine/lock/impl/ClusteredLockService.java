/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.lock.impl;

import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

/**
 * create and release locks using hazelcast
 * 
 * @author Baptiste Mesta
 */
public class ClusteredLockService implements LockService {

    private static final String SEPARATOR = "_";

    private final HazelcastInstance hazelcastInstance;

    public ClusteredLockService(final HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
    }

    private String getKey(final long objectToLockId, final String objectType) throws SLockException {
        final StringBuilder stb = new StringBuilder();
        stb.append(objectType);
        stb.append(SEPARATOR);
        stb.append(objectToLockId);
        return stb.toString();
    }

    @Override
    public void unlock(final long objectToLockId, final String objectType) throws SLockException {
        try {
            final ILock lock = hazelcastInstance.getLock(getKey(objectToLockId, objectType));
            lock.unlock();
        } catch (final Exception e) {
            throw new SLockException(e);
        }

    }

    @Override
    public boolean tryLock(final long objectToLockId, final String objectType) throws SLockException {
        try {
            final ILock lock = hazelcastInstance.getLock(getKey(objectToLockId, objectType));
            return lock.tryLock();
        } catch (final Exception e) {
            throw new SLockException(e);
        }
    }

    @Override
    public void lock(final long objectToLockId, final String objectType) throws SLockException {
        try {
            final ILock lock = hazelcastInstance.getLock(getKey(objectToLockId, objectType));
            lock.lock();
        } catch (final Exception e) {
            throw new SLockException(e);
        }

    }

}
