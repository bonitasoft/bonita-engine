/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static java.lang.String.format;
import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.console.common.server.utils.ContractTypeConverter;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * REST controller for process instantiation.
 * Public URL: /API/bpm/process/{processDefinitionId}/instantiation
 * Internal path (via URL rewrite): /APISpringInternal/bpm/process/{processDefinitionId}/instantiation
 *
 * @author Nicolas Tith
 */
@RestController
@ConditionalOnSingleCandidate(ProcessInstantiationController.class)
@RequestMapping("/" + API_SPRING_INTERNAL + "/bpm/process/{processDefinitionId}/instantiation")
public class ProcessInstantiationController extends AbstractRESTController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstantiationController.class);
    private static final String CASE_ID_ATTRIBUTE = "caseId";
    private static final long NO_RETRY_AFTER = -1L;
    protected final ContractTypeConverter typeConverterUtil = new ContractTypeConverter(
            ContractTypeConverter.ISO_8601_DATE_PATTERNS);

    @PostMapping
    public ResponseEntity<String> instantiateProcess(
            @PathVariable final long processDefinitionId,
            @RequestParam(name = "user", required = false) final Long userId,
            @RequestBody(required = false) final Map<String, Serializable> inputs,
            final HttpSession httpSession,
            final HttpServletResponse response)
            throws BonitaException, FileNotFoundException {
        final ProcessAPI processAPI = getProcessAPI(httpSession);

        try {
            final ContractDefinition processContract = processAPI.getProcessContract(processDefinitionId);
            final long maxSizeForTenant = getMaxFileSize();
            final Map<String, Serializable> processedInputs = typeConverterUtil.getProcessedInput(processContract,
                    inputs, maxSizeForTenant);

            long processInstanceId;
            if (userId == null) {
                processInstanceId = processAPI.startProcessWithInputs(processDefinitionId, processedInputs).getId();
            } else {
                processInstanceId = processAPI.startProcessWithInputs(userId, processDefinitionId, processedInputs)
                        .getId();
            }

            final ObjectNode returnedObject = JsonNodeFactory.instance.objectNode();
            returnedObject.put(CASE_ID_ATTRIBUTE, processInstanceId);
            return ResponseEntity.ok(returnedObject.toString());
        } catch (final ProcessExecutionException e) {
            String errorMessage = "Unable to start the process with ID " + processDefinitionId;
            LOGGER.error("{}. Caused by: {}", errorMessage, e.getMessage());
            if (e.getRetryAfter() != NO_RETRY_AFTER) {
                // Return a 429 status code with Retry-After header to indicate the client
                // that he should retry later in case of case creation limit reached
                response.addHeader(HttpHeaders.RETRY_AFTER, new Date(e.getRetryAfter()).toString());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Case creation limit reached.");
            }
            // Avoid throwing original exception that may contain sensitive information unwanted in the HTTP response
            throw new ProcessExecutionException(errorMessage + " (consult the logs for more information).");
        } catch (final ContractViolationException e) {
            // Re-throw as a specific exception to be handled by the global exception
            // handler
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            // clean temp files
            typeConverterUtil.deleteTemporaryFiles(inputs);
        }
    }

    // Visible for testing
    protected long getMaxFileSize() {
        return PropertiesFactory.getConsoleProperties().getMaxSize();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if ("user".equals(ex.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(format("'user' URL query parameter should be Integer. Received '%s'", ex.getValue()));
        }
        // Re-throw for other parameters
        throw ex;
    }

}
