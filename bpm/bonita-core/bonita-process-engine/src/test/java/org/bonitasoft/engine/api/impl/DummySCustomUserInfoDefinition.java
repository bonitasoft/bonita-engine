package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;

/**
 * @author Vincent Elcrin
 */
public class DummySCustomUserInfoDefinition implements SCustomUserInfoDefinition {

    private static final long serialVersionUID = 22773173920443547L;

    public static final String MESSAGE = "This is a dummy object!";

    private long id;

    private final String name;

    private final String description;

    public DummySCustomUserInfoDefinition(long id) {
        this(id, "", "");
    }

    public DummySCustomUserInfoDefinition(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getDiscriminator() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void setTenantId(long id) {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
