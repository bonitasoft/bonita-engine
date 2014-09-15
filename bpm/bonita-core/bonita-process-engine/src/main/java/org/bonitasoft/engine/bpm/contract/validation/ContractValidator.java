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
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.mvel2.MVEL;

/**
 * @author Matthieu Chaffotte
 */
public class ContractValidator {

    private final TechnicalLoggerService logger;
    private final ContractStructureValidator contractStructureValidator;

    private final List<String> comments;

    public ContractValidator(final ContractStructureValidator contractStructureValidator, final TechnicalLoggerService loggerService) {
        comments = new ArrayList<String>();
        this.contractStructureValidator = contractStructureValidator;
        this.logger = loggerService;
    }

    public boolean isValid(final SContractDefinition contract, final Map<String, Object> variables) {
        comments.clear();

        try {
            contractStructureValidator.validate(contract, variables);
        } catch (ContractViolationException e) {
            comments.addAll(e.getExplanations());
        }
        final List<SRuleDefinition> rules = contract.getRules();
        if (comments.isEmpty()) {
            validateRules(variables, rules);
        }
        return comments.isEmpty();
    }

    private void validateRules(final Map<String, Object> variables, final List<SRuleDefinition> rules) {
        for (final SRuleDefinition rule : rules) {
            log(TechnicalLogSeverity.DEBUG, "Evaluating rule [" + rule.getName() + "] on input(s) " + rule.getInputNames());
            Boolean valid;
            try {
                valid = MVEL.evalToBoolean(rule.getExpression(), variables);
            } catch (final Exception e) {
                valid = Boolean.FALSE;
            }
            if (!valid) {
                log(TechnicalLogSeverity.WARNING, "Rule [" + rule.getName() + "] on input(s) " + rule.getInputNames() + " is not valid");
                comments.add(rule.getExplanation());
            }
        }
    }

    private void log(final TechnicalLogSeverity severity, final String message) {
        if (logger.isLoggable(ContractValidator.class, severity)) {
            logger.log(ContractValidator.class, severity, message);
        }
    }

    public List<String> getComments() {
        return comments;
    }

}
