package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;

public class ComplexContractStructureValidator {

    private ContractStructureValidator validator;

    public ComplexContractStructureValidator(ContractStructureValidator validator) {
        this.validator = validator;
    }

    public void validate(SContractDefinition contract, Map<String, Object> inputs) throws ContractViolationException {
        List<String> message = new ArrayList<String>();
        message.addAll(recursive(contract, inputs));
       if (!message.isEmpty()) {
           throw new ContractViolationException("Ca marche pas", message);
       }
    }
    
    private List<String> recursive(SContractDefinition contract, Map<String, Object> inputs) {
        List<String> message = new ArrayList<String>();
        
        try {
            validator.validate(contract, inputs);
        } catch (ContractViolationException e) {
            message.addAll(e.getExplanations());
        }

        List<SComplexInputDefinition> complexInputs = contract.getComplexInputs();
        for (SComplexInputDefinition def : complexInputs) {
            Map<String, Object> map = (Map<String, Object>) inputs.get(def.getName());
            // TODO ClassCastException
            List<SSimpleInputDefinition> simpleInputDefinitions = def.getSimpleInputDefinitions();
            message.addAll(validateSimpleFields(simpleInputDefinitions, map));

            SContractDefinitionImpl sContractDefinitionImpl = new SContractDefinitionImpl();
            for (SComplexInputDefinition sComplexInputDefinition : def.getComplexInputDefinitions()) {
                sContractDefinitionImpl.addComplexInput(sComplexInputDefinition);
            }
            message.addAll(recursive(sContractDefinitionImpl, map));
        }
        return message;
    }

    private List<String> validateSimpleFields(List<SSimpleInputDefinition> simpleInputs, Map<String, Object> inputs) {

        SContractDefinitionImpl sContractDefinitionImpl = new SContractDefinitionImpl();
        for (SSimpleInputDefinition sSimpleInputDefinition : simpleInputs) {
            sContractDefinitionImpl.addSimpleInput(sSimpleInputDefinition);
        }
        try {
            validator.validate(sContractDefinitionImpl, inputs);
        } catch (ContractViolationException e) {
            return e.getExplanations();
        }
        return new ArrayList<String>();
    }
}
