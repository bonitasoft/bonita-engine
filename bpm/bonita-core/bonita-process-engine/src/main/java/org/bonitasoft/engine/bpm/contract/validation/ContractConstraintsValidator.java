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
package org.bonitasoft.engine.bpm.contract.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.mvel2.MVEL;

public class ContractConstraintsValidator {

    private final TechnicalLoggerService logger;

    public ContractConstraintsValidator(final TechnicalLoggerService logger) {
        this.logger = logger;
    }

    public void validate(final SContractDefinition contract, final Map<String, Serializable> variables) throws ContractViolationException {
        final List<String> comments = new ArrayList<String>();
        for (final SConstraintDefinition constraint : contract.getConstraints()) {
            log(TechnicalLogSeverity.DEBUG, "Evaluating constraint [" + constraint.getName() + "] on input(s) " + constraint.getInputNames());
            validateContraint(comments, constraint, variables);
        }
        if (!comments.isEmpty()) {
            throw new ContractViolationException("Error while validating constraints", comments);
        }
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
