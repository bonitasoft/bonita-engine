/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.DescriptionBuilder;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;


/**
 * Definition builder for Parameter (specific Bonita BPM Subscription Edition feature).
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see ParameterDefinition
 */
public class ParameterDefinitionBuilder extends ProcessBuilderExt implements DescriptionBuilder {

    private final ParameterDefinitionImpl parameter;

    ParameterDefinitionBuilder(final ProcessDefinitionBuilderExt processDefinitionBuilder, final DesignProcessDefinitionImpl process, final String parameterName,
            final String type) {
        super(process, processDefinitionBuilder);
        parameter = new ParameterDefinitionImpl(parameterName, type);
        process.addParameter(parameter);
    }

    @Override
    public ParameterDefinitionBuilder addDescription(final String description) {
        parameter.setDescription(description);
        return this;
    }

}
