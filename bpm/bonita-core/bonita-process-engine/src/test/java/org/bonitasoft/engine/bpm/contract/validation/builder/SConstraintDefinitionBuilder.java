package org.bonitasoft.engine.bpm.contract.validation.builder;

import static java.util.Arrays.asList;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;

public class SConstraintDefinitionBuilder {

    private List<String> inputNames;
    private String expression;
    private String explanation;
    private String name;

    public SConstraintDefinitionBuilder(String... inputNames) {
        this.inputNames = asList(inputNames);
    }

    public static SConstraintDefinitionBuilder aRuleFor(String... inputName) {
        return new SConstraintDefinitionBuilder(inputName);
    }
    
    public SConstraintDefinitionBuilder expression(String expression) {
        this.expression = expression;
        return this;
    }
    
    public SConstraintDefinitionBuilder explanation(String explanation) {
        this.explanation = explanation;
        return this;
    }
    
    public SConstraintDefinitionBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public SConstraintDefinition build() {
        SConstraintDefinitionImpl rule = new SConstraintDefinitionImpl(name, expression, explanation);
        for (String inputName : inputNames) {
            rule.addInputName(inputName);
        }
        return rule;
    }
}
