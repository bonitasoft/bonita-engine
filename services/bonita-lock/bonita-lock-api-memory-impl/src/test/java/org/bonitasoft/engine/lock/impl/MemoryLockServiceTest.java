package org.bonitasoft.engine.lock.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

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

    private final class TryLockThread extends Thread {

        private BonitaLock tryLock;

        private final int id;

        private final String type;

        private final boolean isTry;

        public TryLockThread(int id, String type, boolean isTry) {
            this.id = id;
            this.type = type;
            this.isTry = isTry;
        }

        @Override
        public void run() {
            try {
                if (isTry) {

                    tryLock = memoryLockService.tryLock(id, type, new RejectedLockHandler() {

                        @Override
                        public void executeOnLockFree() throws SLockException {
                            // TODO Auto-generated method stub

                        }
                    });
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

        BonitaLock lock = memoryLockService.lock(1, "a");
        TryLockThread tryLockThread = new TryLockThread(1, "a", true);
        tryLockThread.start();
        tryLockThread.join(100);
        assertFalse("should not be able to lock", tryLockThread.getResult());
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
        BonitaLock lock = memoryLockService.lock(2, "a");
        TryLockThread tryLockThread = new TryLockThread(2, "a", false);
        tryLockThread.start();
        tryLockThread.join(1200);
        assertFalse("should not be able to lock", tryLockThread.getResult());
    }

}
