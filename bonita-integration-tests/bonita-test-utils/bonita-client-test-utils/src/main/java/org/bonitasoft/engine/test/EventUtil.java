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
package org.bonitasoft.engine.test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.session.APISession;

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

    private static final String STATE_ID = "stateId";

    private static final String ID = "id";

    private static final String STATE = "state";

    public static Map<String, Serializable> getReadyTaskEvent(final long processInstanceId, final String taskName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(5);
        map.put(TYPE, FLOW_NODE);
        map.put(ROOT_CONTAINER_ID, processInstanceId);
        map.put(NAME, taskName);
        map.put(STATE_ID, 4);
        return map;
    }

    public static Map<String, Serializable> getProcessInstanceFinishedEvent(final long processInstanceId) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, PROCESS);
        map.put(ID, processInstanceId);
        map.put(STATE_ID, 6);
        return map;
    }

    public static void undeployCommand(final APISession apiSession) throws BonitaException {
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(apiSession);
        commandAPI.unregister("waitServerCommand");
        commandAPI.removeDependency("commands");
    }

    public static void deployCommand(final APISession apiSession) throws BonitaException {
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(apiSession);

        byte[] commandJar;
        try {
            commandJar = IOUtil.getAllContentFrom(APITestUtil.class.getResourceAsStream("/server-command.bak"));
        } catch (final IOException e) {
            throw new RetrieveException(e);
        }
        commandAPI.addDependency("commands", commandJar);

        commandAPI.register("waitServerCommand", "waitServerCommand", "org.bonitasoft.engine.test.synchro.WaitServerCommand");
        commandAPI.register("addHandlerCommand", "addHandlerCommand", "org.bonitasoft.engine.test.synchro.AddHandlerCommand");

        final Map<String, Serializable> parameters = Collections.emptyMap();
        commandAPI.execute("addHandlerCommand", parameters);
    }

    static Long executeWaitServerCommand(final CommandAPI commandAPI, final Map<String, Serializable> event, final int defaultTimeout)
            throws CommandNotFoundException, CommandParameterizationException, CommandExecutionException, TimeoutException {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        parameters.put("event", (Serializable) event);
        parameters.put("timeout", defaultTimeout);
        try {
            return (Long) commandAPI.execute("waitServerCommand", parameters);
        } catch (final CommandExecutionException e) {
            if (e.getMessage().toLowerCase().contains("timeout")) {
                throw new TimeoutException("Timeout (" + defaultTimeout + "ms)looking for element: " + event);
            }
            throw e;
        }
    }

    public static void clearRepo(final CommandAPI commandAPI) throws BonitaException {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("clear", true);
        commandAPI.execute("waitServerCommand", parameters);
    }

    public static Map<String, Serializable> getTaskInState(final long processInstanceId, final String stateName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(3);
        map.put(TYPE, FLOW_NODE);
        map.put(ROOT_CONTAINER_ID, processInstanceId);
        map.put(STATE, stateName);
        return map;
    }

    public static Map<String, Serializable> getTaskInState(final long processInstanceId, final String state, final String flowNodeName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(3);
        map.put(TYPE, FLOW_NODE);
        map.put(ROOT_CONTAINER_ID, processInstanceId);
        map.put(STATE, state);
        map.put(NAME, flowNodeName);
        return map;
    }

    public static Map<String, Serializable> getTaskInStateWithParentId(final long processInstanceId, final String state, final String flowNodeName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(3);
        map.put(TYPE, FLOW_NODE);
        map.put(PARENT_CONTAINER_ID, processInstanceId);
        map.put(STATE, state);
        map.put(NAME, flowNodeName);
        return map;
    }

}
