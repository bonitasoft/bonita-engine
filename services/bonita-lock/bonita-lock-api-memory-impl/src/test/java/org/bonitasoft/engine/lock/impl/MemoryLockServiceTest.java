package org.bonitasoft.engine.lock.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.lock.BonitaLock;
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
                lock = memoryLockService.lock(id, type);
            } catch (SLockException e) {
                // NOTHING
            }
        }

        public boolean isLockObtained() {
            return lock != null;
        }
    }

    private final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);

    private final ReadSessionAccessor sessionAccessor = mock(ReadSessionAccessor.class);

    private MemoryLockService memoryLockService;

    @Before
    public void before() {
        memoryLockService = new MemoryLockService(logger, sessionAccessor, 1);
    }

    @Test
    public void testUnlock() throws Exception {

        BonitaLock lock = memoryLockService.lock(5, "a");
        LockThread lockThread = new LockThread(5, "a");
        lockThread.start();
        Thread.sleep(100);
        memoryLockService.unlock(lock);
        lockThread.join(1200);
        assertTrue("should not be able to lock", lockThread.isLockObtained());
    }

    @Test(expected = SLockException.class)
    public void testLockOnSameThread() throws Exception {
        memoryLockService.lock(123, "abc");
        memoryLockService.lock(123, "abc");
    }

    @Test
    public void testLock() throws Exception {
        memoryLockService.lock(3, "a");
        LockThread lockThread = new LockThread(4, "a");
        lockThread.start();
        lockThread.join(100);
        assertTrue("should able to lock", lockThread.isLockObtained());
    }

    @Test
    public void testLockTimeout() throws Exception {
        memoryLockService.lock(2, "a");
        LockThread lockThread = new LockThread(2, "a");
        lockThread.start();
        lockThread.join(1200);
        assertFalse("should not be able to lock", lockThread.isLockObtained());
    }

}
