package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;

/**
 * @author Vincent Elcrin
 */
public class DummySCustomUserInfoDefinition implements SCustomUserInfoDefinition {

    public static final String MESSAGE = "This is a dummy object!";

    private long id;

    private String name;

    private String displayName;

    private String description;

    public DummySCustomUserInfoDefinition(long id) {
        this(id, "", "", "");
    }

    public DummySCustomUserInfoDefinition(long id, String name, String displayName, String description) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
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
