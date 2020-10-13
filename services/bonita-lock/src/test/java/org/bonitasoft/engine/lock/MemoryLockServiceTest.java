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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class MemoryLockServiceTest {

    private final Long tenantId = 1L;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final class LockThread extends Thread {

        private BonitaLock lock;

        private final int id;

        private final String type;

        public LockThread(final int id, final String type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                lock = memoryLockService.lock(id, type, tenantId);
            } catch (final SLockException | SLockTimeoutException e) {
                // NOTHING
            }
        }

        public boolean isLockObtained() {
            return lock != null;
        }
    }

    private final class TryLockThread extends Thread {

        private BonitaLock lock;

        private final int id;

        private final String type;

        private final Semaphore semaphore;

        private final String name2;

        public TryLockThread(final String name, final int id, final String type, final Semaphore semaphore) {
            name2 = name;
            this.id = id;
            this.type = type;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                lock = memoryLockService.tryLock(id, type, 20, TimeUnit.SECONDS, tenantId);
                semaphore.acquire();
                memoryLockService.unlock(lock, tenantId);
                lock = null;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } catch (SLockException e) {
                e.printStackTrace();
            }
        }

        public void waitForLockToBeObtained(final long maxTime) throws InterruptedException {
            final long initialTimestamp = System.currentTimeMillis();
            while (maxTime >= System.currentTimeMillis() - initialTimestamp) {
                if (lock != null) {
                    return;
                }
                Thread.sleep(20);
            }
            throw new InterruptedException("Lock not obtained after waiting " + maxTime + " ms");
        }
    }

    private final TechnicalLoggerService logger = new TechnicalLoggerSLF4JImpl();

    private MemoryLockService memoryLockService;

    @Before
    public void before() {
        memoryLockService = new MemoryLockService(logger, 1);
    }

    @Test
    public void testUnlock() throws Exception {

        final BonitaLock lock = memoryLockService.lock(5, "a", tenantId);
        final LockThread lockThread = new LockThread(5, "a");
        lockThread.start();
        Thread.sleep(100);
        memoryLockService.unlock(lock, tenantId);
        lockThread.join(1200);
        assertTrue("should not be able to lock", lockThread.isLockObtained());
    }

    @Test
    public void should_lock_multiple_times_on_the_same_thread() throws Exception {
        BonitaLock bonitaLock = memoryLockService.lock(123, "abc", tenantId);
        memoryLockService.lock(123, "abc", tenantId);

        //Unable to lock in an other thread
        assertThat(tryLockInAnOtherThread(123, "abc", this.tenantId)).isNull();

        //Unable to lock in an other thread: the lock is still hold once by the current thread
        memoryLockService.unlock(bonitaLock, tenantId);
        assertThat(tryLockInAnOtherThread(123, "abc", this.tenantId)).isNull();

        //Able to unlock the thread release all holds
        memoryLockService.unlock(bonitaLock, tenantId);
        assertThat(tryLockInAnOtherThread(123, "abc", this.tenantId)).isNotNull();
    }

    private BonitaLock tryLockInAnOtherThread(int objectToLockId, String abc, Long tenantId)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        return executorService
                .submit(() -> memoryLockService.tryLock(objectToLockId, abc, 10, TimeUnit.MILLISECONDS, tenantId))
                .get();
    }

    @Test
    public void testLock() throws Exception {
        memoryLockService.lock(3, "a", tenantId);
        final LockThread lockThread = new LockThread(4, "a");
        lockThread.start();
        lockThread.join(100);
        assertTrue("should be able to lock", lockThread.isLockObtained());
    }

    @Test
    public void testLockTimeout() throws Exception {
        memoryLockService.lock(2, "a", tenantId);
        final LockThread lockThread = new LockThread(2, "a");
        lockThread.start();
        lockThread.join(1200);
        assertFalse("should not be able to lock", lockThread.isLockObtained());
    }

    /*
     * Issue with lock removed from map too early: the lock is removed from the map but a thread have a reference on it
     * T1 have the lock L1
     * T2 fait trylock et est blocké
     * T1 release
     * T2 block L1
     * T1 enleve de la map
     * T3 appel tryLock
     * T3 lock L2
     */
    @Test
    public void testRemoveLockFromMap() throws Exception {
        int rd = new Random().nextInt();
        final Semaphore s1 = new Semaphore(1);
        final Semaphore s2 = new Semaphore(1);
        final Semaphore s3 = new Semaphore(1);
        final TryLockThread t1 = new TryLockThread("t1", rd, "t", s1);
        final TryLockThread t2 = new TryLockThread("t2", rd, "t", s2);
        final TryLockThread t3 = new TryLockThread("t3", rd, "t", s3);
        s1.acquire();
        s2.acquire();
        s3.acquire();
        t1.start();
        t2.start();
        t3.start();
        // state : S1 ∈ T, S2 ∈ T, S3 ∈ T, S1 <- T1, S2 <- T2, S3 <- T3, BL free
        s1.release();
        Thread.sleep(5);
        // t1 take the lock | state : S1 ∈ T1, S2 ∈ T, S3 ∈ T, S1 <- T1, S2 <- T2, S3 <- T3, BL ∈ T1
        s2.release();
        Thread.sleep(5);
        // t2 is locked | state : S1 ∈ T1, S2 ∈ T2, S3 ∈ T, S1 <- T1, S3 <- T3, BL ∈ T1, BL <- T1
        s1.release();
        Thread.sleep(20);
        // t1 unlock & t2 have the lock | state : S1, S2 ∈ T2, S3 ∈ T, S3 <- T3, BL ∈ T2
        t2.waitForLockToBeObtained(500);
        s3.release();
        Thread.sleep(20);
        // t3 try acquire | state : S1, S2 ∈ T2, S3 ∈ T3, BL ∈ T2, BL <- T3
        // t3 should not be able to lock
        try {
            t3.waitForLockToBeObtained(500);
            fail();
        } catch (final InterruptedException ignored) {
        }
        s2.release();
        Thread.sleep(20);
        // now it should be able to have it | state : S1, S2, S3 ∈ T3, BL ∈ T3
        t3.waitForLockToBeObtained(500);
        s3.release();
        // state : S1, S2, S3, BL
    }
}
