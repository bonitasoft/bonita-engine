package org.bonitasoft.engine.identity;

/**
 * Aggregate information of {@link CustomUserInfoDefinition} and {@link CustomUserInfoValue} 
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

    /**
     * @return the {@link CustomUserInfoDefinition}
     * @since 6.3
     */
    public CustomUserInfoDefinition getDefinition() {
        return definition;
    }

    /**
     * @return the user identifier
     * @since 6.3
     */
    public long getUserId() {
        return userId;
    }

    /**
     * @return the custom user info value
     * @since 6.3
     */
    public String getValue() {
        return value;
    }
}
