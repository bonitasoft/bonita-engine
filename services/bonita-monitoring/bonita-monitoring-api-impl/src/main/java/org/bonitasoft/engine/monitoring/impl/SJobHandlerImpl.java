/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.monitoring.impl;

import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;

/**
 * * This handler will handle the following events :
 * <ul>
 * <li>JOB_EXECUTING = "JOB_EXECUTING"</li>
 * <li>JOB_FAILED = "JOB_FAILED"</li>
 * <li>JOB_COMPLETED = "JOB_COMPLETED"</li>
 * </ul>
 * 
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public class SJobHandlerImpl implements SHandler<SEvent> {

    public static final String JOB_EXECUTING = "JOB_EXECUTING";

    public static final String JOB_FAILED = "JOB_FAILED";

    public static final String JOB_COMPLETED = "JOB_COMPLETED";

    private int executingJobs = 0;

    private int completedJobs = 0;

    @Override
    public void execute(final SEvent event) {
        final String type = event.getType();
        if (JOB_EXECUTING.compareToIgnoreCase(type) == 0) {
            executingJobs++;
        } else if (JOB_COMPLETED.compareToIgnoreCase(type) == 0) {
            executingJobs--;
            completedJobs++;
        } else if (JOB_FAILED.compareToIgnoreCase(type) == 0) {
            executingJobs--;
        }
    }

    @Override
    public boolean isInterested(final SEvent event) {
        // FIXME filter by tenant id
        final String type = event.getType();
        if (JOB_COMPLETED.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (JOB_EXECUTING.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (JOB_FAILED.compareToIgnoreCase(type) == 0) {
            return true;
        }
        return false;
    }

    public int getExecutingJobs() {
        return executingJobs;
    }

}
