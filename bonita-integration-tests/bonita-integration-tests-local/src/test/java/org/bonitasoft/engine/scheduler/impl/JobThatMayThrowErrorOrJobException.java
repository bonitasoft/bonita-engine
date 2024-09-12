/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.scheduler.job.GroupJob;
import org.bonitasoft.engine.scheduler.job.VariableStorage;

public class JobThatMayThrowErrorOrJobException extends GroupJob {

    static final String TYPE = "type";
    static final String ERROR = "ERROR";
    static final String JOBEXCEPTION = "JOBEXCEPTION";
    static final String NO_EXCEPTION = "NO_EXCEPTION";
    static final String FAIL_ONCE = "FAIL_ONCE";
    static final String FAIL_ONCE_WITH_RETRYABLE = "FAIL_ONCE_WITH_RETRYABLE";
    private boolean throwsJobExecutionException;
    private boolean throwsError;
    private boolean failOnce;
    private boolean failOnceWithRetryable;
    private final VariableStorage variableStorage = VariableStorage.getInstance();

    @Override
    public String getDescription() {
        return "throw error";
    }

    @Override
    public void setAttributes(Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        Serializable type = attributes.get(TYPE);
        throwsJobExecutionException = JOBEXCEPTION.equals(type);
        throwsError = ERROR.equals(type);
        failOnce = FAIL_ONCE.equals(type);
        failOnceWithRetryable = FAIL_ONCE_WITH_RETRYABLE.equals(type);
    }

    @Override
    public void execute() throws SJobExecutionException {
        System.err.println(
                "Executing job:"
                        + failOnce + ", "
                        + failOnceWithRetryable + ", "
                        + throwsError + ", "
                        + throwsJobExecutionException);
        if (failOnce) {
            if (variableStorage.getVariableValue("nbJobException", 0) == 0) {
                variableStorage.setVariable("nbJobException", 1);
                throw new SJobExecutionException("Failing only once");
            }
        }
        if (failOnceWithRetryable) {
            if (variableStorage.getVariableValue("nbJobException", 0) == 0) {
                variableStorage.setVariable("nbJobException", 1);
                throw new SRetryableException("Failing only once");
            }
        }
        if (throwsError) {
            variableStorage.setVariable("nbError", variableStorage.getVariableValue("nbError", 0) + 1);
            throw new Error("an Error");
        }
        if (throwsJobExecutionException) {
            variableStorage.setVariable("nbJobException", variableStorage.getVariableValue("nbJobException", 0) + 1);
            throw new SJobExecutionException("a Job exception");
        }
        variableStorage.setVariable("nbSuccess", variableStorage.getVariableValue("nbSuccess", 0) + 1);
    }
}
