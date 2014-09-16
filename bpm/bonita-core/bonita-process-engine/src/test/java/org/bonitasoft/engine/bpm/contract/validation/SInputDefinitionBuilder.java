package org.bonitasoft.engine.bpm.contract.validation;

import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SSimpleInputDefinitionImpl;


public class SInputDefinitionBuilder {
    
    private String name = "a name";
    private String description = "a description";
    private SType type;
    
    
    public SInputDefinitionBuilder(SType type) {
        this.type = type;
    }
    
    public static SInputDefinitionBuilder anInput(SType type) {
        return new SInputDefinitionBuilder(type);
    }
    
    public static SInputDefinitionBuilder anInput() {
        return new SInputDefinitionBuilder(SType.TEXT);
    }
    
    public SSimpleInputDefinition build() {
        return new SSimpleInputDefinitionImpl(name, type, description);
    }
    
    public SInputDefinitionBuilder withName(String name) {
        this.name = name;
        return this;
    }
}
