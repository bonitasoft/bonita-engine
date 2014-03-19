package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;

/**
 * @author Vincent Elcrin
 */
public class DummySCustomUserInfoValue implements SCustomUserInfoValue {

    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "This is a dummy object!";

    private long userId = -1L;

    private long definitionId;

    private String value = "";

    public DummySCustomUserInfoValue(long definitionId) {
        this(definitionId, "", 1L);
    }

    public DummySCustomUserInfoValue(long definitionId, String value, long userId) {
        this.definitionId = definitionId;
        this.value = value;
        this.userId = userId;
    }

    @Override
    public long getDefinitionId() {
        return definitionId;
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
