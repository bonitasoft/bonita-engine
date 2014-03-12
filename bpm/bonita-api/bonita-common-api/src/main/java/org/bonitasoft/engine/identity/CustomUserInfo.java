package org.bonitasoft.engine.identity;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfo {

    private final CustomUserInfoDefinition definition;

    public CustomUserInfo(CustomUserInfoDefinition definition) {
        this.definition = definition;
    }

    public CustomUserInfoDefinition getDefinition() {
        return definition;
    }
}
