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

import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.DEBUG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class ContractStructureValidator {

    private final ContractTypeValidator typeValidator;
    private final TechnicalLoggerService logger;

    public ContractStructureValidator(final ContractTypeValidator typeValidator, final TechnicalLoggerService loggerService) {
        this.typeValidator = typeValidator;
        logger = loggerService;
    }

    public void validate(final SContractDefinition contract, final Map<String, Object> inputs) throws ContractViolationException {
        final List<String> messages = new ArrayList<String>();
        messages.addAll(recursive(contract.getSimpleInputs(), contract.getComplexInputs(), inputs));
        if (!messages.isEmpty()) {
            throw new ContractViolationException("Error while validating expected inputs", messages);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> recursive(final List<SSimpleInputDefinition> simpleInputs, final List<SComplexInputDefinition> complexInputs,
            final Map<String, Object> inputs) {

        logInputsWhichAreNotInContract(DEBUG, simpleInputs, inputs);

        final List<String> message = new ArrayList<String>();
        for (final SInputDefinition def : union(simpleInputs, complexInputs)) {
            try {
                validateInput(def, inputs);
                if (def instanceof SComplexInputDefinition) {
                    final SComplexInputDefinition complex = (SComplexInputDefinition) def;
                    if (def.isMultiple()) {
                        for (final Map<String, Object> complexItem : (List<Map<String, Object>>) inputs.get(def.getName())) {
                            validateComplexItem(complexItem, message, def, complex);
                        }
                    }
                    else {
                        validateComplexItem((Map<String, Object>) inputs.get(def.getName()), message, def, complex);
                    }
                }
            } catch (final InputValidationException e) {
                message.add(e.getMessage());
            }
        }
        return message;
    }

    private void validateComplexItem(final Map<String, Object> complexItem, final List<String> message, final SInputDefinition def,
            final SComplexInputDefinition complex) {
        message.addAll(recursive(complex.getSimpleInputDefinitions(), complex.getComplexInputDefinitions(), complexItem));
    }

    private List<SInputDefinition> union(final List<SSimpleInputDefinition> simpleInputs, final List<SComplexInputDefinition> complexInputs) {
        final List<SInputDefinition> all = new ArrayList<SInputDefinition>();
        all.addAll(simpleInputs);
        all.addAll(complexInputs);
        return all;
    }

    private void validateInput(final SInputDefinition definition, final Map<String, Object> inputs) throws InputValidationException {
        final String inputName = definition.getName();
        if (!inputs.containsKey(inputName)) {
            throw new InputValidationException("Expected input [" + inputName + "] is missing");
        } else {
            typeValidator.validate(definition, inputs.get(inputName));
        }
    }

    private void logInputsWhichAreNotInContract(final TechnicalLogSeverity severity, final List<SSimpleInputDefinition> simpleInputs,
            final Map<String, Object> inputs) {
        if (logger.isLoggable(ContractStructureValidator.class, severity)) {
            for (final String input : getInputsWhichAreNotInContract(simpleInputs, inputs)) {
                logger.log(ContractStructureValidator.class, severity, "Unexpected input [" + input + "] provided");
            }
        }
    }

    private List<String> getInputsWhichAreNotInContract(final List<SSimpleInputDefinition> simpleInputs, final Map<String, Object> inputs) {
        final List<String> keySet = new ArrayList<String>(inputs.keySet());
        for (final SInputDefinition def : simpleInputs) {
            keySet.remove(def.getName());
        }
        return keySet;
    }
}
