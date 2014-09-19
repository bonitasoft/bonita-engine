package org.bonitasoft.engine.bpm.contract.validation;

import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SSimpleInputDefinitionImpl;


public class SSimpleInputDefinitionBuilder {
    
    private String name = "aName";
    private String description = "a description";
    private SType type;
    
    
    public SSimpleInputDefinitionBuilder(SType type) {
        this.type = type;
    }
    
    public static SSimpleInputDefinitionBuilder anInput(SType type) {
        return new SSimpleInputDefinitionBuilder(type);
    }
    
    public static SSimpleInputDefinitionBuilder anInput() {
        return new SSimpleInputDefinitionBuilder(SType.TEXT);
    }
    
    public SSimpleInputDefinition build() {
        return new SSimpleInputDefinitionImpl(name, type, description);
    }
    
    public SSimpleInputDefinitionBuilder withName(String name) {
        this.name = name;
        return this;
    }
}
