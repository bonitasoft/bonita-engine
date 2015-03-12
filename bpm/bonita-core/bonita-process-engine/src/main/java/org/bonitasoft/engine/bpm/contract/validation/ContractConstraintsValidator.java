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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final ContractVariableHelper contractVariableHelper;

    public ContractConstraintsValidator(final TechnicalLoggerService logger, final ConstraintsDefinitionHelper constraintsDefinitionHelper,
            final ContractVariableHelper contractVariableHelper) {
        this.logger = logger;
        this.constraintsDefinitionHelper = constraintsDefinitionHelper;
        this.contractVariableHelper = contractVariableHelper;
    }

    public void validate(final SContractDefinition contract, final Map<String, Serializable> variables) throws ContractViolationException {
        final List<String> comments = new ArrayList<String>();
        for (final SConstraintDefinition constraint : contract.getConstraints()) {
            log(TechnicalLogSeverity.DEBUG, "Evaluating constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames());
            if (isMandatoryConstraint(constraint)) {
                if (constraint.getInputNames().size() != 1) {
                    log(TechnicalLogSeverity.WARNING, "Constraint [" + constraint.getName() + "] inputNames are not valid");
                    comments.add("Constraint [" + constraint.getName() + "] inputNames are not valid");
                }
                else {
                    validateMandatoryContraint(comments, constraintsDefinitionHelper.getInputDefinition(contract, constraint.getInputNames().get(0)),
                            constraint, variables);
                }
            } else {
                validateContraint(comments, constraint, variables);
            }
        }
        if (!comments.isEmpty()) {
            throw new ContractViolationException("Error while validating constraints", comments);
        }
    }

    private void validateMandatoryContraint(final List<String> comments, final SInputDefinition sInputDefinition, final SConstraintDefinition constraint,
            final Map<String, Serializable> variables) {
        final List<Map<String, Serializable>> inputVariables = contractVariableHelper.buildMandatoryMultipleInputVariables(constraint, variables);
        for (final Map<String, Serializable> inputVariable : inputVariables) {
            if (sInputDefinition.isMultiple()) {
                final List<Map<String, Serializable>> multipleVariables = contractVariableHelper.convertMultipleToList(inputVariable);
                for (final Map<String, Serializable> multipleVariable : multipleVariables) {
                    validateContraint(comments, constraint, multipleVariable);
                }
            }
            else {
                validateContraint(comments, constraint, inputVariable);
            }
        }
    }

    private boolean isMandatoryConstraint(final SConstraintDefinition constraint) {
        if (constraint.getConstraintType() == null) {
            return false;
        }
        return constraint.getConstraintType().equals(SConstraintType.MANDATORY);
    }

    private void validateContraint(final List<String> comments, final SConstraintDefinition constraint, final Map<String, Serializable> variables) {
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

    private void log(final TechnicalLogSeverity severity, final String message) {
        if (logger.isLoggable(ContractConstraintsValidator.class, severity)) {
            logger.log(ContractConstraintsValidator.class, severity, message);
        }
    }
}
