/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.process.impl.ProcessBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;

/**
 * Bonita BPM Subscription Edition specific builder for process element definition.
 *
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class ProcessBuilderExt extends ProcessBuilder {

    ProcessBuilderExt(final DesignProcessDefinitionImpl process, final ProcessDefinitionBuilderExt processDefinitionBuilder) {
        super(process, processDefinitionBuilder);
    }

    /**
     * Adds a parameter definition on this process.
     *
     * @param parameterName the name of the parameter that will be its reference name in the process.
     * @param type the fully qualified parameter class type (complete class name)
     * @return a reference to the newly created <code>ParameterDefinitionBuilder</code>
     */
    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder((ProcessDefinitionBuilderExt) getProcessBuilder(), process, parameterName, type);
    }

}
