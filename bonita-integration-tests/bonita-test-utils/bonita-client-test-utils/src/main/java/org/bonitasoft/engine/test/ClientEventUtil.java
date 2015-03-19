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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to get maps that match process as wanted
 *
 * @author Baptiste Mesta
 */
public class ClientEventUtil {

    public static final String EXECUTE_EVENTS_COMMAND = "executeEventsCommand";

    public static final String ADD_HANDLER_COMMAND = "addHandlerCommand";

    public static final String WAIT_SERVER_COMMAND = "waitServerCommand";

    private static final String FLOW_NODE = "flowNode";

    private static final String PROCESS = "process";

    private static final String TYPE = "type";

    private static final String NAME = "name";

    private static final String ROOT_CONTAINER_ID = "rootContainerId";

    private static final String PARENT_CONTAINER_ID = "parentContainerId";

    private static final String STATE_ID = "stateId";

    private static final String ID = "id";

    private static final String STATE = "state";

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientEventUtil.class.getName());

    public static void undeployCommand(final APISession apiSession) throws BonitaException {
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(apiSession);
        try {
            commandAPI.unregister(WAIT_SERVER_COMMAND);
            commandAPI.unregister(ADD_HANDLER_COMMAND);
            commandAPI.unregister(EXECUTE_EVENTS_COMMAND);
            LOGGER.debug("commands undeployed");
        } catch (final BonitaException e) {
            // ok
            LOGGER.debug("error undeploying command (maybe already undeployed?) " + e.getMessage());
        }
    }

    public static void deployCommand(final APISession apiSession) throws BonitaException {
        final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(apiSession);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);

        final CommandDescriptor waitServerCommand = commandAPI.register(WAIT_SERVER_COMMAND, WAIT_SERVER_COMMAND,
                "org.bonitasoft.engine.synchro.WaitServerCommand");
        final CommandDescriptor addHandlerCommand = commandAPI.register(ADD_HANDLER_COMMAND, ADD_HANDLER_COMMAND,
                "org.bonitasoft.engine.synchro.AddHandlerCommand");
        final CommandDescriptor executeEventsCommand = commandAPI.register(EXECUTE_EVENTS_COMMAND, EXECUTE_EVENTS_COMMAND,
                "org.bonitasoft.engine.jobs.ExecuteEventHandlingNow");

        parameters.put("commands", (Serializable) Arrays.asList(waitServerCommand.getId(), addHandlerCommand.getId(), executeEventsCommand.getId()));
        commandAPI.execute(ADD_HANDLER_COMMAND, parameters);
        LOGGER.debug("commands deployed");
    }

    public static Long executeWaitServerCommand(final CommandAPI commandAPI, final Map<String, Serializable> event, final int defaultTimeout)
            throws CommandNotFoundException, CommandParameterizationException, CommandExecutionException, TimeoutException {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        parameters.put("event", (Serializable) event);
        parameters.put("timeout", defaultTimeout);
        try {
            return (Long) commandAPI.execute(WAIT_SERVER_COMMAND, parameters);
        } catch (final CommandExecutionException e) {
            LOGGER.error("error while executing wait server command for element:" + event, e);
            if (e.getMessage().toLowerCase().contains("timeout")) {
                throw new TimeoutException("Timeout (" + defaultTimeout + " ms) when looking for element: " + event);
            }
            throw e;
        }
    }

    public static void clearRepo(final CommandAPI commandAPI) {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put("clear", true);
        try {
            commandAPI.execute(WAIT_SERVER_COMMAND, parameters);
        } catch (final BonitaException e) {
            // ok
            LOGGER.debug("error undeploying command (maybe already undeployed?) " + e.getMessage());
        }
    }

    public static Map<String, Serializable> getReadyFlowNodeEvent(final long processInstanceId, final String taskName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(5);
        map.put(TYPE, FLOW_NODE);
        map.put(ROOT_CONTAINER_ID, processInstanceId);
        map.put(NAME, taskName);
        map.put(STATE_ID, 4);
        return map;
    }

    public static Map<String, Serializable> getReadyFlowNodeEvent(final String taskName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(5);
        map.put(TYPE, FLOW_NODE);
        map.put(NAME, taskName);
        map.put(STATE_ID, 4);
        return map;
    }

    public static Map<String, Serializable> getFlowNodeInState(final String state, final String taskName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(5);
        map.put(TYPE, FLOW_NODE);
        map.put(NAME, taskName);
        map.put(STATE, state);
        return map;
    }

    public static Map<String, Serializable> getFlowNodeInState(final long rootContainerId, final String state, final String flowNodeName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, FLOW_NODE);
        map.put(ROOT_CONTAINER_ID, rootContainerId);
        map.put(STATE, state);
        map.put(NAME, flowNodeName);
        return map;
    }

    public static Map<String, Serializable> getFlowNode(final long rootContainerId, final String flowNodeName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, FLOW_NODE);
        map.put(ROOT_CONTAINER_ID, rootContainerId);
        map.put(NAME, flowNodeName);
        return map;
    }

    public static Map<String, Serializable> getFlowNodeInState(final long rootContainerId, final String stateName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(3);
        map.put(TYPE, FLOW_NODE);
        map.put(ROOT_CONTAINER_ID, rootContainerId);
        map.put(STATE, stateName);
        return map;
    }

    public static Map<String, Serializable> getFlowNodeInStateWithParentId(final long processInstanceId, final String state, final String flowNodeName) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, FLOW_NODE);
        map.put(PARENT_CONTAINER_ID, processInstanceId);
        map.put(STATE, state);
        map.put(NAME, flowNodeName);
        return map;
    }

    public static Map<String, Serializable> getProcessInstanceInState(final long processInstanceId, final int stateId) {
        final HashMap<String, Serializable> map = new HashMap<String, Serializable>(3);
        map.put(TYPE, PROCESS);
        map.put(ID, processInstanceId);
        map.put(STATE_ID, stateId);
        return map;
    }

    public static Map<String, Serializable> getProcessInstanceInState(final int stateId) {
        final HashMap<String, Serializable> map = new HashMap<String, Serializable>(3);
        map.put(TYPE, PROCESS);
        map.put(STATE_ID, stateId);
        return map;
    }

    public static Map<String, Serializable> getProcessInstanceFinishedEvent(final long processInstanceId) {
        final Map<String, Serializable> map = new HashMap<String, Serializable>(4);
        map.put(TYPE, PROCESS);
        map.put(ID, processInstanceId);
        map.put(STATE_ID, 6);
        return map;
    }

}
