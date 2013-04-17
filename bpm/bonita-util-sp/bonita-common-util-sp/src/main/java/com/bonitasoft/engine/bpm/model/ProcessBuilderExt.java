/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.model;

import org.bonitasoft.engine.bpm.model.ProcessBuilder;
import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;

/**
 * @author Baptiste Mesta
 */
public class ProcessBuilderExt extends ProcessBuilder {

    ProcessBuilderExt(final DesignProcessDefinitionImpl process, final ProcessDefinitionBuilderExt processDefinitionBuilder) {
        super(process, processDefinitionBuilder);
    }

    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder((ProcessDefinitionBuilderExt) getProcessBuilder(), process, parameterName, type);
    }

}
