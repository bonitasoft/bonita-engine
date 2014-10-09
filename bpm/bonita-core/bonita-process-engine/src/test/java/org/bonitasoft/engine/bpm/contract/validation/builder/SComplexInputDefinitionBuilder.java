package org.bonitasoft.engine.bpm.contract.validation.builder;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SComplexInputDefinitionImpl;

public class SComplexInputDefinitionBuilder {

    private String name = "aName";
    private String description = "a description";
    private boolean multiple = false;
    private final List<SSimpleInputDefinition> inputDefinitions = new ArrayList<SSimpleInputDefinition>();
    private final List<SComplexInputDefinition> complexDefinitions = new ArrayList<SComplexInputDefinition>();

    public static SComplexInputDefinitionBuilder aComplexInput() {
        return new SComplexInputDefinitionBuilder();
    }

    public SComplexInputDefinition build() {
        return new SComplexInputDefinitionImpl(name, description, multiple, inputDefinitions, complexDefinitions);
    }

    public SComplexInputDefinitionBuilder withInput(final SSimpleInputDefinitionBuilder... definitions) {
        for (final SSimpleInputDefinitionBuilder definition : definitions) {
            inputDefinitions.add(definition.build());
        }
        return this;
    }

    public SComplexInputDefinitionBuilder withInput(final SInputDefinition... definitions) {
        for (final SInputDefinition definition : definitions) {
            if (definition instanceof SSimpleInputDefinition) {
                inputDefinitions.add((SSimpleInputDefinition) definition);
            } else if (definition instanceof SComplexInputDefinition) {
                complexDefinitions.add((SComplexInputDefinition) definition);
            }
        }
        return this;
    }

    public SComplexInputDefinitionBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public SComplexInputDefinitionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public SComplexInputDefinitionBuilder withMultiple(final boolean multiple) {
        this.multiple = multiple;
        return this;
    }
}
