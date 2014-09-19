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

    private ContractTypeValidator typeValidator;
    private TechnicalLoggerService logger;

    public ContractStructureValidator(ContractTypeValidator typeValidator, TechnicalLoggerService loggerService) {
        this.typeValidator = typeValidator;
        this.logger = loggerService;
    }

    public void validate(SContractDefinition contract, Map<String, Object> inputs) throws ContractViolationException {
        List<String> messages = new ArrayList<String>();
        messages.addAll(recursive(contract.getSimpleInputs(), contract.getComplexInputs(), inputs));
        if (!messages.isEmpty()) {
            throw new ContractViolationException("Error while validating expected inputs", messages);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> recursive(List<SSimpleInputDefinition> simpleInputs, List<SComplexInputDefinition> complexInputs, Map<String, Object> inputs) {

        logInputsWhichAreNotInContract(DEBUG, simpleInputs, inputs);
        
        List<String> message = new ArrayList<String>();
        for (SInputDefinition def : union(simpleInputs, complexInputs)) {
            try {
                validateInput(def, inputs);
                if (def instanceof SComplexInputDefinition) {
                    SComplexInputDefinition complex = (SComplexInputDefinition) def;
                    message.addAll(recursive(complex.getSimpleInputDefinitions(), complex.getComplexInputDefinitions(), (Map<String, Object>) inputs.get(def.getName())));
                }
            } catch (InputValidationException e) {
                message.add(e.getMessage());
            }
        }
        return message;
    }

    private List<SInputDefinition> union(List<SSimpleInputDefinition> simpleInputs, List<SComplexInputDefinition> complexInputs) {
        List<SInputDefinition> all = new ArrayList<SInputDefinition>();
        all.addAll(simpleInputs);
        all.addAll(complexInputs);
        return all;
    }

    private void validateInput(SInputDefinition definition, Map<String, Object> inputs) throws InputValidationException {
        String inputName = definition.getName();
        if (!inputs.containsKey(inputName)) {
            throw new InputValidationException("Expected input [" + inputName + "] is missing");
        } else {
            typeValidator.validate(definition, inputs.get(inputName));
        }
    }

    private void logInputsWhichAreNotInContract(TechnicalLogSeverity severity, List<SSimpleInputDefinition> simpleInputs, Map<String, Object> inputs) {
        if (logger.isLoggable(ContractStructureValidator.class, severity)) {
            for (String input : getInputsWhichAreNotInContract(simpleInputs, inputs)) {
                logger.log(ContractStructureValidator.class, severity, "Unexpected input [" + input + "] provided");
            }
        }
    }

    private List<String> getInputsWhichAreNotInContract(List<SSimpleInputDefinition> simpleInputs, Map<String, Object> inputs) {
        List<String> keySet = new ArrayList<String>(inputs.keySet());
        for (SInputDefinition def : simpleInputs) {
            keySet.remove(def.getName());
        }
        return keySet;
    }
}
