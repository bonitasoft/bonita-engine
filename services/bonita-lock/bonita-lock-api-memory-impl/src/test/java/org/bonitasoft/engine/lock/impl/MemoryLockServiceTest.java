package org.bonitasoft.engine.lock.impl;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class MemoryLockServiceTest {

    private final Long tenantId = 1l;

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
            } catch (SLockException e) {
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
                System.out.println(name2 + " wait to acquire the lock");
                System.out.println(name2 + " will lock");
                semaphore.acquire();
                lock = memoryLockService.tryLock(id, type, 20, TimeUnit.SECONDS, tenantId);
                System.out.println(name2 + " lock obtained, wait to unlock");
                semaphore.acquire();
                System.out.println(name2 + " will unlock");
                memoryLockService.unlock(lock, tenantId);
                lock = null;
                System.out.println(name2 + " unlocked");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public boolean isLockObtained() {
            return lock != null;
        }
    }

    private final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);

    private MemoryLockService memoryLockService;

    @Before
    public void before() {
        memoryLockService = new MemoryLockService(logger, 1, 12);
    }

    @Test
    public void testUnlock() throws Exception {

        BonitaLock lock = memoryLockService.lock(5, "a", tenantId);
        LockThread lockThread = new LockThread(5, "a");
        lockThread.start();
        Thread.sleep(100);
        memoryLockService.unlock(lock, tenantId);
        lockThread.join(1200);
        assertTrue("should not be able to lock", lockThread.isLockObtained());
    }

    @Test(expected = SLockException.class)
    public void testLockOnSameThread() throws Exception {
        memoryLockService.lock(123, "abc", tenantId);
        memoryLockService.lock(123, "abc", tenantId);
    }

    @Test
    public void testLock() throws Exception {
        memoryLockService.lock(3, "a", tenantId);
        LockThread lockThread = new LockThread(4, "a");
        lockThread.start();
        lockThread.join(100);
        assertTrue("should able to lock", lockThread.isLockObtained());
    }

    @Test
    public void testLockTimeout() throws Exception {
        memoryLockService.lock(2, "a", tenantId);
        LockThread lockThread = new LockThread(2, "a");
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
        Semaphore s1 = new Semaphore(1);
        Semaphore s2 = new Semaphore(1);
        Semaphore s3 = new Semaphore(1);
        TryLockThread t1 = new TryLockThread("t1", 1, "t", s1);
        TryLockThread t2 = new TryLockThread("t2", 1, "t", s2);
        TryLockThread t3 = new TryLockThread("t3", 1, "t", s3);
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
        // t2 is locked | state : S1 ∈ T1, S2 ∈ T2, S3 ∈ T, S1 <- T1, S3 <- T3, BL ∈ T1, BL <- T2
        s1.release();
        Thread.sleep(20);
        // t1 unlock & t2 have the lock | state : S1, S2 ∈ T2, S3 ∈ T, S3 <- T3, BL ∈ T2
        assertTrue(t2.isLockObtained());
        s3.release();
        Thread.sleep(20);
        // t3 try acquire | state : S1, S2 ∈ T2, S3 ∈ T3, BL ∈ T2, BL <- T3
        // t3 should not be able to lock
        assertFalse(t3.isLockObtained());
        s2.release();
        Thread.sleep(20);
        // now it should be able to have it | state : S1, S2, S3 ∈ T3, BL ∈ T3
        assertTrue(t3.isLockObtained());
        s3.release();
        // state : S1, S2, S3, BL
    }

    @Test
    public void getDetailsOnLockShouldReturnLockingTheadOwnerName() {
        // given:
        MemoryLockService spiedLockService = spy(memoryLockService);
        long objectToLockId = 151L;
        String lockKey = "objectType_" + objectToLockId + "_" + tenantId;
        ReentrantLock lock = spy(new ReentrantLock());
        lock.lock();
        doReturn(false).when(lock).isHeldByCurrentThread();
        doReturn(lock).when(spiedLockService).getLockFromKey(lockKey);

        // when:
        StringBuilder detailsOnLock = spiedLockService.getDetailsOnLock(objectToLockId, "objectType", tenantId);

        // then:
        assertThat(detailsOnLock).describedAs("detailsOnLock should contain 'held by thread main'").contains("held by thread main");
    }
}
