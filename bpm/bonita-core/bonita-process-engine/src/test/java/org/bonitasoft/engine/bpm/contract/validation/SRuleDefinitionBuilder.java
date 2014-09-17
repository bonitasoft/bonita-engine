package org.bonitasoft.engine.bpm.contract.validation;

import static java.util.Arrays.asList;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SRuleDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SRuleDefinitionImpl;

public class SRuleDefinitionBuilder {

    private List<String> inputNames;
    private String expression;
    private String explanation;
    private String name;

    public SRuleDefinitionBuilder(String... inputNames) {
        this.inputNames = asList(inputNames);
    }

    public static SRuleDefinitionBuilder aRuleFor(String... inputName) {
        return new SRuleDefinitionBuilder(inputName);
    }
    
    public SRuleDefinitionBuilder expression(String expression) {
        this.expression = expression;
        return this;
    }
    
    public SRuleDefinitionBuilder explanation(String explanation) {
        this.explanation = explanation;
        return this;
    }
    
    public SRuleDefinitionBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public SRuleDefinition build() {
        SRuleDefinitionImpl rule = new SRuleDefinitionImpl(name, expression, explanation);
        for (String inputName : inputNames) {
            rule.addInputName(inputName);
        }
        return rule;
    }
}
