package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;


public class SContractDefinitionBuilder {

    private List<SSimpleInputDefinition> inputs = new ArrayList<SSimpleInputDefinition>();
    private List<SRuleDefinition> rules = new ArrayList<SRuleDefinition>();

    public static SContractDefinitionBuilder aContract() {
        return new SContractDefinitionBuilder();
    }
    
    public SContractDefinitionBuilder withInput(SSimpleInputDefinition input) {
        inputs.add(input);
        return this;
    }
    
    public SContractDefinitionBuilder withRule(SRuleDefinition rule) {
        rules.add(rule);
        return this;
    }
    
    public SContractDefinition build() {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        for (SSimpleInputDefinition input : inputs) {
            contract.addSimpleInput(input);
        }
        for (SRuleDefinition rule : rules) {
            contract.addRule(rule);
        }
        return contract;
    }
}
