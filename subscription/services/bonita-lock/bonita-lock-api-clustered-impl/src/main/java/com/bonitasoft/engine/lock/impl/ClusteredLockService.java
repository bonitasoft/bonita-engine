/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.lock.impl;

import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * create and release locks using hazelcast
 * 
 * @author Baptiste Mesta
 */
public class ClusteredLockService implements LockService {

    private static final String SEPARATOR = "_";

    protected final TechnicalLoggerService logger;

    protected final int lockTimeout;

    protected final boolean debugEnable;

    private final boolean traceEnable;

    private final IMap<Object, Object> lockMap;

    public ClusteredLockService(final HazelcastInstance hazelcastInstance, final TechnicalLoggerService logger,
            final int lockTimeout) {
        this.logger = logger;
        this.lockTimeout = lockTimeout;
        this.debugEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG);
        this.traceEnable = logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE);
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        lockMap = hazelcastInstance.getMap("BONITA_LOCK_MAP");
    }

    @Override
    public BonitaLock tryLock(final long objectToLockId, final String objectType, final long timeout, final TimeUnit timeUnit, final long tenantId) {
        try {
            return innerTryLock(objectToLockId, objectType, timeout, timeUnit, tenantId);
        } catch (SLockException e) {
            return null;
        }
    }

    @Override
    public BonitaLock lock(final long objectToLockId, final String objectType, final long tenantId) throws SLockException {
        return innerTryLock(objectToLockId, objectType, lockTimeout, TimeUnit.SECONDS, tenantId);
    }

    public BonitaLock innerTryLock(final long objectToLockId, final String objectType, final long timeout, final TimeUnit timeUnit, final long tenantId)
            throws SLockException {
        final String key = buildKey(objectToLockId, objectType, tenantId);
        final long before = System.currentTimeMillis();
        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "lock id=" + key);
        }
        boolean lockObtained = false;
        try {
            lockObtained = lockMap.tryLock(key, timeout, timeUnit);
        } catch (Exception e) {
            throw new SLockException(e);
        }

        if (!lockObtained) {
            throw new SLockException("Timeout trying to lock " + objectToLockId + ":" + objectType);
        }

        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "locked id=" + key);
        }
        final long time = System.currentTimeMillis() - before;

        final TechnicalLogSeverity severity = selectSeverity(time);
        if (severity != null) {
            logger.log(getClass(), severity, "The blocking call to lock for the key " + key + " took " + time + "ms.");
            if (TechnicalLogSeverity.DEBUG.equals(severity)) {
                logger.log(getClass(), severity, new Exception("Stack trace : lock for the key " + key));
            }
        }
        return new BonitaLock(null, objectType, objectToLockId);
    }

    @Override
    public void unlock(final BonitaLock bonitaLock, final long tenantId) {
        final String key = buildKey(bonitaLock.getObjectToLockId(), bonitaLock.getObjectType(), tenantId);
        if (traceEnable) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "will unlock " + bonitaLock.getLock().hashCode() + " id=" + key);
        }
        lockMap.unlock(key);
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

    protected String buildKey(final long objectToLockId, final String objectType, final long tenantId) {
        return objectType + SEPARATOR + objectToLockId + SEPARATOR + tenantId;
    }

    /*
     * @Override
     * protected Lock getLock(final String key) {
     * return hazelcastInstance.getLock(key);
     * }
     * @Override
     * protected void innerUnLock(BonitaLock lock) {
     * lock.getLock().unlock();
     * }
     */

}
