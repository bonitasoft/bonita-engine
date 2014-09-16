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
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * Validate that task inputs fit with contract structure and types
 * 
 * @author Colin Puy
 *
 */
public class ContractStructureValidator {

    private TechnicalLoggerService logger;
    private ContractTypeValidator typeValidator;

    public ContractStructureValidator(ContractTypeValidator typeValidator, TechnicalLoggerService loggerService) {
        this.typeValidator = typeValidator;
        this.logger = loggerService;
    }

    public void validate(SContractDefinition contract, Map<String, Object> inputs) throws ContractViolationException {
        logInputsWhichAreNotInContract(DEBUG, contract, inputs);
        List<String> problems = findEventualProblems(contract, inputs);
        if (!problems.isEmpty()) {
            throw new ContractViolationException("Error in task inputs structure", problems);
        }
    }

    private List<String> findEventualProblems(SContractDefinition contract, Map<String, Object> inputs) {
        List<String> problems = new ArrayList<String>();
        for (SInputDefinition definition : contract.getInputs()) {
            String inputName = definition.getName();
            if (!inputs.containsKey(inputName)) {
                problems.add("Contract need field [" + inputName + "] but it has not been provided");
            } else {
                Object value = inputs.get(inputName);
                if (!typeValidator.isValid(definition.getType(), value)) {
                    problems.add(value + " cannot be assigned to " + definition.getType());
                }
            }
        }
        return problems;
    }

    private void logInputsWhichAreNotInContract(TechnicalLogSeverity severity, SContractDefinition contract, Map<String, Object> inputs) {
        if (logger.isLoggable(ContractStructureValidator.class, severity)) {
            for (String input : getInputsWhichAreNotInContract(contract, inputs)) {
                logger.log(ContractStructureValidator.class, severity, "Field [" + input + "] has been provided but is not expected in task contract");
            }
        }
    }

    private List<String> getInputsWhichAreNotInContract(SContractDefinition contract, Map<String, Object> inputs) {
        List<String> keySet = new ArrayList<String>(inputs.keySet());
        for (SInputDefinition def : contract.getInputs()) {
            keySet.remove(def.getName());
        }
        return keySet;
    }
}
