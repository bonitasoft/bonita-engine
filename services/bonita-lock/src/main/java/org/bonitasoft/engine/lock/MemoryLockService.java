/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.lock;

import static java.util.Collections.synchronizedMap;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnSingleCandidate(LockService.class)
public class MemoryLockService implements LockService {

    private final TechnicalLogger logger;
    // need to have a synchronized map to synchronize the get with map modifications
    private final Map<String, ReentrantLock> locks = synchronizedMap(new HashMap<>());
    private final int lockTimeoutSeconds;

    public MemoryLockService(@Qualifier("platformTechnicalLoggerService") TechnicalLoggerService loggerService,
            @Value("${bonita.platform.lock.memory.timeout}") int lockTimeoutSeconds) {
        this.lockTimeoutSeconds = lockTimeoutSeconds;
        logger = loggerService.asLogger(MemoryLockService.class);
    }

    @Override
    public void unlock(BonitaLock lock, long tenantId) throws SLockException {
        String key = buildKey(lock.getObjectToLockId(), lock.getObjectType(), tenantId);
        locks.computeIfPresent(key, (k, l) -> {
            if (l.hasQueuedThreads()) {
                logger.debug("Lock released {}, keeping it, some other threads are requesting it", lock);
                l.unlock();
                return l;
            }
            if (l.getHoldCount() > 1) {
                logger.debug("Lock released {}, keeping it, it was locked multiple times by the current thread", lock);
                l.unlock();
                return l;
            } else {
                logger.debug("Lock released {}, removing it, no other thread is requesting it", lock);
                l.unlock();
                return null;
            }
        });
    }

    private String buildKey(final long objectToLockId, final String objectType, final long tenantId) {
        return String.format("%s_%s_%s", objectType, objectToLockId, tenantId);
    }

    @Override
    public BonitaLock lock(long objectToLockId, String objectType, long tenantId)
            throws SLockException, SLockTimeoutException {
        BonitaLock bonitaLock = tryLock(objectToLockId, objectType, lockTimeoutSeconds, SECONDS, tenantId);
        if (bonitaLock == null) {
            throw new SLockTimeoutException(String.format("Unable to acquire lock %s,%s,%s in %s seconds",
                    objectToLockId, objectType, tenantId, lockTimeoutSeconds));
        }
        return bonitaLock;
    }

    private ReentrantLock createLock(long objectToLockId, String objectType, long tenantId) {
        String key = buildKey(objectToLockId, objectType, tenantId);
        return locks.computeIfAbsent(key, k -> {
            ReentrantLock lock = new ReentrantLock();
            logger.debug("Created new lock for key {}", key);
            return lock;
        });
    }

    @Override
    public BonitaLock tryLock(long objectToLockId, String objectType, long timeout, TimeUnit timeUnit, long tenantId)
            throws SLockException {
        String key = buildKey(objectToLockId, objectType, tenantId);
        ReentrantLock lock = createLock(objectToLockId, objectType, tenantId);
        try {
            if (lock.tryLock(timeout, timeUnit)) {
                //this get need to be synchronized with the unlock that can change the map
                ReentrantLock currentLockInTheMap = locks.get(key);
                if (!lock.equals(currentLockInTheMap)) {
                    //lock was taken but someone replace it in the map, get it next time
                    // this can happen when the lock is removed just between `createLock` and `tryLock`
                    // retry it...
                    logger.debug(
                            "Lock for key {} was acquired but it was replaced due to a race condition. We will retry.",
                            key);
                    lock.unlock();
                    return tryLock(objectToLockId, objectType, timeout, timeUnit, tenantId);
                }

                logger.debug("Locked acquired for key {}", key);
                return new BonitaLock(objectType, objectToLockId);
            } else {
                logger.debug("Locked was not acquired for key {}", key);
                return null;
            }
        } catch (InterruptedException e) {
            throw new SLockException("interrupted while trying to get the lock", e);
        }
    }

}
