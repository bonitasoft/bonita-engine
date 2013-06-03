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
package org.bonitasoft.engine.bpm.process;

import java.util.HashMap;
import java.util.Map;

public enum ProcessInstanceState {

    INITIALIZING(0), STARTED(1), SUSPENDED(2), CANCELLED(3), ABORTED(4), COMPLETING(5), COMPLETED(6), ERROR(7), ABORTING(11);

    private static Map<Integer, ProcessInstanceState> map = new HashMap<Integer, ProcessInstanceState>(11);

    private int id;

    private ProcessInstanceState(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ProcessInstanceState getFromId(final int id) {
        if (!map.containsKey(id)) {
            ProcessInstanceState state;
            switch (id) {
                case 0:
                    state = INITIALIZING;
                    break;
                case 1:
                    state = STARTED;
                    break;
                case 2:
                    state = SUSPENDED;
                    break;
                case 3:
                    state = CANCELLED;
                    break;
                case 4:
                    state = ABORTED;
                    break;
                case 5:
                    state = COMPLETING;
                    break;
                case 6:
                    state = COMPLETED;
                    break;
                case 7:
                    state = ERROR;
                    break;
                case 11:
                    state = ABORTING;
                    break;
                default:
                    state = null;
                    break;
            }
            map.put(id, state);
        }
        return map.get(id);
    }
}
