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
import org.bonitasoft.engine.core.process.definition.model.SInputContainerDefinition;
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
        final ErrorReporter errorReporter = new ErrorReporter();
        validateInputContainer(contract, inputs != null ? inputs : Collections.<String, Serializable>emptyMap(), errorReporter);
        if (errorReporter.hasError()) {
            throw new ContractViolationException("Error while validating expected inputs", errorReporter.getErrors());
        }
    }

    void validateInputContainer(SInputContainerDefinition inputContainer, Map<String, Serializable> inputs, ErrorReporter errorReporter) {
        logInputsWhichAreNotInContract(DEBUG, inputContainer.getInputDefinitions(), inputs);
        for (final SInputDefinition inputDefinition : inputContainer.getInputDefinitions()) {
            validateInput(inputs, errorReporter, inputDefinition);
        }
    }

    private void validateInput(Map<String, Serializable> inputs, ErrorReporter errorReporter, SInputDefinition inputDefinition) {
        final String inputName = inputDefinition.getName();
        if (!checkExists(inputs, errorReporter, inputName)) return;
        if (!checkNotNull(inputs, errorReporter, inputName)) return;
        if (!typeValidator.validate(inputDefinition, inputs.get(inputName), errorReporter)) return;
        validateChildren(inputs, errorReporter, inputDefinition);
    }

    private void validateChildren(Map<String, Serializable> inputs, ErrorReporter errorReporter, SInputDefinition inputDefinition) {
        if (inputDefinition.hasChildren() && inputDefinition.getType() == null) {
            if (inputDefinition.isMultiple()) {
                for (final Map<String, Serializable> complexItem : (List<Map<String, Serializable>>) inputs.get(inputDefinition.getName())) {
                    validateInputContainer(inputDefinition, complexItem, errorReporter);
                }
            } else {
                validateInputContainer(inputDefinition, (Map<String, Serializable>) inputs.get(inputDefinition.getName()), errorReporter);
            }
        }
    }

    private boolean checkNotNull(Map<String, Serializable> inputs, ErrorReporter errorReporter, String inputName) {
        if (inputs.get(inputName) == null) {
            errorReporter.addError("Input [" + inputName + "] has a null value.");
            return false;
        }
        return true;
    }

    private boolean checkExists(Map<String, Serializable> inputs, ErrorReporter errorReporter, String inputName) {
        if (!inputs.containsKey(inputName)) {
            errorReporter.addError("Expected input [" + inputName + "] is missing");
            return false;
        }
        return true;
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
        if (inputs == null || inputs.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> keySet = new ArrayList<>(inputs.keySet());
        for (final SInputDefinition def : simpleInputs) {
            keySet.remove(def.getName());
        }
        return keySet;
    }
}
