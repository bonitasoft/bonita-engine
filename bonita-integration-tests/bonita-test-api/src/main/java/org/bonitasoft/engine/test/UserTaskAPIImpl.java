/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.junit.After;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertNotNull;

/**
 * @author mazourd
 */
public class UserTaskAPIImpl implements UserTaskAPI {

    private ProcessAPI processAPI;
    private CommandAPI commandAPI;
    private APISession session;
    public static final int DEFAULT_TIMEOUT;
    public static final int DEFAULT_REPEAT_EACH = 500;

    static {
        final String strTimeout = System.getProperty("sysprop.bonita.default.test.timeout");
        if (strTimeout != null) {
            DEFAULT_TIMEOUT = Integer.valueOf(strTimeout);
        } else {
            DEFAULT_TIMEOUT = 2 * 60 * 1000;
        }
    }

    public UserTaskAPIImpl() {
    }

    public UserTaskAPIImpl(APISession session) {
        session = this.session;
    }

    public void usingSession(APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        this.session = session;
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));

    }

    @After
    public void logout() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, LogoutException, SessionNotFoundException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        loginAPI.logout(session);
        setSession(null);
        setProcessAPI(null);
        setCommandAPI(null);
    }

    public APISession getSession() {
        return session;
    }

    public void setSession(APISession session) {
        this.session = session;
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public void setProcessAPI(ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public CommandAPI getCommandAPI() {
        return commandAPI;
    }

    public void setCommandAPI(CommandAPI commandAPI) {
        this.commandAPI = commandAPI;
    }

    @Override
    public List<String> checkNoWaitingEvent() throws BonitaException {
        final List<String> messages = new ArrayList<String>();
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put("searchOptions", new SearchOptionsBuilder(0, 200).done());

        @SuppressWarnings("unchecked")
        final List<WaitingEvent> waitingEvents = ((SearchResult<WaitingEvent>) getCommandAPI().execute("searchWaitingEventsCommand", parameters)).getResult();
        if (waitingEvents.size() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Waiting Event are still present: ");
            for (final WaitingEvent waitingEvent : waitingEvents) {
                messageBuilder.append(
                        "[process instance:" + waitingEvent.getProcessName() + ", flow node instance:" + waitingEvent.getFlowNodeInstanceId() + "]").append(
                        ", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    private HumanTaskInstance getHumanTaskInstance(final Long id) throws ActivityInstanceNotFoundException, RetrieveException {
        if (id != null) {
            return getProcessAPI().getHumanTaskInstance(id);
        }
        throw new RuntimeException("no id returned for human task ");
    }

    @Override
    public HumanTaskInstance waitForUserTaskAndAssigneIt(final long processInstanceId, final String taskName, final User user) throws Exception {
        final HumanTaskInstance humanTaskInstance = waitForUserTaskAndGetIt(processInstanceId, taskName, DEFAULT_TIMEOUT);
        getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        return humanTaskInstance;
    }

    @Override
    public long waitForUserTaskAndExecuteIt(final long processInstanceId, final String taskName, final User user) throws Exception {
        final long humanTaskInstanceId = waitForUserTask(processInstanceId, taskName, DEFAULT_TIMEOUT);
        getProcessAPI().assignUserTask(humanTaskInstanceId, user.getId());
        getProcessAPI().executeFlowNode(humanTaskInstanceId);
        return humanTaskInstanceId;
    }

    @Override
    public HumanTaskInstance waitForUserTaskAndGetIt(final long processInstanceId, final String taskName, final int timeout) throws Exception {
        final long activityInstanceId = waitForUserTask(processInstanceId, taskName, timeout);
        final HumanTaskInstance getHumanTaskInstance = getHumanTaskInstance(activityInstanceId);
        assertNotNull(getHumanTaskInstance);
        return getHumanTaskInstance;
    }

    @Override
    public Long waitForFlowNode(final long processInstanceId, final TestStates state, final String flowNodeName, final boolean useRootProcessInstance,
            final int timeout) throws Exception {
        Map<String, Serializable> params;
        if (useRootProcessInstance) {
            params = ClientEventUtil.getFlowNodeInState(processInstanceId, state.getStateName(), flowNodeName);
        } else {
            params = ClientEventUtil.getFlowNodeInStateWithParentId(processInstanceId, state.getStateName(), flowNodeName);
        }
        return ClientEventUtil.executeWaitServerCommand(getCommandAPI(), params, timeout);
    }

    @Override
    public long waitForUserTask(long processInstanceId, String taskName, int timeout) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException, TimeoutException {
        int localTimeout = timeout;
        if (localTimeout <= 0) {
            localTimeout = DEFAULT_TIMEOUT;
        }
        final Map<String, Serializable> readyTaskEvent;
        if (processInstanceId > 0) {
            readyTaskEvent = ClientEventUtil.getReadyFlowNodeEvent(processInstanceId, taskName);
        } else {
            readyTaskEvent = ClientEventUtil.getReadyFlowNodeEvent(taskName);
        }
        return ClientEventUtil.executeWaitServerCommand(getCommandAPI(), readyTaskEvent, localTimeout);
    }

    @Override
    public void waitForProcessToBeInState(final long processInstanceId, final ProcessInstanceState state) throws Exception {
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceInState(processInstanceId, state.getId()),
                DEFAULT_TIMEOUT);
    }

    @Override
    public void waitForInitializingProcess() throws Exception {
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceInState(ProcessInstanceState.INITIALIZING.getId()),
                DEFAULT_TIMEOUT);
    }

    @Override
    public void waitForProcessToFinish(final long processInstanceId, final int timeout) throws Exception {
        int localTimeout = timeout;
        if (localTimeout <= 0) {
            localTimeout = DEFAULT_TIMEOUT;
        }
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceFinishedEvent(processInstanceId), localTimeout);
    }

    @Override
    public GatewayInstance waitForGateway(final ProcessInstance processInstance, final String name) throws Exception {
        final Map<String, Serializable> readyTaskEvent = ClientEventUtil.getFlowNode(processInstance.getId(), name);
        final Long activityInstanceId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(), readyTaskEvent, DEFAULT_REPEAT_EACH * DEFAULT_TIMEOUT);
        final FlowNodeInstance flowNodeInstance = getProcessAPI().getFlowNodeInstance(activityInstanceId);
        assertNotNull(flowNodeInstance);
        if (flowNodeInstance instanceof GatewayInstance) {
            return (GatewayInstance) flowNodeInstance;
        }
        return null;
    }

    @Override
    public void skipTask(final long activityId) throws UpdateException {
        getProcessAPI().setActivityStateByName(activityId, ActivityStates.SKIPPED_STATE);
    }

    @Override
    public void skipTasks(final long processId) throws UpdateException {
        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processId, 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final long activityInstanceId = activityInstance.getId();
            skipTask(activityInstanceId);
        }
    }

    public void forceMatchingOfEvents() throws CommandNotFoundException, CommandExecutionException, CommandParameterizationException {
        commandAPI.execute(ClientEventUtil.EXECUTE_EVENTS_COMMAND, Collections.<String, Serializable> emptyMap());
    }
    @Override
    public ActivityInstance waitForTaskToFail(final ProcessInstance processInstance) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getFlowNodeInState(processInstance.getId(), TestStates.FAILED.getStateName()), DEFAULT_TIMEOUT);
        return getActivityInstance(activityId);
    }


    private ActivityInstance getActivityInstance(final Long id) throws ActivityInstanceNotFoundException, RetrieveException {
        if (id != null) {
            return getProcessAPI().getActivityInstance(id);
        }
        throw new RuntimeException("no id returned for activity instance ");
    }

}
