/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public class ProcessBuilderExt extends ProcessBuilder {

    /**
     * Default Constructor.
     * 
     * @param designProcessDefinitionImpl
     *        The {@link DesignProcessDefinition}
     * @param processDefinitionBuilder
     *        The {@link ProcessDefinitionBuilder} to build the {@link DesignProcessDefinition}
     */
    ProcessBuilderExt(final DesignProcessDefinitionImpl designProcessDefinitionImpl, final ProcessDefinitionBuilderExt processDefinitionBuilder) {
        super(designProcessDefinitionImpl, processDefinitionBuilder);
    }

    /**
     * Adds a parameter definition on this process.
     *
     * @param parameterName
     *        The name of the parameter that will be its reference name in the process.
     * @param type
     *        The fully qualified parameter class type (complete class name)
     * @return A reference to the newly created {@link ParameterDefinitionBuilder}
     */
    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder((ProcessDefinitionBuilderExt) getProcessBuilder(), process, parameterName, type);
    }

}
