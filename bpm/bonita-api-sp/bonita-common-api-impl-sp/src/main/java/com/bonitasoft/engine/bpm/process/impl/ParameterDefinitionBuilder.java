/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.DescriptionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;

/**
 * Definition builder for Parameter (specific Bonita BPM Subscription Edition feature).
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see org.bonitasoft.engine.bpm.parameter.ParameterDefinition
 * @version 6.4.1
 * @since 6.0.0
 */
public class ParameterDefinitionBuilder extends ProcessBuilderExt implements DescriptionBuilder {

    private final ParameterDefinitionImpl parameter;

    /**
     * Default Constructor.
     * To build a new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     *
     * @param processDefinitionBuilder
     *        The {@link org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder} to build the
     *        {@link org.bonitasoft.engine.bpm.process.DesignProcessDefinition}
     * @param designProcessDefinitionImpl
     *        The {@link org.bonitasoft.engine.bpm.process.DesignProcessDefinition} where add the new
     *        {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     * @param parameterName
     *        The name of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     * @param type
     *        The type of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     */
    ParameterDefinitionBuilder(final ProcessDefinitionBuilderExt processDefinitionBuilder, final DesignProcessDefinitionImpl designProcessDefinitionImpl,
            final String parameterName, final String type) {
        super(designProcessDefinitionImpl, processDefinitionBuilder);
        parameter = new ParameterDefinitionImpl(parameterName, type);
        designProcessDefinitionImpl.addParameter(parameter);
    }

    @Override
    public ParameterDefinitionBuilder addDescription(final String description) {
        parameter.setDescription(description);
        return this;
    }

}
