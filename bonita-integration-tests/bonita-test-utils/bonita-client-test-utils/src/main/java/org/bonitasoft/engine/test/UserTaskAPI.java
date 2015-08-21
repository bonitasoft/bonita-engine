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

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.session.APISession;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author mazourd
 */
public interface UserTaskAPI {

    Long waitForFlowNode(final long processInstanceId, final TestStates state, final String flowNodeName, final boolean useRootProcessInstance,
            final int timeout) throws Exception;

    ActivityInstance waitForTaskToFail(final ProcessInstance processInstance) throws Exception;

    void waitForInitializingProcess() throws Exception;

    void waitForProcessToBeInState(final long processInstanceId, final ProcessInstanceState state) throws Exception;

    void waitForProcessToFinish(final long processInstanceId, final int timeout) throws Exception;

    List<String> checkNoWaitingEvent() throws BonitaException;

    GatewayInstance waitForGateway(final ProcessInstance processInstance, final String name) throws Exception;

    void usingSession(APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException;

    HumanTaskInstance waitForUserTaskAndGetIt(final long processInstanceId, final String taskName, final int timeout) throws Exception;

    long waitForUserTask(long processInstanceId, String taskName, int timeout) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException, TimeoutException;

    long waitForUserTaskAndExecuteIt(final long processInstanceId, final String taskName, final User user) throws Exception;

    HumanTaskInstance waitForUserTaskAndAssigneIt(final long processInstanceId, final String taskName, final User user) throws Exception;

    void skipTask(final long activityId) throws UpdateException;

    void skipTasks(final long processId) throws UpdateException;
}
