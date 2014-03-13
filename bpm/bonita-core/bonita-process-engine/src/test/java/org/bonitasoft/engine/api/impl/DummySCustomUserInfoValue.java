package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;

/**
 * @author Vincent Elcrin
 */
public class DummySCustomUserInfoValue implements SCustomUserInfoValue {

    public static final String MESSAGE = "This is a dummy object!";

    private long userId = -1L;

    private String name;

    private String value = "";

    public DummySCustomUserInfoValue(String name, String value, long userId) {
        this.name = name;
        this.value = value;
        this.userId = userId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public long getId() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public String getDiscriminator() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void setId(long id) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void setTenantId(long id) {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
