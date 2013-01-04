/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bpm.model;

import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.bpm.model.impl.ParameterDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterDefinitionBuilder extends ProcessBuilder {

    private final ParameterDefinitionImpl parameter;

    ParameterDefinitionBuilder(final ProcessDefinitionBuilder processDefinitionBuilder, final DesignProcessDefinitionImpl process, final String parameterName,
            final String type) {
        super(process, processDefinitionBuilder);
        parameter = new ParameterDefinitionImpl(parameterName, type);
        process.addParameter(parameter);
    }

    public ParameterDefinitionBuilder addDescription(final String description) {
        parameter.setDescription(description);
        return this;
    }

}
