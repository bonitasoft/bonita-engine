package org.bonitasoft.engine.bpm.contract.validation.builder;

import static java.util.Arrays.asList;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintType;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;

public class SConstraintDefinitionBuilder {

    private final List<String> inputNames;
    private String expression;
    private String explanation;
    private String name;
    private SConstraintType sConstraintType;

    public SConstraintDefinitionBuilder(final String... inputNames) {
        this.inputNames = asList(inputNames);
    }

    public static SConstraintDefinitionBuilder aRuleFor(final String... inputName) {
        return new SConstraintDefinitionBuilder(inputName);
    }

    public SConstraintDefinitionBuilder expression(final String expression) {
        this.expression = expression;
        return this;
    }

    public SConstraintDefinitionBuilder explanation(final String explanation) {
        this.explanation = explanation;
        return this;
    }

    public SConstraintDefinitionBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public SConstraintDefinitionBuilder constraintType(final SConstraintType sConstraintType) {
        this.sConstraintType = sConstraintType;
        return this;
    }

    public SConstraintDefinition build() {
        final SConstraintDefinitionImpl rule = new SConstraintDefinitionImpl(name, expression, explanation, sConstraintType);
        for (final String inputName : inputNames) {
            rule.addInputName(inputName);
        }
        return rule;
    }
}
