package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * Aggregate information of {@link CustomUserInfoDefinition} and {@link CustomUserInfoValue}
 *
 * @author Vincent Elcrin
 * @see CustomUserInfoDefinition
 * @see CustomUserInfoValue
 * @since 6.3
 */
public class CustomUserInfo implements BonitaObject {

    private static final long serialVersionUID = 4376121609647215025L;

    private final CustomUserInfoDefinition definition;

    private String value;

    private final long userId;

    /**
     * Default Constructor.
     * 
     * @param userId the user id
     * @param definition the {@link CustomUserInfoDefinition} definition
     * @param value the {@link CustomUserInfoValue} value
     */
    public CustomUserInfo(final long userId, final CustomUserInfoDefinition definition, final CustomUserInfoValue value) {
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
