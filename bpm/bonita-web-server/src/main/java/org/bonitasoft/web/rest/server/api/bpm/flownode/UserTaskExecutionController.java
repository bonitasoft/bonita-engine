/**
 * Copyright (C) 2026 Bonitasoft S.A.
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

import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.console.common.server.utils.ContractTypeConverter;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.bonitasoft.web.rest.server.api.resource.ErrorMessageWithExplanations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user task execution.
 * Public URL: /API/bpm/userTask/{taskId}/execution
 * Internal path (via URL rewrite): /APISpringInternal/bpm/userTask/{taskId}/execution
 *
 * @author Emmanuel Duchastenier
 * @author Fabio Lombardi
 */
@Slf4j
@RestController
@RequestMapping("/" + API_SPRING_INTERNAL + "/bpm/userTask/{taskId}/execution")
public class UserTaskExecutionController extends AbstractRESTController {

    protected ContractTypeConverter typeConverterUtil = new ContractTypeConverter(
            ContractTypeConverter.ISO_8601_DATE_PATTERNS);

    /**
     * Executes a user task with contract inputs.
     *
     * @param taskId the ID of the user task to execute
     * @param userId optional user ID to execute the task for (defaults to session user)
     * @param assign whether to assign the task before execution
     * @param inputs contract inputs for the task
     * @param httpSession the HTTP session
     * @throws BonitaException if an error occurs during execution
     */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void executeTask(
            @PathVariable final long taskId,
            @RequestParam(name = "user", required = false) final Long userId,
            @RequestParam(name = "assign", defaultValue = "false") final boolean assign,
            @RequestBody(required = false) final Map<String, Serializable> inputs,
            final HttpSession httpSession) throws BonitaException, java.io.FileNotFoundException {

        final ProcessAPI processAPI = getProcessAPI(httpSession);
        final long effectiveUserId = userId != null ? userId : getApiSession(httpSession).getUserId();

        try {
            final ContractDefinition taskContract = processAPI.getUserTaskContract(taskId);
            final Map<String, Serializable> processedInputs = typeConverterUtil.getProcessedInput(taskContract, inputs,
                    getMaxSize());

            if (assign) {
                processAPI.assignAndExecuteUserTask(effectiveUserId, taskId, processedInputs);
            } else {
                processAPI.executeUserTask(effectiveUserId, taskId, processedInputs);
            }
            typeConverterUtil.deleteTemporaryFiles(inputs);
        } catch (final FlowNodeExecutionException e) {
            String errorMessage = "Unable to execute the task with ID " + taskId;
            log.error("{}. Caused by: {}", errorMessage, e.getMessage());
            // Avoid throwing original exception that may contain sensitive information unwanted in the HTTP response
            throw new FlowNodeExecutionException(errorMessage + " (consult the logs for more information).");
        } catch (final ContractViolationException e) {
            logContractViolation(e);
            throw e;
        }
    }

    // Visible for testing
    protected long getMaxSize() {
        return PropertiesFactory.getConsoleProperties().getMaxSize();
    }

    private void logContractViolation(final ContractViolationException e) {
        if (log.isInfoEnabled()) {
            final StringBuilder explanations = new StringBuilder();
            for (final String explanation : e.getExplanations()) {
                explanations.append(explanation).append("\n");
            }
            log.info("{}\nExplanations:\n{}", e.getSimpleMessage(), explanations);
        }
    }

    @ExceptionHandler(ContractViolationException.class)
    public ResponseEntity<ErrorMessageWithExplanations> handleContractViolation(ContractViolationException e) {
        final ErrorMessageWithExplanations errorMessage = new ErrorMessageWithExplanations(e);
        errorMessage.setMessage(e.getSimpleMessage());
        errorMessage.setExplanations(e.getExplanations());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

}
