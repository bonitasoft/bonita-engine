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

import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * This service must be configured as a singleton.
 * 
 * @author Elias Ricken de Medeiros
 */
public class MemoryLockService extends AbstractLockService {

    private final Map<String, BonitaReentrantLock> locks = new HashMap<String, BonitaReentrantLock>();

    private final Map<String, List<RejectedLockHandler>> rejectedLockHandlers = new HashMap<String, List<RejectedLockHandler>>();

    /**
     * 
     * @param logger
     * @param sessionAccessor
     * @param lockTimeout
     *            timeout to obtain a lock in seconds
     */
    public MemoryLockService(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor, final int lockTimeout) {
        super(logger, sessionAccessor, lockTimeout);
    }

    @Override
    protected RejectedLockHandler getOneRejectedHandler(final String key) {
        if (rejectedLockHandlers.containsKey(key)) {
            final RejectedLockHandler handler = rejectedLockHandlers.get(key).remove(0);
            if (rejectedLockHandlers.get(key).size() == 0) {
                rejectedLockHandlers.remove(key);
            }
            return handler;
        }
        return null;
    }

    @Override
    protected void storeRejectedLock(final String key, final RejectedLockHandler handler) {
        if (!rejectedLockHandlers.containsKey(key)) {
            rejectedLockHandlers.put(key, new ArrayList<RejectedLockHandler>());
        }
        rejectedLockHandlers.get(key).add(handler);

    }

    @Override
    protected Lock getLock(final String key) {
        if (!locks.containsKey(key)) {
            // use fair mode?
            locks.put(key, new BonitaReentrantLock());
        }
        return locks.get(key);
    }

    @Override
    protected void removeLockFromMapIfnotUsed(final String key) {
        BonitaReentrantLock reentrantLock = locks.get(key);
        /*
         * The reentrant lock must not have waiting thread that try to lock it, nor a lockservice.lock that locked it nor rejectedlockhandlers waiting for it
         */
        if (reentrantLock != null && !reentrantLock.hasQueuedThreads() && !reentrantLock.isLocked()
                && (rejectedLockHandlers.get(key) == null || rejectedLockHandlers.get(key).isEmpty())) {
            if (debugEnable) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, "removed from map " + reentrantLock.hashCode() + " id=" + key);
            }
            locks.remove(key);
        }
    }

    @Override
    protected boolean isOwnedByCurrentThread(final Lock lock, final String key) {
        BonitaReentrantLock bonitaReentrantLock = (BonitaReentrantLock) lock;
        Thread owner = bonitaReentrantLock.getOwner();
        if (owner == null) {
            return false;
        }
        boolean isCurrentThread = owner.getId() != Thread.currentThread().getId();
        if (debugEnable) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "the lock is owned by the same thread key=" + key);
        }
        return isCurrentThread;
    }
}
