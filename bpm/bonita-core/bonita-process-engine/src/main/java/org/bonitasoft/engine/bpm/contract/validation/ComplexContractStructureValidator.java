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
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;

public class ComplexContractStructureValidator {

    private ContractStructureValidator validator;
    private ContractTypeValidator typeValidator;

    public ComplexContractStructureValidator(ContractStructureValidator validator, ContractTypeValidator typeValidator) {
        this.validator = validator;
        this.typeValidator = typeValidator;
    }

    public void validate(SContractDefinition contract, Map<String, Object> inputs) throws ContractViolationException {
        List<String> message = new ArrayList<String>();
        message.addAll(recursive(contract.getSimpleInputs(), contract.getComplexInputs(), inputs));
        if (!message.isEmpty()) {
            throw new ContractViolationException("Ca marche pas", message);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> recursive(List<SSimpleInputDefinition> simpleInputs, List<SComplexInputDefinition> complexInputs, Map<String, Object> inputs) {
        List<String> message = new ArrayList<String>();

        if (!simpleInputs.isEmpty()) {
            try {
                validator.validate(simpleInputs, inputs);
            } catch (ContractViolationException e) {
                message.addAll(e.getExplanations());
            }
        }

        for (SComplexInputDefinition def : complexInputs) {
            if (!inputs.containsKey(def.getName())) {
                message.add("Contract need field [" + def.getName() + "] but it has not been provided");
            } else {
                
                Object value = inputs.get(def.getName());
                if (!typeValidator.isValid(def, value)) {
                    message.add(value + " cannot be assigned to COMPLEX type");
                } else {
                    message.addAll(recursive(def.getSimpleInputDefinitions(), def.getComplexInputDefinitions(), (Map<String, Object>) value));
                }
            }
        }
        return message;
    }
}
