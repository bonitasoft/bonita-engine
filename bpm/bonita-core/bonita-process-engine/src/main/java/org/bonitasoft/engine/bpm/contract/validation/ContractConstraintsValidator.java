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
import org.bonitasoft.engine.core.process.definition.model.SConstraintType;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.mvel2.MVEL;

public class ContractConstraintsValidator {

    private final TechnicalLoggerService logger;
    private final ConstraintsDefinitionHelper constraintsDefinitionHelper;

    public ContractConstraintsValidator(final TechnicalLoggerService logger, final ConstraintsDefinitionHelper constraintsDefinitionHelper) {
        this.logger = logger;
        this.constraintsDefinitionHelper = constraintsDefinitionHelper;
    }

    public void validate(final SContractDefinition contract, final Map<String, Object> variables) throws ContractViolationException {
        final List<String> comments = new ArrayList<String>();
        for (final SConstraintDefinition constraint : contract.getConstraints()) {
            log(TechnicalLogSeverity.DEBUG, "Evaluating constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames());
            if (isMandatoryConstraint(constraint)) {
                validateMandatoryContraint(comments, constraintsDefinitionHelper.getInputDefinition(contract, constraint.getName()), constraint, variables);
            } else {
                validateContraint(comments, constraint, variables);
            }
        }
        if (!comments.isEmpty()) {
            throw new ContractViolationException("Error while validating constraints", comments);
        }
    }

    @SuppressWarnings("unchecked")
    private void validateMandatoryContraint(final List<String> comments, final SInputDefinition sInputDefinition, final SConstraintDefinition constraint,
            final Map<String, Object> variables) {
        final Map<String, Object> inputVariables = buildMandatoryInputVariables(constraint, variables);
        if (sInputDefinition.isMultiple()) {
            final Object multipleInputVariable = variables.get(sInputDefinition.getName());
            if (multipleInputVariable != null && multipleInputVariable instanceof List<?>) {
                for (final Object variableValue : (List<Object>) multipleInputVariable) {
                    final Map<String, Object> variable = new HashMap<String, Object>();
                    variable.put(sInputDefinition.getName(), variableValue);
                    validateContraint(comments, constraint, variable);
                }
            } else {
                log(TechnicalLogSeverity.WARNING, "Constraint [" + constraint.getName() + "] on multiple " + constraint.getInputNames() + " is not valid");
            }
        }
        else {
            validateContraint(comments, constraint, inputVariables);
        }
    }

    private boolean isMandatoryConstraint(final SConstraintDefinition constraint) {
        if (constraint.getConstraintType() == null) {
            return false;
        }
        return constraint.getConstraintType().equals(SConstraintType.MANDATORY);
    }

    private void validateContraint(final List<String> comments, final SConstraintDefinition constraint, final Map<String, Object> variables) {
        Boolean valid = Boolean.FALSE;
        try {
            valid = MVEL.evalToBoolean(constraint.getExpression(), variables);
        } catch (final Exception e) {
            valid = Boolean.FALSE;
        }
        if (!valid) {
            log(TechnicalLogSeverity.WARNING, "Constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames() + " is not valid");
            comments.add(constraint.getExplanation());
        }
    }

    private Map<String, Object> buildMandatoryInputVariables(final SConstraintDefinition constraint, final Map<String, Object> variables) {
        final Map<String, Object> constraintValues = new HashMap<String, Object>();
        for (final String inputName : constraint.getInputNames()) {
            buildRecursiveVariable(variables, constraintValues, inputName);
        }
        return constraintValues;
    }

    private void buildRecursiveVariable(final Map<String, Object> variables, final Map<String, Object> constraintValues, final String inputName) {
        if (variables.containsKey(inputName)) {
            constraintValues.put(inputName, variables.get(inputName));
        } else {
            buildRecursiveComplexVariable(variables, constraintValues, inputName);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildRecursiveComplexVariable(final Map<String, Object> variables, final Map<String, Object> values, final String inputName) {
        for (final Entry<String, Object> variableEntry : variables.entrySet()) {
            final Object variableValue = variableEntry.getValue();
            if (variableValue instanceof Map<?, ?>) {
                final Map<String, Object> complexObject = new HashMap<String, Object>();
                complexObject.put(variableEntry.getKey(), variableEntry.getValue());
                buildRecursiveVariable((Map<String, Object>) variableValue, values, inputName);
            }
        }

    }

    private void log(final TechnicalLogSeverity severity, final String message) {
        if (logger.isLoggable(ContractConstraintsValidator.class, severity)) {
            logger.log(ContractConstraintsValidator.class, severity, message);
        }
    }
}
