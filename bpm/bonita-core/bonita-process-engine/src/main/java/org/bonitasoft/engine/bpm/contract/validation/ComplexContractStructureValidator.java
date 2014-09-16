package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;


public class ComplexContractStructureValidator {

    
    private ContractStructureValidator validator;

    public ComplexContractStructureValidator(ContractStructureValidator validator) {
        this.validator = validator;
    }
    
    public void validate(SContractDefinition contract, Map<String, Object> inputs) {
//        List<String> message = new ArrayList<String>();
//        message.addAll(validateSimpleFields(contract, inputs));
//        
//        List<SComplexInputDefinition> complexInputs = contract.getComplexInputs();
//        for (SComplexInputDefinition sComplexInputDefinition : complexInputs) {
//           
//            inputs.get
//            
//            validate(contract, sComplexInputDefinition);
//        }
    }

    private List<String> validateSimpleFields(SContractDefinition contract, Map<String, Object> inputs) {
        List<SSimpleInputDefinition> inputs2 = contract.getSimpleInputs();
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (SInputDefinition sInputDefinition : inputs2) {
            map.put(sInputDefinition.getName(), inputs.get(sInputDefinition.getName()));
        }
        
        
        try {
            validator.validate(contract, map);
        } catch (ContractViolationException e) {
           return e.getExplanations();
        }
        
        return new ArrayList<String>();
    }
}
