/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SNamedElementImpl;

import com.bonitasoft.engine.bpm.model.ParameterDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SParameterDefinitionImpl extends SNamedElementImpl implements SParameterDefinition {

    private static final long serialVersionUID = -6048365663287821057L;

    private String description;

    private final String type;

    public SParameterDefinitionImpl(final ParameterDefinition parameterDefinition) {
        super(parameterDefinition.getName());
        description = parameterDefinition.getDescription();
        type = parameterDefinition.getType();
    }

    public SParameterDefinitionImpl(final String name, final String type) {
        super(name);
        this.type = type;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
