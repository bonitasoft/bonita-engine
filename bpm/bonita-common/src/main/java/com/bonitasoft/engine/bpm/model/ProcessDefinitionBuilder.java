/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bpm.model;

/**
 * @author Baptiste Mesta
 */
public final class ProcessDefinitionBuilder extends org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder {

    @Override
    public ProcessDefinitionBuilder createNewInstance(final String name, final String version) {
        super.createNewInstance(name, version);
        return this;
    }

    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(this, process, parameterName, type);
    }

}
