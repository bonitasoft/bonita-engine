package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;

/**
 * @author Vincent Elcrin
 */
public class DummySCustomUserInfoValue implements SCustomUserInfoValue {

    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "This is a dummy object!";

    private long id = 1L;

    private long userId = -1L;

    private final long definitionId;

    private String value = "";

    public DummySCustomUserInfoValue(long id) {
        this(id, 1L, 1L, "");
    }

    public DummySCustomUserInfoValue(long id, long definitionId, long userId, String value) {
        this.id = id;
        this.definitionId = definitionId;
        this.userId = userId;
        this.value = value;
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
        return id;
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
