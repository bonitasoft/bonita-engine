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
 * @author Christophe Havard
 */
public class SSchedulerHandlerImpl implements SHandler<SEvent> {

    public static final String SCHEDULER_STARTED = "SCHEDULER_STARTED";

    public static final String SCHEDULER_STOPPED = "SCHEDULER_STOPPED";

    private boolean isSchedulerStarted = false;

    @Override
    public void execute(final SEvent event) {
        final String type = event.getType();
        if (SCHEDULER_STARTED.compareToIgnoreCase(type) == 0) {
            isSchedulerStarted = true;
        } else if (SCHEDULER_STOPPED.compareToIgnoreCase(type) == 0) {
            isSchedulerStarted = false;
        }
    }

    @Override
    public boolean isInterested(final SEvent event) {
        final String type = event.getType();
        if (SCHEDULER_STARTED.compareToIgnoreCase(type) == 0) {
            return true;
        } else if (SCHEDULER_STOPPED.compareToIgnoreCase(type) == 0) {
            return true;
        }
        return false;
    }

    public boolean isSchedulerStarted() {
        return isSchedulerStarted;
    }

}
