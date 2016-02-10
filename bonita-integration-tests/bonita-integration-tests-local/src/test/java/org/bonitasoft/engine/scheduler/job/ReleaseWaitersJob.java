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
package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Baptiste Mesta
 */
public class ReleaseWaitersJob extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private static JobSemaphore semaphore;

    private static String jobKey;

    @Override
    public void execute() {
        System.out.println("ReleaseWaitersJob EXECUTES at time " + new Date().getSeconds());
        if (semaphore != null) {
            semaphore.key = jobKey;
            semaphore.release();
        }
    }

    @Override
    public String getDescription() {
        return "release a semaphore";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        jobKey = (String) attributes.get("jobKey");
    }

    /*
     * create a new semphore and wait for the release to be called
     */
    public static void waitForJobToExecuteOnce() throws Exception {
        semaphore = new JobSemaphore(1);
        semaphore.acquire();
        boolean acquired = semaphore.tryAcquire(30, TimeUnit.SECONDS);
        if (!acquired) {
            throw new Exception("job was not triggered");
        }
    }

    public static void checkNotExecutedDuring(final int milliseconds) throws Exception {
        semaphore = new JobSemaphore(1);
        semaphore.acquire();
        boolean acquired = semaphore.tryAcquire(milliseconds, TimeUnit.MILLISECONDS);
        if (acquired) {
            throw new Exception("job " + jobKey + " was triggered");
        }
    }

}
