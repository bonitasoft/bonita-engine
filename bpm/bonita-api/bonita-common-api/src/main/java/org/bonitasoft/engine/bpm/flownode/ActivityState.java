/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.flownode;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Matthieu Chaffotte
 */
@Deprecated
public enum ActivityState {

    READY, STARTED, EXECUTING, SUSPENDED, FINISHED, CANCELLED, ABORTED, SKIPPED, FAILED;

    public static Collection<ActivityState> getAllStates() {
        final Collection<ActivityState> taskStates = new HashSet<ActivityState>();
        taskStates.add(ActivityState.READY);
        taskStates.add(ActivityState.STARTED);
        taskStates.add(ActivityState.EXECUTING);
        taskStates.add(ActivityState.FINISHED);
        taskStates.add(ActivityState.SUSPENDED);
        taskStates.add(ActivityState.CANCELLED);
        taskStates.add(ActivityState.ABORTED);
        taskStates.add(ActivityState.SKIPPED);
        return taskStates;
    }

}
