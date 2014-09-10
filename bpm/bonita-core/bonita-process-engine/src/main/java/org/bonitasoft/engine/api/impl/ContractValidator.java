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
package org.bonitasoft.engine.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.STypeConverter;
import org.bonitasoft.engine.core.process.definition.model.impl.SRuleDefinitionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.mvel2.MVEL;

/**
 * @author Matthieu Chaffotte
 */
public class ContractValidator {

    private final TechnicalLoggerService loggerService;

    private final List<String> comments;

    public ContractValidator(final TechnicalLoggerService loggerService) {
        comments = new ArrayList<String>();
        this.loggerService = loggerService;
    }

    public boolean isValid(final SContractDefinition contract, final Map<String, Object> variables) {
        comments.clear();

        final List<SRuleDefinition> rules = contract.getRules();
        if (rules.isEmpty()) {
            validateContract(contract, variables);
        }
        //input are mandatory prior to type checking
        if (comments.isEmpty()) {
            final List<SRuleDefinition> checkInputTypeRules = new ArrayList<SRuleDefinition>();
            for (final SInputDefinition sInputDefinition : contract.getInputs()) {
                if (variables.containsKey(sInputDefinition.getName()) && variables.get(sInputDefinition.getName()) != null) {
                    checkInputTypeRules.add(createInputValidateTypeRule(sInputDefinition));
                }
            }
            validateRules(variables, checkInputTypeRules);
        }
        //type checking is mandatory prior to contract rules
        if (comments.isEmpty()) {
            validateRules(variables, rules);
        }
        return comments.isEmpty();
    }

    private SRuleDefinition createInputValidateTypeRule(final SInputDefinition sInputDefinition) {
        final String realType = getRealType(sInputDefinition);

        final StringBuilder ruleExpressionBuilder = new StringBuilder();
        ruleExpressionBuilder.append(sInputDefinition.getName());
        ruleExpressionBuilder.append(" instanceof ");
        ruleExpressionBuilder.append(realType);

        final StringBuilder ruleNameBuilder = new StringBuilder();
        ruleNameBuilder.append("validate type for input ");
        ruleNameBuilder.append(sInputDefinition.getName());

        final StringBuilder ruleExplanationBuilder = new StringBuilder();
        ruleExplanationBuilder.append("unable to bind input ");
        ruleExplanationBuilder.append(sInputDefinition.getName());
        ruleExplanationBuilder.append(" to type ");
        ruleExplanationBuilder.append(realType);

        final SRuleDefinitionImpl rule = new SRuleDefinitionImpl(ruleNameBuilder.toString(), ruleExpressionBuilder.toString(),
                ruleExplanationBuilder.toString());
        rule.addInputName(sInputDefinition.getName());
        return rule;
    }

    private String getRealType(SInputDefinition sInputDefinition) {
        STypeConverter sTypeConverter;
        SType type=sInputDefinition.getType();
        try {
            sTypeConverter = type.getClass().getField(type.name()).getAnnotation(STypeConverter.class);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("unable to get type for  input " + sInputDefinition.getName(), e);
        }
        return sTypeConverter.implementationClass().getName();
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

    private void validateContract(final SContractDefinition contract, final Map<String, Object> variables) {
        final Map<String, Object> copy = new HashMap<String, Object>();
        for (final String key : variables.keySet()) {
            copy.put(key, "");
        }
        final Set<String> keys = copy.keySet();
        for (final SInputDefinition input : contract.getInputs()) {
            if (!variables.containsKey(input.getName())) {
                comments.add(input.getName() + " is not defined");
            } else {
                keys.remove(input.getName());
            }
        }
        for (final String key : keys) {
            comments.add("variable " + key + " is not expected");

        }
    }

    private void log(final TechnicalLogSeverity severity, final String message) {
        if (loggerService.isLoggable(ContractValidator.class, severity)) {
            loggerService.log(ContractValidator.class, severity, message);
        }
    }

    public List<String> getComments() {
        return comments;
    }

}
