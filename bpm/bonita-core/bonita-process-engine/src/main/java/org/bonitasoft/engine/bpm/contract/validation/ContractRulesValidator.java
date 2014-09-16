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
import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.mvel2.MVEL;

public class ContractRulesValidator {

    private TechnicalLoggerService logger;

    public ContractRulesValidator(TechnicalLoggerService logger) {
        this.logger = logger;
    }

    public void validate(final List<SRuleDefinition> rules, final Map<String, Object> variables) throws ContractViolationException {
        List<String> comments = new ArrayList<String>();
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
        
        if (!comments.isEmpty()) {
            throw new ContractViolationException("Error while validating rules", comments);
        }
    }

    private void log(final TechnicalLogSeverity severity, final String message) {
        if (logger.isLoggable(ContractRulesValidator.class, severity)) {
            logger.log(ContractRulesValidator.class, severity, message);
        }
    }
}
