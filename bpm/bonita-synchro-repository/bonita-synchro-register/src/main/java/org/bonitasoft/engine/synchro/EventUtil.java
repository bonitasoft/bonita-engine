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

import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;

/**
 * Utility methods to get maps that match process as wanted
 *
 * @author Baptiste Mesta
 */
public class EventUtil {

    private static final String FLOW_NODE = "flowNode";

    private static final String PROCESS = "process";

    private static final String TYPE = "type";

    private static final String NAME = "name";

    private static final String ROOT_CONTAINER_ID = "rootContainerId";

    private static final String PARENT_CONTAINER_ID = "parentContainerId";

    private static final String PROCESS_DEFINITION_ID = "processDefinitionId";

    private static final String STATE_ID = "stateId";

    private static final String ID = "id";

    private static final String STATE = "state";

    public static Map<String, Serializable> getEventForProcess(final SProcessInstance instance) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, PROCESS);
        map.put(ID, instance.getId());
        map.put(STATE_ID, instance.getStateId());
        map.put(PROCESS_DEFINITION_ID, instance.getProcessDefinitionId());
        map.put(NAME, instance.getName());
        return map;
    }

    public static Map<String, Serializable> getEventForFlowNode(final SFlowNodeInstance flowNodeInstance) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(5);
        map.put(TYPE, FLOW_NODE);
        map.put(ID, flowNodeInstance.getId());
        map.put(ROOT_CONTAINER_ID, flowNodeInstance.getRootContainerId());
        map.put(PARENT_CONTAINER_ID, flowNodeInstance.getParentContainerId());
        map.put(NAME, flowNodeInstance.getName());
        map.put(STATE_ID, flowNodeInstance.getStateId());
        map.put(STATE, flowNodeInstance.getStateName());
        return map;
    }

}
