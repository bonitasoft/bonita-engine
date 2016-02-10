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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods to get maps that match process as wanted
 * 
 * @author Baptiste Mesta
 */
public class PerfEventUtil {

    private static final String FLOW_NODE = "flowNode";

    private static final String PROCESS = "process";

    private static final String TYPE = "type";

    private static final String NAME = "name";

    private static final String STATE_ID = "stateId";

    private static final String ID = "id";

    public static Map<String, Serializable> getProcessInstanceFinishedEvent(final long processInstanceId) {
        final HashMap<String, Serializable> map = new HashMap<String, Serializable>(3);
        map.put(TYPE, PROCESS);
        map.put(ID, processInstanceId);
        map.put(STATE_ID, 6);
        return map;
    }

    public static Map<String, Serializable> getReadyTaskEvent(final long processInstanceId, final String taskName) {
        final HashMap<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, FLOW_NODE);
        map.put(ID, processInstanceId);
        map.put(NAME, taskName);
        map.put(STATE_ID, 4);
        return map;
    }

    public static Map<String, Serializable> getFlowNodeReachStateEvent(final long processInstanceId, final String taskName, final int stateId) {
        final HashMap<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, FLOW_NODE);
        map.put(ID, processInstanceId);
        map.put(NAME, taskName);
        map.put(STATE_ID, stateId);
        return map;
    }

}
