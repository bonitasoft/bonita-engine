/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.console.common.server.utils.ContractTypeConverter;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Duchastenier
 * @author Fabio Lombardi
 */
public class UserTaskExecutionResource extends CommonResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskExecutionResource.class.getName());

    static final String TASK_ID = "taskId";

    private static final String USER_PARAM = "user";

    private static final String ASSIGN = "assign";

    private final ProcessAPI processAPI;

    private final APISession apiSession;

    protected ContractTypeConverter typeConverterUtil = new ContractTypeConverter(
            ContractTypeConverter.ISO_8601_DATE_PATTERNS);

    public UserTaskExecutionResource(final ProcessAPI processAPI, final APISession apiSession) {
        this.processAPI = processAPI;
        this.apiSession = apiSession;
    }

    @Post("json")
    public void executeTask(final Map<String, Serializable> inputs)
            throws UserTaskNotFoundException, FlowNodeExecutionException, FileNotFoundException, UpdateException {
        final String userIdParameter = getRequestParameter(USER_PARAM);
        final long userId = userIdParameter != null ? Long.parseLong(userIdParameter) : apiSession.getUserId();
        final long taskId = getTaskIdParameter();
        boolean assign = Boolean.parseBoolean(getRequestParameter(ASSIGN));
        try {
            final ContractDefinition taskContract = processAPI.getUserTaskContract(taskId);
            final long maxSizeForTenant = PropertiesFactory.getConsoleProperties().getMaxSize();
            final Map<String, Serializable> processedInputs = typeConverterUtil.getProcessedInput(taskContract, inputs,
                    maxSizeForTenant);
            if (assign) {
                processAPI.assignAndExecuteUserTask(userId, taskId, processedInputs);
            } else {
                processAPI.executeUserTask(userId, taskId, processedInputs);
            }
            typeConverterUtil.deleteTemporaryFiles(inputs);
        } catch (FlowNodeExecutionException e) {
            String errorMessage = "Unable to execute the task with ID " + taskId;
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errorMessage + " Error: " + e.getMessage());
            }
            //Avoid throwing original exception that may contain sensitive information unwanted in the HTTP response
            throw new FlowNodeExecutionException(errorMessage + " (consult the logs for more information).");
        } catch (final ContractViolationException e) {
            manageContractViolationException(e, "Cannot execute task.");
        }
    }

    protected long getTaskIdParameter() {
        final String taskId = getAttribute(TASK_ID);
        if (taskId == null) {
            throw new APIException("Attribute '" + TASK_ID + "' is mandatory");
        }
        return Long.parseLong(taskId);
    }
}
