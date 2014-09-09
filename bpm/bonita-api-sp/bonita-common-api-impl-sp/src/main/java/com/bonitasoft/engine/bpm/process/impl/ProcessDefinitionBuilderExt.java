/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.BusinessDataDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;

/**
 * Bonita BPM Subscription Edition specific builder for process definition.
 * It gives access to Subscription Edition specific features:
 * <ul>
 * <li>Parameters</li>
 * <li>StringIndexes</li>
 * <li>BusinessData</li>
 * </ul>
 *
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public final class ProcessDefinitionBuilderExt extends ProcessDefinitionBuilder {

    @Override
    public ProcessDefinitionBuilderExt createNewInstance(final String name, final String version) {
        super.createNewInstance(name, version);
        return this;
    }

    /**
     * Adds a parameter on this process.
     * @param parameterName parameter name.
     * @param type parameter type (complete class name)
     * @return
     */
    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(this, process, parameterName, type);
    }

    /**
     * Sets the process string index at the given position.
     * @param index position to be set. Valid values are between 1 and 5 (inclusive)
     * @param label label to be displayed for this position
     * @param initialValue expression representing the initial index value
     */
    public void setStringIndex(final int index, final String label, final Expression initialValue) {
        process.setStringIndex(index, label, initialValue);
    }

    /**
     * Adds a Business Data on this process.
     * @param name Business Data name
     * @param className complete name of class defining the Business Data Type
     * @param defaultValue expression representing the default value
     * @return
     */
    public BusinessDataDefinitionBuilder addBusinessData(final String name, final String className, final Expression defaultValue) {
        return new BusinessDataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

}
