package org.bonitasoft.engine.bpm.contract.validation;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SComplexInputDefinitionImpl;

public class SComplexInputDefinitionBuilder {

    private String name = "aName";
    private String description = "a description";
    private List<SSimpleInputDefinition> inputDefinitions = new ArrayList<SSimpleInputDefinition>();
    private List<SComplexInputDefinition> complexDefinitions = new ArrayList<SComplexInputDefinition>();

    public static SComplexInputDefinitionBuilder aComplexInput() {
        return new SComplexInputDefinitionBuilder();
    }

    public SComplexInputDefinition build() {
        return new SComplexInputDefinitionImpl(name, description, inputDefinitions, complexDefinitions);
    }

    public SComplexInputDefinitionBuilder withName(String name) {
        this.name = name;
        return this;
    }
}
