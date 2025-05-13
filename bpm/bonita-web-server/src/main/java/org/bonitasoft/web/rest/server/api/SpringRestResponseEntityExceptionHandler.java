/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataCrudOperationException;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class SpringRestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { TenantStatusException.class })
    protected ResponseEntity<Object> handleMaintenanceMode(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, "Platform under maintenance",
                new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    @ExceptionHandler(value = { InvalidSessionException.class })
    protected ResponseEntity<Object> handleInvalidSession(RuntimeException ex, WebRequest request,
            HttpSession httpSession) {
        SessionUtil.sessionLogout(httpSession);
        return handleExceptionInternal(ex, "Invalid session",
                new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> defaultToInternalServerError(Exception ex, WebRequest request,
            HttpSession httpSession) {
        log.error("Server-side error", ex);
        return handleExceptionInternal(ex, "Internal server error",
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private static final Map<String, String> parameterErrorNames = Map.of("c", "count", "p", "page");

    @ExceptionHandler(value = { MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Object> handleInvalidParameters(HttpServletRequest req,
            MethodArgumentTypeMismatchException ex) {
        // replicate the error message produced by former API written with Restlet (see CommonResource)
        String parameterName = ex.getName();
        if (log.isDebugEnabled()) {
            String error = "Invalid parameter [" + req.getPathInfo() + "] " + parameterName + ": " + ex.getMessage();
            log.debug(error);
        }

        String mapping = parameterErrorNames.get(parameterName);
        if (mapping != null) {
            return bonitaHandleException(
                    new IllegalArgumentException(
                            "query parameter " + parameterName + " (" + mapping + ") should be a number"),
                    HttpStatus.BAD_REQUEST);
        }

        Object value = ex.getValue();
        Class<?> requiredType = ex.getRequiredType();
        boolean isNumber = Integer.class.equals(requiredType) || Long.class.equals(requiredType);
        if (isNumber) {
            return bonitaHandleException(new IllegalArgumentException("[ " + value + " ] must be a number"),
                    HttpStatus.BAD_REQUEST);
        }

        return bonitaHandleException(new IllegalArgumentException("Bad parameter " + parameterName + "=" + value),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { NotFoundException.class })
    public ResponseEntity<Object> handleNotFound(NotFoundException exception) {
        return bonitaHandleException(exception, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { ExecutionException.class })
    public ResponseEntity<Object> handleExecutionException(ExecutionException exception) {
        if (exception instanceof CommandExecutionException) {
            Throwable wrapped = exception.getCause();
            // TODO remove, not used in production
            // BusinessDataControllerTest requires it, but in reality, it is not used because the exception is wrapped in another exception (see the code right after this block)
            if (wrapped instanceof DataNotFoundException) {
                return bonitaHandleException(wrapped, HttpStatus.NOT_FOUND);
            }

            final Throwable causedByIsDataNotFoundException = getFirstCauseOfType(wrapped, NotFoundException.class);
            if (causedByIsDataNotFoundException != null) {
                return bonitaHandleException(causedByIsDataNotFoundException, HttpStatus.NOT_FOUND);
            }

            final Throwable causedByBusinessDataCrudOperationException = getFirstCauseOfType(wrapped,
                    BusinessDataCrudOperationException.class);
            if (causedByBusinessDataCrudOperationException != null) {
                return generateErrorResponse(causedByBusinessDataCrudOperationException, HttpStatus.BAD_REQUEST,
                        causedByBusinessDataCrudOperationException.getMessage());
            }
        }

        return bonitaHandleException(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private <T extends Throwable> Throwable getFirstCauseOfType(Throwable exception, Class<T> exceptionTypeToSearch) {
        if (exception == null) {
            return null;
        }
        if (exceptionTypeToSearch.isAssignableFrom(exception.getClass())) {
            return exception;
        }
        return getFirstCauseOfType(exception.getCause(), exceptionTypeToSearch);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        // replicate the error message produced by former API written with Restlet (see CommonResource)
        String parameterName = exception.getParameterName();

        String mapping = parameterErrorNames.get(parameterName);
        String message;
        if (mapping != null) {
            message = "query parameter " + parameterName + " (" + mapping + ") is mandatory";
        } else {
            message = "query parameter " + parameterName + " is mandatory";
        }

        return bonitaHandleException(new IllegalArgumentException(message), HttpStatus.BAD_REQUEST);
    }

    private static ResponseEntity<Object> bonitaHandleException(Throwable exception, HttpStatus status) {
        // replicate the behaviour of former API written with Restlet (see CommonResource)
        final Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        return generateErrorResponse(exception, status, cause.getMessage());
    }

    private static ResponseEntity<Object> generateErrorResponse(Throwable exception, HttpStatus status,
            String message) {
        Map<String, String> response = new HashMap<>();
        response.put("exception", "class " + exception.getClass().getName());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

}
