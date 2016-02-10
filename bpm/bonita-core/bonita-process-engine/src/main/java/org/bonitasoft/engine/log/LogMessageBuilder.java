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
package org.bonitasoft.engine.log;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Elias Ricken de Medeiros
 */
public class LogMessageBuilder {

    /**
     * Build the log message using the flow node's context (display name, id, parent activity id, parent process instance id, root process instance id, and
     * process definition id)
     * 
     * @param flowNodeInstance
     * @return the message log built using the flow node's context.
     */
    public static String buildFlowNodeContextMessage(final SFlowNodeInstance flowNodeInstance) {
        StringBuilder stb = new StringBuilder();
        stb.append(" [name = <");
        stb.append(flowNodeInstance.getName());
        stb.append(">, display name = <");
        stb.append(flowNodeInstance.getDisplayName());
        stb.append(">, id = <");
        stb.append(flowNodeInstance.getId());
        if (flowNodeInstance.getParentActivityInstanceId() > 0) {
            stb.append(">, parent activity instance = <");
            stb.append(flowNodeInstance.getParentActivityInstanceId());
        }
        stb.append(">, parent process instance = <");
        stb.append(flowNodeInstance.getParentProcessInstanceId());
        stb.append(">, root process instance = <");
        stb.append(flowNodeInstance.getRootProcessInstanceId());
        stb.append(">, process definition = <");
        stb.append(flowNodeInstance.getProcessDefinitionId());
        stb.append(">]");
        return stb.toString();
    }

    public static String buildExecuteTaskContextMessage(final SFlowNodeInstance flowNodeInstance, final String username, final long executerUserId,
            final long executerSubstituteId, Map<String, Serializable> inputs) {
        final StringBuilder stb = new StringBuilder();
        stb.append("The user <" + username + "> ");
        if (executerUserId != executerSubstituteId) {
            stb.append("acting as delegate of the user with id = <" + executerUserId + "> ");
        }
        stb.append("has executed the task");
        stb.append(LogMessageBuilder.buildFlowNodeContextMessage(flowNodeInstance));
        if (inputs != null) {
            stb.append(" with task inputs: " + inputs);
        }
        return stb.toString();
    }

    /**
     * Build message "The user <session.getUsername> (acting as delegate of user with id <starterId>)"
     * 
     * @param session
     * @param starterId
     * @return
     */
    public static String builUserActionPrefix(final SSession session, final long starterId) {
        final StringBuilder stb = new StringBuilder();
        stb.append("The user <");
        stb.append(session.getUserName());
        stb.append("> ");
        if (starterId != session.getUserId()) {
            stb.append("acting as delegate of the user with id = <");
            stb.append(starterId);
            stb.append("> ");
        }
        return stb.toString();
    }

}
