package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoDefinitionConverter {

    public CustomUserInfoDefinitionImpl convert(SCustomUserInfoDefinition sDefinition) {
        CustomUserInfoDefinitionImpl definition = new CustomUserInfoDefinitionImpl();
        definition.setId(sDefinition.getId());
        definition.setName(sDefinition.getName());
        definition.setDisplayName(sDefinition.getDisplayName());
        definition.setDescription(sDefinition.getDescription());
        return definition;
    }
}
