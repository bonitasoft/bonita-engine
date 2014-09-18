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
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;

/**
 * Validate that task inputs fit with contract structure and types
 * 
 * @author Colin Puy
 */
public class ContractStructureValidator {

    private ContractTypeValidator typeValidator;

    public ContractStructureValidator(ContractTypeValidator typeValidator) {
        this.typeValidator = typeValidator;
    }

    public void validate(List<SSimpleInputDefinition> simpleInputs, Map<String, Object> inputs) throws ContractViolationException {
        List<String> problems = findEventualProblems(simpleInputs, inputs);
        if (!problems.isEmpty()) {
            throw new ContractViolationException("Error in task inputs structure", problems);
        }
    }

    protected List<String> findEventualProblems(List<SSimpleInputDefinition> simpleInputs, Map<String, Object> inputs) {
        List<String> problems = new ArrayList<String>();
        for (SSimpleInputDefinition definition : simpleInputs) {
            try {
                validateInput(definition, inputs);
            } catch (InputValidationException e) {
                problems.add(e.getMessage());
            }
        }
        return problems;
    }

    protected void validateInput(SInputDefinition definition, Map<String, Object> inputs) throws InputValidationException {
        String inputName = definition.getName();
        if (!inputs.containsKey(inputName)) {
            throw new InputValidationException("Contract need field [" + inputName + "] but it has not been provided");
        } else {
            typeValidator.validate(definition, inputs.get(inputName));
        }
    }
}
