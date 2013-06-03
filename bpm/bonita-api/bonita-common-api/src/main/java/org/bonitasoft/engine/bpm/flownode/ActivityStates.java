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

import java.util.HashSet;
import java.util.Set;

/**
 * Temporary class to be substituted by ActivityStateService.
 * 
 * @author Emmanuel Duchastenier
 */
public class ActivityStates {

    public static final String FAILED_STATE = "failed";

    public static final String INITIALIZING_STATE = "initializing";

    public static final String READY_STATE = "ready";

    public static final String EXECUTING_STATE = "executing";

    public static final String COMPLETING_STATE = "completing";

    public static final String COMPLETED_STATE = "completed";

    public static final String WAITING_STATE = "waiting";

    public static final String SKIPPED_STATE = "skipped";

    public static final String CANCELLED_STATE = "cancelled";

    public static final String ABORTED_STATE = "aborted";

    public static final String CANCELLING_SUBTASKS_STATE = "cancelling subtasks";

    public static final Set<String> STATES = new HashSet<String>();

    static {
        STATES.add(FAILED_STATE);
        STATES.add(INITIALIZING_STATE);
        STATES.add(READY_STATE);
        STATES.add(EXECUTING_STATE);
        STATES.add(COMPLETING_STATE);
        STATES.add(COMPLETED_STATE);
        STATES.add(WAITING_STATE);
        STATES.add(SKIPPED_STATE);
        STATES.add(CANCELLED_STATE);
        STATES.add(CANCELLING_SUBTASKS_STATE);
        STATES.add(ABORTED_STATE);
    }
}
