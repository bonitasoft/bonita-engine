package org.bonitasoft.engine.bpm.contract.validation.builder;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;


public class SContractDefinitionBuilder {

    private List<SSimpleInputDefinition> simpleInputs = new ArrayList<SSimpleInputDefinition>();
    private List<SComplexInputDefinition> complexInputs = new ArrayList<SComplexInputDefinition>();
    private List<SRuleDefinition> rules = new ArrayList<SRuleDefinition>();

    public static SContractDefinitionBuilder aContract() {
        return new SContractDefinitionBuilder();
    }
    
    public SContractDefinitionBuilder withInput(SSimpleInputDefinitionBuilder builder) {
        return withInput(builder.build());
    }
    
    public SContractDefinitionBuilder withInput(SComplexInputDefinitionBuilder builder) {
        return withInput(builder.build());
    }
    
    public SContractDefinitionBuilder withInput(SInputDefinition input) {
        if (input instanceof SSimpleInputDefinition) {
            simpleInputs.add((SSimpleInputDefinition) input);
        }
        if (input instanceof SComplexInputDefinition) {
            complexInputs.add((SComplexInputDefinition) input);
        }
        return this;
    }
    
    public SContractDefinitionBuilder withRule(SRuleDefinition rule) {
        rules.add(rule);
        return this;
    }
    
    public SContractDefinition build() {
        SContractDefinitionImpl contract = new SContractDefinitionImpl();
        for (SSimpleInputDefinition input : simpleInputs) {
            contract.addSimpleInput(input);
        }
        for (SComplexInputDefinition input : complexInputs) {
            contract.addComplexInput(input);
        }
        for (SRuleDefinition rule : rules) {
            contract.addRule(rule);
        }
        return contract;
    }
}
