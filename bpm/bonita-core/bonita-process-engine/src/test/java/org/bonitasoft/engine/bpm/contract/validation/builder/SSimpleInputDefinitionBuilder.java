package org.bonitasoft.engine.bpm.contract.validation.builder;

import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SSimpleInputDefinitionImpl;

public class SSimpleInputDefinitionBuilder {

    private String name = "aName";
    private String description = "a description";
    private boolean multiple = false;
    private SType type;

    public SSimpleInputDefinitionBuilder(final SType type) {
        this.type = type;
    }

    public static SSimpleInputDefinitionBuilder aSimpleInput(final SType type) {
        return new SSimpleInputDefinitionBuilder(type);
    }

    public static SSimpleInputDefinitionBuilder aSimpleInput() {
        return new SSimpleInputDefinitionBuilder(SType.TEXT);
    }

    public SSimpleInputDefinition build() {
        return new SSimpleInputDefinitionImpl(name, type, description, multiple);
    }

    public SSimpleInputDefinitionBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public SSimpleInputDefinitionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public SSimpleInputDefinitionBuilder withMultiple(final boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public SSimpleInputDefinitionBuilder withType(final SType type) {
        this.type = type;
        return this;
    }
}
