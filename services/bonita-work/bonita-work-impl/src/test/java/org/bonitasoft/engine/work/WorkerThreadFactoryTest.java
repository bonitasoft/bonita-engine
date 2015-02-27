/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.work;

import static org.bonitasoft.engine.work.WorkerThreadFactory.guessPadding;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WorkerThreadFactoryTest {

    @Test
    public void newThreadStartsWithName() {
        WorkerThreadFactory threadFactory = new WorkerThreadFactory("foo", 11, 1);
        Thread newThread = threadFactory.newThread(buildRunnable());
        assertEquals("foo-11-1", newThread.getName());
    }

    @Test
    public void newThreadNameNumberIsPadded() {
        WorkerThreadFactory threadFactory = new WorkerThreadFactory("foo", 22, 100);
        Thread newThread = threadFactory.newThread(buildRunnable());
        assertEquals("foo-22-001", newThread.getName());
    }

    @Test
    public void testPadding() {
        assertEquals(1, guessPadding(1));
        assertEquals(2, guessPadding(10));
        assertEquals(3, guessPadding(100));
        assertEquals(4, guessPadding(1000));
    }

    /**
     * @return
     */
    private Runnable buildRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

            }
        };
    }
}
