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
package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.bonitasoft.engine.scheduler.job.IncrementItselfJob;

/**
 * @author Baptiste Mesta
 */
public class WaitForIncrementJobToHaveValue  {

    private final int value;
    private final int timeout;
    private final int repeatEach;

    public WaitForIncrementJobToHaveValue(final int repeatEach, final int timeout, int value) {
        assertTrue("timeout is not big enough", repeatEach < timeout);
        this.repeatEach = repeatEach;
        this.timeout = timeout;
        this.value = value;
    }
    
    /**
     * @param timeout
     * @param value
     */
    public WaitForIncrementJobToHaveValue(final int timeout, final int value) {
        this(10, timeout, value);
    }

    boolean check() {
        return IncrementItselfJob.getValue() == value;
    }

    public boolean waitFor() throws InterruptedException {
        final long limit = new Date().getTime() + timeout;
        while (new Date().getTime() < limit) {
            Thread.sleep(repeatEach);
            if (check()) {
                return true;
            }
        }
        return check();
    }
}
