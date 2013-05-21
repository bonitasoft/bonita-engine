/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;


/**
 * @author Baptiste Mesta
 */
public final class ProcessDefinitionBuilderExt extends ProcessDefinitionBuilder {

    @Override
    public ProcessDefinitionBuilderExt createNewInstance(final String name, final String version) {
        super.createNewInstance(name, version);
        return this;
    }

    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(this, process, parameterName, type);
    }

    public void setStringIndex(final int index, final String label, final Expression initialValue) {
        process.setStringIndex(index, label, initialValue);
    }

}
