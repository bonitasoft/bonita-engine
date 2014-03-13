package org.bonitasoft.engine.identity;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfo {

    private final CustomUserInfoDefinition definition;

    private String value;

    private final long userId;

    public CustomUserInfo(long userId, CustomUserInfoDefinition definition, CustomUserInfoValue value) {
        this.userId = userId;
        this.definition = definition;
        if(value != null) {
            this.value = value.getValue();
        }
    }

    public CustomUserInfoDefinition getDefinition() {
        return definition;
    }

    public long getUserId() {
        return userId;
    }

    public String getValue() {
        return value;
    }
}
