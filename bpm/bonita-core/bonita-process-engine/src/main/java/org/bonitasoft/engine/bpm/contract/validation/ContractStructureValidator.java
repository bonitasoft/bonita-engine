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
 */
package org.bonitasoft.engine.bpm.contract.validation;

import static org.bonitasoft.engine.log.technical.TechnicalLogSeverity.DEBUG;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

public class ContractStructureValidator {

    private final ContractTypeValidator typeValidator;
    private final TechnicalLoggerService logger;

    public ContractStructureValidator(final ContractTypeValidator typeValidator, final TechnicalLoggerService loggerService) {
        this.typeValidator = typeValidator;
        logger = loggerService;
    }

    public void validate(final SContractDefinition contract, final Map<String, Serializable> inputs) throws ContractViolationException {
        final List<String> messages = new ArrayList<>();
        final Map<String, Serializable> inputsToValidate = inputs != null ? inputs : Collections.<String, Serializable> emptyMap();
        messages.addAll(recursive(contract.getInputs(), inputsToValidate));
        if (!messages.isEmpty()) {
            throw new ContractViolationException("Error while validating expected inputs", messages);
        }
    }

    private List<String> recursive(final List<SInputDefinition> inputDefinitions,
            final Map<String, Serializable> inputs) {

        logInputsWhichAreNotInContract(DEBUG, inputDefinitions, inputs);

        final List<String> message = new ArrayList<>();
        for (final SInputDefinition def : inputDefinitions) {
            try {
                validateInput(def, inputs);
                if (!def.hasChildren()) {
                    continue;
                }
                if (def.isMultiple()) {
                    for (final Map<String, Serializable> complexItem : (List<Map<String, Serializable>>) inputs.get(def.getName())) {
                        validateComplexItem(complexItem, message, def, def);
                    }
                } else {
                    validateComplexItem((Map<String, Serializable>) inputs.get(def.getName()), message, def, def);
                }
            } catch (final InputValidationException e) {
                message.add(e.getMessage());
            }
        }
        return message;
    }

    private void validateComplexItem(final Map<String, Serializable> complexItem, final List<String> message, final SInputDefinition def,
            final SInputDefinition complex) {
        message.addAll(recursive(complex.getInputDefinitions(), complexItem));
    }

    private void validateInput(final SInputDefinition definition, final Map<String, Serializable> inputs) throws InputValidationException {
        final String inputName = definition.getName();
        if (!inputs.containsKey(inputName)) {
            throw new InputValidationException("Expected input [" + inputName + "] is missing");
        }
        if (inputs.get(inputName) == null) {
            throw new InputValidationException("Input [" + inputName + "] has a null value.");
        }
        typeValidator.validate(definition, inputs.get(inputName));
    }

    private void logInputsWhichAreNotInContract(final TechnicalLogSeverity severity, final List<SInputDefinition> simpleInputs,
            final Map<String, Serializable> inputs) {
        if (logger.isLoggable(ContractStructureValidator.class, severity)) {
            for (final String input : getInputsWhichAreNotInContract(simpleInputs, inputs)) {
                logger.log(ContractStructureValidator.class, severity, "Unexpected input [" + input + "] provided");
            }
        }
    }

    private List<String> getInputsWhichAreNotInContract(final List<SInputDefinition> simpleInputs, final Map<String, Serializable> inputs) {
        final List<String> keySet = new ArrayList<>(inputs.keySet());
        for (final SInputDefinition def : simpleInputs) {
            keySet.remove(def.getName());
        }
        return keySet;
    }
}
