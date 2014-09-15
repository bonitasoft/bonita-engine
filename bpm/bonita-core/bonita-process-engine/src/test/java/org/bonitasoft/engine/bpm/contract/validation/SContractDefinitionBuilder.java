package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;


public class SContractDefinitionBuilder {

    private List<SInputDefinition> inputs = new ArrayList<SInputDefinition>();

    public static SContractDefinitionBuilder aContract() {
        return new SContractDefinitionBuilder();
    }
    
    public SContractDefinitionBuilder withInput(SInputDefinition input) {
        inputs.add(input);
        return this;
    }
    
    public SContractDefinition build() {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        for (SInputDefinition input : inputs) {
            contract.addInput(input);
        }
        return contract;
    }
}
