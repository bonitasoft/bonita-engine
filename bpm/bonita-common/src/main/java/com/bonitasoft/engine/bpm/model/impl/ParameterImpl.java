/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bpm.model.impl;

import org.bonitasoft.engine.bpm.model.impl.NamedElementImpl;

import com.bonitasoft.engine.bpm.model.ParameterInstance;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterImpl extends NamedElementImpl implements ParameterInstance {

    private static final long serialVersionUID = 4096607590317516470L;

    private final String description;

    private final Object value;

    private final String type;

    public ParameterImpl(final String name, final String description, final Object value, final String type) {
        super(name);
        this.description = description;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

}
