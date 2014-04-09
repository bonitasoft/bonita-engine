package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.impl.CustomUserInfoValueImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoConverter {

    public CustomUserInfoDefinitionImpl convert(SCustomUserInfoDefinition sDefinition) {
        CustomUserInfoDefinitionImpl definition = new CustomUserInfoDefinitionImpl();
        definition.setId(sDefinition.getId());
        definition.setName(sDefinition.getName());
        definition.setDescription(sDefinition.getDescription());
        return definition;
    }

    public CustomUserInfoValueImpl convert(SCustomUserInfoValue sValue) {
        if(sValue == null) {
            return null;
        }
        CustomUserInfoValueImpl value = new CustomUserInfoValueImpl();
        value.setDefinitionId(sValue.getDefinitionId());
        value.setUserId(sValue.getUserId());
        value.setValue(sValue.getValue());
        return value;
    }
}
