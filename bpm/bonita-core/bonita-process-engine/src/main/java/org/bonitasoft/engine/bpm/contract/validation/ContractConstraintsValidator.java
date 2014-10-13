/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.mvel2.MVEL;

public class ContractConstraintsValidator {

    private final TechnicalLoggerService logger;

    public ContractConstraintsValidator(final TechnicalLoggerService logger) {
        this.logger = logger;
    }

    public void validate(final List<SConstraintDefinition> constraints, final Map<String, Object> variables) throws ContractViolationException {
        final List<String> comments = new ArrayList<String>();
        for (final SConstraintDefinition constraint : constraints) {
            log(TechnicalLogSeverity.DEBUG, "Evaluating constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames());
            Boolean valid;
            try {
                valid = evaluateContraintExpression(variables, constraint);
            } catch (final Exception e) {
                valid = Boolean.FALSE;
            }
            if (!valid) {
                log(TechnicalLogSeverity.WARNING, "Constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames() + " is not valid");
                comments.add(constraint.getExplanation());
            }
        }

        if (!comments.isEmpty()) {
            throw new ContractViolationException("Error while validating constraints", comments);
        }
    }

    private Boolean evaluateContraintExpression(final Map<String, Object> variables, final SConstraintDefinition constraint) {
        final Map<String, Object> injectVariables = injectVariables(constraint, variables);
        return MVEL.evalToBoolean(constraint.getExpression(), injectVariables);
    }

    private Map<String, Object> injectVariables(final SConstraintDefinition constraint, final Map<String, Object> variables) {
        final Map<String, Object> constraintValues = new HashMap<String, Object>();
        for (final String inputName : constraint.getInputNames()) {
            injectVariable(variables, constraintValues, inputName);
        }
        return constraintValues;
    }

    private void injectVariable(final Map<String, Object> variables, final Map<String, Object> constraintValues, final String inputName) {
        if (variables.containsKey(inputName)) {
            constraintValues.put(inputName, variables.get(inputName));
        } else {
            injectFromComplexInput(variables, constraintValues, inputName);
        }
    }

    @SuppressWarnings("unchecked")
    private void injectFromComplexInput(final Map<String, Object> variables, final Map<String, Object> values, final String inputName) {
        for (final Entry<String, Object> variableEntry : variables.entrySet()) {
            final Object variableValue = variableEntry.getValue();
            if (variableValue instanceof Map<?, ?>) {
                final Map<String, Object> complexObject = new HashMap<String, Object>();
                complexObject.put(variableEntry.getKey(), variableEntry.getValue());
                injectVariable((Map<String, Object>) variableValue, values, inputName);
            }
        }

    }

    private void log(final TechnicalLogSeverity severity, final String message) {
        if (logger.isLoggable(ContractConstraintsValidator.class, severity)) {
            logger.log(ContractConstraintsValidator.class, severity, message);
        }
    }
}
