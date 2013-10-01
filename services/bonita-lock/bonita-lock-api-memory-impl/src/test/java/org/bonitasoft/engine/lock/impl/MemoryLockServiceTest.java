package org.bonitasoft.engine.lock.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.locks.ReentrantLock;

import org.bonitasoft.engine.lock.BonitaLock;
import org.bonitasoft.engine.lock.RejectedLockHandler;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
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
 * 
 * @author Baptiste Mesta
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class MemoryLockServiceTest {

    private final class RejectedLockHandlerWithState implements RejectedLockHandler {

        private static final long serialVersionUID = 1L;

        @Override
        public void executeOnLockFree() {
        }
    }

    private final class RejectedLockHandlerThatLock implements RejectedLockHandler {

        private static final long serialVersionUID = 1L;

        private final int id;

        private final String type;

        public RejectedLockHandlerThatLock(final int id, final String type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public void executeOnLockFree() throws SLockException {
            BonitaLock tryLock = memoryLockService.tryLock(id, type, new RejectedLockHandlerWithState());
            assertTrue(tryLock != null);
            memoryLockService.unlock(tryLock);
        }
    }

    private final class TryLockThread extends Thread {

        private BonitaLock tryLock;

        private final int id;

        private final String type;

        private final boolean isTry;

        private final RejectedLockHandler rejectedLockHandler;

        public TryLockThread(final int id, final String type, final boolean isTry, final RejectedLockHandler rejectedLockHandler) {
            this.id = id;
            this.type = type;
            this.isTry = isTry;
            this.rejectedLockHandler = rejectedLockHandler;
        }

        public TryLockThread(final int id, final String type, final boolean isTry) {
            this.id = id;
            this.type = type;
            this.isTry = isTry;
            this.rejectedLockHandler = new RejectedLockHandler() {

                private static final long serialVersionUID = 1L;

                @Override
                public void executeOnLockFree() {
                    // TODO Auto-generated method stub

                }
            };
        }

        @Override
        public void run() {
            try {
                if (isTry) {

                    tryLock = memoryLockService.tryLock(id, type, rejectedLockHandler);
                } else {
                    tryLock = memoryLockService.lock(id, type);
                }
            } catch (SLockException e) {
                e.printStackTrace();
            }
        }

        public boolean getResult() {
            return tryLock != null;
        }
    }

    private final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);

    private final ReadSessionAccessor sessionAccessor = mock(ReadSessionAccessor.class);

    private final MemoryLockService memoryLockService = new MemoryLockService(logger, sessionAccessor, 1);

    @Before
    public void before() {
    }

    @Test
    public void testUnlock() throws Exception {

        BonitaLock lock = memoryLockService.lock(5, "a");
        TryLockThread tryLockThread = new TryLockThread(5, "a", false);
        tryLockThread.start();
        Thread.sleep(100);
        memoryLockService.unlock(lock);
        tryLockThread.join(1200);
        assertTrue("should not be able to lock", tryLockThread.getResult());
    }

    @Test
    public void testTryLock() throws Exception {
        memoryLockService.lock(1, "a");
        TryLockThread tryLockThread = new TryLockThread(1, "a", true);
        tryLockThread.start();
        tryLockThread.join(100);
        assertFalse("should not be able to lock", tryLockThread.getResult());
    }

    @Test
    public void testTryLockOnSameThread() throws Exception {
        RejectedLockHandlerWithState rejectedLockHandler1 = new RejectedLockHandlerWithState();
        RejectedLockHandlerWithState rejectedLockHandler2 = new RejectedLockHandlerWithState();
        BonitaLock lock = memoryLockService.tryLock(123, "abc", rejectedLockHandler1);
        BonitaLock lock2 = memoryLockService.tryLock(123, "abc", rejectedLockHandler2);
        memoryLockService.unlock(lock);
        ReentrantLock rlock = (ReentrantLock) lock.getLock();
        ReentrantLock rlock2 = (ReentrantLock) lock2.getLock();
        assertTrue(rlock.isLocked());
        memoryLockService.unlock(lock);
        assertFalse(rlock2.isLocked());
    }

    @Test
    public void testTryLockWithRejectThatLock() throws Exception {
        BonitaLock lock = memoryLockService.tryLock(124, "abc", new RejectedLockHandlerWithState());
        RejectedLockHandlerThatLock rejectedLockHandler = new RejectedLockHandlerThatLock(124, "abc");
        TryLockThread tryLockThread = new TryLockThread(124, "abc", true, rejectedLockHandler);
        tryLockThread.start();
        Thread.sleep(50);
        memoryLockService.unlock(lock);
        tryLockThread.join(100);
    }

    @Test
    public void testLock() throws Exception {
        memoryLockService.lock(3, "a");
        TryLockThread tryLockThread = new TryLockThread(4, "a", true);
        tryLockThread.start();
        tryLockThread.join(100);
        assertTrue("should able to lock", tryLockThread.getResult());
    }

    @Test
    public void testLockTimeout() throws Exception {
        memoryLockService.lock(2, "a");
        TryLockThread tryLockThread = new TryLockThread(2, "a", false);
        tryLockThread.start();
        tryLockThread.join(1200);
        assertFalse("should not be able to lock", tryLockThread.getResult());
    }

}
