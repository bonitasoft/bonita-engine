/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.lock.impl;

import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.lock.impl.AbstractLockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;

/**
 * create and release locks using hazelcast
 * 
 * @author Baptiste Mesta
 */
public class ClusteredLockService extends AbstractLockService {

    private final HazelcastInstance hazelcastInstance;

    public ClusteredLockService(final HazelcastInstance hazelcastInstance, final TechnicalLoggerService logger, ReadSessionAccessor sessionAccessor,
            int lockTimeout) {
        super(logger, sessionAccessor, lockTimeout);
        this.hazelcastInstance = hazelcastInstance;
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
    }

    @Override
    protected Lock getLock(String key) {
        return hazelcastInstance.getLock(key);
    }

    @Override
    protected void removeLockFromMapIfnotUsed(String key) {
    }

}
