/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bpm.model;

import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;

/**
 * @author Baptiste Mesta
 */
public class ProcessBuilder extends org.bonitasoft.engine.bpm.model.ProcessBuilder {

    /**
     * @param process
     * @param processDefinitionBuilder
     */
    ProcessBuilder(final DesignProcessDefinitionImpl process, final ProcessDefinitionBuilder processDefinitionBuilder) {
        super(process, processDefinitionBuilder);
    }

    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder((ProcessDefinitionBuilder) getProcessBuilder(), process, parameterName, type);
    }

}
