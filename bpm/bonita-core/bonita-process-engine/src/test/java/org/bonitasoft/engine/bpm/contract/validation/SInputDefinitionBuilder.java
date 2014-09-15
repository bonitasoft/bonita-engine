package org.bonitasoft.engine.bpm.contract.validation;

import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.impl.SInputDefinitionImpl;


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
    
    public SInputDefinition build() {
        return new SInputDefinitionImpl(name, type, description);
    }
    
    public SInputDefinitionBuilder withName(String name) {
        this.name = name;
        return this;
    }
}
