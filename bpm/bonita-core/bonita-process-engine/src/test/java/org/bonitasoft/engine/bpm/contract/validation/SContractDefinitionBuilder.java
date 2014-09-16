package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;


public class SContractDefinitionBuilder {

    private List<SSimpleInputDefinition> inputs = new ArrayList<SSimpleInputDefinition>();

    public static SContractDefinitionBuilder aContract() {
        return new SContractDefinitionBuilder();
    }
    
    public SContractDefinitionBuilder withInput(SSimpleInputDefinition input) {
        inputs.add(input);
        return this;
    }
    
    public SContractDefinition build() {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        for (SSimpleInputDefinition input : inputs) {
            contract.addSimpleInput(input);
        }
        return contract;
    }
}
